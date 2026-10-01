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
import java.util.Enumeration;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

public class SbaSettingsAcceptHeaderFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		filterChain.doFilter(shouldRewriteAcceptHeader(request) ? new AcceptHeaderRequestWrapper(request) : request,
				response);
	}

	private static boolean shouldRewriteAcceptHeader(HttpServletRequest request) {
		Enumeration<String> rawHeaders = request.getHeaders(HttpHeaders.ACCEPT);
		if (rawHeaders == null || !rawHeaders.hasMoreElements()) {
			return true;
		}
		List<String> acceptHeaders = Collections.list(rawHeaders);
		if (acceptHeaders.isEmpty() || acceptHeaders.stream().allMatch(String::isBlank)) {
			return true;
		}
		try {
			List<MediaType> mediaTypes = MediaType.parseMediaTypes(acceptHeaders);
			return !mediaTypes.isEmpty() && mediaTypes.stream()
				.allMatch((mediaType) -> mediaType.isWildcardType() && mediaType.isWildcardSubtype());
		}
		catch (IllegalArgumentException ex) {
			return false;
		}
	}

	private static final class AcceptHeaderRequestWrapper extends HttpServletRequestWrapper {

		private static final String ACCEPT = "application/javascript";

		private AcceptHeaderRequestWrapper(HttpServletRequest request) {
			super(request);
		}

		@Override
		public String getHeader(String name) {
			if (HttpHeaders.ACCEPT.equalsIgnoreCase(name)) {
				return ACCEPT;
			}
			return super.getHeader(name);
		}

		@Override
		public Enumeration<String> getHeaders(String name) {
			if (HttpHeaders.ACCEPT.equalsIgnoreCase(name)) {
				return Collections.enumeration(List.of(ACCEPT));
			}
			return super.getHeaders(name);
		}

		@Override
		public Enumeration<String> getHeaderNames() {
			List<String> names = Collections.list(super.getHeaderNames());
			if (names.stream().noneMatch(HttpHeaders.ACCEPT::equalsIgnoreCase)) {
				names.add(HttpHeaders.ACCEPT);
			}
			return Collections.enumeration(names);
		}

	}

}
