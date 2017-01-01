package de.codecentric.boot.admin.registration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import de.codecentric.boot.admin.client.registration.Application;

public class ApplicationTest {

	@Test
	public void test_json_format() throws JsonProcessingException, IOException {
		ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

		Application app = Application.create("test").withHealthUrl("http://health")
				.withServiceUrl("http://service").withManagementUrl("http://management").build();

		DocumentContext json = JsonPath.parse(objectMapper.writeValueAsString(app));

		assertThat((String)json.read("$.name")).isEqualTo("test");
		assertThat((String)json.read("$.serviceUrl")).isEqualTo("http://service");
		assertThat((String)json.read("$.managementUrl")).isEqualTo("http://management");
		assertThat((String)json.read("$.healthUrl")).isEqualTo("http://health");
	}

	@Test
	public void test_equals_hashCode() {
		Application a1 = Application.create("foo").withHealthUrl("healthUrl")
				.withManagementUrl("mgmt").withServiceUrl("svc").build();
		Application a2 = Application.create("foo").withHealthUrl("healthUrl")
				.withManagementUrl("mgmt").withServiceUrl("svc").build();

		assertThat(a1).isEqualTo(a2);
		assertThat(a1.hashCode()).isEqualTo(a2.hashCode());

		Application a3 = Application.create("foo").withHealthUrl("healthUrl2")
				.withManagementUrl("mgmt").withServiceUrl("svc").build();

		assertThat(a1).isNotEqualTo(a3);
		assertThat(a2).isNotEqualTo(a3);
	}
}
