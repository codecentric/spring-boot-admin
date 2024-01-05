/*
 * Copyright 2014-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.server.cloud.discovery;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.lang.Nullable;
import org.springframework.web.util.UriComponentsBuilder;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.values.Registration;

import static java.util.Collections.emptyMap;
import static org.springframework.util.StringUtils.hasText;

/**
 * Converts any {@link ServiceInstance}s to {@link Instance}s. To customize the health- or
 * management-url for all instances you can set healthEndpointPath or
 * managementContextPath respectively. If you want to influence the url per service you
 * can add <code>management.scheme</code>, <code>management.address</code>,
 * <code>management.port</code>, <code>management.context-path</code> or
 * <code>health.path</code> to the instances metadata.
 *
 * @author Johannes Edmeier
 */
public class DefaultServiceInstanceConverter implements ServiceInstanceConverter {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServiceInstanceConverter.class);

	private static final String[] KEYS_MANAGEMENT_SCHEME = { "management.scheme", "management-scheme" };

	private static final String[] KEYS_MANAGEMENT_ADDRESS = { "management.address", "management-address" };

	private static final String[] KEYS_MANAGEMENT_PORT = { "management.port", "management-port", "port.management" };

	private static final String[] KEYS_MANAGEMENT_PATH = { "management.context-path", "management-context-path" };

	private static final String[] KEYS_HEALTH_PATH = { "health.path", "health-path" };

	/**
	 * Default context-path to be appended to the url of the discovered service for the
	 * managment-url.
	 */
	private String managementContextPath = "/actuator";

	/**
	 * Default path of the health-endpoint to be used for the health-url of the discovered
	 * service.
	 */
	private String healthEndpointPath = "health";

	protected static @Nullable String getMetadataValue(ServiceInstance instance, String... keys) {
		Map<String, String> metadata = instance.getMetadata();
		for (String key : keys) {
			String value = metadata.get(key);
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	@Override
	public Registration convert(ServiceInstance instance) {
		LOGGER.debug("Converting service '{}' running at '{}' with metadata {}", instance.getServiceId(),
				instance.getUri(), instance.getMetadata());

		String healthUrl = getHealthUrl(instance).toString();
		String managementUrl = getManagementUrl(instance).toString();
		String serviceUrl = getServiceUrl(instance).toString();

		return Registration.create(instance.getServiceId(), healthUrl)
			.managementUrl(managementUrl)
			.serviceUrl(serviceUrl)
			.metadata(getMetadata(instance))
			.build();
	}

	protected URI getHealthUrl(ServiceInstance instance) {
		return UriComponentsBuilder.fromUri(getManagementUrl(instance))
			.path("/")
			.path(getHealthPath(instance))
			.build()
			.toUri();
	}

	protected String getHealthPath(ServiceInstance instance) {
		String healthPath = getMetadataValue(instance, KEYS_HEALTH_PATH);
		if (hasText(healthPath)) {
			return healthPath;
		}
		return this.healthEndpointPath;
	}

	protected URI getManagementUrl(ServiceInstance instance) {
		URI serviceUrl = this.getServiceUrl(instance);
		String managementScheme = this.getManagementScheme(instance);
		String managementHost = this.getManagementHost(instance);
		int managementPort = this.getManagementPort(instance);

		UriComponentsBuilder builder;
		if (serviceUrl.getHost().equals(managementHost) && serviceUrl.getScheme().equals(managementScheme)
				&& serviceUrl.getPort() == managementPort) {
			builder = UriComponentsBuilder.fromUri(serviceUrl);
		}
		else {
			builder = UriComponentsBuilder.newInstance().scheme(managementScheme).host(managementHost);
			if (managementPort != -1) {
				builder.port(managementPort);
			}
		}

		return builder.path("/").path(getManagementPath(instance)).build().toUri();
	}

	private String getManagementScheme(ServiceInstance instance) {
		String managementServerScheme = getMetadataValue(instance, KEYS_MANAGEMENT_SCHEME);
		if (hasText(managementServerScheme)) {
			return managementServerScheme;
		}
		return getServiceUrl(instance).getScheme();
	}

	protected String getManagementHost(ServiceInstance instance) {
		String managementServerHost = getMetadataValue(instance, KEYS_MANAGEMENT_ADDRESS);
		if (hasText(managementServerHost)) {
			return managementServerHost;
		}
		return getServiceUrl(instance).getHost();
	}

	protected int getManagementPort(ServiceInstance instance) {
		String managementPort = getMetadataValue(instance, KEYS_MANAGEMENT_PORT);
		if (hasText(managementPort)) {
			return Integer.parseInt(managementPort);
		}
		return getServiceUrl(instance).getPort();
	}

	protected String getManagementPath(ServiceInstance instance) {
		String managementPath = getMetadataValue(instance, KEYS_MANAGEMENT_PATH);
		if (hasText(managementPath)) {
			return managementPath;
		}
		return this.managementContextPath;
	}

	protected URI getServiceUrl(ServiceInstance instance) {
		return instance.getUri();
	}

	protected Map<String, String> getMetadata(ServiceInstance instance) {
		return (instance.getMetadata() != null) ? instance.getMetadata()
			.entrySet()
			.stream()
			.filter((e) -> e.getKey() != null && e.getValue() != null)
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)) : emptyMap();
	}

	public String getManagementContextPath() {
		return this.managementContextPath;
	}

	public void setManagementContextPath(String managementContextPath) {
		this.managementContextPath = managementContextPath;
	}

	public String getHealthEndpointPath() {
		return this.healthEndpointPath;
	}

	public void setHealthEndpointPath(String healthEndpointPath) {
		this.healthEndpointPath = healthEndpointPath;
	}

}
