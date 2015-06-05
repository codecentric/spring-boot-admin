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
package de.codecentric.boot.admin.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.registry.ApplicationRegistry;
import de.codecentric.boot.admin.registry.HashingApplicationUrlIdGenerator;
import de.codecentric.boot.admin.registry.store.SimpleApplicationStore;

public class RegistryControllerTest {

	private RegistryController controller;
	private ApplicationRegistry registry;

	@Before
	public void setup() {
		registry = new ApplicationRegistry(new SimpleApplicationStore(),
				new HashingApplicationUrlIdGenerator());
		registry.setApplicationEventPublisher(Mockito.mock(ApplicationEventPublisher.class));
		controller = new RegistryController(registry);
	}

	@Test
	public void register() {
		ResponseEntity<Application> response = controller.register(Application
				.create("test").withHealthUrl("http://localhost/mgmt/health")
				.withManagementUrl("http://localhost/mgmt")
				.withServiceUrl("http://localhost/").build());
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals("http://localhost/mgmt/health", response.getBody().getHealthUrl());
		assertEquals("http://localhost/mgmt", response.getBody().getManagementUrl());
		assertEquals("http://localhost/", response.getBody().getServiceUrl());
		assertEquals("test", response.getBody().getName());
	}

	@Test
	public void register_twice() {
		Application app = Application.create("test")
				.withHealthUrl("http://localhost/health").build();
		controller.register(app);
		ResponseEntity<Application> response = controller.register(app);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals("http://localhost/health", response.getBody().getHealthUrl());
		assertEquals("test", response.getBody().getName());
	}

	@Test
	public void register_sameUrl() {
		controller.register(Application.create("FOO")
				.withHealthUrl("http://localhost/mgmt/health").build());
		ResponseEntity<?> response = controller.register(Application
				.create("BAR").withHealthUrl("http://localhost/mgmt/health")
				.build());
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
	}

	@Test
	public void get() {
		Application app = controller.register(
				Application.create("FOO")
						.withHealthUrl("http://localhost/health").build())
				.getBody();

		ResponseEntity<Application> response = controller.get(app.getId());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("http://localhost/health", response.getBody().getHealthUrl());
		assertEquals("FOO", response.getBody().getName());
	}

	@Test
	public void get_notFound() {
		controller.register(Application.create("FOO")
				.withHealthUrl("http://localhost/mgmt/health").build());

		ResponseEntity<?> response = controller.get("unknown");
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}

	@Test
	public void unregister() {
		Application app = controller.register(
				Application.create("FOO")
				.withHealthUrl("http://localhost/mgmt/health").build())
				.getBody();

		ResponseEntity<?> response = controller.unregister(app.getId());
		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
		assertEquals(app, response.getBody());

		assertEquals(HttpStatus.NOT_FOUND, controller.get(app.getId()).getStatusCode());
	}

	@Test
	public void unregister_notFound() {
		controller.register(Application.create("FOO")
				.withHealthUrl("http://localhost/mgmt/health").build());

		ResponseEntity<?> response = controller.unregister("unknown");
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}

	@Test
	public void applications() {
		Application app = controller.register(
				Application.create("FOO")
				.withHealthUrl("http://localhost/mgmt/health").build())
				.getBody();

		Collection<Application> applications = controller.applications(null);
		assertEquals(1, applications.size());
		assertTrue(applications.contains(app));
	}

	@Test
	public void applicationsByName() {
		Application app = controller.register(
				Application.create("FOO")
				.withHealthUrl("http://localhost1/mgmt/health")
				.build()).getBody();
		Application app2 = controller.register(
				Application.create("FOO")
				.withHealthUrl("http://localhost2/mgmt/health")
				.build()).getBody();
		Application app3 = controller.register(
				Application.create("BAR")
				.withHealthUrl("http://localhost3/mgmt/health")
				.build()).getBody();

		Collection<Application> applications = controller.applications("FOO");
		assertEquals(2, applications.size());
		assertTrue(applications.contains(app));
		assertTrue(applications.contains(app2));
		assertFalse(applications.contains(app3));
	}
}
