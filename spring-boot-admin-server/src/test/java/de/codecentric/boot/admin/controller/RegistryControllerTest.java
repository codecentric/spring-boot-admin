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
		Application application = Application.create("test")
				.withHealthUrl("http://localhost/mgmt/health")
				.withManagementUrl("http://localhost/mgmt").withServiceUrl("http://localhost/")
				.build();

		ResponseEntity<Application> response = controller.register(application);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals("http://localhost/mgmt/health", response.getBody().getHealthUrl());
		assertEquals("http://localhost/mgmt", response.getBody().getManagementUrl());
		assertEquals("http://localhost/", response.getBody().getServiceUrl());
		assertEquals("test", response.getBody().getName());
	}

	@Test
	public void register_twice() {
		Application application = Application.create("test")
				.withHealthUrl("http://localhost/health").build();

		controller.register(application);
		ResponseEntity<Application> response = controller.register(application);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals("http://localhost/health", response.getBody().getHealthUrl());
		assertEquals("test", response.getBody().getName());
	}

	@Test
	public void register_sameUrl() {
		Application application = Application.create("FOO")
				.withHealthUrl("http://localhost/mgmt/health").build();
		controller.register(application);

		ResponseEntity<?> response = controller.register(Application.create("BAR")
				.withHealthUrl("http://localhost/mgmt/health").build());

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
	}

	@Test
	public void get() {
		Application application = Application.create("FOO")
				.withHealthUrl("http://localhost/health").build();

		application = controller.register(application).getBody();

		ResponseEntity<?> response = controller.get(application.getId());
		Application body = (Application) response.getBody();
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("http://localhost/health", body.getHealthUrl());
		assertEquals("FOO", body.getName());
	}

	@Test
	public void get_notFound() {
		Application application = Application.create("FOO")
				.withHealthUrl("http://localhost/mgmt/health").build();
		controller.register(application);

		ResponseEntity<?> response = controller.get("unknown");
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}

	@Test
	public void unregister() {
		Application application = Application.create("FOO")
				.withHealthUrl("http://localhost/mgmt/health").build();

		application = controller.register(application).getBody();

		ResponseEntity<?> response = controller.unregister(application.getId());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(application, response.getBody());

		assertEquals(HttpStatus.NOT_FOUND, controller.get(application.getId()).getStatusCode());
	}

	@Test
	public void unregister_notFound() {
		Application application = Application.create("FOO")
				.withHealthUrl("http://localhost/mgmt/health").build();

		controller.register(application);

		ResponseEntity<?> response = controller.unregister("unknown");
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}

	@Test
	public void applications() {
		Application app = controller.register(
				Application.create("FOO").withHealthUrl("http://localhost/mgmt/health").build())
				.getBody();

		Collection<Application> applications = controller.applications(null);
		assertEquals(1, applications.size());
		assertTrue(applications.contains(app));
	}

	@Test
	public void applicationsByName() {
		Application application = Application.create("FOO")
				.withHealthUrl("http://localhost1/mgmt/health").build();
		application = controller.register(application).getBody();

		Application application2 = Application.create("FOO")
				.withHealthUrl("http://localhost2/mgmt/health").build();
		application2 = controller.register(application2).getBody();

		Application application3 = Application.create("BAR")
				.withHealthUrl("http://localhost3/mgmt/health").build();
		application3 = controller.register(application3).getBody();

		Collection<Application> applications = controller.applications("FOO");
		assertEquals(2, applications.size());
		assertTrue(applications.contains(application));
		assertTrue(applications.contains(application2));
		assertFalse(applications.contains(application3));
	}
}
