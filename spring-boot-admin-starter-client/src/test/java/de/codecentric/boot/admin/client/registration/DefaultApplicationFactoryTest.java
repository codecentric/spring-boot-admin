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

package de.codecentric.boot.admin.client.registration;

import de.codecentric.boot.admin.client.config.InstanceProperties;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.server.Ssl;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultApplicationFactoryTest {
    private InstanceProperties instanceProperties = new InstanceProperties();
    private ServerProperties server = new ServerProperties();
    private ManagementServerProperties management = new ManagementServerProperties();
    private DefaultApplicationFactory factory = new DefaultApplicationFactory(instanceProperties, management, server,
            "/health");

    @Before
    public void setup() {
        instanceProperties.setName("test");
    }

    @Test
    public void test_mgmtPortPath() {
        management.setContextPath("/admin");
        DefaultApplicationFactory factory = new DefaultApplicationFactory(instanceProperties, management, server,
                "/alive");

        publishApplicationReadyEvent(factory, 8080, 8081);

        Application app = factory.createApplication();
        assertThat(app.getManagementUrl()).isEqualTo("http://" + getHostname() + ":8081/admin/");
        assertThat(app.getHealthUrl()).isEqualTo("http://" + getHostname() + ":8081/admin/alive/");
        assertThat(app.getServiceUrl()).isEqualTo("http://" + getHostname() + ":8080/");
    }

    @Test
    public void test_default() {
        publishApplicationReadyEvent(factory, 8080, null);

        Application app = factory.createApplication();
        assertThat(app.getManagementUrl()).isEqualTo("http://" + getHostname() + ":8080/application/");
        assertThat(app.getHealthUrl()).isEqualTo("http://" + getHostname() + ":8080/application/health/");
        assertThat(app.getServiceUrl()).isEqualTo("http://" + getHostname() + ":8080/");
    }

    @Test
    public void test_ssl() {
        server.setSsl(new Ssl());
        server.getSsl().setEnabled(true);
        publishApplicationReadyEvent(factory, 8080, null);

        Application app = factory.createApplication();
        assertThat(app.getManagementUrl()).isEqualTo("https://" + getHostname() + ":8080/application/");
        assertThat(app.getHealthUrl()).isEqualTo("https://" + getHostname() + ":8080/application/health/");
        assertThat(app.getServiceUrl()).isEqualTo("https://" + getHostname() + ":8080/");
    }

    @Test
    public void test_ssl_management() {
        management.setSsl(new Ssl());
        management.getSsl().setEnabled(true);
        publishApplicationReadyEvent(factory, 8080, 9090);

        Application app = factory.createApplication();
        assertThat(app.getManagementUrl()).isEqualTo("https://" + getHostname() + ":9090/application/");
        assertThat(app.getHealthUrl()).isEqualTo("https://" + getHostname() + ":9090/application/health/");
        assertThat(app.getServiceUrl()).isEqualTo("http://" + getHostname() + ":8080/");
    }

    @Test
    public void test_preferIpAddress_serveraddress_missing() {
        instanceProperties.setPreferIp(true);
        publishApplicationReadyEvent(factory, 8080, null);

        Application app = factory.createApplication();
        assertThat(app.getServiceUrl()).matches("http://\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}:8080/");
    }

    @Test
    public void test_preferIpAddress_managementaddress_missing() {
        instanceProperties.setPreferIp(true);
        publishApplicationReadyEvent(factory, 8080, 8081);

        Application app = factory.createApplication();
        assertThat(app.getManagementUrl()).matches(
                "http://\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}:8081/application/");
    }

    @Test
    public void test_preferIpAddress() throws UnknownHostException {
        instanceProperties.setPreferIp(true);
        server.setAddress(InetAddress.getByName("127.0.0.1"));
        management.setAddress(InetAddress.getByName("127.0.0.2"));
        publishApplicationReadyEvent(factory, 8080, 8081);

        Application app = factory.createApplication();
        assertThat(app.getManagementUrl()).isEqualTo("http://127.0.0.2:8081/application/");
        assertThat(app.getHealthUrl()).isEqualTo("http://127.0.0.2:8081/application/health/");
        assertThat(app.getServiceUrl()).isEqualTo("http://127.0.0.1:8080/");
    }

    @Test
    public void test_allcustom() {
        instanceProperties.setHealthUrl("http://health");
        instanceProperties.setManagementUrl("http://management");
        instanceProperties.setServiceUrl("http://service");

        Application app = factory.createApplication();
        assertThat(app.getServiceUrl()).isEqualTo("http://service");
        assertThat(app.getManagementUrl()).isEqualTo("http://management");
        assertThat(app.getHealthUrl()).isEqualTo("http://health");
    }

    @Test
    public void test_all_baseUrls() {
        instanceProperties.setManagementBaseUrl("http://management:8090");
        instanceProperties.setServiceBaseUrl("http://service:80");
        management.setContextPath("/admin");

        Application app = factory.createApplication();
        assertThat(app.getServiceUrl()).isEqualTo("http://service:80/");
        assertThat(app.getManagementUrl()).isEqualTo("http://management:8090/admin/");
        assertThat(app.getHealthUrl()).isEqualTo("http://management:8090/admin/health/");
    }

    @Test
    public void test_service_baseUrl() {
        instanceProperties.setServiceBaseUrl("http://service:80");
        management.setContextPath("/admin");

        Application app = factory.createApplication();
        assertThat(app.getServiceUrl()).isEqualTo("http://service:80/");
        assertThat(app.getManagementUrl()).isEqualTo("http://service:80/admin/");
        assertThat(app.getHealthUrl()).isEqualTo("http://service:80/admin/health/");
    }

    @Test
    public void test_missingports() {
        assertThatThrownBy(() -> factory.createApplication()).isInstanceOf(IllegalStateException.class)
                                                             .hasMessageContaining("service-base-url");
    }

    private String getHostname() {
        try {
            return InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            throw new IllegalStateException(e);
        }
    }

    private void publishApplicationReadyEvent(DefaultApplicationFactory factory,
                                              Integer serverport,
                                              Integer managementport) {
        MockEnvironment env = new MockEnvironment();
        if (serverport != null) {
            env.setProperty("local.server.port", serverport.toString());
        }
        if (managementport != null) {
            env.setProperty("local.management.port", managementport.toString());
        }

        ConfigurableWebApplicationContext context = mock(ConfigurableWebApplicationContext.class);
        when(context.getEnvironment()).thenReturn(env);
        factory.onApplicationReady(new ApplicationReadyEvent(mock(SpringApplication.class), new String[]{}, context));
    }
}
