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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.mvc.EndpointHandlerMapping;
import org.springframework.boot.actuate.endpoint.mvc.JolokiaMvcEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;
import org.springframework.web.servlet.mvc.ServletWrappingController;

import de.codecentric.boot.admin.actuate.LogfileMvcEndpoint;
import de.codecentric.boot.admin.services.SpringBootAdminRegistrator;
import de.codecentric.boot.admin.web.BasicAuthHttpRequestInterceptor;
import de.codecentric.boot.admin.web.EndpointCorsFilter;
import de.codecentric.boot.admin.web.EndpointCorsInterceptor;

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


	/**
	 * HTTP filter to enable Cross-Origin Resource Sharing.
	 */
	@Bean
	@ConditionalOnMissingBean
	public EndpointCorsFilter endpointCorsFilter(EndpointHandlerMapping endpointHandlerMapping) {
		return new EndpointCorsFilter(endpointHandlerMapping);
	}

	@Autowired
	private ApplicationContext applicationContext;

	@Bean
	public ApplicationListener<EmbeddedServletContainerInitializedEvent> appListener() {
		/*
		 * In case a second servletContainer is fired up (because server.port !=
		 * managament port), there is no viable way to register the endpointCorsFilter.
		 *
		 * Instead we register an HandlerInterceptor for the Endpoint handler mapping and
		 * Set jolokias AgentServlet to support Options request and the Dispatcher servlet
		 * to forward such.
		 * Also @see https://github.com/spring-projects/spring-boot/issues/1987
		 */
		return new ApplicationListener<EmbeddedServletContainerInitializedEvent>() {
			@Override
			public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event) {
				if ("management".equals(event.getApplicationContext().getNamespace())) {
					// register HandlerIntercepor
					for (EndpointHandlerMapping handlerMapping : event.getApplicationContext()
							.getBeansOfType(EndpointHandlerMapping.class).values()) {
						try {
							Field interceptorsField = AbstractHandlerMapping.class.getDeclaredField("adaptedInterceptors");
							interceptorsField.setAccessible(true);
							@SuppressWarnings("unchecked")
							List<HandlerInterceptor> adaptedInterceptors = (List<HandlerInterceptor>) interceptorsField
							.get(handlerMapping);
							EndpointCorsInterceptor interceptor = new EndpointCorsInterceptor();
							event.getApplicationContext().getBeanFactory().autowireBean(interceptor);
							adaptedInterceptors.add(interceptor);
						}
						catch (Exception ex) {
							throw new RuntimeException("Couldn't add handlerInterceptor for cors", ex);
						}
					}

					// set DispatcherServlet to forward OptionsRequest
					for (DispatcherServlet servlet : event.getApplicationContext()
							.getBeansOfType(DispatcherServlet.class).values()) {
						servlet.setDispatchOptionsRequest(true);
					}

					// set Jolokias ServletWrappingController to support OPTIONS
					for (JolokiaMvcEndpoint jolokiaMvcEndpoint : SpringBootAdminClientAutoConfiguration.this.applicationContext
							.getBeansOfType(JolokiaMvcEndpoint.class).values()) {
						try {
							Field controllerField = JolokiaMvcEndpoint.class.getDeclaredField("controller");
							ReflectionUtils.makeAccessible(controllerField);
							ServletWrappingController controller = (ServletWrappingController) controllerField
									.get(jolokiaMvcEndpoint);
							controller.setSupportedMethods("GET", "HEAD", "POST", "OPTIONS");
						}
						catch (Exception ex) {
							throw new RuntimeException("Couldn't reconfigure servletWrappingController for Jolokia", ex);
						}
					}
				}
			}
		};
	}

}
