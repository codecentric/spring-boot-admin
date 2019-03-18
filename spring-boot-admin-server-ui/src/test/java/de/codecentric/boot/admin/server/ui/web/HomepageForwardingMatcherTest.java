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

package de.codecentric.boot.admin.server.ui.web;

import lombok.Data;

import java.util.List;
import org.junit.Test;
import org.springframework.http.MediaType;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class HomepageForwardingMatcherTest {
    private final HomepageForwardingMatcher<MockRequest> matcher = new HomepageForwardingMatcher<>(singletonList(
        "/viewRoute/**"), MockRequest::getMethod, MockRequest::getPath, MockRequest::getAccepts);

    @Test
    public void should_return_false_when_method_is_not_get() {
        assertThat(this.matcher.test(new MockRequest("POST",
            "/viewRoute",
            singletonList(MediaType.TEXT_HTML)
        ))).isFalse();
    }

    @Test
    public void should_return_false_when_path_does_not_match() {
        assertThat(this.matcher.test(new MockRequest("GET", "/api", singletonList(MediaType.TEXT_HTML)))).isFalse();
    }

    @Test
    public void should_return_false_when_accepts_does_not_match() {
        assertThat(this.matcher.test(new MockRequest("GET",
            "/viewRoute",
            singletonList(MediaType.APPLICATION_XML)
        ))).isFalse();
    }

    @Test
    public void should_return_true() {
        assertThat(this.matcher.test(new MockRequest("GET",
            "/viewRoute/detail?query",
            singletonList(MediaType.TEXT_HTML)
        ))).isTrue();
    }

    @Data
    private static class MockRequest {
        private final String method;
        private final String path;
        private final List<MediaType> accepts;
    }
}
