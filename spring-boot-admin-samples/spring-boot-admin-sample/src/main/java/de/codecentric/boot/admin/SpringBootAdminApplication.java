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
package de.codecentric.boot.admin;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Scheduled;

import de.codecentric.boot.admin.config.EnableAdminServer;
import de.codecentric.boot.admin.notify.LoggingNotifier;
import de.codecentric.boot.admin.notify.Notifier;
import de.codecentric.boot.admin.notify.RemindingNotifier;
import de.codecentric.boot.admin.notify.filter.FilteringNotifier;

@Configuration
@EnableAutoConfiguration
@EnableAdminServer
public class SpringBootAdminApplication {
	public static void main(String[] args) {
		SpringApplication.run(SpringBootAdminApplication.class, args);
	}

	@Bean
	@Primary
	public RemindingNotifier remindingNotifier() {
		RemindingNotifier notifier = new RemindingNotifier(filteringNotifier(loggerNotifier()));
		notifier.setReminderPeriod(TimeUnit.SECONDS.toMillis(10));
		return notifier;
	}

	@Scheduled(fixedRate = 1_000L)
	public void remind() {
		remindingNotifier().sendReminders();
	}

	@Bean
	public FilteringNotifier filteringNotifier(Notifier delegate) {
		return new FilteringNotifier(delegate);
	}

	@Bean
	public LoggingNotifier loggerNotifier() {
		return new LoggingNotifier();
	}
}
