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

package de.codecentric.boot.admin.server.services;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.codecentric.boot.admin.server.domain.values.InstanceId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HealthGroupsCacheTest {

	private HealthGroupsCache cache;

	private InstanceId instanceId;

	@BeforeEach
	void setUp() {
		this.cache = new InMemoryHealthGroupsCache();
		this.instanceId = InstanceId.of("test-instance");
	}

	@Test
	void updateAndGetGroups() {
		List<String> groups = List.of("liveness", "readiness");
		this.cache.updateGroups(this.instanceId, groups);
		assertThat(this.cache.getGroups(this.instanceId)).containsExactly("liveness", "readiness");
	}

	@Test
	void getGroupsReturnsEmptyListForUnknownInstance() {
		assertThat(this.cache.getGroups(InstanceId.of("unknown"))).isEmpty();
	}

	@Test
	void updateGroupsWithNullRemovesEntry() {
		this.cache.updateGroups(this.instanceId, List.of("liveness", "readiness"));
		this.cache.updateGroups(this.instanceId, null);
		assertThat(this.cache.getGroups(this.instanceId)).isEmpty();
	}

	@Test
	void updateGroupsWithEmptyListRemovesEntry() {
		this.cache.updateGroups(this.instanceId, List.of("liveness", "readiness"));
		this.cache.updateGroups(this.instanceId, List.of());
		assertThat(this.cache.getGroups(this.instanceId)).isEmpty();
	}

	@Test
	void removeGroups() {
		this.cache.updateGroups(this.instanceId, List.of("liveness", "readiness"));
		this.cache.remove(this.instanceId);
		assertThat(this.cache.getGroups(this.instanceId)).isEmpty();
	}

	@Test
	void returnedListIsUnmodifiable() {
		this.cache.updateGroups(this.instanceId, List.of("liveness", "readiness"));
		List<String> groups = this.cache.getGroups(this.instanceId);
		assertThatThrownBy(() -> groups.add("test")).isInstanceOf(UnsupportedOperationException.class);
	}

}
