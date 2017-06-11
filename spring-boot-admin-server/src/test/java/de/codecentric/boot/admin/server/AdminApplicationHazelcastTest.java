/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.codecentric.boot.admin.server;

import de.codecentric.boot.admin.server.config.EnableAdminServer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.ListConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.TcpIpConfig;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test to verify the correct functionality of the REST API with Hazelcast
 *
 * @author Dennis Schulte
 */
public class AdminApplicationHazelcastTest {
    private TestRestTemplate template = new TestRestTemplate();
    private ServletWebServerApplicationContext instance1;
    private ServletWebServerApplicationContext instance2;

    @Before
    public void setup() throws InterruptedException {
        System.setProperty("hazelcast.wait.seconds.before.join", "0");
        instance1 = (ServletWebServerApplicationContext) SpringApplication.run(TestAdminApplication.class,
                "--server.port=0", "--spring.jmx.enabled=false");
        instance2 = (ServletWebServerApplicationContext) SpringApplication.run(TestAdminApplication.class,
                "--server.port=0", "--spring.jmx.enabled=false");
    }

    @After
    public void shutdown() {
        instance1.close();
        instance2.close();
    }

    @Test
    public void test() throws Exception {
        // publish app on instance1
        ResponseEntity<Map<String, String>> postResponse = registerApp("Hazelcast Test", "http://127.0.0.1/health",
                instance1);
        Map<String, String> app = postResponse.getBody();
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(app.get("id")).isNotNull();

        // publish app2 on instance2
        ResponseEntity<Map<String, String>> postResponse2 = registerApp("Hazelcast Test", "http://127.0.0.2/health",
                instance2);
        Map<String, String> app2 = postResponse2.getBody();
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(app2.get("id")).isNotNull();

        // retrieve app from instance2
        ResponseEntity<Map<String, String>> getResponse = getApp(app.get("id"), instance2);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().get("id")).isEqualTo(app.get("id"));

        // retrieve app and app2 from instance1 (but not app3)
        Map<String, String> app3 = registerApp("Do not find", "http://127.0.0.1:3/health", instance1).getBody();
        Collection<Map<String, String>> apps = getAppByName("Hazelcast Test", instance1).getBody();

        assertThat(apps).extracting("id").containsExactly(app.get("id"), app2.get("id")).doesNotContain(app3.get("id"));
    }

    private ResponseEntity<Map<String, String>> getApp(String id, ServletWebServerApplicationContext context) {
        int port = context.getWebServer().getPort();
        @SuppressWarnings("unchecked") ResponseEntity<Map<String, String>> response = template.getForEntity(
                "http://localhost:" + port + "/api/applications/" + id,
                (Class<Map<String, String>>) (Class<?>) Map.class);
        return response;
    }

    private ResponseEntity<Map<String, String>> registerApp(String name,
                                                            String healthUrl,
                                                            ServletWebServerApplicationContext context) {
        Map<String, String> app = new HashMap<>();
        app.put("name", name);
        app.put("healthUrl", healthUrl);
        int port = context.getWebServer().getPort();
        @SuppressWarnings("unchecked") ResponseEntity<Map<String, String>> responseEntity = template.postForEntity(
                "http://localhost:" + port + "/api/applications", app,
                (Class<Map<String, String>>) (Class<?>) Map.class);
        return responseEntity;
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<Collection<Map<String, String>>> getAppByName(String name,
                                                                         ServletWebServerApplicationContext context) {
        int port = context.getWebServer().getPort();
        ResponseEntity<?> response = template.getForEntity("http://localhost:" + port + "/api/applications?name={name}",
                List.class, Collections.singletonMap("name", name));
        return (ResponseEntity<Collection<Map<String, String>>>) response;
    }

    @Configuration
    @EnableAutoConfiguration(exclude = {})
    @EnableAdminServer
    public static class TestAdminApplication {
        @Bean
        public Config hazelcastConfig() {
            Config config = new Config();

            config.addMapConfig(new MapConfig("spring-boot-admin-application-store").setBackupCount(1)
                                                                                    .setEvictionPolicy(
                                                                                            EvictionPolicy.NONE));

            config.addListConfig(
                    new ListConfig("spring-boot-admin-application-store").setBackupCount(1).setMaxSize(1000));

            config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
            TcpIpConfig tcpIpConfig = config.getNetworkConfig().getJoin().getTcpIpConfig();
            tcpIpConfig.setEnabled(true);
            tcpIpConfig.setMembers(singletonList("127.0.0.1"));
            return config;
        }
    }

}
