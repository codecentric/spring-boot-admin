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
package de.codecentric.boot.admin.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.junit.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import de.codecentric.boot.admin.event.ClientApplicationEvent;
import de.codecentric.boot.admin.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.journal.ApplicationEventJournal;
import de.codecentric.boot.admin.journal.store.SimpleJournaledEventStore;
import de.codecentric.boot.admin.model.Application;

public class JournalControllerTest {

	private ApplicationEventJournal journal = new ApplicationEventJournal(
			new SimpleJournaledEventStore());
	private JournalController controller = new JournalController(journal);

	@Test
	public void test_getJournal() {
		ClientApplicationEvent emittedEvent = new ClientApplicationRegisteredEvent(
				Application.create("foo").withId("bar").withHealthUrl("http://health").build());
		journal.onClientApplicationEvent(emittedEvent);

		Collection<ClientApplicationEvent> history = controller.getJournal();

		assertThat(history.size(), is(1));

		ClientApplicationEvent event = history.iterator().next();
		assertThat(event, sameInstance(emittedEvent));
	}

	@Test
	public void test_getJournal_sse() {
		ClientApplicationEvent emittedEvent = new ClientApplicationRegisteredEvent(
				Application.create("foo").withId("bar").withHealthUrl("http://health").build());

		SseEmitter emitter = controller.getJournalEvents();
		journal.onClientApplicationEvent(emittedEvent);

		assertThat(emitter, notNullValue());
	}
}
