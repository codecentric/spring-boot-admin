package de.codecentric.boot.admin.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

public class InfoTest {
	private ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

	@Test
	public void test_json_serialize() throws Exception {
		Info info = Info.from(Collections.singletonMap("foo", "bar"));
		String json = objectMapper.writeValueAsString(info);

		DocumentContext doc = JsonPath.parse(json);

		assertThat(doc.read("$.foo", String.class)).isEqualTo("bar");
	}

	@Test
	public void test_retain_order() {
		Map<String, String> map = new LinkedHashMap<>();
		map.put("z", "1");
		map.put("x", "2");

		Iterator<?> iter = Info.from(map).getValues().entrySet().iterator();

		assertThat(iter.next()).hasFieldOrPropertyWithValue("key", "z")
				.hasFieldOrPropertyWithValue("value", "1");
		assertThat(iter.next()).hasFieldOrPropertyWithValue("key", "x")
				.hasFieldOrPropertyWithValue("value", "2");
	}

}
