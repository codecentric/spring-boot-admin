package de.codecentric.boot.admin.model;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ApplicationTest {

	private ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

	@Test
	public void test_old_json_format() throws JsonProcessingException, IOException {
		String json = "{ \"name\" : \"test\", \"url\" : \"http://test\" }";

		Application value = objectMapper.readValue(json, Application.class);

		assertThat(value.getId(), nullValue());
		assertThat(value.getName(), is("test"));
		assertThat(value.getManagementUrl(), is("http://test"));
		assertThat(value.getHealthUrl(), is("http://test/health"));
		assertThat(value.getServiceUrl(), nullValue());
	}

	@Test
	public void test_new_json_format() throws JsonProcessingException, IOException {
		String json = "{ \"name\" : \"test\", \"managementUrl\" : \"http://test\" , \"healthUrl\" : \"http://health\" , \"serviceUrl\" : \"http://service\"}";

		Application value = objectMapper.readValue(json, Application.class);

		assertThat(value.getId(), nullValue());
		assertThat(value.getName(), is("test"));
		assertThat(value.getManagementUrl(), is("http://test"));
		assertThat(value.getHealthUrl(), is("http://health"));
		assertThat(value.getServiceUrl(), is("http://service"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_name_expected() throws JsonProcessingException, IOException {
		Application.create("http://url", "", "", "", "",
				null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_healthUrl_expected() throws JsonProcessingException, IOException {
		Application.create("", "", "", "", "name", null);
	}

	@Test
	public void test_equals_hashCode() {
		Application a1 = new Application("healthUrl", "managementUrl", "serviceUrl",
				"name", "id");
		Application a2 = new Application("healthUrl", "managementUrl", "serviceUrl",
				"name", "id");

		assertThat(a1, is(a2));
		assertThat(a1.hashCode(), is(a2.hashCode()));

		Application a3 = new Application("healthUrl2", "managementUrl", "serviceUrl",
				"name", "id");
		assertThat(a1, not(is(a3)));
		assertThat(a2, not(is(a3)));
	}

}
