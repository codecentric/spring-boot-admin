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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TurbineRouteLocatorTest {

    @Test
    public void test_route_http_location() {
        ZuulRoute turbineRoute = new ZuulRoute("/path/**", "http://example.com/target");
        TurbineRouteLocator locator = new TurbineRouteLocator(turbineRoute, "", new ZuulProperties(), null);

        assertThat(locator.getRoutes()).hasSize(1);

        Route route = locator.getRoutes().get(0);
        assertThat(route.getPath()).isEqualTo("/**");
        assertThat(route.getPrefix()).isEqualTo("/path");
        assertThat(route.getLocation()).isEqualTo("http://example.com/target");

        Route matchingRoute = locator.getMatchingRoute("/path/foo");
        assertThat(matchingRoute.getLocation()).isEqualTo("http://example.com/target");
        assertThat(matchingRoute.getPath()).isEqualTo("/foo/turbine.stream");

        assertThat(locator.getMatchingRoute("/404/foo")).isNull();
    }

    @Test
    public void test_route_service_location() {
        ZuulRoute route = new ZuulRoute("/path/**", "turbine");
        DiscoveryClient discovery = mock(DiscoveryClient.class);
        when(discovery.getInstances("turbine")).thenReturn(
                Arrays.<ServiceInstance>asList(new DefaultServiceInstance("turbine", "example.com", 80, false)));

        TurbineRouteLocator locator = new TurbineRouteLocator(route, "", new ZuulProperties(), discovery);

        Route matchingRoute = locator.getMatchingRoute("/path/foo");
        assertThat(matchingRoute.getLocation()).isEqualTo("http://example.com:80");
        assertThat(matchingRoute.getPath()).isEqualTo("/foo/turbine.stream");
    }

    @Test(expected = IllegalStateException.class)
    public void test_route_noservice() {
        ZuulRoute route = new ZuulRoute("/path/**", "turbine");
        DiscoveryClient discovery = new SimpleDiscoveryClient(new SimpleDiscoveryProperties());
        TurbineRouteLocator locator = new TurbineRouteLocator(route, "", new ZuulProperties(), discovery);

        locator.getMatchingRoute("/path/foo");
    }
}
