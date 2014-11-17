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
package de.codecentric.boot.admin.registry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;

import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.registry.store.SimpleApplicationStore;

public class ApplicationRegistryTest {

	private ApplicationRegistry registry = new ApplicationRegistry(new SimpleApplicationStore(),
			new HashingApplicationUrlIdGenerator());

	@Test(expected = NullPointerException.class)
	public void registerFailed1() throws Exception {
		registry.register(new Application(null, null));
	}

	@Test(expected = NullPointerException.class)
	public void registerFailed2() throws Exception {
		Application app = new Application(null, "abc");
		registry.register(app);
	}

	@Test(expected = IllegalArgumentException.class)
	public void registerFailed3() throws Exception {
		Application app = new Application("not-an-url", "abc");
		registry.register(app);
	}

	@Test
	public void register() throws Exception {
		Application app = new Application("http://localhost:8080", "abc");
		Application response = registry.register(app);

		assertEquals("http://localhost:8080", response.getUrl());
		assertEquals("abc", response.getName());
		assertNotNull(response.getId());
	}

	@Test
	public void getApplication() throws Exception {
		Application app = new Application("http://localhost:8080", "abc");
		app = registry.register(app);

		assertEquals(app, registry.getApplication(app.getId()));
	}

	@Test
	public void getApplications() throws Exception {
		Application app = new Application("http://localhost:8080", "abc");
		app = registry.register(app);

		Collection<Application> applications = registry.getApplications();
		assertEquals(1, applications.size());
		assertTrue(applications.contains(app));
	}

	@Test
	public void getApplicationsByName() throws Exception {
		Application app = new Application("http://localhost:8080", "abc");
		app = registry.register(app);
		Application app2 = new Application("http://localhost:8081", "abc");
		app2 = registry.register(app2);
		Application app3 = new Application("http://localhost:8082", "cba");
		app3 = registry.register(app3);

		Collection<Application> applications = registry.getApplicationsByName("abc");
		assertEquals(2, applications.size());
		assertTrue(applications.contains(app));
		assertTrue(applications.contains(app2));
		assertFalse(applications.contains(app3));
	}
}
