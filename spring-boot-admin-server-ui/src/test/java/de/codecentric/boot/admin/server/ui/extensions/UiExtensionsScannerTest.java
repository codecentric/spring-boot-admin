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

package de.codecentric.boot.admin.server.ui.extensions;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import de.codecentric.boot.admin.server.ui.config.AdminServerUiProperties;

import static org.assertj.core.api.Assertions.assertThat;

public class UiExtensionsScannerTest {

	private final UiExtensionsScanner scanner = new UiExtensionsScanner(new PathMatchingResourcePatternResolver());

	private final AdminServerUiProperties adminUi = new AdminServerUiProperties();

	@Test
	public void should_find_extensions() throws IOException {
		List<UiExtension> extensions = this.scanner.scan(this.adminUi.getExtensionResourceLocations(),
				this.adminUi.getResourceLocations());
		assertThat(extensions).containsExactlyInAnyOrder(new UiExtension("extensions/custom/custom.abcdef.js",
				"classpath:/META-INF/spring-boot-admin-server-ui/extensions/custom/custom.abcdef.js"),
				new UiExtension("extensions/custom/custom.abcdef.css",
						"classpath:/META-INF/spring-boot-admin-server-ui/extensions/custom/custom.abcdef.css"));
	}

	@Test
	public void should_not_find_extensions() throws IOException {
		assertThat(this.scanner.scan(new String[] { "classpath:/META-INF/NO-test-extensions/" },
				new String[] { "classpath:/META-INF/test-extensions/" })).isEmpty();
		assertThat(this.scanner.scan(new String[] { "classpath:/META-INF/NO-test-extensions/" }, new String[] {}))
				.isEmpty();
		assertThat(this.scanner.scan(this.adminUi.getExtensionResourceLocations(),
				new String[] { "classpath:/META-INF/NO-test-extensions/" })).isEmpty();
	}

}
