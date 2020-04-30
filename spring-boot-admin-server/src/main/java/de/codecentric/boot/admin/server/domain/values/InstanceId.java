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

package de.codecentric.boot.admin.server.domain.values;

import java.io.Serializable;

import org.springframework.util.Assert;

/**
 * Value type for the instance identifier
 */
@lombok.Data
public final class InstanceId implements Serializable, Comparable<InstanceId> {

	private final String value;

	private InstanceId(String value) {
		Assert.hasText(value, "'value' must have text");
		this.value = value;
	}

	public static InstanceId of(String value) {
		return new InstanceId(value);
	}

	@Override
	public String toString() {
		return value;
	}

	@Override
	public int compareTo(InstanceId that) {
		return this.value.compareTo(that.value);
	}

}
