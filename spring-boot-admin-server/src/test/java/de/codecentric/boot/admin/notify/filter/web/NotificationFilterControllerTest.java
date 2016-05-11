package de.codecentric.boot.admin.notify.filter.web;

import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import de.codecentric.boot.admin.notify.LoggingNotifier;
import de.codecentric.boot.admin.notify.filter.FilteringNotifier;

public class NotificationFilterControllerTest {

	private MockMvc mvc = MockMvcBuilders
			.standaloneSetup(
					new NotificationFilterController(new FilteringNotifier(new LoggingNotifier())))
			.build();

	@Test
	public void test_missing_parameters() throws Exception {
		mvc.perform(post("/api/notifications/filters")).andExpect(status().isBadRequest());
	}

	@Test
	public void test_delete_notfound() throws Exception {
		mvc.perform(delete("/api/notifications/filters/abcdef")).andExpect(status().isNotFound());
	}

	@Test
	public void test_post_delete() throws Exception {
		String id = mvc.perform(post("/api/notifications/filters?id=1337&ttl=10000"))
				.andExpect(status().isOk()).andExpect(content().string(not(isEmptyString())))
				.andReturn().getResponse().getContentAsString();

		mvc.perform(get("/api/notifications/filters")).andExpect(status().isOk())
		.andExpect(jsonPath("$..id").value("1337"));

		mvc.perform(delete("/api/notifications/filters/{id}", id)).andExpect(status().isOk());

		mvc.perform(get("/api/notifications/filters")).andExpect(status().isOk())
				.andExpect(jsonPath("$").isEmpty());
	}
}
