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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Generates InstanceId "cfApplicationGuid:cfInstanceIndex" for CloudFoundry instance
 * Generates an SHA-1 Hash based on the instance health url for non-CloudFoundry instance
 */
public class CloudFoundryInstanceIdGenerator extends HashingInstanceUrlIdGenerator {

    @Override
    public InstanceId generateId(Registration registration) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            String cfApplicationGuid = registration.getMetadata().get("cf_application_guid");
            String cfInstanceIndex = registration.getMetadata().get("cf_instance_index");

            // CloudFoundry instance set InstanceId as "{cfApplicationGuid}:{cfInstanceIndex}"
            if (StringUtils.hasText(cfApplicationGuid) && StringUtils.hasText(cfInstanceIndex)) {
                return InstanceId.of(String.format("%s:%s", cfApplicationGuid, cfInstanceIndex));
            }
            byte[] bytes = digest.digest(registration.getHealthUrl().getBytes(StandardCharsets.UTF_8));
            return InstanceId.of(new String(encodeHex(bytes, 0, 12)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
