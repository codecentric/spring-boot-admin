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

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;

/**
 * Represents the info fetched from the info actuator endpoint
 *
 * @author Johannes Edmeier
 */
@lombok.Data
public class Info implements Serializable {
    private static final Info EMPTY = new Info(null);

    private final Map<String, Object> values;

    private Info(Map<String, Object> values) {
        this.values = values != null ? new LinkedHashMap<>(values) : Collections.emptyMap();
    }

    public static Info from(Map<String, Object> values) {
        return new Info(values);
    }

    public static Info empty() {
        return EMPTY;
    }

    public String getVersion() {
        Object build = this.values.get("build");
        if (build instanceof Map) {
            Object version = ((Map<?, ?>) build).get("version");
            if (version instanceof String) {
                return (String) version;
            }
        }

        Object version = this.values.get("version");
        if (version instanceof String) {
            return (String) version;
        }
        return null;
    }

    @JsonAnyGetter
    public Map<String, Object> getValues() {
        return Collections.unmodifiableMap(values);
    }
}
