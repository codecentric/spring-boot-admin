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
package de.codecentric.boot.admin.model;

import java.io.IOException;
import java.io.Serializable;

import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * The domain model for all registered application at the spring boot admin application.
 */
@JsonDeserialize(using = Application.Deserializer.class)
public class Application implements Serializable {
	private static final long serialVersionUID = 2L;

	private final String id;
	private final String name;
	private final String managementUrl;
	private final String healthUrl;
	private final String serviceUrl;
	private final StatusInfo statusInfo;
	private final String source;

	protected Application(Builder builder) {
		Assert.hasText(builder.name, "name must not be empty!");
		Assert.hasText(builder.healthUrl, "healthUrl must not be empty!");
		Assert.notNull(builder.statusInfo, "statusInfo must not be null!");
		this.healthUrl = builder.healthUrl;
		this.managementUrl = builder.managementUrl;
		this.serviceUrl = builder.serviceUrl;
		this.name = builder.name;
		this.id = builder.id;
		this.statusInfo = builder.statusInfo;
		this.source = builder.source;
	}

	public static Builder create(String name) {
		return new Builder(name);
	}

	public static Builder create(Application application) {
		return new Builder(application);
	}

	public static class Builder {
		private String id;
		private String name;
		private String managementUrl;
		private String healthUrl;
		private String serviceUrl;
		private StatusInfo statusInfo = StatusInfo.ofUnknown();
		private String source;

		private Builder(String name) {
			this.name = name;
		}

		private Builder(Application application) {
			this.healthUrl = application.healthUrl;
			this.managementUrl = application.managementUrl;
			this.serviceUrl = application.serviceUrl;
			this.name = application.name;
			this.id = application.id;
			this.statusInfo = application.statusInfo;
			this.source = application.source;
		}

		public Builder withName(String name) {
			this.name = name;
			return this;
		}

		public Builder withId(String id) {
			this.id = id;
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

		public Builder withStatusInfo(StatusInfo statusInfo) {
			this.statusInfo = statusInfo;
			return this;
		}

		public Builder withSource(String source) {
			this.source = source;
			return this;
		}

		public Application build() {
			return new Application(this);
		}
	}

	public String getId() {
		return id;
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

	public StatusInfo getStatusInfo() {
		return statusInfo;
	}

	public String getSource() {
		return source;
	}

	@Override
	public String toString() {
		return "Application [id=" + id + ", name=" + name + ", managementUrl="
				+ managementUrl + ", healthUrl=" + healthUrl + ", serviceUrl=" + serviceUrl + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((healthUrl == null) ? 0 : healthUrl.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
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

	public static class Deserializer extends StdDeserializer<Application> {
		private static final long serialVersionUID = 1L;

		protected Deserializer() {
			super(Application.class);
		}

		@Override
		public Application deserialize(JsonParser p, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			JsonNode node = p.readValueAsTree();

			Builder builder = create(node.get("name").asText());

			if (node.has("url")) {
				String url = node.get("url").asText();
				builder.withHealthUrl(url.replaceFirst("/+$", "") + "/health")
						.withManagementUrl(url);
			} else {
				if (node.has("healthUrl")) {
					builder.withHealthUrl(node.get("healthUrl").asText());
				}
				if (node.has("managementUrl")) {
					builder.withManagementUrl(node.get("managementUrl").asText());
				}
				if (node.has("serviceUrl")) {
					builder.withServiceUrl(node.get("serviceUrl").asText());
				}
			}
			return builder.build();
		}
	}
}
