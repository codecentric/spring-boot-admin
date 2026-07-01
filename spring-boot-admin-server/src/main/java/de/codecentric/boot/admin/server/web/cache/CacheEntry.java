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

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;

/**
 * Immutable snapshot of a proxied actuator endpoint response that can be stored in a
 * cache and replayed to clients without hitting the monitored application again.
 *
 * <p>
 * The class is {@link Serializable} so that it can be stored in a distributed cache (e.g.
 * Hazelcast) without needing a custom serializer.
 */
public final class CacheEntry implements Serializable {

	private static final long serialVersionUID = 1L;

	private final int statusCode;

	/**
	 * Response headers stored as a plain, serializable map. Security-sensitive headers
	 * are stripped before the entry is created (see {@code InstanceWebProxy}).
	 */
	private final Map<String, List<String>> headers;

	private final byte[] body;

	private final Instant cachedAt;

	/**
	 * Creates a new entry with the current timestamp.
	 * @param statusCode the HTTP status code of the response
	 * @param headers filtered response headers
	 * @param body response body bytes
	 */
	public CacheEntry(int statusCode, HttpHeaders headers, byte[] body) {
		this.statusCode = statusCode;
		this.headers = toSerializableMap(headers);
		this.body = body.clone();
		this.cachedAt = Instant.now();
	}

	public int getStatusCode() {
		return this.statusCode;
	}

	/**
	 * Reconstructs an {@link HttpHeaders} instance from the stored map.
	 * @return reconstructed {@link HttpHeaders}
	 */
	public HttpHeaders getHttpHeaders() {
		HttpHeaders httpHeaders = new HttpHeaders();
		this.headers.forEach((name, values) -> httpHeaders.put(name, new ArrayList<>(values)));
		return httpHeaders;
	}

	/**
	 * Returns a defensive copy of the cached body bytes. Callers that only need a
	 * read-only view of the body should prefer {@link #getBodyRef()} to avoid an
	 * unnecessary array copy.
	 * @return defensive copy of the body bytes
	 */
	public byte[] getBody() {
		return this.body.clone();
	}

	/**
	 * Returns a direct reference to the internal body byte array. The caller <strong>must
	 * not</strong> modify the returned array; doing so would corrupt the cached entry.
	 * @return the internal body byte array (read-only)
	 */
	public byte[] getBodyRef() {
		return this.body;
	}

	/**
	 * Returns the number of bytes in the cached body without copying.
	 * @return number of body bytes
	 */
	public int bodyLength() {
		return this.body.length;
	}

	public Instant getCachedAt() {
		return this.cachedAt;
	}

	private static Map<String, List<String>> toSerializableMap(HttpHeaders headers) {
		Map<String, List<String>> map = new LinkedHashMap<>(headers.size());
		headers.forEach((name, values) -> map.put(name, new ArrayList<>(values)));
		return map;
	}

}
