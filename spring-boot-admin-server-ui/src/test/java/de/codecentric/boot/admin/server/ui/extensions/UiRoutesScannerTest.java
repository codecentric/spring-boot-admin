/*
 * Copyright 2014-2019 the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThat;

public class UiRoutesScannerTest {
    private final UiRoutesScanner scanner = new UiRoutesScanner(new PathMatchingResourcePatternResolver());

    @Test
    public void should_find_route() throws IOException {
        List<String> routes = this.scanner.scan("classpath:/META-INF/test-extensions/");
        assertThat(routes).containsExactlyInAnyOrder("/custom/**");
    }
}
