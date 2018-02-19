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

package de.codecentric.boot.admin.server.config;

import de.codecentric.boot.admin.server.services.CloudFoundryInstanceIdGenerator;
import de.codecentric.boot.admin.server.services.HashingInstanceUrlIdGenerator;
import de.codecentric.boot.admin.server.services.InstanceIdGenerator;
import de.codecentric.boot.admin.server.web.client.CloudFoundryHttpHeaderProvider;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnCloudPlatform;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnCloudPlatform(CloudPlatform.CLOUD_FOUNDRY)
@AutoConfigureBefore({AdminServerAutoConfiguration.class})
public class AdminServerCloudFoundryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public InstanceIdGenerator instanceIdGenerator() {
        return new CloudFoundryInstanceIdGenerator(new HashingInstanceUrlIdGenerator());
    }

    @Bean
    @ConditionalOnMissingBean
    public CloudFoundryHttpHeaderProvider cloudFoundryHttpHeaderProvider() {
        return new CloudFoundryHttpHeaderProvider();
    }
}
