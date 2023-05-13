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
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import de.codecentric.boot.admin.server.domain.events.InstanceInfoChangedEvent;
import de.codecentric.boot.admin.server.domain.values.Info;
import de.codecentric.boot.admin.server.domain.values.InstanceId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class InstanceInfoChangedEventMixinTest {

	private final ObjectMapper objectMapper;

	private JacksonTester<InstanceInfoChangedEvent> jsonTester;

	public InstanceInfoChangedEventMixinTest() {
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
			.put("type", "INFO_CHANGED")
			.put("info", new JSONObject().put("build", new JSONObject().put("version", "1.0.0")).put("foo", "bar"))
			.toString();

		InstanceInfoChangedEvent event = objectMapper.readValue(json, InstanceInfoChangedEvent.class);
		assertThat(event).isNotNull();
		assertThat(event.getInstance()).isEqualTo(InstanceId.of("test123"));
		assertThat(event.getVersion()).isEqualTo(12345678L);
		assertThat(event.getTimestamp()).isEqualTo(Instant.ofEpochSecond(1587751031).truncatedTo(ChronoUnit.SECONDS));

		Info info = event.getInfo();
		assertThat(info).isNotNull();
		assertThat(info.getValues()).containsOnly(entry("build", Collections.singletonMap("version", "1.0.0")),
				entry("foo", "bar"));
	}

	@Test
	public void verifyDeserializeWithOnlyRequiredProperties() throws JSONException, JsonProcessingException {
		String json = new JSONObject().put("instance", "test123")
			.put("timestamp", 1587751031.000000000)
			.put("type", "INFO_CHANGED")
			.toString();

		InstanceInfoChangedEvent event = objectMapper.readValue(json, InstanceInfoChangedEvent.class);
		assertThat(event).isNotNull();
		assertThat(event.getInstance()).isEqualTo(InstanceId.of("test123"));
		assertThat(event.getVersion()).isEqualTo(0L);
		assertThat(event.getTimestamp()).isEqualTo(Instant.ofEpochSecond(1587751031).truncatedTo(ChronoUnit.SECONDS));
		assertThat(event.getInfo()).isNull();
	}

	@Test
	public void verifyDeserializeWithEmptyInfo() throws JSONException, JsonProcessingException {
		String json = new JSONObject().put("instance", "test123")
			.put("version", 12345678L)
			.put("timestamp", 1587751031.000000000)
			.put("type", "INFO_CHANGED")
			.put("info", new JSONObject())
			.toString();

		InstanceInfoChangedEvent event = objectMapper.readValue(json, InstanceInfoChangedEvent.class);
		assertThat(event).isNotNull();
		assertThat(event.getInstance()).isEqualTo(InstanceId.of("test123"));
		assertThat(event.getVersion()).isEqualTo(12345678L);
		assertThat(event.getTimestamp()).isEqualTo(Instant.ofEpochSecond(1587751031).truncatedTo(ChronoUnit.SECONDS));

		Info info = event.getInfo();
		assertThat(info).isNotNull();
		assertThat(info.getValues()).isEmpty();
	}

	@Test
	public void verifySerialize() throws IOException {
		InstanceId id = InstanceId.of("test123");
		Instant timestamp = Instant.ofEpochSecond(1587751031).truncatedTo(ChronoUnit.SECONDS);
		Map<String, Object> data = new HashMap<>();
		data.put("build", Collections.singletonMap("version", "1.0.0"));
		data.put("foo", "bar");
		InstanceInfoChangedEvent event = new InstanceInfoChangedEvent(id, 12345678L, timestamp, Info.from(data));

		JsonContent<InstanceInfoChangedEvent> jsonContent = jsonTester.write(event);
		assertThat(jsonContent).extractingJsonPathStringValue("$.instance").isEqualTo("test123");
		assertThat(jsonContent).extractingJsonPathNumberValue("$.version").isEqualTo(12345678);
		assertThat(jsonContent).extractingJsonPathNumberValue("$.timestamp").isEqualTo(1587751031.000000000);
		assertThat(jsonContent).extractingJsonPathStringValue("$.type").isEqualTo("INFO_CHANGED");
		assertThat(jsonContent).extractingJsonPathMapValue("$.info").containsOnlyKeys("build", "foo");

		assertThat(jsonContent).extractingJsonPathStringValue("$.info['build'].['version']").isEqualTo("1.0.0");
		assertThat(jsonContent).extractingJsonPathStringValue("$.info['foo']").isEqualTo("bar");
	}

	@Test
	public void verifySerializeWithOnlyRequiredProperties() throws IOException {
		InstanceId id = InstanceId.of("test123");
		Instant timestamp = Instant.ofEpochSecond(1587751031).truncatedTo(ChronoUnit.SECONDS);
		InstanceInfoChangedEvent event = new InstanceInfoChangedEvent(id, 0L, timestamp, null);

		JsonContent<InstanceInfoChangedEvent> jsonContent = jsonTester.write(event);
		assertThat(jsonContent).extractingJsonPathStringValue("$.instance").isEqualTo("test123");
		assertThat(jsonContent).extractingJsonPathNumberValue("$.version").isEqualTo(0);
		assertThat(jsonContent).extractingJsonPathNumberValue("$.timestamp").isEqualTo(1587751031.000000000);
		assertThat(jsonContent).extractingJsonPathStringValue("$.type").isEqualTo("INFO_CHANGED");
		assertThat(jsonContent).extractingJsonPathMapValue("$.info").isNull();
	}

	@Test
	public void verifySerializeWithEmptyInfo() throws IOException {
		InstanceId id = InstanceId.of("test123");
		Instant timestamp = Instant.ofEpochSecond(1587751031).truncatedTo(ChronoUnit.SECONDS);
		InstanceInfoChangedEvent event = new InstanceInfoChangedEvent(id, 12345678L, timestamp,
				Info.from(Collections.emptyMap()));

		JsonContent<InstanceInfoChangedEvent> jsonContent = jsonTester.write(event);
		assertThat(jsonContent).extractingJsonPathStringValue("$.instance").isEqualTo("test123");
		assertThat(jsonContent).extractingJsonPathNumberValue("$.version").isEqualTo(12345678);
		assertThat(jsonContent).extractingJsonPathNumberValue("$.timestamp").isEqualTo(1587751031.000000000);
		assertThat(jsonContent).extractingJsonPathStringValue("$.type").isEqualTo("INFO_CHANGED");
		assertThat(jsonContent).extractingJsonPathMapValue("$.info").isEmpty();
	}

}
