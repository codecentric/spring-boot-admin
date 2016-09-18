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

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import de.codecentric.boot.admin.config.AdminClientProperties;
import de.codecentric.boot.admin.config.AdminProperties;
import de.codecentric.boot.admin.model.Application;

/**
 * Registers the client application at spring-boot-admin-server
 */
public class ApplicationRegistrator {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationRegistrator.class);

	private static HttpHeaders HTTP_HEADERS = createHttpHeaders();

	private final AtomicReference<String> registeredId = new AtomicReference<>();

	private AdminClientProperties client;

	private AdminProperties admin;

	private final RestTemplate template;

	public ApplicationRegistrator(RestTemplate template, AdminProperties admin,
			AdminClientProperties client) {
		this.client = client;
		this.admin = admin;
		this.template = template;
	}

	private static HttpHeaders createHttpHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		return HttpHeaders.readOnlyHttpHeaders(headers);
	}

	/**
	 * Registers the client application at spring-boot-admin-server.
	 *
	 * @return true if successful registration on at least one admin server
	 */
	public boolean register() {
		boolean isRegistrationSuccessful = false;
		Application self = createApplication();
		for (String adminUrl : admin.getAdminUrl()) {
			try {
				@SuppressWarnings("rawtypes")
				ResponseEntity<Map> response = template.postForEntity(adminUrl,
						new HttpEntity<>(self, HTTP_HEADERS), Map.class);

				if (response.getStatusCode().equals(HttpStatus.CREATED)) {
					if (registeredId.compareAndSet(null, response.getBody().get("id").toString())) {
						LOGGER.info("Application registered itself as {}", response.getBody());
					} else {
						LOGGER.debug("Application refreshed itself as {}", response.getBody());
					}

					isRegistrationSuccessful = true;
					if (admin.isRegisterOnce()) {
						break;
					}
				} else {
					LOGGER.warn("Application failed to registered itself as {}. Response: {}", self,
							response.toString());
				}
			} catch (Exception ex) {
				LOGGER.warn("Failed to register application as {} at spring-boot-admin ({}): {}",
						self, admin.getAdminUrl(), ex.getMessage());
			}
		}

		return isRegistrationSuccessful;
	}

	public void deregister() {
		String id = registeredId.get();
		if (id != null) {
			for (String adminUrl : admin.getAdminUrl()) {
				try {
					template.delete(adminUrl + "/" + id);
					registeredId.compareAndSet(id, null);
					if (admin.isRegisterOnce()) {
						break;
					}
				} catch (Exception ex) {
					LOGGER.warn(
							"Failed to deregister application (id={}) at spring-boot-admin ({}): {}",
							id, adminUrl, ex.getMessage());
				}
			}
		}
	}

	protected Application createApplication() {
		return Application.create(client.getName()).withHealthUrl(client.getHealthUrl())
				.withManagementUrl(client.getManagementUrl()).withServiceUrl(client.getServiceUrl())
				.build();
	}
}
