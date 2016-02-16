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

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.util.StringUtils;

import de.codecentric.boot.admin.model.Application;

/**
 * Converts any {@link ServiceInstance}s to {@link Application}s.
 *
 * @author Johannes Edmeier
 */
public class DefaultServiceInstanceConverter implements ServiceInstanceConverter {
	private String managementContextPath = "";
	private String healthEndpointPath = "health";

	@Override
	public Application convert(ServiceInstance instance) {
		String serviceUrl = instance.getUri().toString();
		String managementUrl = append(serviceUrl, managementContextPath);
		String healthUrl = append(managementUrl, healthEndpointPath);

		return Application.create(instance.getServiceId()).withHealthUrl(healthUrl)
				.withManagementUrl(managementUrl).withServiceUrl(serviceUrl).build();
	}

	protected final String append(String uri, String path) {
		String baseUri = uri.replaceFirst("/+$", "");
		if (StringUtils.isEmpty(path)) {
			return baseUri;
		}

		String normPath = path.replaceFirst("^/+", "").replaceFirst("/+$", "");
		return baseUri + "/" + normPath;
	}

	/**
	 * <code>management.context-path</code> to be appended to the url of the discovered service for
	 * the managment-url.
	 *
	 * @param managementContextPath the management context-path.
	 */
	public void setManagementContextPath(String managementContextPath) {
		this.managementContextPath = managementContextPath;
	}

	/**
	 * path of the health-endpoint to be used for the health-url of the discovered service.
	 *
	 * @param healthEndpointPath the path for the health-endpoint.
	 */
	public void setHealthEndpointPath(String healthEndpointPath) {
		this.healthEndpointPath = healthEndpointPath;
	}
}
