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
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.codecentric.boot.admin.controller.JournalController;
import de.codecentric.boot.admin.controller.RegistryController;
import de.codecentric.boot.admin.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.journal.ApplicationEventJournal;
import de.codecentric.boot.admin.journal.store.JournaledEventStore;
import de.codecentric.boot.admin.journal.store.SimpleJournaledEventStore;
import de.codecentric.boot.admin.registry.ApplicationIdGenerator;
import de.codecentric.boot.admin.registry.ApplicationRegistry;
import de.codecentric.boot.admin.registry.HashingApplicationUrlIdGenerator;
import de.codecentric.boot.admin.registry.StatusUpdater;
import de.codecentric.boot.admin.registry.store.ApplicationStore;

@Configuration
@Import({ RevereseZuulProxyConfiguration.class, MailNotifierConfiguration.class,
		HazelcastStoreConfiguration.class, SimpleStoreConfig.class,
		DiscoveryClientConfiguration.class })
public class AdminServerWebConfiguration extends WebMvcConfigurerAdapter implements
		ApplicationContextAware {

	private ApplicationContext applicationContext;

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

	@Bean
	@ConditionalOnMissingBean
	@ConfigurationProperties("spring.boot.admin.monitor")
	public StatusUpdater statusUpdater(ApplicationStore store) {
		RestTemplate template = new RestTemplate();
		template.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		template.setErrorHandler(new DefaultResponseErrorHandler() {
			@Override
			protected boolean hasError(HttpStatus statusCode) {
				return false;
			}
		});
		return new StatusUpdater(template, store);
	}

	@Bean
	public ApplicationListener<ClientApplicationRegisteredEvent> updateRegistrationListener(
			final StatusUpdater updater) {
		return new ApplicationListener<ClientApplicationRegisteredEvent>() {
			@Override
			public void onApplicationEvent(ClientApplicationRegisteredEvent event) {
				updater.updateStatus(event.getApplication());
			}
		};
	}

	@Bean
	public ScheduledTaskRegistrar updateTaskRegistrar(final StatusUpdater updater,
			@Value("${spring.boot.admin.monitor.period:10000}") long monitorPeriod) {
		ScheduledTaskRegistrar registrar = new ScheduledTaskRegistrar();
		Runnable registratorTask = new Runnable() {
			@Override
			public void run() {
				updater.updateStatusForAllApplications();
			}
		};

		registrar.addFixedRateTask(registratorTask, monitorPeriod);
		return registrar;
	}

	@Bean
	@ConditionalOnMissingBean
	public ApplicationEventJournal applicationEventJournal(JournaledEventStore store) {
		return new ApplicationEventJournal(store);
	}

	@Bean
	@ConditionalOnMissingBean
	public JournaledEventStore journaledEventStore() {
		return new SimpleJournaledEventStore();
	}

	@Bean
	@ConditionalOnMissingBean
	public JournalController journalController(ApplicationEventJournal eventJournal) {
		return new JournalController(eventJournal);
	}

}
