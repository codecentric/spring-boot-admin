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
package de.codecentric.boot.admin.client.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.boot.admin.client")
public class AdminClientProperties {
	/**
	 * Client-management-URL to register with. Inferred at runtime, can be overridden in case the
	 * reachable URL is different (e.g. Docker).
	 */
	private String managementUrl;

	/**
	 * Client-service-URL register with. Inferred at runtime, can be overridden in case the reachable
	 * URL is different (e.g. Docker).
	 */
	private String serviceUrl;

	/**
	 * Client-health-URL to register with. Inferred at runtime, can be overridden in case the
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

	/**
	 * Metadata that should be associated with this application
	 */
	private Map<String, String> metadata = new HashMap<>();

	public String getManagementUrl() {
		return managementUrl;
	}

	public void setManagementUrl(String managementUrl) {
		this.managementUrl = managementUrl;
	}

	public String getServiceUrl() {
		return serviceUrl;
	}

	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	public String getHealthUrl() {
		return healthUrl;
	}

	public void setHealthUrl(String healthUrl) {
		this.healthUrl = healthUrl;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isPreferIp() {
		return preferIp;
	}

	public void setPreferIp(boolean preferIp) {
		this.preferIp = preferIp;
	}

	public Map<String, String> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}
}
