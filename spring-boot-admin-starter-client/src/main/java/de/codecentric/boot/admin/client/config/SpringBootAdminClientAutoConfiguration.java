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
package de.codecentric.boot.admin.client.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import de.codecentric.boot.admin.client.registration.ApplicationFactory;
import de.codecentric.boot.admin.client.registration.ApplicationRegistrator;
import de.codecentric.boot.admin.client.registration.DefaultApplicationFactory;
import de.codecentric.boot.admin.client.registration.RegistrationApplicationListener;

@Configuration
@EnableConfigurationProperties({ AdminProperties.class, AdminClientProperties.class })
@Conditional(SpringBootAdminClientEnabledCondition.class)
public class SpringBootAdminClientAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public ApplicationRegistrator registrator(AdminProperties admin,
			ApplicationFactory applicationFactory, RestTemplateBuilder restTemplBuilder) {
		RestTemplateBuilder builder = restTemplBuilder
				.messageConverters(new MappingJackson2HttpMessageConverter())
				.requestFactory(SimpleClientHttpRequestFactory.class);
		if (admin.getUsername() != null) {
			builder = builder.basicAuthorization(admin.getUsername(), admin.getPassword());
		}
		return new ApplicationRegistrator(builder.build(), admin, applicationFactory);
	}

	@Bean
	@ConditionalOnMissingBean
	public ApplicationFactory applicationFactory(AdminClientProperties client,
			ManagementServerProperties management, ServerProperties server,
			@Value("${endpoints.health.path:/${endpoints.health.id:health}}") String healthEndpointPath) {
		return new DefaultApplicationFactory(client, management, server, healthEndpointPath);
	}

	@Bean
	@Qualifier("registrationTaskScheduler")
	public TaskScheduler registrationTaskScheduler() {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setPoolSize(1);
		taskScheduler.setRemoveOnCancelPolicy(true);
		taskScheduler.setThreadNamePrefix("registrationTask");
		return taskScheduler;
	}

	@Bean
	@ConditionalOnMissingBean
	public RegistrationApplicationListener registrationListener(AdminProperties admin,
			ApplicationRegistrator registrator) {
		RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator,
				registrationTaskScheduler());
		listener.setAutoRegister(admin.isAutoRegistration());
		listener.setAutoDeregister(admin.isAutoDeregistration());
		listener.setRegisterPeriod(admin.getPeriod());
		return listener;
	}
}
