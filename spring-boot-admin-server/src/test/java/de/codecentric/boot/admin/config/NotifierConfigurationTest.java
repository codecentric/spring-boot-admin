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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import de.codecentric.boot.admin.event.ClientApplicationEvent;
import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.model.StatusInfo;
import de.codecentric.boot.admin.notify.Notifier;

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
		load(TestNotifierConfig.class);
		context.publishEvent(APP_DOWN);
		assertThat(context.getBean(TestNotifier.class).getEvents(), is(Arrays.asList(APP_DOWN)));
	}

	@Test
	public void test_no_notifierListener() {
		load(null);
		assertThat(context.getBeansOfType(Notifier.class).values(), empty());
	}

	private void load(Class<?> config) {
		context = new AnnotationConfigWebApplicationContext();
		if (config != null) {
			context.register(config);
		}
		context.register(NotifierConfiguration.class);
		context.refresh();
	}

	public static class TestNotifierConfig {
		@Bean
		public Notifier testNotifier() {
			return new TestNotifier();
		}

	}

	private static class TestNotifier implements Notifier {
		private List<ClientApplicationEvent> events = new ArrayList<ClientApplicationEvent>();

		@Override
		public void notify(ClientApplicationEvent event) {
			this.events.add(event);
		}

		public List<ClientApplicationEvent> getEvents() {
			return events;
		}
	}
}
