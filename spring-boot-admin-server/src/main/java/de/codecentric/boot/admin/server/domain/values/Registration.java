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

package de.codecentric.boot.admin.server.domain.values;

import lombok.ToString;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Registration info for the instance registers with (inluding metadata)
 *
 * @author Johannes Edmeier
 */
@lombok.Data
@JsonDeserialize(using = Registration.Deserializer.class)
@ToString(exclude = "metadata")
public class Registration implements Serializable {
    private final String name;
    private final String managementUrl;
    private final String healthUrl;
    private final String serviceUrl;
    private final String source;
    @JsonSerialize(using = Registration.MetadataSerializer.class)
    private final Map<String, String> metadata;

    @lombok.Builder(builderClassName = "Builder", toBuilder = true)
    private Registration(String name,
                         String managementUrl,
                         String healthUrl,
                         String serviceUrl,
                         String source,
                         @lombok.Singular("metadata") Map<String, String> metadata) {
        Assert.hasText(name, "Name must not be null");
        Assert.hasText(healthUrl, "Health-URL must not be null");
        Assert.isTrue(checkUrl(healthUrl), "Health-URL is not valid");
        Assert.isTrue(StringUtils.isEmpty(managementUrl) || checkUrl(managementUrl), "Management-URL is not valid");
        Assert.isTrue(StringUtils.isEmpty(serviceUrl) || checkUrl(serviceUrl), "Service-URL is not valid");
        this.name = name;
        this.managementUrl = managementUrl;
        this.healthUrl = healthUrl;
        this.serviceUrl = serviceUrl;
        this.source = source;
        this.metadata = new HashMap<>(metadata);
    }

    public static Registration.Builder create(String name, String healthUrl) {
        return builder().name(name).healthUrl(healthUrl);
    }

    public static Registration.Builder copyOf(Registration registration) {
        return registration.toBuilder();
    }

    public Map<String, String> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    /**
     * Checks the syntax of the given URL.
     *
     * @param url The URL.
     * @return true, if valid.
     */
    private boolean checkUrl(String url) {
        try {
            URI uri = new URI(url);
            return uri.isAbsolute();
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public static class MetadataSerializer extends StdSerializer<Map<String, String>> {
        private static final long serialVersionUID = 1L;
        private static Pattern[] keysToSanitize = createPatterns(".*password$", ".*secret$", ".*key$", ".*$token$",
                ".*credentials.*", ".*vcap_services$");

        @SuppressWarnings("unchecked")
        public MetadataSerializer() {
            super((Class<Map<String, String>>) (Class<?>) Map.class);
        }

        private static Pattern[] createPatterns(String... keys) {
            Pattern[] patterns = new Pattern[keys.length];
            for (int i = 0; i < keys.length; i++) {
                patterns[i] = Pattern.compile(keys[i]);
            }
            return patterns;
        }

        @Override
        public void serialize(Map<String, String> value,
                              JsonGenerator gen,
                              SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            for (Map.Entry<String, String> entry : value.entrySet()) {
                gen.writeStringField(entry.getKey(), sanitize(entry.getKey(), entry.getValue()));
            }
            gen.writeEndObject();
        }

        private String sanitize(String key, String value) {
            for (Pattern pattern : MetadataSerializer.keysToSanitize) {
                if (pattern.matcher(key).matches()) {
                    return (value == null ? null : "******");
                }
            }
            return value;
        }
    }

    public static class Deserializer extends StdDeserializer<Registration> {
        protected Deserializer() {
            super(Registration.class);
        }

        @Override
        public Registration deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.readValueAsTree();
            Registration.Builder builder = Registration.builder();

            if (node.has("name")) {
                builder.name(node.get("name").asText());
            }
            if (node.has("url")) {
                String url = node.get("url").asText();
                builder.healthUrl(url.replaceFirst("/+$", "") + "/health").managementUrl(url);
            } else {
                if (node.has("healthUrl")) {
                    builder.healthUrl(node.get("healthUrl").asText());
                }
                if (node.has("managementUrl")) {
                    builder.managementUrl(node.get("managementUrl").asText());
                }
                if (node.has("serviceUrl")) {
                    builder.serviceUrl(node.get("serviceUrl").asText());
                }
            }

            if (node.has("metadata")) {
                Iterator<Map.Entry<String, JsonNode>> it = node.get("metadata").fields();
                while (it.hasNext()) {
                    Map.Entry<String, JsonNode> entry = it.next();
                    builder.metadata(entry.getKey(), entry.getValue().asText());
                }
            }
            return builder.build();
        }
    }
}
