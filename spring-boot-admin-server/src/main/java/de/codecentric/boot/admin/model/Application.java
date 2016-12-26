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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

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
	@JsonSerialize(using = Application.MetadataSerializer.class)
	private final Map<String, String> metadata;
	private final Info info;

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
		this.metadata = Collections.unmodifiableMap(new HashMap<>(builder.metadata));
		this.info = builder.info;
	}

	public static Builder create(String name) {
		return new Builder(name);
	}

	public static Builder copyOf(Application application) {
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
		private Map<String, String> metadata = new HashMap<>();
		private Info info = Info.empty();

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
			this.metadata.putAll(application.getMetadata());
			this.info = application.info;
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

		public Builder addMetadata(String key, String value) {
			this.metadata.put(key, value);
			return this;
		}

		public Builder withMetadata(Map<String, String> metadata) {
			this.metadata = metadata;
			return this;
		}

		public Builder withInfo(Info info) {
			this.info = info;
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

	public Map<String, String> getMetadata() {
		return metadata;
	}

	public Info getInfo() {
		return info;
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

			if (node.has("metadata")) {
				Iterator<Entry<String, JsonNode>> it = node.get("metadata").fields();
				while (it.hasNext()) {
					Entry<String, JsonNode> entry = it.next();
					builder.addMetadata(entry.getKey(), entry.getValue().asText());
				}
			}
			return builder.build();
		}
	}

	public static class MetadataSerializer extends StdSerializer<Map<String, String>> {
		private static final long serialVersionUID = 1L;
		private static Pattern[] keysToSanitize = createPatterns(".*password$", ".*secret$",
				".*key$", ".*$token$", ".*credentials.*", ".*vcap_services$");

		@SuppressWarnings("unchecked")
		public MetadataSerializer() {
			super((Class<Map<String, String>>) (Class<?>) Map.class);
		}

		private static Pattern[] createPatterns(String... keys) {
			Pattern[] patterns = new Pattern[keys.length];
			for (int i = 0; i < keys.length; i++) {
				patterns[i] = Pattern.compile(keys[i]);
			}
			return patterns;
		}

		@Override
		public void serialize(Map<String, String> value, JsonGenerator gen,
				SerializerProvider provider) throws IOException {
			gen.writeStartObject();
			for (Entry<String, String> entry : value.entrySet()) {
				gen.writeStringField(entry.getKey(), sanitize(entry.getKey(), entry.getValue()));
			}
			gen.writeEndObject();
		}

		private String sanitize(String key, String value) {
			for (Pattern pattern : MetadataSerializer.keysToSanitize) {
				if (pattern.matcher(key).matches()) {
					return (value == null ? null : "******");
				}
			}
			return value;
		}
	}
}
