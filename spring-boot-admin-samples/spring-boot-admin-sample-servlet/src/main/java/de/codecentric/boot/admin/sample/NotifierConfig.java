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

package de.codecentric.boot.admin.sample;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.notify.CompositeNotifier;
import de.codecentric.boot.admin.server.notify.Notifier;
import de.codecentric.boot.admin.server.notify.RemindingNotifier;
import de.codecentric.boot.admin.server.notify.filter.FilteringNotifier;

// tag::configuration-filtering-notifier[]
@Configuration(proxyBeanMethods = false)
public class NotifierConfig {

	private final InstanceRepository repository;

	private final ObjectProvider<List<Notifier>> otherNotifiers;

	public NotifierConfig(InstanceRepository repository, ObjectProvider<List<Notifier>> otherNotifiers) {
		this.repository = repository;
		this.otherNotifiers = otherNotifiers;
	}

	@Bean
	public FilteringNotifier filteringNotifier() { // <1>
		CompositeNotifier delegate = new CompositeNotifier(this.otherNotifiers.getIfAvailable(Collections::emptyList));
		return new FilteringNotifier(delegate, this.repository);
	}

	@Primary
	@Bean(initMethod = "start", destroyMethod = "stop")
	public RemindingNotifier remindingNotifier() { // <2>
		RemindingNotifier notifier = new RemindingNotifier(filteringNotifier(), this.repository);
		notifier.setReminderPeriod(Duration.ofMinutes(10));
		notifier.setCheckReminderInverval(Duration.ofSeconds(10));
		return notifier;
	}

}
// end::configuration-filtering-notifier[]
