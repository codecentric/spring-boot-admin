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

import jakarta.servlet.ServletContext;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.boot.webmvc.autoconfigure.DispatcherServletPath;
import org.springframework.cloud.commons.util.InetUtils;

import de.codecentric.boot.admin.client.config.InstanceProperties;
import de.codecentric.boot.admin.client.registration.metadata.MetadataContributor;

public class InetUtilsServletApplicationFactory extends ServletApplicationFactory {

	private final InetUtils inetUtils;

	public InetUtilsServletApplicationFactory(InstanceProperties instance, ManagementServerProperties management,
			ServerProperties server, ServletContext servletContext, PathMappedEndpoints pathMappedEndpoints,
			WebEndpointProperties webEndpoint, MetadataContributor metadataContributor,
			DispatcherServletPath dispatcherServletPath, InetUtils inetUtils) {
		super(instance, management, server, servletContext, pathMappedEndpoints, webEndpoint, metadataContributor,
				dispatcherServletPath);
		this.inetUtils = inetUtils;
	}

	@Override
	protected InetAddress getLocalHost() {
		InetAddress address = this.inetUtils.findFirstNonLoopbackAddress();
		return (address != null) ? address : super.getLocalHost();
	}

}
