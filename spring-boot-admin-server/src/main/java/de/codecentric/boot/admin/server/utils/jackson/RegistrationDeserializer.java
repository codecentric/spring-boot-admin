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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.util.ObjectUtils;

import de.codecentric.boot.admin.server.domain.values.Registration;

import org.springframework.util.ObjectUtils;

public class RegistrationDeserializer extends StdDeserializer<Registration> {

	private static final long serialVersionUID = 1L;

	public RegistrationDeserializer() {
		super(Registration.class);
	}

	@Override
	public Registration deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		JsonNode node = p.readValueAsTree();
		Map<String, String> normalizedKvPair = getNormalizedKvPair(node);
		Registration.Builder builder = Registration.builder();

		if (node.hasNonNull("name")) {
			builder.name(node.get("name").asText());
		}
		if (node.hasNonNull("url")) {
			String url = node.get("url").asText();
			builder.healthUrl(url.replaceFirst("/+$", "") + "/health").managementUrl(url);
		}
		else {
			if (!ObjectUtils.isEmpty(normalizedKvPair.get("healthurl"))) {
				builder.healthUrl(normalizedKvPair.get("healthurl"));
			}
			if (!ObjectUtils.isEmpty(normalizedKvPair.get("managementurl"))) {
				builder.managementUrl(normalizedKvPair.get("managementurl"));
			}
			if (!ObjectUtils.isEmpty(normalizedKvPair.get("serviceurl"))) {
				builder.serviceUrl(normalizedKvPair.get("serviceurl"));
			}
		}

		if (node.has("metadata")) {
			Iterator<Map.Entry<String, JsonNode>> it = node.get("metadata").fields();
			while (it.hasNext()) {
				Map.Entry<String, JsonNode> entry = it.next();
				builder.metadata(entry.getKey(), entry.getValue().asText());
			}
		}

		if (node.hasNonNull("source")) {
			builder.source(node.get("source").asText());
		}

		return builder.build();
	}

	private Map<String, String> getNormalizedKvPair(JsonNode jn) throws IOException {
		Map<String, String> normalizedKvPair = new HashMap<>();
		JsonParser jp = jn.traverse();
		while (!jp.isClosed()) {
			if (jp.nextToken() == JsonToken.FIELD_NAME) {
				String fieldName = jp.currentName();
				if (!ObjectUtils.isEmpty(fieldName)) {
					JsonToken jsonValueToken = jp.nextValue();
					if (jsonValueToken == JsonToken.VALUE_STRING) {
						normalizedKvPair.putIfAbsent(fieldName.replaceAll("[_-]", "").toLowerCase(),
								jp.getValueAsString());
					}
				}
			}
		}
		return normalizedKvPair;
	}

}
