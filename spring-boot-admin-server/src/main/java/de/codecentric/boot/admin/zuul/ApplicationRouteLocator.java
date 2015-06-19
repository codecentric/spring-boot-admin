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

import java.util.LinkedHashMap;

import org.springframework.cloud.netflix.zuul.filters.ProxyRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties.ZuulRoute;
import org.springframework.util.StringUtils;

import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.registry.ApplicationRegistry;

/**
 * RouteLocator to register all applications' routes to zuul
 *
 * @author Johannes Stelzer
 */
public class ApplicationRouteLocator extends ProxyRouteLocator {

	private ApplicationRegistry registry;
	private String prefix;

	public ApplicationRouteLocator(String servletPath, ApplicationRegistry registry,
			ZuulProperties properties, String prefix) {
		super(servletPath, null, properties);
		this.registry = registry;
		this.prefix = prefix;
	}

	@Override
	protected LinkedHashMap<String, ZuulRoute> locateRoutes() {
		LinkedHashMap<String, ZuulRoute> locateRoutes = super.locateRoutes();

		if (registry != null) {
			for (Application application : registry.getApplications()) {
				addRoute(locateRoutes, prefix + "/" + application.getId() + "/health/**",
						application.getHealthUrl());

				if (!StringUtils.isEmpty(application.getManagementUrl())) {
					addRoute(locateRoutes, prefix + "/" + application.getId() + "/*/**",
							application.getManagementUrl());
				}
			}
		}

		return locateRoutes;
	}

	private void addRoute(LinkedHashMap<String, ZuulRoute> locateRoutes, String path, String url) {
		locateRoutes.put(path, new ZuulRoute(path, url));
	}

}
