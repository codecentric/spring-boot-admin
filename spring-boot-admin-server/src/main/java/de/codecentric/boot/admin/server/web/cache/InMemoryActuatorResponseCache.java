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

package de.codecentric.boot.admin.server.web.cache;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

import de.codecentric.boot.admin.server.config.AdminServerProperties.EndpointCacheProperties;
import de.codecentric.boot.admin.server.domain.values.InstanceId;

/**
 * In-memory implementation of {@link ActuatorResponseCache} backed by a
 * {@link ConcurrentHashMap}.
 *
 * <p>
 * TTL is enforced lazily: expired entries are detected and discarded at read time. No
 * background eviction thread is used to keep the implementation simple. For long-running
 * deployments with many instances, consider using the Hazelcast-backed implementation
 * which performs native TTL eviction.
 */
public class InMemoryActuatorResponseCache implements ActuatorResponseCache {

	private static final Logger log = LoggerFactory.getLogger(InMemoryActuatorResponseCache.class);

	private final ConcurrentMap<String, CacheEntry> store = new ConcurrentHashMap<>();

	private final EndpointCacheProperties properties;

	public InMemoryActuatorResponseCache(EndpointCacheProperties properties) {
		this.properties = properties;
	}

	@Override
	public Optional<CacheEntry> get(InstanceId instanceId, String endpointPath, @Nullable String queryString) {
		if (!this.properties.isEnabled()) {
			return Optional.empty();
		}
		String key = buildKey(instanceId, endpointPath, queryString);
		CacheEntry entry = this.store.get(key);
		if (entry == null) {
			return Optional.empty();
		}
		Instant expiresAt = entry.getCachedAt().plus(getTtl(extractEndpointId(endpointPath)));
		if (Instant.now().isAfter(expiresAt)) {
			this.store.remove(key, entry);
			log.trace("Evicted expired cache entry for key '{}'", key);
			return Optional.empty();
		}
		return Optional.of(entry);
	}

	@Override
	public void put(InstanceId instanceId, String endpointPath, @Nullable String queryString, CacheEntry entry) {
		if (!this.properties.isEnabled()) {
			return;
		}
		String key = buildKey(instanceId, endpointPath, queryString);
		this.store.put(key, entry);
		log.trace("Cached entry for key '{}'", key);
	}

	@Override
	public void invalidateAllForInstance(InstanceId instanceId) {
		String prefix = CacheKeyBuilder.instancePrefix(instanceId);
		boolean removed = this.store.keySet().removeIf((key) -> key.startsWith(prefix));
		if (removed) {
			log.debug("Invalidated cache entries for instance {}", instanceId);
		}
	}

	@Override
	public void invalidateEndpointForInstance(InstanceId instanceId, String endpointId) {
		String baseKey = CacheKeyBuilder.buildKey(instanceId, endpointId, null);
		String baseKeyWithSlash = baseKey + "/";
		String baseKeyWithQuery = baseKey + "?";
		boolean removed = this.store.keySet()
			.removeIf((key) -> key.equals(baseKey) || key.startsWith(baseKeyWithSlash)
					|| key.startsWith(baseKeyWithQuery));
		if (removed) {
			log.debug("Invalidated cache entries for instance {} endpoint '{}'", instanceId, endpointId);
		}
	}

	@Override
	public boolean shouldCache(HttpMethod method, String endpointId) {
		return this.properties.isEnabled() && HttpMethod.GET.equals(method)
				&& this.properties.getEndpoints().contains(endpointId);
	}

	@Override
	public long getMaxPayloadSize() {
		return this.properties.getMaxPayloadSize();
	}

	private java.time.Duration getTtl(String endpointId) {
		return this.properties.getTtl().getOrDefault(endpointId, this.properties.getDefaultTtl());
	}

	static String buildKey(InstanceId instanceId, String endpointPath, @Nullable String queryString) {
		return CacheKeyBuilder.buildKey(instanceId, endpointPath, queryString);
	}

	static String extractEndpointId(String endpointPath) {
		int slash = endpointPath.indexOf('/');
		return (slash > 0) ? endpointPath.substring(0, slash) : endpointPath;
	}

}
