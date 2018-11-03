/*
 * Copyright 2014-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.server.cloud.discovery;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.values.Registration;

import java.net.URI;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.web.util.UriComponentsBuilder;

import static java.util.Collections.emptyMap;
import static org.springframework.util.StringUtils.isEmpty;

/**
 * Converts any {@link ServiceInstance}s to {@link Instance}s. To customize the health- or
 * management-url for all instances you can set healthEndpointPath or managementContextPath
 * respectively. If you want to influence the url per service you can add
 * <code>management.context-path</code>, <code>management.port</code>, <code>management.address</code> or <code>health.path</code>
 * to the instances metadata.
 *
 * @author Johannes Edmeier
 */
public class DefaultServiceInstanceConverter implements ServiceInstanceConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServiceInstanceConverter.class);
    private static final String KEY_MANAGEMENT_PORT = "management.port";
    private static final String KEY_MANAGEMENT_PATH = "management.context-path";
    private static final String KEY_MANAGEMENT_ADDRESS = "management.address";
    private static final String KEY_HEALTH_PATH = "health.path";

    /**
     * Default context-path to be appended to the url of the discovered service for the
     * managment-url.
     */
    private String managementContextPath = "/actuator";
    /**
     * Default path of the health-endpoint to be used for the health-url of the discovered service.
     */
    private String healthEndpointPath = "health";

    @Override
    public Registration convert(ServiceInstance instance) {
        LOGGER.debug(
            "Converting service '{}' running at '{}' with metadata {}",
            instance.getServiceId(),
            instance.getUri(),
            instance.getMetadata()
        );

        return Registration.create(instance.getServiceId(), getHealthUrl(instance).toString())
                           .managementUrl(getManagementUrl(instance).toString())
                           .serviceUrl(getServiceUrl(instance).toString())
                           .metadata(getMetadata(instance))
                           .build();
    }

    protected URI getHealthUrl(ServiceInstance instance) {
        String healthPath = instance.getMetadata().get(KEY_HEALTH_PATH);
        if (isEmpty(healthPath)) {
            healthPath = healthEndpointPath;
        }

        return UriComponentsBuilder.fromUri(getManagementUrl(instance)).path("/").path(healthPath).build().toUri();
    }

    protected URI getManagementUrl(ServiceInstance instance) {
        String managementPath = instance.getMetadata().get(KEY_MANAGEMENT_PATH);
        if (isEmpty(managementPath)) {
            managementPath = managementContextPath;
        }

        URI serviceUrl = getServiceUrl(instance);

        String managementServerAddress = instance.getMetadata().get(KEY_MANAGEMENT_ADDRESS);
        if (isEmpty(managementServerAddress)) {
            managementServerAddress = serviceUrl.getHost();
        }

        String managementPort = instance.getMetadata().get(KEY_MANAGEMENT_PORT);
        if (isEmpty(managementPort)) {
            managementPort = String.valueOf(serviceUrl.getPort());
        }

        return UriComponentsBuilder.fromUri(serviceUrl)
                                   .host(managementServerAddress)
                                   .port(managementPort)
                                   .path("/")
                                   .path(managementPath)
                                   .build()
                                   .toUri();
    }

    protected URI getServiceUrl(ServiceInstance instance) {
        return UriComponentsBuilder.fromUri(instance.getUri()).path("/").build().toUri();
    }

    protected Map<String, String> getMetadata(ServiceInstance instance) {
        return instance.getMetadata() != null ? instance.getMetadata() : emptyMap();
    }


    public void setManagementContextPath(String managementContextPath) {
        this.managementContextPath = managementContextPath;
    }

    public String getManagementContextPath() {
        return managementContextPath;
    }

    public void setHealthEndpointPath(String healthEndpointPath) {
        this.healthEndpointPath = healthEndpointPath;
    }

    public String getHealthEndpointPath() {
        return healthEndpointPath;
    }
}
