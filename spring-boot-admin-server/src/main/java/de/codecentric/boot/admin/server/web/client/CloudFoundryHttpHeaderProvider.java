/*
 * Copyright 2014-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.server.web.client;

import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import de.codecentric.boot.admin.server.domain.entities.Instance;

/**
 * Provides CloudFoundry related X-CF-APP-INSTANCE header for the {@link Instance} using
 * the metadata for "applicationId" and "instanceId".
 *
 * @author Tetsushi Awano
 */
public class CloudFoundryHttpHeaderProvider implements HttpHeadersProvider {

	@Override
	public HttpHeaders getHeaders(Instance instance) {
		String applicationId = instance.getRegistration().getMetadata().get("applicationId");
		String instanceId = instance.getRegistration().getMetadata().get("instanceId");

		if (StringUtils.hasText(applicationId) && StringUtils.hasText(instanceId)) {
			HttpHeaders headers = new HttpHeaders();
			headers.set("X-CF-APP-INSTANCE", applicationId + ":" + instanceId);
			return headers;
		}

		return HttpHeaders.EMPTY;
	}

}
