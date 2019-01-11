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

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.actuate.endpoint.EndpointId;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.autoconfigure.web.ServerProperties;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CloudFoundryApplicationFactoryTest {
    private InstanceProperties instanceProperties = new InstanceProperties();
    private ServerProperties server = new ServerProperties();
    private ManagementServerProperties management = new ManagementServerProperties();
    private PathMappedEndpoints pathMappedEndpoints = mock(PathMappedEndpoints.class);
    private WebEndpointProperties webEndpoint = new WebEndpointProperties();
    private CloudFoundryApplicationProperties cfApplicationProperties = new CloudFoundryApplicationProperties();
    private CloudFoundryApplicationFactory factory = new CloudFoundryApplicationFactory(instanceProperties, management,
        server, pathMappedEndpoints, webEndpoint, () -> singletonMap("contributor", "test"), cfApplicationProperties);

    @Before
    public void setup() {
        instanceProperties.setName("test");
    }

    @Test
    public void should_use_application_uri() {
        when(pathMappedEndpoints.getPath(EndpointId.of("health"))).thenReturn("/actuator/health");
        cfApplicationProperties.setUris(singletonList("application/Uppercase"));

        Application app = factory.createApplication();

        assertThat(app.getManagementUrl()).isEqualTo("http://application/Uppercase/actuator");
        assertThat(app.getHealthUrl()).isEqualTo("http://application/Uppercase/actuator/health");
        assertThat(app.getServiceUrl()).isEqualTo("http://application/Uppercase/");
    }

}
