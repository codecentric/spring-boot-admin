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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties.ZuulRoute;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;

import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.registry.ApplicationRegistry;

/**
 * RouteLocator to register all applications' routes to zuul
 *
 * @author Johannes Stelzer
 */
public class ApplicationRouteLocator implements RouteLocator {
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationRouteLocator.class);
	private AtomicReference<Map<String, ZuulRoute>> routes = new AtomicReference<>();
	private ApplicationRegistry registry;
	private String prefix;
	private PathMatcher pathMatcher = new AntPathMatcher();
	private String servletPath;

	public ApplicationRouteLocator(String servletPath, ApplicationRegistry registry,
			String prefix) {
		this.servletPath = servletPath;
		this.registry = registry;
		this.prefix = prefix;
	}

	private LinkedHashMap<String, ZuulRoute> locateRoutes() {
		LinkedHashMap<String, ZuulRoute> locateRoutes = new LinkedHashMap<String, ZuulRoute>();
		for (Application application : registry.getApplications()) {
			addRoute(locateRoutes, prefix + "/" + application.getId() + "/health/**",
					application.getHealthUrl());
			if (!StringUtils.isEmpty(application.getManagementUrl())) {
				addRoute(locateRoutes, prefix + "/" + application.getId() + "/*/**",
						application.getManagementUrl());
			}
		}

		return locateRoutes;
	}

	private void addRoute(LinkedHashMap<String, ZuulRoute> locateRoutes, String path, String url) {
		locateRoutes.put(path, new ZuulRoute(path, url));
	}

	public ProxyRouteSpec getMatchingRoute(String path) {
		LOGGER.info("Finding route for path: {}; servletPath={}", path, this.servletPath);
		if (StringUtils.hasText(this.servletPath) && !this.servletPath.equals("/")
				&& path.startsWith(this.servletPath)) {
			path = path.substring(this.servletPath.length());
		}
		for (Entry<String, ZuulRoute> entry : this.routes.get().entrySet()) {
			String pattern = entry.getKey();
			LOGGER.debug("Matching pattern: {}", pattern);
			if (this.pathMatcher.match(pattern, path)) {
				ZuulRoute route = entry.getValue();
				int index = route.getPath().indexOf("*") - 1;
				String routePrefix = route.getPath().substring(0, index);
				String targetPath = path.replaceFirst(routePrefix, "");
				return new ProxyRouteSpec(route.getId(), targetPath, route.getLocation(),
						routePrefix);
			}
		}
		return null;
	}

	@Override
	public Collection<String> getRoutePaths() {
		return getRoutes().keySet();
	}

	public void resetRoutes() {
		this.routes.set(locateRoutes());
	}

	public Map<String, String> getRoutes() {
		if (this.routes.get() == null) {
			this.routes.set(locateRoutes());
		}
		Map<String, String> values = new LinkedHashMap<>();
		for (String key : this.routes.get().keySet()) {
			String url = key;
			values.put(url, this.routes.get().get(key).getLocation());
		}
		return values;
	}

	@Override
	public Collection<String> getIgnoredPaths() {
		return Collections.emptyList();
	}

	public static class ProxyRouteSpec {
		private final String id;
		private final String path;
		private final String location;
		private final String prefix;

		public ProxyRouteSpec(String id, String path, String location, String prefix) {
			this.id = id;
			this.path = path;
			this.location = location;
			this.prefix = prefix;
		}

		public String getId() {
			return id;
		}

		public String getPath() {
			return path;
		}

		public String getLocation() {
			return location;
		}

		public String getPrefix() {
			return prefix;
		}
	}
}
