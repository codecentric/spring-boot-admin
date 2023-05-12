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

package de.codecentric.boot.admin.server.domain.values;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import static java.util.stream.Collectors.toMap;

@lombok.EqualsAndHashCode
@lombok.ToString
public final class Endpoints implements Iterable<Endpoint>, Serializable {

	private final Map<String, Endpoint> endpoints;

	private static final Endpoints EMPTY = new Endpoints(Collections.emptyList());

	private Endpoints(Collection<Endpoint> endpoints) {
		if (endpoints.isEmpty()) {
			this.endpoints = Collections.emptyMap();
		}
		else {
			this.endpoints = endpoints.stream().collect(toMap(Endpoint::getId, Function.identity()));
		}
	}

	public Optional<Endpoint> get(String id) {
		return Optional.ofNullable(this.endpoints.get(id));
	}

	public boolean isPresent(String id) {
		return this.endpoints.containsKey(id);
	}

	@Override
	public Iterator<Endpoint> iterator() {
		return new UnmodifiableIterator<>(this.endpoints.values().iterator());
	}

	public static Endpoints empty() {
		return EMPTY;
	}

	public static Endpoints single(String id, String url) {
		return new Endpoints(Collections.singletonList(Endpoint.of(id, url)));
	}

	public static Endpoints of(@Nullable Collection<Endpoint> endpoints) {
		if (endpoints == null || endpoints.isEmpty()) {
			return empty();
		}
		return new Endpoints(endpoints);
	}

	public Endpoints withEndpoint(String id, String url) {
		Endpoint endpoint = Endpoint.of(id, url);
		HashMap<String, Endpoint> newEndpoints = new HashMap<>(this.endpoints);
		newEndpoints.put(endpoint.getId(), endpoint);
		return new Endpoints(newEndpoints.values());
	}

	public Stream<Endpoint> stream() {
		return this.endpoints.values().stream();
	}

	private static final class UnmodifiableIterator<T> implements Iterator<T> {

		private final Iterator<T> delegate;

		private UnmodifiableIterator(Iterator<T> delegate) {
			this.delegate = delegate;
		}

		@Override
		public boolean hasNext() {
			return this.delegate.hasNext();
		}

		@Override
		public T next() {
			return this.delegate.next();
		}

	}

}
