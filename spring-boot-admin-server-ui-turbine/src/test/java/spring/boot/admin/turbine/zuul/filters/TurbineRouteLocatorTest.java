package spring.boot.admin.turbine.zuul.filters;

import java.util.Arrays;
import org.junit.Test;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClient;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryProperties;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties.ZuulRoute;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TurbineRouteLocatorTest {

    @Test
    public void test_route_http_location() {
        ZuulRoute route = new ZuulRoute("/path/**", "http://example.com/target");
        TurbineRouteLocator locator = new TurbineRouteLocator(route, "", new ZuulProperties(), null);

        assertThat(locator.getRoutes().size(), is(1));
        assertThat(locator.getRoutes().get(0).getPath(), is("/**"));
        assertThat(locator.getRoutes().get(0).getPrefix(), is("/path"));
        assertThat(locator.getRoutes().get(0).getLocation(), is("http://example.com/target"));

        Route matchingRoute = locator.getMatchingRoute("/path/foo");
        assertThat(matchingRoute.getLocation(), is("http://example.com/target"));
        assertThat(matchingRoute.getPath(), is("/foo/turbine.stream"));

        assertThat(locator.getMatchingRoute("/404/foo"), nullValue());
    }

    @Test
    public void test_route_service_location() {
        ZuulRoute route = new ZuulRoute("/path/**", "turbine");
        DiscoveryClient discovery = mock(DiscoveryClient.class);
        when(discovery.getInstances("turbine")).thenReturn(
                Arrays.<ServiceInstance>asList(new DefaultServiceInstance("turbine", "example.com", 80, false)));

        TurbineRouteLocator locator = new TurbineRouteLocator(route, "", new ZuulProperties(), discovery);

        Route matchingRoute = locator.getMatchingRoute("/path/foo");
        assertThat(matchingRoute.getLocation(), is("http://example.com:80"));
        assertThat(matchingRoute.getPath(), is("/foo/turbine.stream"));
    }

    @Test(expected = IllegalStateException.class)
    public void test_route_noservice() {
        ZuulRoute route = new ZuulRoute("/path/**", "turbine");
        DiscoveryClient discovery = new SimpleDiscoveryClient(new SimpleDiscoveryProperties());
        TurbineRouteLocator locator = new TurbineRouteLocator(route, "", new ZuulProperties(), discovery);

        locator.getMatchingRoute("/path/foo");
    }
}
