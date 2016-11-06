/*
 * Copyright 2013-2016 the original author or authors.
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
package spring.boot.admin.turbine.config;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.boot.admin.turbine")
public class TurbineProperties {

	/**
	 * URL to the <code>turbine.stream</code>. Must be reachable from the admin server.
	 */
	private URI url;

	/**
	 * List of available Turbine clusters.
	 */
	private String[] clusters = { "default" };

	/**
	 * Enable the Spring Boot Admin backend configuration for Turbine.
	 */
	private boolean enabled = true;


	public String[] getClusters() {
		return clusters;
	}

	public void setClusters(String[] clusters) {
		this.clusters = clusters;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public URI getUrl() {
		return url;
	}

	public void setUrl(URI url) {
		this.url = url;
	}
}
