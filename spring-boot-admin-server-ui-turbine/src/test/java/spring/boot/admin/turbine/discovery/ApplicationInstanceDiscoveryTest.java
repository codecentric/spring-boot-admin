package spring.boot.admin.turbine.discovery;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.netflix.turbine.discovery.Instance;
import com.netflix.turbine.plugins.DefaultAggregatorFactory;
import com.netflix.turbine.plugins.PluginsFactory;

import de.codecentric.boot.admin.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.model.Application;

public class ApplicationInstanceDiscoveryTest {

    @Before
    public void init() {
        PluginsFactory.setClusterMonitorFactory(new DefaultAggregatorFactory());
    }

    @Test
    public void registerNoApplication() throws Exception {
        ApplicationDiscoveryClient discoveryClient = new ApplicationDiscoveryClient();

        ApplicationInstanceDiscovery applicationInstanceDiscovery = new ApplicationInstanceDiscovery(discoveryClient);

        assertThat(applicationInstanceDiscovery.getInstanceList(), hasSize(0));
    }

    @Test
    public void registerOneApplication() throws Exception {
        ApplicationDiscoveryClient discoveryClient = new ApplicationDiscoveryClient();

        String serviceId = "test";
        discoveryClient.registerApplication(new ClientApplicationRegisteredEvent(Application
                                                                                         .create(serviceId)
                                                                                         .withServiceUrl("http://localhost:8080")
                                                                                         .withHealthUrl("http://localhost:8080/health")
                                                                                         .build()));

        ApplicationInstanceDiscovery applicationInstanceDiscovery = new ApplicationInstanceDiscovery(discoveryClient);
        assertThat(applicationInstanceDiscovery.getInstanceList(), hasSize(1));

        discoveryClient.registerApplication(new ClientApplicationRegisteredEvent(Application
                                                    .create(serviceId)
                                                    .withServiceUrl("http://localhost:8081")
                                                    .withHealthUrl("http://localhost:8081/health")
                                                    .build()));

        assertThat(applicationInstanceDiscovery.getInstanceList(), hasSize(2));
        assertThat(applicationInstanceDiscovery.getInstancesForApp(serviceId), hasSize(2));

        List<Instance> instances = applicationInstanceDiscovery.getInstancesForApp(serviceId);
        assertThat(instances.get(0).getCluster(), is(serviceId));
        assertThat(instances.get(0).getHostname(), is("localhost:8080"));
        assertThat(instances.get(1).getCluster(), is(serviceId));
        assertThat(instances.get(1).getHostname(), is("localhost:8081"));

    }
}
