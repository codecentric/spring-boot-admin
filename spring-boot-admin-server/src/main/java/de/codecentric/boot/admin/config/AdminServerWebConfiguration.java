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

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.codecentric.boot.admin.event.ClientApplicationDeregisteredEvent;
import de.codecentric.boot.admin.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.event.RoutesOutdatedEvent;
import de.codecentric.boot.admin.journal.ApplicationEventJournal;
import de.codecentric.boot.admin.journal.web.JournalController;
import de.codecentric.boot.admin.registry.ApplicationRegistry;
import de.codecentric.boot.admin.registry.web.RegistryController;
import de.codecentric.boot.admin.web.AdminController;
import de.codecentric.boot.admin.web.PrefixHandlerMapping;
import de.codecentric.boot.admin.web.servlet.resource.ConcatenatingResourceResolver;
import de.codecentric.boot.admin.web.servlet.resource.PreferMinifiedFilteringResourceResolver;
import de.codecentric.boot.admin.web.servlet.resource.ResourcePatternResolvingResourceResolver;

@Configuration
public class AdminServerWebConfiguration extends WebMvcConfigurerAdapter
		implements ApplicationContextAware {
	private final ApplicationEventPublisher publisher;
	private final ServerProperties server;
	private final ResourcePatternResolver resourcePatternResolver;
	private final AdminServerProperties adminServerProperties;
	private ApplicationContext applicationContext;

	public AdminServerWebConfiguration(ApplicationEventPublisher publisher, ServerProperties server,
			ResourcePatternResolver resourcePatternResolver,
			AdminServerProperties adminServerProperties) {
		this.publisher = publisher;
		this.server = server;
		this.resourcePatternResolver = resourcePatternResolver;
		this.adminServerProperties = adminServerProperties;
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
		registry.addResourceHandler(adminServerProperties.getContextPath() + "/**")
				.addResourceLocations("classpath:/META-INF/spring-boot-admin-server-ui/")
				.resourceChain(true)
				.addResolver(new PreferMinifiedFilteringResourceResolver(".min"));

		registry.addResourceHandler(adminServerProperties.getContextPath() + "/all-modules.css")
				.resourceChain(true)
				.addResolver(new ResourcePatternResolvingResourceResolver(resourcePatternResolver,
						"classpath*:/META-INF/spring-boot-admin-server-ui/*/module.css"))
				.addResolver(new ConcatenatingResourceResolver("\n".getBytes()));

		registry.addResourceHandler(adminServerProperties.getContextPath() + "/all-modules.js")
				.resourceChain(true)
				.addResolver(new ResourcePatternResolvingResourceResolver(resourcePatternResolver,
						"classpath*:/META-INF/spring-boot-admin-server-ui/*/module.js"))
				.addResolver(new PreferMinifiedFilteringResourceResolver(".min"))
				.addResolver(new ConcatenatingResourceResolver(";\n".getBytes()));
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		String contextPath = adminServerProperties.getContextPath();
		if (StringUtils.hasText(contextPath)) {
			registry.addRedirectViewController(contextPath, server.getPath(contextPath) + "/");
		}
		registry.addViewController(contextPath + "/").setViewName("forward:index.html");
	}

	@Bean
	public PrefixHandlerMapping prefixHandlerMapping() {
		Map<String, Object> beans = applicationContext
				.getBeansWithAnnotation(AdminController.class);
		PrefixHandlerMapping prefixHandlerMapping = new PrefixHandlerMapping(
				beans.values().toArray(new Object[beans.size()]));
		prefixHandlerMapping.setPrefix(adminServerProperties.getContextPath());
		return prefixHandlerMapping;
	}

	@Bean
	@ConditionalOnMissingBean
	public RegistryController registryController(ApplicationRegistry applicationRegistry) {
		return new RegistryController(applicationRegistry);
	}

	@Bean
	@ConditionalOnMissingBean
	public JournalController journalController(ApplicationEventJournal applicationEventJournal) {
		return new JournalController(applicationEventJournal);
	}

	@EventListener
	public void onClientApplicationRegistered(ClientApplicationRegisteredEvent event) {
		publisher.publishEvent(new RoutesOutdatedEvent());
	}

	@EventListener
	public void onClientApplicationDeregistered(ClientApplicationDeregisteredEvent event) {
		publisher.publishEvent(new RoutesOutdatedEvent());
	}

}
