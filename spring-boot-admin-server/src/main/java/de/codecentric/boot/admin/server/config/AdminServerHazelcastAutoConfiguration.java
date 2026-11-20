/*
 * Copyright 2014-2026 the original author or authors.
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.jspecify.annotations.Nullable;
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
import org.springframework.core.env.Environment;

import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.eventstore.HazelcastEventStore;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import de.codecentric.boot.admin.server.eventstore.InstanceEventStore;
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

	public static final String SENT_NOTIFICATIONS_BEAN_NAME = "sentNotificationsMap";

	public static final String NOTIFICATION_FILTERS_BEAN_NAME = "notificationFiltersMap";

	@Value("${spring.boot.admin.hazelcast.event-store:" + DEFAULT_NAME_EVENT_STORE_MAP + "}")
	private final String nameEventStoreMap = DEFAULT_NAME_EVENT_STORE_MAP;

	@Bean
	@ConditionalOnMissingBean(InstanceEventStore.class)
	public InstanceEventStore eventStore(HazelcastInstance hazelcastInstance,
			AdminServerProperties adminServerProperties, Environment environment) {
		AdminServerProperties.EventStoreProperties eventStoreProperties = adminServerProperties.getEventStore();
		int maxLogSizePerAggregate = eventStoreProperties.getMaxLogSizePerAggregate();
		boolean pruneInfoUpdatedEvents = eventStoreProperties.isPruneInfoUpdatedEvents();
		if (isEnabled(environment, "event-store-enabled")) {
			IMap<InstanceId, List<InstanceEvent>> map = hazelcastInstance.getMap(this.nameEventStoreMap);
			return new HazelcastEventStore(maxLogSizePerAggregate, map, pruneInfoUpdatedEvents);
		}
		return new InMemoryEventStore(maxLogSizePerAggregate, pruneInfoUpdatedEvents);
	}

	/**
	 * Distributed map used to deduplicate notifications across the cluster. It is exposed
	 * as a bean because {@link AdminServerNotifierAutoConfiguration} must not reference
	 * Hazelcast classes, as Hazelcast is optional. It obtains the map as a plain
	 * {@code ConcurrentMap} through an {@code ObjectProvider}, which is resolved lazily
	 * when the notification trigger is created. The result therefore does not depend on
	 * the order in which the auto-configurations are evaluated.
	 * <p>
	 * If disabled with
	 * {@code spring.boot.admin.hazelcast.sent-notifications-enabled=false}, this bean is
	 * {@code null} and a plain notification trigger is created, so every node sends its
	 * own notifications.
	 * @param hazelcastInstance the Hazelcast instance
	 * @param nameSentNotificationsMap the name of the backing map
	 * @param environment the environment
	 * @return the map holding the version of the last event a notification was sent for,
	 * or {@code null} if disabled
	 */
	@Bean(name = SENT_NOTIFICATIONS_BEAN_NAME)
	@Lazy
	@ConditionalOnMissingBean(name = SENT_NOTIFICATIONS_BEAN_NAME)
	@Nullable public IMap<InstanceId, Long> sentNotificationsMap(
			HazelcastInstance hazelcastInstance, @Value("${spring.boot.admin.hazelcast.sent-notifications:"
					+ DEFAULT_NAME_SENT_NOTIFICATIONS_MAP + "}") String nameSentNotificationsMap,
			Environment environment) {
		if (!isEnabled(environment, "sent-notifications-enabled")) {
			return null;
		}
		return hazelcastInstance.getMap(nameSentNotificationsMap);
	}

	/**
	 * Distributed map holding the notification filters, so that filters are shared across
	 * the cluster. Pass it to a {@link FilteringNotifier} declared in your application.
	 * <p>
	 * If disabled with
	 * {@code spring.boot.admin.hazelcast.notification-filters-enabled=false}, a local map
	 * is returned, so the declaration of the {@link FilteringNotifier} does not need to
	 * change, but filters are not shared.
	 * @param hazelcastInstance the Hazelcast instance
	 * @param nameNotificationFiltersMap the name of the backing map
	 * @param environment the environment
	 * @return the map holding the notification filters
	 */
	@Bean(name = NOTIFICATION_FILTERS_BEAN_NAME)
	@Lazy
	@ConditionalOnMissingBean(name = NOTIFICATION_FILTERS_BEAN_NAME)
	public ConcurrentMap<String, NotificationFilter> notificationFiltersMap(
			HazelcastInstance hazelcastInstance, @Value("${spring.boot.admin.hazelcast.notification-filters:"
					+ DEFAULT_NAME_NOTIFICATION_FILTERS_MAP + "}") String nameNotificationFiltersMap,
			Environment environment) {
		if (!isEnabled(environment, "notification-filters-enabled")) {
			return new ConcurrentHashMap<>();
		}
		return hazelcastInstance.getMap(nameNotificationFiltersMap);
	}

	/**
	 * Whether the given Hazelcast backed feature is enabled. This is decided at runtime
	 * and not with {@code @ConditionalOnProperty}, because conditions are evaluated at
	 * build time when AOT is used, and the properties would be ignored at runtime.
	 * @param environment the environment
	 * @param feature the name of the {@code spring.boot.admin.hazelcast.*} property
	 * @return {@code true} if Hazelcast and the feature are enabled
	 */
	private static boolean isEnabled(Environment environment, String feature) {
		return environment.getProperty("spring.boot.admin.hazelcast.enabled", Boolean.class, true)
				&& environment.getProperty("spring.boot.admin.hazelcast." + feature, Boolean.class, true);
	}

}
