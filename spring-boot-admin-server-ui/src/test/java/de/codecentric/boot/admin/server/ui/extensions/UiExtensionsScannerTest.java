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

package de.codecentric.boot.admin.server.ui.extensions;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import static org.assertj.core.api.Assertions.assertThat;

public class UiExtensionsScannerTest {

	private final UiExtensionsScanner scanner = new UiExtensionsScanner(new PathMatchingResourcePatternResolver());

	@Test
	public void should_find_extensions() throws IOException {
		UiExtensions extensions = this.scanner.scan("classpath:/META-INF/test-extensions/");
		assertThat(extensions.getCssExtensions()).contains(new UiExtension("custom/custom.abcdef.css",
				"classpath:/META-INF/test-extensions/custom/custom.abcdef.css"));

		assertThat(extensions.getJsExtensions()).contains(new UiExtension("custom/custom.abcdef.js",
				"classpath:/META-INF/test-extensions/custom/custom.abcdef.js"));
	}

	@Test
	public void should_not_find_extensions() throws IOException {
		UiExtensions extensions = this.scanner.scan("classpath:/META-INF/NO-test-extensions/");
		assertThat(extensions.getCssExtensions()).isEmpty();
		assertThat(extensions.getJsExtensions()).isEmpty();
	}

}
