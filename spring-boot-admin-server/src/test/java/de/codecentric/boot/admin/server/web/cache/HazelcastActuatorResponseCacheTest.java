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

import java.time.Duration;
import java.util.Optional;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import de.codecentric.boot.admin.server.config.AdminServerProperties.EndpointCacheProperties;
import de.codecentric.boot.admin.server.domain.values.InstanceId;

import static org.assertj.core.api.Assertions.assertThat;

class HazelcastActuatorResponseCacheTest {

	private HazelcastInstance hazelcast;

	private HazelcastActuatorResponseCache cache;

	private EndpointCacheProperties props;

	@BeforeEach
	void setUp() {
		Config config = new Config();
		config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
		config.getNetworkConfig().getJoin().getAutoDetectionConfig().setEnabled(false);
		this.hazelcast = Hazelcast.newHazelcastInstance(config);
		IMap<String, CacheEntry> map = this.hazelcast.getMap("test-response-cache-" + System.currentTimeMillis());
		this.props = new EndpointCacheProperties();
		this.props.setDefaultTtl(Duration.ofMinutes(5));
		this.cache = new HazelcastActuatorResponseCache(map, this.props);
	}

	@AfterEach
	void tearDown() {
		if (this.hazelcast != null) {
			this.hazelcast.shutdown();
		}
	}

	@Test
	void should_return_empty_when_no_entry() {
		Optional<CacheEntry> result = this.cache.get(InstanceId.of("id1"), "mappings", null);
		assertThat(result).isEmpty();
	}

	@Test
	void should_store_and_retrieve_entry() {
		InstanceId id = InstanceId.of("id1");
		CacheEntry entry = new CacheEntry(200, new HttpHeaders(), new byte[] { 1, 2, 3 });

		this.cache.put(id, "mappings", null, entry);

		Optional<CacheEntry> result = this.cache.get(id, "mappings", null);
		assertThat(result).isPresent();
		assertThat(result.get().getStatusCode()).isEqualTo(200);
		assertThat(result.get().getBody()).isEqualTo(new byte[] { 1, 2, 3 });
	}

	@Test
	void should_treat_query_string_as_part_of_key() {
		InstanceId id = InstanceId.of("id1");
		CacheEntry entryA = new CacheEntry(200, new HttpHeaders(), new byte[] { 1 });
		CacheEntry entryB = new CacheEntry(200, new HttpHeaders(), new byte[] { 2 });

		this.cache.put(id, "beans", "foo=bar", entryA);
		this.cache.put(id, "beans", "foo=baz", entryB);

		assertThat(this.cache.get(id, "beans", "foo=bar")).isPresent();
		assertThat(this.cache.get(id, "beans", "foo=baz")).isPresent();
		assertThat(this.cache.get(id, "beans", null)).isEmpty();
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
	void should_invalidate_endpoint_exact_sub_path_and_query_variants() {
		InstanceId id1 = InstanceId.of("id1");
		InstanceId id2 = InstanceId.of("id2");
		this.cache.put(id1, "mappings", null, new CacheEntry(200, new HttpHeaders(), new byte[0]));
		this.cache.put(id1, "mappings/sub", null, new CacheEntry(200, new HttpHeaders(), new byte[0]));
		this.cache.put(id1, "mappings", "page=0", new CacheEntry(200, new HttpHeaders(), new byte[0]));
		this.cache.put(id1, "beans", null, new CacheEntry(200, new HttpHeaders(), new byte[0]));
		this.cache.put(id2, "mappings", null, new CacheEntry(200, new HttpHeaders(), new byte[0]));

		this.cache.invalidateEndpointForInstance(id1, "mappings");

		// all mappings variants for id1 are evicted
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
	void should_evict_entry_after_ttl() {
		// Use a short but Hazelcast-safe TTL: server-side TTL eviction may fire
		// before the first read, so we only assert eventual emptiness.
		this.props.setDefaultTtl(Duration.ofMillis(500));
		InstanceId id = InstanceId.of("id1");
		this.cache.put(id, "mappings", null, new CacheEntry(200, new HttpHeaders(), new byte[0]));

		Awaitility.await()
			.atMost(Duration.ofSeconds(10))
			.untilAsserted(() -> assertThat(this.cache.get(id, "mappings", null)).isEmpty());
	}

	@Test
	void should_respect_per_endpoint_ttl() {
		this.props.setDefaultTtl(Duration.ofSeconds(60));
		this.props.getTtl().put("mappings", Duration.ofMillis(500));
		InstanceId id = InstanceId.of("id1");
		this.cache.put(id, "mappings", null, new CacheEntry(200, new HttpHeaders(), new byte[0]));
		this.cache.put(id, "beans", null, new CacheEntry(200, new HttpHeaders(), new byte[0]));

		Awaitility.await()
			.atMost(Duration.ofSeconds(10))
			.untilAsserted(() -> assertThat(this.cache.get(id, "mappings", null)).isEmpty());
		assertThat(this.cache.get(id, "beans", null)).isPresent();
	}

	@Test
	void shouldCache_returns_true_for_configured_get_endpoint() {
		assertThat(this.cache.shouldCache(HttpMethod.GET, "mappings")).isTrue();
		assertThat(this.cache.shouldCache(HttpMethod.GET, "beans")).isTrue();
	}

	@Test
	void shouldCache_returns_false_for_unconfigured_endpoint() {
		assertThat(this.cache.shouldCache(HttpMethod.GET, "health")).isFalse();
	}

	@Test
	void shouldCache_returns_false_for_non_get_method() {
		assertThat(this.cache.shouldCache(HttpMethod.POST, "mappings")).isFalse();
	}

	@Test
	void shouldCache_returns_false_when_disabled() {
		this.props.setEnabled(false);
		assertThat(this.cache.shouldCache(HttpMethod.GET, "mappings")).isFalse();
	}

}
