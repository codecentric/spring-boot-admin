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

import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpMethod;

import de.codecentric.boot.admin.server.domain.values.InstanceId;

/**
 * Cache for proxied actuator endpoint responses. Implementations must be thread-safe.
 *
 * <p>
 * The cache is keyed by instance id, endpoint path (including sub-paths), and query
 * string so that different sub-resources of the same endpoint are cached independently.
 *
 * <p>
 * Two implementations are provided out of the box:
 * <ul>
 * <li>{@link InMemoryActuatorResponseCache} – local, per-node cache (default)</li>
 * <li>{@code HazelcastActuatorResponseCache} – distributed cache shared across SBA
 * cluster nodes (activated automatically when Hazelcast is on the classpath)</li>
 * </ul>
 */
public interface ActuatorResponseCache {

	/**
	 * Returns the cached entry for the given key, or {@link Optional#empty()} if the
	 * entry does not exist or has expired.
	 * @param instanceId the registered instance
	 * @param endpointPath path relative to {@code /actuator/} (e.g. {@code "mappings"} or
	 * {@code "sbom/application"})
	 * @param queryString raw query string from the original request, may be {@code null}
	 * @return the cached entry, or empty if not found or expired
	 */
	Optional<CacheEntry> get(InstanceId instanceId, String endpointPath, @Nullable String queryString);

	/**
	 * Stores an entry.
	 * @param instanceId the registered instance
	 * @param endpointPath path relative to {@code /actuator/}
	 * @param queryString raw query string, may be {@code null}
	 * @param entry the entry to store
	 */
	void put(InstanceId instanceId, String endpointPath, @Nullable String queryString, CacheEntry entry);

	/**
	 * Invalidates all cached entries for the given instance (e.g. when the instance
	 * deregisters or its registration is updated).
	 * @param instanceId the registered instance
	 */
	void invalidateAllForInstance(InstanceId instanceId);

	/**
	 * Invalidates all cached entries for the given instance and endpoint. Called after a
	 * successful mutating request (POST/PUT/PATCH/DELETE) so that the next GET returns
	 * fresh data.
	 * <p>
	 * The default implementation falls back to {@link #invalidateAllForInstance}, which
	 * is always safe but overly broad. Implementations are encouraged to override this
	 * with a targeted eviction.
	 * @param instanceId the registered instance
	 * @param endpointId first path segment of the actuator path (e.g. {@code "loggers"})
	 */
	default void invalidateEndpointForInstance(InstanceId instanceId, String endpointId) {
		invalidateAllForInstance(instanceId);
	}

	/**
	 * Returns {@code true} if a response for the given HTTP method and endpoint id should
	 * be looked up from / stored in the cache. Implementations use this to enforce the
	 * configured endpoint inclusion list and restrict caching to safe HTTP methods (GET).
	 * @param method the HTTP method of the incoming proxy request
	 * @param endpointId first path segment of the actuator path (e.g. {@code "mappings"})
	 * @return {@code true} if the request should use the cache
	 */
	boolean shouldCache(HttpMethod method, String endpointId);

	/**
	 * Returns the maximum response body size in bytes that will be stored. Responses
	 * larger than this threshold are forwarded as-is without caching.
	 * @return maximum cacheable payload size in bytes
	 */
	long getMaxPayloadSize();

}
