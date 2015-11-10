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
package de.codecentric.boot.admin.zuul;

import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UrlPathHelper;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.constants.ZuulHeaders;
import com.netflix.zuul.context.RequestContext;

import de.codecentric.boot.admin.zuul.ApplicationRouteLocator.ProxyRouteSpec;

/**
 * @author Johannes Edmeier
 */
public class PreDecorationFilter extends ZuulFilter {
	private static final Logger LOGGER = LoggerFactory.getLogger(PreDecorationFilter.class);
	private ApplicationRouteLocator routeLocator;
	private boolean addProxyHeaders;

	private UrlPathHelper urlPathHelper = new UrlPathHelper();

	public PreDecorationFilter(ApplicationRouteLocator routeLocator, boolean addProxyHeaders) {
		this.routeLocator = routeLocator;
		this.addProxyHeaders = addProxyHeaders;
	}

	@Override
	public int filterOrder() {
		return 5;
	}

	@Override
	public String filterType() {
		return "pre";
	}

	@Override
	public boolean shouldFilter() {
		return true;
	}

	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
		final String requestURI = this.urlPathHelper.getPathWithinApplication(ctx.getRequest());
		ProxyRouteSpec route = this.routeLocator.getMatchingRoute(requestURI);
		if (route != null) {
			ctx.put("requestURI", route.getPath());
			ctx.put("proxy", route.getId());
			ctx.setRouteHost(getUrl(route.getLocation()));
			ctx.addOriginResponseHeader("X-Zuul-Service", route.getLocation());
			if (this.addProxyHeaders) {
				ctx.addZuulRequestHeader("X-Forwarded-Host", ctx.getRequest().getServerName() + ":"
						+ String.valueOf(ctx.getRequest().getServerPort()));
				ctx.addZuulRequestHeader(ZuulHeaders.X_FORWARDED_PROTO,
						ctx.getRequest().getScheme());
				if (StringUtils.hasText(route.getPrefix())) {
					ctx.addZuulRequestHeader("X-Forwarded-Prefix", route.getPrefix());
				}
			}
		} else {
			LOGGER.warn("No route found for uri: " + requestURI);
			ctx.set("forward.to", requestURI);
		}
		return null;
	}

	private URL getUrl(String target) {
		try {
			return new URL(target);
		} catch (MalformedURLException ex) {
			throw new IllegalStateException("Target URL is malformed", ex);
		}
	}
}
