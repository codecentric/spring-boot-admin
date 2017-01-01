package de.codecentric.boot.admin.zuul.filters.pre;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;

import com.netflix.zuul.context.RequestContext;

import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.web.client.HttpHeadersProvider;
import de.codecentric.boot.admin.zuul.ApplicationRouteLocator;
import de.codecentric.boot.admin.zuul.ApplicationRouteLocator.ApplicationRoute;

public class ApplicationHeadersFilterTest {
	private ApplicationRouteLocator routeLocator = mock(ApplicationRouteLocator.class);
	private HttpHeadersProvider headerProvider = mock(HttpHeadersProvider.class);
	private ApplicationHeadersFilter filter = new ApplicationHeadersFilter(headerProvider,
			routeLocator);

	@Test
	public void test_add_headers_on_matching_route_only() {
		Application application = Application.create("test").withHealthUrl("/health").build();
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("test", "qwertz");

		when(routeLocator.getMatchingRoute("/health"))
				.thenReturn(new ApplicationRoute(application, null, null, null, null));
		when(headerProvider.getHeaders(application)).thenReturn(httpHeaders);

		RequestContext context = creteRequestContext("/health");
		RequestContext.testSetCurrentContext(context);
		filter.run();
		assertThat(context.getZuulRequestHeaders()).containsEntry("test", "qwertz");

		context = creteRequestContext("/foobar");
		RequestContext.testSetCurrentContext(context);
		filter.run();
		assertThat(context.getZuulRequestHeaders()).doesNotContainKey("test");
	}

	private RequestContext creteRequestContext(String uri) {
		RequestContext context = new RequestContext();
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI(uri);
		context.setRequest(request);
		return context;
	}

}
