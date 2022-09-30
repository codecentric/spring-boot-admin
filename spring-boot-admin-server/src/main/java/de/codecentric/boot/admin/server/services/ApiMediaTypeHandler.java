/*
 * Copyright 2014-2022 the original author or authors.
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

package de.codecentric.boot.admin.server.services;

import java.util.stream.Stream;

import org.springframework.boot.actuate.endpoint.ApiVersion;
import org.springframework.http.MediaType;

public class ApiMediaTypeHandler {

	public boolean isApiMediaType(MediaType mediaType) {
		return Stream.of(ApiVersion.values()).map(ApiVersion::getProducedMimeType)
				.anyMatch((mimeType) -> mimeType.isCompatibleWith(mediaType));
	}

}
