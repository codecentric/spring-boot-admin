/*
 * Copyright 2014-2020 the original author or authors.
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

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
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
import de.codecentric.boot.admin.server.notify.SlackNotifier;
import de.codecentric.boot.admin.server.notify.TelegramNotifier;
import de.codecentric.boot.admin.server.notify.filter.FilteringNotifier;
import de.codecentric.boot.admin.server.notify.filter.web.NotificationFilterController;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(NotifierProxyProperties.class)
@AutoConfigureAfter({ MailSenderAutoConfiguration.class })
public class AdminServerNotifierAutoConfiguration {

	private static RestTemplate createNotifierRestTemplate(NotifierProxyProperties proxyProperties) {
		RestTemplate restTemplate = new RestTemplate();
		if (proxyProperties.getHost() != null) {
			HttpClientBuilder builder = HttpClientBuilder.create();
			builder.setProxy(new HttpHost(proxyProperties.getHost(), proxyProperties.getPort()));

			if (proxyProperties.getUsername() != null && proxyProperties.getPassword() != null) {
				CredentialsProvider credsProvider = new BasicCredentialsProvider();
				credsProvider.setCredentials(new AuthScope(proxyProperties.getHost(), proxyProperties.getPort()),
						new UsernamePasswordCredentials(proxyProperties.getUsername(), proxyProperties.getPassword()));
				builder.setDefaultCredentialsProvider(credsProvider);
			}

			restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(builder.build()));
		}
		return restTemplate;
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnBean(Notifier.class)
	@Lazy(false)
	public static class NotifierTriggerConfiguration {

		@Bean(initMethod = "start", destroyMethod = "stop")
		@ConditionalOnMissingBean(NotificationTrigger.class)
		public NotificationTrigger notificationTrigger(Notifier notifier, Publisher<InstanceEvent> events) {
			return new NotificationTrigger(notifier, events);
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnBean(Notifier.class)
	@AutoConfigureBefore({ NotifierTriggerConfiguration.class })
	@Lazy(false)
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

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnSingleCandidate(FilteringNotifier.class)
	@Lazy(false)
	public static class FilteringNotifierWebConfiguration {

		private final FilteringNotifier filteringNotifier;

		public FilteringNotifierWebConfiguration(FilteringNotifier filteringNotifier) {
			this.filteringNotifier = filteringNotifier;
		}

		@Bean
		public NotificationFilterController notificationFilterController() {
			return new NotificationFilterController(this.filteringNotifier);
		}

	}

	@Configuration(proxyBeanMethods = false)
	@AutoConfigureBefore({ NotifierTriggerConfiguration.class, CompositeNotifierConfiguration.class })
	@ConditionalOnBean(MailSender.class)
	@Lazy(false)
	public static class MailNotifierConfiguration {

		private final ApplicationContext applicationContext;

		public MailNotifierConfiguration(ApplicationContext applicationContext) {
			this.applicationContext = applicationContext;
		}

		@Bean
		@ConditionalOnMissingBean
		@ConfigurationProperties("spring.boot.admin.notify.mail")
		public MailNotifier mailNotifier(JavaMailSender mailSender, InstanceRepository repository) {
			return new MailNotifier(mailSender, repository, mailNotifierTemplateEngine());
		}

		@Bean
		public TemplateEngine mailNotifierTemplateEngine() {
			SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
			resolver.setApplicationContext(this.applicationContext);
			resolver.setTemplateMode(TemplateMode.HTML);
			resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());

			SpringTemplateEngine templateEngine = new SpringTemplateEngine();
			templateEngine.addTemplateResolver(resolver);
			return templateEngine;
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnProperty(prefix = "spring.boot.admin.notify.hipchat", name = "url")
	@AutoConfigureBefore({ NotifierTriggerConfiguration.class, CompositeNotifierConfiguration.class })
	@Lazy(false)
	public static class HipchatNotifierConfiguration {

		@Bean
		@ConditionalOnMissingBean
		@ConfigurationProperties("spring.boot.admin.notify.hipchat")
		public HipchatNotifier hipchatNotifier(InstanceRepository repository, NotifierProxyProperties proxyProperties) {
			return new HipchatNotifier(repository, createNotifierRestTemplate(proxyProperties));
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnProperty(prefix = "spring.boot.admin.notify.slack", name = "webhook-url")
	@AutoConfigureBefore({ NotifierTriggerConfiguration.class, CompositeNotifierConfiguration.class })
	@Lazy(false)
	public static class SlackNotifierConfiguration {

		@Bean
		@ConditionalOnMissingBean
		@ConfigurationProperties("spring.boot.admin.notify.slack")
		public SlackNotifier slackNotifier(InstanceRepository repository, NotifierProxyProperties proxyProperties) {
			return new SlackNotifier(repository, createNotifierRestTemplate(proxyProperties));
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnProperty(prefix = "spring.boot.admin.notify.letschat", name = "url")
	@AutoConfigureBefore({ NotifierTriggerConfiguration.class, CompositeNotifierConfiguration.class })
	@Lazy(false)
	public static class LetsChatNotifierConfiguration {

		@Bean
		@ConditionalOnMissingBean
		@ConfigurationProperties("spring.boot.admin.notify.letschat")
		public LetsChatNotifier letsChatNotifier(InstanceRepository repository,
				NotifierProxyProperties proxyProperties) {
			return new LetsChatNotifier(repository, createNotifierRestTemplate(proxyProperties));
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnProperty(prefix = "spring.boot.admin.notify.pagerduty", name = "service-key")
	@AutoConfigureBefore({ NotifierTriggerConfiguration.class, CompositeNotifierConfiguration.class })
	@Lazy(false)
	public static class PagerdutyNotifierConfiguration {

		@Bean
		@ConditionalOnMissingBean
		@ConfigurationProperties("spring.boot.admin.notify.pagerduty")
		public PagerdutyNotifier pagerdutyNotifier(InstanceRepository repository,
				NotifierProxyProperties proxyProperties) {
			return new PagerdutyNotifier(repository, createNotifierRestTemplate(proxyProperties));
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnProperty(prefix = "spring.boot.admin.notify.opsgenie", name = "api-key")
	@AutoConfigureBefore({ NotifierTriggerConfiguration.class, CompositeNotifierConfiguration.class })
	@Lazy(false)
	public static class OpsGenieNotifierConfiguration {

		@Bean
		@ConditionalOnMissingBean
		@ConfigurationProperties("spring.boot.admin.notify.opsgenie")
		public OpsGenieNotifier opsgenieNotifier(InstanceRepository repository,
				NotifierProxyProperties proxyProperties) {
			return new OpsGenieNotifier(repository, createNotifierRestTemplate(proxyProperties));
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnProperty(prefix = "spring.boot.admin.notify.ms-teams", name = "webhook-url")
	@AutoConfigureBefore({ NotifierTriggerConfiguration.class, CompositeNotifierConfiguration.class })
	@Lazy(false)
	public static class MicrosoftTeamsNotifierConfiguration {

		@Bean
		@ConditionalOnMissingBean
		@ConfigurationProperties("spring.boot.admin.notify.ms-teams")
		public MicrosoftTeamsNotifier microsoftTeamsNotifier(InstanceRepository repository,
				NotifierProxyProperties proxyProperties) {
			return new MicrosoftTeamsNotifier(repository, createNotifierRestTemplate(proxyProperties));
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnProperty(prefix = "spring.boot.admin.notify.telegram", name = "auth-token")
	@AutoConfigureBefore({ NotifierTriggerConfiguration.class, CompositeNotifierConfiguration.class })
	@Lazy(false)
	public static class TelegramNotifierConfiguration {

		@Bean
		@ConditionalOnMissingBean
		@ConfigurationProperties("spring.boot.admin.notify.telegram")
		public TelegramNotifier telegramNotifier(InstanceRepository repository,
				NotifierProxyProperties proxyProperties) {
			return new TelegramNotifier(repository, createNotifierRestTemplate(proxyProperties));
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnProperty(prefix = "spring.boot.admin.notify.discord", name = "webhook-url")
	@AutoConfigureBefore({ NotifierTriggerConfiguration.class, CompositeNotifierConfiguration.class })
	@Lazy(false)
	public static class DiscordNotifierConfiguration {

		@Bean
		@ConditionalOnMissingBean
		@ConfigurationProperties("spring.boot.admin.notify.discord")
		public DiscordNotifier discordNotifier(InstanceRepository repository, NotifierProxyProperties proxyProperties) {
			return new DiscordNotifier(repository, createNotifierRestTemplate(proxyProperties));
		}

	}

}
