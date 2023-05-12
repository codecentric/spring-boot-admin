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

package de.codecentric.boot.admin.server.utils.jackson;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.Nullable;

import de.codecentric.boot.admin.server.domain.values.StatusInfo;

/**
 * Jackson Mixin class helps in serialize/deserialize {@link StatusInfo}.
 *
 * @author Stefan Rempfer
 */
public abstract class StatusInfoMixin {

	@JsonCreator
	public static StatusInfo valueOf(@JsonProperty("status") String statusCode,
			@JsonProperty("details") @Nullable Map<String, ?> details) {
		return StatusInfo.valueOf(statusCode, details);
	}

	@JsonIgnore
	public abstract boolean isUp();

	@JsonIgnore
	public abstract boolean isOffline();

	@JsonIgnore
	public abstract boolean isDown();

	@JsonIgnore
	public abstract boolean isUnknown();

}
