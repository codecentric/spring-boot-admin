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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.boot.admin.client", ignoreUnknownFields = false)
public class AdminClientProperties {

	@Value("http://#{T(java.net.InetAddress).localHost.canonicalHostName}:${server.port:${management.port:8080}}${management.context-path:/}")
	private String url;

	@Value("${spring.application.name:spring-boot-application}")
	private String name;

	/**
	 * @return Client-management-URL to register with. Can be overriden in case the
	 *         reachable URL is different (e.g. Docker). Must be unique in registry.
	 */
	public String getUrl() {
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
}
