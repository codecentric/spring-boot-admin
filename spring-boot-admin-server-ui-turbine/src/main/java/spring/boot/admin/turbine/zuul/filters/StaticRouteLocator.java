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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cloud.netflix.zuul.filters.SimpleRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties.ZuulRoute;

/**
 * RouteLocator that uses static non-configurable routes.
 * 
 * @author Johannes Edmeier
 */
public class StaticRouteLocator extends SimpleRouteLocator {
	private final Map<String, ZuulRoute> routes = new HashMap<>();

	public StaticRouteLocator(Collection<ZuulRoute> routes, String servletPath,
			ZuulProperties properties) {
		super(servletPath, properties);
		for (ZuulRoute route : routes) {
			this.routes.put(route.getPath(), route);
		}
	}

	@Override
	protected Map<String, ZuulRoute> locateRoutes() {
		return Collections.unmodifiableMap(this.routes);
	}
}
