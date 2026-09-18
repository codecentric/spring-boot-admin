/*
 * Copyright 2014-2026 the original author or authors.
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

package de.codecentric.boot.admin.server.utils.concurrency;

import java.lang.reflect.Constructor;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConcurrencyUtilsTest {

	@Test
	void halfCpus_returnsHalfOfAvailableProcessors_withMinimumOfOne() {
		int availableProcessors = Runtime.getRuntime().availableProcessors();
		int expected = Math.max(1, availableProcessors / 2);

		assertThat(ConcurrencyUtils.halfCpus()).isEqualTo(expected);
	}

	@Test
	void halfCpus_returnsOne_whenAvailableProcessorsIsOne() {
		int availableProcessors = 1;
		int expected = 1;

		assertThat(ConcurrencyUtils.halfCpus(availableProcessors)).isEqualTo(expected);
	}

	@Test
	void constructor_throwsAssertionError() throws Exception {
		Constructor<ConcurrencyUtils> constructor = ConcurrencyUtils.class.getDeclaredConstructor();
		constructor.setAccessible(true);

		assertThatThrownBy(constructor::newInstance).hasRootCauseInstanceOf(AssertionError.class)
			.hasRootCauseMessage("Utility class should not be instantiated");
	}

}
