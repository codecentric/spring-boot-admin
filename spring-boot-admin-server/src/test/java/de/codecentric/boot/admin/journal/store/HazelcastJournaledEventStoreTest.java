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
package de.codecentric.boot.admin.journal.store;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.HazelcastInstanceFactory;

import de.codecentric.boot.admin.event.ClientApplicationDeregisteredEvent;
import de.codecentric.boot.admin.event.ClientApplicationEvent;
import de.codecentric.boot.admin.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.model.Application;

public class HazelcastJournaledEventStoreTest {

	private HazelcastJournaledEventStore store;

	@Before
	public void setup() {
		HazelcastInstance hazelcast = HazelcastInstanceFactory.newHazelcastInstance(new Config());
		store = new HazelcastJournaledEventStore(
				hazelcast.<ClientApplicationEvent> getList("testList"));
	}

	@Test
	public void test_store() {
		Application application = Application.create("foo").withId("bar")
				.withHealthUrl("http://health").build();
		List<ClientApplicationEvent> events = Arrays.asList(
				new ClientApplicationRegisteredEvent(application),
				new ClientApplicationDeregisteredEvent(application));

		for (ClientApplicationEvent event : events) {
			store.store(event);
		}

		// Items are stored in reverse order
		List<ClientApplicationEvent> reversed = new ArrayList<>(events);
		Collections.reverse(reversed);

		assertThat(store.findAll(), is((Collection<ClientApplicationEvent>) reversed));
	}
}
