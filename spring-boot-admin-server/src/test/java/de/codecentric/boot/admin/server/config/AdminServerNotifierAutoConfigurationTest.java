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

package de.codecentric.boot.admin.server.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.hazelcast.autoconfigure.HazelcastAutoConfiguration;
import org.springframework.boot.http.client.autoconfigure.reactive.ClientHttpConnectorAutoConfiguration;
import org.springframework.boot.restclient.autoconfigure.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.webclient.autoconfigure.WebClientAutoConfiguration;
import org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import de.codecentric.boot.admin.server.notify.CompositeNotifier;
import de.codecentric.boot.admin.server.notify.DiscordNotifier;
import de.codecentric.boot.admin.server.notify.HipchatNotifier;
import de.codecentric.boot.admin.server.notify.LetsChatNotifier;
import de.codecentric.boot.admin.server.notify.MailNotifier;
import de.codecentric.boot.admin.server.notify.MicrosoftTeamsNotifier;
import de.codecentric.boot.admin.server.notify.NotificationTrigger;
import de.codecentric.boot.admin.server.notify.Notifier;
import de.codecentric.boot.admin.server.notify.NotifierProxyProperties;
import de.codecentric.boot.admin.server.notify.OpsGenieNotifier;
import de.codecentric.boot.admin.server.notify.PagerdutyNotifier;
import de.codecentric.boot.admin.server.notify.RocketChatNotifier;
import de.codecentric.boot.admin.server.notify.SlackNotifier;
import de.codecentric.boot.admin.server.notify.TelegramNotifier;
import de.codecentric.boot.admin.server.notify.TestNotifier;
import de.codecentric.boot.admin.server.notify.WebexNotifier;

import static org.assertj.core.api.Assertions.assertThat;

class AdminServerNotifierAutoConfigurationTest {

	private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(RestTemplateAutoConfiguration.class,
				ClientHttpConnectorAutoConfiguration.class, WebClientAutoConfiguration.class,
				HazelcastAutoConfiguration.class, WebMvcAutoConfiguration.class, AdminServerAutoConfiguration.class,
				AdminServerNotifierAutoConfiguration.class))
		.withUserConfiguration(AdminServerMarkerConfiguration.class);

	@Test
	void test_notifierListener() {
		this.contextRunner.withUserConfiguration(TestSingleNotifierConfig.class).run((context) -> {
			assertThat(context).getBean(Notifier.class).isInstanceOf(TestNotifier.class);
			assertThat(context).getBeans(Notifier.class).hasSize(1);
		});
	}

	@Test
	void test_no_notifierListener() {
		this.contextRunner.run((context) -> assertThat(context).doesNotHaveBean(NotificationTrigger.class));
	}

	@Test
	void test_mail() {
		this.contextRunner.withUserConfiguration(MailSenderConfig.class)
			.run((context) -> assertThat(context).getBean(MailNotifier.class).isInstanceOf(MailNotifier.class));
	}

	@Test
	void test_hipchat() {
		this.contextRunner.withPropertyValues("spring.boot.admin.notify.hipchat.url:https://example.com")
			.run((context) -> assertThat(context).hasSingleBean(HipchatNotifier.class));
	}

	@Test
	void test_letschat() {
		this.contextRunner.withPropertyValues("spring.boot.admin.notify.letschat.url:https://example.com")
			.run((context) -> assertThat(context).hasSingleBean(LetsChatNotifier.class));
	}

	@Test
	void test_slack() {
		this.contextRunner.withPropertyValues("spring.boot.admin.notify.slack.webhook-url:https://example.com")
			.run((context) -> assertThat(context).hasSingleBean(SlackNotifier.class));
	}

	@Test
	void test_pagerduty() {
		this.contextRunner.withPropertyValues("spring.boot.admin.notify.pagerduty.service-key:foo")
			.run((context) -> assertThat(context).hasSingleBean(PagerdutyNotifier.class));
	}

	@Test
	void test_opsgenie() {
		this.contextRunner.withPropertyValues("spring.boot.admin.notify.opsgenie.api-key:foo")
			.run((context) -> assertThat(context).hasSingleBean(OpsGenieNotifier.class));
	}

	@Test
	void test_ms_teams() {
		this.contextRunner.withPropertyValues("spring.boot.admin.notify.ms-teams.webhook-url:https://example.com")
			.run((context) -> assertThat(context).hasSingleBean(MicrosoftTeamsNotifier.class));
	}

	@Test
	void test_telegram() {
		this.contextRunner
			.withPropertyValues(
					"spring.boot.admin.notify.telegram.auth-token:123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11")
			.run((context) -> assertThat(context).hasSingleBean(TelegramNotifier.class));
	}

	@Test
	void test_discord() {
		this.contextRunner.withPropertyValues("spring.boot.admin.notify.discord.webhook-url:https://example.com")
			.run((context) -> assertThat(context).hasSingleBean(DiscordNotifier.class));
	}

	@Test
	void test_rocketchat() {
		this.contextRunner.withPropertyValues("spring.boot.admin.notify.rocketchat.url:https://example.com")
			.run((context) -> assertThat(context).hasSingleBean(RocketChatNotifier.class));
	}

	@Test
	void test_webex() {
		this.contextRunner
			.withPropertyValues("spring.boot.admin.notify.webex.auth-token:123456:abtshubzztk-abtabhixta-788654")
			.run((context) -> assertThat(context).hasSingleBean(WebexNotifier.class));
	}

	@Test
	void test_multipleNotifiers() {
		this.contextRunner.withUserConfiguration(TestMultipleNotifierConfig.class).run((context) -> {
			assertThat(context.getBean(Notifier.class)).isInstanceOf(CompositeNotifier.class);
			assertThat(context).getBeans(Notifier.class).hasSize(3);
		});
	}

	@Test
	void test_multipleNotifiersWithPrimary() {
		this.contextRunner.withUserConfiguration(TestMultipleWithPrimaryNotifierConfig.class).run((context) -> {
			assertThat(context.getBean(Notifier.class)).isInstanceOf(TestNotifier.class);
			assertThat(context).getBeans(Notifier.class).hasSize(2);
		});
	}

	@Test
	void test_notifierProxyProperties() {
		this.contextRunner.withPropertyValues("spring.boot.admin.notify.proxy.host")
			.run((context) -> assertThat(context).hasSingleBean(NotifierProxyProperties.class));
	}

	public static class TestSingleNotifierConfig {

		@Bean
		@Qualifier("testNotifier")
		public TestNotifier testNotifier() {
			return new TestNotifier();
		}

	}

	public static class MailSenderConfig {

		@Bean
		public JavaMailSenderImpl mailSender() {
			return new JavaMailSenderImpl();
		}

	}

	public static class TestMultipleNotifierConfig {

		@Bean
		@Qualifier("testNotifier1")
		public TestNotifier testNotifier1() {
			return new TestNotifier();
		}

		@Bean
		@Qualifier("testNotifier2")
		public TestNotifier testNotifier2() {
			return new TestNotifier();
		}

	}

	public static class TestMultipleWithPrimaryNotifierConfig {

		@Bean
		@Primary
		@Qualifier("testNotifier")
		public TestNotifier testNotifierPrimary() {
			return new TestNotifier();
		}

		@Bean
		@Qualifier("testNotifier3")
		public TestNotifier testNotifier2() {
			return new TestNotifier();
		}

	}

}
