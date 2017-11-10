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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;

import static java.util.Collections.singletonMap;

public class LegacyEndpointConverters {
    private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE = new ParameterizedTypeReference<Map<String, Object>>() {
    };
    private static final Jackson2JsonDecoder DECODER = new Jackson2JsonDecoder();
    private static final Jackson2JsonEncoder ENCODER = new Jackson2JsonEncoder();

    public static LegacyEndpointConverter health() {
        return new LegacyEndpointConverter(Endpoint.HEALTH, convertMapUsing(LegacyEndpointConverters::convertHealth));
    }

    public static LegacyEndpointConverter env() {
        return new LegacyEndpointConverter(Endpoint.ENV, convertMapUsing(LegacyEndpointConverters::convertEnv));
    }

    @SuppressWarnings("unchecked")
    private static Function<Flux<DataBuffer>, Flux<DataBuffer>> convertMapUsing(Function<Map<String, Object>, Map<String, Object>> converterFn) {
        return input -> DECODER.decodeToMono(input, ResolvableType.forType(RESPONSE_TYPE), null, null)
                               .map(body -> converterFn.apply((Map<String, Object>) body))
                               .flatMapMany(output -> ENCODER.encode(Mono.just(output), new DefaultDataBufferFactory(),
                                       ResolvableType.forType(RESPONSE_TYPE), null, null));
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

                values.forEach((propKey, propValue) -> {
                    properties.put(propKey, singletonMap("value", propValue));
                });

                Map<String, Object> propertySource = new LinkedHashMap<>();
                propertySource.put("name", key);
                propertySource.put("properties", properties);
                propertySources.add(propertySource);
            }
        });
        converted.put("propertySources", propertySources);

        return converted;
    }

}
