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

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.boot.admin")
public class AdminProperties {

	/**
	 * The admin server urls to register at
	 */
	private String[] url;

	/**
	 * The admin rest-apis path.
	 */
	private String apiPath = "api/applications";

	/**
	 * Time interval (in ms) the registration is repeated
	 */
	private long period = 10_000L;

	/**
	 * Username for basic authentication on admin server
	 */
	private String username;

	/**
	 * Password for basic authentication on admin server
	 */
	private String password;

	/**
	 * Enable automatic deregistration on shutdown
	 */
	private boolean autoDeregistration;

	/**
	 * Enable automatic registration when the application is ready
	 */
	private boolean autoRegistration = true;

	/**
	 * Enable registration against one or all admin servers
	 */
	private boolean registerOnce = true;

	public void setUrl(String[] url) {
		this.url = url.clone();
	}

	public String[] getUrl() {
		return url.clone();
	}

	public void setApiPath(String apiPath) {
		this.apiPath = apiPath;
	}

	public String getApiPath() {
		return apiPath;
	}

	public String[] getAdminUrl() {
		String adminUrls[] = url.clone();
		for (int i = 0; i < adminUrls.length; i++) {
			adminUrls[i] += "/" + apiPath;
		}
		return adminUrls;
	}

	public long getPeriod() {
		return period;
	}

	public void setPeriod(int period) {
		this.period = period;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public boolean isAutoDeregistration() {
		return autoDeregistration;
	}

	public void setAutoDeregistration(boolean autoDeregistration) {
		this.autoDeregistration = autoDeregistration;
	}

	public boolean isAutoRegistration() {
		return autoRegistration;
	}

	public void setAutoRegistration(boolean autoRegistration) {
		this.autoRegistration = autoRegistration;
	}

	public boolean isRegisterOnce() {
		return registerOnce;
	}

	public void setRegisterOnce(boolean registerOnce) {
		this.registerOnce = registerOnce;
	}
}
