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

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Map;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.context.Context;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;

@SpringBootTest(properties = { "spring.mail.host=localhost", "spring.boot.admin.notify.mail=true" })
class MailNotifierIntegrationTest implements WithAssertions {

	@Autowired
	MailNotifier mailNotifier;

	@Test
	void fileProtocolIsNotAllowed() {
		assertThatThrownBy(() -> {
			URL resource = getClass().getClassLoader().getResource(".");
			mailNotifier.setTemplate(
					"file://" + resource.getFile() + "de/codecentric/boot/admin/server/notify/vulnerable-file.html");
			mailNotifier.getBody(new Context());
		}).hasCauseInstanceOf(FileNotFoundException.class);
	}

	@Test
	void httpProtocolIsNotAllowed() {
		assertThatThrownBy(() -> {
			mailNotifier.setTemplate(
					"https://raw.githubusercontent.com/codecentric/spring-boot-admin/gh-pages/vulnerable-file.html");
			mailNotifier.getBody(new Context());
		}).hasCauseInstanceOf(FileNotFoundException.class);
	}

	@Test
	void classpathProtocolIsAllowed() {
		assertThatNoException().isThrownBy(() -> {
			mailNotifier.setTemplate("/de/codecentric/boot/admin/server/notify/allowed-file.html");
			mailNotifier.getBody(new Context());
		});
	}

	@Test
	void callToReflectionUtilsAreNotAllowed() {
		assertThatThrownBy(() -> {
			mailNotifier.setTemplate("/de/codecentric/boot/admin/server/notify/vulnerable-file.html");
			mailNotifier.getBody(new Context());
		}).rootCause()
			.hasMessageContaining(
					"Access is forbidden for type 'org.springframework.util.ReflectionUtils' in this expression context.");
	}

	@Test
	void defaultTemplateRendersNestedStatusDetails() {
		Instance instance = Instance.create(InstanceId.of("cafebabe"))
			.register(Registration.create("application-name", "http://localhost:8081/actuator/health").build());
		Map<String, Object> details = Map.of("Complex Value", Map.of("Nested Simple Value", "99!"));
		Context ctx = new Context();
		ctx.setVariable("instance", instance);
		ctx.setVariable("event",
				new InstanceStatusChangedEvent(instance.getId(), instance.getVersion(), StatusInfo.ofDown(details)));
		ctx.setVariable("lastStatus", "UP");

		assertThat(mailNotifier.getBody(ctx)).contains("Nested Simple Value").contains("99!");
	}

	@EnableAdminServer
	@EnableAutoConfiguration
	@SpringBootConfiguration
	public static class TestAdminApplication {

	}

}
