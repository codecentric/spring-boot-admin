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

package de.codecentric.boot.admin.server.services;

import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;

import org.springframework.util.StringUtils;

/**
 * Generates CF instance uniqueId "applicationId:instanceId" for CloudFoundry instance.
 * Uses a fallback InstanceIdGenerator when the metadata isn't present.
 *
 * @author Tetsushi Awano
 */
public class CloudFoundryInstanceIdGenerator implements InstanceIdGenerator {
    private final InstanceIdGenerator fallbackIdGenerator;

    public CloudFoundryInstanceIdGenerator(InstanceIdGenerator fallbackIdGenerator) {
        this.fallbackIdGenerator = fallbackIdGenerator;
    }

    @Override
    public InstanceId generateId(Registration registration) {
        String applicationId = registration.getMetadata().get("applicationId");
        String instanceId = registration.getMetadata().get("instanceId");

        if (StringUtils.hasText(applicationId) && StringUtils.hasText(instanceId)) {
            return InstanceId.of(String.format("%s:%s", applicationId, instanceId));
        }
        return fallbackIdGenerator.generateId(registration);
    }
}
