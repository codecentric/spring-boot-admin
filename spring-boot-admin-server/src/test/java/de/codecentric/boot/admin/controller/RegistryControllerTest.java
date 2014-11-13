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

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.registry.ApplicationRegistry;
import de.codecentric.boot.admin.registry.HashingApplicationUrlIdGenerator;
import de.codecentric.boot.admin.registry.store.SimpleApplicationStore;

public class RegistryControllerTest {

	private RegistryController controller;

	@Before
	public void setup() {
		controller = new RegistryController(new ApplicationRegistry(new SimpleApplicationStore(),
				new HashingApplicationUrlIdGenerator()));
	}

	@Test
	public void register() {
		ResponseEntity<Application> response = controller.register(new Application("http://localhost", "test"));
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals("http://localhost", response.getBody().getUrl());
		assertEquals("test", response.getBody().getName());
	}

	@Test
	public void register_twice() {
		controller.register(new Application("http://localhost", "test"));
		Application app = new Application("http://localhost", "test");
		ResponseEntity<Application> response = controller.register(app);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals("http://localhost", response.getBody().getUrl());
		assertEquals("test", response.getBody().getName());
	}

	@Test
	public void register_sameUrl() {
		controller.register(new Application("http://localhost", "FOO"));
		ResponseEntity<?> response = controller.register(new Application("http://localhost", "BAR"));
		assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
	}

	@Test
	public void get() {
		Application app = new Application("http://localhost", "FOO");
		app = controller.register(app).getBody();

		ResponseEntity<Application> response = controller.get(app.getId());
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("http://localhost", response.getBody().getUrl());
		assertEquals("FOO", response.getBody().getName());
	}

	@Test
	public void get_notFound() {
		controller.register(new Application("http://localhost", "FOO"));

		ResponseEntity<?> response = controller.get("unknown");
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}

	@Test
	public void unregister() {
		Application app = new Application("http://localhost", "FOO");
		app = controller.register(app).getBody();

		ResponseEntity<?> response = controller.unregister(app.getId());
		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
		assertEquals(app, response.getBody());

		assertEquals(HttpStatus.NOT_FOUND, controller.get(app.getId()).getStatusCode());
	}

	@Test
	public void unregister_notFound() {
		controller.register(new Application("http://localhost", "FOO"));

		ResponseEntity<?> response = controller.unregister("unknown");
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}

	@Test
	public void applications() {
		Application app = new Application("http://localhost", "FOO");
		controller.register(app);

		List<Application> applications = controller.applications();
		assertEquals(1, applications.size());

		assertEquals(app.getName(), applications.get(0).getName());
		assertEquals(app.getUrl(), applications.get(0).getUrl());
	}
}
