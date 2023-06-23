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

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.kubernetes.commons.discovery.KubernetesDiscoveryProperties;

import static org.springframework.util.StringUtils.hasText;

public class KubernetesServiceInstanceConverter extends DefaultServiceInstanceConverter {

	public static final String MANAGEMENT_PORT_NAME = "management";

	private final String portsPrefix;

	public KubernetesServiceInstanceConverter(KubernetesDiscoveryProperties discoveryProperties) {
		if (discoveryProperties.metadata() != null && discoveryProperties.metadata().portsPrefix() != null) {
			this.portsPrefix = discoveryProperties.metadata().portsPrefix();
		}
		else {
			this.portsPrefix = "";
		}
	}

	@Override
	protected int getManagementPort(ServiceInstance instance) {
		// the DiscoveryClient implementation using Kubernetes Client
		// (KubernetesInformerDiscoveryClient) currently ignores
		// the portsPrefix from KubernetesDiscoveryProperties
		String managementPort = getMetadataValue(instance, portsPrefix + MANAGEMENT_PORT_NAME, MANAGEMENT_PORT_NAME);
		if (hasText(managementPort)) {
			return Integer.parseInt(managementPort);
		}
		return super.getManagementPort(instance);
	}

}
