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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.jspecify.annotations.Nullable;

import de.codecentric.boot.admin.server.domain.values.InstanceId;

/**
 * Builds unambiguous cache keys for actuator response cache entries.
 *
 * <p>
 * The instance id portion of the key is encoded as a SHA-256 hex digest so that instance
 * ids containing delimiter characters (e.g. the {@code applicationId:instanceId} format
 * produced by
 * {@link de.codecentric.boot.admin.server.services.CloudFoundryInstanceIdGenerator})
 * never collide with each other or with the endpoint path segment of the key.
 *
 * <p>
 * Key format: {@code SHA256_HEX(instanceId) + ":" + endpointPath [+ "?" + queryString]}
 */
final class CacheKeyBuilder {

	private CacheKeyBuilder() {
	}

	/**
	 * Returns the cache key for the given instance, endpoint path, and optional query
	 * string.
	 * @param instanceId the registered instance
	 * @param endpointPath path relative to {@code /actuator/}
	 * @param queryString raw query string, may be {@code null}
	 * @return the cache key
	 */
	static String buildKey(InstanceId instanceId, String endpointPath, @Nullable String queryString) {
		return instancePrefix(instanceId) + endpointPath + ((queryString != null) ? "?" + queryString : "");
	}

	/**
	 * Returns the instance-scoped key prefix used for invalidating all entries for a
	 * given instance. Format: {@code SHA256_HEX(instanceId) + ":"}
	 * @param instanceId the registered instance
	 * @return the key prefix for this instance
	 */
	static String instancePrefix(InstanceId instanceId) {
		return sha256Hex(instanceId.getValue()) + ":";
	}

	private static String sha256Hex(String value) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hash = md.digest(value.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hash);
		}
		catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 algorithm not available", ex);
		}
	}

}
