package spring.boot.admin.turbine.discovery;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cloud.client.ServiceInstance;

import com.netflix.turbine.plugins.DefaultAggregatorFactory;
import com.netflix.turbine.plugins.PluginsFactory;

import de.codecentric.boot.admin.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.model.Application;

public class ApplicationDiscoveryClientTest {

    @Before
    public void init() {
        PluginsFactory.setClusterMonitorFactory(new DefaultAggregatorFactory());
    }

    @Test
    public void registerNoApplication() {
        ApplicationDiscoveryClient applicationDiscoveryClient = new ApplicationDiscoveryClient();
        assertThat(applicationDiscoveryClient.getServices(), Matchers.<String>empty());
    }

    @Test
    public void registerOneApplication() {
        ApplicationDiscoveryClient discoveryClient = new ApplicationDiscoveryClient();

        String serviceId = "test";
        discoveryClient.registerApplication(new ClientApplicationRegisteredEvent(Application
                                                                                         .create(serviceId)
                                                                                         .withServiceUrl("http://localhost:8080")
                                                                                         .withHealthUrl("http://localhost:8080/health")
                                                                                         .build()));

        assertThat(discoveryClient.getServices(), hasSize(1));
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
        assertThat(instances, notNullValue());
        assertThat(instances, hasSize(1));

        ServiceInstance serviceInstance = instances.get(0);
        assertThat(serviceInstance.getHost(), is("localhost"));
        assertThat(serviceInstance.getPort(), is(8080));
    }
}
