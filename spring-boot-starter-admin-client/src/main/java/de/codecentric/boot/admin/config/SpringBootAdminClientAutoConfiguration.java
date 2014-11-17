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

import java.util.Arrays;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.web.client.RestTemplate;

import de.codecentric.boot.admin.actuate.LogfileMvcEndpoint;
import de.codecentric.boot.admin.services.SpringBootAdminRegistrator;
import de.codecentric.boot.admin.web.BasicAuthHttpRequestInterceptor;
import de.codecentric.boot.admin.web.SimpleCORSFilter;

/**
 * This configuration adds a registrator bean to the spring context. This bean checks periodicaly, if the using
 * application is registered at the spring-boot-admin application. If not, it registers itself.
 */
@Configuration
@ConditionalOnProperty("spring.boot.admin.url")
@EnableConfigurationProperties({ AdminProperties.class, AdminClientProperties.class })
public class SpringBootAdminClientAutoConfiguration {

	/**
	 * Task that registers the application at the spring-boot-admin application.
	 */
	@Bean
	@ConditionalOnMissingBean
	public SpringBootAdminRegistrator registrator(AdminProperties adminProps, AdminClientProperties clientProps) {
		return new SpringBootAdminRegistrator(createRestTemplate(adminProps), adminProps, clientProps);
	}

	protected RestTemplate createRestTemplate(AdminProperties adminProps) {
		RestTemplate template = new RestTemplate();
		template.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

		if (adminProps.getUsername() != null) {
			template.setInterceptors(Arrays.<ClientHttpRequestInterceptor> asList(new BasicAuthHttpRequestInterceptor(
					adminProps.getUsername(), adminProps.getPassword())));
		}

		return template;
	}

	/**
	 * HTTP filter to enable Cross-Origin Resource Sharing.
	 */
	@Bean
	public SimpleCORSFilter corsFilter() {
		return new SimpleCORSFilter();
	}

	/**
	 * TaskRegistrar that triggers the RegistratorTask every ten seconds.
	 */
	@Bean
	public ScheduledTaskRegistrar taskRegistrar(final SpringBootAdminRegistrator registrator, AdminProperties adminProps) {
		ScheduledTaskRegistrar registrar = new ScheduledTaskRegistrar();

		Runnable registratorTask = new Runnable() {
			@Override
			public void run() {
				registrator.register();
			}
		};

		registrar.addFixedRateTask(registratorTask, adminProps.getPeriod());
		return registrar;
	}

	@Configuration
	@ConditionalOnExpression("${endpoints.logfile.enabled:true}")
	public static class LogfileEndpointAutoConfiguration {
		/**
		 * Exposes the logfile as acutator endpoint
		 */
		@Bean
		public LogfileMvcEndpoint logfileEndpoint() {
			return new LogfileMvcEndpoint();
		}
	}

}
