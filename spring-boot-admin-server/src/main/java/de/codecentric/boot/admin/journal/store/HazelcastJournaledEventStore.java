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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.hazelcast.core.IList;

import de.codecentric.boot.admin.event.ClientApplicationEvent;

/**
 * Event-Store backed by a Hazelcast-list.
 *
 * @author Johannes Edmeier
 */
public class HazelcastJournaledEventStore implements JournaledEventStore {

	private IList<ClientApplicationEvent> store;

	public HazelcastJournaledEventStore(IList<ClientApplicationEvent> store) {
		this.store = store;
	}

	@Override
	public Collection<ClientApplicationEvent> findAll() {
		ArrayList<ClientApplicationEvent> list = new ArrayList<>(store);
		Collections.reverse(list);
		return list;
	}

	@Override
	public void store(ClientApplicationEvent event) {
		store.add(event);
	}

}
