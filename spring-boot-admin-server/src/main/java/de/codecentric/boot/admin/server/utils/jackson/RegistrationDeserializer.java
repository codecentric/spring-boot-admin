/*
 * Copyright 2014-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.server.utils.jackson;

import java.io.IOException;
import java.io.Serial;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import de.codecentric.boot.admin.server.domain.values.Registration;

public class RegistrationDeserializer extends StdDeserializer<Registration> {

	@Serial
	private static final long serialVersionUID = 1L;

	public RegistrationDeserializer() {
		super(Registration.class);
	}

	@Override
	public Registration deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		JsonNode node = p.readValueAsTree();
		Registration.Builder builder = Registration.builder();

		builder.name(firstNonNullAsText(node, "name"));

		if (node.hasNonNull("url")) {
			String url = firstNonNullAsText(node, "url");
			builder.healthUrl(url.replaceFirst("/+$", "") + "/health").managementUrl(url);
		}
		else {
			builder.healthUrl(firstNonNullAsText(node, "healthUrl", "health_url"));
			builder.managementUrl(firstNonNullAsText(node, "managementUrl", "management_url"));
			builder.serviceUrl(firstNonNullAsText(node, "serviceUrl", "service_url"));
		}

		if (node.has("metadata")) {
			Iterator<Map.Entry<String, JsonNode>> it = node.get("metadata").fields();
			while (it.hasNext()) {
				Map.Entry<String, JsonNode> entry = it.next();
				builder.metadata(entry.getKey(), entry.getValue().asText());
			}
		}

		builder.source(firstNonNullAsText(node, "source"));

		return builder.build();
	}

	private String firstNonNullAsText(JsonNode node, String... fieldNames) {
		for (String fieldName : fieldNames) {
			if (node.hasNonNull(fieldName)) {
				return node.get(fieldName).asText();
			}
		}
		return null;
	}

}
