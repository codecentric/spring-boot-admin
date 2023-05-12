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

import de.codecentric.boot.admin.server.domain.values.Info;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class InfoMixinTest {

	private final ObjectMapper objectMapper;

	private JacksonTester<Info> jsonTester;

	public InfoMixinTest() {
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
		String json = new JSONObject().put("build", new JSONObject().put("version", "1.0.0"))
			.put("foo", "bar")
			.toString();

		Info info = objectMapper.readValue(json, Info.class);
		assertThat(info).isNotNull();
		assertThat(info.getValues()).containsOnly(entry("build", Collections.singletonMap("version", "1.0.0")),
				entry("foo", "bar"));
	}

	@Test
	public void verifySerialize() throws IOException {
		Map<String, Object> data = new HashMap<>();
		data.put("build", Collections.singletonMap("version", "1.0.0"));
		data.put("foo", "bar");
		Info info = Info.from(data);

		JsonContent<Info> jsonContent = jsonTester.write(info);
		assertThat(jsonContent).extractingJsonPathMapValue("$").containsOnlyKeys("build", "foo");
		assertThat(jsonContent).extractingJsonPathStringValue("$['build'].['version']").isEqualTo("1.0.0");
		assertThat(jsonContent).extractingJsonPathStringValue("$['foo']").isEqualTo("bar");
	}

}
