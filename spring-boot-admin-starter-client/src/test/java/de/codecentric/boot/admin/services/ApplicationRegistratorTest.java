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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import de.codecentric.boot.admin.config.AdminClientProperties;
import de.codecentric.boot.admin.config.AdminProperties;
import de.codecentric.boot.admin.model.Application;

public class ApplicationRegistratorTest {

	private ApplicationRegistrator registrator;
	private RestTemplate restTemplate;
	private HttpHeaders headers;

	@Before
	public void setup() {
		restTemplate = mock(RestTemplate.class);

		AdminProperties adminProps = new AdminProperties();
		adminProps.setUrl("http://sba:8080");

		AdminClientProperties clientProps = new AdminClientProperties();
		clientProps.setManagementUrl("http://localhost:8080/mgmt");
		clientProps.setHealthUrl("http://localhost:8080/health");
		clientProps.setServiceUrl("http://localhost:8080");
		clientProps.setName("AppName");

		registrator = new ApplicationRegistrator(restTemplate, adminProps, clientProps);

		headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
	}

	@Test
	public void register_successful() {
		when(
				restTemplate.postForEntity(isA(String.class), isA(HttpEntity.class),
						eq(Application.class))).thenReturn(
								new ResponseEntity<Application>(HttpStatus.CREATED));

		boolean result = registrator.register();

		assertTrue(result);
		verify(restTemplate).postForEntity(
				"http://sba:8080/api/applications",
				new HttpEntity<Application>(Application.create("AppName")
						.withHealthUrl("http://localhost:8080/health")
						.withManagementUrl("http://localhost:8080/mgmt")
						.withServiceUrl("http://localhost:8080").build(),
						headers), Application.class);
	}

	@Test
	public void register_failed() {
		when(
				restTemplate.postForEntity(isA(String.class), isA(HttpEntity.class),
						eq(Application.class))).thenThrow(
								new RestClientException("Error"));

		boolean result = registrator.register();

		assertFalse(result);
	}

	@Test
	public void deregister() {
		when(
				restTemplate.postForEntity(isA(String.class),
						isA(HttpEntity.class), eq(Application.class)))
						.thenReturn(
								new ResponseEntity<Application>(Application
										.create("foo").withId("-id-").build(),
										HttpStatus.CREATED));
		registrator.register();
		registrator.deregister();

		verify(restTemplate).delete("http://sba:8080/api/applications/-id-");
	}
}
