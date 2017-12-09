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
import org.springframework.boot.actuate.autoconfigure.endpoint.web.EndpointPathProvider;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultApplicationFactoryTest {
    private InstanceProperties instanceProperties = new InstanceProperties();
    private ServerProperties server = new ServerProperties();
    private ManagementServerProperties management = new ManagementServerProperties();
    private EndpointPathProvider endpointPathProvider = mock(EndpointPathProvider.class);
    private WebEndpointProperties webEndpoint = new WebEndpointProperties();

    private DefaultApplicationFactory factory = new DefaultApplicationFactory(instanceProperties, management, server,
            endpointPathProvider, webEndpoint);

    @Before
    public void setup() {
        instanceProperties.setName("test");
    }

    @Test
    public void test_mgmtPortPath() {
        webEndpoint.setBasePath("/admin");
        when(endpointPathProvider.getPath("health")).thenReturn("/admin/alive");
        publishApplicationReadyEvent(factory, 8080, 8081);

        Application app = factory.createApplication();
        assertThat(app.getManagementUrl()).isEqualTo("http://" + getHostname() + ":8081/admin");
        assertThat(app.getHealthUrl()).isEqualTo("http://" + getHostname() + ":8081/admin/alive");
        assertThat(app.getServiceUrl()).isEqualTo("http://" + getHostname() + ":8080/");
    }

    @Test
    public void test_default() {
        when(endpointPathProvider.getPath("health")).thenReturn("/actuator/health");
        publishApplicationReadyEvent(factory, 8080, null);

        Application app = factory.createApplication();
        assertThat(app.getManagementUrl()).isEqualTo("http://" + getHostname() + ":8080/actuator");
        assertThat(app.getHealthUrl()).isEqualTo("http://" + getHostname() + ":8080/actuator/health");
        assertThat(app.getServiceUrl()).isEqualTo("http://" + getHostname() + ":8080/");
    }

    @Test
    public void test_ssl() {
        server.setSsl(new Ssl());
        server.getSsl().setEnabled(true);
        when(endpointPathProvider.getPath("health")).thenReturn("/actuator/health");
        publishApplicationReadyEvent(factory, 8080, null);

        Application app = factory.createApplication();
        assertThat(app.getManagementUrl()).isEqualTo("https://" + getHostname() + ":8080/actuator");
        assertThat(app.getHealthUrl()).isEqualTo("https://" + getHostname() + ":8080/actuator/health");
        assertThat(app.getServiceUrl()).isEqualTo("https://" + getHostname() + ":8080/");
    }

    @Test
    public void test_ssl_management() {
        management.setSsl(new Ssl());
        management.getSsl().setEnabled(true);
        when(endpointPathProvider.getPath("health")).thenReturn("/actuator/alive");
        publishApplicationReadyEvent(factory, 8080, 9090);

        Application app = factory.createApplication();
        assertThat(app.getManagementUrl()).isEqualTo("https://" + getHostname() + ":9090/actuator");
        assertThat(app.getHealthUrl()).isEqualTo("https://" + getHostname() + ":9090/actuator/alive");
        assertThat(app.getServiceUrl()).isEqualTo("http://" + getHostname() + ":8080/");
    }

    @Test
    public void test_preferIpAddress_serveraddress_missing() {
        instanceProperties.setPreferIp(true);
        when(endpointPathProvider.getPath("health")).thenReturn("/application/alive");
        publishApplicationReadyEvent(factory, 8080, null);

        Application app = factory.createApplication();
        assertThat(app.getServiceUrl()).matches("http://\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}:8080/");
    }

    @Test
    public void test_preferIpAddress_managementaddress_missing() {
        instanceProperties.setPreferIp(true);
        when(endpointPathProvider.getPath("health")).thenReturn("/application/alive");
        publishApplicationReadyEvent(factory, 8080, 8081);

        Application app = factory.createApplication();
        assertThat(app.getManagementUrl()).matches("http://\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}:8081/actuator");
    }

    @Test
    public void test_preferIpAddress() throws UnknownHostException {
        instanceProperties.setPreferIp(true);
        server.setAddress(InetAddress.getByName("127.0.0.1"));
        management.setAddress(InetAddress.getByName("127.0.0.2"));
        when(endpointPathProvider.getPath("health")).thenReturn("/actuator/health");
        publishApplicationReadyEvent(factory, 8080, 8081);

        Application app = factory.createApplication();
        assertThat(app.getManagementUrl()).isEqualTo("http://127.0.0.2:8081/actuator");
        assertThat(app.getHealthUrl()).isEqualTo("http://127.0.0.2:8081/actuator/health");
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
        webEndpoint.setBasePath("/admin");
        when(endpointPathProvider.getPath("health")).thenReturn("/admin/health");

        Application app = factory.createApplication();
        assertThat(app.getServiceUrl()).isEqualTo("http://service:80/");
        assertThat(app.getManagementUrl()).isEqualTo("http://management:8090/admin");
        assertThat(app.getHealthUrl()).isEqualTo("http://management:8090/admin/health");
    }

    @Test
    public void test_service_baseUrl() {
        instanceProperties.setServiceBaseUrl("http://service:80");
        webEndpoint.setBasePath("/admin");
        when(endpointPathProvider.getPath("health")).thenReturn("/admin/health");

        Application app = factory.createApplication();
        assertThat(app.getServiceUrl()).isEqualTo("http://service:80/");
        assertThat(app.getManagementUrl()).isEqualTo("http://service:80/admin");
        assertThat(app.getHealthUrl()).isEqualTo("http://service:80/admin/health");
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
        factory.onWebServerInitialized(new TestWebServerInitializedEvent("server", serverport));
        factory.onWebServerInitialized(
                new TestWebServerInitializedEvent("management", managementport != null ? managementport : serverport));
    }

    private static class TestWebServerInitializedEvent extends WebServerInitializedEvent {
        private final String serverId;
        private final WebServer server;

        private TestWebServerInitializedEvent(String serverId, int port) {
            super(mock(WebServer.class));
            this.serverId = serverId;
            this.server = mock(WebServer.class);
            when(server.getPort()).thenReturn(port);
        }

        @Override
        public ApplicationContext getApplicationContext() {
            return null;
        }

        @Override
        public String getServerId() {
            return this.serverId;
        }

        @Override
        public WebServer getWebServer() {
            return this.server;
        }
    }

}
