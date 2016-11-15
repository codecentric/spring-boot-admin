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

		assertThat(value.getName(), is("test"));
		assertThat(value.getManagementUrl(), is("http://test"));
		assertThat(value.getHealthUrl(), is("http://test/health"));
		assertThat(value.getServiceUrl(), nullValue());
	}

	@Test
	public void test_new_json_format() throws JsonProcessingException, IOException {
		String json = "{ \"name\" : \"test\", \"managementUrl\" : \"http://test\" , \"healthUrl\" : \"http://health\" , \"serviceUrl\" : \"http://service\"}";

		Application value = objectMapper.readValue(json, Application.class);

		assertThat(value.getName(), is("test"));
		assertThat(value.getManagementUrl(), is("http://test"));
		assertThat(value.getHealthUrl(), is("http://health"));
		assertThat(value.getServiceUrl(), is("http://service"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_name_expected() throws JsonProcessingException, IOException {
		Application.fromJson("http://url", "", "", "", null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_healthUrl_expected() throws JsonProcessingException, IOException {
		Application.fromJson("", "", "", "name", null);
	}

	@Test
	public void test_equals_hashCode() {
		Application a1 = Application.create("foo").withHealthUrl("healthUrl")
				.withManagementUrl("mgmt").withServiceUrl("svc").withId("id").build();
		Application a2 = Application.create("foo").withHealthUrl("healthUrl")
				.withManagementUrl("mgmt").withServiceUrl("svc").withId("id").build();

		assertThat(a1, is(a2));
		assertThat(a1.hashCode(), is(a2.hashCode()));

		Application a3 = Application.create("foo").withHealthUrl("healthUrl2")
				.withManagementUrl("mgmt").withServiceUrl("svc").withId("other").build();

		assertThat(a1, not(is(a3)));
		assertThat(a2, not(is(a3)));
	}

	@Test
	public void test_builder_copy() {
		Application app = Application.create("App").withId("-id-").withHealthUrl("http://health")
				.withManagementUrl("http://mgmgt").withServiceUrl("http://svc")
				.withStatusInfo(StatusInfo.ofUp()).build();
		Application copy = Application.create(app).build();
		assertThat(app, is(copy));
	}
}