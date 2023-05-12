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

package de.codecentric.boot.admin.server.ui.web.servlet;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.util.UrlPathHelper;

import de.codecentric.boot.admin.server.ui.web.HomepageForwardingFilterConfig;
import de.codecentric.boot.admin.server.ui.web.HomepageForwardingMatcher;

/**
 * A servlet filter that forwards matching HTML requests to the home page. Further routing
 * will is done in the browser by VueRouter using history mode.
 *
 * @see <a href="https://router.vuejs.org/guide/essentials/history-mode.html">VueRouter
 * history mode</a>
 */
public class HomepageForwardingFilter implements Filter {

	private static final Logger log = LoggerFactory.getLogger(HomepageForwardingFilter.class);

	private final String homepage;

	private final HomepageForwardingMatcher<HttpServletRequest> matcher;

	public HomepageForwardingFilter(String homepage, List<String> routes, List<String> excludedRoutes) {
		this.homepage = homepage;
		UrlPathHelper urlPathHelper = new UrlPathHelper();
		this.matcher = new HomepageForwardingMatcher<>(routes, excludedRoutes, HttpServletRequest::getMethod,
				urlPathHelper::getPathWithinApplication,
				(r) -> MediaType.parseMediaTypes(r.getHeader(HttpHeaders.ACCEPT)));
	}

	public HomepageForwardingFilter(HomepageForwardingFilterConfig filterConfig) {
		this(filterConfig.getHomepage(), filterConfig.getRoutesIncludes(), filterConfig.getRoutesExcludes());
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (request instanceof HttpServletRequest httpRequest) {
			if (this.matcher.test(httpRequest)) {
				log.trace("Forwarding request with URL {} to index", httpRequest.getRequestURI());
				request.getRequestDispatcher(this.homepage).forward(request, response);
				return;
			}
		}

		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig filterConfig) {
	}

	@Override
	public void destroy() {
	}

}
