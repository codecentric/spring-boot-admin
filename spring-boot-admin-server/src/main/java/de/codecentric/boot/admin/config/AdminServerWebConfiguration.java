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

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.actuate.trace.TraceRepository;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.noop.NoopDiscoveryClientAutoConfiguration;
import org.springframework.cloud.netflix.zuul.RoutesEndpoint;
import org.springframework.cloud.netflix.zuul.ZuulFilterInitializer;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.cloud.netflix.zuul.filters.ProxyRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.post.SendErrorFilter;
import org.springframework.cloud.netflix.zuul.filters.post.SendResponseFilter;
import org.springframework.cloud.netflix.zuul.filters.pre.DebugFilter;
import org.springframework.cloud.netflix.zuul.filters.pre.FormBodyWrapperFilter;
import org.springframework.cloud.netflix.zuul.filters.pre.PreDecorationFilter;
import org.springframework.cloud.netflix.zuul.filters.pre.Servlet30WrapperFilter;
import org.springframework.cloud.netflix.zuul.filters.route.SimpleHostRoutingFilter;
import org.springframework.cloud.netflix.zuul.web.ZuulController;
import org.springframework.cloud.netflix.zuul.web.ZuulHandlerMapping;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.config.Config;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapEvent;
import com.netflix.zuul.ZuulFilter;

import de.codecentric.boot.admin.controller.RegistryController;
import de.codecentric.boot.admin.discovery.ApplicationDiscoveryListener;
import de.codecentric.boot.admin.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.event.ClientApplicationUnregisteredEvent;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.registry.ApplicationIdGenerator;
import de.codecentric.boot.admin.registry.ApplicationRegistry;
import de.codecentric.boot.admin.registry.HashingApplicationUrlIdGenerator;
import de.codecentric.boot.admin.registry.store.ApplicationStore;
import de.codecentric.boot.admin.registry.store.HazelcastApplicationStore;
import de.codecentric.boot.admin.registry.store.SimpleApplicationStore;
import de.codecentric.boot.admin.zuul.ApplicationRouteLocator;
import de.codecentric.boot.admin.zuul.ApplicationRouteRefreshListener;

@Configuration
public class AdminServerWebConfiguration extends WebMvcConfigurerAdapter implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
		if (!hasConverter(converters, MappingJackson2HttpMessageConverter.class)) {
			ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().applicationContext(this.applicationContext)
					.build();
			converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
		}
	}

	private boolean hasConverter(List<HttpMessageConverter<?>> converters,
			Class<? extends HttpMessageConverter<?>> clazz) {
		for (HttpMessageConverter<?> converter : converters) {
			if (clazz.isInstance(converter)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param registry the backing Application registry.
	 * @return Controller with REST-API for spring-boot applications to register itself.
	 */
	@Bean
	public RegistryController registryController(ApplicationRegistry registry) {
		return new RegistryController(registry);
	}

	/**
	 * @param applicationStore the backing store
	 * @param applicationIdGenerator the id generator to use
	 * @return Default registry for all registered application.
	 */
	@Bean
	public ApplicationRegistry applicationRegistry(ApplicationStore applicationStore,
			ApplicationIdGenerator applicationIdGenerator) {
		return new ApplicationRegistry(applicationStore, applicationIdGenerator);
	}

	/**
	 * @return Default applicationId Generator
	 */
	@Bean
	@ConditionalOnMissingBean
	public ApplicationIdGenerator applicationIdGenerator() {
		return new HashingApplicationUrlIdGenerator();
	}

	@Configuration
	public static class SimpleStoreConfig {
		@Bean
		@ConditionalOnMissingBean
		public ApplicationStore applicationStore() {
			return new SimpleApplicationStore();
		}
	}

	@Configuration
	@EnableConfigurationProperties(ZuulProperties.class)
	public static class RevereseZuulProxyConfiguration {

		@Autowired(required = false)
		private TraceRepository traces;

		@Autowired
		private ZuulProperties zuulProperties;

		@Autowired
		private ServerProperties server;

		@Autowired
		private ApplicationRegistry registry;

		@Bean
		public ApplicationRouteLocator routeLocator() {
			return new ApplicationRouteLocator(this.server.getServletPrefix(), registry, this.zuulProperties,
					RegistryController.PATH);
		}

		@Bean
		public PreDecorationFilter preDecorationFilter() {
			return new PreDecorationFilter(routeLocator(), this.zuulProperties.isAddProxyHeaders());
		}

		@Bean
		public SimpleHostRoutingFilter simpleHostRoutingFilter() {
			ProxyRequestHelper helper = new ProxyRequestHelper();
			if (this.traces != null) {
				helper.setTraces(this.traces);
			}
			return new SimpleHostRoutingFilter(helper);
		}

		@Bean
		public ZuulController zuulController() {
			return new ZuulController();
		}

		@Bean
		public ZuulHandlerMapping zuulHandlerMapping(RouteLocator routes) {
			return new ZuulHandlerMapping(routes, zuulController());
		}

		// pre filters

		@Bean
		public FormBodyWrapperFilter formBodyWrapperFilter() {
			return new FormBodyWrapperFilter();
		}

		@Bean
		public DebugFilter debugFilter() {
			return new DebugFilter();
		}

		@Bean
		public Servlet30WrapperFilter servlet30WrapperFilter() {
			return new Servlet30WrapperFilter();
		}

		// post filters

		@Bean
		public SendResponseFilter sendResponseFilter() {
			return new SendResponseFilter();
		}

		@Bean
		public SendErrorFilter sendErrorFilter() {
			return new SendErrorFilter();
		}

		@Configuration
		protected static class ZuulFilterConfiguration {

			@Autowired
			private Map<String, ZuulFilter> filters;

			@Bean
			public ZuulFilterInitializer zuulFilterInitializer() {
				return new ZuulFilterInitializer(this.filters);
			}

		}

		@Bean
		public ApplicationRouteRefreshListener applicationRouteRefreshListener() {
			return new ApplicationRouteRefreshListener(routeLocator(), zuulHandlerMapping(routeLocator()));
		}

		@Configuration
		@ConditionalOnClass(Endpoint.class)
		protected static class RoutesEndpointConfiguration {

			@Autowired
			private ProxyRouteLocator routeLocator;

			@Bean
			public RoutesEndpoint zuulEndpoint() {
				return new RoutesEndpoint(this.routeLocator);
			}

		}

	}

	@Configuration
	@ConditionalOnClass({ Hazelcast.class })
	@ConditionalOnProperty(prefix = "spring.boot.admin.hazelcast", name = "enabled", matchIfMissing = true)
	@AutoConfigureBefore(SimpleStoreConfig.class)
	public static class HazelcastStoreConfiguration {

		@Value("${spring.boot.admin.hazelcast.map:spring-boot-admin-application-store}")
		private String hazelcastMapName;

		@Bean
		@ConditionalOnMissingBean
		public Config hazelcastConfig() {
			return new Config();
		}

		@Bean(destroyMethod = "shutdown")
		@ConditionalOnMissingBean
		public HazelcastInstance hazelcastInstance(Config hazelcastConfig) {
			return Hazelcast.newHazelcastInstance(hazelcastConfig);
		}

		@Bean
		@ConditionalOnMissingBean
		public ApplicationStore applicationStore(HazelcastInstance hazelcast) {
			IMap<String, Application> map = hazelcast.<String, Application> getMap(hazelcastMapName);
			map.addIndex("name", false);
			map.addEntryListener(entryListener(), false);
			return new HazelcastApplicationStore(map);
		}

		@Bean
		public EntryListener<String, Application> entryListener() {
			return new ApplicationEntryListener();
		}

		private static class ApplicationEntryListener implements EntryListener<String, Application> {
			@Autowired
			ApplicationContext context;

			@Override
			public void entryAdded(EntryEvent<String, Application> event) {
				context.publishEvent(new ClientApplicationRegisteredEvent(this,event.getValue()));
			}

			@Override
			public void entryRemoved(EntryEvent<String, Application> event) {
				context.publishEvent(new ClientApplicationUnregisteredEvent(this,event.getValue()));
			}

			@Override
			public void entryUpdated(EntryEvent<String, Application> event) {
				context.publishEvent(new ClientApplicationRegisteredEvent(this,event.getValue()));
			}

			@Override
			public void entryEvicted(EntryEvent<String, Application> event) {
				context.publishEvent(new ClientApplicationRegisteredEvent(this,null));
			}

			@Override
			public void mapEvicted(MapEvent event) {
				context.publishEvent(new ClientApplicationRegisteredEvent(this,null));
			}

			@Override
			public void mapCleared(MapEvent event) {
				context.publishEvent(new ClientApplicationRegisteredEvent(this,null));
			}
		}
	}

	@Configuration
	@ConditionalOnClass({ DiscoveryClient.class })
	@ConditionalOnProperty(prefix = "spring.boot.admin.discovery", name = "enabled", matchIfMissing = true)
	@AutoConfigureAfter({ NoopDiscoveryClientAutoConfiguration.class })
	public static class DiscoveryClientConfiguration {

		@Value("${spring.boot.admin.discovery.management.context-path:}")
		private String managementPath;

		@Autowired
		private DiscoveryClient discoveryClient;

		@Autowired
		private ApplicationRegistry registry;

		@Bean
		ApplicationListener<ApplicationEvent> applicationDiscoveryListener() {
			ApplicationDiscoveryListener listener = new ApplicationDiscoveryListener(discoveryClient, registry);
			listener.setManagementContextPath(managementPath);
			return listener;
		}
	}

}
