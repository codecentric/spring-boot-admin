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

package de.codecentric.boot.admin.server.config;

import java.util.List;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.hazelcast.autoconfigure.HazelcastAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.eventstore.HazelcastEventStore;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import de.codecentric.boot.admin.server.eventstore.InstanceEventStore;
import de.codecentric.boot.admin.server.notify.HazelcastNotificationTrigger;
import de.codecentric.boot.admin.server.notify.NotificationTrigger;
import de.codecentric.boot.admin.server.notify.Notifier;
import de.codecentric.boot.admin.server.notify.filter.FilteringNotifier;
import de.codecentric.boot.admin.server.notify.filter.NotificationFilter;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(AdminServerMarkerConfiguration.Marker.class)
@ConditionalOnSingleCandidate(HazelcastInstance.class)
@AutoConfigureBefore({ AdminServerAutoConfiguration.class, AdminServerNotifierAutoConfiguration.class })
@AutoConfigureAfter(HazelcastAutoConfiguration.class)
@Lazy(false)
public class AdminServerHazelcastAutoConfiguration {

	public static final String DEFAULT_NAME_EVENT_STORE_MAP = "spring-boot-admin-event-store";

	public static final String DEFAULT_NAME_SENT_NOTIFICATIONS_MAP = "spring-boot-admin-sent-notifications";

	public static final String DEFAULT_NAME_NOTIFICATION_FILTERS_MAP = "spring-boot-admin-notification-filters";

	@Value("${spring.boot.admin.hazelcast.enabled:true}")
	private final boolean hazelcastEnabled = true;

	@Value("${spring.boot.admin.hazelcast.event-store:" + DEFAULT_NAME_EVENT_STORE_MAP + "}")
	private final String nameEventStoreMap = DEFAULT_NAME_EVENT_STORE_MAP;

	@Value("${spring.boot.admin.hazelcast.event-store-enabled:true}")
	private final boolean eventStoreEnabled = true;

	@Value("${spring.boot.admin.hazelcast.sent-notifications:" + DEFAULT_NAME_SENT_NOTIFICATIONS_MAP + "}")
	private final String nameSentNotificationsMap = DEFAULT_NAME_SENT_NOTIFICATIONS_MAP;

	@Value("${spring.boot.admin.hazelcast.sent-notifications-enabled:true}")
	private final boolean sentNotificationsEnabled = true;

	@Value("${spring.boot.admin.hazelcast.notification-filters:" + DEFAULT_NAME_NOTIFICATION_FILTERS_MAP + "}")
	private final String nameNotificationFiltersMap = DEFAULT_NAME_NOTIFICATION_FILTERS_MAP;

	@Value("${spring.boot.admin.hazelcast.notification-filters-enabled:true}")
	private final boolean notificationFiltersEnabled = true;

	@Bean
	@ConditionalOnMissingBean(InstanceEventStore.class)
	public InstanceEventStore eventStore(HazelcastInstance hazelcastInstance,
			AdminServerProperties adminServerProperties) {
		AdminServerProperties.EventStoreProperties eventStoreProperties = adminServerProperties.getEventStore();
		int maxLogSizePerAggregate = eventStoreProperties.getMaxLogSizePerAggregate();
		boolean pruneInfoUpdatedEvents = eventStoreProperties.isPruneInfoUpdatedEvents();
		if (this.hazelcastEnabled && this.eventStoreEnabled) {
			IMap<InstanceId, List<InstanceEvent>> map = hazelcastInstance.getMap(this.nameEventStoreMap);
			return new HazelcastEventStore(maxLogSizePerAggregate, map, pruneInfoUpdatedEvents);
		}
		return new InMemoryEventStore(maxLogSizePerAggregate, pruneInfoUpdatedEvents);
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnBean(Notifier.class)
	public class NotifierTriggerConfiguration {

		@Bean(initMethod = "start", destroyMethod = "stop")
		@ConditionalOnMissingBean(NotificationTrigger.class)
		public NotificationTrigger notificationTrigger(HazelcastInstance hazelcastInstance, Notifier notifier,
				Publisher<InstanceEvent> events) {
			if (!AdminServerHazelcastAutoConfiguration.this.hazelcastEnabled
					|| !AdminServerHazelcastAutoConfiguration.this.sentNotificationsEnabled) {
				return new NotificationTrigger(notifier, events);
			}
			return new HazelcastNotificationTrigger(notifier, events,
					hazelcastInstance.getMap(AdminServerHazelcastAutoConfiguration.this.nameSentNotificationsMap));
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnBean(Notifier.class)
	@AutoConfigureBefore({ AdminServerNotifierAutoConfiguration.NotifierTriggerConfiguration.class })
	public class NotificationFilterConfiguration {

		@Bean
		@ConditionalOnMissingBean(FilteringNotifier.class)
		public FilteringNotifier filteringNotifier(HazelcastInstance hazelcastInstance, Notifier delegate,
				InstanceRepository repository) {
			if (!AdminServerHazelcastAutoConfiguration.this.hazelcastEnabled
					|| !AdminServerHazelcastAutoConfiguration.this.notificationFiltersEnabled) {
				return new FilteringNotifier(delegate, repository);
			}
			IMap<String, NotificationFilter> filtersMap = hazelcastInstance
				.getMap(AdminServerHazelcastAutoConfiguration.this.nameNotificationFiltersMap);
			return new FilteringNotifier(delegate, repository, filtersMap);
		}

	}

}
