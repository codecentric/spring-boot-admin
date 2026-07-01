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

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import tools.jackson.databind.annotation.JsonDeserialize;

import de.codecentric.boot.admin.server.domain.values.Info;

/**
 * Jackson Mixin class helps in serialize/deserialize {@link Info}.
 *
 * @author Stefan Rempfer
 */
@JsonDeserialize(builder = InfoMixin.Builder.class)
public abstract class InfoMixin {

	@JsonAnyGetter
	public abstract Map<String, Object> getValues();

	public static class Builder {

		private final Map<String, Object> values = new LinkedHashMap<>();

		@JsonAnySetter
		public Builder set(String key, Object value) {
			this.values.put(key, value);
			return this;
		}

		public Info build() {
			return Info.from(this.values);
		}

	}

}
