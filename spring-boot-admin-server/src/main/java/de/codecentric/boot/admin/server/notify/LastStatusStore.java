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

package de.codecentric.boot.admin.server.notify;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

import de.codecentric.boot.admin.server.domain.values.InstanceId;

/**
 * Holds the last known status of instances for a single notifier. The backing map can be
 * shared between the members of a cluster, in which case the namespace keeps the state of
 * different notifiers apart.
 *
 * @author YadavKshitiz
 * @since 4.1.4
 */
public class LastStatusStore {

	private final ConcurrentMap<String, String> statuses;

	private final String namespace;

	public LastStatusStore() {
		this(new ConcurrentHashMap<>(), "");
	}

	public LastStatusStore(ConcurrentMap<String, String> statuses, String namespace) {
		Assert.notNull(statuses, "'statuses' must not be null");
		Assert.notNull(namespace, "'namespace' must not be null");
		this.statuses = statuses;
		this.namespace = namespace;
	}

	@Nullable public String get(InstanceId id) {
		return this.statuses.get(key(id));
	}

	public void put(InstanceId id, String status) {
		this.statuses.put(key(id), status);
	}

	public void remove(InstanceId id) {
		this.statuses.remove(key(id));
	}

	private String key(InstanceId id) {
		return this.namespace + ":" + id.getValue();
	}

}
