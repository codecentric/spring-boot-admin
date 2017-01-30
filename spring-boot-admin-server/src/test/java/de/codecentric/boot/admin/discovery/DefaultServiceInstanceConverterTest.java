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

package de.codecentric.boot.admin.discovery;

import de.codecentric.boot.admin.model.Application;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultServiceInstanceConverterTest {

    @Test
    public void test_convert_with_defaults() {
        ServiceInstance service = new DefaultServiceInstance("test", "localhost", 80, false);
        Application application = new DefaultServiceInstanceConverter().convert(service);

        assertThat(application.getId()).isNull();
        assertThat(application.getName()).isEqualTo("test");
        assertThat(application.getServiceUrl()).isEqualTo("http://localhost:80");
        assertThat(application.getManagementUrl()).isEqualTo("http://localhost:80");
        assertThat(application.getHealthUrl()).isEqualTo("http://localhost:80/health");
    }

    @Test
    public void test_convert_with_custom_defaults() {
        DefaultServiceInstanceConverter converter = new DefaultServiceInstanceConverter();
        converter.setHealthEndpointPath("ping");
        converter.setManagementContextPath("mgmt");

        ServiceInstance service = new DefaultServiceInstance("test", "localhost", 80, false);
        Application application = converter.convert(service);

        assertThat(application.getId()).isNull();
        assertThat(application.getName()).isEqualTo("test");
        assertThat(application.getServiceUrl()).isEqualTo("http://localhost:80");
        assertThat(application.getManagementUrl()).isEqualTo("http://localhost:80/mgmt");
        assertThat(application.getHealthUrl()).isEqualTo("http://localhost:80/mgmt/ping");
    }

    @Test
    public void test_convert_with_metadata() {
        ServiceInstance service = new DefaultServiceInstance("test", "localhost", 80, false);
        Map<String, String> metadata = new HashMap<>();
        metadata.put("health.path", "ping");
        metadata.put("management.context-path", "mgmt");
        metadata.put("management.port", "1234");
        service.getMetadata().putAll(metadata);

        Application application = new DefaultServiceInstanceConverter().convert(service);

        assertThat(application.getId()).isNull();
        assertThat(application.getName()).isEqualTo("test");
        assertThat(application.getServiceUrl()).isEqualTo("http://localhost:80");
        assertThat(application.getManagementUrl()).isEqualTo("http://localhost:1234/mgmt");
        assertThat(application.getHealthUrl()).isEqualTo("http://localhost:1234/mgmt/ping");
        assertThat(application.getMetadata()).isEqualTo(metadata);
    }

}
