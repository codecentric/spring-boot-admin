package spring.boot.admin.turbine.zuul.filters;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties.ZuulRoute;

public class StaticRouteLocatorTest {

	@Test
	public void test_route() {

		List<ZuulRoute> routes = Collections
				.singletonList(new ZuulRoute("/path/**", "http://example.com/target"));
		StaticRouteLocator locator = new StaticRouteLocator(routes, "", new ZuulProperties());

		assertThat(locator.getRoutes().size(), is(1) );
		assertThat(locator.getRoutes().get(0).getPath(), is("/**"));
		assertThat(locator.getRoutes().get(0).getPrefix(), is("/path"));
		assertThat(locator.getRoutes().get(0).getLocation(), is( "http://example.com/target"));

		assertThat(locator.getMatchingRoute("/path/foo").getLocation(),
				is("http://example.com/target"));

		assertThat(locator.getMatchingRoute("/404/foo"), nullValue());
	}
}
