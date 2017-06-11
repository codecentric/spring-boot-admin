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

package de.codecentric.boot.admin.server.discovery;

import de.codecentric.boot.admin.server.model.Application;

import java.net.URI;
import org.junit.Test;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient.EurekaServiceInstance;
import com.netflix.appinfo.InstanceInfo;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EurekaServiceInstanceConverterTest {

    @Test
    public void convert_secure() {
        InstanceInfo instanceInfo = mock(InstanceInfo.class);
        when(instanceInfo.getSecureHealthCheckUrl()).thenReturn("");
        when(instanceInfo.getHealthCheckUrl()).thenReturn("http://localhost:80/mgmt/ping");
        EurekaServiceInstance service = mock(EurekaServiceInstance.class);
        when(service.getInstanceInfo()).thenReturn(instanceInfo);
        when(service.getUri()).thenReturn(URI.create("http://localhost:80"));
        when(service.getServiceId()).thenReturn("test");
        when(service.getMetadata()).thenReturn(singletonMap("management.context-path", "/mgmt"));

        Application application = new EurekaServiceInstanceConverter().convert(service);

        assertThat(application.getId()).isNull();
        assertThat(application.getName()).isEqualTo("test");
        assertThat(application.getServiceUrl()).isEqualTo("http://localhost:80");
        assertThat(application.getManagementUrl()).isEqualTo("http://localhost:80/mgmt");
        assertThat(application.getHealthUrl()).isEqualTo("http://localhost:80/mgmt/ping");
    }

    @Test
    public void convert_missing_mgmtpath() {
        InstanceInfo instanceInfo = mock(InstanceInfo.class);
        when(instanceInfo.getHealthCheckUrl()).thenReturn("http://localhost:80/mgmt/ping");
        EurekaServiceInstance service = mock(EurekaServiceInstance.class);
        when(service.getInstanceInfo()).thenReturn(instanceInfo);
        when(service.getUri()).thenReturn(URI.create("http://localhost:80"));
        when(service.getServiceId()).thenReturn("test");

        Application application = new EurekaServiceInstanceConverter().convert(service);

        assertThat(application.getManagementUrl()).isEqualTo("http://localhost:80");
    }

    @Test
    public void convert_secure_healthUrl() {
        InstanceInfo instanceInfo = mock(InstanceInfo.class);
        when(instanceInfo.getSecureHealthCheckUrl()).thenReturn("https://localhost:80/health");
        EurekaServiceInstance service = mock(EurekaServiceInstance.class);
        when(service.getInstanceInfo()).thenReturn(instanceInfo);
        when(service.getUri()).thenReturn(URI.create("http://localhost:80"));
        when(service.getServiceId()).thenReturn("test");

        Application application = new EurekaServiceInstanceConverter().convert(service);

        assertThat(application.getHealthUrl()).isEqualTo("https://localhost:80/health");
    }
}
