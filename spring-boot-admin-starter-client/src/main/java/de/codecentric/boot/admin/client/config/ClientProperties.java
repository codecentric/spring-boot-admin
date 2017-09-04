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
package de.codecentric.boot.admin.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@lombok.Data
@ConfigurationProperties(prefix = "spring.boot.admin.client")
public class ClientProperties {

    /**
     * The admin server urls to register at
     */
    private String[] url = new String[]{};

    /**
     * The admin rest-apis path.
     */
    private String apiPath = "instances";

    /**
     * Time interval (in ms) the registration is repeated
     */
    private long period = 10_000L;

    /**
     * Connect timeout (in ms) for the registration.
     */
    private int connectTimeout = 5_000;

    /**
     * Read timeout (in ms) for the registration.
     */
    private int readTimeout = 5_000;

    /**
     * Username for basic authentication on admin server
     */
    private String username;

    /**
     * Password for basic authentication on admin server
     */
    private String password;

    /**
     * Enable automatic deregistration on shutdown
     */
    private boolean autoDeregistration;

    /**
     * Enable automatic registration when the application is ready
     */
    private boolean autoRegistration = true;

    /**
     * Enable registration against one or all admin servers
     */
    private boolean registerOnce = true;

    /**
     * Enable Spring Boot Admin Client.
     */
    private boolean enabled = true;

    public String[] getAdminUrl() {
        String adminUrls[] = url.clone();
        for (int i = 0; i < adminUrls.length; i++) {
            adminUrls[i] += "/" + apiPath;
        }
        return adminUrls;
    }
}
