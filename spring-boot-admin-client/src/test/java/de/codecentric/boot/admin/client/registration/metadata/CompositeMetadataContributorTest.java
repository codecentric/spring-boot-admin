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

package de.codecentric.boot.admin.client.registration.metadata;

import java.util.Map;

import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class CompositeMetadataContributorTest {

	@Test
	public void should_merge_metadata() {
		CompositeMetadataContributor contributor = new CompositeMetadataContributor(
				asList(() -> singletonMap("a", "first"), () -> singletonMap("b", "second"),
						() -> singletonMap("b", "second-new")));

		Map<String, String> metadata = contributor.getMetadata();

		assertThat(metadata).containsExactly(entry("a", "first"), entry("b", "second-new"));
	}

	@Test
	public void should_return_empty_metadata() {
		CompositeMetadataContributor contributor = new CompositeMetadataContributor(emptyList());

		Map<String, String> metadata = contributor.getMetadata();

		assertThat(metadata).isEmpty();
	}

}
