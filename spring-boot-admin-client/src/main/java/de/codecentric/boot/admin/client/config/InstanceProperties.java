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

package de.codecentric.boot.admin.client.config;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@lombok.Data
@ConfigurationProperties(prefix = "spring.boot.admin.client.instance")
public class InstanceProperties {
    /**
     * Management-url to register with. Inferred at runtime, can be overridden in case the
     * reachable URL is different (e.g. Docker).
     */
    @Nullable
    private String managementUrl;

    /**
     * Base url for computing the management-url to register with. The path is inferred at runtime, and appended to the base url.
     */
    @Nullable
    private String managementBaseUrl;

    /**
     * Client-service-URL register with. Inferred at runtime, can be overridden in case the reachable
     * URL is different (e.g. Docker).
     */
    @Nullable
    private String serviceUrl;

    /**
     * Base url for computing the service-url to register with. The path is inferred at runtime, and appended to the base url.
     */
    @Nullable
    private String serviceBaseUrl;

    /**
     * Client-health-URL to register with. Inferred at runtime, can be overridden in case the
     * reachable URL is different (e.g. Docker). Must be unique all services registry.
     */
    @Nullable
    private String healthUrl;

    /**
     * Name to register with. Defaults to ${spring.application.name}
     */
    @Value("${spring.application.name:spring-boot-application}")
    private String name = "spring-boot-application";

    /**
     * Should the registered urls be built with server.address or with hostname.
     */
    private boolean preferIp = false;

    /**
     * Metadata that should be associated with this application
     */
    private Map<String, String> metadata = new LinkedHashMap<>();

}
