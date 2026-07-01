/*
 * Copyright 2014-2025 the original author or authors.
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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import de.codecentric.boot.admin.server.domain.values.InstanceId;

/**
 * In-memory {@link HealthGroupsCache} implementation backed by a {@link ConcurrentMap}.
 */
public class InMemoryHealthGroupsCache implements HealthGroupsCache {

	private final ConcurrentMap<InstanceId, List<String>> cache = new ConcurrentHashMap<>();

	@Override
	public void updateGroups(InstanceId instanceId, List<String> groups) {
		if (groups == null || groups.isEmpty()) {
			this.cache.remove(instanceId);
		}
		else {
			this.cache.put(instanceId, List.copyOf(groups));
		}
	}

	@Override
	public List<String> getGroups(InstanceId instanceId) {
		return this.cache.getOrDefault(instanceId, Collections.emptyList());
	}

	@Override
	public void remove(InstanceId instanceId) {
		this.cache.remove(instanceId);
	}

}
