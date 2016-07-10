/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.codecentric.boot.admin.config;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import de.codecentric.boot.admin.event.ClientApplicationEvent;
import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.model.StatusInfo;
import de.codecentric.boot.admin.notify.CompositeNotifier;
import de.codecentric.boot.admin.notify.HipchatNotifier;
import de.codecentric.boot.admin.notify.MailNotifier;
import de.codecentric.boot.admin.notify.Notifier;
import de.codecentric.boot.admin.notify.NotifierListener;
import de.codecentric.boot.admin.notify.PagerdutyNotifier;
import de.codecentric.boot.admin.notify.SlackNotifier;

public class NotifierConfigurationTest {
	private static final ClientApplicationEvent APP_DOWN = new ClientApplicationStatusChangedEvent(
			Application.create("App").withId("id-1").withHealthUrl("http://health")
					.withStatusInfo(StatusInfo.ofDown()).build(),
			StatusInfo.ofUp(), StatusInfo.ofDown());

	private AnnotationConfigWebApplicationContext context;

	@After
	public void close() {
		if (this.context != null) {
			this.context.close();
		}
	}

	@Test
	public void test_notifierListener() {
		load(TestSingleNotifierConfig.class);
		context.publishEvent(APP_DOWN);
		assertThat(context.getBean(TestNotifier.class).getEvents(), is(Arrays.asList(APP_DOWN)));
	}

	@Test
	public void test_no_notifierListener() {
		load(null);
		assertThat(context.getBeansOfType(NotifierListener.class).values(), empty());
	}

	@Test
	public void test_mail() {
		load(null, "spring.mail.host:localhost");
		assertThat(context.getBean(MailNotifier.class), is(instanceOf(MailNotifier.class)));
	}

	@Test
	public void test_pagerduty() {
		load(null, "spring.boot.admin.notify.pagerduty.service-key:foo");
		assertThat(context.getBean(PagerdutyNotifier.class),
				is(instanceOf(PagerdutyNotifier.class)));
	}

	@Test
	public void test_hipchat() {
		load(null, "spring.boot.admin.notify.hipchat.url:http://example.com");
		assertThat(context.getBean(HipchatNotifier.class), is(instanceOf(HipchatNotifier.class)));
	}

	@Test
	public void test_slack() {
		load(null, "spring.boot.admin.notify.slack.webhook-url:http://example.com");
		assertThat(context.getBean(SlackNotifier.class), is(instanceOf(SlackNotifier.class)));
	}

	@Test
	public void test_multipleNotifiers() {
		load(TestMultipleNotifierConfig.class);
		assertThat(context.getBean(Notifier.class), is(instanceOf(CompositeNotifier.class)));
		assertThat(context.getBeansOfType(Notifier.class).values(), hasSize(3));
	}

	@Test
	public void test_multipleNotifiersWithPrimary() {
		load(TestMultipleWithPrimaryNotifierConfig.class);
		assertThat(context.getBean(Notifier.class), is(instanceOf(TestNotifier.class)));
		assertThat(context.getBeansOfType(Notifier.class).values(), hasSize(2));
	}

	private void load(Class<?> config, String... environment) {
		context = new AnnotationConfigWebApplicationContext();
		if (config != null) {
			context.register(config);
		}
		context.register(MailSenderAutoConfiguration.class);
		context.register(NotifierConfiguration.class);

		EnvironmentTestUtils.addEnvironment(context, environment);
		context.refresh();
	}

	public static class TestSingleNotifierConfig {
		@Bean
		public Notifier testNotifier() {
			return new TestNotifier();
		}

	}

	private static class TestMultipleNotifierConfig {
		@Bean
		public Notifier testNotifier1() {
			return new TestNotifier();
		}

		@Bean
		public Notifier testNotifier2() {
			return new TestNotifier();
		}
	}

	private static class TestMultipleWithPrimaryNotifierConfig {
		@Bean
		@Primary
		public Notifier testNotifierPrimary() {
			return new TestNotifier();
		}

		@Bean
		public Notifier testNotifier2() {
			return new TestNotifier();
		}
	}

	private static class TestNotifier implements Notifier {
		private List<ClientApplicationEvent> events = new ArrayList<>();

		@Override
		public void notify(ClientApplicationEvent event) {
			this.events.add(event);
		}

		public List<ClientApplicationEvent> getEvents() {
			return events;
		}
	}
}
