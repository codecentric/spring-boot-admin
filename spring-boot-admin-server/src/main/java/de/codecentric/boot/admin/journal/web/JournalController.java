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

import static java.util.Collections.synchronizedCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import de.codecentric.boot.admin.event.ClientApplicationEvent;
import de.codecentric.boot.admin.journal.ApplicationEventJournal;
import de.codecentric.boot.admin.web.AdminController;

/**
 * REST-Controller for querying all client application events.
 *
 * @author Johannes Edmeier
 */
@AdminController
@ResponseBody
@RequestMapping("/api/journal")
public class JournalController {
	private static final Logger LOGGER = LoggerFactory.getLogger(JournalController.class);

	private ApplicationEventJournal eventJournal;
	private final Collection<SseEmitter> emitters = synchronizedCollection(
			new LinkedList<SseEmitter>());

	public JournalController(ApplicationEventJournal eventJournal) {
		this.eventJournal = eventJournal;
	}

	@RequestMapping(produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
	public Collection<ClientApplicationEvent> getJournal() {
		return eventJournal.getEvents();
	}

	@RequestMapping(produces = "text/event-stream")
	public SseEmitter getJournalEvents() {
		final SseEmitter emitter = new SseEmitter();
		emitter.onCompletion(new Runnable() {
			@Override
			public void run() {
				emitters.remove(emitter);
			}
		});
		emitters.add(emitter);
		return emitter;
	}

	@EventListener
	public void onClientApplicationEvent(ClientApplicationEvent event) {
		for (SseEmitter emitter : new ArrayList<>(emitters)) {
			try {
				emitter.send(event, MediaType.APPLICATION_JSON);
			} catch (Exception ex) {
				LOGGER.debug("Error sending event to client ", ex);
			}
		}
	}

	@ExceptionHandler(AsyncRequestTimeoutException.class)
	public void asyncRequestTimeoutExceptionHandler(HttpServletRequest req) {
		LOGGER.debug("Async request to '{}' timed out", req.getRequestURI());
	}
}
