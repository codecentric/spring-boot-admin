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

import de.codecentric.boot.admin.client.registration.CloudFoundryApplicationFactory;
import de.codecentric.boot.admin.client.registration.metadata.CloudFoundryMetadataContributor;
import de.codecentric.boot.admin.client.registration.metadata.MetadataContributor;

import de.codecentric.boot.admin.client.utils.InetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnCloudPlatform;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnWebApplication
@ConditionalOnCloudPlatform(CloudPlatform.CLOUD_FOUNDRY)
@Conditional(SpringBootAdminClientEnabledCondition.class)
@EnableConfigurationProperties(CloudFoundryApplicationProperties.class)
@AutoConfigureBefore({SpringBootAdminClientAutoConfiguration.class,UtilAutoConfiguration.class})
public class SpringBootAdminClientCloudFoundryAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public CloudFoundryMetadataContributor cloudFoundryMetadataContributor(CloudFoundryApplicationProperties cloudFoundryApplicationProperties) {
        return new CloudFoundryMetadataContributor(cloudFoundryApplicationProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public CloudFoundryApplicationFactory applicationFactory(@Autowired(required = false) InetUtils inetUtils, InstanceProperties instance,
                                                             ManagementServerProperties management,
                                                             ServerProperties server,
                                                             PathMappedEndpoints pathMappedEndpoints,
                                                             WebEndpointProperties webEndpoint,
                                                             MetadataContributor metadataContributor,
                                                             CloudFoundryApplicationProperties cfApplicationProperties) {
        return new CloudFoundryApplicationFactory(inetUtils,instance, management, server, pathMappedEndpoints, webEndpoint,
            metadataContributor, cfApplicationProperties);
    }
}
