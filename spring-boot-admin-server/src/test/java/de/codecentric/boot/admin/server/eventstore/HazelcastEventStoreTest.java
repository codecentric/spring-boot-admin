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

import java.time.Duration;
import java.util.List;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegisteredEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;

import static java.util.Collections.singletonList;

public class HazelcastEventStoreTest extends AbstractEventStoreTest {

	HazelcastInstance hazelcast;

	@Override
	protected InstanceEventStore createStore(int maxLogSizePerAggregate) {
		Config config = new Config();
		config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
		config.getNetworkConfig().getJoin().getAutoDetectionConfig().setEnabled(false);
		hazelcast = Hazelcast.newHazelcastInstance(config);
		return new HazelcastEventStore(maxLogSizePerAggregate,
				hazelcast.getMap("testList" + System.currentTimeMillis()));
	}

	@Override
	protected void shutdownStore() {
		if (this.hazelcast != null) {
			this.hazelcast.shutdown();
		}
	}

	@Test
	public void should_publish_events_for_added_entries() {
		Config config = new Config();
		config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
		config.getNetworkConfig().getJoin().getAutoDetectionConfig().setEnabled(false);
		HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
		try {
			InstanceId id = InstanceId.of("id");
			Registration registration = Registration.create("foo", "https://health").build();
			InstanceEvent event = new InstanceRegisteredEvent(id, 0L, registration);
			IMap<InstanceId, List<InstanceEvent>> eventLog = hazelcastInstance
				.getMap("testList" + System.currentTimeMillis());
			InstanceEventStore store = new HazelcastEventStore(100, eventLog);

			StepVerifier.create(store)
				.expectSubscription()
				.then(() -> eventLog.put(id, singletonList(event)))
				.expectNext(event)
				.thenCancel()
				.verify(Duration.ofSeconds(10));
		}
		finally {
			hazelcastInstance.shutdown();
		}
	}

}
