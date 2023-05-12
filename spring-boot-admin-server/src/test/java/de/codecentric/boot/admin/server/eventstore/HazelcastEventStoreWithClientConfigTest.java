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

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.junit.jupiter.api.Tag;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;

@Testcontainers(disabledWithoutDocker = true)
@Tag("docker")
public class HazelcastEventStoreWithClientConfigTest extends AbstractEventStoreTest {

	@Container
	private static final GenericContainer<?> hazelcastServer = new GenericContainer<>("hazelcast/hazelcast:4.2.2")
		.withExposedPorts(5701);

	private final HazelcastInstance hazelcast;

	public HazelcastEventStoreWithClientConfigTest() {
		this.hazelcast = createHazelcastInstance();
	}

	@Override
	protected InstanceEventStore createStore(int maxLogSizePerAggregate) {
		IMap<InstanceId, List<InstanceEvent>> eventLog = this.hazelcast.getMap("testList" + System.currentTimeMillis());
		return new HazelcastEventStore(maxLogSizePerAggregate, eventLog);
	}

	@Override
	protected void shutdownStore() {
		this.hazelcast.shutdown();
	}

	private HazelcastInstance createHazelcastInstance() {
		String address = hazelcastServer.getContainerIpAddress() + ":" + hazelcastServer.getMappedPort(5701);

		ClientConfig clientConfig = new ClientConfig();
		clientConfig.getNetworkConfig().addAddress(address);

		return HazelcastClient.newHazelcastClient(clientConfig);
	}

}
