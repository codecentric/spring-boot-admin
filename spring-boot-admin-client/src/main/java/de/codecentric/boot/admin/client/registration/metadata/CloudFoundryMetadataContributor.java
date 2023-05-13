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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

import de.codecentric.boot.admin.client.config.CloudFoundryApplicationProperties;

public class CloudFoundryMetadataContributor implements MetadataContributor {

	private final CloudFoundryApplicationProperties cfApplicationProperties;

	public CloudFoundryMetadataContributor(CloudFoundryApplicationProperties cfApplicationProperties) {
		this.cfApplicationProperties = cfApplicationProperties;
	}

	@Override
	public Map<String, String> getMetadata() {
		if (StringUtils.hasText(this.cfApplicationProperties.getApplicationId())
				&& StringUtils.hasText(this.cfApplicationProperties.getInstanceIndex())) {
			Map<String, String> map = new HashMap<>();
			map.put("applicationId", this.cfApplicationProperties.getApplicationId());
			map.put("instanceId", this.cfApplicationProperties.getInstanceIndex());
			return map;
		}
		return Collections.emptyMap();
	}

}
