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
import java.util.Collections;
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
 * Notifier submitting events to Slack.
 *
 * @author Artur Dobosiewicz
 */
public class SlackNotifier extends AbstractStatusChangeNotifier {

	private static final String DEFAULT_MESSAGE = "*#{instance.registration.name}* (#{instance.id}) is *#{event.statusInfo.status}*";

	private final SpelExpressionParser parser = new SpelExpressionParser();

	private RestTemplate restTemplate;

	/**
	 * Webhook url for Slack API (i.e. https://hooks.slack.com/services/xxx)
	 */
	@Nullable
	private URI webhookUrl;

	/**
	 * Optional channel name without # sign (i.e. somechannel)
	 */
	@Nullable
	private String channel;

	/**
	 * Optional emoji icon without colons (i.e. my-emoji)
	 */
	@Nullable
	private String icon;

	/**
	 * Optional username which sends notification
	 */
	@Nullable
	private String username = "Spring Boot Admin";

	/**
	 * Message formatted using Slack markups. SpEL template using event as root
	 */
	private Expression message;

	public SlackNotifier(InstanceRepository repository, RestTemplate restTemplate) {
		super(repository);
		this.restTemplate = restTemplate;
		this.message = parser.parseExpression(DEFAULT_MESSAGE, ParserContext.TEMPLATE_EXPRESSION);
	}

	@Override
	protected Mono<Void> doNotify(InstanceEvent event, Instance instance) {
		if (webhookUrl == null) {
			return Mono.error(new IllegalStateException("'webhookUrl' must not be null."));
		}
		return Mono
			.fromRunnable(() -> restTemplate.postForEntity(webhookUrl, createMessage(event, instance), Void.class));
	}

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	protected Object createMessage(InstanceEvent event, Instance instance) {
		Map<String, Object> messageJson = new HashMap<>();
		messageJson.put("username", username);
		if (icon != null) {
			messageJson.put("icon_emoji", ":" + icon + ":");
		}
		if (channel != null) {
			messageJson.put("channel", channel);
		}

		Map<String, Object> attachments = new HashMap<>();
		attachments.put("text", getText(event, instance));
		attachments.put("color", getColor(event));
		attachments.put("mrkdwn_in", Collections.singletonList("text"));
		messageJson.put("attachments", Collections.singletonList(attachments));

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return new HttpEntity<>(messageJson, headers);
	}

	@Nullable
	protected String getText(InstanceEvent event, Instance instance) {
		Map<String, Object> root = new HashMap<>();
		root.put("event", event);
		root.put("instance", instance);
		root.put("lastStatus", getLastStatus(event.getInstance()));
		SimpleEvaluationContext context = SimpleEvaluationContext
			.forPropertyAccessors(DataBindingPropertyAccessor.forReadOnlyAccess(), new MapAccessor())
			.withRootObject(root)
			.build();

		return message.getValue(context, String.class);
	}

	protected String getColor(InstanceEvent event) {
		if (event instanceof InstanceStatusChangedEvent) {
			return StatusInfo.STATUS_UP.equals(((InstanceStatusChangedEvent) event).getStatusInfo().getStatus())
					? "good" : "danger";
		}
		else {
			return "#439FE0";
		}
	}

	@Nullable
	public URI getWebhookUrl() {
		return webhookUrl;
	}

	public void setWebhookUrl(@Nullable URI webhookUrl) {
		this.webhookUrl = webhookUrl;
	}

	@Nullable
	public String getChannel() {
		return channel;
	}

	public void setChannel(@Nullable String channel) {
		this.channel = channel;
	}

	@Nullable
	public String getIcon() {
		return icon;
	}

	public void setIcon(@Nullable String icon) {
		this.icon = icon;
	}

	@Nullable
	public String getUsername() {
		return username;
	}

	public void setUsername(@Nullable String username) {
		this.username = username;
	}

	public String getMessage() {
		return message.getExpressionString();
	}

	public void setMessage(String message) {
		this.message = parser.parseExpression(message, ParserContext.TEMPLATE_EXPRESSION);
	}

}
