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
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.boot.webmvc.autoconfigure.DispatcherServletPath;
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

	public ServletApplicationFactory(final InstanceProperties instance, final ManagementServerProperties management,
			final ServerProperties server, final ServletContext servletContext,
			final PathMappedEndpoints pathMappedEndpoints, final WebEndpointProperties webEndpoint,
			final MetadataContributor metadataContributor, final DispatcherServletPath dispatcherServletPath) {
		super(instance, management, server, pathMappedEndpoints, webEndpoint, metadataContributor);
		this.servletContext = servletContext;
		this.server = server;
		this.management = management;
		this.instance = instance;
		this.dispatcherServletPath = dispatcherServletPath;
	}

	@Override
	protected String getServiceUrl() {
		if (this.instance.getServiceUrl() != null) {
			return this.instance.getServiceUrl();
		}

		return UriComponentsBuilder.fromUriString(getServiceBaseUrl())
			.path(getServicePath())
			.path(getServerContextPath())
			.toUriString();
	}

	@Override
	protected String getManagementBaseUrl() {
		final String baseUrl = this.instance.getManagementBaseUrl();

		if (StringUtils.hasText(baseUrl)) {
			return baseUrl;
		}

		if (isManagementPortEqual()) {
			return UriComponentsBuilder.fromUriString(getServiceUrl())
				.path("/")
				.path(getDispatcherServletPrefix())
				.path(getManagementContextPath())
				.toUriString();
		}

		final Ssl ssl = (this.management.getSsl() != null) ? this.management.getSsl() : this.server.getSsl();
		return UriComponentsBuilder.newInstance()
			.scheme(getScheme(ssl))
			.host(getManagementHost())
			.port(getLocalManagementPort())
			.path(getManagementContextPath())
			.toUriString();
	}

	protected String getManagementContextPath() {
		return this.management.getBasePath();
	}

	protected String getServerContextPath() {
		return this.servletContext.getContextPath();
	}

	protected String getDispatcherServletPrefix() {
		return this.dispatcherServletPath.getPrefix();
	}

}
