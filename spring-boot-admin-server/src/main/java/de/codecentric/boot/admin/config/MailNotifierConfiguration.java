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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSender;

import de.codecentric.boot.admin.notify.MailNotifier;

@Configuration
@ConditionalOnBean(MailSender.class)
@AutoConfigureAfter({ MailSenderAutoConfiguration.class })
public class MailNotifierConfiguration {

	@Autowired
	private MailSender mailSender;

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(prefix = "spring.boot.admin.notify.mail", name = "enabled", matchIfMissing = true)
	@ConfigurationProperties("spring.boot.admin.notify.mail")
	public MailNotifier mailNotifier() {
		return new MailNotifier(mailSender);
	}
}