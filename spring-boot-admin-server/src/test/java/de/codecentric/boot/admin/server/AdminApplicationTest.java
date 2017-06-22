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
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;

public class AdminApplicationTest extends AbstractAdminApplicationTest {
    private ServletWebServerApplicationContext instance;

    @Before
    public void setup() throws InterruptedException {
        System.setProperty("hazelcast.wait.seconds.before.join", "0");
        instance = (ServletWebServerApplicationContext) SpringApplication.run(TestAdminApplication.class,
                "--server.port=0", "--spring.jmx.enabled=false", "--management.context-path=/mgmt",
                "--info.test=foobar");

        int port = instance.getWebServer().getPort();
        super.setPort(port);

        WebTestClient webClient = createWebClient(port);

        super.setWebClient(webClient);
    }

    private WebTestClient createWebClient(int port) {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JsonOrgModule());
        return WebTestClient.bindToServer()
                            .baseUrl("http://localhost:" + port)
                            .exchangeStrategies(ExchangeStrategies.builder().codecs((configurer) -> {
                                configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(mapper));
                                configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(mapper));
                            }).build())
                            .build();
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
