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

package de.codecentric.boot.admin.server.ui.web.reactive;

import de.codecentric.boot.admin.server.ui.web.HomepageForwardingMatcher;
import reactor.core.publisher.Mono;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

public class HomepageForwardingFilter implements WebFilter {
    private static final Logger log = LoggerFactory.getLogger(HomepageForwardingFilter.class);
    private final String homepage;
    private final HomepageForwardingMatcher<ServerHttpRequest> matcher;

    public HomepageForwardingFilter(String homepage, List<String> routes) {
        this.homepage = homepage;
        this.matcher = new HomepageForwardingMatcher<>(
            routes,
            ServerHttpRequest::getMethodValue,
            r -> r.getPath().pathWithinApplication().toString(),
            r -> r.getHeaders().getAccept()
        );
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (this.matcher.test(exchange.getRequest())) {
            log.trace("Forwarding request with URL {} to index", exchange.getRequest().getURI());
            exchange = exchange.mutate().request(request -> request.path(this.homepage)).build();
        }
        return chain.filter(exchange);
    }
}
