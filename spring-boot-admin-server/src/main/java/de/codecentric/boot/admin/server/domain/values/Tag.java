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

import org.springframework.util.Assert;

import java.io.Serializable;

@lombok.Data
public class Tag implements Serializable {
    private final String key;
    private final String value;

    public Tag(String key, String value) {
        Assert.hasText(key, "'key' must not be empty.");
        Assert.hasText(value, "'value' must not be empty.");
        this.key = key;
        this.value = value;
    }

    public static Tag of(String key, String value) {
        return new Tag(key, value);
    }
}
