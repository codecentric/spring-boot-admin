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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import de.codecentric.boot.admin.services.ApplicationRegistrator;
import de.codecentric.boot.admin.services.RegistrationApplicationListener;
import de.codecentric.boot.admin.web.BasicAuthHttpRequestInterceptor;

@Configuration
@EnableConfigurationProperties({ AdminProperties.class, AdminClientProperties.class })
@Conditional(SpringBootAdminClientEnabledCondition.class)
public class SpringBootAdminClientAutoConfiguration {

	@Autowired
	private AdminClientProperties client;

	@Autowired
	private AdminProperties admin;

	/**
	 * Task that registers the application at the spring-boot-admin application.
	 */
	@Bean
	@ConditionalOnMissingBean
	public ApplicationRegistrator registrator() {
		return new ApplicationRegistrator(createRestTemplate(admin), admin, client);
	}

	protected RestTemplate createRestTemplate(AdminProperties admin) {
		RestTemplate template = new RestTemplate();
		template.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

		if (admin.getUsername() != null) {
			template.setInterceptors(Arrays.<ClientHttpRequestInterceptor> asList(
					new BasicAuthHttpRequestInterceptor(admin.getUsername(), admin.getPassword())));
		}

		return template;
	}

	/**
	 * ApplicationListener triggering registration after being ready/shutdown
	 */
	@Bean
	@ConditionalOnMissingBean
	public RegistrationApplicationListener registrationListener() {
		RegistrationApplicationListener listener = new RegistrationApplicationListener(
				registrator());
		listener.setAutoRegister(admin.isAutoRegistration());
		listener.setAutoDeregister(admin.isAutoDeregistration());
		listener.setRegisterPeriod(admin.getPeriod());
		return listener;
	}
}
