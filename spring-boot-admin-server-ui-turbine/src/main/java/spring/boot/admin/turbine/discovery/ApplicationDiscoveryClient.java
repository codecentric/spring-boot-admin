package spring.boot.admin.turbine.discovery;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryProperties.SimpleServiceInstance;
import org.springframework.cloud.netflix.turbine.SpringAggregatorFactory;
import org.springframework.context.event.EventListener;
import org.springframework.util.StringUtils;

import com.netflix.config.ConfigurationManager;
import com.netflix.turbine.discovery.InstanceDiscovery;
import com.netflix.turbine.plugins.PluginsFactory;

import de.codecentric.boot.admin.discovery.ApplicationDiscoveryListener;
import de.codecentric.boot.admin.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.model.Application;

/**
 * Create a discovery client which will discover the applications registered within Spring Boot Admin
 *
 * @author Jérôme Mirc
 */
public class ApplicationDiscoveryClient implements DiscoveryClient {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ApplicationDiscoveryListener.class);


    private Map<String, List<ServiceInstance>> instances = new HashMap<>();

    @EventListener
    public void registerApplication(ClientApplicationRegisteredEvent clientApplicationRegisteredEvent) {
        Application application = clientApplicationRegisteredEvent.getApplication();
        String serviceId = application.getName();

        if (!instances.containsKey(serviceId)) {
            instances.put(serviceId, new ArrayList<ServiceInstance>());
        }

        ServiceInstance serviceInstance = marshall(application);

        if (serviceInstance != null) {
            instances.get(serviceId).add(serviceInstance);
        }

        // Register each registered applications as a turbine's cluster
        registerTurbineClusters();
    }

    @Override
    public String description() {
        return "Spring Boot Monitoring Instances Discovery";
    }

    @Override
    public ServiceInstance getLocalServiceInstance() {
        return null;
    }

    @Override
    public List<ServiceInstance> getInstances(String serviceId) {
        return instances.get(serviceId);

    }

    @Override
    public List<String> getServices() {
        return new ArrayList<>(instances.keySet());
    }

    /**
     * Private helper that marshals the information from each instance into something that
     * Turbine can understand.
     *
     * @return Instance
     */
    private ServiceInstance marshall(Application application) {
        try {
            URL serviceURL = new URL(application.getServiceUrl());
            return new SimpleServiceInstance(serviceURL.toURI().toString());
        } catch (Exception e) {
            LOGGER.error("Failed to parse the URL {}", application.getServiceUrl(), e);
        }

        return null;
    }

    /**
     *  Turbine clusters are registered dynamically
     */
    private void registerTurbineClusters() {
        ConfigurationManager
                .getConfigInstance()
                .setProperty(InstanceDiscovery.TURBINE_AGGREGATOR_CLUSTER_CONFIG, StringUtils.collectionToCommaDelimitedString(getServices()));

        PluginsFactory.getClusterMonitorFactory().initClusterMonitors();
    }
}
