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

import java.util.Collection;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.codecentric.boot.admin.journal.ApplicationEventJournal;
import de.codecentric.boot.admin.journal.JournaledEvent;

/**
 * REST-Controller for querying all client application events.
 *
 * @author Johannes Stelzer
 *
 */
@RestController
@RequestMapping("/api/journal")
public class JournalController {

	private ApplicationEventJournal eventJournal;

	public JournalController(ApplicationEventJournal eventJournal) {
		this.eventJournal = eventJournal;
	}

	@RequestMapping
	public Collection<JournaledEvent> getJournal() {
		return eventJournal.getEvents();
	}

}
