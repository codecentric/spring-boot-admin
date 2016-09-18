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
package de.codecentric.boot.admin.notify;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.model.StatusInfo;

public class PagerdutyNotifierTest {

	private PagerdutyNotifier notifier;
	private RestTemplate restTemplate;

	@Before
	public void setUp() {
		restTemplate = mock(RestTemplate.class);
		notifier = new PagerdutyNotifier();
		notifier.setServiceKey("--service--");
		notifier.setClient("TestClient");
		notifier.setClientUrl(URI.create("http://localhost"));
		notifier.setRestTemplate(restTemplate);
	}

	@Test
	public void test_onApplicationEvent_resolve() {
		StatusInfo infoDown = StatusInfo.ofDown();
		StatusInfo infoUp = StatusInfo.ofUp();

		notifier.notify(new ClientApplicationStatusChangedEvent(
				Application.create("App").withId("-id-").withHealthUrl("http://health").build(),
				infoDown, infoUp));

		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put("service_key", "--service--");
		expected.put("incident_key", "App/-id-");
		expected.put("event_type", "resolve");
		expected.put("description", "App/-id- is UP");
		Map<String, Object> details = new HashMap<String, Object>();
		details.put("from", infoDown);
		details.put("to", infoUp);
		expected.put("details", details);

		verify(restTemplate).postForEntity(eq(PagerdutyNotifier.DEFAULT_URI), eq(expected),
				eq(Void.class));
	}

	@Test
	public void test_onApplicationEvent_trigger() {
		StatusInfo infoDown = StatusInfo.ofDown();
		StatusInfo infoUp = StatusInfo.ofUp();

		notifier.notify(new ClientApplicationStatusChangedEvent(
				Application.create("App").withId("-id-").withHealthUrl("http://health").build(),
				infoUp, infoDown));

		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put("service_key", "--service--");
		expected.put("incident_key", "App/-id-");
		expected.put("event_type", "trigger");
		expected.put("description", "App/-id- is DOWN");
		expected.put("client", "TestClient");
		expected.put("client_url", URI.create("http://localhost"));
		Map<String, Object> details = new HashMap<String, Object>();
		details.put("from", infoUp);
		details.put("to", infoDown);
		expected.put("details", details);
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("type", "link");
		context.put("href", "http://health");
		context.put("text", "Application health-endpoint");
		expected.put("contexts", Arrays.asList(context));

		verify(restTemplate).postForEntity(eq(PagerdutyNotifier.DEFAULT_URI), eq(expected),
				eq(Void.class));
	}

}
