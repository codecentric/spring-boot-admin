/*
 * Copyright 2013-2014 the original author or authors.
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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

import de.codecentric.boot.admin.notify.CompositeNotifier;
import de.codecentric.boot.admin.notify.HipchatNotifier;
import de.codecentric.boot.admin.notify.MailNotifier;
import de.codecentric.boot.admin.notify.Notifier;
import de.codecentric.boot.admin.notify.NotifierListener;
import de.codecentric.boot.admin.notify.PagerdutyNotifier;
import de.codecentric.boot.admin.notify.SlackNotifier;
import de.codecentric.boot.admin.notify.LetsChatNotifier;
import de.codecentric.boot.admin.notify.filter.FilteringNotifier;
import de.codecentric.boot.admin.notify.filter.web.NotificationFilterController;
import de.codecentric.boot.admin.web.PrefixHandlerMapping;

@Configuration
public class NotifierConfiguration {

	@Configuration
	@ConditionalOnBean(Notifier.class)
	public static class NotifierListenerConfiguration {
		@Autowired
		private Notifier notifier;

		@Bean
		@ConditionalOnMissingBean
		public NotifierListener notifierListener() {
			return new NotifierListener(notifier);
		}
	}

	@Configuration
	@ConditionalOnBean(Notifier.class)
	@AutoConfigureBefore({ NotifierListenerConfiguration.class })
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

		@Autowired
		private FilteringNotifier filteringNotifier;

		@Autowired
		private AdminServerProperties adminServerProperties;

		@Bean
		public NotificationFilterController notificationFilterController() {
			return new NotificationFilterController(filteringNotifier);
		}

		@Bean
		public PrefixHandlerMapping prefixHandlerMappingNotificationFilterController() {
			PrefixHandlerMapping prefixHandlerMapping = new PrefixHandlerMapping(notificationFilterController());
			prefixHandlerMapping.setPrefix(adminServerProperties.getContextPath());
			return prefixHandlerMapping;
		}
	}

	@Configuration
	@ConditionalOnBean(MailSender.class)
	@AutoConfigureAfter({ MailSenderAutoConfiguration.class })
	@AutoConfigureBefore({ NotifierListenerConfiguration.class,
		CompositeNotifierConfiguration.class })
	public static class MailNotifierConfiguration {
		@Autowired
		private MailSender mailSender;

		@Bean
		@ConditionalOnMissingBean
		@ConfigurationProperties("spring.boot.admin.notify.mail")
		public MailNotifier mailNotifier() {
			return new MailNotifier(mailSender);
		}
	}

	@Configuration
	@ConditionalOnProperty(prefix = "spring.boot.admin.notify.pagerduty", name = "service-key")
	@AutoConfigureBefore({ NotifierListenerConfiguration.class,
		CompositeNotifierConfiguration.class })
	public static class PagerdutyNotifierConfiguration {
		@Bean
		@ConditionalOnMissingBean
		@ConfigurationProperties("spring.boot.admin.notify.pagerduty")
		public PagerdutyNotifier pagerdutyNotifier() {
			return new PagerdutyNotifier();
		}
	}

	@Configuration
	@ConditionalOnProperty(prefix = "spring.boot.admin.notify.hipchat", name = "url")
	@AutoConfigureBefore({ NotifierListenerConfiguration.class,
		CompositeNotifierConfiguration.class })
	public static class HipchatNotifierConfiguration {
		@Bean
		@ConditionalOnMissingBean
		@ConfigurationProperties("spring.boot.admin.notify.hipchat")
		public HipchatNotifier hipchatNotifier() {
			return new HipchatNotifier();
		}
	}

	@Configuration
	@ConditionalOnProperty(prefix = "spring.boot.admin.notify.slack", name = "webhook-url")
	@AutoConfigureBefore({ NotifierListenerConfiguration.class,
		CompositeNotifierConfiguration.class })
	public static class SlackNotifierConfiguration {
		@Bean
		@ConditionalOnMissingBean
		@ConfigurationProperties("spring.boot.admin.notify.slack")
		public SlackNotifier slackNotifier() {
			return new SlackNotifier();
		}
	}

	@Configuration
	@ConditionalOnProperty(prefix = "spring.boot.admin.notify.letschat", name = "url")
	@AutoConfigureBefore({ NotifierListenerConfiguration.class,
			CompositeNotifierConfiguration.class })
	public static class LetsChatNotifierConfiguration {
		@Bean
		@ConditionalOnMissingBean
		@ConfigurationProperties("spring.boot.admin.notify.letschat")
		public LetsChatNotifier letsChatNotifier() {
			return new LetsChatNotifier();
		}
	}
}
