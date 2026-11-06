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

import java.util.concurrent.ConcurrentMap;

import com.hazelcast.config.Config;
import com.hazelcast.map.IMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.hazelcast.autoconfigure.HazelcastAutoConfiguration;
import org.springframework.boot.http.client.autoconfigure.reactive.ReactiveHttpClientAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.webclient.autoconfigure.WebClientAutoConfiguration;
import org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.notify.HazelcastNotificationTrigger;
import de.codecentric.boot.admin.server.notify.NotificationTrigger;
import de.codecentric.boot.admin.server.notify.Notifier;
import de.codecentric.boot.admin.server.notify.filter.FilteringNotifier;
import de.codecentric.boot.admin.server.notify.filter.NotificationFilter;

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

	@Test
	void should_start_and_use_hazelcast_trigger_with_single_user_defined_notifier() {
		this.contextRunner.withUserConfiguration(UserNotifierConfig.class).run((context) -> {
			assertThat(context).hasNotFailed();
			assertThat(context).hasSingleBean(NotificationTrigger.class);
			assertThat(context).getBean(NotificationTrigger.class).isInstanceOf(HazelcastNotificationTrigger.class);
			assertThat(context).doesNotHaveBean(FilteringNotifier.class);
		});
	}

	@Test
	void should_share_filters_map_with_user_defined_filtering_notifier() {
		this.contextRunner.withUserConfiguration(UserFilteringNotifierConfig.class).run((context) -> {
			assertThat(context).hasNotFailed();
			assertThat(context).getBean(NotificationTrigger.class).isInstanceOf(HazelcastNotificationTrigger.class);
			assertThat(context.getBean(FilteringNotifier.class)).extracting("filters").isInstanceOf(IMap.class);
		});
	}

	@Test
	void should_start_with_primary_filtering_notifier_wrapping_property_configured_notifier() {
		this.contextRunner.withUserConfiguration(PrimaryFilteringNotifierConfig.class)
			.withPropertyValues("spring.boot.admin.notify.slack.webhook-url:https://example.com")
			.run((context) -> {
				assertThat(context).hasNotFailed();
				assertThat(context).hasSingleBean(FilteringNotifier.class);
				assertThat(context).getBean(NotificationTrigger.class).isInstanceOf(HazelcastNotificationTrigger.class);
			});
	}

	public static class HazelcastOnlyConfig {

		@Bean
		public Config config() {
			return new Config();
		}

	}

	public static class UserNotifierConfig {

		@Bean
		public Notifier notifier() {
			return (event) -> Mono.empty();
		}

	}

	public static class UserFilteringNotifierConfig {

		@Bean
		public Notifier delegateNotifier() {
			return (event) -> Mono.empty();
		}

		@Bean
		@Primary
		public FilteringNotifier filteringNotifier(@Qualifier("delegateNotifier") Notifier delegate,
				InstanceRepository repository,
				@Qualifier("notificationFiltersMap") ConcurrentMap<String, NotificationFilter> filters) {
			return new FilteringNotifier(delegate, repository, filters);
		}

	}

	public static class PrimaryFilteringNotifierConfig {

		@Bean
		@Primary
		public FilteringNotifier filteringNotifier(@Qualifier("slackNotifier") Notifier delegate,
				InstanceRepository repository,
				@Qualifier("notificationFiltersMap") ConcurrentMap<String, NotificationFilter> filters) {
			return new FilteringNotifier(delegate, repository, filters);
		}

	}

}
