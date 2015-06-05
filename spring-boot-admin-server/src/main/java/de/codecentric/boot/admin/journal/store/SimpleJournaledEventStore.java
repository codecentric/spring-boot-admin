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
import java.util.List;

import de.codecentric.boot.admin.journal.JournaledEvent;

/**
 * Simple, non-persistent Store for JournaledEvent
 *
 * @author Johannes Stelzer
 *
 */
public class SimpleJournaledEventStore implements JournaledEventStore {

	private final List<JournaledEvent> store = Collections
			.synchronizedList(new ArrayList<JournaledEvent>(1000));

	@Override
	public Collection<JournaledEvent> findAll() {
		ArrayList<JournaledEvent> list = new ArrayList<JournaledEvent>(
				store);
		Collections.reverse(list);
		return list;
	}

	@Override
	public void store(JournaledEvent event) {
		store.add(event);
	}

}
