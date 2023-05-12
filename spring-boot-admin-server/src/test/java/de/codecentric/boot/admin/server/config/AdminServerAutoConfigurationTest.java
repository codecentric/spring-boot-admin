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

package de.codecentric.boot.admin.server.config;

import com.hazelcast.config.Config;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.function.client.ClientHttpConnectorAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.entities.SnapshottingInstanceRepository;
import de.codecentric.boot.admin.server.eventstore.ConcurrentMapEventStore;
import de.codecentric.boot.admin.server.eventstore.HazelcastEventStore;
import de.codecentric.boot.admin.server.eventstore.InstanceEventStore;
import de.codecentric.boot.admin.server.notify.HazelcastNotificationTrigger;
import de.codecentric.boot.admin.server.notify.MailNotifier;
import de.codecentric.boot.admin.server.notify.NotificationTrigger;
import de.codecentric.boot.admin.server.notify.Notifier;

import static org.assertj.core.api.Assertions.assertThat;

public class AdminServerAutoConfigurationTest {

	private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(RestTemplateAutoConfiguration.class,
				ClientHttpConnectorAutoConfiguration.class, WebClientAutoConfiguration.class,
				HazelcastAutoConfiguration.class, WebMvcAutoConfiguration.class,
				AdminServerHazelcastAutoConfiguration.class, AdminServerAutoConfiguration.class))
		.withUserConfiguration(AdminServerMarkerConfiguration.class);

	@Test
	public void simpleConfig() {
		this.contextRunner.run((context) -> {
			assertThat(context).getBean(InstanceRepository.class).isInstanceOf(SnapshottingInstanceRepository.class);
			assertThat(context).doesNotHaveBean(MailNotifier.class);
			assertThat(context).getBean(InstanceEventStore.class).isInstanceOf(ConcurrentMapEventStore.class);
		});
	}

	@Test
	public void hazelcastConfig() {
		this.contextRunner.withUserConfiguration(TestHazelcastConfig.class).run((context) -> {
			assertThat(context).getBean(InstanceEventStore.class).isInstanceOf(HazelcastEventStore.class);
			assertThat(context).getBean(NotificationTrigger.class).isInstanceOf(HazelcastNotificationTrigger.class);
		});
	}

	public static class TestHazelcastConfig {

		@Bean
		public Config config() {
			return new Config();
		}

		@Bean
		public Notifier notifier() {
			return (e) -> Mono.empty();
		}

	}

}
