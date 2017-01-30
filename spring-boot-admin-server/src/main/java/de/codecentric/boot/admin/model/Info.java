/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.codecentric.boot.admin.model;

import static java.util.Collections.unmodifiableMap;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents the info fetched from the info actuator endpoint at a certain time.
 *
 * @author Johannes Edmeier
 */
public class Info implements Serializable {
	private static final long serialVersionUID = 2L;
	private static Info EMPTY = new Info(0L, null);

	@JsonIgnore
	private final long timestamp;
	private final Map<String, Serializable> values;

	protected Info(long timestamp, Map<String, ? extends Serializable> values) {
		this.timestamp = timestamp;
		this.values = values != null ? unmodifiableMap(new LinkedHashMap<>(values))
				: Collections.<String, Serializable>emptyMap();
	}

	public static Info from(Map<String, ? extends Serializable> values) {
		return new Info(System.currentTimeMillis(), values);
	}

	public static Info empty() {
		return EMPTY;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String getVersion() {
		Object build = this.values.get("build");
		if (build instanceof Map) {
			Object version = ((Map<?, ?>) build).get("version");
			if (version instanceof String) {
				return (String) version;
			}
		}

		Object version = this.values.get("version");
		if (version instanceof String) {
			return (String) version;
		}

		return "";
	}

	@JsonAnyGetter
	public Map<String, Serializable> getValues() {
		return values;
	}

	@Override
	public String toString() {
		return "Info [timestamp=" + timestamp + ", values=" + values + "]";
	}

}
