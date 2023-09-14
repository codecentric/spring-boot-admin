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

package de.codecentric.boot.admin.server.ui.web.reactive;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.ui.web.HomepageForwardingFilterConfig;
import de.codecentric.boot.admin.server.ui.web.HomepageForwardingMatcher;

public class HomepageForwardingFilter implements WebFilter {

	private static final Logger log = LoggerFactory.getLogger(HomepageForwardingFilter.class);

	private final String homepage;

	private final HomepageForwardingMatcher<ServerHttpRequest> matcher;

	public HomepageForwardingFilter(String homepage, List<String> routeIncludes, List<String> routeExcludes) {
		this.homepage = homepage;
		this.matcher = new HomepageForwardingMatcher<>(routeIncludes, routeExcludes,
				(request) -> request.getMethod().name(),
				(request) -> request.getPath().pathWithinApplication().toString(),
				(request) -> request.getHeaders().getAccept());
	}

	public HomepageForwardingFilter(HomepageForwardingFilterConfig filterConfig) {
		this(filterConfig.getHomepage(), filterConfig.getRoutesIncludes(), filterConfig.getRoutesExcludes());
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		if (this.matcher.test(exchange.getRequest())) {
			log.trace("Forwarding request with URL {} to index", exchange.getRequest().getURI());
			exchange = exchange.mutate().request((request) -> request.path(this.homepage)).build();
		}
		return chain.filter(exchange);
	}

}
