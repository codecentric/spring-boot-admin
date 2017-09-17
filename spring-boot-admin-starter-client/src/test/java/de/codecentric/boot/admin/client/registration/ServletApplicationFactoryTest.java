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
import org.springframework.boot.actuate.autoconfigure.endpoint.web.EndpointPathProvider;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServletApplicationFactoryTest {
    private InstanceProperties instance = new InstanceProperties();
    private ServerProperties server = new ServerProperties();
    private ManagementServerProperties management = new ManagementServerProperties();
    private MockServletContext servletContext = new MockServletContext();
    private EndpointPathProvider endpointPathProvider = mock(EndpointPathProvider.class);
    private ServletApplicationFactory factory = new ServletApplicationFactory(instance, management, server,
            servletContext, endpointPathProvider);

    @Before
    public void setup() {
        instance.setName("test");
    }

    @Test
    public void test_contextPath_mgmtPath() {
        servletContext.setContextPath("app");
        management.setContextPath("/admin");
        when(endpointPathProvider.getPath("health")).thenReturn("/admin/health");
        publishApplicationReadyEvent(factory, 8080, null);

        Application app = factory.createApplication();
        assertThat(app.getManagementUrl()).isEqualTo("http://" + getHostname() + ":8080/app/admin");
        assertThat(app.getHealthUrl()).isEqualTo("http://" + getHostname() + ":8080/app/admin/health");
        assertThat(app.getServiceUrl()).isEqualTo("http://" + getHostname() + ":8080/app");
    }

    @Test
    public void test_contextPath_mgmtPortPath() {
        servletContext.setContextPath("app");
        management.setContextPath("/admin");
        when(endpointPathProvider.getPath("health")).thenReturn("/admin/health");
        publishApplicationReadyEvent(factory, 8080, 8081);

        Application app = factory.createApplication();
        assertThat(app.getManagementUrl()).isEqualTo("http://" + getHostname() + ":8081/admin");
        assertThat(app.getHealthUrl()).isEqualTo("http://" + getHostname() + ":8081/admin/health");
        assertThat(app.getServiceUrl()).isEqualTo("http://" + getHostname() + ":8080/app");
    }

    @Test
    public void test_contextPath() {
        servletContext.setContextPath("app");
        when(endpointPathProvider.getPath("health")).thenReturn("/application/health");
        publishApplicationReadyEvent(factory, 80, null);

        Application app = factory.createApplication();
        assertThat(app.getManagementUrl()).isEqualTo("http://" + getHostname() + ":80/app/application");
        assertThat(app.getHealthUrl()).isEqualTo("http://" + getHostname() + ":80/app/application/health");
        assertThat(app.getServiceUrl()).isEqualTo("http://" + getHostname() + ":80/app");
    }

    @Test
    public void test_servletPath() {
        server.getServlet().setPath("app");
        servletContext.setContextPath("srv");
        when(endpointPathProvider.getPath("health")).thenReturn("/application/health");
        publishApplicationReadyEvent(factory, 80, null);

        Application app = factory.createApplication();
        assertThat(app.getManagementUrl()).isEqualTo("http://" + getHostname() + ":80/srv/app/application");
        assertThat(app.getHealthUrl()).isEqualTo("http://" + getHostname() + ":80/srv/app/application/health");
        assertThat(app.getServiceUrl()).isEqualTo("http://" + getHostname() + ":80/srv");
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
