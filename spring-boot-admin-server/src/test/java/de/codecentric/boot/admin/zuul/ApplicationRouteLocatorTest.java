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

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
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

		assertThat(locator.getRoutes()).containsOnly(new Route("1234-health", "/**",
				"http://localhost/health", "/api/applications/1234/health", false, null));

		Route matchingRoute = locator.getMatchingRoute("/api/applications/1234/health");
		assertThat(matchingRoute).isEqualTo(new Route("1234-health", "", "http://localhost/health",
				"/api/applications/1234/health", false, null));

		assertThat(locator.getMatchingRoute("/api/applications/1234/danger")).isNull();
	}

	@Test
	public void getRoutes() {
		when(registry.getApplications()).thenReturn(
				singletonList(Application.create("app1").withHealthUrl("http://localhost/health")
						.withManagementUrl("http://localhost").withId("1234").build()));

		assertThat(locator.getRoutes()).containsOnly(
				new Route("1234-health", "/**", "http://localhost/health",
						"/api/applications/1234/health", false, null),
				new Route("1234-env", "/**", "http://localhost/env", "/api/applications/1234/env",
						false, null));

		Route matchingHealth = locator.getMatchingRoute("/api/applications/1234/health");
		assertThat(matchingHealth).isEqualTo(new Route("1234-health", "", "http://localhost/health",
				"/api/applications/1234/health", false, null));

		Route matchingEnv = locator.getMatchingRoute("/api/applications/1234/env/reset");
		assertThat(matchingEnv).isEqualTo(new Route("1234-env", "/reset", "http://localhost/env",
				"/api/applications/1234/env", false, null));

		assertThat(locator.getMatchingRoute("/api/applications/1234/danger")).isNull();
	}

	@Test
	public void ignoredPaths() {
		assertThat(locator.getIgnoredPaths()).isEmpty();
	}

	@Test
	public void refresh() {
		when(registry.getApplications()).thenReturn(Collections.<Application>emptyList());
		locator.refresh();
		assertThat(locator.getRoutes()).isEmpty();

		when(registry.getApplications()).thenReturn(singletonList(Application.create("app1")
				.withHealthUrl("http://localhost/health").withId("1234").build()));
		locator.refresh();
		assertThat(locator.getRoutes()).hasSize(1);

		when(registry.getApplications()).thenReturn(Collections.<Application>emptyList());
		locator.refresh();
		assertThat(locator.getRoutes()).isEmpty();
	}
}
