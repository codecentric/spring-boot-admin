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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import de.codecentric.boot.admin.controller.RegistryController;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.registry.ApplicationIdGenerator;
import de.codecentric.boot.admin.registry.ApplicationRegistry;
import de.codecentric.boot.admin.registry.HashingApplicationUrlIdGenerator;
import de.codecentric.boot.admin.registry.store.ApplicationStore;
import de.codecentric.boot.admin.registry.store.HazelcastApplicationStore;
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
	public ApplicationRegistry applicationRegistry(ApplicationStore applicationStore,
			ApplicationIdGenerator applicationIdGenerator) {
		return new ApplicationRegistry(applicationStore, applicationIdGenerator);
	}

	/**
	 * Default applicationId Generator
	 */
	@Bean
	@ConditionalOnMissingBean
	public ApplicationIdGenerator applicationIdGenerator() {
		return new HashingApplicationUrlIdGenerator();
	}

	@Configuration
	public static class SimpleConfig {
		@Bean
		@ConditionalOnMissingBean
		public ApplicationStore applicationStore() {
			return new SimpleApplicationStore();
		}
	}

	@Configuration
	@ConditionalOnClass({ Hazelcast.class })
	@ConditionalOnExpression("${spring.boot.admin.hazelcast.enable:true}")
	@AutoConfigureBefore(SimpleConfig.class)
	public static class HazelcastConfig {

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
			return new HazelcastApplicationStore(hazelcast.<String, Application> getMap(hazelcastMapName));
		}
	}

}
