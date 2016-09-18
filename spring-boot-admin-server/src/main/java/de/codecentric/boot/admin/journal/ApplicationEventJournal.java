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

import java.util.Collection;

import org.springframework.context.event.EventListener;

import de.codecentric.boot.admin.event.ClientApplicationEvent;
import de.codecentric.boot.admin.journal.store.JournaledEventStore;

/**
 * Listens for all ClientApplicationEvents and stores them as JournaledEvents in a store.
 *
 * @author Johannes Stelzer
 */
public class ApplicationEventJournal {

	private final JournaledEventStore store;

	public ApplicationEventJournal(JournaledEventStore store) {
		this.store = store;
	}

	@EventListener
	public void onClientApplicationEvent(ClientApplicationEvent event) {
		store.store(event);
	}

	public Collection<ClientApplicationEvent> getEvents() {
		return store.findAll();
	}
}
