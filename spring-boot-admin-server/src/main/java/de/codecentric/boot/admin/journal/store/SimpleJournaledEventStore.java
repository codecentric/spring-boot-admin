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
import java.util.LinkedList;
import java.util.List;

import de.codecentric.boot.admin.event.ClientApplicationEvent;

/**
 * Simple, non-persistent Store for JournaledEvent
 *
 * @author Johannes Stelzer
 */
public class SimpleJournaledEventStore implements JournaledEventStore {
	private final List<ClientApplicationEvent> store = new LinkedList<ClientApplicationEvent>();

	private int capacity = 1_000;

	@Override
	public Collection<ClientApplicationEvent> findAll() {
		synchronized (this.store) {
			return new ArrayList<>(store);
		}
	}

	@Override
	public void store(ClientApplicationEvent event) {
		synchronized (this.store) {
			while (store.size() >= capacity) {
				store.remove(capacity - 1);
			}
			store.add(0, event);
		}
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
}
