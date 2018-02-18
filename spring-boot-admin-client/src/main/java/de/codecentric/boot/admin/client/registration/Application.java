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

package de.codecentric.boot.admin.client.registration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.util.Assert;

/**
 * Contains all informations which is used when this application is registered.
 *
 * @author Johannes Edmeier
 */
@lombok.Data
public class Application {
    private final String name;
    private final String managementUrl;
    private final String healthUrl;
    private final String serviceUrl;
    private final Map<String, String> metadata;

    @lombok.Builder(builderClassName = "Builder")
    protected Application(String name,
                          String managementUrl,
                          String healthUrl,
                          String serviceUrl,
                          @lombok.Singular("metadata") Map<String, String> metadata) {
        Assert.hasText(name, "name must not be empty!");
        Assert.hasText(healthUrl, "healthUrl must not be empty!");
        this.name = name;
        this.managementUrl = managementUrl;
        this.healthUrl = healthUrl;
        this.serviceUrl = serviceUrl;
        this.metadata = new HashMap<>(metadata);
    }

    public static Builder create(String name) {
        return Application.builder().name(name);
    }

    public Map<String, String> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }
}
