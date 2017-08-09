/*
 * Copyright 2014-2017 the original author or authors.
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

package de.codecentric.boot.admin.server.domain.values;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@EqualsAndHashCode
@ToString
public class Endpoints implements Iterable<Endpoint>, Serializable {
    private final Map<String, Endpoint> endpoints;
    private static final Endpoints EMPTY = new Endpoints(null);

    private Endpoints(Collection<Endpoint> endpoints) {
        if (endpoints == null || endpoints.isEmpty()) {
            this.endpoints = Collections.emptyMap();
        } else if (endpoints.size() == 1) {
            Endpoint endpoint = endpoints.iterator().next();
            this.endpoints = Collections.singletonMap(endpoint.getId(), endpoint);
        } else {
            this.endpoints = endpoints.stream().collect(toMap(Endpoint::getId, Function.identity()));
        }
    }

    public Endpoint get(String id) {
        Endpoint endpoint = endpoints.get(id);
        if (endpoint == null) {
            throw new NoSuchElementException("There is no Endpoint '" + id + "'");
        }
        return endpoint;
    }

    public boolean isPresent(String id) {
        return endpoints.containsKey(id);
    }

    @Override
    public Iterator<Endpoint> iterator() {
        return new UnmodifiableIterator<>(endpoints.values().iterator());
    }

    public static Endpoints empty() {
        return EMPTY;
    }

    public static Endpoints single(String id, String url) {
        return new Endpoints(Collections.singletonList(Endpoint.of(id, url)));
    }

    public static Endpoints of(Collection<Endpoint> endpoints) {
        return new Endpoints(endpoints);
    }

    public Endpoints withEndpoint(String id, String url) {
        Endpoint endpoint = Endpoint.of(id, url);
        HashMap<String, Endpoint> newEndpoints = new HashMap<>(this.endpoints);
        newEndpoints.put(endpoint.getId(), endpoint);
        return new Endpoints(newEndpoints.values());
    }

    private static class UnmodifiableIterator<T> implements Iterator<T> {
        private final Iterator<T> delegate;

        private UnmodifiableIterator(Iterator<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void forEachRemaining(Consumer<? super T> action) {
            delegate.forEachRemaining(action);
        }

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public T next() {
            return delegate.next();
        }
    }
}
