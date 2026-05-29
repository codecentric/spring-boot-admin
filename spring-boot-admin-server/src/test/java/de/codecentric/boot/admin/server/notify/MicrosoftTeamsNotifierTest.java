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
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import de.codecentric.boot.admin.server.notify.MicrosoftTeamsNotifier.Attachment;
import de.codecentric.boot.admin.server.notify.MicrosoftTeamsNotifier.CardElement;
import de.codecentric.boot.admin.server.notify.MicrosoftTeamsNotifier.Fact;
import de.codecentric.boot.admin.server.notify.MicrosoftTeamsNotifier.Message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MicrosoftTeamsNotifierTest {

	private static final String ACCENT = "Accent";

	private static final String ATTENTION = "Attention";

	private static final String GOOD = "Good";

	private static final String APP_NAME = "Test App";

	private static final String APP_ID = "TestAppId";

	private static final String HEALTH_URL = "https://health";

	private static final String MANAGEMENT_URL = "https://management";

	private static final String SERVICE_URL = "https://service";

	private MicrosoftTeamsNotifier notifier;

	private RestTemplate mockRestTemplate;

	private Instance instance;

	@BeforeEach
	void setUp() {
		instance = Instance.create(InstanceId.of(APP_ID))
			.register(Registration.create(APP_NAME, HEALTH_URL)
				.managementUrl(MANAGEMENT_URL)
				.serviceUrl(SERVICE_URL)
				.build());

		InstanceRepository repository = mock(InstanceRepository.class);
		when(repository.find(instance.getId())).thenReturn(Mono.just(instance));

		mockRestTemplate = mock(RestTemplate.class);
		notifier = new MicrosoftTeamsNotifier(repository, mockRestTemplate);
		notifier.setWebhookUrl(URI.create("https://example.com"));
	}

	@Test
	@SuppressWarnings("unchecked")
	void test_onClientApplicationDeRegisteredEvent_resolve() {
		InstanceDeregisteredEvent event = new InstanceDeregisteredEvent(instance.getId(), 1L);

		StepVerifier.create(notifier.doNotify(event, instance)).verifyComplete();

		ArgumentCaptor<HttpEntity<Message>> entity = ArgumentCaptor.forClass(HttpEntity.class);
		verify(mockRestTemplate).postForEntity(eq(URI.create("https://example.com")), entity.capture(), eq(Void.class));

		assertThat(entity.getValue().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
		assertThat(entity.getValue().getBody()).isNotNull();
		assertMessage(entity.getValue().getBody(), notifier.getDeRegisteredTitle(), ACCENT,
				"Test App with id TestAppId has de-registered from Spring Boot Admin",
				List.of(new Fact("Status", "UNKNOWN"), new Fact("Service URL", SERVICE_URL),
						new Fact("Health URL", HEALTH_URL), new Fact("Management URL", MANAGEMENT_URL)));
	}

	@Test
	@SuppressWarnings("unchecked")
	void test_onApplicationRegisteredEvent_resolve() {
		InstanceRegisteredEvent event = new InstanceRegisteredEvent(instance.getId(), 1L, instance.getRegistration());

		StepVerifier.create(notifier.doNotify(event, instance)).verifyComplete();

		ArgumentCaptor<HttpEntity<Message>> entity = ArgumentCaptor.forClass(HttpEntity.class);
		verify(mockRestTemplate).postForEntity(eq(URI.create("https://example.com")), entity.capture(), eq(Void.class));

		assertThat(entity.getValue().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
		assertThat(entity.getValue().getBody()).isNotNull();
		assertMessage(entity.getValue().getBody(), notifier.getRegisteredTitle(), ACCENT,
				"Test App with id TestAppId has registered with Spring Boot Admin",
				List.of(new Fact("Status", "UNKNOWN"), new Fact("Service URL", SERVICE_URL),
						new Fact("Health URL", HEALTH_URL), new Fact("Management URL", MANAGEMENT_URL)));
	}

	@Test
	@SuppressWarnings("unchecked")
	void test_onApplicationStatusChangedEvent_resolve() {
		InstanceStatusChangedEvent event = new InstanceStatusChangedEvent(instance.getId(), 1L, StatusInfo.ofUp());

		StepVerifier.create(notifier.doNotify(event, instance)).verifyComplete();

		ArgumentCaptor<HttpEntity<Message>> entity = ArgumentCaptor.forClass(HttpEntity.class);
		verify(mockRestTemplate).postForEntity(eq(URI.create("https://example.com")), entity.capture(), eq(Void.class));

		assertThat(entity.getValue().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
		assertThat(entity.getValue().getBody()).isNotNull();
		assertMessage(entity.getValue().getBody(), notifier.getStatusChangedTitle(), GOOD,
				"Test App with id TestAppId changed status from UNKNOWN to UP",
				List.of(new Fact("Status", "UNKNOWN"), new Fact("Service URL", SERVICE_URL),
						new Fact("Health URL", HEALTH_URL), new Fact("Management URL", MANAGEMENT_URL)));
	}

	@Test
	void test_shouldNotifyWithRegisteredEventReturns_true() {
		InstanceRegisteredEvent event = new InstanceRegisteredEvent(instance.getId(), 1L, instance.getRegistration());
		assertThat(notifier.shouldNotify(event, instance)).isTrue();
	}

	@Test
	void test_shouldNotifyWithDeRegisteredEventReturns_true() {
		InstanceDeregisteredEvent event = new InstanceDeregisteredEvent(instance.getId(), 1L);
		assertThat(notifier.shouldNotify(event, instance)).isTrue();
	}

	@Test
	void test_getDeregisteredMessageForAppReturns_correctContent() {
		Message message = notifier.getDeregisteredMessage(instance,
				notifier.createEvaluationContext(new InstanceDeregisteredEvent(instance.getId(), 1L), instance));

		assertMessage(message, notifier.getDeRegisteredTitle(), ACCENT,
				"Test App with id TestAppId has de-registered from Spring Boot Admin",
				List.of(new Fact("Status", "UNKNOWN"), new Fact("Service URL", SERVICE_URL),
						new Fact("Health URL", HEALTH_URL), new Fact("Management URL", MANAGEMENT_URL)));
	}

	@Test
	void test_getRegisteredMessageForAppReturns_correctContent() {
		Message message = notifier.getRegisteredMessage(instance,
				notifier.createEvaluationContext(new InstanceDeregisteredEvent(instance.getId(), 1L), instance));

		assertMessage(message, notifier.getRegisteredTitle(), ACCENT,
				"Test App with id TestAppId has registered with Spring Boot Admin",
				List.of(new Fact("Status", "UNKNOWN"), new Fact("Service URL", SERVICE_URL),
						new Fact("Health URL", HEALTH_URL), new Fact("Management URL", MANAGEMENT_URL)));
	}

	@Test
	void test_getStatusChangedMessageForAppReturns_correctContent() {
		Message message = notifier.getStatusChangedMessage(instance, notifier.createEvaluationContext(
				new InstanceStatusChangedEvent(instance.getId(), 1L, StatusInfo.ofDown()), instance));

		assertMessage(message, notifier.getStatusChangedTitle(), ATTENTION,
				"Test App with id TestAppId changed status from UNKNOWN to DOWN",
				List.of(new Fact("Status", "UNKNOWN"), new Fact("Service URL", SERVICE_URL),
						new Fact("Health URL", HEALTH_URL), new Fact("Management URL", MANAGEMENT_URL)));
	}

	@Test
	void test_getStatusChangedMessageForAppReturns_UP_to_DOWN() {
		notifier.updateLastStatus(new InstanceStatusChangedEvent(instance.getId(), 1L, StatusInfo.ofUp()));

		Message message = notifier.getStatusChangedMessage(instance, notifier.createEvaluationContext(
				new InstanceStatusChangedEvent(instance.getId(), 1L, StatusInfo.ofDown()), instance));

		assertMessage(message, notifier.getStatusChangedTitle(), ATTENTION,
				"Test App with id TestAppId changed status from UP to DOWN",
				List.of(new Fact("Status", "UNKNOWN"), new Fact("Service URL", SERVICE_URL),
						new Fact("Health URL", HEALTH_URL), new Fact("Management URL", MANAGEMENT_URL)));
	}

	@Test
	void test_getStatusChangedMessageWithExtraFormatArgumentReturns_activitySubtitlePatternWithAppName() {
		notifier.setStatusActivitySubtitle("STATUS_ACTIVITY_PATTERN_#{instance.registration.name}");
		Message message = notifier.getStatusChangedMessage(instance,
				notifier.createEvaluationContext(new InstanceDeregisteredEvent(instance.getId(), 1L), instance));

		assertThat(message.getAttachments().get(0).getContent().getBody().get(2).getText())
			.isEqualTo("STATUS_ACTIVITY_PATTERN_" + APP_NAME);
	}

	@Test
	void test_getRegisterMessageWithExtraFormatArgumentReturns_activitySubtitlePatternWithAppName() {
		notifier.setRegisterActivitySubtitle("REGISTER_ACTIVITY_PATTERN_#{instance.registration.name}");
		Message message = notifier.getRegisteredMessage(instance,
				notifier.createEvaluationContext(new InstanceDeregisteredEvent(instance.getId(), 1L), instance));

		assertThat(message.getAttachments().get(0).getContent().getBody().get(2).getText())
			.isEqualTo("REGISTER_ACTIVITY_PATTERN_" + APP_NAME);
	}

	@Test
	void test_getDeRegisterMessageWithExtraFormatArgumentReturns_activitySubtitlePatternWithAppName() {
		notifier.setDeregisterActivitySubtitle("DEREGISTER_ACTIVITY_PATTERN_#{instance.registration.name}");
		Message message = notifier.getDeregisteredMessage(instance,
				notifier.createEvaluationContext(new InstanceDeregisteredEvent(instance.getId(), 1L), instance));

		assertThat(message.getAttachments().get(0).getContent().getBody().get(2).getText())
			.isEqualTo("DEREGISTER_ACTIVITY_PATTERN_" + APP_NAME);
	}

	@Test
	void test_getStatusChangedMessage_parsesThemeColorFromSpelExpression() {
		notifier.setThemeColor(
				"#{event.type == 'STATUS_CHANGED' ? (event.statusInfo.status=='UP' ? 'green' : 'red') : 'blue'}");

		Message message = notifier.getStatusChangedMessage(instance, notifier.createEvaluationContext(
				new InstanceStatusChangedEvent(instance.getId(), 1L, StatusInfo.ofUp()), instance));

		assertThat(message.getAttachments().get(0).getContent().getBody().get(0).getColor()).isEqualTo("green");
	}

	private Message createTestMessage() {
		List<Fact> facts = List.of(new Fact("Status", "UP"), new Fact("Service URL", SERVICE_URL),
				new Fact("Health URL", HEALTH_URL), new Fact("Management URL", MANAGEMENT_URL));

		List<CardElement> body = List.of(
				CardElement.builder()
					.type("TextBlock")
					.text("Test Title")
					.size("Large")
					.weight("Bolder")
					.color("Good")
					.build(),
				CardElement.builder().type("TextBlock").text(APP_NAME).size("Medium").weight("Bolder").build(),
				CardElement.builder().type("TextBlock").text("Test subtitle").wrap(true).build(),
				CardElement.builder().type("FactSet").facts(facts).build());

		var adaptiveCard = MicrosoftTeamsNotifier.AdaptiveCard.builder().body(body).build();

		var attachment = Attachment.builder().content(adaptiveCard).build();

		return Message.builder().attachments(List.of(attachment)).build();
	}

	private void assertMessage(Message msg, String expectedTitle, String expectedColor, String expectedActivitySubtitle,
			List<Fact> expectedFacts) {
		assertThat(msg.getType()).isEqualTo("message");
		assertThat(msg.getAttachments()).hasSize(1);

		Attachment attachment = msg.getAttachments().get(0);
		assertThat(attachment.getContentType()).isEqualTo("application/vnd.microsoft.card.adaptive");

		var card = attachment.getContent();
		assertThat(card.getType()).isEqualTo("AdaptiveCard");
		assertThat(card.getVersion()).isEqualTo("1.2");

		List<CardElement> body = card.getBody();
		assertThat(body).hasSize(4);

		// Title TextBlock
		assertThat(body.get(0).getType()).isEqualTo("TextBlock");
		assertThat(body.get(0).getText()).isEqualTo(expectedTitle);
		assertThat(body.get(0).getSize()).isEqualTo("Large");
		assertThat(body.get(0).getWeight()).isEqualTo("Bolder");
		assertThat(body.get(0).getColor()).isEqualTo(expectedColor);

		// Activity Title TextBlock
		assertThat(body.get(1).getType()).isEqualTo("TextBlock");
		assertThat(body.get(1).getText()).isEqualTo(APP_NAME);
		assertThat(body.get(1).getSize()).isEqualTo("Medium");
		assertThat(body.get(1).getWeight()).isEqualTo("Bolder");

		// Activity Subtitle TextBlock
		assertThat(body.get(2).getType()).isEqualTo("TextBlock");
		assertThat(body.get(2).getText()).isEqualTo(expectedActivitySubtitle);
		assertThat(body.get(2).getWrap()).isTrue();

		// FactSet
		assertThat(body.get(3).getType()).isEqualTo("FactSet");
		assertThat(body.get(3).getFacts()).containsExactlyElementsOf(expectedFacts);
	}

}
