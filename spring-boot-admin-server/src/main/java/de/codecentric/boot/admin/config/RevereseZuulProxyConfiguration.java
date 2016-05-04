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
package de.codecentric.boot.admin.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.actuate.trace.TraceRepository;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.cloud.netflix.zuul.RoutesEndpoint;
import org.springframework.cloud.netflix.zuul.ZuulConfiguration;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.pre.PreDecorationFilter;
import org.springframework.cloud.netflix.zuul.filters.route.SimpleHostRoutingFilter;
import org.springframework.cloud.netflix.zuul.web.ZuulHandlerMapping;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.codecentric.boot.admin.event.RoutesOutdatedEvent;
import de.codecentric.boot.admin.registry.ApplicationRegistry;
import de.codecentric.boot.admin.zuul.ApplicationRouteLocator;

@Configuration
@AutoConfigureAfter({ AdminServerWebConfiguration.class })
public class RevereseZuulProxyConfiguration extends ZuulConfiguration {

	@Autowired(required = false)
	private TraceRepository traces;

	@Autowired
	private ServerProperties server;

	@Autowired
	private ApplicationRegistry registry;

	@Autowired
	private AdminServerProperties adminServer;

	@Autowired
	private ZuulHandlerMapping zuulHandlerMapping;

	@Bean
	@Override
	public ApplicationRouteLocator routeLocator() {
		return new ApplicationRouteLocator(this.server.getServletPrefix(), registry,
				adminServer.getContextPath() + "/api/applications/");
	}

	// pre filters
	@Bean
	public PreDecorationFilter preDecorationFilter() {
		return new PreDecorationFilter(routeLocator(), this.server.getServletPrefix(),
				new ZuulProperties(), new ProxyRequestHelper());
	}

	@Bean
	public SimpleHostRoutingFilter simpleHostRoutingFilter() {
		ProxyRequestHelper helper = new ProxyRequestHelper();
		if (this.traces != null) {
			helper.setTraces(this.traces);
		}
		return new SimpleHostRoutingFilter(helper, new ZuulProperties());
	}

	@Bean
	@Override
	public ApplicationListener<ApplicationEvent> zuulRefreshRoutesListener() {
		return new ZuulRefreshListener(zuulHandlerMapping);
	}

	@Configuration
	@ConditionalOnClass(Endpoint.class)
	protected static class RoutesEndpointConfiguration {
		@Bean
		public RoutesEndpoint zuulEndpoint(RouteLocator routeLocator) {
			return new RoutesEndpoint(routeLocator);
		}
	}

	private static class ZuulRefreshListener implements ApplicationListener<ApplicationEvent> {
		private ZuulHandlerMapping zuulHandlerMapping;

		public ZuulRefreshListener(ZuulHandlerMapping zuulHandlerMapping) {
			this.zuulHandlerMapping = zuulHandlerMapping;
		}

		@Override
		public void onApplicationEvent(ApplicationEvent event) {
			if (event instanceof PayloadApplicationEvent && ((PayloadApplicationEvent<?>) event)
					.getPayload() instanceof RoutesOutdatedEvent) {
				zuulHandlerMapping.setDirty(true);
			}
		}
	}
}
