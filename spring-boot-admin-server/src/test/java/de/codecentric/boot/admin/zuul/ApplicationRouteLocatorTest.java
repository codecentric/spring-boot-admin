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

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.springframework.cloud.netflix.zuul.filters.Route;

import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.registry.ApplicationRegistry;

public class ApplicationRouteLocatorTest {

	private ApplicationRouteLocator locator;
	private ApplicationRegistry registry;

	@Before
	public void setup() {
		registry = mock(ApplicationRegistry.class);
		locator = new ApplicationRouteLocator("/", registry, "/api/applications/");
		locator.setEndpoints(new String[] { "env" });
	}

	@Test
	public void getRoutes_healthOnly() {
		when(registry.getApplications()).thenReturn(singletonList(Application.create("app1")
				.withHealthUrl("http://localhost/health").withId("1234").build()));

		assertEquals(1, locator.getRoutes().size());
		assertEquals(asList(new Route("1234-health", "/**", "http://localhost/health",
				"/api/applications/1234/health", false, null)), locator.getRoutes());

		Route matchingRoute = locator.getMatchingRoute("/api/applications/1234/health");
		assertEquals(new Route("1234-health", "", "http://localhost/health",
				"/api/applications/1234/health", false, null), matchingRoute);

		assertNull(locator.getMatchingRoute("/api/applications/1234/danger"));
	}

	@Test
	public void getRoutes() {
		when(registry.getApplications()).thenReturn(
				singletonList(Application.create("app1").withHealthUrl("http://localhost/health")
						.withManagementUrl("http://localhost").withId("1234").build()));

		assertEquals(2, locator.getRoutes().size());

		assertEquals(asList(
				new Route("1234-health", "/**", "http://localhost/health",
						"/api/applications/1234/health", false, null),
				new Route("1234-env", "/**", "http://localhost/env", "/api/applications/1234/env",
						false, null)),
				locator.getRoutes());

		Route matchingHealth = locator.getMatchingRoute("/api/applications/1234/health");
		assertEquals(new Route("1234-health", "", "http://localhost/health",
				"/api/applications/1234/health", false, null), matchingHealth);

		Route matchingEnv = locator.getMatchingRoute("/api/applications/1234/env/reset");
		assertEquals(new Route("1234-env", "/reset", "http://localhost/env",
				"/api/applications/1234/env", false, null), matchingEnv);

		assertNull(locator.getMatchingRoute("/api/applications/1234/danger"));
	}

	@Test
	public void ignoredPaths() {
		assertEquals(emptyList(), locator.getIgnoredPaths());
	}

	@Test
	public void refresh() {
		when(registry.getApplications()).thenReturn(Collections.<Application> emptyList());
		locator.refresh();
		assertTrue(locator.getRoutes().isEmpty());

		when(registry.getApplications()).thenReturn(singletonList(Application.create("app1")
				.withHealthUrl("http://localhost/health").withId("1234").build()));
		locator.refresh();
		assertEquals(1, locator.getRoutes().size());

		when(registry.getApplications()).thenReturn(Collections.<Application> emptyList());
		locator.refresh();
		assertTrue(locator.getRoutes().isEmpty());
	}
}
