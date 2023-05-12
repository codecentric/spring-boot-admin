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
import java.util.Collections;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

public class InstanceStatusChangedEventMixinTest {

	private final ObjectMapper objectMapper;

	private JacksonTester<InstanceStatusChangedEvent> jsonTester;

	public InstanceStatusChangedEventMixinTest() {
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
			.put("type", "STATUS_CHANGED")
			.put("statusInfo",
					new JSONObject().put("status", "OFFLINE").put("details", new JSONObject().put("foo", "bar")))
			.toString();

		InstanceStatusChangedEvent event = objectMapper.readValue(json, InstanceStatusChangedEvent.class);
		assertThat(event).isNotNull();
		assertThat(event.getInstance()).isEqualTo(InstanceId.of("test123"));
		assertThat(event.getVersion()).isEqualTo(12345678L);
		assertThat(event.getTimestamp()).isEqualTo(Instant.ofEpochSecond(1587751031).truncatedTo(ChronoUnit.SECONDS));

		StatusInfo statusInfo = event.getStatusInfo();
		assertThat(statusInfo).isNotNull();
		assertThat(statusInfo.getStatus()).isEqualTo("OFFLINE");
		assertThat(statusInfo.getDetails()).containsOnly(entry("foo", "bar"));
	}

	@Test
	public void verifyDeserializeWithOnlyRequiredProperties() throws JSONException, JsonProcessingException {
		String json = new JSONObject().put("instance", "test123")
			.put("timestamp", 1587751031.000000000)
			.put("type", "STATUS_CHANGED")
			.put("statusInfo", new JSONObject().put("status", "OFFLINE"))
			.toString();

		InstanceStatusChangedEvent event = objectMapper.readValue(json, InstanceStatusChangedEvent.class);
		assertThat(event).isNotNull();
		assertThat(event.getInstance()).isEqualTo(InstanceId.of("test123"));
		assertThat(event.getVersion()).isEqualTo(0L);
		assertThat(event.getTimestamp()).isEqualTo(Instant.ofEpochSecond(1587751031).truncatedTo(ChronoUnit.SECONDS));

		StatusInfo statusInfo = event.getStatusInfo();
		assertThat(statusInfo).isNotNull();
		assertThat(statusInfo.getStatus()).isEqualTo("OFFLINE");
		assertThat(statusInfo.getDetails()).isEmpty();
	}

	@Test
	public void verifyDeserializeWithoutStatusInfo() throws JSONException, JsonProcessingException {
		String json = new JSONObject().put("instance", "test123")
			.put("version", 12345678L)
			.put("timestamp", 1587751031.000000000)
			.put("type", "STATUS_CHANGED")
			.toString();

		InstanceStatusChangedEvent event = objectMapper.readValue(json, InstanceStatusChangedEvent.class);
		assertThat(event).isNotNull();
		assertThat(event.getInstance()).isEqualTo(InstanceId.of("test123"));
		assertThat(event.getVersion()).isEqualTo(12345678L);
		assertThat(event.getTimestamp()).isEqualTo(Instant.ofEpochSecond(1587751031).truncatedTo(ChronoUnit.SECONDS));
		assertThat(event.getStatusInfo()).isNull();
	}

	@Test
	public void verifyDeserializeWithEmptyStatusInfo() throws JSONException, JsonProcessingException {
		String json = new JSONObject().put("instance", "test123")
			.put("version", 12345678L)
			.put("timestamp", 1587751031.000000000)
			.put("type", "STATUS_CHANGED")
			.put("statusInfo", new JSONObject())
			.toString();

		assertThatThrownBy(() -> objectMapper.readValue(json, InstanceStatusChangedEvent.class))
			.isInstanceOf(JsonMappingException.class)
			.hasCauseInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("must not be empty");
	}

	@Test
	public void verifySerialize() throws IOException {
		InstanceId id = InstanceId.of("test123");
		Instant timestamp = Instant.ofEpochSecond(1587751031).truncatedTo(ChronoUnit.SECONDS);
		StatusInfo statusInfo = StatusInfo.valueOf("OFFLINE", Collections.singletonMap("foo", "bar"));

		InstanceStatusChangedEvent event = new InstanceStatusChangedEvent(id, 12345678L, timestamp, statusInfo);

		JsonContent<InstanceStatusChangedEvent> jsonContent = jsonTester.write(event);
		assertThat(jsonContent).extractingJsonPathStringValue("$.instance").isEqualTo("test123");
		assertThat(jsonContent).extractingJsonPathNumberValue("$.version").isEqualTo(12345678);
		assertThat(jsonContent).extractingJsonPathNumberValue("$.timestamp").isEqualTo(1587751031.000000000);
		assertThat(jsonContent).extractingJsonPathStringValue("$.type").isEqualTo("STATUS_CHANGED");
		assertThat(jsonContent).extractingJsonPathValue("$.statusInfo").isNotNull();

		assertThat(jsonContent).extractingJsonPathStringValue("$.statusInfo.status").isEqualTo("OFFLINE");
		assertThat(jsonContent).extractingJsonPathMapValue("$.statusInfo.details").containsOnly(entry("foo", "bar"));
	}

	@Test
	public void verifySerializeWithOnlyRequiredProperties() throws IOException {
		InstanceId id = InstanceId.of("test123");
		Instant timestamp = Instant.ofEpochSecond(1587751031).truncatedTo(ChronoUnit.SECONDS);
		StatusInfo statusInfo = StatusInfo.valueOf("OFFLINE");

		InstanceStatusChangedEvent event = new InstanceStatusChangedEvent(id, 0L, timestamp, statusInfo);

		JsonContent<InstanceStatusChangedEvent> jsonContent = jsonTester.write(event);
		assertThat(jsonContent).extractingJsonPathStringValue("$.instance").isEqualTo("test123");
		assertThat(jsonContent).extractingJsonPathNumberValue("$.version").isEqualTo(0);
		assertThat(jsonContent).extractingJsonPathNumberValue("$.timestamp").isEqualTo(1587751031.000000000);
		assertThat(jsonContent).extractingJsonPathStringValue("$.type").isEqualTo("STATUS_CHANGED");
		assertThat(jsonContent).extractingJsonPathValue("$.statusInfo").isNotNull();

		assertThat(jsonContent).extractingJsonPathStringValue("$.statusInfo.status").isEqualTo("OFFLINE");
		assertThat(jsonContent).extractingJsonPathMapValue("$.statusInfo.details").isEmpty();
	}

	@Test
	public void verifySerializeWithoutStatusInfo() throws IOException {
		InstanceId id = InstanceId.of("test123");
		Instant timestamp = Instant.ofEpochSecond(1587751031).truncatedTo(ChronoUnit.SECONDS);

		InstanceStatusChangedEvent event = new InstanceStatusChangedEvent(id, 12345678L, timestamp, null);

		JsonContent<InstanceStatusChangedEvent> jsonContent = jsonTester.write(event);
		assertThat(jsonContent).extractingJsonPathStringValue("$.instance").isEqualTo("test123");
		assertThat(jsonContent).extractingJsonPathNumberValue("$.version").isEqualTo(12345678);
		assertThat(jsonContent).extractingJsonPathNumberValue("$.timestamp").isEqualTo(1587751031.000000000);
		assertThat(jsonContent).extractingJsonPathStringValue("$.type").isEqualTo("STATUS_CHANGED");
		assertThat(jsonContent).extractingJsonPathValue("$.statusInfo").isNull();
	}

}
