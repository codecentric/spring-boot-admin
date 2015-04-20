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

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.springframework.cloud.netflix.zuul.filters.ProxyRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;

import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.registry.ApplicationRegistry;

public class ApplicationRouteLocatorTest {

	ApplicationRouteLocator locator;
	private ApplicationRegistry registry;

	@Before
	public void setup() {
		registry = mock(ApplicationRegistry.class);
		locator = new ApplicationRouteLocator("/", registry, new ZuulProperties(),
				"/api/applications");
	}

	@Test
	public void locateRoutes_healthOnly() {
		when(registry.getApplications()).thenReturn(
				Collections.singletonList(new Application("http://localhost/health", "",
						"", "app1", "1234")));

		locator.resetRoutes();

		assertEquals(1, locator.getRoutes().size());
		assertEquals(Collections.singleton("/api/applications/1234/health/**"),
				locator.getRoutePaths());

		assertEquals("http://localhost/health",
				locator.getRoutes().get("/api/applications/1234/health/**"));
		assertEquals(new ProxyRouteLocator.ProxyRouteSpec("api/applications/1234/health",
				"", "http://localhost/health", "/api/applications/1234/health",
				null), locator.getMatchingRoute("/api/applications/1234/health"));
	}

	@Test
	public void locateRoutes() {
		when(registry.getApplications()).thenReturn(
				Collections.singletonList(new Application("http://localhost/health",
						"http://localhost", "", "app1", "1234")));

		locator.resetRoutes();

		assertEquals(2, locator.getRoutes().size());

		assertThat(locator.getRoutePaths(),
				hasItems(
						"/api/applications/1234/health/**",
						"/api/applications/1234/*/**"));

		assertEquals("http://localhost/health",
				locator.getRoutes().get("/api/applications/1234/health/**"));
		assertEquals(new ProxyRouteLocator.ProxyRouteSpec("api/applications/1234/health",
				"", "http://localhost/health", "/api/applications/1234/health",
				null), locator.getMatchingRoute("/api/applications/1234/health"));

		assertEquals("http://localhost",
				locator.getRoutes().get("/api/applications/1234/*/**"));
		assertEquals(new ProxyRouteLocator.ProxyRouteSpec("api/applications/1234",
				"/*/**", "http://localhost", "/api/applications/1234", null),
				locator.getMatchingRoute("/api/applications/1234/*/**"));
	}

}
