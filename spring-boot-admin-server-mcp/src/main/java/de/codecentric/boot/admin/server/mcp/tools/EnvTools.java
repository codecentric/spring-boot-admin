/*
 * Copyright 2014-2026 the original author or authors.
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

package de.codecentric.boot.admin.server.mcp.tools;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.web.util.UriUtils;
import reactor.core.publisher.Mono;

/**
 * MCP tools for querying the environment of registered Spring Boot applications.
 *
 * <ul>
 * <li>{@code get-env} — resolves a single configuration property (including environment
 * variables) across all property sources</li>
 * <li>{@code list-env} — retrieves all environment properties grouped by property
 * source</li>
 * </ul>
 */
public class EnvTools {

	private final ActuatorClient actuatorClient;

	/**
	 * Creates a new {@code EnvTools} instance.
	 * @param actuatorClient the shared actuator call helper
	 */
	public EnvTools(ActuatorClient actuatorClient) {
		this.actuatorClient = actuatorClient;
	}

	/**
	 * Resolves a single configuration property for the named application by calling its
	 * {@code /actuator/env/{propertyName}} endpoint. This covers environment variables
	 * (e.g. {@code HELLO}), system properties and any other property source.
	 * @param applicationName the registered application name (case-sensitive)
	 * @param propertyName the property or environment variable name (e.g. {@code HELLO})
	 * @return plain-text resolved value with its originating property sources, or an
	 * error message
	 */
	@McpTool(name = "get-env",
			description = "Resolve a single configuration property or environment variable for a registered "
					+ "Spring Boot application by calling its /actuator/env/{name} endpoint. Works for "
					+ "environment variables (e.g. HELLO), system properties and application properties. "
					+ "Requires the env actuator endpoint to be exposed on the monitored application.")
	public Mono<String> getEnv(
			@McpToolParam(description = "The registered application name (case-sensitive)",
					required = true) String applicationName,
			@McpToolParam(description = "The property or environment variable name (e.g. HELLO)",
					required = true) String propertyName) {
		return this.actuatorClient.query(applicationName,
				"/env/" + UriUtils.encodePathSegment(propertyName, StandardCharsets.UTF_8),
				(app, body) -> formatProperty(app, propertyName, body));
	}

	/**
	 * Retrieves all environment properties for the named application by calling its
	 * {@code /actuator/env} endpoint. Properties are grouped by their originating
	 * property source (e.g. {@code systemEnvironment}, {@code systemProperties},
	 * application config). An optional case-insensitive filter restricts the result to
	 * property names containing the given text.
	 * @param applicationName the registered application name (case-sensitive)
	 * @param filter optional case-insensitive substring; only property names containing
	 * it are returned. When {@code null} or blank, all properties are returned.
	 * @return plain-text listing of every (matching) property grouped by property source,
	 * or an error message
	 */
	@McpTool(name = "list-env",
			description = "Retrieve environment properties for a registered Spring Boot application by calling "
					+ "its /actuator/env endpoint. Returns properties grouped by their property source "
					+ "(e.g. systemEnvironment, systemProperties, application config). Provide an optional "
					+ "'filter' to return only property names containing that text (case-insensitive); omit "
					+ "it to return ALL properties. Values may be masked (******) if the monitored application "
					+ "does not expose them. Requires the env actuator endpoint to be exposed on the monitored "
					+ "application.")
	public Mono<String> listEnv(
			@McpToolParam(description = "The registered application name (case-sensitive)",
					required = true) String applicationName,
			@McpToolParam(description = "Optional case-insensitive substring; only property names containing it are "
					+ "returned. Omit to return all properties.", required = false) String filter) {
		return this.actuatorClient.query(applicationName, "/env", (app, body) -> formatEnv(app, filter, body));
	}

	@SuppressWarnings("unchecked")
	private String formatEnv(String applicationName, String filter, Map<String, Object> body) {
		Object propertySources = body.get("propertySources");
		if (!(propertySources instanceof List<?> sources) || sources.isEmpty()) {
			return "No environment properties available for " + applicationName + ".";
		}

		boolean filtered = filter != null && !filter.isBlank();
		String needle = filtered ? filter.toLowerCase(Locale.ROOT) : null;

		StringBuilder body_sb = new StringBuilder();
		int matches = 0;

		for (Object source : sources) {
			if (!(source instanceof Map<?, ?> sourceEntry)) {
				continue;
			}
			Object name = sourceEntry.get("name");
			Object properties = sourceEntry.get("properties");
			if (!(properties instanceof Map<?, ?> props)) {
				continue;
			}
			StringBuilder sourceSb = new StringBuilder();
			int sourceMatches = 0;
			for (Map.Entry<?, ?> entry : props.entrySet()) {
				String key = String.valueOf(entry.getKey());
				if (filtered && !key.toLowerCase(Locale.ROOT).contains(needle)) {
					continue;
				}
				sourceMatches++;
				sourceSb.append("  ").append(key);
				if (entry.getValue() instanceof Map<?, ?> propValue) {
					sourceSb.append(" = ").append(propValue.get("value"));
					Object origin = propValue.get("origin");
					if (origin != null) {
						sourceSb.append(" (").append(origin).append(")");
					}
				}
				sourceSb.append("\n");
			}
			if (sourceMatches > 0) {
				body_sb.append("\n[").append(name).append("] (").append(sourceMatches).append("):\n").append(sourceSb);
				matches += sourceMatches;
			}
		}

		if (filtered && matches == 0) {
			return "No environment properties matching '" + filter + "' for " + applicationName + ".";
		}

		StringBuilder sb = new StringBuilder("Environment for ").append(applicationName);
		if (filtered) {
			sb.append(" (filtered by \"").append(filter).append("\")");
		}
		sb.append(":\n");

		Object activeProfiles = body.get("activeProfiles");
		if (activeProfiles instanceof List<?> profiles && !profiles.isEmpty()) {
			sb.append("  active profiles: ").append(profiles).append("\n");
		}

		sb.append(body_sb);
		return sb.toString().trim();
	}

	@SuppressWarnings("unchecked")
	private String formatProperty(String applicationName, String propertyName, Map<String, Object> body) {
		Object property = body.get("property");
		if (!(property instanceof Map<?, ?> resolved) || resolved.get("value") == null) {
			return "Property '" + propertyName + "' is not set for " + applicationName + ".";
		}

		StringBuilder sb = new StringBuilder(applicationName).append(" — ").append(propertyName).append(":\n");
		sb.append("  value: ").append(resolved.get("value")).append("\n");
		Object source = resolved.get("source");
		if (source != null) {
			sb.append("  source: ").append(source).append("\n");
		}

		Object propertySources = body.get("propertySources");
		if (propertySources instanceof List<?> sources && !sources.isEmpty()) {
			sb.append("  property sources:\n");
			for (Object item : sources) {
				if (item instanceof Map<?, ?> sourceEntry && sourceEntry.get("property") instanceof Map<?, ?> prop) {
					sb.append("    - ").append(sourceEntry.get("name")).append(": ").append(prop.get("value"));
					Object origin = prop.get("origin");
					if (origin != null) {
						sb.append(" (").append(origin).append(")");
					}
					sb.append("\n");
				}
			}
		}
		return sb.toString().trim();
	}

}
