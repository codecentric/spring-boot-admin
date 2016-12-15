/*
 * Copyright 2013-2015 the original author or authors.
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
package de.codecentric.boot.admin.zuul.filters.pre;

import java.util.List;
import java.util.Map.Entry;

import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UrlPathHelper;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

import de.codecentric.boot.admin.web.client.HttpHeadersProvider;
import de.codecentric.boot.admin.zuul.ApplicationRouteLocator;
import de.codecentric.boot.admin.zuul.ApplicationRouteLocator.ApplicationRoute;

/**
 * This filter adds headers to the zuulRequest specific for the application using a
 * {@link HttpHeadersProvider}.
 *
 * @author Johannes Edmeier
 */
public class ApplicationHeadersFilter extends ZuulFilter {

	private final HttpHeadersProvider headersProvider;
	private final ApplicationRouteLocator routeLocator;
	private final UrlPathHelper urlPathHelper = new UrlPathHelper();

	public ApplicationHeadersFilter(HttpHeadersProvider headersProvider,
			ApplicationRouteLocator routeLocator) {
		this.headersProvider = headersProvider;
		this.routeLocator = routeLocator;
	}

	@Override
	public boolean shouldFilter() {
		return true;
	}

	@Override
	public int filterOrder() {
		return 0;
	}

	@Override
	public String filterType() {
		return "pre";
	}

	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
		String requestURI = this.urlPathHelper.getPathWithinApplication(ctx.getRequest());
		ApplicationRoute route = this.routeLocator.getMatchingRoute(requestURI);
		if (route != null) {
			HttpHeaders headers = headersProvider.getHeaders(route.getApplication());
			for (Entry<String, List<String>> header : headers.entrySet()) {
				ctx.addZuulRequestHeader(header.getKey(), header.getValue().get(0));
			}
		}
		return null;
	}
}