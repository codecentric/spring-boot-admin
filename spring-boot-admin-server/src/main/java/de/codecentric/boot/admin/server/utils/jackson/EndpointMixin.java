/*
 * Copyright 2014-2020 the original author or authors.
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

package de.codecentric.boot.admin.server.utils.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.codecentric.boot.admin.server.domain.values.Endpoint;

/**
 * Jackson Mixin class helps in serialize/deserialize {@link Endpoint}.
 *
 * @author Stefan Rempfer
 */
public abstract class EndpointMixin {

	@JsonCreator
	public static Endpoint of(@JsonProperty("id") String id, @JsonProperty("url") String url) {
		return Endpoint.of(id, url);
	}

}
