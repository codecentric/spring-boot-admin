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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class TagsTest {

	@Test
	public void should_return_empty_from_factory_method() {
		assertThat(Tags.empty().getValues()).isEmpty();
		assertThat(Tags.from(Collections.emptyMap())).isSameAs(Tags.empty());
	}

	@Test
	public void should_return_tags_from_flat_map() {
		Map<String, String> flatTags = new LinkedHashMap<>();
		flatTags.put("tags.env", "test");
		flatTags.put("tags.foo", "bar");
		flatTags.put("ignore", "ignored");
		flatTags.put("tagsi", "ignored");

		assertThat(Tags.from(flatTags, "tags").getValues()).containsExactly(entry("env", "test"), entry("foo", "bar"));
	}

	@Test
	public void should_return_tags_from_nested_map() {
		Map<String, String> tags = new LinkedHashMap<>();
		tags.put("env", "test");
		tags.put("foo", "bar");

		Map<String, Object> nestedTags = new HashMap<>();
		nestedTags.put("tags", tags);
		nestedTags.put("tagsi", singletonMap("ignore", "ignored"));

		assertThat(Tags.from(nestedTags, "tags").getValues()).containsExactly(entry("env", "test"),
				entry("foo", "bar"));
	}

	@Test
	public void should_append_tags() {
		Tags tags = Tags.empty()
			.append(Tags.from(singletonMap("tags.env", "test"), "tags"))
			.append(Tags.from(singletonMap("env", "test2")))
			.append(Tags.from(singletonMap("foo", "bar")));

		assertThat(tags.getValues()).containsExactly(entry("env", "test2"), entry("foo", "bar"));
	}

}
