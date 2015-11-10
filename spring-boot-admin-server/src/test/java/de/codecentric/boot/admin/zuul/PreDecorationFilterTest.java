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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.netflix.zuul.context.RequestContext;

import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.registry.ApplicationRegistry;
import de.codecentric.boot.admin.registry.HashingApplicationUrlIdGenerator;
import de.codecentric.boot.admin.registry.store.ApplicationStore;
import de.codecentric.boot.admin.registry.store.SimpleApplicationStore;

public class PreDecorationFilterTest {

	private PreDecorationFilter filter;

	private ApplicationRouteLocator routeLocator;

	private MockHttpServletRequest request = new MockHttpServletRequest();

	private ApplicationRegistry registry;

	private ApplicationStore store;

	@Before
	public void init() {
		store = new SimpleApplicationStore();
		store.save(Application.create("test").withId("-id-").withHealthUrl("http://test/health")
				.withManagementUrl("http://mgmt").build());
		registry = new ApplicationRegistry(store, new HashingApplicationUrlIdGenerator());
		routeLocator = new ApplicationRouteLocator("/", registry, "/proxied");
		routeLocator.resetRoutes();
		filter = new PreDecorationFilter(routeLocator, true);
		RequestContext ctx = RequestContext.getCurrentContext();
		ctx.clear();
		ctx.setRequest(request);
	}

	@Test
	public void basicProperties() throws Exception {
		assertEquals(5, this.filter.filterOrder());
		assertEquals(true, this.filter.shouldFilter());
		assertEquals("pre", this.filter.filterType());
	}

	@Test
	public void prefixRouteAddsHeader() throws Exception {
		request.setRequestURI("/proxied/-id-/foo");
		filter.run();
		RequestContext ctx = RequestContext.getCurrentContext();
		assertEquals("/foo", ctx.get("requestURI"));
		assertEquals("http://mgmt", ctx.getRouteHost().toString());
		assertEquals("localhost:80", ctx.getZuulRequestHeaders().get("x-forwarded-host"));
		assertEquals("http", ctx.getZuulRequestHeaders().get("x-forwarded-proto"));
		assertEquals("/proxied/-id-", ctx.getZuulRequestHeaders().get("x-forwarded-prefix"));
	}
}
