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

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import de.codecentric.boot.admin.config.AdminClientProperties;
import de.codecentric.boot.admin.config.AdminProperties;
import de.codecentric.boot.admin.model.Application;

public class SpringBootAdminRegistratorTest {

	@Test
	public void register_successful() {
		AdminProperties adminProps = new AdminProperties();
		adminProps.setUrl("http://sba:8080");

		AdminClientProperties clientProps = new AdminClientProperties();
		clientProps.setUrl("http://localhost:8080");
		clientProps.setName("AppName");

		RestTemplate restTemplate = mock(RestTemplate.class);
		when(restTemplate.postForEntity(isA(String.class), isA(Application.class), eq(Application.class))).thenReturn(
				new ResponseEntity<Application>(HttpStatus.CREATED));

		SpringBootAdminRegistrator registrator = new SpringBootAdminRegistrator(restTemplate, adminProps, clientProps);
		boolean result = registrator.register();

		assertTrue(result);
		verify(restTemplate).postForEntity("http://sba:8080/api/applications",
				new Application("http://localhost:8080", "AppName"), Application.class);
	}

	@Test
	public void register_failed() {
		AdminProperties adminProps = new AdminProperties();
		adminProps.setUrl("http://sba:8080");
		AdminClientProperties clientProps = new AdminClientProperties();
		clientProps.setUrl("http://localhost:8080");
		clientProps.setName("AppName");

		RestTemplate restTemplate = mock(RestTemplate.class);
		when(restTemplate.postForEntity(isA(String.class), isA(Application.class), eq(Application.class))).thenThrow(
				new RestClientException("Error"));

		SpringBootAdminRegistrator registrator = new SpringBootAdminRegistrator(restTemplate, adminProps, clientProps);
		boolean result = registrator.register();

		assertFalse(result);
	}

	@Test
	public void register_failed_conflict() {
		AdminProperties adminProps = new AdminProperties();
		adminProps.setUrl("http://sba:8080");
		AdminClientProperties clientProps = new AdminClientProperties();
		clientProps.setUrl("http://localhost:8080");
		clientProps.setName("AppName");

		RestTemplate restTemplate = mock(RestTemplate.class);
		when(restTemplate.postForEntity(isA(String.class), isA(Application.class), eq(Application.class))).thenReturn(
				new ResponseEntity<Application>(HttpStatus.CONFLICT));

		SpringBootAdminRegistrator registrator = new SpringBootAdminRegistrator(restTemplate, adminProps, clientProps);
		boolean result = registrator.register();

		assertFalse(result);
	}

}
