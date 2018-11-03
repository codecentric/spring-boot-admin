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

import de.codecentric.boot.admin.client.config.CloudFoundryApplicationProperties;
import de.codecentric.boot.admin.client.config.InstanceProperties;
import de.codecentric.boot.admin.client.registration.metadata.MetadataContributor;

import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.autoconfigure.web.ServerProperties;

public class CloudFoundryApplicationFactory extends DefaultApplicationFactory {
    private final CloudFoundryApplicationProperties cfApplicationProperties;

    public CloudFoundryApplicationFactory(InstanceProperties instance,
                                          ManagementServerProperties management,
                                          ServerProperties server,
                                          PathMappedEndpoints pathMappedEndpoints,
                                          WebEndpointProperties webEndpoint,
                                          MetadataContributor metadataContributor,
                                          CloudFoundryApplicationProperties cfApplicationProperties) {
        super(instance, management, server, pathMappedEndpoints, webEndpoint, metadataContributor);
        this.cfApplicationProperties = cfApplicationProperties;
    }

    @Override
    protected String getServiceBaseUrl() {
        if (cfApplicationProperties.getUris().isEmpty()) {
            return super.getServiceBaseUrl();
        }

        String uri = cfApplicationProperties.getUris().get(0);
        return "http://" + uri;
    }
}
