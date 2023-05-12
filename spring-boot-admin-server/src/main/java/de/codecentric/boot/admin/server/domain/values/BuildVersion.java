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

package de.codecentric.boot.admin.server.domain.values;

import java.io.Serializable;
import java.util.Map;
import java.util.Scanner;

import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

@lombok.Data
public final class BuildVersion implements Serializable, Comparable<BuildVersion> {

	private static final String DEFAULT_VERSION = "UNKNOWN";

	private final String value;

	private BuildVersion(String value) {
		if (!StringUtils.hasText(value)) {
			this.value = DEFAULT_VERSION;
		}
		else {
			this.value = value;
		}
	}

	public static BuildVersion valueOf(String s) {
		return new BuildVersion(s);
	}

	@Nullable
	public static BuildVersion from(Map<String, ?> map) {
		if (map.isEmpty()) {
			return null;
		}

		Object build = map.get("build");
		if (build instanceof Map) {
			Object version = ((Map<?, ?>) build).get("version");
			if (version instanceof String) {
				return valueOf((String) version);
			}
		}

		Object version = map.get("build.version");
		if (version instanceof String) {
			return valueOf((String) version);
		}

		version = map.get("version");
		if (version instanceof String) {
			return valueOf((String) version);
		}
		return null;
	}

	public String getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		return this.value;
	}

	@Override
	public int compareTo(BuildVersion other) {
		try (Scanner s1 = new Scanner(this.value); Scanner s2 = new Scanner(other.value)) {
			s1.useDelimiter("[.\\-+]");
			s2.useDelimiter("[.\\-+]");

			while (s1.hasNext() && s2.hasNext()) {
				int c;
				if (s1.hasNextInt() && s2.hasNextInt()) {
					c = Integer.compare(s1.nextInt(), s2.nextInt());
				}
				else {
					c = s1.next().compareTo(s2.next());
				}
				if (c != 0) {
					return c;
				}
			}

			if (s1.hasNext()) {
				return 1;
			}
			else if (s2.hasNext()) {
				return -1;
			}
		}
		return 0;
	}

}
