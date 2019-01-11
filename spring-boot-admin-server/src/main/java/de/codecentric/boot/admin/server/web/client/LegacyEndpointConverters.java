/*
 * Copyright 2014-2018 the original author or authors.
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

import java.time.DateTimeException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nullable;
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
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class LegacyEndpointConverters {
    private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE_MAP = new ParameterizedTypeReference<Map<String, Object>>() {
    };
    private static final ParameterizedTypeReference<List<Object>> RESPONSE_TYPE_LIST = new ParameterizedTypeReference<List<Object>>() {
    };
    private static final ParameterizedTypeReference<List<Map<String, Object>>> RESPONSE_TYPE_LIST_MAP = new ParameterizedTypeReference<List<Map<String, Object>>>() {
    };
    private static final Jackson2JsonDecoder DECODER;
    private static final Jackson2JsonEncoder ENCODER;
    private static final DateTimeFormatter TIMESTAMP_PATTERN = DateTimeFormatter.ofPattern(
        "yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    static {
        ObjectMapper om = Jackson2ObjectMapperBuilder.json()
                                                     .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                                                     .build();
        DECODER = new Jackson2JsonDecoder(om);
        ENCODER = new Jackson2JsonEncoder(om);
    }

    private LegacyEndpointConverters() {
    }

    public static LegacyEndpointConverter health() {
        return new LegacyEndpointConverter(Endpoint.HEALTH,
            convertUsing(RESPONSE_TYPE_MAP, RESPONSE_TYPE_MAP, LegacyEndpointConverters::convertHealth));
    }

    public static LegacyEndpointConverter env() {
        return new LegacyEndpointConverter(Endpoint.ENV,
            convertUsing(RESPONSE_TYPE_MAP, RESPONSE_TYPE_MAP, LegacyEndpointConverters::convertEnv));
    }

    public static LegacyEndpointConverter httptrace() {
        return new LegacyEndpointConverter(Endpoint.HTTPTRACE,
            convertUsing(RESPONSE_TYPE_LIST_MAP, RESPONSE_TYPE_MAP, LegacyEndpointConverters::convertHttptrace));
    }

    public static LegacyEndpointConverter threaddump() {
        return new LegacyEndpointConverter(Endpoint.THREADDUMP,
            convertUsing(RESPONSE_TYPE_LIST, RESPONSE_TYPE_MAP, LegacyEndpointConverters::convertThreaddump));
    }

    public static LegacyEndpointConverter liquibase() {
        return new LegacyEndpointConverter(Endpoint.LIQUIBASE,
            convertUsing(RESPONSE_TYPE_LIST_MAP, RESPONSE_TYPE_MAP, LegacyEndpointConverters::convertLiquibase));
    }

    public static LegacyEndpointConverter flyway() {
        return new LegacyEndpointConverter(Endpoint.FLYWAY,
            convertUsing(RESPONSE_TYPE_LIST_MAP, RESPONSE_TYPE_MAP, LegacyEndpointConverters::convertFlyway));
    }

    public static LegacyEndpointConverter info() {
        return new LegacyEndpointConverter(Endpoint.INFO, flux -> flux);
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

    private static Map<String, Object> convertHttptrace(List<Map<String, Object>> traces) {
        return singletonMap("traces",
            traces.stream().sequential().map(LegacyEndpointConverters::convertHttptrace).collect(toList()));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> convertHttptrace(Map<String, Object> in) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("timestamp", getInstant(in.get("timestamp")));
        Map<String, Object> in_info = (Map<String, Object>) in.get("info");
        if (in_info != null) {
            Map<String, Object> request = new LinkedHashMap<>();
            request.put("method", in_info.get("method"));
            request.put("uri", in_info.get("path"));
            out.put("request", request);

            Map<String, Object> response = new LinkedHashMap<>();

            Map<String, Object> in_headers = (Map<String, Object>) in_info.get("headers");
            if (in_headers != null) {
                Map<String, Object> in_request_headers = (Map<String, Object>) in_headers.get("request");
                if (in_request_headers != null) {
                    Map<String, Object> requestHeaders = new LinkedHashMap<>();
                    in_request_headers.forEach((k, v) -> requestHeaders.put(k, singletonList(v)));
                    request.put("headers", requestHeaders);
                }

                Map<String, Object> in_response_headers = (Map<String, Object>) in_headers.get("response");
                if (in_response_headers != null) {
                    if (in_response_headers.get("status") instanceof String) {
                        response.put("status", Long.valueOf(in_response_headers.get("status").toString()));
                    }

                    Map<String, Object> responseHeaders = new LinkedHashMap<>();
                    in_response_headers.forEach((k, v) -> responseHeaders.put(k, singletonList(v)));
                    responseHeaders.remove("status");
                    response.put("headers", responseHeaders);
                }
            }

            out.put("response", response);
            if (in_info.get("timeTaken") instanceof String) {
                out.put("timeTaken", Long.valueOf(in_info.get("timeTaken").toString()));
            }
        }
        return out;
    }

    private static Map<String, Object> convertThreaddump(List<Object> threads) {
        return singletonMap("threads", threads);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> convertLiquibase(List<Map<String, Object>> reports) {
        Map<String, Object> liquibaseBeans = reports.stream()
                                                    .sequential()
                                                    .collect(toMap(r -> (String) r.get("name"),
                                                        r -> singletonMap("changeSets",
                                                            LegacyEndpointConverters.convertLiquibaseChangesets(
                                                                (List<Map<String, Object>>) r.get("changeLogs")))));

        return singletonMap("contexts", singletonMap("application", singletonMap("liquibaseBeans", liquibaseBeans)));
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
    private static Map<String, Object> convertFlyway(List<Map<String, Object>> reports) {
        Map<String, Object> flywayBeans = reports.stream()
                                                 .sequential()
                                                 .collect(toMap(r -> (String) r.get("name"),
                                                     r -> singletonMap("migrations",
                                                         LegacyEndpointConverters.convertFlywayMigrations(
                                                             (List<Map<String, Object>>) r.get("migrations")))));
        return singletonMap("contexts", singletonMap("application", singletonMap("flywayBeans", flywayBeans)));
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

    @Nullable
    private static Instant getInstant(Object o) {
        try {
            if (o instanceof String) {
                return OffsetDateTime.parse((String) o, TIMESTAMP_PATTERN).toInstant();
            } else if (o instanceof Long) {
                return Instant.ofEpochMilli((Long) o);
            }
        } catch (DateTimeException | ClassCastException e) {
            return null;
        }
        return null;
    }
}
