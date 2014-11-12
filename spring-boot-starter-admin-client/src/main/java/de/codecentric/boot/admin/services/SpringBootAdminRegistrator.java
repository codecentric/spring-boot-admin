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
package de.codecentric.boot.admin.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import de.codecentric.boot.admin.config.AdminClientProperties;
import de.codecentric.boot.admin.config.AdminProperties;
import de.codecentric.boot.admin.model.Application;

/**
 * Registers the client application at spring-boot-admin-server
 */
public class SpringBootAdminRegistrator {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpringBootAdminRegistrator.class);

	public SpringBootAdminRegistrator(RestTemplate template, AdminProperties adminProps,
			AdminClientProperties clientProps) {
		this.clientProps = clientProps;
		this.adminProps = adminProps;
		this.template = template;
	}

	private AdminClientProperties clientProps;

	private AdminProperties adminProps;

	private final RestTemplate template;

	/**
	 * Registers the client application at spring-boot-admin-server.
	 * @return true if successful
	 */
	public boolean register() {
		Application app = createApplication();

		try {
			ResponseEntity<Application> response = template.postForEntity(
					adminProps.getUrl() + '/' + adminProps.getContextPath(), app, Application.class);

			if (response.getStatusCode().equals(HttpStatus.CREATED)) {
				LOGGER.info("Application registered itself as {}", response.getBody());
				return true;
			}
			else if (response.getStatusCode().equals(HttpStatus.CONFLICT)) {
				LOGGER.warn("Application failed to registered itself as {} because of conflict in registry.", app);
			}
			else {
				LOGGER.warn("Application failed to registered itself as {}. Response: {}", app, response.toString());
			}
		}
		catch (Exception ex) {
			LOGGER.warn("Failed to register application as {} at spring-boot-admin: {}", app, ex.getMessage());
		}

		return false;
	}

	protected Application createApplication() {
		Application app = new Application(clientProps.getUrl(), clientProps.getName());
		return app;
	}
}
