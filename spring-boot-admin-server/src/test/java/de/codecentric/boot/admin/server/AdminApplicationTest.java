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

import de.codecentric.boot.admin.server.AdminApplicationTest.TestAdminApplication;
import de.codecentric.boot.admin.server.config.EnableAdminServer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test to verify the correct functionality of the REST API.
 *
 * @author Dennis Schulte
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestAdminApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT, properties = {"spring.cloud.config.enabled=false"})
public class AdminApplicationTest {

    @Value("${local.server.port}")
    private int port = 0;

    @Test
    public void testGetApplications() {
        @SuppressWarnings("rawtypes") ResponseEntity<List> entity = new TestRestTemplate().getForEntity(
                "http://localhost:" + port + "/api/applications", List.class);
        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // FIXME
    @Test
    @Ignore
    public void testReverseProxy() {
        String apiBaseUrl = "http://localhost:" + port + "/api/applications";

        Map<String, String> application = new HashMap<>();
        application.put("name", "TestApp");
        application.put("managementUrl", "http://localhost:" + port);
        application.put("serviceUrl", "http://localhost:" + port);
        application.put("healthUrl", "http://localhost:" + port + "/health");

        @SuppressWarnings("unchecked") ResponseEntity<Map<String, String>> entity = new TestRestTemplate().postForEntity(
                apiBaseUrl, application, (Class<Map<String, String>>) (Class<?>) Map.class);

        @SuppressWarnings("rawtypes") ResponseEntity<Map> app = new TestRestTemplate().getForEntity(
                apiBaseUrl + "/" + entity.getBody().get("id"), Map.class);
        assertThat(app.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(app.getBody().get("name")).isEqualTo("TestApp");

        @SuppressWarnings("rawtypes") ResponseEntity<Map> info = new TestRestTemplate().getForEntity(
                apiBaseUrl + "/" + entity.getBody().get("id") + "/info", Map.class);
        assertThat(info.getStatusCode()).isEqualTo(HttpStatus.OK);

        @SuppressWarnings("rawtypes") ResponseEntity<Map> health = new TestRestTemplate().getForEntity(
                apiBaseUrl + "/" + entity.getBody().get("id") + "/health", Map.class);
        assertThat(health.getStatusCode()).isEqualTo(HttpStatus.OK);

    }

    @Configuration
    @EnableAutoConfiguration(exclude = {})
    @EnableAdminServer
    public static class TestAdminApplication {
    }
}
