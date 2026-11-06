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

import org.jspecify.annotations.Nullable;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.endpoint.EndpointId;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import de.codecentric.boot.admin.client.config.InstanceProperties;
import de.codecentric.boot.admin.client.registration.metadata.MetadataContributor;

/**
 * Base class for creating the {@link Application} from the configured
 * {@link InstanceProperties} and the actuator endpoints. Subclasses determine the service
 * and management base URLs. It references no {@code spring-boot-web-server} types, so it
 * can be used when that module is not on the classpath.
 */
public abstract class AbstractApplicationFactory implements ApplicationFactory {

	private final InstanceProperties instance;

	private final PathMappedEndpoints pathMappedEndpoints;

	private final WebEndpointProperties webEndpoint;

	private final MetadataContributor metadataContributor;

	protected AbstractApplicationFactory(InstanceProperties instance, PathMappedEndpoints pathMappedEndpoints,
			WebEndpointProperties webEndpoint, MetadataContributor metadataContributor) {
		this.instance = instance;
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

	protected InstanceProperties getInstance() {
		return this.instance;
	}

	protected String getName() {
		return this.instance.getName();
	}

	protected String getServiceUrl() {
		if (this.instance.getServiceUrl() != null) {
			return this.instance.getServiceUrl();
		}

		return UriComponentsBuilder.fromUriString(getServiceBaseUrl())
			.path(getServicePath())
			.path(getContextPath())
			.toUriString();
	}

	protected abstract String getServiceBaseUrl();

	protected String getServicePath() {
		String path = this.instance.getServicePath();

		if (StringUtils.hasText(path)) {
			return path;
		}

		return "/";
	}

	/**
	 * Path the application is served under, appended to the service URL.
	 * @return the context path, or {@code null} if there is none
	 */
	protected @Nullable String getContextPath() {
		return null;
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

	protected abstract String getManagementBaseUrl();

	protected String getEndpointsWebPath() {
		return this.webEndpoint.getBasePath();
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
