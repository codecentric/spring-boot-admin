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

import java.time.Duration;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest(classes = AdminServerUiProperties.class)
@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = { "spring.boot.admin.ui.cache.maxAge: -1s", "spring.boot.admin.ui.cache.noCache: true",
		"spring.boot.admin.ui.cache.noStore: true", "spring.boot.admin.ui.theme.color: #ffffff",
		"spring.boot.admin.ui.theme.palette.50: #50", "spring.boot.admin.ui.theme.palette.100: #100",
		"spring.boot.admin.ui.theme.palette.200: #200", "spring.boot.admin.ui.theme.palette.300: #300",
		"spring.boot.admin.ui.theme.palette.400: #400", "spring.boot.admin.ui.theme.palette.500: #500",
		"spring.boot.admin.ui.theme.palette.600: #600", "spring.boot.admin.ui.theme.palette.700: #700",
		"spring.boot.admin.ui.theme.palette.800: #800", "spring.boot.admin.ui.theme.palette.900: #900" })
@EnableConfigurationProperties({ AdminServerUiProperties.class })
class AdminServerUiPropertiesTest implements WithAssertions {

	@Autowired
	private AdminServerUiProperties adminServerUiProperties;

	@Nested
	class CacheTest {

		@Test
		void maxAge() {
			assertThat(adminServerUiProperties.getCache().getMaxAge()).isEqualTo(Duration.ofSeconds(-1));
		}

		@Test
		void noCache() {
			assertThat(adminServerUiProperties.getCache().getNoCache()).isEqualTo(true);
		}

		@Test
		void noStore() {
			assertThat(adminServerUiProperties.getCache().getNoStore()).isEqualTo(true);
		}

	}

	@Nested
	class ThemeTest {

		@Test
		void color() {
			assertThat(adminServerUiProperties.getTheme().getColor()).isEqualTo("#ffffff");
		}

		@Nested
		class PaletteTest {

			@Test
			void shades() {
				AdminServerUiProperties.UiTheme theme = adminServerUiProperties.getTheme();
				AdminServerUiProperties.Palette palette = theme.getPalette();

				assertThat(palette.getShade50()).isEqualTo("#50");
				assertThat(palette.getShade100()).isEqualTo("#100");
				assertThat(palette.getShade200()).isEqualTo("#200");
				assertThat(palette.getShade300()).isEqualTo("#300");
				assertThat(palette.getShade400()).isEqualTo("#400");
				assertThat(palette.getShade500()).isEqualTo("#500");
				assertThat(palette.getShade600()).isEqualTo("#600");
				assertThat(palette.getShade700()).isEqualTo("#700");
				assertThat(palette.getShade800()).isEqualTo("#800");
				assertThat(palette.getShade900()).isEqualTo("#900");
			}

		}

	}

}
