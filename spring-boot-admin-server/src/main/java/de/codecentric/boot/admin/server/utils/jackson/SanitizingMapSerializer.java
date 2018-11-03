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

package de.codecentric.boot.admin.server.utils.jackson;


import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;


public class SanitizingMapSerializer extends StdSerializer<Map<String, String>> {
    private static final long serialVersionUID = 1L;
    private final Pattern[] keysToSanitize;

    @SuppressWarnings("unchecked")
    public SanitizingMapSerializer(String[] patterns) {
        super((Class<Map<String, String>>) (Class<?>) Map.class);
        this.keysToSanitize = createPatterns(patterns);
    }

    private static Pattern[] createPatterns(String... keys) {
        return Arrays.stream(keys).map(key -> Pattern.compile(key, Pattern.CASE_INSENSITIVE)).toArray(Pattern[]::new);
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

    @Nullable
    private String sanitize(String key, @Nullable String value) {
        if (value == null) {
            return null;
        }

        boolean matchesAnyPattern = Arrays.stream(this.keysToSanitize)
                                          .anyMatch(pattern -> pattern.matcher(key).matches());
        return matchesAnyPattern ? "******" : value;
    }
}
