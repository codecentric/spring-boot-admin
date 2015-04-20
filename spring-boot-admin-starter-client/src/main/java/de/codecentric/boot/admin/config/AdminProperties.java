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

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.boot.admin")
public class AdminProperties {

	private String url;

	private String contextPath = "api/applications";

	private int period = 10000;

	private String username;

	private String password;

	private boolean autoDeregistration;

	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the Spring Boot Admin Server's url.
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @return the Spring Boot Admin Server's context path.
	 */
	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	/**
	 * @return the time interval (in ms) the registration is repeated.
	 */
	public int getPeriod() {
		return period;
	}

	public void setPeriod(int period) {
		this.period = period;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return username for basic authentication .
	 */
	public String getUsername() {
		return username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return password for basic authentication.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @return wether the application deregisters automatically on shutdown.
	 */
	public boolean isAutoDeregistration() {
		return autoDeregistration;
	}

	public void setAutoDeregistration(boolean autoDeregistration) {
		this.autoDeregistration = autoDeregistration;
	}
}
