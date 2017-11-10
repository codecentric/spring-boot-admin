/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.server.web.client;

import de.codecentric.boot.admin.server.domain.values.Endpoint;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class LegacyEndpointConverters {
    private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE_MAP = new ParameterizedTypeReference<Map<String, Object>>() {
    };
    private static final ParameterizedTypeReference<List<Object>> RESPONSE_TYPE_LIST = new ParameterizedTypeReference<List<Object>>() {
    };
    private static final Jackson2JsonDecoder DECODER;
    private static final Jackson2JsonEncoder ENCODER;

    static {
        ObjectMapper om = Jackson2ObjectMapperBuilder.json()
                                                     .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                                                     .build();
        DECODER = new Jackson2JsonDecoder(om);
        ENCODER = new Jackson2JsonEncoder(om);
    }

    public static LegacyEndpointConverter health() {
        return new LegacyEndpointConverter(Endpoint.HEALTH,
                convertUsing(RESPONSE_TYPE_MAP, RESPONSE_TYPE_MAP, LegacyEndpointConverters::convertHealth));
    }

    public static LegacyEndpointConverter env() {
        return new LegacyEndpointConverter(Endpoint.ENV,
                convertUsing(RESPONSE_TYPE_MAP, RESPONSE_TYPE_MAP, LegacyEndpointConverters::convertEnv));
    }

    public static LegacyEndpointConverter trace() {
        return new LegacyEndpointConverter(Endpoint.TRACE,
                convertUsing(RESPONSE_TYPE_LIST, RESPONSE_TYPE_MAP, LegacyEndpointConverters::convertTrace));
    }

    public static LegacyEndpointConverter threaddump() {
        return new LegacyEndpointConverter(Endpoint.THREADDUMP,
                convertUsing(RESPONSE_TYPE_LIST, RESPONSE_TYPE_MAP, LegacyEndpointConverters::convertThreaddump));
    }

    public static LegacyEndpointConverter liquibase() {
        return new LegacyEndpointConverter(Endpoint.LIQUIBASE,
                convertUsing(RESPONSE_TYPE_LIST, RESPONSE_TYPE_MAP, LegacyEndpointConverters::convertLiquibase));
    }

    public static LegacyEndpointConverter flyway() {
        return new LegacyEndpointConverter(Endpoint.FLYWAY,
                convertUsing(RESPONSE_TYPE_LIST, RESPONSE_TYPE_MAP, LegacyEndpointConverters::convertFlyway));
    }

    @SuppressWarnings("unchecked")
    private static <S, T> Function<Flux<DataBuffer>, Flux<DataBuffer>> convertUsing(ParameterizedTypeReference<S> sourceType,
                                                                                    ParameterizedTypeReference<T> targetType,
                                                                                    Function<S, T> converterFn) {
        return input -> DECODER.decodeToMono(input, ResolvableType.forType(sourceType), null, null)
                               .map(body -> converterFn.apply((S) body))
                               .flatMapMany(output -> ENCODER.encode(Mono.just(output), new DefaultDataBufferFactory(),
                                       ResolvableType.forType(targetType), null, null));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> convertHealth(Map<String, Object> body) {
        Map<String, Object> converted = new LinkedHashMap<>();
        Map<String, Object> details = new LinkedHashMap<>();

        body.forEach((key, value) -> {
            if ("status".equals(key)) {
                converted.put(key, value);
            } else if (value instanceof Map) {
                details.put(key, convertHealth((Map<String, Object>) value));
            } else {
                details.put(key, value);
            }
        });
        if (!details.isEmpty()) {
            converted.put("details", details);
        }

        return converted;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> convertEnv(Map<String, Object> body) {
        Map<String, Object> converted = new LinkedHashMap<>();
        List<Map<String, Object>> propertySources = new ArrayList<>(body.size());

        body.forEach((key, value) -> {
            if ("profiles".equals(key)) {
                converted.put("activeProfiles", value);
            } else if (value instanceof Map) {
                Map<String, Object> values = (Map<String, Object>) value;

                Map<String, Object> properties = new LinkedHashMap<>();
                values.forEach((propKey, propValue) -> properties.put(propKey, singletonMap("value", propValue)));

                Map<String, Object> propertySource = new LinkedHashMap<>();
                propertySource.put("name", key);
                propertySource.put("properties", properties);
                propertySources.add(propertySource);
            }
        });
        converted.put("propertySources", propertySources);

        return converted;
    }

    private static Map<String, Object> convertTrace(List<Object> traces) {
        return singletonMap("traces", traces);
    }

    private static Map<String, Object> convertThreaddump(List<Object> threads) {
        return singletonMap("threads", threads);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> convertLiquibase(List<Object> reports) {
        return reports.stream()
                      .filter(Map.class::isInstance)
                      .map(r -> (Map<String, Object>) r)
                      .collect(toMap(r -> (String) r.get("name"), r -> singletonMap("changeSets",
                              LegacyEndpointConverters.convertLiquibaseChangesets(
                                      (List<Map<String, Object>>) r.get("changeLogs")))));
    }

    private static List<Map<String, Object>> convertLiquibaseChangesets(List<Map<String, Object>> changeSets) {
        return changeSets.stream().map(changeset -> {
            Map<String, Object> converted = new LinkedHashMap<>();
            converted.put("id", changeset.get("ID"));
            converted.put("author", changeset.get("AUTHOR"));
            converted.put("changeLog", changeset.get("FILENAME"));
            if (changeset.get("DATEEXECUTED") instanceof Long) {
                converted.put("dateExecuted", new Date((Long) changeset.get("DATEEXECUTED")));
            }
            converted.put("orderExecuted", changeset.get("ORDEREXECUTED"));
            converted.put("execType", changeset.get("EXECTYPE"));
            converted.put("checksum", changeset.get("MD5SUM"));
            converted.put("description", changeset.get("DESCRIPTION"));
            converted.put("comments", changeset.get("COMMENTS"));
            converted.put("tag", changeset.get("TAG"));
            converted.put("contexts", changeset.get("CONTEXTS") instanceof String ?
                    new LinkedHashSet<>(asList(((String) changeset.get("CONTEXTS")).split(",\\s*"))) :
                    emptySet());
            converted.put("labels", changeset.get("LABELS") instanceof String ?
                    new LinkedHashSet<>(asList(((String) changeset.get("LABELS")).split(",\\s*"))) :
                    emptySet());
            converted.put("deploymentId", changeset.get("DEPLOYMENT_ID"));
            return converted;
        }).collect(toList());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> convertFlyway(List<Object> reports) {
        return reports.stream()
                      .filter(Map.class::isInstance)
                      .map(r -> (Map<String, Object>) r)
                      .collect(toMap(r -> (String) r.get("name"), r -> singletonMap("migrations",
                              LegacyEndpointConverters.convertFlywayMigrations(
                                      (List<Map<String, Object>>) r.get("migrations")))));
    }

    private static List<Map<String, Object>> convertFlywayMigrations(List<Map<String, Object>> migrations) {
        return migrations.stream().map(migration -> {
            Map<String, Object> converted = new LinkedHashMap<>(migration);
            if (migration.get("installedOn") instanceof Long) {
                converted.put("installedOn", new Date((Long) migration.get("installedOn")));
            }
            return converted;
        }).collect(toList());
    }
}
