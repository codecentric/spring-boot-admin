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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.DataBindingPropertyAccessor;
import org.springframework.expression.spel.support.MapAccessor;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;

import static java.util.Collections.singletonList;

public class MicrosoftTeamsNotifier extends AbstractStatusChangeNotifier {

	private static final String STATUS_KEY = "Status";

	private static final String SERVICE_URL_KEY = "Service URL";

	private static final String HEALTH_URL_KEY = "Health URL";

	private static final String MANAGEMENT_URL_KEY = "Management URL";

	private static final String SOURCE_KEY = "Source";

	private static final String DEFAULT_TITLE_COLOR_EXPRESSION = "#{event.type == 'STATUS_CHANGED' ? (event.statusInfo.status=='UP' ? 'Good' : 'Attention') : 'Accent'}";

	private static final String DEFAULT_DEREGISTER_TEXT_EXPRESSION = "#{instance.registration.name} with id #{instance.id} has de-registered from Spring Boot Admin";

	private static final String DEFAULT_REGISTER_TEXT_EXPRESSION = "#{instance.registration.name} with id #{instance.id} has registered with Spring Boot Admin";

	private static final String DEFAULT_STATUS_CHANGED_TEXT_EXPRESSION = "#{instance.registration.name} with id #{instance.id} changed status from #{lastStatus} to #{event.statusInfo.status}";

	private final SpelExpressionParser parser = new SpelExpressionParser();

	@Setter
	private RestTemplate restTemplate;

	/**
	 * Webhook url for Microsoft Teams Channel Webhook connector (i.e.
	 * <a href="https://outlook.office.com/webhook/">...</a>{webhook-id})
	 */
	@Nullable private String webhookUrl;

	/**
	 * Expression for the color of the message title, see
	 * <a href="https://adaptivecards.microsoft.com/?topic=TextBlock#color">supported
	 * colors</a>
	 */
	private Expression titleColorExpression;

	/**
	 * Expression for the text that will be displayed when an app deregisters.
	 */
	private Expression deregisteredTextExpression;

	/**
	 * Expression for the text that will be displayed when an app registers
	 */
	private Expression registeredTextExpression;

	/**
	 * Expression for the text that will be displayed when an app changes status
	 */
	private Expression statusChangedTextExpression;

	/**
	 * Title of the Teams message when an app deregisters
	 */
	@Setter
	@Getter
	private String deregisteredTitle = "Deregistered";

	/**
	 * Title of the Teams message when an app registers
	 */
	@Setter
	@Getter
	private String registeredTitle = "Registered";

	/**
	 * Title of the Teams message when an app changes status
	 */
	@Setter
	@Getter
	private String statusChangedTitle = "Status Changed";

	public MicrosoftTeamsNotifier(InstanceRepository repository, RestTemplate restTemplate) {
		super(repository);
		this.restTemplate = restTemplate;
		this.titleColorExpression = parser.parseExpression(DEFAULT_TITLE_COLOR_EXPRESSION,
				ParserContext.TEMPLATE_EXPRESSION);
		this.deregisteredTextExpression = parser.parseExpression(DEFAULT_DEREGISTER_TEXT_EXPRESSION,
				ParserContext.TEMPLATE_EXPRESSION);
		this.registeredTextExpression = parser.parseExpression(DEFAULT_REGISTER_TEXT_EXPRESSION,
				ParserContext.TEMPLATE_EXPRESSION);
		this.statusChangedTextExpression = parser.parseExpression(DEFAULT_STATUS_CHANGED_TEXT_EXPRESSION,
				ParserContext.TEMPLATE_EXPRESSION);
	}

	@Override
	protected Mono<Void> doNotify(InstanceEvent event, Instance instance) {
		Message message;
		EvaluationContext context = createEvaluationContext(event, instance);
		if (event instanceof InstanceRegisteredEvent) {
			message = getRegisteredMessage(instance, context);
		}
		else if (event instanceof InstanceDeregisteredEvent) {
			message = getDeregisteredMessage(instance, context);
		}
		else if (event instanceof InstanceStatusChangedEvent) {
			message = getStatusChangedTextExpression(instance, context);
		}
		else {
			return Mono.empty();
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		if (webhookUrl == null) {
			return Mono.error(new IllegalStateException("'webhookUrl' must not be null."));
		}

		URI uri = URI.create(webhookUrl);
		return Mono.fromRunnable(
				() -> this.restTemplate.postForEntity(uri, new HttpEntity<Object>(message, headers), Void.class));
	}

	@Override
	protected boolean shouldNotify(InstanceEvent event, Instance instance) {
		return event instanceof InstanceRegisteredEvent || event instanceof InstanceDeregisteredEvent
				|| super.shouldNotify(event, instance);
	}

	protected Message getDeregisteredMessage(Instance instance, EvaluationContext context) {
		String textValue = evaluateExpression(context, deregisteredTextExpression);
		return createMessage(instance, deregisteredTitle, textValue, context);
	}

	protected Message getRegisteredMessage(Instance instance, EvaluationContext context) {
		String textValue = evaluateExpression(context, registeredTextExpression);
		return createMessage(instance, registeredTitle, textValue, context);
	}

	protected Message getStatusChangedTextExpression(Instance instance, EvaluationContext context) {
		String textValue = evaluateExpression(context, statusChangedTextExpression);
		return createMessage(instance, statusChangedTitle, textValue, context);
	}

	protected Message createMessage(Instance instance, String registeredTitle, String activitySubtitle,
			EvaluationContext context) {
		List<Fact> facts = new ArrayList<>();
		addFactIfNotNull(facts, STATUS_KEY, instance.getStatusInfo().getStatus());
		addFactIfNotNull(facts, SERVICE_URL_KEY, instance.getRegistration().getServiceUrl());
		addFactIfNotNull(facts, HEALTH_URL_KEY, instance.getRegistration().getHealthUrl());
		addFactIfNotNull(facts, MANAGEMENT_URL_KEY, instance.getRegistration().getManagementUrl());
		addFactIfNotNull(facts, SOURCE_KEY, instance.getRegistration().getSource());

		String titleColorValue = evaluateExpression(context, titleColorExpression);

		List<CardElement> cardBody = new ArrayList<>();

		// Title
		cardBody.add(CardElement.builder()
			.type("TextBlock")
			.text(registeredTitle)
			.size("Large")
			.weight("Bolder")
			.color(titleColorValue)
			.build());

		// Service Name
		cardBody.add(CardElement.builder()
			.type("TextBlock")
			.text(instance.getRegistration().getName())
			.size("Medium")
			.weight("Bolder")
			.build());

		// Text
		cardBody.add(CardElement.builder().type("TextBlock").text(activitySubtitle).wrap(true).build());

		// Facts
		cardBody.add(CardElement.builder().type("FactSet").facts(facts).build());

		AdaptiveCard adaptiveCard = AdaptiveCard.builder().body(cardBody).build();

		Attachment attachment = Attachment.builder().content(adaptiveCard).build();

		return Message.builder().attachments(singletonList(attachment)).build();
	}

	protected String evaluateExpression(EvaluationContext context, Expression expression) {
		return Objects.requireNonNull(expression.getValue(context, String.class));
	}

	protected EvaluationContext createEvaluationContext(InstanceEvent event, Instance instance) {
		Map<String, Object> root = new HashMap<>();
		root.put("event", event);
		root.put("instance", instance);
		root.put("lastStatus", getLastStatus(event.getInstance()));
		return SimpleEvaluationContext
			.forPropertyAccessors(DataBindingPropertyAccessor.forReadOnlyAccess(), new MapAccessor())
			.withRootObject(root)
			.build();
	}

	private void addFactIfNotNull(List<Fact> facts, String title, @Nullable String value) {
		if (value != null && !value.isBlank()) {
			facts.add(new Fact(title, value));
		}
	}

	@Nullable public String getWebhookUrl() {
		return webhookUrl;
	}

	public void setWebhookUrl(@Nullable String webhookUrl) {
		this.webhookUrl = webhookUrl;
	}

	public String getTitleColorExpression() {
		return titleColorExpression.getExpressionString();
	}

	public void setTitleColorExpression(String titleColorExpression) {
		this.titleColorExpression = parser.parseExpression(titleColorExpression, ParserContext.TEMPLATE_EXPRESSION);
	}

	public String getDeregisteredTextExpression() {
		return deregisteredTextExpression.getExpressionString();
	}

	public void setDeregisteredTextExpression(String deregisteredTextExpression) {
		this.deregisteredTextExpression = parser.parseExpression(deregisteredTextExpression,
				ParserContext.TEMPLATE_EXPRESSION);
	}

	public String getRegisteredTextExpression() {
		return registeredTextExpression.getExpressionString();
	}

	public void setRegisteredTextExpression(String registeredTextExpression) {
		this.registeredTextExpression = parser.parseExpression(registeredTextExpression,
				ParserContext.TEMPLATE_EXPRESSION);
	}

	public String getStatusChangedTextExpression() {
		return statusChangedTextExpression.getExpressionString();
	}

	public void setStatusChangedTextExpression(String statusChangedTextExpression) {
		this.statusChangedTextExpression = parser.parseExpression(statusChangedTextExpression,
				ParserContext.TEMPLATE_EXPRESSION);
	}

	@Data
	@Builder
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class Message {

		private final String type = "message";

		@Builder.Default
		private final List<Attachment> attachments = new ArrayList<>();

	}

	@Data
	@Builder
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class Attachment {

		private final String contentType = "application/vnd.microsoft.card.adaptive";

		@Nullable private final String contentUrl = null;

		private final AdaptiveCard content;

	}

	@Data
	@Builder
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class AdaptiveCard {

		@Builder.Default
		@JsonProperty("$schema")
		private final String schema = "http://adaptivecards.io/schemas/adaptive-card.json";

		private final String type = "AdaptiveCard";

		private final String version = "1.2";

		@Builder.Default
		private final List<CardElement> body = new ArrayList<>();

	}

	@Data
	@Builder
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class CardElement {

		private final String type;

		@Nullable private final String text;

		@Nullable private final String size;

		@Nullable private final String weight;

		@Nullable private final String color;

		@Nullable private final Boolean wrap;

		@Nullable private final List<Fact> facts;

	}

	public record Fact(String title, @Nullable String value) {
	}

}
