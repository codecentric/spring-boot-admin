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
package de.codecentric.boot.admin.registration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import de.codecentric.boot.admin.client.config.AdminProperties;
import de.codecentric.boot.admin.client.registration.Application;
import de.codecentric.boot.admin.client.registration.ApplicationFactory;
import de.codecentric.boot.admin.client.registration.ApplicationRegistrator;

public class ApplicationRegistratorTest {

	private AdminProperties adminProps;
	private ApplicationRegistrator registrator;
	private RestTemplate restTemplate;
	private HttpHeaders headers;

	@Before
	public void setup() {
		restTemplate = mock(RestTemplate.class);

		adminProps = new AdminProperties();
		adminProps.setUrl(new String[] { "http://sba:8080", "http://sba2:8080" });

		ApplicationFactory factory = mock(ApplicationFactory.class);
		when(factory.createApplication()).thenReturn(Application.create("AppName")
			.withManagementUrl("http://localhost:8080/mgmt")
			.withHealthUrl("http://localhost:8080/health")
			.withServiceUrl("http://localhost:8080")
		.build());

		registrator = new ApplicationRegistrator(restTemplate, adminProps, factory);

		headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void register_successful() {
		when(restTemplate.postForEntity(isA(String.class), isA(HttpEntity.class), eq(Map.class)))
				.thenReturn(new ResponseEntity<Map>(Collections.singletonMap("id", "-id-"),
						HttpStatus.CREATED));

		assertTrue(registrator.register());
		assertEquals("-id-", registrator.getRegisteredId());
		verify(restTemplate)
				.postForEntity("http://sba:8080/api/applications",
						new HttpEntity<>(Application.create("AppName")
								.withHealthUrl("http://localhost:8080/health")
								.withManagementUrl("http://localhost:8080/mgmt")
								.withServiceUrl("http://localhost:8080").build(), headers),
				Map.class);
	}

	@Test
	public void register_failed() {
		when(restTemplate.postForEntity(isA(String.class), isA(HttpEntity.class),
				eq(Application.class))).thenThrow(new RestClientException("Error"));

		assertFalse(registrator.register());
		assertEquals(null, registrator.getRegisteredId());
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void register_retry() {
		when(restTemplate.postForEntity(isA(String.class), isA(HttpEntity.class),
				eq(Application.class))).thenThrow(new RestClientException("Error"));
		when(restTemplate.postForEntity(isA(String.class), isA(HttpEntity.class), eq(Map.class)))
				.thenReturn(new ResponseEntity<Map>(Collections.singletonMap("id", "-id-"),
						HttpStatus.CREATED));

		assertTrue(registrator.register());
		assertEquals("-id-", registrator.getRegisteredId());
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void deregister() {
		when(restTemplate.postForEntity(isA(String.class), isA(HttpEntity.class), eq(Map.class)))
				.thenReturn(new ResponseEntity<Map>(Collections.singletonMap("id", "-id-"),
						HttpStatus.CREATED));
		registrator.register();
		assertEquals("-id-", registrator.getRegisteredId());
		registrator.deregister();
		assertEquals(null, registrator.getRegisteredId());

		verify(restTemplate).delete("http://sba:8080/api/applications/-id-");
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void register_multiple() {
		adminProps.setRegisterOnce(false);

		when(restTemplate.postForEntity(isA(String.class), isA(HttpEntity.class), eq(Map.class)))
				.thenReturn(new ResponseEntity<Map>(Collections.singletonMap("id", "-id-"),
						HttpStatus.CREATED));

		assertTrue(registrator.register());
		assertEquals("-id-", registrator.getRegisteredId());

		verify(restTemplate)
				.postForEntity("http://sba:8080/api/applications",
						new HttpEntity<>(Application.create("AppName")
								.withHealthUrl("http://localhost:8080/health")
								.withManagementUrl("http://localhost:8080/mgmt")
								.withServiceUrl("http://localhost:8080").build(), headers),
						Map.class);

		verify(restTemplate)
				.postForEntity("http://sba2:8080/api/applications",
						new HttpEntity<>(Application.create("AppName")
								.withHealthUrl("http://localhost:8080/health")
								.withManagementUrl("http://localhost:8080/mgmt")
								.withServiceUrl("http://localhost:8080").build(), headers),
						Map.class);
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void register_multiple_one_failure() {
		adminProps.setRegisterOnce(false);

		when(restTemplate.postForEntity(isA(String.class), isA(HttpEntity.class), eq(Map.class)))
				.thenReturn(new ResponseEntity<Map>(Collections.singletonMap("id", "-id-"),
						HttpStatus.CREATED))
				.thenThrow(new RestClientException("Error"));

		assertTrue(registrator.register());
		assertEquals("-id-", registrator.getRegisteredId());

		verify(restTemplate)
				.postForEntity("http://sba:8080/api/applications",
						new HttpEntity<>(Application.create("AppName")
								.withHealthUrl("http://localhost:8080/health")
								.withManagementUrl("http://localhost:8080/mgmt")
								.withServiceUrl("http://localhost:8080").build(), headers),
						Map.class);

		verify(restTemplate)
				.postForEntity("http://sba2:8080/api/applications",
						new HttpEntity<>(Application.create("AppName")
								.withHealthUrl("http://localhost:8080/health")
								.withManagementUrl("http://localhost:8080/mgmt")
								.withServiceUrl("http://localhost:8080").build(), headers),
						Map.class);
	}

	@Test
	public void register_multiple_all_failures() {
		adminProps.setRegisterOnce(false);

		when(restTemplate.postForEntity(isA(String.class), isA(HttpEntity.class), eq(Map.class)))
				.thenThrow(new RestClientException("Error"))
				.thenThrow(new RestClientException("Error"));

		assertFalse(registrator.register());
	}
}
