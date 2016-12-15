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
package de.codecentric.boot.admin.registry;

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.model.StatusInfo;
import de.codecentric.boot.admin.registry.store.SimpleApplicationStore;
import de.codecentric.boot.admin.web.client.HttpHeadersProvider;

public class StatusUpdaterTest {

	private StatusUpdater updater;
	private SimpleApplicationStore store;
	private RestTemplate template;
	private ApplicationEventPublisher publisher;

	@Before
	public void setup() {
		HttpHeadersProvider httpHeadersProvider = mock(HttpHeadersProvider.class);
		when(httpHeadersProvider.getHeaders(any(Application.class))).thenReturn(new HttpHeaders());

		store = new SimpleApplicationStore();
		template = mock(RestTemplate.class);
		updater = new StatusUpdater(template, store, httpHeadersProvider);
		publisher = mock(ApplicationEventPublisher.class);
		updater.setApplicationEventPublisher(publisher);
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void test_update_statusChanged() {
		when(template.exchange(eq("health"), eq(HttpMethod.GET), isA(HttpEntity.class),
				eq(Map.class))).thenReturn(
						ResponseEntity.ok().body((Map) Collections.singletonMap("status", "UP")));

		updater.updateStatus(
				Application.create("foo").withId("id").withHealthUrl("health").build());

		Application app = store.find("id");

		assertThat(app.getStatusInfo().getStatus(), CoreMatchers.is("UP"));
		verify(publisher)
				.publishEvent(argThat(CoreMatchers.isA(ClientApplicationStatusChangedEvent.class)));
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void test_update_statusUnchanged() {
		when(template.exchange(eq("health"), eq(HttpMethod.GET), isA(HttpEntity.class),
				eq(Map.class))).thenReturn(
						ResponseEntity.ok((Map) Collections.singletonMap("status", "UNKNOWN")));

		updater.updateStatus(
				Application.create("foo").withId("id").withHealthUrl("health").build());

		verify(publisher, never())
				.publishEvent(argThat(CoreMatchers.isA(ClientApplicationStatusChangedEvent.class)));
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void test_update_noBody() {
		// HTTP 200 - UP
		when(template.exchange(eq("health"), eq(HttpMethod.GET), isA(HttpEntity.class),
				eq(Map.class))).thenReturn(ResponseEntity.ok((Map) null));

		updater.updateStatus(
				Application.create("foo").withId("id").withHealthUrl("health").build());

		assertThat(store.find("id").getStatusInfo().getStatus(), CoreMatchers.is("UP"));

		// HTTP != 200 - DOWN
		when(template.exchange(eq("health"), eq(HttpMethod.GET), isA(HttpEntity.class),
				eq(Map.class))).thenReturn(ResponseEntity.status(503).body((Map) null));

		updater.updateStatus(
				Application.create("foo").withId("id").withHealthUrl("health").build());

		assertThat(store.find("id").getStatusInfo().getStatus(), CoreMatchers.is("DOWN"));
	}

	@Test
	public void test_update_offline() {
		when(template.exchange(eq("health"), eq(HttpMethod.GET), isA(HttpEntity.class),
				eq(Map.class))).thenThrow(new ResourceAccessException("error"));

		Application app = Application.create("foo").withId("id").withHealthUrl("health")
				.withStatusInfo(StatusInfo.ofUp()).build();
		updater.updateStatus(app);

		assertThat(store.find("id").getStatusInfo().getStatus(), CoreMatchers.is("OFFLINE"));
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void test_updateStatusForApplications() throws InterruptedException {
		updater.setStatusLifetime(100L);
		store.save(Application.create("foo").withId("id-1").withHealthUrl("health-1").build());
		Thread.sleep(120L); // Let the StatusInfo of id-1 expire
		store.save(Application.create("foo").withId("id-2").withHealthUrl("health-2").build());

		when(template.exchange(eq("health-1"), eq(HttpMethod.GET), isA(HttpEntity.class),
				eq(Map.class))).thenReturn(ResponseEntity.ok((Map) null));

		updater.updateStatusForAllApplications();

		assertThat(store.find("id-1").getStatusInfo().getStatus(), CoreMatchers.is("UP"));
		verify(template, never()).exchange(eq("health-2"), eq(HttpMethod.GET),
				isA(HttpEntity.class), eq(Map.class));
	}

}
