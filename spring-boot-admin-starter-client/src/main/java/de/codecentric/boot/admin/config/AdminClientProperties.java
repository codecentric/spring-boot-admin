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
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "spring.boot.admin.client", ignoreUnknownFields = false)
@Order(Ordered.LOWEST_PRECEDENCE - 100)
public class AdminClientProperties implements ApplicationListener<ApplicationEvent> {

	private String managementUrl;
	private String serviceUrl;
	private String healthUrl;

	@Value("${spring.application.name:spring-boot-application}")
	private String name;

	@Value("${endpoints.health.id:health}")
	private String healthEndpointId;

	@Autowired
	private ManagementServerProperties management;

	@Autowired
	private ServerProperties server;

	private int serverPort = -1;

	private int managementPort = -1;

	private boolean serverInitialized = false;

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof EmbeddedServletContainerInitializedEvent) {
			EmbeddedServletContainerInitializedEvent initEvent = (EmbeddedServletContainerInitializedEvent) event;
			serverInitialized = true;
			if ("management".equals(initEvent.getApplicationContext().getNamespace())) {
				managementPort = initEvent.getEmbeddedServletContainer().getPort();
			} else {
				serverPort = initEvent.getEmbeddedServletContainer().getPort();
			}
		} else if (startedDeployedWar(event)) {
			serverInitialized = true;
			if (!StringUtils.hasText(serviceUrl)) {
				throw new RuntimeException(
						"spring.boot.admin.client.serviceUrl must be set for deployed war files!");
			}
		}
	}

	private boolean startedDeployedWar(ApplicationEvent event) {
		if (event instanceof ContextRefreshedEvent) {
			ApplicationContextEvent contextEvent = (ApplicationContextEvent) event;
			if (contextEvent.getApplicationContext() instanceof EmbeddedWebApplicationContext) {
				EmbeddedWebApplicationContext context = (EmbeddedWebApplicationContext) contextEvent
						.getApplicationContext();
				return context.getEmbeddedServletContainer() == null;
			}
		}
		return false;
	}


	/**
	 * @return Client-management-URL to register with. Can be overriden in case the
	 * reachable URL is different (e.g. Docker). Must be unique in registry.
	 */
	public String getManagementUrl() {
		if (managementUrl == null) {
			if (managementPort != -1) {
				return createLocalUri(managementPort,
						management.getContextPath());
			}
			else {
				return append(getServiceUrl(), management.getContextPath());
			}
		}

		return managementUrl;
	}

	public void setManagementUrl(String managementUrl) {
		this.managementUrl = managementUrl;
	}

	/**
	 * @return Client-health-URL to register with. Can be overriden in case the reachable
	 * URL is different (e.g. Docker). Must be unique in registry.
	 */

	public String getHealthUrl() {
		if (healthUrl == null) {
			return append(getManagementUrl(), healthEndpointId);
		}
		return healthUrl;
	}

	public void setHealthUrl(String healthUrl) {
		this.healthUrl = healthUrl;
	}

	/**
	 * @return Client-service-URL to register with. Can be overriden in case the reachable
	 * URL is different (e.g. Docker). Must be unique in registry.
	 */
	public String getServiceUrl() {
		if (serviceUrl == null) {
			if (serverPort != -1){
				return createLocalUri(serverPort, server.getContextPath());
			} else {
				throw new IllegalStateException(
						"EmbeddedServletContainer has not been initialized yet!");
			}
		}

		return serviceUrl;
	}

	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	public boolean isServerInitialized() {
		return serverInitialized;
	}

	/**
	 * @return Name to register with.
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private String getHostname() {
		try {
			return InetAddress.getLocalHost().getCanonicalHostName();
		}
		catch (UnknownHostException ex) {
			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}

	private String createLocalUri(int port, String path) {
		return append("http://" + getHostname() + ":" + port + "/", path);
	}

	private String append(String uri, String path) {
		if (StringUtils.isEmpty(path)) {
			return uri;
		}

		String baseUri = uri.endsWith("/") ? uri.replaceFirst("/+$", "") : uri;
		return baseUri + (path.startsWith("/") ? "" : "/") + path
				+ (path.endsWith("/") ? "" : "/");
	}
}
