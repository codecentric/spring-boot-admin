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

package de.codecentric.boot.admin.server.ui.config;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

class CssColorUtilsTest implements WithAssertions {

	@Test
	void hexToRgb() {
		CssColorUtils cssColorUtils = new CssColorUtils();

		assertThat(cssColorUtils.hexToRgb(new AdminServerUiProperties.Palette().getShade50()))
			.isEqualTo("238, 252, 250");
	}

	@Test
	void hexToRgb_throws_exception_on_invalid_format() {
		CssColorUtils cssColorUtils = new CssColorUtils();

		assertThatThrownBy(() -> cssColorUtils.hexToRgb("EEFCFA")).isInstanceOf(IllegalArgumentException.class);
	}

}
