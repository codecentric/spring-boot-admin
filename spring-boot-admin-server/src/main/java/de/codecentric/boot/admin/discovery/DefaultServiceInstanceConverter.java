/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.codecentric.boot.admin.discovery;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang.StringUtils.stripStart;

import java.net.URI;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.web.util.UriComponentsBuilder;

import de.codecentric.boot.admin.model.Application;

/**
 * Converts any {@link ServiceInstance}s to {@link Application}s. To customize the health- or
 * management-url for all applications you can set healthEndpointPath or managementContextPath
 * respectively. If you want to influence the url per service you can add
 * <code>management.context-path</code> or <code>management.port</code> or <code>health.path</code>
 * to the instances metadata.
 *
 * @author Johannes Edmeier
 */
public class DefaultServiceInstanceConverter implements ServiceInstanceConverter {
	private static final String KEY_MANAGEMENT_PORT = "management.port";
	private static final String KEY_MANAGEMENT_PATH = "management.context-path";
	private static final String KEY_HEALTH_PATH = "health.path";
	private String managementContextPath = "";
	private String healthEndpointPath = "health";

	@Override
	public Application convert(ServiceInstance instance) {
		Application.Builder builder = Application.create(instance.getServiceId());
		URI healthUrl = getHealthUrl(instance);
		if (healthUrl != null) {
			builder.withHealthUrl(healthUrl.toString());
		}

		URI managementUrl = getManagementUrl(instance);
		if (managementUrl != null) {
			builder.withManagementUrl(managementUrl.toString());
		}

		URI serviceUrl = getServiceUrl(instance);
		if (serviceUrl != null) {
			builder.withServiceUrl(serviceUrl.toString());
		}

		return builder.build();
	}

	protected URI getHealthUrl(ServiceInstance instance) {
		String healthPath = defaultIfEmpty(instance.getMetadata().get(KEY_HEALTH_PATH),
				healthEndpointPath);
		healthPath = stripStart(healthPath, "/");

		return UriComponentsBuilder.fromUri(getManagementUrl(instance)).pathSegment(healthPath)
				.build().toUri();
	}

	protected URI getManagementUrl(ServiceInstance instance) {
		String managamentPath = defaultIfEmpty(instance.getMetadata().get(KEY_MANAGEMENT_PATH),
				managementContextPath);
		managamentPath = stripStart(managamentPath, "/");

		URI serviceUrl = getServiceUrl(instance);
		String managamentPort = defaultIfEmpty(instance.getMetadata().get(KEY_MANAGEMENT_PORT),
				String.valueOf(serviceUrl.getPort()));

		return UriComponentsBuilder.fromUri(serviceUrl).port(managamentPort)
				.pathSegment(managamentPath).build().toUri();
	}

	protected URI getServiceUrl(ServiceInstance instance) {
		return instance.getUri();
	}

	/**
	 * Default <code>management.context-path</code> to be appended to the url of the discovered
	 * service for the managment-url.
	 *
	 * @param managementContextPath the management context-path.
	 */
	public void setManagementContextPath(String managementContextPath) {
		this.managementContextPath = managementContextPath;
	}

	/**
	 * Default path of the health-endpoint to be used for the health-url of the discovered service.
	 *
	 * @param healthEndpointPath the path for the health-endpoint.
	 */
	public void setHealthEndpointPath(String healthEndpointPath) {
		this.healthEndpointPath = healthEndpointPath;
	}
}
