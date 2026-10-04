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

package de.codecentric.boot.admin.client.registration;

import java.net.InetAddress;

import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.boot.webflux.autoconfigure.WebFluxProperties;
import org.springframework.cloud.commons.util.InetUtils;

import de.codecentric.boot.admin.client.config.InstanceProperties;
import de.codecentric.boot.admin.client.registration.metadata.MetadataContributor;

public class InetUtilsReactiveApplicationFactory extends ReactiveApplicationFactory {

	private final InetUtils inetUtils;

	public InetUtilsReactiveApplicationFactory(InstanceProperties instance, ManagementServerProperties management,
			ServerProperties server, PathMappedEndpoints pathMappedEndpoints, WebEndpointProperties webEndpoint,
			MetadataContributor metadataContributor, WebFluxProperties webFluxProperties, InetUtils inetUtils) {
		super(instance, management, server, pathMappedEndpoints, webEndpoint, metadataContributor, webFluxProperties);
		this.inetUtils = inetUtils;
	}

	@Override
	protected InetAddress getLocalHost() {
		InetAddress address = this.inetUtils.findFirstNonLoopbackAddress();
		return (address != null) ? address : super.getLocalHost();
	}

}
