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
package de.codecentric.boot.admin.journal.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import de.codecentric.boot.admin.event.ClientApplicationEvent;
import de.codecentric.boot.admin.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.journal.ApplicationEventJournal;
import de.codecentric.boot.admin.journal.store.SimpleJournaledEventStore;
import de.codecentric.boot.admin.model.Application;

public class JournalControllerTest {

	private ApplicationEventJournal journal = new ApplicationEventJournal(
			new SimpleJournaledEventStore());
	private MockMvc mvc = MockMvcBuilders.standaloneSetup(new JournalController(journal)).build();

	@Test
	public void test_getJournal() throws Exception {
		ClientApplicationEvent emittedEvent = new ClientApplicationRegisteredEvent(
				Application.create("foo").withId("bar").withHealthUrl("http://health").build());
		journal.onClientApplicationEvent(emittedEvent);

		mvc.perform(get("/api/journal").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$[0].type").value("REGISTRATION"));

		mvc.perform(get("/api/journal").accept(MediaType.parseMediaType("text/event-stream")))
				.andExpect(status().isOk());
	}
}
