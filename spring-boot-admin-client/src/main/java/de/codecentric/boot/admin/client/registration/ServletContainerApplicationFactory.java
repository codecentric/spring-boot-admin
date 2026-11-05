/*
 * Copyright 2014-2026 the original author or authors.
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

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.ServletContext;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.endpoint.EndpointId;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.webmvc.autoconfigure.DispatcherServletPath;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import de.codecentric.boot.admin.client.config.InstanceProperties;
import de.codecentric.boot.admin.client.registration.metadata.MetadataContributor;

/**
 * {@link ApplicationFactory} for servlet applications deployed to an external servlet
 * container without Spring Boot's embedded web server infrastructure
 * ({@code spring-boot-web-server}) on the classpath. The container port is unknown to the
 * application, so {@code spring.boot.admin.client.instance.service-base-url} (or
 * {@code service-url}) must be set.
 */
public class ServletContainerApplicationFactory implements ApplicationFactory {

	private final InstanceProperties instance;

	private final ServletContext servletContext;

	private final PathMappedEndpoints pathMappedEndpoints;

	private final WebEndpointProperties webEndpoint;

	private final MetadataContributor metadataContributor;

	private final DispatcherServletPath dispatcherServletPath;

	public ServletContainerApplicationFactory(InstanceProperties instance, ServletContext servletContext,
			PathMappedEndpoints pathMappedEndpoints, WebEndpointProperties webEndpoint,
			MetadataContributor metadataContributor, DispatcherServletPath dispatcherServletPath) {
		this.instance = instance;
		this.servletContext = servletContext;
		this.pathMappedEndpoints = pathMappedEndpoints;
		this.webEndpoint = webEndpoint;
		this.metadataContributor = metadataContributor;
		this.dispatcherServletPath = dispatcherServletPath;
	}

	@Override
	public Application createApplication() {
		return Application.create(this.instance.getName())
			.healthUrl(getHealthUrl())
			.managementUrl(getManagementUrl())
			.serviceUrl(getServiceUrl())
			.metadata(getMetadata())
			.build();
	}

	protected String getServiceUrl() {
		if (this.instance.getServiceUrl() != null) {
			return this.instance.getServiceUrl();
		}
		String baseUrl = this.instance.getServiceBaseUrl();
		if (!StringUtils.hasText(baseUrl)) {
			throw new IllegalStateException(
					"spring.boot.admin.client.instance.service-base-url must be set when deployed to an external servlet container.");
		}
		String servicePath = StringUtils.hasText(this.instance.getServicePath()) ? this.instance.getServicePath() : "/";
		return UriComponentsBuilder.fromUriString(baseUrl)
			.path(servicePath)
			.path(this.servletContext.getContextPath())
			.toUriString();
	}

	protected String getManagementBaseUrl() {
		String baseUrl = this.instance.getManagementBaseUrl();
		if (StringUtils.hasText(baseUrl)) {
			return baseUrl;
		}
		return UriComponentsBuilder.fromUriString(getServiceUrl())
			.path("/")
			.path(this.dispatcherServletPath.getPrefix())
			.toUriString();
	}

	protected String getManagementUrl() {
		if (this.instance.getManagementUrl() != null) {
			return this.instance.getManagementUrl();
		}
		return UriComponentsBuilder.fromUriString(getManagementBaseUrl())
			.path("/")
			.path(this.webEndpoint.getBasePath())
			.toUriString();
	}

	protected String getHealthUrl() {
		if (this.instance.getHealthUrl() != null) {
			return this.instance.getHealthUrl();
		}
		return UriComponentsBuilder.fromUriString(getManagementBaseUrl())
			.path("/")
			.path(getHealthEndpointPath())
			.toUriString();
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

	protected Map<String, String> getMetadata() {
		Map<String, String> metadata = new LinkedHashMap<>();
		metadata.putAll(this.metadataContributor.getMetadata());
		metadata.putAll(this.instance.getMetadata());
		return metadata;
	}

}
