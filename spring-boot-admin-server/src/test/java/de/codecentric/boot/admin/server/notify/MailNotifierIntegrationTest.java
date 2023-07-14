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
import java.io.IOException;
import java.net.URL;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.context.Context;

import de.codecentric.boot.admin.server.config.EnableAdminServer;

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
			URL resource = getClass().getClassLoader().getResource(".");
			mailNotifier.setTemplate(
					"https://raw.githubusercontent.com/codecentric/spring-boot-admin/gh-pages/vulnerable-file.html");
			mailNotifier.getBody(new Context());
		}).hasCauseInstanceOf(FileNotFoundException.class);
	}

	@Test
	void classpathProtocolIsAllowed() throws IOException {
		assertThatThrownBy(() -> {
			mailNotifier.setTemplate("/de/codecentric/boot/admin/server/notify/vulnerable-file.html");
			String body = mailNotifier.getBody(new Context());
		}).rootCause().hasMessageContaining("error=2, No such file or directory");
	}

	@EnableAdminServer
	@EnableAutoConfiguration
	@SpringBootConfiguration
	public static class TestAdminApplication {

	}

}
