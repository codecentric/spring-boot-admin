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

package de.codecentric.boot.admin.server.web.reactive;

import de.codecentric.boot.admin.server.web.HomepageForwardingMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * A web filter that forwards matching HTML requests to the home page.
 * Further routing will is done in the browser by VueRouter using history mode.
 *
 * @see <a href="https://router.vuejs.org/guide/essentials/history-mode.html">VueRouter history mode</a>
 */
public class HomepageForwardingWebFilter implements WebFilter {
    private static final Logger log = LoggerFactory.getLogger(HomepageForwardingWebFilter.class);
    private final HomepageForwardingMatcher homepageForwardingMatcher;

    public HomepageForwardingWebFilter(HomepageForwardingMatcher homepageForwardingMatcher) {
        this.homepageForwardingMatcher = homepageForwardingMatcher;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (match(exchange.getRequest())) {
            log.trace("Forwarding request with URL {} to homepage", exchange.getRequest().getURI());
            return chain.filter(exchange.mutate().request(exchange.getRequest().mutate().path(
                homepageForwardingMatcher.getHomepagePath()).build()).build());
        }

        return chain.filter(exchange);
    }

    private boolean match(ServerHttpRequest request) {
        return homepageForwardingMatcher.match(request.getMethod(), request.getURI().getPath(), request.getHeaders()
            .getAccept());
    }
}
