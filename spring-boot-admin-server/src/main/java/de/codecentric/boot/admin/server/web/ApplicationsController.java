/*
 * Copyright 2014-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.server.web;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.entities.Application;
import de.codecentric.boot.admin.server.services.ApplicationRegistry;
import de.codecentric.boot.admin.server.web.client.RefreshInstancesEvent;

/**
 * REST controller for controlling registration of managed instances.
 */
@AdminController
@ResponseBody
public class ApplicationsController {

	private static final Logger log = LoggerFactory.getLogger(ApplicationsController.class);

	private static final ServerSentEvent<?> PING = ServerSentEvent.builder().comment("ping").build();

	private static final Flux<ServerSentEvent<?>> PING_FLUX = Flux.interval(Duration.ZERO, Duration.ofSeconds(10L))
		.map((tick) -> PING);

	private final ApplicationRegistry registry;

	private final ApplicationEventPublisher publisher;

	public ApplicationsController(ApplicationRegistry registry, ApplicationEventPublisher publisher) {
		this.registry = registry;
		this.publisher = publisher;
	}

	@GetMapping(path = "/applications", produces = MediaType.APPLICATION_JSON_VALUE)
	public Flux<Application> applications() {
		return registry.getApplications();
	}

	@PostMapping(path = "/applications", produces = MediaType.APPLICATION_JSON_VALUE)
	public void refreshApplications() {
		publisher.publishEvent(new RefreshInstancesEvent(this));
	}

	@GetMapping(path = "/applications/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<Application>> application(@PathVariable("name") String name) {
		return registry.getApplication(name).map(ResponseEntity::ok).defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@GetMapping(path = "/applications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<ServerSentEvent<Application>> applicationsStream() {
		return registry.getApplicationStream()
			.map((application) -> ServerSentEvent.builder(application).build())
			.mergeWith(ping());
	}

	@DeleteMapping(path = "/applications/{name}")
	public Mono<ResponseEntity<Void>> unregister(@PathVariable("name") String name) {
		log.debug("Unregister application with name '{}'", name);
		return registry.deregister(name)
			.collectList()
			.map((deregistered) -> !deregistered.isEmpty() ? ResponseEntity.noContent().build()
					: ResponseEntity.notFound().build());
	}

	@SuppressWarnings("unchecked")
	private static <T> Flux<ServerSentEvent<T>> ping() {
		return (Flux<ServerSentEvent<T>>) (Flux) PING_FLUX;
	}

}
