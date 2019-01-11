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

import lombok.ToString;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Registration info for the instance registers with (including metadata)
 *
 * @author Johannes Edmeier
 */
@lombok.Data
@ToString(exclude = "metadata")
public class Registration implements Serializable {
    private final String name;
    @Nullable
    private final String managementUrl;
    private final String healthUrl;
    @Nullable
    private final String serviceUrl;
    private final String source;
    private final Map<String, String> metadata;

    @lombok.Builder(builderClassName = "Builder", toBuilder = true)
    private Registration(String name,
                         @Nullable String managementUrl,
                         String healthUrl,
                         @Nullable String serviceUrl,
                         String source,
                         @lombok.Singular("metadata") Map<String, String> metadata) {
        Assert.hasText(name, "'name' must not be empty.");
        Assert.hasText(healthUrl, "'healthUrl' must not be empty.");
        Assert.isTrue(checkUrl(healthUrl), "'healthUrl' is not valid: " + healthUrl);
        Assert.isTrue(StringUtils.isEmpty(managementUrl) || checkUrl(managementUrl),
            "'managementUrl' is not valid: " + managementUrl
        );
        Assert.isTrue(StringUtils.isEmpty(serviceUrl) || checkUrl(serviceUrl),
            "'serviceUrl' is not valid: " + serviceUrl
        );
        this.name = name;
        this.managementUrl = managementUrl;
        this.healthUrl = healthUrl;
        this.serviceUrl = serviceUrl;
        this.source = source;
        this.metadata = new LinkedHashMap<>(metadata);
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
}
