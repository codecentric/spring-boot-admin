/*
 * Copyright 2014-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.server.config;

import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import de.codecentric.boot.admin.server.eventstore.InstanceEventStore;
import de.codecentric.boot.admin.server.notify.CompositeNotifier;
import de.codecentric.boot.admin.server.notify.HipchatNotifier;
import de.codecentric.boot.admin.server.notify.LetsChatNotifier;
import de.codecentric.boot.admin.server.notify.MailNotifier;
import de.codecentric.boot.admin.server.notify.MicrosoftTeamsNotifier;
import de.codecentric.boot.admin.server.notify.NotificationTrigger;
import de.codecentric.boot.admin.server.notify.Notifier;
import de.codecentric.boot.admin.server.notify.OpsGenieNotifier;
import de.codecentric.boot.admin.server.notify.PagerdutyNotifier;
import de.codecentric.boot.admin.server.notify.SlackNotifier;
import de.codecentric.boot.admin.server.notify.TelegramNotifier;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;
import reactor.test.StepVerifier;

import java.util.Collections;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import static org.assertj.core.api.Assertions.assertThat;

public class AdminServerNotifierAutoConfigurationTest {
    private static final InstanceEvent APP_DOWN = new InstanceStatusChangedEvent(InstanceId.of("id-2"), 0L,
        StatusInfo.ofDown());

    private AnnotationConfigApplicationContext context;

    @After
    public void close() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    public void test_notifierListener() {
        load(TestSingleNotifierConfig.class);
        InstanceEventStore store = context.getBean(InstanceEventStore.class);

        StepVerifier.create(context.getBean(TestNotifier.class).getFlux())
                    .expectSubscription()
                    .then(() -> StepVerifier.create(store.append(Collections.singletonList(APP_DOWN))).verifyComplete())
                    .expectNext(APP_DOWN)
                    .thenCancel()
                    .verify();
    }

    @Test
    public void test_no_notifierListener() {
        load(null);
        assertThat(context.getBeansOfType(NotificationTrigger.class)).isEmpty();
    }

    @Test
    public void test_mail() {
        load(MailSenderConfig.class);
        assertThat(context.getBean(MailNotifier.class)).isInstanceOf(MailNotifier.class);
    }

    @Test
    public void test_hipchat() {
        load(null, "spring.boot.admin.notify.hipchat.url:http://example.com");
        assertThat(context.getBean(HipchatNotifier.class)).isInstanceOf(HipchatNotifier.class);
    }

    @Test
    public void test_letschat() {
        load(null, "spring.boot.admin.notify.letschat.url:http://example.com");
        assertThat(context.getBean(LetsChatNotifier.class)).isInstanceOf(LetsChatNotifier.class);
    }

    @Test
    public void test_slack() {
        load(null, "spring.boot.admin.notify.slack.webhook-url:http://example.com");
        assertThat(context.getBean(SlackNotifier.class)).isInstanceOf(SlackNotifier.class);
    }

    @Test
    public void test_pagerduty() {
        load(null, "spring.boot.admin.notify.pagerduty.service-key:foo");
        assertThat(context.getBean(PagerdutyNotifier.class)).isInstanceOf(PagerdutyNotifier.class);
    }

    @Test
    public void test_opsgenie() {
        load(null, "spring.boot.admin.notify.opsgenie.api-key:foo");
        assertThat(context.getBean(OpsGenieNotifier.class)).isInstanceOf(OpsGenieNotifier.class);
    }

    @Test
    public void test_ms_teams() {
        load(null, "spring.boot.admin.notify.ms-teams.webhook-url:http://example.com");
        assertThat(context.getBean(MicrosoftTeamsNotifier.class)).isInstanceOf(MicrosoftTeamsNotifier.class);
    }

    @Test
    public void test_telegram() {
        load(null, "spring.boot.admin.notify.telegram.auth-token:123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11");
        assertThat(context.getBean(Notifier.class)).isInstanceOf(TelegramNotifier.class);
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
        context = new AnnotationConfigApplicationContext();
        if (config != null) {
            context.register(config);
        }
        context.register(RestTemplateAutoConfiguration.class);
        context.register(AdminServerMarkerConfiguration.class);
        context.register(AdminServerAutoConfiguration.class);
        context.register(AdminServerNotifierAutoConfiguration.class);

        TestPropertyValues.of(environment).applyTo(context);
        context.refresh();
    }

    public static class TestSingleNotifierConfig {
        @Bean
        @Qualifier("testNotifier")
        public TestNotifier testNotifier() {
            return new TestNotifier();
        }
    }

    private static class MailSenderConfig {
        @Bean
        public JavaMailSenderImpl mailSender() {
            return new JavaMailSenderImpl();
        }
    }

    private static class TestMultipleNotifierConfig {
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

    private static class TestMultipleWithPrimaryNotifierConfig {
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

    private static class TestNotifier implements Notifier {
        private final Flux<InstanceEvent> publishedFlux;
        private final FluxSink<InstanceEvent> sink;

        private TestNotifier() {
            UnicastProcessor<InstanceEvent> unicastProcessor = UnicastProcessor.create();
            this.publishedFlux = unicastProcessor;
            this.sink = unicastProcessor.sink();
        }

        @Override
        public Mono<Void> notify(InstanceEvent event) {
            this.sink.next(event);
            return Mono.empty();
        }

        public Flux<InstanceEvent> getFlux() {
            return publishedFlux;
        }
    }
}
