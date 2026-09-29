/*
 * Copyright 2014-2026 the original author or authors.
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

import com.hazelcast.config.Config;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.hazelcast.autoconfigure.HazelcastAutoConfiguration;
import org.springframework.boot.http.client.autoconfigure.reactive.ReactiveHttpClientAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.webclient.autoconfigure.WebClientAutoConfiguration;
import org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;

import de.codecentric.boot.admin.server.notify.HazelcastNotificationTrigger;
import de.codecentric.boot.admin.server.notify.NotificationTrigger;

import static org.assertj.core.api.Assertions.assertThat;

class AdminServerHazelcastAutoConfigurationTest {

	private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(ReactiveHttpClientAutoConfiguration.class,
				WebClientAutoConfiguration.class, HazelcastAutoConfiguration.class, WebMvcAutoConfiguration.class,
				AdminServerHazelcastAutoConfiguration.class, AdminServerAutoConfiguration.class,
				AdminServerNotifierAutoConfiguration.class))
		.withUserConfiguration(AdminServerMarkerConfiguration.class, HazelcastOnlyConfig.class);

	@Test
	void should_use_hazelcast_trigger_for_property_configured_notifier() {
		this.contextRunner.withPropertyValues("spring.boot.admin.notify.slack.webhook-url:https://example.com")
			.run((context) -> {
				assertThat(context).hasSingleBean(NotificationTrigger.class);
				assertThat(context).getBean(NotificationTrigger.class).isInstanceOf(HazelcastNotificationTrigger.class);
			});
	}

	@Test
	void should_use_hazelcast_trigger_for_multiple_property_configured_notifiers() {
		this.contextRunner
			.withPropertyValues("spring.boot.admin.notify.slack.webhook-url:https://example.com",
					"spring.boot.admin.notify.discord.webhook-url:https://example.com")
			.run((context) -> {
				assertThat(context).hasSingleBean(NotificationTrigger.class);
				assertThat(context).getBean(NotificationTrigger.class).isInstanceOf(HazelcastNotificationTrigger.class);
			});
	}

	@Test
	void should_use_plain_trigger_when_hazelcast_is_disabled() {
		this.contextRunner
			.withPropertyValues("spring.boot.admin.hazelcast.enabled:false",
					"spring.boot.admin.notify.slack.webhook-url:https://example.com")
			.run((context) -> {
				assertThat(context).hasSingleBean(NotificationTrigger.class);
				assertThat(context).getBean(NotificationTrigger.class)
					.isNotInstanceOf(HazelcastNotificationTrigger.class);
			});
	}

	public static class HazelcastOnlyConfig {

		@Bean
		public Config config() {
			return new Config();
		}

	}

}
