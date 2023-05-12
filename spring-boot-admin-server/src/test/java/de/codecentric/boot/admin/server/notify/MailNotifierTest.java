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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import jakarta.activation.DataHandler;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.util.StreamUtils;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class MailNotifierTest {

	private final Instance instance = Instance.create(InstanceId.of("cafebabe"))
		.register(Registration.create("application-name", "http://localhost:8081/actuator/health")
			.managementUrl("http://localhost:8081/actuator")
			.serviceUrl("http://localhost:8081/")
			.build());

	private JavaMailSender sender;

	private MailNotifier notifier;

	private InstanceRepository repository;

	@BeforeEach
	public void setup() {
		repository = mock(InstanceRepository.class);
		when(repository.find(instance.getId())).thenReturn(Mono.just(instance));

		sender = mock(JavaMailSender.class);
		when(sender.createMimeMessage()).thenAnswer((args) -> new MimeMessage(Session.getInstance(new Properties())));

		SpringTemplateEngine templateEngine = new SpringTemplateEngine();
		ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
		resolver.setTemplateMode(TemplateMode.HTML);
		resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
		templateEngine.addTemplateResolver(resolver);

		notifier = new MailNotifier(sender, repository, templateEngine);
		notifier.setTo(new String[] { "foo@bar.com" });
		notifier.setCc(new String[] { "bar@foo.com" });
		notifier.setFrom("SBA <no-reply@example.com>");
		notifier.setBaseUrl("http://localhost:8080");
		notifier.setTemplate("/META-INF/spring-boot-admin-server/mail/status-changed.html");
	}

	@Test
	public void should_send_mail_using_default_template() throws IOException, MessagingException {
		Map<String, Object> details = new HashMap<>();
		details.put("Simple Value", 1234);
		details.put("Complex Value", singletonMap("Nested Simple Value", "99!"));

		StepVerifier.create(notifier.notify(
				new InstanceStatusChangedEvent(instance.getId(), instance.getVersion(), StatusInfo.ofDown(details))))
			.verifyComplete();

		ArgumentCaptor<MimeMessage> mailCaptor = ArgumentCaptor.forClass(MimeMessage.class);
		verify(sender).send(mailCaptor.capture());

		MimeMessage mail = mailCaptor.getValue();

		assertThat(mail.getSubject()).isEqualTo("application-name (cafebabe) is DOWN");
		assertThat(mail.getRecipients(Message.RecipientType.TO)).containsExactly(new InternetAddress("foo@bar.com"));
		assertThat(mail.getRecipients(Message.RecipientType.CC)).containsExactly(new InternetAddress("bar@foo.com"));
		assertThat(mail.getFrom()).containsExactly(new InternetAddress("SBA <no-reply@example.com>"));
		assertThat(mail.getDataHandler().getContentType()).isEqualTo("text/html;charset=UTF-8");

		String body = extractBody(mail.getDataHandler());
		assertThat(body).isEqualTo(loadExpectedBody("expected-default-mail"));
	}

	@Test
	public void should_send_mail_using_custom_template_with_additional_properties()
			throws IOException, MessagingException {
		notifier.setTemplate("/de/codecentric/boot/admin/server/notify/custom-mail.html");
		notifier.getAdditionalProperties().put("customProperty", "HELLO WORLD!");

		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(instance.getId(), instance.getVersion(), StatusInfo.ofDown())))
			.verifyComplete();

		ArgumentCaptor<MimeMessage> mailCaptor = ArgumentCaptor.forClass(MimeMessage.class);
		verify(sender).send(mailCaptor.capture());

		MimeMessage mail = mailCaptor.getValue();
		String body = extractBody(mail.getDataHandler());
		assertThat(body).isEqualTo(loadExpectedBody("expected-custom-mail"));
	}

	// The following tests are rather for AbstractNotifier

	@Test
	public void should_not_send_mail_when_disabled() {
		notifier.setEnabled(false);
		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(instance.getId(), instance.getVersion(), StatusInfo.ofUp())))
			.verifyComplete();

		verifyNoMoreInteractions(sender);
	}

	@Test
	public void should_not_send_when_unknown_to_up() {
		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(instance.getId(), instance.getVersion(), StatusInfo.ofUp())))
			.verifyComplete();

		verifyNoMoreInteractions(sender);
	}

	@Test
	public void should_not_send_on_wildcard_ignore() {
		notifier.setIgnoreChanges(new String[] { "*:UP" });
		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(instance.getId(), instance.getVersion(), StatusInfo.ofUp())))
			.verifyComplete();

		verifyNoMoreInteractions(sender);
	}

	@Test
	public void should_not_propagate_error() {
		Notifier notifier = new AbstractStatusChangeNotifier(repository) {
			@Override
			protected Mono<Void> doNotify(InstanceEvent event, Instance application) {
				return Mono.error(new IllegalStateException("test"));
			}
		};
		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(instance.getId(), instance.getVersion(), StatusInfo.ofUp())))
			.verifyComplete();
	}

	private String loadExpectedBody(String resource) throws IOException {
		return StreamUtils.copyToString(this.getClass().getResourceAsStream(resource), StandardCharsets.UTF_8);
	}

	private String extractBody(DataHandler dataHandler) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream(4096);
		dataHandler.writeTo(os);
		return os.toString(StandardCharsets.UTF_8).replaceAll("\\r?\\n", "\n");
	}

}
