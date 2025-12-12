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

package de.codecentric.boot.admin.server.notify;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.notify.filter.AbstractContentNotifier;

// The following class, `WebexNotifier`, is responsible for sending notifications through the Webex API
// whenever events related to the state of instances within the Spring Boot Admin server occur.

/**
 * `WebexNotifier` sends notifications via Webex API when instance events occur. It is
 * part of the spring-boot-admin-server which is used for monitoring and managing Spring
 * Boot applications.
 */
public class WebexNotifier extends AbstractContentNotifier {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebexNotifier.class);

	private static final URI DEFAULT_URL = URI.create("https://webexapis.com/v1/messages");

	private static final String DEFAULT_MESSAGE = "<strong>#{name}</strong>/#{id} is <strong>#{status}</strong>";

	private RestTemplate restTemplate;

	/**
	 * base url for Webex API (i.e. https://webexapis.com/v1/messages)
	 */
	private URI url = DEFAULT_URL;

	/**
	 * Bearer authentication token for Webex API
	 */
	@Nullable
	private String authToken;

	/**
	 * Room identifier in Webex where the message will be sent
	 */
	@Nullable
	private String roomId;

	/**
	 * Creates a new WebexNotifier with the given repository and restTemplate.
	 * @param repository the instance repository responsible for storing instances
	 * @param restTemplate the restTemplate used to make HTTP requests
	 */
	public WebexNotifier(InstanceRepository repository, RestTemplate restTemplate) {
		super(repository);
		this.restTemplate = restTemplate;
	}

	/**
	 * Sends a notification with the given event and instance.
	 * @param event the instance event to notify
	 * @param instance the instance associated with the event
	 * @return a Mono representing the completion of the notification
	 * @throws IllegalStateException if 'authToken' is null
	 */
	@Override
	protected Mono<Void> doNotify(InstanceEvent event, Instance instance) {

		if (authToken == null) {
			return Mono.error(new IllegalStateException("'authToken' must not be null."));
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(authToken);

		LOGGER.debug("Event: {}", event.getInstance());

		return Mono.fromRunnable(() -> restTemplate.postForEntity(url,
				new HttpEntity<>(createMessage(event, instance), headers), Void.class));
	}

	/**
	 * Creates a message object containing the parameters required for sending a
	 * notification.
	 * @param event the instance event for which the message is being created
	 * @param instance the instance associated with the event
	 * @return a Map object containing the parameters for sending a notification
	 */
	protected Object createMessage(InstanceEvent event, Instance instance) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("roomId", this.roomId);
		parameters.put("markdown", createContent(event, instance));
		return parameters;
	}

	@Override
	protected String getDefaultMessage() {
		return DEFAULT_MESSAGE;
	}

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public URI getUrl() {
		return url;
	}

	public void setUrl(URI url) {
		this.url = url;
	}

	@Nullable
	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(@Nullable String authToken) {
		this.authToken = authToken;
	}

	@Nullable
	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(@Nullable String roomId) {
		this.roomId = roomId;
	}

}
