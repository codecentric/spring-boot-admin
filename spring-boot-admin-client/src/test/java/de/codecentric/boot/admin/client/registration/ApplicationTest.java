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

package de.codecentric.boot.admin.client.registration;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationTest {

	@Test
	public void test_json_format() throws IOException {
		ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

		Application app = Application.create("test")
			.healthUrl("http://health")
			.serviceUrl("http://service")
			.managementUrl("http://management")
			.build();

		DocumentContext json = JsonPath.parse(objectMapper.writeValueAsString(app));

		assertThat((String) json.read("$.name")).isEqualTo("test");
		assertThat((String) json.read("$.serviceUrl")).isEqualTo("http://service");
		assertThat((String) json.read("$.managementUrl")).isEqualTo("http://management");
		assertThat((String) json.read("$.healthUrl")).isEqualTo("http://health");
	}

	@Test
	public void test_equals_hashCode() {
		Application a1 = Application.create("foo")
			.healthUrl("healthUrl")
			.managementUrl("mgmt")
			.serviceUrl("svc")
			.build();
		Application a2 = Application.create("foo")
			.healthUrl("healthUrl")
			.managementUrl("mgmt")
			.serviceUrl("svc")
			.build();

		assertThat(a1).isEqualTo(a2);
		assertThat(a1.hashCode()).isEqualTo(a2.hashCode());

		Application a3 = Application.create("foo")
			.healthUrl("healthUrl2")
			.managementUrl("mgmt")
			.serviceUrl("svc")
			.build();

		assertThat(a1).isNotEqualTo(a3);
		assertThat(a2).isNotEqualTo(a3);
	}

	@Test
	public void should_not_return_sensitive_data_in_toString() {
		Application application = Application.create("app").healthUrl("HEALTH").metadata("password", "geheim").build();
		assertThat(application.toString()).doesNotContain("geheim");
	}

}
