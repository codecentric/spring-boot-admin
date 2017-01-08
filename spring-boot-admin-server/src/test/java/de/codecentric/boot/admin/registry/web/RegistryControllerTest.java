/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.codecentric.boot.admin.registry.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.jayway.jsonpath.JsonPath;

import de.codecentric.boot.admin.registry.ApplicationRegistry;
import de.codecentric.boot.admin.registry.HashingApplicationUrlIdGenerator;
import de.codecentric.boot.admin.registry.store.SimpleApplicationStore;

public class RegistryControllerTest {

	private static final String APPLICATION_TEST_JSON;
	private static final String APPLICATION_TWICE_JSON;

	static {
		try {
			APPLICATION_TEST_JSON = new JSONObject().put("name", "test")
					.put("healthUrl", "http://localhost/mgmt/health").toString();
			APPLICATION_TWICE_JSON = new JSONObject().put("name", "twice")
					.put("healthUrl", "http://localhost/mgmt/health").toString();
		} catch (JSONException ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}

	private MockMvc mvc;

	@Before
	public void setup() {
		ApplicationRegistry registry = new ApplicationRegistry(new SimpleApplicationStore(),
				new HashingApplicationUrlIdGenerator());
		registry.setApplicationEventPublisher(Mockito.mock(ApplicationEventPublisher.class));
		mvc = MockMvcBuilders.standaloneSetup(new RegistryController(registry)).build();
	}

	@Test
	public void test_register_twice_get_and_remove() throws Exception {
		MvcResult result = mvc
				.perform(post("/api/applications").contentType(MediaType.APPLICATION_JSON)
						.content(APPLICATION_TEST_JSON))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.name").value("test"))
				.andExpect(jsonPath("$.healthUrl").value("http://localhost/mgmt/health"))
				.andExpect(jsonPath("$.id").isNotEmpty()).andReturn();

		String id = extractId(result);

		mvc.perform(post("/api/applications").contentType(MediaType.APPLICATION_JSON)
				.content(APPLICATION_TWICE_JSON)).andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("twice"))
				.andExpect(jsonPath("$.healthUrl").value("http://localhost/mgmt/health"))
				.andExpect(jsonPath("$.id").value(id));

		mvc.perform(get("/api/applications")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(id));

		mvc.perform(get("/api/applications?name=twice")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(id));

		mvc.perform(get("/api/applications/{id}", id)).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id));

		mvc.perform(delete("/api/applications/{id}", id)).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id));

		mvc.perform(get("/api/applications/{id}", id)).andExpect(status().isNotFound());
	}

	private String extractId(MvcResult result) throws UnsupportedEncodingException {
		return JsonPath.compile("$.id").read(result.getResponse().getContentAsString());
	}

	@Test
	public void test_get_notFound() throws Exception {
		mvc.perform(get("/api/applications/unknown")).andExpect(status().isNotFound());
		mvc.perform(get("/api/applications?name=unknown")).andExpect(status().isOk())
				.andExpect(jsonPath("$").isEmpty());
	}

	@Test
	public void test_delete_notFound() throws Exception {
		mvc.perform(delete("/api/applications/unknown")).andExpect(status().isNotFound());
	}

}
