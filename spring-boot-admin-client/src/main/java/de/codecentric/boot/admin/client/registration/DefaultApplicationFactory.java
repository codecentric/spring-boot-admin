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
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.actuate.endpoint.EndpointId;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.boot.web.server.Ssl;
import org.springframework.context.event.EventListener;
import org.springframework.lang.Nullable;
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
public class DefaultApplicationFactory implements ApplicationFactory {

	private final InstanceProperties instance;

	private final ServerProperties server;

	private final ManagementServerProperties management;

	private final PathMappedEndpoints pathMappedEndpoints;

	private final WebEndpointProperties webEndpoint;

	private final MetadataContributor metadataContributor;

	@Nullable
	private Integer localServerPort;

	@Nullable
	private Integer localManagementPort;

	public DefaultApplicationFactory(InstanceProperties instance, ManagementServerProperties management,
			ServerProperties server, PathMappedEndpoints pathMappedEndpoints, WebEndpointProperties webEndpoint,
			MetadataContributor metadataContributor) {
		this.instance = instance;
		this.management = management;
		this.server = server;
		this.pathMappedEndpoints = pathMappedEndpoints;
		this.webEndpoint = webEndpoint;
		this.metadataContributor = metadataContributor;
	}

	@Override
	public Application createApplication() {
		return Application.create(getName())
			.healthUrl(getHealthUrl())
			.managementUrl(getManagementUrl())
			.serviceUrl(getServiceUrl())
			.metadata(getMetadata())
			.build();
	}

	protected String getName() {
		return this.instance.getName();
	}

	protected String getServiceUrl() {
		if (this.instance.getServiceUrl() != null) {
			return this.instance.getServiceUrl();
		}

		return UriComponentsBuilder.fromUriString(getServiceBaseUrl()).path(getServicePath()).toUriString();
	}

	protected String getServiceBaseUrl() {
		String baseUrl = this.instance.getServiceBaseUrl();

		if (StringUtils.hasText(baseUrl)) {
			return baseUrl;
		}

		return UriComponentsBuilder.newInstance()
			.scheme(getScheme(this.server.getSsl()))
			.host(getServiceHost())
			.port(getLocalServerPort())
			.toUriString();
	}

	protected String getServicePath() {
		String path = this.instance.getServicePath();

		if (StringUtils.hasText(path)) {
			return path;
		}

		return "/";
	}

	protected String getManagementUrl() {
		if (this.instance.getManagementUrl() != null) {
			return this.instance.getManagementUrl();
		}

		return UriComponentsBuilder.fromUriString(getManagementBaseUrl())
			.path("/")
			.path(getEndpointsWebPath())
			.toUriString();
	}

	protected String getManagementBaseUrl() {
		String baseUrl = this.instance.getManagementBaseUrl();

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

	protected String getEndpointsWebPath() {
		return this.webEndpoint.getBasePath();
	}

	protected String getHealthUrl() {
		if (this.instance.getHealthUrl() != null) {
			return this.instance.getHealthUrl();
		}
		return UriComponentsBuilder.fromHttpUrl(getManagementBaseUrl())
			.path("/")
			.path(getHealthEndpointPath())
			.toUriString();
	}

	protected Map<String, String> getMetadata() {
		Map<String, String> metadata = new LinkedHashMap<>();
		metadata.putAll(this.metadataContributor.getMetadata());
		metadata.putAll(this.instance.getMetadata());
		return metadata;
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

	protected String getHealthEndpointPath() {
		String health = this.pathMappedEndpoints.getPath(EndpointId.of("health"));
		if (StringUtils.hasText(health)) {
			return health;
		}
		String status = this.pathMappedEndpoints.getPath(EndpointId.of("status"));
		if (StringUtils.hasText(status)) {
			return status;
		}
		throw new IllegalStateException("Either health or status endpoint must be enabled!");
	}

	protected String getScheme(@Nullable Ssl ssl) {
		return ((ssl != null) && ssl.isEnabled()) ? "https" : "http";
	}

	protected String getHost(InetAddress address) {
		if (this.instance.isPreferIp()) {
			return address.getHostAddress();
		}

		switch (this.instance.getServiceHostType()) {
			case IP:
				return address.getHostAddress();
			case HOST_NAME:
				return address.getHostName();
			case CANONICAL_HOST_NAME:
			default:
				return address.getCanonicalHostName();
		}
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
