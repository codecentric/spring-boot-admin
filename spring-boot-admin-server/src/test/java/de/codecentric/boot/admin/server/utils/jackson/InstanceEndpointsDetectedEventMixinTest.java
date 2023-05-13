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

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import de.codecentric.boot.admin.server.domain.events.InstanceEndpointsDetectedEvent;
import de.codecentric.boot.admin.server.domain.values.Endpoint;
import de.codecentric.boot.admin.server.domain.values.Endpoints;
import de.codecentric.boot.admin.server.domain.values.InstanceId;

import static org.assertj.core.api.Assertions.assertThat;

public class InstanceEndpointsDetectedEventMixinTest {

	private final ObjectMapper objectMapper;

	private JacksonTester<InstanceEndpointsDetectedEvent> jsonTester;

	public InstanceEndpointsDetectedEventMixinTest() {
		AdminServerModule adminServerModule = new AdminServerModule(new String[] { ".*password$" });
		JavaTimeModule javaTimeModule = new JavaTimeModule();
		objectMapper = Jackson2ObjectMapperBuilder.json().modules(adminServerModule, javaTimeModule).build();
	}

	@BeforeEach
	public void setup() {
		JacksonTester.initFields(this, objectMapper);
	}

	@Test
	public void verifyDeserialize() throws JSONException, JsonProcessingException {
		String json = new JSONObject().put("instance", "test123")
			.put("version", 12345678L)
			.put("timestamp", 1587751031.000000000)
			.put("type", "ENDPOINTS_DETECTED")
			.put("endpoints",
					new JSONArray().put(new JSONObject().put("id", "info").put("url", "http://localhost:8080/info"))
						.put(new JSONObject().put("id", "health").put("url", "http://localhost:8080/health")))
			.toString();

		InstanceEndpointsDetectedEvent event = objectMapper.readValue(json, InstanceEndpointsDetectedEvent.class);
		assertThat(event).isNotNull();
		assertThat(event.getInstance()).isEqualTo(InstanceId.of("test123"));
		assertThat(event.getVersion()).isEqualTo(12345678L);
		assertThat(event.getTimestamp()).isEqualTo(Instant.ofEpochSecond(1587751031).truncatedTo(ChronoUnit.SECONDS));
		assertThat(event.getEndpoints()).containsExactlyInAnyOrder(Endpoint.of("info", "http://localhost:8080/info"),
				Endpoint.of("health", "http://localhost:8080/health"));
	}

	@Test
	public void verifyDeserializeWithEmptyEndpoints() throws JSONException, JsonProcessingException {
		String json = new JSONObject().put("instance", "test123")
			.put("version", 12345678L)
			.put("timestamp", 1587751031.000000000)
			.put("type", "ENDPOINTS_DETECTED")
			.put("endpoints", new JSONArray())
			.toString();

		InstanceEndpointsDetectedEvent event = objectMapper.readValue(json, InstanceEndpointsDetectedEvent.class);
		assertThat(event).isNotNull();
		assertThat(event.getInstance()).isEqualTo(InstanceId.of("test123"));
		assertThat(event.getVersion()).isEqualTo(12345678L);
		assertThat(event.getTimestamp()).isEqualTo(Instant.ofEpochSecond(1587751031).truncatedTo(ChronoUnit.SECONDS));
		assertThat(event.getEndpoints()).isEmpty();
	}

	@Test
	public void verifyDeserializeWithOnlyRequiredProperties() throws JSONException, JsonProcessingException {
		String json = new JSONObject().put("instance", "test123")
			.put("timestamp", 1587751031.000000000)
			.put("type", "ENDPOINTS_DETECTED")
			.toString();

		InstanceEndpointsDetectedEvent event = objectMapper.readValue(json, InstanceEndpointsDetectedEvent.class);
		assertThat(event).isNotNull();
		assertThat(event.getInstance()).isEqualTo(InstanceId.of("test123"));
		assertThat(event.getVersion()).isEqualTo(0L);
		assertThat(event.getTimestamp()).isEqualTo(Instant.ofEpochSecond(1587751031).truncatedTo(ChronoUnit.SECONDS));
		assertThat(event.getEndpoints()).isNull();
	}

	@Test
	public void verifySerialize() throws IOException {
		InstanceId id = InstanceId.of("test123");
		Instant timestamp = Instant.ofEpochSecond(1587751031).truncatedTo(ChronoUnit.SECONDS);
		Endpoints endpoints = Endpoints.single("info", "http://localhost:8080/info")
			.withEndpoint("health", "http://localhost:8080/health");
		InstanceEndpointsDetectedEvent event = new InstanceEndpointsDetectedEvent(id, 12345678L, timestamp, endpoints);

		JsonContent<InstanceEndpointsDetectedEvent> jsonContent = jsonTester.write(event);
		assertThat(jsonContent).extractingJsonPathStringValue("$.instance").isEqualTo("test123");
		assertThat(jsonContent).extractingJsonPathNumberValue("$.version").isEqualTo(12345678);
		assertThat(jsonContent).extractingJsonPathNumberValue("$.timestamp").isEqualTo(1587751031.000000000);
		assertThat(jsonContent).extractingJsonPathStringValue("$.type").isEqualTo("ENDPOINTS_DETECTED");
		assertThat(jsonContent).extractingJsonPathArrayValue("$.endpoints").hasSize(2);

		assertThat(jsonContent).extractingJsonPathStringValue("$.endpoints[0].id").isIn("info", "health");
		assertThat(jsonContent).extractingJsonPathStringValue("$.endpoints[0].url")
			.isIn("http://localhost:8080/info", "http://localhost:8080/health");

		assertThat(jsonContent).extractingJsonPathStringValue("$.endpoints[1].id").isIn("info", "health");
		assertThat(jsonContent).extractingJsonPathStringValue("$.endpoints[1].url")
			.isIn("http://localhost:8080/info", "http://localhost:8080/health");
	}

	@Test
	public void verifySerializeWithOnlyRequiredProperties() throws IOException {
		InstanceId id = InstanceId.of("test123");
		Instant timestamp = Instant.ofEpochSecond(1587751031).truncatedTo(ChronoUnit.SECONDS);
		InstanceEndpointsDetectedEvent event = new InstanceEndpointsDetectedEvent(id, 0L, timestamp, null);

		JsonContent<InstanceEndpointsDetectedEvent> jsonContent = jsonTester.write(event);
		assertThat(jsonContent).extractingJsonPathStringValue("$.instance").isEqualTo("test123");
		assertThat(jsonContent).extractingJsonPathNumberValue("$.version").isEqualTo(0);
		assertThat(jsonContent).extractingJsonPathNumberValue("$.timestamp").isEqualTo(1587751031.000000000);
		assertThat(jsonContent).extractingJsonPathStringValue("$.type").isEqualTo("ENDPOINTS_DETECTED");
		assertThat(jsonContent).extractingJsonPathArrayValue("$.endpoints").isNull();
	}

	@Test
	public void verifySerializeWithEmptyEndpoints() throws IOException {
		InstanceId id = InstanceId.of("test123");
		Instant timestamp = Instant.ofEpochSecond(1587751031).truncatedTo(ChronoUnit.SECONDS);
		InstanceEndpointsDetectedEvent event = new InstanceEndpointsDetectedEvent(id, 0L, timestamp, Endpoints.empty());

		JsonContent<InstanceEndpointsDetectedEvent> jsonContent = jsonTester.write(event);
		assertThat(jsonContent).extractingJsonPathStringValue("$.instance").isEqualTo("test123");
		assertThat(jsonContent).extractingJsonPathNumberValue("$.version").isEqualTo(0);
		assertThat(jsonContent).extractingJsonPathNumberValue("$.timestamp").isEqualTo(1587751031.000000000);
		assertThat(jsonContent).extractingJsonPathStringValue("$.type").isEqualTo("ENDPOINTS_DETECTED");
		assertThat(jsonContent).extractingJsonPathArrayValue("$.endpoints").isEmpty();
	}

}
