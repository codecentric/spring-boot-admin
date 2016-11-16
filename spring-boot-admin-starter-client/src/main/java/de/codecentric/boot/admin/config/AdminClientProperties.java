/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.codecentric.boot.admin.config;

import static org.springframework.util.StringUtils.trimLeadingCharacter;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.embedded.Ssl;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.event.EventListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.UriComponentsBuilder;

@ConfigurationProperties(prefix = "spring.boot.admin.client")
public class AdminClientProperties {
	/**
	 * Client-management-URL to register with. Inferred at runtime, can be overriden in case the
	 * reachable URL is different (e.g. Docker).
	 */
	private String managementUrl;

	/**
	 * Client-service-URL register with. Inferred at runtime, can be overriden in case the reachable
	 * URL is different (e.g. Docker).
	 */
	private String serviceUrl;

	/**
	 * Client-health-URL to register with. Inferred at runtime, can be overriden in case the
	 * reachable URL is different (e.g. Docker). Must be unique in registry.
	 */
	private String healthUrl;

	/**
	 * Name to register with. Defaults to ${spring.application.name}
	 */
	@Value("${spring.application.name:spring-boot-application}")
	private String name;

	/**
	 * Should the registered urls be built with server.address or with hostname.
	 */
	private boolean preferIp = false;

	@Value("${endpoints.health.path:/${endpoints.health.id:health}}")
	private String healthEndpointPath;

	@Autowired
	private ManagementServerProperties management;

	@Autowired
	private ServerProperties server;

	private Integer serverPort;

	private Integer managementPort;

	@EventListener
	public void onApplicationReady(ApplicationReadyEvent event) {
		if (event.getApplicationContext() instanceof WebApplicationContext) {
			serverPort = event.getApplicationContext().getEnvironment()
					.getProperty("local.server.port", Integer.class);
			managementPort = event.getApplicationContext().getEnvironment()
					.getProperty("local.management.port", Integer.class, serverPort);
		}
	}

	public String getServiceUrl() {
		if (serviceUrl != null) {
			return serviceUrl;
		}

		if (serverPort == null) {
			throw new IllegalStateException(
					"serviceUrl must be set when deployed to servlet-container");
		}

		return UriComponentsBuilder.newInstance().scheme(getScheme(server.getSsl()))
				.host(getServiceHost()).port(serverPort).path(server.getContextPath())
				.toUriString();
	}

	public String getManagementUrl() {
		if (managementUrl != null) {
			return managementUrl;
		}

		if (managementPort == null || managementPort.equals(serverPort)) {
			return UriComponentsBuilder.fromHttpUrl(getServiceUrl())
					.pathSegment(server.getServletPrefix().split("/"))
					.pathSegment(trimLeadingCharacter(management.getContextPath(), '/').split("/"))
					.toUriString();
		}

		Ssl ssl = management.getSsl() != null ? management.getSsl() : server.getSsl();
		return UriComponentsBuilder.newInstance().scheme(getScheme(ssl)).host(getManagementHost())
				.port(managementPort).path(management.getContextPath()).toUriString();
	}

	public String getHealthUrl() {
		if (healthUrl != null) {
			return healthUrl;
		}
		return UriComponentsBuilder.fromHttpUrl(getManagementUrl())
				.pathSegment(trimLeadingCharacter(healthEndpointPath, '/').split("/"))
				.toUriString();
	}

	public void setManagementUrl(String managementUrl) {
		this.managementUrl = managementUrl;
	}

	public void setHealthUrl(String healthUrl) {
		this.healthUrl = healthUrl;
	}

	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPreferIp(boolean preferIp) {
		this.preferIp = preferIp;
	}

	public boolean isPreferIp() {
		return preferIp;
	}

	private String getScheme(Ssl ssl) {
		return ssl != null && ssl.isEnabled() ? "https" : "http";
	}

	private String getHost(InetAddress address) {
		return preferIp ? address.getHostAddress() : address.getCanonicalHostName();
	}

	private String getServiceHost() {
		InetAddress address = server.getAddress();
		if (address == null) {
			address = getLocalHost();
		}
		return getHost(address);
	}

	private String getManagementHost() {
		InetAddress address = management.getAddress();
		if (address != null) {
			return getHost(address);
		}
		return getServiceHost();
	}

	private InetAddress getLocalHost() {
		try {
			return InetAddress.getLocalHost();
		} catch (UnknownHostException ex) {
			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}

}
