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

import org.junit.jupiter.api.Test;

import de.codecentric.boot.admin.client.config.CloudFoundryApplicationProperties;

import static org.assertj.core.api.Assertions.assertThat;

public class CloudFoundryMetadataContributorTest {

	@Test
	public void should_return_empty_metadata() {
		CloudFoundryMetadataContributor contributor = new CloudFoundryMetadataContributor(
				new CloudFoundryApplicationProperties());
		assertThat(contributor.getMetadata()).isEmpty();
	}

	@Test
	public void should_return_metadata() {
		CloudFoundryApplicationProperties cfApplicationProperties = new CloudFoundryApplicationProperties();
		cfApplicationProperties.setApplicationId("appId");
		cfApplicationProperties.setInstanceIndex("1");
		CloudFoundryMetadataContributor contributor = new CloudFoundryMetadataContributor(cfApplicationProperties);
		assertThat(contributor.getMetadata()).containsEntry("applicationId", "appId").containsEntry("instanceId", "1");
	}

}
