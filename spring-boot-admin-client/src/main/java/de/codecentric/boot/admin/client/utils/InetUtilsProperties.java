/*
 * Copyright 2017 the original author or authors.
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

package de.codecentric.boot.admin.client.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Spencer Gibb
 */
@ConfigurationProperties(InetUtilsProperties.PREFIX)
public class InetUtilsProperties {
	public static final String PREFIX = "spring.cloud.utils";

	/**
	 * The default hostname. Used in case of errors.
	 */
	private String defaultHostname = "localhost";

	/**
	 * The default ipaddress. Used in case of errors.
	 */
	private String defaultIpAddress = "127.0.0.1";

	/**
	 * Timeout in seconds for calculating hostname.
	 */
	@Value("${spring.util.timeout.sec:${SPRING_UTIL_TIMEOUT_SEC:1}}")
	private int timeoutSeconds = 1;

	/**
	 * List of Java regex expressions for network interfaces that will be ignored.
	 */
	private List<String> ignoredInterfaces = new ArrayList<>();

	/**
	 * Use only interfaces with site local addresses. See {@link InetAddress#isSiteLocalAddress()} for more details.
	 */
	private boolean useOnlySiteLocalInterfaces = false;

	/**
	 * List of Java regex expressions for network addresses that will be preferred.
	 */
	private List<String> preferredNetworks = new ArrayList<>();

	public static String getPREFIX() {
		return PREFIX;
	}

	public String getDefaultHostname() {
		return defaultHostname;
	}

	public void setDefaultHostname(String defaultHostname) {
		this.defaultHostname = defaultHostname;
	}

	public String getDefaultIpAddress() {
		return defaultIpAddress;
	}

	public void setDefaultIpAddress(String defaultIpAddress) {
		this.defaultIpAddress = defaultIpAddress;
	}

	public int getTimeoutSeconds() {
		return timeoutSeconds;
	}

	public void setTimeoutSeconds(int timeoutSeconds) {
		this.timeoutSeconds = timeoutSeconds;
	}

	public List<String> getIgnoredInterfaces() {
		return ignoredInterfaces;
	}

	public void setIgnoredInterfaces(List<String> ignoredInterfaces) {
		this.ignoredInterfaces = ignoredInterfaces;
	}

	public boolean isUseOnlySiteLocalInterfaces() {
		return useOnlySiteLocalInterfaces;
	}

	public void setUseOnlySiteLocalInterfaces(boolean useOnlySiteLocalInterfaces) {
		this.useOnlySiteLocalInterfaces = useOnlySiteLocalInterfaces;
	}

	public List<String> getPreferredNetworks() {
		return preferredNetworks;
	}

	public void setPreferredNetworks(List<String> preferredNetworks) {
		this.preferredNetworks = preferredNetworks;
	}
}
