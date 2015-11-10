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
package de.codecentric.boot.admin.zuul;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.registry.ApplicationRegistry;
import de.codecentric.boot.admin.zuul.ApplicationRouteLocator.ProxyRouteSpec;

public class ApplicationRouteLocatorTest {

	ApplicationRouteLocator locator;
	private ApplicationRegistry registry;

	@Before
	public void setup() {
		registry = mock(ApplicationRegistry.class);
		locator = new ApplicationRouteLocator("/", registry, "/api/applications");
	}

	@Test
	public void locateRoutes_healthOnly() {
		when(registry.getApplications()).thenReturn(singletonList(Application.create("app1")
				.withHealthUrl("http://localhost/health").withId("1234").build()));

		locator.resetRoutes();

		assertEquals(1, locator.getRoutes().size());
		assertEquals(singleton("/api/applications/1234/health/**"), locator.getRoutePaths());

		assertEquals("http://localhost/health",
				locator.getRoutes().get("/api/applications/1234/health/**"));
		ProxyRouteSpec route = locator.getMatchingRoute("/api/applications/1234/health");
		assertEquals("api/applications/1234/health", route.getId());
		assertEquals("", route.getPath());
		assertEquals("http://localhost/health", route.getLocation());
		assertEquals("/api/applications/1234/health", route.getPrefix());
	}

	@Test
	public void locateRoutes() {
		when(registry.getApplications()).thenReturn(
				singletonList(Application.create("app1").withHealthUrl("http://localhost/health")
						.withManagementUrl("http://localhost").withId("1234").build()));

		locator.resetRoutes();

		assertEquals(2, locator.getRoutes().size());

		assertThat(locator.getRoutePaths(),
				hasItems("/api/applications/1234/health/**", "/api/applications/1234/*/**"));

		assertEquals("http://localhost/health",
				locator.getRoutes().get("/api/applications/1234/health/**"));
		ProxyRouteSpec route = locator.getMatchingRoute("/api/applications/1234/health");
		assertEquals("api/applications/1234/health", route.getId());
		assertEquals("", route.getPath());
		assertEquals("http://localhost/health", route.getLocation());
		assertEquals("/api/applications/1234/health", route.getPrefix());

		route = locator.getMatchingRoute("/api/applications/1234/notify");
		assertEquals("api/applications/1234", route.getId());
		assertEquals("/notify", route.getPath());
		assertEquals("http://localhost", route.getLocation());
		assertEquals("/api/applications/1234", route.getPrefix());
	}
}
