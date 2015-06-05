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
package de.codecentric.boot.admin.journal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Test;

import de.codecentric.boot.admin.event.ClientApplicationDeregisteredEvent;
import de.codecentric.boot.admin.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.journal.JournaledEvent;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.model.StatusInfo;

public class JournaledEventTest {

	@Test
	public void test_from_event_registration() {
		JournaledEvent event = JournaledEvent
				.fromEvent(new ClientApplicationRegisteredEvent(new Object(),
						Application.create("foo").withId("bar").build()));

		assertThat(event.getApplication(),
				is(Application.create("foo").withId("bar").build()));
		assertThat(event.getData(), is(Collections.<String, Object> emptyMap()));
		assertThat(event.getType(), is(JournaledEvent.Type.REGISTRATION));
		assertTrue(event.getTimestamp() > 0);
	}

	@Test
	public void test_from_event_deregistration() {
		JournaledEvent event = JournaledEvent
				.fromEvent(new ClientApplicationDeregisteredEvent(new Object(),
						Application.create("foo").withId("bar").build()));

		assertThat(event.getApplication(),
				is(Application.create("foo").withId("bar").build()));
		assertThat(event.getData(), is(Collections.<String, Object> emptyMap()));
		assertThat(event.getType(), is(JournaledEvent.Type.DEREGISTRATION));
		assertTrue(event.getTimestamp() > 0);
	}

	@Test
	public void test_from_event_status_change() {
		JournaledEvent event = JournaledEvent
				.fromEvent(new ClientApplicationStatusChangedEvent(
						new Object(),
						Application.create("foo").withId("bar").build(),
						StatusInfo.ofUnknown(), StatusInfo.ofUp()));

		assertThat(event.getApplication(),
				is(Application.create("foo").withId("bar").build()));
		assertThat(event.getData().get("from"), is((Object) StatusInfo
				.ofUnknown()
				.getStatus()));
		assertThat(event.getData().get("to"), is((Object) StatusInfo.ofUp()
				.getStatus()));
		assertThat(event.getType(), is(JournaledEvent.Type.STATUS_CHANGE));
		assertTrue(event.getTimestamp() > 0);
	}

}
