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

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RegistrationTest {

	@Test
	public void invariants() {
		assertThatThrownBy(() -> Registration.create(null, null).build()).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("'name' must not be empty.");

		assertThatThrownBy(() -> Registration.create("test", null).build()).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("'healthUrl' must not be empty.");

		assertThatThrownBy(() -> Registration.create("test", "invalid").build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("'healthUrl' is not valid: invalid");

		assertThatThrownBy(() -> Registration.create("test", "http://example.com").managementUrl("invalid").build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("'managementUrl' is not valid: invalid");

		assertThatThrownBy(() -> Registration.create("test", "http://example.com").serviceUrl("invalid").build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("'serviceUrl' is not valid: invalid");
	}

}
