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
package de.codecentric.boot.admin.server;

import de.codecentric.boot.admin.server.config.EnableAdminServer;

import java.net.URI;
import java.util.List;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryProperties;
import org.springframework.http.MediaType;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;


public class AdminApplicationDiscoveryTest extends AbstractAdminApplicationTest {
    private ServletWebServerApplicationContext instance;
    private SimpleDiscoveryProperties simpleDiscovery;

    @Before
    public void setUp() throws Exception {
        instance = (ServletWebServerApplicationContext) SpringApplication.run(TestAdminApplication.class,
                "--server.port=0", "--management.context-path=/mgmt", "--info.test=foobar",
                "--management.security.enabled=false");

        simpleDiscovery = instance.getBean(SimpleDiscoveryProperties.class);

        super.setUp(instance.getWebServer().getPort());
    }


    @Override
    protected URI registerInstance() {
        //We register the instance by setting static values for the SimpleDiscoveryClient and issuing a
        //InstanceRegisteredEvent that makes sure the instance gets registered.
        SimpleDiscoveryProperties.SimpleServiceInstance serviceInstance = new SimpleDiscoveryProperties.SimpleServiceInstance();
        serviceInstance.setServiceId("Test-Instance");
        serviceInstance.setUri(URI.create("http://localhost:" + getPort()));
        serviceInstance.getMetadata().put("management.context-path", "/mgmt");
        simpleDiscovery.getInstances().put("Test-Application", singletonList(serviceInstance));

        instance.publishEvent(new InstanceRegisteredEvent<>(new Object(), null));

        //To get the location of the registered instances we fetch the instance with the name.
        List<JSONObject> applications = getWebClient().get()
                                                      .uri("/instances?name=Test-Instance")
                                                      .accept(MediaType.APPLICATION_JSON)
                                                      .exchange()
                                                      .expectStatus()
                                                      .isOk()
                                                      .returnResult(JSONObject.class)
                                                      .getResponseBody()
                                                      .collectList()
                                                      .block();
        assertThat(applications).hasSize(1);
        return URI.create("http://localhost:" + getPort() + "/instances/" + applications.get(0).optString("id"));
    }


    @Override
    protected void deregisterInstance(URI uri) {
        simpleDiscovery.getInstances().clear();
        instance.publishEvent(new InstanceRegisteredEvent<>(new Object(), null));
    }

    @After
    public void shutdown() {
        instance.close();
    }

    @EnableAdminServer
    @EnableAutoConfiguration
    @SpringBootConfiguration
    public static class TestAdminApplication {
    }
}
