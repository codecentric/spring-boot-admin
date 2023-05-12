/*
 * Copyright 2014-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.server.utils.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEndpointsDetectedEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceInfoChangedEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegistrationUpdatedEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;

import static org.assertj.core.api.Assertions.assertThat;

public class InstanceEventMixinTest {

	private final ObjectMapper objectMapper;

	public InstanceEventMixinTest() {
		AdminServerModule adminServerModule = new AdminServerModule(new String[] { ".*password$" });
		JavaTimeModule javaTimeModule = new JavaTimeModule();
		objectMapper = Jackson2ObjectMapperBuilder.json().modules(adminServerModule, javaTimeModule).build();
	}

	@Nested
	public class InstanceEventTests {

		private JacksonTester<InstanceEvent> jsonTester;

		@BeforeEach
		public void setup() {
			JacksonTester.initFields(this, objectMapper);
		}

		@Test
		public void verifyDeserializeOfInstanceDeregisteredEvent() throws JSONException, JsonProcessingException {
			String json = new JSONObject().put("instance", "test123")
				.put("timestamp", 1587751031.000000000)
				.put("type", "DEREGISTERED")
				.toString();

			InstanceEvent event = objectMapper.readValue(json, InstanceEvent.class);
			assertThat(event).isInstanceOf(InstanceDeregisteredEvent.class);
		}

		@Test
		public void verifyDeserializeOfInstanceEndpointsDetectedEvent() throws JSONException, JsonProcessingException {
			String json = new JSONObject().put("instance", "test123")
				.put("timestamp", 1587751031.000000000)
				.put("type", "ENDPOINTS_DETECTED")
				.toString();

			InstanceEvent event = objectMapper.readValue(json, InstanceEvent.class);
			assertThat(event).isInstanceOf(InstanceEndpointsDetectedEvent.class);
		}

		@Test
		public void verifyDeserializeOfInstanceInfoChangedEvent() throws JSONException, JsonProcessingException {
			String json = new JSONObject().put("instance", "test123")
				.put("timestamp", 1587751031.000000000)
				.put("type", "INFO_CHANGED")
				.toString();

			InstanceEvent event = objectMapper.readValue(json, InstanceEvent.class);
			assertThat(event).isInstanceOf(InstanceInfoChangedEvent.class);
		}

		@Test
		public void verifyDeserializeOfInstanceRegisteredEvent() throws JSONException, JsonProcessingException {
			String json = new JSONObject().put("instance", "test123")
				.put("timestamp", 1587751031.000000000)
				.put("type", "REGISTERED")
				.put("registration",
						new JSONObject().put("name", "test").put("healthUrl", "http://localhost:9080/heath"))
				.toString();

			InstanceEvent event = objectMapper.readValue(json, InstanceEvent.class);
			assertThat(event).isInstanceOf(InstanceRegisteredEvent.class);
		}

		@Test
		public void verifyDeserializeOfInstanceRegistrationUpdatedEvent()
				throws JSONException, JsonProcessingException {
			String json = new JSONObject().put("instance", "test123")
				.put("timestamp", 1587751031.000000000)
				.put("type", "REGISTRATION_UPDATED")
				.put("registration",
						new JSONObject().put("name", "test").put("healthUrl", "http://localhost:9080/heath"))
				.toString();

			InstanceEvent event = objectMapper.readValue(json, InstanceEvent.class);
			assertThat(event).isInstanceOf(InstanceRegistrationUpdatedEvent.class);
		}

		@Test
		public void verifyDeserializeOfInstanceStatusChangedEvent() throws JSONException, JsonProcessingException {
			String json = new JSONObject().put("instance", "test123")
				.put("timestamp", 1587751031.000000000)
				.put("type", "STATUS_CHANGED")
				.put("statusInfo", new JSONObject().put("status", "OFFLINE"))
				.toString();

			InstanceEvent event = objectMapper.readValue(json, InstanceEvent.class);
			assertThat(event).isInstanceOf(InstanceStatusChangedEvent.class);
		}

	}

}
