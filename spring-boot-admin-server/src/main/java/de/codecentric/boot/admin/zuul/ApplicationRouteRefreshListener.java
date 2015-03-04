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

import org.springframework.cloud.netflix.zuul.web.ZuulHandlerMapping;
import org.springframework.context.ApplicationListener;

import de.codecentric.boot.admin.event.ClientApplicationEvent;

/**
 * Listener to trigger recomputation of the applications' routes.
 * @author Johannes Stelzer
 *
 */
public class ApplicationRouteRefreshListener implements ApplicationListener<ClientApplicationEvent> {

	private final ApplicationRouteLocator routeLocator;
	private final ZuulHandlerMapping zuulHandlerMapping;

	public ApplicationRouteRefreshListener(ApplicationRouteLocator routeLocator, ZuulHandlerMapping zuulHandlerMapping) {
		this.routeLocator = routeLocator;
		this.zuulHandlerMapping = zuulHandlerMapping;
	}

	@Override
	public void onApplicationEvent(ClientApplicationEvent event) {
		this.routeLocator.resetRoutes();
		this.zuulHandlerMapping.registerHandlers();
	}

}
