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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.RefreshableRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;

import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.registry.ApplicationRegistry;

/**
 * RouteLocator to register all applications' routes to zuul
 *
 * @author Johannes Edmeier
 */
public class ApplicationRouteLocator implements RefreshableRouteLocator {
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationRouteLocator.class);

	private ApplicationRegistry registry;
	private PathMatcher pathMatcher = new AntPathMatcher();
	private AtomicReference<List<ApplicationRoute>> routes = new AtomicReference<>();
	private String prefix;
	private String servletPath;
	private String[] endpoints = {};

	public ApplicationRouteLocator(String servletPath, ApplicationRegistry registry,
			String prefix) {
		this.servletPath = servletPath;
		this.registry = registry;
		this.prefix = prefix;
	}

	protected List<ApplicationRoute> locateRoutes() {
		Collection<Application> applications = registry.getApplications();

		List<ApplicationRoute> locateRoutes = new ArrayList<>(
				applications.size() * (endpoints.length + 1));

		for (Application application : applications) {
			addRoute(locateRoutes, application, "health", application.getHealthUrl());
			if (!StringUtils.isEmpty(application.getManagementUrl())) {
				for (String endpoint : endpoints) {
					addRoute(locateRoutes, application, endpoint,
							application.getManagementUrl() + "/" + endpoint);
				}
			}
		}

		return locateRoutes;
	}

	private void addRoute(List<ApplicationRoute> locateRoutes, Application application,
			String endpoint, String targetUrl) {
		ApplicationRoute route = new ApplicationRoute(application,
				application.getId() + "-" + endpoint, "/**", targetUrl,
				prefix + application.getId() + "/" + endpoint);
		locateRoutes.add(route);
	}

	@Override
	public ApplicationRoute getMatchingRoute(final String path) {
		LOGGER.debug("Finding route for path: {}", path);

		if (this.routes.get() == null) {
			this.routes.set(locateRoutes());
		}

		LOGGER.debug("servletPath= {}", this.servletPath);

		String adjustedPath = stripServletPath(path);

		for (ApplicationRoute route : this.routes.get()) {
			String pattern = route.getFullPath();
			LOGGER.debug("Matching pattern: {}", pattern);
			if (this.pathMatcher.match(pattern, adjustedPath)) {
				LOGGER.debug("route matched= {}", route);
				return adjustPathRoute(route, adjustedPath);
			}
		}

		return null;
	}

	private ApplicationRoute adjustPathRoute(ApplicationRoute route, String path) {
		String adjustedPath;
		if (path.startsWith(route.getPrefix())) {
			adjustedPath = path.substring(route.getPrefix().length());
		} else {
			adjustedPath = path;
		}
		return new ApplicationRoute(route.getApplication(), route.getId(), adjustedPath,
				route.getLocation(), route.getPrefix());
	}

	@Override
	public List<Route> getRoutes() {
		if (this.routes.get() == null) {
			this.routes.set(locateRoutes());
		}
		return new ArrayList<Route>(routes.get());
	}

	@Override
	public void refresh() {
		routes.set(locateRoutes());
	}

	@Override
	public Collection<String> getIgnoredPaths() {
		return Collections.emptyList();
	}

	public void setEndpoints(String[] proxyEndpoints) {
		for (String endpoint : proxyEndpoints) {
			Assert.hasText(endpoint, "The proxyEndpoints must not contain null");
			Assert.isTrue(!endpoint.startsWith("/"), "All proxyEndpoints must not start with '/'");
		}
		this.endpoints = proxyEndpoints.clone();
	}

	private String stripServletPath(final String path) {
		String adjustedPath = path;

		if (StringUtils.hasText(servletPath) && !"/".equals(servletPath)) {
			adjustedPath = path.substring(this.servletPath.length());
		}

		LOGGER.debug("adjustedPath={}", path);
		return adjustedPath;
	}

	public static class ApplicationRoute extends Route {
		private final Application application;

		public ApplicationRoute(Application application, String id, String path, String location,
				String prefix) {
			super(id, path, location, prefix, false, null);
			this.application = application;
		}

		public Application getApplication() {
			return application;
		}

	}
}
