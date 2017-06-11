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
package de.codecentric.boot.admin.server.config;

import de.codecentric.boot.admin.server.event.ClientApplicationEvent;
import de.codecentric.boot.admin.server.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.server.model.Application;
import de.codecentric.boot.admin.server.model.ApplicationId;
import de.codecentric.boot.admin.server.model.StatusInfo;
import de.codecentric.boot.admin.server.notify.CompositeNotifier;
import de.codecentric.boot.admin.server.notify.HipchatNotifier;
import de.codecentric.boot.admin.server.notify.MailNotifier;
import de.codecentric.boot.admin.server.notify.Notifier;
import de.codecentric.boot.admin.server.notify.NotifierListener;
import de.codecentric.boot.admin.server.notify.SlackNotifier;

import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Test;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

public class NotifierConfigurationTest {
    private static final ClientApplicationEvent APP_DOWN = new ClientApplicationStatusChangedEvent(
            Application.create("App").withId(ApplicationId.of("id-2"))
                       .withHealthUrl("http://health")
                       .withStatusInfo(StatusInfo.ofDown())
                       .build(), StatusInfo.ofUp(), StatusInfo.ofDown());

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
        assertThat(context.getBean(TestNotifier.class).getEvents()).containsOnly(APP_DOWN);
    }

    @Test
    public void test_no_notifierListener() {
        load(null);
        assertThat(context.getBeansOfType(NotifierListener.class)).isEmpty();
    }

    @Test
    public void test_mail() {
        load(null, "spring.mail.host:localhost");
        assertThat(context.getBean(MailNotifier.class)).isInstanceOf(MailNotifier.class);
    }

    @Test
    public void test_hipchat() {
        load(null, "spring.boot.admin.notify.hipchat.url:http://example.com");
        assertThat(context.getBean(HipchatNotifier.class)).isInstanceOf(HipchatNotifier.class);
    }

    @Test
    public void test_slack() {
        load(null, "spring.boot.admin.notify.slack.webhook-url:http://example.com");
        assertThat(context.getBean(SlackNotifier.class)).isInstanceOf(SlackNotifier.class);
    }

    @Test
    public void test_multipleNotifiers() {
        load(TestMultipleNotifierConfig.class);
        assertThat(context.getBean(Notifier.class)).isInstanceOf(CompositeNotifier.class);
        assertThat(context.getBeansOfType(Notifier.class)).hasSize(3);
    }

    @Test
    public void test_multipleNotifiersWithPrimary() {
        load(TestMultipleWithPrimaryNotifierConfig.class);
        assertThat(context.getBean(Notifier.class)).isInstanceOf(TestNotifier.class);
        assertThat(context.getBeansOfType(Notifier.class)).hasSize(2);
    }

    private void load(Class<?> config, String... environment) {
        context = new AnnotationConfigWebApplicationContext();
        if (config != null) {
            context.register(config);
        }
        context.register(MailSenderAutoConfiguration.class);
        context.register(NotifierConfiguration.class);

        TestPropertyValues.of(environment).applyTo(context);
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
