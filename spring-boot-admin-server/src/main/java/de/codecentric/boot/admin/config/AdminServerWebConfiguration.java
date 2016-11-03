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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.StringUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.codecentric.boot.admin.event.ClientApplicationDeregisteredEvent;
import de.codecentric.boot.admin.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.event.RoutesOutdatedEvent;
import de.codecentric.boot.admin.journal.ApplicationEventJournal;
import de.codecentric.boot.admin.journal.store.JournaledEventStore;
import de.codecentric.boot.admin.journal.store.SimpleJournaledEventStore;
import de.codecentric.boot.admin.journal.web.JournalController;
import de.codecentric.boot.admin.registry.ApplicationIdGenerator;
import de.codecentric.boot.admin.registry.ApplicationRegistry;
import de.codecentric.boot.admin.registry.HashingApplicationUrlIdGenerator;
import de.codecentric.boot.admin.registry.StatusUpdateApplicationListener;
import de.codecentric.boot.admin.registry.StatusUpdater;
import de.codecentric.boot.admin.registry.store.ApplicationStore;
import de.codecentric.boot.admin.registry.store.SimpleApplicationStore;
import de.codecentric.boot.admin.registry.web.RegistryController;
import de.codecentric.boot.admin.web.AdminController;
import de.codecentric.boot.admin.web.PrefixHandlerMapping;
import de.codecentric.boot.admin.web.servlet.resource.ConcatenatingResourceResolver;
import de.codecentric.boot.admin.web.servlet.resource.PreferMinifiedFilteringResourceResolver;
import de.codecentric.boot.admin.web.servlet.resource.ResourcePatternResolvingResourceResolver;

@Configuration
@EnableConfigurationProperties
public class AdminServerWebConfiguration extends WebMvcConfigurerAdapter
		implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	@Autowired
	private ApplicationEventPublisher publisher;

	@Autowired
	private ApplicationStore applicationStore;

	@Autowired
	private ServerProperties server;

	@Autowired
	private ResourcePatternResolver resourcePatternResolver;

	@Autowired
	private RestTemplateBuilder restTemplBuilder;

	@Bean
	@ConditionalOnMissingBean
	public AdminServerProperties adminServerProperties() {
		return new AdminServerProperties();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
		if (!hasConverter(converters, MappingJackson2HttpMessageConverter.class)) {
			ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json()
					.applicationContext(this.applicationContext).build();
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

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler(adminServerProperties().getContextPath() + "/**")
				.addResourceLocations("classpath:/META-INF/spring-boot-admin-server-ui/")
				.resourceChain(true)
				.addResolver(new PreferMinifiedFilteringResourceResolver(".min"));

		registry.addResourceHandler(adminServerProperties().getContextPath() + "/all-modules.css")
				.resourceChain(true)
				.addResolver(new ResourcePatternResolvingResourceResolver(resourcePatternResolver,
						"classpath*:/META-INF/spring-boot-admin-server-ui/*/module.css"))
				.addResolver(new ConcatenatingResourceResolver("\n".getBytes()));

		registry.addResourceHandler(adminServerProperties().getContextPath() + "/all-modules.js")
				.resourceChain(true)
				.addResolver(new ResourcePatternResolvingResourceResolver(resourcePatternResolver,
						"classpath*:/META-INF/spring-boot-admin-server-ui/*/module.js"))
				.addResolver(new PreferMinifiedFilteringResourceResolver(".min"))
				.addResolver(new ConcatenatingResourceResolver(";\n".getBytes()));
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		if (StringUtils.hasText(adminServerProperties().getContextPath())) {
			registry.addRedirectViewController(adminServerProperties().getContextPath(),
					server.getPath(adminServerProperties().getContextPath()) + "/");
		}
		registry.addViewController(adminServerProperties().getContextPath() + "/")
				.setViewName("forward:index.html");
	}

	@Bean
	public PrefixHandlerMapping prefixHandlerMapping() {
		Map<String, Object> beans = applicationContext
				.getBeansWithAnnotation(AdminController.class);
		PrefixHandlerMapping prefixHandlerMapping = new PrefixHandlerMapping(
				beans.values().toArray(new Object[beans.size()]));
		prefixHandlerMapping.setPrefix(adminServerProperties().getContextPath());
		return prefixHandlerMapping;
	}

	/**
	 * @return Controller with REST-API for spring-boot applications to register itself.
	 */
	@Bean
	public RegistryController registryController() {
		return new RegistryController(applicationRegistry());
	}

	/**
	 * @return Default registry for all registered application.
	 */
	@Bean
	public ApplicationRegistry applicationRegistry() {
		return new ApplicationRegistry(applicationStore, applicationIdGenerator());
	}

	/**
	 * @return Default applicationId Generator
	 */
	@Bean
	@ConditionalOnMissingBean
	public ApplicationIdGenerator applicationIdGenerator() {
		return new HashingApplicationUrlIdGenerator();
	}

	@Bean
	@ConditionalOnMissingBean
	public StatusUpdater statusUpdater() {
		RestTemplateBuilder builder = restTemplBuilder
				.messageConverters(new MappingJackson2HttpMessageConverter())
				.errorHandler(new DefaultResponseErrorHandler() {
			@Override
			protected boolean hasError(HttpStatus statusCode) {
				return false;
			}
		});

		StatusUpdater statusUpdater = new StatusUpdater(builder.build(), applicationStore);
		statusUpdater.setStatusLifetime(adminServerProperties().getMonitor().getStatusLifetime());
		return statusUpdater;
	}

	@Bean
	@Qualifier("updateTaskScheduler")
	public TaskScheduler updateTaskScheduler() {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setPoolSize(1);
		taskScheduler.setRemoveOnCancelPolicy(true);
		taskScheduler.setThreadNamePrefix("updateTask");
		return taskScheduler;
	}

	@Bean
	public StatusUpdateApplicationListener statusUpdateApplicationListener() {
		StatusUpdateApplicationListener listener = new StatusUpdateApplicationListener(
				statusUpdater(), updateTaskScheduler());
		listener.setUpdatePeriod(adminServerProperties().getMonitor().getPeriod());
		return listener;
	}

	@EventListener
	public void onClientApplicationRegistered(ClientApplicationRegisteredEvent event) {
		publisher.publishEvent(new RoutesOutdatedEvent());
	}

	@EventListener
	public void onClientApplicationDeregistered(ClientApplicationDeregisteredEvent event) {
		publisher.publishEvent(new RoutesOutdatedEvent());
	}

	@Bean
	@ConditionalOnMissingBean
	public ApplicationEventJournal applicationEventJournal() {
		return new ApplicationEventJournal(journaledEventStore());
	}

	@Bean
	@ConditionalOnMissingBean
	public JournaledEventStore journaledEventStore() {
		return new SimpleJournaledEventStore();
	}

	@Bean
	@ConditionalOnMissingBean
	public JournalController journalController() {
		return new JournalController(applicationEventJournal());
	}

	@Bean
	@ConditionalOnMissingBean
	public ApplicationStore applicationStore() {
		return new SimpleApplicationStore();
	}

}
