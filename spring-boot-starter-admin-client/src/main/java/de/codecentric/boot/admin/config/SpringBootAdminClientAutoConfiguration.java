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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.actuate.endpoint.mvc.EndpointHandlerMapping;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;

import de.codecentric.boot.admin.actuate.LogfileMvcEndpoint;
import de.codecentric.boot.admin.services.SpringBootAdminRegistrator;
import de.codecentric.boot.admin.web.BasicAuthHttpRequestInterceptor;
import de.codecentric.boot.admin.web.SimpleCORSHandlerInterceptor;

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

	/**
	 * HTTP filter to enable Cross-Origin Resource Sharing.
	 */
	@Bean
	@ConditionalOnMissingBean
	public SimpleCORSHandlerInterceptor endpointCorsFilter() {
		SimpleCORSHandlerInterceptor endpointCorsFilter = new SimpleCORSHandlerInterceptor();
		return endpointCorsFilter;
	}

	@Bean
	public ApplicationListener<EmbeddedServletContainerInitializedEvent> listener() {
		return new  ApplicationListener<EmbeddedServletContainerInitializedEvent>() {
			@Override
			public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event) {
				// We have to register the CORSHandler in this nasty way.
				// With Spring Boot < 1.2.0.RC2 there is no elegant way to register
				// HandlerInterceptors for the EndpointMapping
				for (EndpointHandlerMapping endpointMappingHandler : event.getApplicationContext()
						.getBeansOfType(EndpointHandlerMapping.class).values()) {

					try {
						Field interceptorsField = AbstractHandlerMapping.class.getDeclaredField("adaptedInterceptors");
						interceptorsField.setAccessible(true);
						@SuppressWarnings("unchecked")
						List<HandlerInterceptor> adaptedInterceptors = (List<HandlerInterceptor>) interceptorsField
						.get(endpointMappingHandler);
						adaptedInterceptors.add(endpointCorsFilter());
					}
					catch (Exception ex) {
						throw new RuntimeException("Couldn't register CORS-Filter for endpoints", ex);
					}
				}
			}
		};
	}

	@Configuration
	@ConditionalOnExpression("${endpoints.logfile.enabled:true}")
	@ConditionalOnProperty("logging.file")
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
