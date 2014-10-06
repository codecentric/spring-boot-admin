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

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import de.codecentric.boot.admin.controller.LogfileController;
import de.codecentric.boot.admin.controller.RefreshController;
import de.codecentric.boot.admin.services.SpringBootAdminRegistratorTask;
import de.codecentric.boot.admin.web.SimpleCORSFilter;

/**
 * This configuration adds a registrator bean to the spring context. This bean checks periodicaly, if the using
 * application is registered at the spring-boot-admin application. If not, it registers itself.
 */
@Configuration
public class SpringBootAdminClientAutoConfiguration {

	/**
	 * Task that registers the application at the spring-boot-admin application.
	 */
	@Bean
	@ConditionalOnProperty("spring.boot.admin.url")
	public Runnable registrator() {
		return new SpringBootAdminRegistratorTask();
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
	public ScheduledTaskRegistrar taskRegistrar() {
		ScheduledTaskRegistrar registrar = new ScheduledTaskRegistrar();
		registrar.addFixedRateTask(registrator(), 10000);
		return registrar;
	}

	/**
	 * Controller to do something with the application logfile(s).
	 */
	@Bean
	@ConditionalOnProperty("logging.file")
	public LogfileController logfileController() {
		return new LogfileController();
	}

	/**
	 * Controller to do a refresh on the application.
	 */
	@Bean
	public RefreshController refreshController() {
		return new RefreshController();
	}

}
