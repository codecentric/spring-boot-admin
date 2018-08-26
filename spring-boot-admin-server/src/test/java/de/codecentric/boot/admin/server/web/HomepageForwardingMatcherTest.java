/*
 * Copyright 2014-2018 the original author or authors.
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

package de.codecentric.boot.admin.server.web;

import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class HomepageForwardingMatcherTest {
    private final HomepageForwardingMatcher matcher = new HomepageForwardingMatcher("");
    private final HomepageForwardingMatcher matcherWithBasePath = new HomepageForwardingMatcher("/base");
    private final List<MediaType> htmlMediaType = Collections.singletonList(MediaType.TEXT_HTML);

    @Test
    public void should_match_for_get_request_with_html_accept_header() {
        assertThat(matcher.match(HttpMethod.GET, "/some/path", htmlMediaType)).isTrue();
        assertThat(matcherWithBasePath.match(HttpMethod.GET, "/base/some/path", htmlMediaType)).isTrue();
    }

    @Test
    public void should_not_match_for_non_get_requests() {
        assertThat(matcher.match(HttpMethod.POST, "/some/path", htmlMediaType)).isFalse();
    }

    @Test
    public void should_not_match_for_homepage_path() {
        assertThat(matcher.match(HttpMethod.GET, "/", htmlMediaType)).isFalse();
        assertThat(matcherWithBasePath.match(HttpMethod.GET, "/base", htmlMediaType)).isFalse();
    }

    @Test
    public void should_not_match_for_ignored_path() {
        assertThat(matcher.match(HttpMethod.GET, "/login", htmlMediaType)).isFalse();
        assertThat(matcherWithBasePath.match(HttpMethod.GET, "/base/login", htmlMediaType)).isFalse();
    }

    @Test
    public void should_not_match_when_no_html_accept_header() {
        assertThat(matcher.match(HttpMethod.GET, "/", Collections.EMPTY_LIST)).isFalse();
    }
}
