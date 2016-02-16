package de.codecentric.boot.admin.discovery.eureka;

import de.codecentric.boot.admin.discovery.ApplicationDiscoveryListener;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.registry.ApplicationRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient;

/**
 * Created by alexf on 15-Feb-16.
 */
public class EurekaApplicationDiscoveryListener extends ApplicationDiscoveryListener{
    @Autowired
    public EurekaApplicationDiscoveryListener(DiscoveryClient discoveryClient, ApplicationRegistry registry) {
        super(discoveryClient, registry);
    }

    @Override
    protected Application convert(ServiceInstance instance) {
        EurekaDiscoveryClient.EurekaServiceInstance eurekaServiceInstance = (EurekaDiscoveryClient.EurekaServiceInstance) instance;
        final Application converted = super.convert(instance);
        return Application.create(converted)
                .withHealthUrl(eurekaServiceInstance.getInstanceInfo().getHealthCheckUrl())
                .withManagementUrl(eurekaServiceInstance.getInstanceInfo().getHomePageUrl())
                .withServiceUrl(eurekaServiceInstance.getInstanceInfo().getHomePageUrl())
                .build();
    }
}
