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

import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class BuildVersionTest {

	@Test
	public void should_return_version() {
		assertThat(BuildVersion.valueOf(null).getValue()).isEqualTo("UNKNOWN");
		assertThat(BuildVersion.from(emptyMap())).isNull();
		assertThat(BuildVersion.from(singletonMap("version", "1.0.0"))).isEqualTo(BuildVersion.valueOf("1.0.0"));
		assertThat(BuildVersion.from(singletonMap("build.version", "1.0.0"))).isEqualTo(BuildVersion.valueOf("1.0.0"));
		assertThat(BuildVersion.from(singletonMap("build", singletonMap("version", "1.0.0"))))
			.isEqualTo(BuildVersion.valueOf("1.0.0"));
	}

	@Test
	public void should_return_simple_string() {
		assertThat(BuildVersion.valueOf("1.0.0").toString()).isEqualTo("1.0.0");
	}

	@Test
	public void compare() {
		assertThat(doCompare("1.0.0", "1.0.0")).isEqualTo(0);
		assertThat(doCompare("1.0.1", "1.0.0")).isEqualTo(1);
		assertThat(doCompare("1.0.0", "1.0.1")).isEqualTo(-1);

		assertThat(doCompare("1.1.0", "1.0.0")).isEqualTo(1);
		assertThat(doCompare("1.0.0", "1.1.0")).isEqualTo(-1);

		assertThat(doCompare("2.0.0", "1.0.0")).isEqualTo(1);
		assertThat(doCompare("1.0.0", "2.0.0")).isEqualTo(-1);

		assertThat(doCompare("1.0.0.0", "1.0.0")).isEqualTo(1);
		assertThat(doCompare("1.0.0", "1.0.0.0")).isEqualTo(-1);

		assertThat(doCompare("1.11.0", "1.2.0")).isEqualTo(1);
		assertThat(doCompare("1.2.0", "1.11.0")).isEqualTo(-1);

		assertThat(doCompare("1.0.0.RC1", "1.0.0.RC1")).isEqualTo(0);
		assertThat(doCompare("1.0.0.RC2", "1.0.0.RC1")).isEqualTo(1);
		assertThat(doCompare("1.0.0.RC1", "1.0.0.RC2")).isEqualTo(-1);
		assertThat(doCompare("1.0.1.RC1", "1.0.0.RC1")).isEqualTo(1);
		assertThat(doCompare("1.0.0.RC1", "1.0.1.RC1")).isEqualTo(-1);

		assertThat(doCompare("1.0.0-beta1", "1.0.0-beta1")).isEqualTo(0);
		assertThat(doCompare("1.0.0-beta2", "1.0.0-beta1")).isEqualTo(1);
		assertThat(doCompare("1.0.0-beta1", "1.0.0-beta2")).isEqualTo(-1);
	}

	private int doCompare(String v1, String v2) {
		return BuildVersion.valueOf(v1).compareTo(BuildVersion.valueOf(v2));
	}

}
