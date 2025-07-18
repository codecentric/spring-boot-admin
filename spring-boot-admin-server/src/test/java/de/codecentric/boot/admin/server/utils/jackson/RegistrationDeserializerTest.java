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
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import de.codecentric.boot.admin.server.domain.values.Registration;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegistrationDeserializerTest {

	private final ObjectMapper objectMapper;

	protected RegistrationDeserializerTest() {
		AdminServerModule module = new AdminServerModule(new String[] { ".*password$" });
		objectMapper = Jackson2ObjectMapperBuilder.json().modules(module).build();
	}

	@Test
	void test_1_2_json_format() throws Exception {
		String json = new JSONObject().put("name", "test").put("url", "https://test").toString();
		Registration value = objectMapper.readValue(json, Registration.class);
		assertThat(value.getName()).isEqualTo("test");
		assertThat(value.getManagementUrl()).isEqualTo("https://test");
		assertThat(value.getHealthUrl()).isEqualTo("https://test/health");
		assertThat(value.getServiceUrl()).isNull();
	}

	@Test
	void test_1_4_json_format() throws Exception {
		String json = new JSONObject().put("name", "test")
			.put("managementUrl", "https://test")
			.put("healthUrl", "https://health")
			.put("serviceUrl", "https://service")
			.put("statusInfo", new JSONObject().put("status", "UNKNOWN"))
			.toString();
		Registration value = objectMapper.readValue(json, Registration.class);
		assertThat(value.getName()).isEqualTo("test");
		assertThat(value.getManagementUrl()).isEqualTo("https://test");
		assertThat(value.getHealthUrl()).isEqualTo("https://health");
		assertThat(value.getServiceUrl()).isEqualTo("https://service");
	}

	@Test
	void test_1_5_json_format() throws Exception {
		String json = new JSONObject().put("name", "test")
			.put("managementUrl", "https://test")
			.put("healthUrl", "https://health")
			.put("serviceUrl", "https://service")
			.put("metadata", new JSONObject().put("labels", "foo,bar"))
			.toString();
		Registration value = objectMapper.readValue(json, Registration.class);
		assertThat(value.getName()).isEqualTo("test");
		assertThat(value.getManagementUrl()).isEqualTo("https://test");
		assertThat(value.getHealthUrl()).isEqualTo("https://health");
		assertThat(value.getServiceUrl()).isEqualTo("https://service");
		assertThat(value.getMetadata()).isEqualTo(singletonMap("labels", "foo,bar"));
	}

	@Test
	void test_onlyHealthUrl() throws Exception {
		String json = new JSONObject().put("name", "test").put("healthUrl", "https://test").toString();
		Registration value = objectMapper.readValue(json, Registration.class);
		assertThat(value.getName()).isEqualTo("test");
		assertThat(value.getHealthUrl()).isEqualTo("https://test");
		assertThat(value.getManagementUrl()).isNull();
		assertThat(value.getServiceUrl()).isNull();
	}

	@Test
	void test_name_expected() throws Exception {
		String json = new JSONObject().put("name", "")
			.put("managementUrl", "https://test")
			.put("healthUrl", "https://health")
			.put("serviceUrl", "https://service")
			.toString();

		assertThatThrownBy(() -> objectMapper.readValue(json, Registration.class))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void test_healthUrl_expected() throws Exception {
		String json = new JSONObject().put("name", "test")
			.put("managementUrl", "https://test")
			.put("healthUrl", "")
			.put("serviceUrl", "https://service")
			.toString();
		assertThatThrownBy(() -> objectMapper.readValue(json, Registration.class))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void test_sanitize_metadata() throws JsonProcessingException {
		Registration app = Registration.create("test", "https://health")
			.metadata("PASSWORD", "qwertz123")
			.metadata("user", "humptydumpty")
			.build();
		String json = objectMapper.writeValueAsString(app);

		assertThat(json).doesNotContain("qwertz123").contains("humptydumpty");
	}

	@Test
	void test_snake_case() throws Exception {
		String json = new JSONObject().put("name", "test")
			.put("management_url", "https://test")
			.put("health_url", "https://health")
			.put("service_url", "https://service")
			.put("metadata", new JSONObject().put("labels", "foo,bar"))
			.toString();
		Registration value = objectMapper.readValue(json, Registration.class);
		assertThat(value.getName()).isEqualTo("test");
		assertThat(value.getManagementUrl()).isEqualTo("https://test");
		assertThat(value.getHealthUrl()).isEqualTo("https://health");
		assertThat(value.getServiceUrl()).isEqualTo("https://service");
		assertThat(value.getMetadata()).isEqualTo(singletonMap("labels", "foo,bar"));

	}

}
