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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.entities.Instance;

import static java.util.stream.Collectors.toList;

public class CompositeReactiveHttpHeadersProvider implements ReactiveHttpHeadersProvider {

	private final Collection<ReactiveHttpHeadersProvider> delegates;

	public CompositeReactiveHttpHeadersProvider(Collection<ReactiveHttpHeadersProvider> delegates) {
		this.delegates = delegates;
	}

	@Override
	public Mono<HttpHeaders> getHeaders(Instance instance) {
		List<Mono<HttpHeaders>> headers = delegates.stream()
			.map((reactiveHttpHeadersProvider) -> reactiveHttpHeadersProvider.getHeaders(instance))
			.collect(toList());

		return Mono.zip(headers, this::mergeMonosToHeaders);
	}

	private HttpHeaders mergeMonosToHeaders(Object[] e) {
		return Arrays.stream(e).map(HttpHeaders.class::cast).reduce(new HttpHeaders(), (h1, h2) -> {
			h1.addAll(h2);
			return h1;
		});
	}

}
