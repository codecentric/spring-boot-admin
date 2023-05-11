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

package de.codecentric.boot.admin.server.domain.values;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import static de.codecentric.boot.admin.server.domain.values.StatusInfo.STATUS_DOWN;
import static de.codecentric.boot.admin.server.domain.values.StatusInfo.STATUS_OFFLINE;
import static de.codecentric.boot.admin.server.domain.values.StatusInfo.STATUS_OUT_OF_SERVICE;
import static de.codecentric.boot.admin.server.domain.values.StatusInfo.STATUS_RESTRICTED;
import static de.codecentric.boot.admin.server.domain.values.StatusInfo.STATUS_UNKNOWN;
import static de.codecentric.boot.admin.server.domain.values.StatusInfo.STATUS_UP;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class StatusInfoTest {

	@Test
	public void invariants() {
		assertThatThrownBy(() -> StatusInfo.valueOf("")).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("'status' must not be empty.");
	}

	@Test
	public void test_isMethods() {
		assertThat(StatusInfo.valueOf("FOO").isUp()).isFalse();
		assertThat(StatusInfo.valueOf("FOO").isDown()).isFalse();
		assertThat(StatusInfo.valueOf("FOO").isUnknown()).isFalse();
		assertThat(StatusInfo.valueOf("FOO").isOffline()).isFalse();

		assertThat(StatusInfo.ofUp().isUp()).isTrue();
		assertThat(StatusInfo.ofUp().isDown()).isFalse();
		assertThat(StatusInfo.ofUp().isUnknown()).isFalse();
		assertThat(StatusInfo.ofUp().isOffline()).isFalse();

		assertThat(StatusInfo.ofDown().isUp()).isFalse();
		assertThat(StatusInfo.ofDown().isDown()).isTrue();
		assertThat(StatusInfo.ofDown().isUnknown()).isFalse();
		assertThat(StatusInfo.ofDown().isOffline()).isFalse();

		assertThat(StatusInfo.ofUnknown().isUp()).isFalse();
		assertThat(StatusInfo.ofUnknown().isDown()).isFalse();
		assertThat(StatusInfo.ofUnknown().isUnknown()).isTrue();
		assertThat(StatusInfo.ofUnknown().isOffline()).isFalse();

		assertThat(StatusInfo.ofOffline().isUp()).isFalse();
		assertThat(StatusInfo.ofOffline().isDown()).isFalse();
		assertThat(StatusInfo.ofOffline().isUnknown()).isFalse();
		assertThat(StatusInfo.ofOffline().isOffline()).isTrue();
	}

	@Test
	public void from_map_should_return_same_result() {
		Map<String, Object> map = new HashMap<>();
		map.put("status", "UP");
		map.put("details", singletonMap("foo", "bar"));

		assertThat(StatusInfo.from(map)).isEqualTo(StatusInfo.ofUp(singletonMap("foo", "bar")));
	}

	@Test
	public void when_first_level_key_is_components() {
		Map<String, Object> map = new HashMap<>();
		map.put("status", "UP");
		map.put("components", singletonMap("foo", "bar"));

		assertThat(StatusInfo.from(map)).isEqualTo(StatusInfo.ofUp(singletonMap("foo", "bar")));
	}

	@Test
	public void should_sort_by_status_order() {
		List<String> unordered = asList(STATUS_OUT_OF_SERVICE, STATUS_UNKNOWN, STATUS_OFFLINE, STATUS_DOWN, STATUS_UP,
				STATUS_RESTRICTED);

		List<String> ordered = unordered.stream().sorted(StatusInfo.severity()).collect(Collectors.toList());
		assertThat(ordered).containsExactly(STATUS_DOWN, STATUS_OUT_OF_SERVICE, STATUS_OFFLINE, STATUS_UNKNOWN,
				STATUS_RESTRICTED, STATUS_UP);
	}

}
