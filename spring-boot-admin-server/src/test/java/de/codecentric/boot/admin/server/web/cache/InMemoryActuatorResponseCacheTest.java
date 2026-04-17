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
import java.time.Duration;
import java.util.Optional;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import de.codecentric.boot.admin.server.config.AdminServerProperties.EndpointCacheProperties;
import de.codecentric.boot.admin.server.domain.values.InstanceId;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryActuatorResponseCacheTest {

	private InMemoryActuatorResponseCache cache;

	private EndpointCacheProperties props;

	@BeforeEach
	void setup() {
		this.props = new EndpointCacheProperties();
		this.props.setDefaultTtl(Duration.ofMinutes(5));
		this.cache = new InMemoryActuatorResponseCache(this.props);
	}

	@Test
	void should_return_empty_when_no_entry() {
		Optional<CacheEntry> result = this.cache.get(InstanceId.of("id1"), "mappings", null);
		assertThat(result).isEmpty();
	}

	@Test
	void should_store_and_retrieve_entry() {
		InstanceId id = InstanceId.of("id1");
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		byte[] body = "{\"foo\":\"bar\"}".getBytes(StandardCharsets.UTF_8);
		CacheEntry entry = new CacheEntry(200, headers, body);

		this.cache.put(id, "mappings", null, entry);

		Optional<CacheEntry> result = this.cache.get(id, "mappings", null);
		assertThat(result).isPresent();
		assertThat(result.get().getStatusCode()).isEqualTo(200);
		assertThat(result.get().getBody()).isEqualTo(body);
		assertThat(result.get().getHttpHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
	}

	@Test
	void should_treat_query_string_as_part_of_key() {
		InstanceId id = InstanceId.of("id1");
		CacheEntry entry1 = new CacheEntry(200, new HttpHeaders(), "body1".getBytes(StandardCharsets.UTF_8));
		CacheEntry entry2 = new CacheEntry(200, new HttpHeaders(), "body2".getBytes(StandardCharsets.UTF_8));

		this.cache.put(id, "beans", "foo=bar", entry1);
		this.cache.put(id, "beans", "foo=baz", entry2);

		assertThat(this.cache.get(id, "beans", "foo=bar")).isPresent()
			.get()
			.extracting((e) -> new String(e.getBody(), StandardCharsets.UTF_8))
			.isEqualTo("body1");
		assertThat(this.cache.get(id, "beans", "foo=baz")).isPresent()
			.get()
			.extracting((e) -> new String(e.getBody(), StandardCharsets.UTF_8))
			.isEqualTo("body2");
		assertThat(this.cache.get(id, "beans", null)).isEmpty();
	}

	@Test
	void should_evict_expired_entries_on_read() {
		this.props.setDefaultTtl(Duration.ofMillis(50));
		InstanceId id = InstanceId.of("id1");
		this.cache.put(id, "mappings", null, new CacheEntry(200, new HttpHeaders(), new byte[0]));

		assertThat(this.cache.get(id, "mappings", null)).isPresent();

		Awaitility.await()
			.atMost(Duration.ofSeconds(5))
			.untilAsserted(() -> assertThat(this.cache.get(id, "mappings", null)).isEmpty());
	}

	@Test
	void should_respect_per_endpoint_ttl() {
		this.props.setDefaultTtl(Duration.ofSeconds(60));
		this.props.getTtl().put("mappings", Duration.ofMillis(50));
		InstanceId id = InstanceId.of("id1");
		this.cache.put(id, "mappings", null, new CacheEntry(200, new HttpHeaders(), new byte[0]));
		this.cache.put(id, "beans", null, new CacheEntry(200, new HttpHeaders(), new byte[0]));

		Awaitility.await()
			.atMost(Duration.ofSeconds(5))
			.untilAsserted(() -> assertThat(this.cache.get(id, "mappings", null)).isEmpty());
		assertThat(this.cache.get(id, "beans", null)).isPresent();
	}

	@Test
	void should_invalidate_all_entries_for_instance() {
		InstanceId id1 = InstanceId.of("id1");
		InstanceId id2 = InstanceId.of("id2");
		this.cache.put(id1, "mappings", null, new CacheEntry(200, new HttpHeaders(), new byte[0]));
		this.cache.put(id1, "beans", null, new CacheEntry(200, new HttpHeaders(), new byte[0]));
		this.cache.put(id2, "mappings", null, new CacheEntry(200, new HttpHeaders(), new byte[0]));

		this.cache.invalidateAllForInstance(id1);

		assertThat(this.cache.get(id1, "mappings", null)).isEmpty();
		assertThat(this.cache.get(id1, "beans", null)).isEmpty();
		assertThat(this.cache.get(id2, "mappings", null)).isPresent();
	}

	@Test
	void should_invalidate_single_endpoint_for_instance() {
		InstanceId id1 = InstanceId.of("id1");
		InstanceId id2 = InstanceId.of("id2");
		this.cache.put(id1, "mappings", null, new CacheEntry(200, new HttpHeaders(), new byte[0]));
		this.cache.put(id1, "mappings/sub", null, new CacheEntry(200, new HttpHeaders(), new byte[0]));
		this.cache.put(id1, "mappings", "page=0", new CacheEntry(200, new HttpHeaders(), new byte[0]));
		this.cache.put(id1, "beans", null, new CacheEntry(200, new HttpHeaders(), new byte[0]));
		this.cache.put(id2, "mappings", null, new CacheEntry(200, new HttpHeaders(), new byte[0]));

		this.cache.invalidateEndpointForInstance(id1, "mappings");

		// mappings (exact, sub-path and query variant) for id1 are gone
		assertThat(this.cache.get(id1, "mappings", null)).isEmpty();
		assertThat(this.cache.get(id1, "mappings/sub", null)).isEmpty();
		assertThat(this.cache.get(id1, "mappings", "page=0")).isEmpty();
		// beans for id1 and mappings for id2 are unaffected
		assertThat(this.cache.get(id1, "beans", null)).isPresent();
		assertThat(this.cache.get(id2, "mappings", null)).isPresent();
	}

	@Test
	void should_not_cache_when_disabled() {
		this.props.setEnabled(false);
		InstanceId id = InstanceId.of("id1");
		this.cache.put(id, "mappings", null, new CacheEntry(200, new HttpHeaders(), new byte[0]));

		assertThat(this.cache.get(id, "mappings", null)).isEmpty();
	}

	@Test
	void shouldCache_returns_true_for_configured_get_endpoint() {
		assertThat(this.cache.shouldCache(HttpMethod.GET, "mappings")).isTrue();
		assertThat(this.cache.shouldCache(HttpMethod.GET, "beans")).isTrue();
		assertThat(this.cache.shouldCache(HttpMethod.GET, "configprops")).isTrue();
	}

	@Test
	void shouldCache_returns_false_for_unconfigured_endpoint() {
		assertThat(this.cache.shouldCache(HttpMethod.GET, "health")).isFalse();
		assertThat(this.cache.shouldCache(HttpMethod.GET, "info")).isFalse();
	}

	@Test
	void shouldCache_returns_false_for_non_get_method() {
		assertThat(this.cache.shouldCache(HttpMethod.POST, "mappings")).isFalse();
		assertThat(this.cache.shouldCache(HttpMethod.DELETE, "mappings")).isFalse();
		assertThat(this.cache.shouldCache(HttpMethod.HEAD, "mappings")).isFalse();
	}

	@Test
	void shouldCache_returns_false_when_disabled() {
		this.props.setEnabled(false);
		assertThat(this.cache.shouldCache(HttpMethod.GET, "mappings")).isFalse();
	}

	@Test
	void key_builder_encodes_sub_paths_and_query_string() {
		String key1 = InMemoryActuatorResponseCache.buildKey(InstanceId.of("abc"), "sbom/application", null);
		String key2 = InMemoryActuatorResponseCache.buildKey(InstanceId.of("abc"), "sbom/application", "format=spdx");

		String expectedPrefix = CacheKeyBuilder.instancePrefix(InstanceId.of("abc"));
		assertThat(key1).isEqualTo(expectedPrefix + "sbom/application");
		assertThat(key2).isEqualTo(expectedPrefix + "sbom/application?format=spdx");
	}

}
