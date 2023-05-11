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

import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.EvaluationContext;
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
import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;

import static java.util.Collections.singletonList;

public class MicrosoftTeamsNotifier extends AbstractStatusChangeNotifier {

	private static final Logger LOGGER = LoggerFactory.getLogger(MicrosoftTeamsNotifier.class);

	private static final String STATUS_KEY = "Status";

	private static final String SERVICE_URL_KEY = "Service URL";

	private static final String HEALTH_URL_KEY = "Health URL";

	private static final String MANAGEMENT_URL_KEY = "Management URL";

	private static final String SOURCE_KEY = "Source";

	private static final String DEFAULT_THEME_COLOR_EXPRESSION = "#{event.type == 'STATUS_CHANGED' ? (event.statusInfo.status=='UP' ? '6db33f' : 'b32d36') : '439fe0'}";

	private static final String DEFAULT_DEREGISTER_ACTIVITY_SUBTITLE_EXPRESSION = "#{instance.registration.name} with id #{instance.id} has de-registered from Spring Boot Admin";

	private static final String DEFAULT_REGISTER_ACTIVITY_SUBTITLE_EXPRESSION = "#{instance.registration.name} with id #{instance.id} has registered with Spring Boot Admin";

	private static final String DEFAULT_STATUS_ACTIVITY_SUBTITLE_EXPRESSION = "#{instance.registration.name} with id #{instance.id} changed status from #{lastStatus} to #{event.statusInfo.status}";

	private final SpelExpressionParser parser = new SpelExpressionParser();

	private RestTemplate restTemplate;

	/**
	 * Webhook url for Microsoft Teams Channel Webhook connector (i.e.
	 * https://outlook.office.com/webhook/{webhook-id})
	 */
	@Nullable
	private URI webhookUrl;

	/**
	 * Theme Color is the color of the accent on the message that appears in Microsoft
	 * Teams. Default is Spring Green
	 */
	private Expression themeColor;

	/**
	 * Message will be used as title of the Activity section of the Teams message when an
	 * app de-registers.
	 */
	private Expression deregisterActivitySubtitle;

	/**
	 * Message will be used as title of the Activity section of the Teams message when an
	 * app registers
	 */
	private Expression registerActivitySubtitle;

	/**
	 * Message will be used as title of the Activity section of the Teams message when an
	 * app changes status
	 */
	private Expression statusActivitySubtitle;

	/**
	 * Title of the Teams message when an app de-registers
	 */
	private String deRegisteredTitle = "De-Registered";

	/**
	 * Title of the Teams message when an app registers
	 */
	private String registeredTitle = "Registered";

	/**
	 * Title of the Teams message when an app changes status
	 */
	private String statusChangedTitle = "Status Changed";

	/**
	 * Summary section of every Teams message originating from Spring Boot Admin
	 */
	private String messageSummary = "Spring Boot Admin Notification";

	public MicrosoftTeamsNotifier(InstanceRepository repository, RestTemplate restTemplate) {
		super(repository);
		this.restTemplate = restTemplate;
		this.themeColor = parser.parseExpression(DEFAULT_THEME_COLOR_EXPRESSION, ParserContext.TEMPLATE_EXPRESSION);
		this.deregisterActivitySubtitle = parser.parseExpression(DEFAULT_DEREGISTER_ACTIVITY_SUBTITLE_EXPRESSION,
				ParserContext.TEMPLATE_EXPRESSION);
		this.registerActivitySubtitle = parser.parseExpression(DEFAULT_REGISTER_ACTIVITY_SUBTITLE_EXPRESSION,
				ParserContext.TEMPLATE_EXPRESSION);
		this.statusActivitySubtitle = parser.parseExpression(DEFAULT_STATUS_ACTIVITY_SUBTITLE_EXPRESSION,
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
			message = getStatusChangedMessage(instance, context);
		}
		else {
			return Mono.empty();
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		if (webhookUrl == null) {
			return Mono.error(new IllegalStateException("'webhookUrl' must not be null."));
		}

		return Mono.fromRunnable(() -> this.restTemplate.postForEntity(webhookUrl,
				new HttpEntity<Object>(message, headers), Void.class));
	}

	@Override
	protected boolean shouldNotify(InstanceEvent event, Instance instance) {
		return event instanceof InstanceRegisteredEvent || event instanceof InstanceDeregisteredEvent
				|| super.shouldNotify(event, instance);
	}

	protected Message getDeregisteredMessage(Instance instance, EvaluationContext context) {
		String activitySubtitle = evaluateExpression(context, deregisterActivitySubtitle);
		return createMessage(instance, deRegisteredTitle, activitySubtitle, context);
	}

	protected Message getRegisteredMessage(Instance instance, EvaluationContext context) {
		String activitySubtitle = evaluateExpression(context, registerActivitySubtitle);
		return createMessage(instance, registeredTitle, activitySubtitle, context);
	}

	protected Message getStatusChangedMessage(Instance instance, EvaluationContext context) {
		String activitySubtitle = evaluateExpression(context, statusActivitySubtitle);
		return createMessage(instance, statusChangedTitle, activitySubtitle, context);
	}

	protected Message createMessage(Instance instance, String registeredTitle, String activitySubtitle,
			EvaluationContext context) {
		List<Fact> facts = new ArrayList<>();
		facts.add(new Fact(STATUS_KEY, instance.getStatusInfo().getStatus()));
		facts.add(new Fact(SERVICE_URL_KEY, instance.getRegistration().getServiceUrl()));
		facts.add(new Fact(HEALTH_URL_KEY, instance.getRegistration().getHealthUrl()));
		facts.add(new Fact(MANAGEMENT_URL_KEY, instance.getRegistration().getManagementUrl()));
		facts.add(new Fact(SOURCE_KEY, instance.getRegistration().getSource()));

		Section section = Section.builder()
			.activityTitle(instance.getRegistration().getName())
			.activitySubtitle(activitySubtitle)
			.facts(facts)
			.build();

		return Message.builder()
			.title(registeredTitle)
			.summary(messageSummary)
			.themeColor(evaluateExpression(context, themeColor))
			.sections(singletonList(section))
			.build();
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

	@Nullable
	public URI getWebhookUrl() {
		return webhookUrl;
	}

	public void setWebhookUrl(@Nullable URI webhookUrl) {
		this.webhookUrl = webhookUrl;
	}

	public String getThemeColor() {
		return themeColor.getExpressionString();
	}

	public void setThemeColor(String themeColor) {
		this.themeColor = parser.parseExpression(themeColor, ParserContext.TEMPLATE_EXPRESSION);
	}

	public String getDeregisterActivitySubtitle() {
		return deregisterActivitySubtitle.getExpressionString();
	}

	public void setDeregisterActivitySubtitle(String deregisterActivitySubtitle) {
		this.deregisterActivitySubtitle = parser.parseExpression(deregisterActivitySubtitle,
				ParserContext.TEMPLATE_EXPRESSION);
	}

	public String getRegisterActivitySubtitle() {
		return registerActivitySubtitle.getExpressionString();
	}

	public void setRegisterActivitySubtitle(String registerActivitySubtitle) {
		this.registerActivitySubtitle = parser.parseExpression(registerActivitySubtitle,
				ParserContext.TEMPLATE_EXPRESSION);
	}

	public String getStatusActivitySubtitle() {
		return statusActivitySubtitle.getExpressionString();
	}

	public void setStatusActivitySubtitle(String statusActivitySubtitle) {
		this.statusActivitySubtitle = parser.parseExpression(statusActivitySubtitle, ParserContext.TEMPLATE_EXPRESSION);
	}

	public String getDeRegisteredTitle() {
		return deRegisteredTitle;
	}

	public void setDeRegisteredTitle(String deRegisteredTitle) {
		this.deRegisteredTitle = deRegisteredTitle;
	}

	public String getRegisteredTitle() {
		return registeredTitle;
	}

	public void setRegisteredTitle(String registeredTitle) {
		this.registeredTitle = registeredTitle;
	}

	public String getStatusChangedTitle() {
		return statusChangedTitle;
	}

	public void setStatusChangedTitle(String statusChangedTitle) {
		this.statusChangedTitle = statusChangedTitle;
	}

	public String getMessageSummary() {
		return messageSummary;
	}

	public void setMessageSummary(String messageSummary) {
		this.messageSummary = messageSummary;
	}

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@Data
	@Builder
	public static class Message {

		private final String summary;

		private final String themeColor;

		private final String title;

		@Builder.Default
		private final List<Section> sections = new ArrayList<>();

	}

	@Data
	@Builder
	public static class Section {

		private final String activityTitle;

		private final String activitySubtitle;

		@Builder.Default
		private final List<Fact> facts = new ArrayList<>();

	}

	@Data
	public static class Fact {

		private final String name;

		@Nullable
		private final String value;

	}

}
