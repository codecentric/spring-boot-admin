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

package de.codecentric.boot.admin.server.eventstore;

import java.util.List;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MergePolicyConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.spi.merge.PutIfAbsentMergePolicy;

import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;

import static de.codecentric.boot.admin.server.config.AdminServerHazelcastAutoConfiguration.DEFAULT_NAME_EVENT_STORE_MAP;
import static de.codecentric.boot.admin.server.config.AdminServerHazelcastAutoConfiguration.DEFAULT_NAME_SENT_NOTIFICATIONS_MAP;
import static java.util.Collections.singletonList;

public class HazelcastEventStoreWithServerConfigTest extends AbstractEventStoreTest {

	private final HazelcastInstance hazelcast;

	public HazelcastEventStoreWithServerConfigTest() {
		this.hazelcast = createHazelcastInstance();
	}

	@Override
	protected InstanceEventStore createStore(int maxLogSizePerAggregate) {
		IMap<InstanceId, List<InstanceEvent>> eventLogs = this.hazelcast
			.getMap("testList" + System.currentTimeMillis());
		return new HazelcastEventStore(maxLogSizePerAggregate, eventLogs);
	}

	@Override
	protected void shutdownStore() {
		hazelcast.shutdown();
	}

	private HazelcastInstance createHazelcastInstance() {
		Config config = createHazelcastConfig();
		return Hazelcast.newHazelcastInstance(config);
	}

	// config from sample project
	private Config createHazelcastConfig() {
		// This map is used to store the events.
		// It should be configured to reliably hold all the data,
		// Spring Boot Admin will compact the events, if there are too many
		MapConfig eventStoreMap = new MapConfig(DEFAULT_NAME_EVENT_STORE_MAP).setInMemoryFormat(InMemoryFormat.OBJECT)
			.setBackupCount(1)
			.setMergePolicyConfig(new MergePolicyConfig(PutIfAbsentMergePolicy.class.getName(), 100));

		// This map is used to deduplicate the notifications.
		// If data in this map gets lost it should not be a big issue as it will atmost
		// lead to
		// the same notification to be sent by multiple instances
		MapConfig sentNotificationsMap = new MapConfig(DEFAULT_NAME_SENT_NOTIFICATIONS_MAP)
			.setInMemoryFormat(InMemoryFormat.OBJECT)
			.setBackupCount(1)
			.setEvictionConfig(new EvictionConfig().setEvictionPolicy(EvictionPolicy.LRU))
			.setMergePolicyConfig(new MergePolicyConfig(PutIfAbsentMergePolicy.class.getName(), 100));

		Config config = new Config();
		config.addMapConfig(eventStoreMap);
		config.addMapConfig(sentNotificationsMap);
		config.setProperty("hazelcast.jmx", "true");

		// WARNING: This setups a local cluster, you change it to fit your needs.
		config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
		TcpIpConfig tcpIpConfig = config.getNetworkConfig().getJoin().getTcpIpConfig();
		tcpIpConfig.setEnabled(true);
		tcpIpConfig.setMembers(singletonList("127.0.0.1"));
		return config;
	}

}
