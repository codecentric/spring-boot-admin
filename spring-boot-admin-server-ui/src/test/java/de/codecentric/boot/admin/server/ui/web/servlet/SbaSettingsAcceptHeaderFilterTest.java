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

package de.codecentric.boot.admin.server.ui.web.servlet;

import java.io.IOException;
import java.util.Collections;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class SbaSettingsAcceptHeaderFilterTest {

	private final SbaSettingsAcceptHeaderFilter filter = new SbaSettingsAcceptHeaderFilter();

	@Test
	void should_rewrite_missing_accept_header() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		this.filter.doFilter(request, response, filterChain);

		HttpServletRequest filteredRequest = (HttpServletRequest) filterChain.getRequest();
		assertThat(filteredRequest.getHeader(HttpHeaders.ACCEPT)).isEqualTo("application/javascript");
		assertThat(Collections.list(filteredRequest.getHeaders(HttpHeaders.ACCEPT)))
			.containsExactly("application/javascript");
		assertThat(Collections.list(filteredRequest.getHeaderNames())).contains(HttpHeaders.ACCEPT);
	}

	@Test
	void should_rewrite_blank_accept_header() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(HttpHeaders.ACCEPT, "   ");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		this.filter.doFilter(request, response, filterChain);

		HttpServletRequest filteredRequest = (HttpServletRequest) filterChain.getRequest();
		assertThat(filteredRequest.getHeader(HttpHeaders.ACCEPT)).isEqualTo("application/javascript");
	}

	@Test
	void should_rewrite_wildcard_accept_header() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(HttpHeaders.ACCEPT, "*/*");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		this.filter.doFilter(request, response, filterChain);

		HttpServletRequest filteredRequest = (HttpServletRequest) filterChain.getRequest();
		assertThat(filteredRequest.getHeader(HttpHeaders.ACCEPT)).isEqualTo("application/javascript");
	}

	@Test
	void should_rewrite_wildcard_with_parameters() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(HttpHeaders.ACCEPT, "*/*;q=0.8");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		this.filter.doFilter(request, response, filterChain);

		HttpServletRequest filteredRequest = (HttpServletRequest) filterChain.getRequest();
		assertThat(filteredRequest.getHeader(HttpHeaders.ACCEPT)).isEqualTo("application/javascript");
	}

	@Test
	void should_not_rewrite_specific_accept_header() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(HttpHeaders.ACCEPT, "application/json");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		this.filter.doFilter(request, response, filterChain);

		HttpServletRequest filteredRequest = (HttpServletRequest) filterChain.getRequest();
		assertThat(filteredRequest.getHeader(HttpHeaders.ACCEPT)).isEqualTo("application/json");
		assertThat(filteredRequest).isSameAs(request);
	}

	@Test
	void should_not_rewrite_mixed_accept_header() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,*/*;q=0.8");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		this.filter.doFilter(request, response, filterChain);

		HttpServletRequest filteredRequest = (HttpServletRequest) filterChain.getRequest();
		assertThat(filteredRequest).isSameAs(request);
	}

	@Test
	void should_not_rewrite_when_any_multiple_headers_are_specific() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(HttpHeaders.ACCEPT, "*/*");
		request.addHeader(HttpHeaders.ACCEPT, "application/json");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		this.filter.doFilter(request, response, filterChain);

		HttpServletRequest filteredRequest = (HttpServletRequest) filterChain.getRequest();
		assertThat(filteredRequest).isSameAs(request);
	}

	@Test
	void should_preserve_other_headers() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("X-Custom-Header", "custom-value");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		this.filter.doFilter(request, response, filterChain);

		HttpServletRequest filteredRequest = (HttpServletRequest) filterChain.getRequest();
		assertThat(filteredRequest.getHeader("X-Custom-Header")).isEqualTo("custom-value");
		assertThat(filteredRequest.getHeader(HttpHeaders.ACCEPT)).isEqualTo("application/javascript");
	}

}
