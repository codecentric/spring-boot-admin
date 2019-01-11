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

package de.codecentric.boot.admin.server.domain.values;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import com.fasterxml.jackson.annotation.JsonAnyGetter;

/**
 * Represents the info fetched from the info actuator endpoint
 *
 * @author Johannes Edmeier
 */
@lombok.Data
public class Info implements Serializable {
    private static final Info EMPTY = new Info(Collections.emptyMap());

    private final Map<String, Object> values;

    private Info(Map<String, Object> values) {
        if (values.isEmpty()) {
            this.values = Collections.emptyMap();
        } else {
            this.values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
        }
    }

    public static Info from(@Nullable Map<String, Object> values) {
        if (values == null || values.isEmpty()) {
            return empty();
        }
        return new Info(values);
    }

    public static Info empty() {
        return EMPTY;
    }

    @JsonAnyGetter
    public Map<String, Object> getValues() {
        return this.values;
    }
}
