/*
 * Copyright 2013-2016 the original author or authors.
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
package spring.boot.admin.turbine.zuul.filters;

import static java.util.Collections.singletonMap;

import java.util.List;
import java.util.Map;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.SimpleRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties.ZuulRoute;

/**
 * RouteLocator for Turbine.
 *
 * @author Johannes Edmeier
 */
public class TurbineRouteLocator extends SimpleRouteLocator {
	private final Map<String, ZuulRoute> routes;
	private DiscoveryClient discovery;

	public TurbineRouteLocator(ZuulRoute route, String servletPath,
			ZuulProperties zuulProperties, DiscoveryClient discovery) {
		super(servletPath, zuulProperties);
		this.routes = singletonMap(route.getPath(), route);
		this.discovery = discovery;
	}

	@Override
	protected Map<String, ZuulRoute> locateRoutes() {
		return this.routes;
	}

	@Override
	public Route getMatchingRoute(String path) {
		Route route = super.getMatchingRoute(path);
		if (route == null) {
			return route;
		}
		String location = route.getLocation();
		if (!(location.startsWith("http:") || location.startsWith("https:"))) {
			location = resolveServiceId(location);
		}

		String targetPath = route.getPath() + "/turbine.stream";

		return new Route(route.getId(), targetPath, location, route.getPrefix(),
				route.getRetryable(), route.getSensitiveHeaders());
	}

	private String resolveServiceId(String location) {
		List<ServiceInstance> instances = discovery.getInstances(location);
		if (instances.isEmpty()) {
			throw new IllegalStateException(
					"No instance found for serviceId '" + location + "'");
		}

		return instances.get(0).getUri().toString();
	}
}
