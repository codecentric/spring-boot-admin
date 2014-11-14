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
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import de.codecentric.boot.admin.config.EnableAdminServer;
import de.codecentric.boot.admin.model.Application;

/**
 * 
 * Integration test to verify the correct functionality of the REST API with Hazelcast
 * 
 * @author Dennis Schulte
 */
public class AdminApplicationHazelcastTest {

	private RestTemplate template = new TestRestTemplate();
	private AnnotationConfigEmbeddedWebApplicationContext instance1;
	private AnnotationConfigEmbeddedWebApplicationContext instance2;

	@Before
	public void setup() {
		instance1 = (AnnotationConfigEmbeddedWebApplicationContext) SpringApplication.run(TestAdminApplication.class,
				new String[] { "--server.port=0", "--spring.jmx.enabled=false" });
		instance2 = (AnnotationConfigEmbeddedWebApplicationContext) SpringApplication.run(TestAdminApplication.class,
				new String[] { "--server.port=0", "--spring.jmx.enabled=false" });
	}

	@After
	public void shutdown() {
		instance1.stop();
		instance2.stop();
	}

	@Test
	public void test() {
		Application app = new Application("http://127.0.0.1", "Hazelcast Test");

		// publish application on instance1
		int port1 = instance1.getEmbeddedServletContainer().getPort();
		ResponseEntity<Application> postResponse = template.postForEntity("http://localhost:" + port1
				+ "/api/applications", app, Application.class);
		assertEquals(HttpStatus.CREATED, postResponse.getStatusCode());
		assertNotNull(postResponse.getBody().getId());

		// retrieve application from instance2
		int port2 = instance2.getEmbeddedServletContainer().getPort();
		ResponseEntity<Application> getResponse = template.getForEntity("http://localhost:" + port2
				+ "/api/application/" + postResponse.getBody().getId(), Application.class);

		assertEquals(HttpStatus.OK, getResponse.getStatusCode());
		assertEquals(postResponse.getBody(), getResponse.getBody());
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableAdminServer
	public static class TestAdminApplication {
	}
}
