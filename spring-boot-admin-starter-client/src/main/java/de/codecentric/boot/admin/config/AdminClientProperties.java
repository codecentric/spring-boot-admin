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

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.event.EventListener;
import org.springframework.util.StringUtils;

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

	@Value("${endpoints.health.id:health}")
	private String healthEndpointId;

	/**
	 * Should the registered urls be built with server.address or with hostname.
	 */
	private boolean preferIp = false;

	@Autowired
	private ManagementServerProperties management;

	@Autowired
	private ServerProperties server;

	private Integer serverPort;

	private Integer managementPort;

	private boolean ready = false;

	@EventListener
	public void onApplicationReady(ApplicationReadyEvent event) {
		serverPort = event.getApplicationContext().getEnvironment().getProperty("local.server.port",
				Integer.class);
		managementPort = event.getApplicationContext().getEnvironment()
				.getProperty("local.management.port", Integer.class, serverPort);
		ready = true;
	}

	public String getManagementUrl() {
		if (managementUrl != null) {
			return managementUrl;
		}

		if ((managementPort == null || managementPort.equals(serverPort))
				&& getServiceUrl() != null) {
			return append(append(getServiceUrl(), server.getServletPrefix()),
					management.getContextPath());
		}

		if (ready && managementPort == null) {
			throw new IllegalStateException(
					"serviceUrl must be set when deployed to servlet-container");
		}

		if (preferIp) {
			InetAddress address = management.getAddress();
			if (address == null) {
				address = getHostAddress();
			}
			return append(append(createLocalUri(address.getHostAddress(), managementPort),
					server.getContextPath()), management.getContextPath());

		}
		return append(
				append(createLocalUri(getHostAddress().getCanonicalHostName(), managementPort),
						server.getContextPath()),
				management.getContextPath());
	}

	public void setManagementUrl(String managementUrl) {
		this.managementUrl = managementUrl;
	}

	public String getHealthUrl() {
		if (healthUrl != null) {
			return healthUrl;
		}
		return append(getManagementUrl(), healthEndpointId);
	}

	public void setHealthUrl(String healthUrl) {
		this.healthUrl = healthUrl;
	}

	public String getServiceUrl() {
		if (serviceUrl != null) {
			return serviceUrl;
		}

		if (ready && serverPort == null) {
			throw new IllegalStateException(
					"serviceUrl must be set when deployed to servlet-container");
		}

		if (preferIp) {
			InetAddress address = server.getAddress();
			if (address == null) {
				address = getHostAddress();
			}
			return append(createLocalUri(address.getHostAddress(), serverPort),
					server.getContextPath());

		}
		return append(createLocalUri(getHostAddress().getCanonicalHostName(), serverPort),
				server.getContextPath());
	}

	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	public boolean isReady() {
		return ready;
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

	private String createLocalUri(String host, int port) {
		String scheme = server.getSsl() != null && server.getSsl().isEnabled() ? "https" : "http";
		return scheme + "://" + host + ":" + port;
	}

	private String append(String uri, String path) {
		String baseUri = uri.replaceFirst("/+$", "");
		if (StringUtils.isEmpty(path)) {
			return baseUri;
		}

		String normPath = path.replaceFirst("^/+", "").replaceFirst("/+$", "");
		return baseUri + "/" + normPath;
	}

	private InetAddress getHostAddress() {
		try {
			return InetAddress.getLocalHost();
		} catch (UnknownHostException ex) {
			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}
}
