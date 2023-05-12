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

import jakarta.servlet.ServletContext;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletPath;
import org.springframework.boot.web.server.Ssl;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import de.codecentric.boot.admin.client.config.InstanceProperties;
import de.codecentric.boot.admin.client.registration.metadata.MetadataContributor;

public class ServletApplicationFactory extends DefaultApplicationFactory {

	private final ServletContext servletContext;

	private final ServerProperties server;

	private final ManagementServerProperties management;

	private final InstanceProperties instance;

	private final DispatcherServletPath dispatcherServletPath;

	public ServletApplicationFactory(InstanceProperties instance, ManagementServerProperties management,
			ServerProperties server, ServletContext servletContext, PathMappedEndpoints pathMappedEndpoints,
			WebEndpointProperties webEndpoint, MetadataContributor metadataContributor,
			DispatcherServletPath dispatcherServletPath) {
		super(instance, management, server, pathMappedEndpoints, webEndpoint, metadataContributor);
		this.servletContext = servletContext;
		this.server = server;
		this.management = management;
		this.instance = instance;
		this.dispatcherServletPath = dispatcherServletPath;
	}

	@Override
	protected String getServiceUrl() {
		if (instance.getServiceUrl() != null) {
			return instance.getServiceUrl();
		}

		return UriComponentsBuilder.fromUriString(getServiceBaseUrl())
			.path(getServicePath())
			.path(getServerContextPath())
			.toUriString();
	}

	@Override
	protected String getManagementBaseUrl() {
		String baseUrl = instance.getManagementBaseUrl();

		if (StringUtils.hasText(baseUrl)) {
			return baseUrl;
		}

		if (isManagementPortEqual()) {
			return UriComponentsBuilder.fromHttpUrl(getServiceUrl())
				.path("/")
				.path(getDispatcherServletPrefix())
				.path(getManagementContextPath())
				.toUriString();
		}

		Ssl ssl = (management.getSsl() != null) ? management.getSsl() : server.getSsl();
		return UriComponentsBuilder.newInstance()
			.scheme(getScheme(ssl))
			.host(getManagementHost())
			.port(getLocalManagementPort())
			.path(getManagementContextPath())
			.toUriString();
	}

	protected String getManagementContextPath() {
		return management.getBasePath();
	}

	protected String getServerContextPath() {
		return servletContext.getContextPath();
	}

	protected String getDispatcherServletPrefix() {
		return this.dispatcherServletPath.getPrefix();
	}

}
