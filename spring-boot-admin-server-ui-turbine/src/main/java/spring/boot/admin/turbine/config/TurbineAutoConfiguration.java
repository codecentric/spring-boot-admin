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
package spring.boot.admin.turbine.config;

import java.util.Collection;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties.ZuulRoute;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import de.codecentric.boot.admin.config.AdminServerProperties;
import de.codecentric.boot.admin.config.AdminServerWebConfiguration;
import de.codecentric.boot.admin.config.RevereseZuulProxyConfiguration;
import spring.boot.admin.turbine.web.TurbineController;
import spring.boot.admin.turbine.zuul.filters.StaticRouteLocator;

/**
 * Configures all necessary components for the Turbine view.
 *
 * @author Johannes Edmeier
 */
@Configuration
@EnableConfigurationProperties(TurbineProperties.class)
@AutoConfigureBefore({ AdminServerWebConfiguration.class, RevereseZuulProxyConfiguration.class })
@Conditional(TurbineEnabledCondition.class)
public class TurbineAutoConfiguration {

	@Autowired
	private TurbineProperties properties;

	@Autowired
	private ServerProperties server;

	@Autowired
	private ZuulProperties zuulProperties;

	@Bean
	public TurbineController TurbineController() {
		return new TurbineController(properties.getClusters());
	}

	@Bean
	@Order(100)
	public StaticRouteLocator staticRouteLocator(AdminServerProperties admin) {
		Collection<ZuulRoute> routes = Collections
				.singleton(new ZuulRoute(admin.getContextPath() + "/api/turbine/stream/**",
						properties.getUrl().toString()));
		return new StaticRouteLocator(routes, server.getServletPrefix(), zuulProperties);
	}

}
