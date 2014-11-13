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

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import de.codecentric.boot.admin.controller.RegistryController;
import de.codecentric.boot.admin.registry.ApplicationIdGenerator;
import de.codecentric.boot.admin.registry.ApplicationRegistry;
import de.codecentric.boot.admin.registry.HashingApplicationUrlIdGenerator;
import de.codecentric.boot.admin.registry.store.ApplicationStore;
import de.codecentric.boot.admin.registry.store.SimpleApplicationStore;

@Configuration
public class WebappConfig extends WebMvcConfigurerAdapter {

	/**
	 * Add JSON MessageConverter to send JSON objects to web clients.
	 */
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(new MappingJackson2HttpMessageConverter());
	}

	/**
	 * Controller with REST-API for spring-boot applications to register itself.
	 */
	@Bean
	public RegistryController registryController(ApplicationRegistry registry) {
		return new RegistryController(registry);
	}

	/**
	 * Default registry for all registered application.
	 */
	@Bean
	@ConditionalOnMissingBean
	public ApplicationRegistry applicationRegistry(ApplicationStore applicationStore,
			ApplicationIdGenerator applicationIdGenerator) {
		return new ApplicationRegistry(applicationStore, applicationIdGenerator);
	}

	/**
	 * Default applicationId Generator
	 */
	@Bean
	@ConditionalOnMissingBean
	public HashingApplicationUrlIdGenerator applicationIdGenerator() {
		return new HashingApplicationUrlIdGenerator();
	}

	/**
	 * Default applicationId Generator
	 */
	@Bean
	@ConditionalOnMissingBean
	public ApplicationStore applicationStore() {
		return new SimpleApplicationStore();
	}

}
