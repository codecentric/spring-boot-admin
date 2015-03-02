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
package de.codecentric.boot.admin;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.codecentric.boot.admin.AdminApplicationTest.TestAdminApplication;
import de.codecentric.boot.admin.config.EnableAdminServer;
import de.codecentric.boot.admin.model.Application;

/**
 *
 * Integration test to verify the correct functionality of the REST API.
 *
 * @author Dennis Schulte
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestAdminApplication.class)
@WebIntegrationTest({ "server.port=0" })
public class AdminApplicationTest {

	@Value("${local.server.port}")
	private int port = 0;

	@Test
	public void testGetApplications() {
		@SuppressWarnings("rawtypes")
		ResponseEntity<List> entity = new TestRestTemplate().getForEntity("http://localhost:" + port
				+ "/api/applications", List.class);
		assertEquals(HttpStatus.OK, entity.getStatusCode());
	}

	@Test
	public void testReverseProxy() {
		String apiBaseUrl = "http://localhost:" + port + "/api/applications";

		ResponseEntity<Application> entity = new TestRestTemplate().postForEntity(apiBaseUrl, new Application(
				"http://localhost:" + port, "TestApp"), Application.class);

		@SuppressWarnings("rawtypes")
		ResponseEntity<Map> health = new TestRestTemplate().getForEntity(apiBaseUrl + "/" + entity.getBody().getId()
				+ "/info", Map.class);
		assertEquals(HttpStatus.OK, health.getStatusCode());
	}


	@Configuration
	@EnableAutoConfiguration
	@EnableAdminServer
	public static class TestAdminApplication {
	}
}
