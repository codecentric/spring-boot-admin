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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@lombok.Data
@ConfigurationProperties(prefix = "spring.boot.admin.client.instance")
public class CloudFoundryInstanceProperties extends InstanceProperties {
    /**
     * Base url for computing the service-url to register with. The path is inferred at runtime, and appended to the base url.
     */
    private String serviceBaseUrl;

    @Override
    @Value("${vcap.application.uris[0]:}")
    public void setServiceBaseUrl(String serviceBaseUrl) {
        if (!StringUtils.isEmpty(serviceBaseUrl)) {
            this.serviceBaseUrl = "http://" + serviceBaseUrl;
        }
    }
}
