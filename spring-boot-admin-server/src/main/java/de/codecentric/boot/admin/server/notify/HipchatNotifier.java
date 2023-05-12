/*
 * Copyright 2014-2023 the original author or authors.
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

import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.DataBindingPropertyAccessor;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
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
import de.codecentric.boot.admin.server.domain.values.StatusInfo;

/**
 * Notifier submitting events to HipChat.
 *
 * @author Jamie Brown
 */
public class HipchatNotifier extends AbstractStatusChangeNotifier {

	private static final String DEFAULT_DESCRIPTION = "<strong>#{instance.registration.name}</strong>/#{instance.id} is <strong>#{event.statusInfo.status}</strong>";

	private final SpelExpressionParser parser = new SpelExpressionParser();

	private RestTemplate restTemplate;

	/**
	 * Base URL for HipChat API (i.e. https://ACCOUNT_NAME.hipchat.com/v2
	 */
	@Nullable
	private URI url;

	/**
	 * API token that has access to notify in the room
	 */
	@Nullable
	private String authToken;

	/**
	 * Id of the room to notify
	 */
	@Nullable
	private String roomId;

	/**
	 * TRUE will cause OS notification, FALSE will only notify to room
	 */
	private boolean notify = false;

	/**
	 * Trigger description. SpEL template using event as root;
	 */
	private Expression description;

	public HipchatNotifier(InstanceRepository repository, RestTemplate restTemplate) {
		super(repository);
		this.restTemplate = restTemplate;
		this.description = parser.parseExpression(DEFAULT_DESCRIPTION, ParserContext.TEMPLATE_EXPRESSION);
	}

	@Override
	protected Mono<Void> doNotify(InstanceEvent event, Instance instance) {
		return Mono.fromRunnable(
				() -> restTemplate.postForEntity(buildUrl(), createHipChatNotification(event, instance), Void.class));
	}

	protected String buildUrl() {
		if (url == null) {
			throw new IllegalStateException("'url' must not be null.");
		}
		return String.format("%s/room/%s/notification?auth_token=%s", url, roomId, authToken);
	}

	protected HttpEntity<Map<String, Object>> createHipChatNotification(InstanceEvent event, Instance instance) {
		Map<String, Object> body = new HashMap<>();
		body.put("color", getColor(event));
		body.put("message", getMessage(event, instance));
		body.put("notify", getNotify());
		body.put("message_format", "html");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return new HttpEntity<>(body, headers);
	}

	protected boolean getNotify() {
		return notify;
	}

	@Nullable
	protected String getMessage(InstanceEvent event, Instance instance) {
		Map<String, Object> root = new HashMap<>();
		root.put("event", event);
		root.put("instance", instance);
		root.put("lastStatus", getLastStatus(event.getInstance()));
		SimpleEvaluationContext context = SimpleEvaluationContext
			.forPropertyAccessors(DataBindingPropertyAccessor.forReadOnlyAccess(), new MapAccessor())
			.withRootObject(root)
			.build();
		return description.getValue(context, String.class);
	}

	protected String getColor(InstanceEvent event) {
		if (event instanceof InstanceStatusChangedEvent) {
			return StatusInfo.STATUS_UP.equals(((InstanceStatusChangedEvent) event).getStatusInfo().getStatus())
					? "green" : "red";
		}
		else {
			return "gray";
		}
	}

	@Nullable
	public URI getUrl() {
		return url;
	}

	public void setUrl(@Nullable URI url) {
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

	public boolean isNotify() {
		return notify;
	}

	public void setNotify(boolean notify) {
		this.notify = notify;
	}

	public String getDescription() {
		return description.getExpressionString();
	}

	public void setDescription(String description) {
		this.description = parser.parseExpression(description, ParserContext.TEMPLATE_EXPRESSION);
	}

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

}
