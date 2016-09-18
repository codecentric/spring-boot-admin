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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.ListConfig;
import com.hazelcast.config.MapConfig;

import de.codecentric.boot.admin.config.EnableAdminServer;
import de.codecentric.boot.admin.model.Application;

/**
 * Integration test to verify the correct functionality of the REST API with Hazelcast
 *
 * @author Dennis Schulte
 */
public class AdminApplicationHazelcastTest {
	private TestRestTemplate template = new TestRestTemplate();
	private EmbeddedWebApplicationContext instance1;
	private EmbeddedWebApplicationContext instance2;

	@Before
	public void setup() throws InterruptedException {
		System.setProperty("hazelcast.wait.seconds.before.join", "0");
		instance1 = (EmbeddedWebApplicationContext) SpringApplication.run(
				TestAdminApplication.class,
				new String[] { "--server.port=0", "--spring.jmx.enabled=false" });
		instance2 = (EmbeddedWebApplicationContext) SpringApplication.run(
				TestAdminApplication.class,
				new String[] { "--server.port=0", "--spring.jmx.enabled=false" });
	}

	@After
	public void shutdown() {
		instance1.close();
		instance2.close();
	}

	@Test
	public void test() {
		Application app = Application.create("Hazelcast Test")
				.withHealthUrl("http://127.0.0.1/health").build();
		Application app2 = Application.create("Hazelcast Test")
				.withHealthUrl("http://127.0.0.1:2/health").build();
		Application app3 = Application.create("Do not find")
				.withHealthUrl("http://127.0.0.1:3/health").build();

		// publish app on instance1
		ResponseEntity<Application> postResponse = registerApp(app, instance1);
		app = postResponse.getBody();
		assertEquals(HttpStatus.CREATED, postResponse.getStatusCode());
		assertNotNull(app.getId());

		// publish app2 on instance2
		ResponseEntity<Application> postResponse2 = registerApp(app2, instance2);
		app2 = postResponse2.getBody();
		assertEquals(HttpStatus.CREATED, postResponse.getStatusCode());
		assertNotNull(app2.getId());

		// retrieve app from instance2
		ResponseEntity<Application> getResponse = getApp(app.getId(), instance2);
		assertEquals(HttpStatus.OK, getResponse.getStatusCode());
		assertEquals(app, getResponse.getBody());

		// retrieve app and app2 from instance1 (but not app3)
		app3 = registerApp(app3, instance1).getBody();
		Collection<Application> apps = getAppByName("Hazelcast Test", instance1).getBody();
		assertEquals(2, apps.size());
		assertTrue(apps.contains(app));
		assertTrue(apps.contains(app2));
		assertFalse(apps.contains(app3));
	}

	private ResponseEntity<Application> getApp(String id, EmbeddedWebApplicationContext context) {
		int port = context.getEmbeddedServletContainer().getPort();
		ResponseEntity<Application> getResponse = template.getForEntity(
				"http://localhost:" + port + "/api/applications/" + id, Application.class);
		return getResponse;
	}

	private ResponseEntity<Application> registerApp(Application app,
			EmbeddedWebApplicationContext context) {
		int port = context.getEmbeddedServletContainer().getPort();
		return template.postForEntity("http://localhost:" + port + "/api/applications", app,
				Application.class);
	}

	@SuppressWarnings("unchecked")
	private ResponseEntity<Collection<Application>> getAppByName(String name,
			EmbeddedWebApplicationContext context) {
		int port = context.getEmbeddedServletContainer().getPort();
		ResponseEntity<?> getResponse = template.getForEntity(
				"http://localhost:" + port + "/api/applications?name={name}", ApplicationList.class,
				Collections.singletonMap("name", name));
		return (ResponseEntity<Collection<Application>>) getResponse;
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableAdminServer
	public static class TestAdminApplication {
		@Bean
		public Config hazelcastConfig() {
			return new Config()
					.addMapConfig(new MapConfig("spring-boot-admin-application-store")
							.setBackupCount(1).setEvictionPolicy(EvictionPolicy.NONE))
					.addListConfig(new ListConfig("spring-boot-admin-application-store")
							.setBackupCount(1).setMaxSize(1000));
		}
	}

	public static class ApplicationList extends ArrayList<Application> {
		private static final long serialVersionUID = 1L;
		// needed for JSON deserialization
	}

}
