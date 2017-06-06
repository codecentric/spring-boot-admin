package spring.boot.admin.turbine.discovery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.turbine.CommonsInstanceDiscovery;
import org.springframework.cloud.netflix.turbine.TurbineProperties;
import org.springframework.util.StringUtils;

import com.netflix.turbine.discovery.Instance;

/**
 *  A custom implementation to support Spring Boot Admin {@link de.codecentric.boot.admin.model.Application}.
 *
 * @author Jérôme Mirc
 */
public class ApplicationInstanceDiscovery extends CommonsInstanceDiscovery {

    private final DiscoveryClient discoveryClient;

    public ApplicationInstanceDiscovery(DiscoveryClient discoveryClient) {
        super(new TurbineProperties(), discoveryClient);
        this.discoveryClient = discoveryClient;
    }

    @Override
    public TurbineProperties getTurbineProperties() {
        TurbineProperties turbineProperties = new TurbineProperties();
        List<String> services = discoveryClient.getServices();
        String appConfigList = StringUtils.collectionToCommaDelimitedString(services);
        turbineProperties.setAppConfig(appConfigList);
        return turbineProperties;
    }

    @Override
    protected List<Instance> getInstancesForApp(String serviceId) throws Exception {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);

        if (instances == null) {
            return Collections.emptyList();
        }

        List<Instance> results = new ArrayList<>();

        for (ServiceInstance instance : instances) {
            results.add(convert(serviceId, instance));
        }

        return results;
    }

    private Instance convert(String serviceId, ServiceInstance serviceInstance) {
        Instance instance = getInstance(serviceInstance.getHost(), String.valueOf(serviceInstance.getPort()), serviceId, true);

        instance.getAttributes().put("fusedHostPort", String.format("%s:%d", serviceInstance.getHost(), serviceInstance.getPort()));

        if (serviceInstance.isSecure()) {
            instance.getAttributes().put("securePort", String.valueOf(serviceInstance.getPort()));
        } else {
            instance.getAttributes().put("port", String.valueOf(serviceInstance.getPort()));
        }

        return instance;
    }
}
