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

package de.codecentric.boot.admin.server.web.client.reactive;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class CompositeReactiveHttpHeadersProviderTest {

	@Test
	public void should_return_all_headers() {
		ReactiveHttpHeadersProvider provider = new CompositeReactiveHttpHeadersProvider(asList((i) -> {
			HttpHeaders headers = new HttpHeaders();
			headers.set("a", "1");
			headers.set("b", "2-a");
			return Mono.just(headers);
		}, (i) -> {
			HttpHeaders headers = new HttpHeaders();
			headers.set("b", "2-b");
			headers.set("c", "3");
			return Mono.just(headers);
		}));

		StepVerifier.create(provider.getHeaders(null)).thenConsumeWhile((headers) -> {
			assertThat(headers.get("a")).isEqualTo(singletonList("1"));
			assertThat(headers.get("b")).isEqualTo(asList("2-a", "2-b"));
			assertThat(headers.get("c")).isEqualTo(singletonList("3"));
			return true;
		}).verifyComplete();
	}

	@Test
	public void should_return_empty_headers() {
		CompositeReactiveHttpHeadersProvider provider = new CompositeReactiveHttpHeadersProvider(emptyList());

		StepVerifier.create(provider.getHeaders(null)).thenConsumeWhile((headers) -> {
			assertThat(headers.size()).isEqualTo(0);
			return true;
		}).verifyComplete();
	}

}
