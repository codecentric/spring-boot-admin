/*
 * Copyright 2014-2017 the original author or authors.
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

import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.notify.CompositeNotifier;
import de.codecentric.boot.admin.server.notify.HipchatNotifier;
import de.codecentric.boot.admin.server.notify.LetsChatNotifier;
import de.codecentric.boot.admin.server.notify.MailNotifier;
import de.codecentric.boot.admin.server.notify.NotificationTrigger;
import de.codecentric.boot.admin.server.notify.Notifier;
import de.codecentric.boot.admin.server.notify.OpsGenieNotifier;
import de.codecentric.boot.admin.server.notify.SlackNotifier;
import de.codecentric.boot.admin.server.notify.filter.FilteringNotifier;
import de.codecentric.boot.admin.server.notify.filter.web.NotificationFilterController;
import de.codecentric.boot.admin.server.web.PrefixHandlerMapping;

import java.util.List;
import org.reactivestreams.Publisher;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.condition.NoneNestedConditions;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.MailSender;

@Configuration
public class AdminServerNotifierConfiguration {

    @Configuration
    @ConditionalOnBean(Notifier.class)
    public static class NotifierTriggerConfiguration {
        @Bean(initMethod = "start", destroyMethod = "stop")
        @ConditionalOnMissingBean
        public NotificationTrigger notificationTrigger(Notifier notifier, Publisher<InstanceEvent> events) {
            return new NotificationTrigger(notifier, events);
        }
    }

    @Configuration
    @ConditionalOnBean(Notifier.class)
    @AutoConfigureBefore({NotifierTriggerConfiguration.class})
    public static class CompositeNotifierConfiguration {
        @Bean
        @Primary
        @Conditional(NoSingleNotifierCandidateCondition.class)
        public CompositeNotifier compositeNotifier(List<Notifier> notifiers) {
            return new CompositeNotifier(notifiers);
        }

        static class NoSingleNotifierCandidateCondition extends NoneNestedConditions {
            NoSingleNotifierCandidateCondition() {
                super(ConfigurationPhase.REGISTER_BEAN);
            }

            @ConditionalOnSingleCandidate(Notifier.class)
            static class HasSingleNotifierInstance {
            }
        }
    }

    @Configuration
    @ConditionalOnSingleCandidate(FilteringNotifier.class)
    public static class FilteringNotifierWebConfiguration {
        private final FilteringNotifier filteringNotifier;
        private final AdminServerProperties adminServer;

        public FilteringNotifierWebConfiguration(FilteringNotifier filteringNotifier,
                                                 AdminServerProperties adminServer) {
            this.filteringNotifier = filteringNotifier;
            this.adminServer = adminServer;
        }

        @Bean
        public NotificationFilterController notificationFilterController() {
            return new NotificationFilterController(filteringNotifier);
        }

        @Bean
        public PrefixHandlerMapping prefixHandlerMappingNotificationFilterController() {
            PrefixHandlerMapping prefixHandlerMapping = new PrefixHandlerMapping(notificationFilterController());
            prefixHandlerMapping.setPrefix(adminServer.getContextPath());
            return prefixHandlerMapping;
        }
    }

    @Configuration
    @ConditionalOnBean(MailSender.class)
    @AutoConfigureAfter({MailSenderAutoConfiguration.class})
    @AutoConfigureBefore({NotifierTriggerConfiguration.class, CompositeNotifierConfiguration.class})
    public static class MailNotifierConfiguration {
        @Bean
        @ConditionalOnMissingBean
        @ConfigurationProperties("spring.boot.admin.notify.mail")
        public MailNotifier mailNotifier(MailSender mailSender, InstanceRepository repository) {
            return new MailNotifier(mailSender, repository);
        }
    }

    @Configuration
    @ConditionalOnProperty(prefix = "spring.boot.admin.notify.hipchat", name = "url")
    @AutoConfigureBefore({NotifierTriggerConfiguration.class, CompositeNotifierConfiguration.class})
    public static class HipchatNotifierConfiguration {
        @Bean
        @ConditionalOnMissingBean
        @ConfigurationProperties("spring.boot.admin.notify.hipchat")
        public HipchatNotifier hipchatNotifier(InstanceRepository repository) {
            return new HipchatNotifier(repository);
        }
    }

    @Configuration
    @ConditionalOnProperty(prefix = "spring.boot.admin.notify.slack", name = "webhook-url")
    @AutoConfigureBefore({NotifierTriggerConfiguration.class, CompositeNotifierConfiguration.class})
    public static class SlackNotifierConfiguration {
        @Bean
        @ConditionalOnMissingBean
        @ConfigurationProperties("spring.boot.admin.notify.slack")
        public SlackNotifier slackNotifier(InstanceRepository repository) {
            return new SlackNotifier(repository);
        }
    }

    @Configuration
    @ConditionalOnProperty(prefix = "spring.boot.admin.notify.letschat", name = "url")
    @AutoConfigureBefore({NotifierTriggerConfiguration.class, CompositeNotifierConfiguration.class})
    public static class LetsChatNotifierConfiguration {
        @Bean
        @ConditionalOnMissingBean
        @ConfigurationProperties("spring.boot.admin.notify.letschat")
        public LetsChatNotifier letsChatNotifier(InstanceRepository repository) {
            return new LetsChatNotifier(repository);
        }
    }

    @Configuration
    @ConditionalOnProperty(prefix = "spring.boot.admin.notify.opsgenie", name = "api-key")
    @AutoConfigureBefore({NotifierTriggerConfiguration.class, CompositeNotifierConfiguration.class})
    public static class OpsGenieNotifierConfiguration {
        @Bean
        @ConditionalOnMissingBean
        @ConfigurationProperties("spring.boot.admin.notify.opsgenie")
        public OpsGenieNotifier opsgenieNotifier(InstanceRepository repository) {
            return new OpsGenieNotifier(repository);
        }
    }
}
