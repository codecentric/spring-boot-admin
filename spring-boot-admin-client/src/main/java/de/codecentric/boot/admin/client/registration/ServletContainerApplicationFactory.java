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

import jakarta.servlet.ServletContext;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
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
public class ServletContainerApplicationFactory extends AbstractApplicationFactory {

	private final ServletContext servletContext;

	private final DispatcherServletPath dispatcherServletPath;

	public ServletContainerApplicationFactory(InstanceProperties instance, ServletContext servletContext,
			PathMappedEndpoints pathMappedEndpoints, WebEndpointProperties webEndpoint,
			MetadataContributor metadataContributor, DispatcherServletPath dispatcherServletPath) {
		super(instance, pathMappedEndpoints, webEndpoint, metadataContributor);
		this.servletContext = servletContext;
		this.dispatcherServletPath = dispatcherServletPath;
	}

	@Override
	protected String getContextPath() {
		return this.servletContext.getContextPath();
	}

	@Override
	protected String getServiceBaseUrl() {
		String baseUrl = getInstance().getServiceBaseUrl();

		if (!StringUtils.hasText(baseUrl)) {
			throw new IllegalStateException(
					"spring.boot.admin.client.instance.service-base-url must be set when deployed to an external servlet container.");
		}
		return baseUrl;
	}

	@Override
	protected String getManagementBaseUrl() {
		String baseUrl = getInstance().getManagementBaseUrl();

		if (StringUtils.hasText(baseUrl)) {
			return baseUrl;
		}

		return UriComponentsBuilder.fromUriString(getServiceUrl())
			.path("/")
			.path(this.dispatcherServletPath.getPrefix())
			.toUriString();
	}

}
