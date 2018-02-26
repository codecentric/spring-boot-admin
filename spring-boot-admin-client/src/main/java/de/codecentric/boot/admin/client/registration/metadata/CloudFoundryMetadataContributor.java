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

package de.codecentric.boot.admin.client.registration.metadata;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class CloudFoundryMetadataContributor implements MetadataContributor {

    @Value("${vcap.application.application_id}")
    private String applicationId;

    @Value("${vcap.application.instance_index}")
    private String instanceId;

    @Value("${vcap.application.uris[0]}")
    private String serviceBaseUrl;

    @Override
    public Map<String, String> getMetadata() {
        Map<String, String> map = new HashMap<>();
        String applicationId = this.applicationId;
        String instanceId = this.instanceId;
        String serviceBaseUrl = this.serviceBaseUrl;
        if (StringUtils.hasText(applicationId) && StringUtils.hasText(instanceId) && StringUtils.hasText(serviceBaseUrl)) {
            map.put("applicationId", applicationId);
            map.put("instanceId", instanceId);

            // Connecting to CloudFoundry instance requires to set the CloudFoundry routes as access URL.
            map.put("service-base-url", serviceBaseUrl);

            // CloudFoundry is Ephemeral Environment thus it should unregister itself when shutdown.
            map.put("auto-deregistration", "true");
        }
        return map;
    }
}
