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

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.hazelcast.autoconfigure.HazelcastAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.eventstore.HazelcastEventStore;
import de.codecentric.boot.admin.server.eventstore.InstanceEventStore;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(AdminServerMarkerConfiguration.Marker.class)
@ConditionalOnSingleCandidate(HazelcastInstance.class)
@ConditionalOnProperty(prefix = "spring.boot.admin.hazelcast", name = "enabled", matchIfMissing = true)
@AutoConfigureBefore({ AdminServerAutoConfiguration.class, AdminServerNotifierAutoConfiguration.class })
@AutoConfigureAfter(HazelcastAutoConfiguration.class)
@Lazy(false)
public class AdminServerHazelcastAutoConfiguration {

	public static final String DEFAULT_NAME_EVENT_STORE_MAP = "spring-boot-admin-event-store";

	public static final String DEFAULT_NAME_SENT_NOTIFICATIONS_MAP = "spring-boot-admin-sent-notifications";

	public static final String SENT_NOTIFICATIONS_BEAN_NAME = "sentNotificationsMap";

	@Value("${spring.boot.admin.hazelcast.event-store:" + DEFAULT_NAME_EVENT_STORE_MAP + "}")
	private final String nameEventStoreMap = DEFAULT_NAME_EVENT_STORE_MAP;

	@Bean
	@ConditionalOnMissingBean(InstanceEventStore.class)
	public HazelcastEventStore eventStore(HazelcastInstance hazelcastInstance) {
		IMap<InstanceId, List<InstanceEvent>> map = hazelcastInstance.getMap(this.nameEventStoreMap);
		return new HazelcastEventStore(map);
	}

	/**
	 * Distributed map used to deduplicate notifications across the cluster. It is picked
	 * up by {@link AdminServerNotifierAutoConfiguration}, which is evaluated after all
	 * notifiers have been registered.
	 * @param hazelcastInstance the Hazelcast instance
	 * @param nameSentNotificationsMap the name of the backing map
	 * @return the map holding the version of the last event a notification was sent for
	 */
	@Bean(name = SENT_NOTIFICATIONS_BEAN_NAME)
	@Lazy
	@ConditionalOnMissingBean(name = SENT_NOTIFICATIONS_BEAN_NAME)
	public IMap<InstanceId, Long> sentNotificationsMap(HazelcastInstance hazelcastInstance,
			@Value("${spring.boot.admin.hazelcast.sent-notifications:" + DEFAULT_NAME_SENT_NOTIFICATIONS_MAP
					+ "}") String nameSentNotificationsMap) {
		return hazelcastInstance.getMap(nameSentNotificationsMap);
	}

}
