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
package de.codecentric.boot.admin.client.registration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

/**
 * Contains all informations which is used when this application is registered.
 *
 * @author Johannes Edmeier
 */
public class Application {
	private final String name;
	private final String managementUrl;
	private final String healthUrl;
	private final String serviceUrl;
	private final Map<String, String> metadata;

	protected Application(Builder builder) {
		Assert.hasText(builder.name, "name must not be empty!");
		Assert.hasText(builder.healthUrl, "healthUrl must not be empty!");
		this.healthUrl = builder.healthUrl;
		this.managementUrl = builder.managementUrl;
		this.serviceUrl = builder.serviceUrl;
		this.name = builder.name;
		this.metadata = Collections.unmodifiableMap(new HashMap<>(builder.metadata));
	}

	public static Builder create(String name) {
		return new Builder(name);
	}

	public static class Builder {
		private String name;
		private String managementUrl;
		private String healthUrl;
		private String serviceUrl;
		private Map<String, String> metadata = new HashMap<>();

		private Builder(String name) {
			this.name = name;
		}

		public Builder withName(String name) {
			this.name = name;
			return this;
		}

		public Builder withServiceUrl(String serviceUrl) {
			this.serviceUrl = serviceUrl;
			return this;
		}

		public Builder withHealthUrl(String healthUrl) {
			this.healthUrl = healthUrl;
			return this;
		}

		public Builder withManagementUrl(String managementUrl) {
			this.managementUrl = managementUrl;
			return this;
		}

		public Builder withMetadata(String key, String value) {
			this.metadata.put(key, value);
			return this;
		}

		public Builder withMetadata(Map<String, String> metadata) {
			this.metadata.putAll(metadata);
			return this;
		}

		public Application build() {
			return new Application(this);
		}
	}

	public String getName() {
		return name;
	}

	public String getManagementUrl() {
		return managementUrl;
	}

	public String getHealthUrl() {
		return healthUrl;
	}

	public String getServiceUrl() {
		return serviceUrl;
	}

	public Map<String, String> getMetadata() {
		return metadata;
	}

	@Override
	public String toString() {
		return "Application [name=" + name + ", managementUrl=" + managementUrl + ", healthUrl="
				+ healthUrl + ", serviceUrl=" + serviceUrl + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((healthUrl == null) ? 0 : healthUrl.hashCode());
		result = prime * result + ((managementUrl == null) ? 0 : managementUrl.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((serviceUrl == null) ? 0 : serviceUrl.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Application other = (Application) obj;
		if (healthUrl == null) {
			if (other.healthUrl != null) {
				return false;
			}
		} else if (!healthUrl.equals(other.healthUrl)) {
			return false;
		}
		if (managementUrl == null) {
			if (other.managementUrl != null) {
				return false;
			}
		} else if (!managementUrl.equals(other.managementUrl)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (serviceUrl == null) {
			if (other.serviceUrl != null) {
				return false;
			}
		} else if (!serviceUrl.equals(other.serviceUrl)) {
			return false;
		}
		return true;
	}

}
