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
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "spring.boot.admin.client", ignoreUnknownFields = false)
public class AdminClientProperties implements ApplicationListener<EmbeddedServletContainerInitializedEvent>{

	private String url;

	@Value("${spring.application.name:spring-boot-application}")
	private String name;

	@Autowired
	private ManagementServerProperties management;

	@Autowired
	private ServerProperties server;

	private int serverPort = -1;

	private int managementPort = -1;


	@Override
	public void onApplicationEvent(
			EmbeddedServletContainerInitializedEvent event) {
		if ("management".equals(event.getApplicationContext().getNamespace())) {
			managementPort = event.getEmbeddedServletContainer().getPort();
		} else {
			serverPort = event.getEmbeddedServletContainer().getPort();
		}
	}

	/**
	 * @return Client-management-URL to register with. Can be overriden in case the
	 *         reachable URL is different (e.g. Docker). Must be unique in registry.
	 */
	public String getUrl() {
		if (url == null) {
			if (managementPort != -1) {
				url = "http://"
						+ (getHostname() + ':' + managementPort + toPathFragment(management
								.getContextPath()))
								.replaceAll("//+", "/");
			} else if (serverPort != -1){
				url = "http://"
						+ (getHostname()
								+ ':'
								+ serverPort
								+ toPathFragment(server.getContextPath()) + toPathFragment(management
										.getContextPath())).replaceAll("//+", "/");
			} else {
				throw new IllegalStateException(
						"EmbeddedServletContainer has not been initialized yet!");
			}
		}

		return url;
	}

	public void setUrl(String url) {
		this.url = url;
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
			throw new IllegalStateException("Couldn't get hostname", ex);
		}
	}

	private String toPathFragment(String fragment) {
		if (StringUtils.isEmpty(fragment)) {
			return "";
		}
		else {
			return "/" + fragment;
		}
	}
}
