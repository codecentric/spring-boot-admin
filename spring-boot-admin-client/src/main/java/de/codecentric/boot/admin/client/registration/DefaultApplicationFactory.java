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
import java.net.UnknownHostException;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.boot.web.server.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import de.codecentric.boot.admin.client.config.InstanceProperties;
import de.codecentric.boot.admin.client.registration.metadata.MetadataContributor;

/**
 * Default implementation for creating the {@link Application} instance which gets
 * registered at the admin server.
 *
 * @author Johannes Edmeier
 * @author Rene Felgenträger
 */
public class DefaultApplicationFactory extends AbstractApplicationFactory {

	private final ServerProperties server;

	private final ManagementServerProperties management;

	@Nullable private Integer localServerPort;

	@Nullable private Integer localManagementPort;

	public DefaultApplicationFactory(InstanceProperties instance, ManagementServerProperties management,
			ServerProperties server, PathMappedEndpoints pathMappedEndpoints, WebEndpointProperties webEndpoint,
			MetadataContributor metadataContributor) {
		super(instance, pathMappedEndpoints, webEndpoint, metadataContributor);
		this.management = management;
		this.server = server;
	}

	@Override
	protected String getServiceBaseUrl() {
		String baseUrl = getInstance().getServiceBaseUrl();

		if (StringUtils.hasText(baseUrl)) {
			return baseUrl;
		}

		return UriComponentsBuilder.newInstance()
			.scheme(getScheme(this.server.getSsl()))
			.host(getServiceHost())
			.port(getLocalServerPort())
			.toUriString();
	}

	@Override
	protected String getManagementBaseUrl() {
		String baseUrl = getInstance().getManagementBaseUrl();

		if (StringUtils.hasText(baseUrl)) {
			return baseUrl;
		}

		if (isManagementPortEqual()) {
			return this.getServiceUrl();
		}

		Ssl ssl = (this.management.getSsl() != null) ? this.management.getSsl() : this.server.getSsl();
		return UriComponentsBuilder.newInstance()
			.scheme(getScheme(ssl))
			.host(getManagementHost())
			.port(getLocalManagementPort())
			.toUriString();
	}

	protected boolean isManagementPortEqual() {
		return this.localManagementPort == null || this.localManagementPort.equals(this.localServerPort);
	}

	protected String getServiceHost() {
		InetAddress address = this.server.getAddress();
		if (address == null) {
			address = getLocalHost();
		}
		return getHost(address);
	}

	protected String getManagementHost() {
		InetAddress address = this.management.getAddress();
		if (address != null) {
			return getHost(address);
		}
		return getServiceHost();
	}

	protected InetAddress getLocalHost() {
		try {
			return InetAddress.getLocalHost();
		}
		catch (UnknownHostException ex) {
			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}

	protected Integer getLocalServerPort() {
		if (this.localServerPort == null) {
			throw new IllegalStateException(
					"couldn't determine local port. Please set spring.boot.admin.client.instance.service-base-url.");
		}
		return this.localServerPort;
	}

	protected Integer getLocalManagementPort() {
		if (this.localManagementPort == null) {
			return this.getLocalServerPort();
		}
		return this.localManagementPort;
	}

	protected String getScheme(@Nullable Ssl ssl) {
		return ((ssl != null) && ssl.isEnabled()) ? "https" : "http";
	}

	protected String getHost(InetAddress address) {
		if (getInstance().isPreferIp()) {
			return address.getHostAddress();
		}

		return switch (getInstance().getServiceHostType()) {
			case IP -> address.getHostAddress();
			case HOST_NAME -> address.getHostName();
			default -> address.getCanonicalHostName();
		};
	}

	@EventListener
	public void onWebServerInitialized(WebServerInitializedEvent event) {
		String name = event.getApplicationContext().getServerNamespace();
		if ("server".equals(name) || !StringUtils.hasText(name)) {
			this.localServerPort = event.getWebServer().getPort();
		}
		else if ("management".equals(name)) {
			this.localManagementPort = event.getWebServer().getPort();
		}
	}

}
