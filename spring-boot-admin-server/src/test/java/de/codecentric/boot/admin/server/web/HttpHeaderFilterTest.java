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

package de.codecentric.boot.admin.server.web;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;
import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;

class HttpHeaderFilterTest {

	@Test
	void shouldRemoveHeadersNamedByConnectionOptions() {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONNECTION, "keep-alive, X-First-Hop");
		headers.add(HttpHeaders.CONNECTION, "\t x-SECOND-hop , X-Missing ");
		headers.add("x-first-hop", "first");
		headers.add("X-Second-Hop", "second");
		headers.add("X-End-To-End", "retained");

		HttpHeaders filtered = new HttpHeaderFilter(Set.of()).filterHeaders(headers);

		assertThat(filtered.headerNames()).containsExactly("X-End-To-End");
		assertThat(filtered.getFirst("X-End-To-End")).isEqualTo("retained");
		assertThat(headers.get(HttpHeaders.CONNECTION)).hasSize(2);
		assertThat(headers.getFirst("x-first-hop")).isEqualTo("first");
		assertThat(headers.getFirst("X-Second-Hop")).isEqualTo("second");
	}

	@Test
	void shouldNotReuseConnectionOptionsForOtherMessages() {
		HttpHeaderFilter filter = new HttpHeaderFilter(Set.of());
		HttpHeaders first = new HttpHeaders();
		first.add(HttpHeaders.CONNECTION, "X-Custom");
		first.add("X-Custom", "connection-specific");
		assertThat(filter.filterHeaders(first).isEmpty()).isTrue();

		HttpHeaders second = new HttpHeaders();
		second.add("X-Custom", "end-to-end");
		assertThat(filter.filterHeaders(second).getFirst("X-Custom")).isEqualTo("end-to-end");
	}

	@Test
	void shouldPreserveEndToEndHeaderValues() {
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-End-To-End", "first");
		headers.add("X-End-To-End", "second");
		headers.add(HttpHeaders.CONTENT_TYPE, "application/json");

		HttpHeaders filtered = new HttpHeaderFilter(Set.of()).filterHeaders(HttpHeaders.readOnlyHttpHeaders(headers));

		assertThat(filtered).isEqualTo(headers);
		assertThat(filtered.get("X-End-To-End")).containsExactly("first", "second");
	}

	@Test
	void shouldRemoveKnownAndConfiguredHeaders() {
		HttpHeaders headers = new HttpHeaders();
		List.of("Host", "Connection", "Keep-Alive", "Proxy-Authenticate", "Proxy-Authorization", "TE", "Trailer",
				"Transfer-Encoding", "Upgrade", "X-Application-Context", "X-Ignored")
			.forEach((name) -> headers.add(name, "ignored"));

		HttpHeaders filtered = new HttpHeaderFilter(Set.of("x-ignored")).filterHeaders(headers);

		assertThat(filtered.isEmpty()).isTrue();
	}

	@Test
	@ResourceLock(Resources.LOCALE)
	void shouldCompareHeaderNamesIndependentlyOfDefaultLocale() {
		Locale previous = Locale.getDefault();
		try {
			Locale.setDefault(Locale.forLanguageTag("tr-TR"));
			HttpHeaders headers = new HttpHeaders();
			headers.add("CONNECTION", "X-INTERMEDIATE");
			headers.add("x-intermediate", "connection-specific");
			headers.add("x-ignored", "configured");

			HttpHeaders filtered = new HttpHeaderFilter(Set.of("X-IGNORED")).filterHeaders(headers);

			assertThat(filtered.isEmpty()).isTrue();
		}
		finally {
			Locale.setDefault(previous);
		}
	}

}
