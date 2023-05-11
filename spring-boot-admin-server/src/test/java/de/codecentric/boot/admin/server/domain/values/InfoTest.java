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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.assertj.core.data.MapEntry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InfoTest {

	@Test
	public void should_keep_order() {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("z", "1");
		map.put("x", "2");

		Iterator<Map.Entry<String, Object>> iterator = Info.from(map).getValues().entrySet().iterator();

		assertThat(iterator.next()).isEqualTo(MapEntry.entry("z", "1"));
		assertThat(iterator.next()).isEqualTo(MapEntry.entry("x", "2"));
	}

}
