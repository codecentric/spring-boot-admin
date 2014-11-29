/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.codecentric.boot.admin.web;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.mvc.EndpointHandlerMapping;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Request filter to allow Cross-Origin Resource Sharing.
 */
public class EndpointCorsFilter extends OncePerRequestFilter {
	private static final Logger LOGGER = LoggerFactory.getLogger(EndpointCorsFilter.class);

	// Configurable origin for CORS - default: * (all)
	@Value("${http.filter.cors.origin:*}")
	private String origin;

	@Value("${http.filter.cors.headers:Origin, X-Requested-With, Content-Type, Accept}")
	private String headers;

	private final EndpointHandlerMapping endpointHandlerMapping;

	public EndpointCorsFilter(EndpointHandlerMapping endpointHandlerMapping) {
		this.endpointHandlerMapping = endpointHandlerMapping;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			if (!endpointHandlerMapping.isDisabled() && endpointHandlerMapping.getHandler(request) != null) {
				response.setHeader("Access-Control-Allow-Origin", origin);
				response.setHeader("Access-Control-Allow-Headers", headers);
			}
		}
		catch (Exception ex) {
			LOGGER.warn("Error occured while adding CORS-Headers", ex);
		}

		filterChain.doFilter(request, response);
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getOrigin() {
		return origin;
	}

	public String getHeaders() {
		return headers;
	}

	public void setHeaders(String headers) {
		this.headers = headers;
	}

}
