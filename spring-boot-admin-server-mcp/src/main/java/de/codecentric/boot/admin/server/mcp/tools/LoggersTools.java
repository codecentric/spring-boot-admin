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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.web.util.UriUtils;
import reactor.core.publisher.Mono;

/**
 * MCP tools for querying and configuring log levels of registered Spring Boot
 * applications.
 *
 * <ul>
 * <li>{@code list-loggers} — lists all configured loggers and their effective log
 * levels</li>
 * <li>{@code get-logger} — retrieves the configured and effective log level for a single
 * logger</li>
 * <li>{@code set-logger-level} — changes the log level of a logger at runtime</li>
 * </ul>
 */
public class LoggersTools {

	private static final Logger log = LoggerFactory.getLogger(LoggersTools.class);

	private final ActuatorClient actuatorClient;

	/**
	 * Creates a new {@code LoggersTools} instance.
	 * @param actuatorClient the shared actuator call helper
	 */
	public LoggersTools(ActuatorClient actuatorClient) {
		this.actuatorClient = actuatorClient;
	}

	/**
	 * Lists all loggers and their effective log levels for the named application by
	 * calling its {@code /actuator/loggers} endpoint. An optional filter restricts the
	 * result to logger names containing the given text.
	 * @param applicationName the registered application name (case-sensitive)
	 * @param filter optional case-insensitive substring; only logger names containing it
	 * are returned. When {@code null} or blank, all loggers are returned.
	 * @return plain-text listing of loggers with their levels, or an error message
	 */
	@McpTool(name = "list-loggers",
			description = "List all loggers and their effective log levels for a registered Spring Boot application "
					+ "by calling its /actuator/loggers endpoint. Provide an optional 'filter' to return only "
					+ "logger names containing that text (case-insensitive). Use set-logger-level to change a "
					+ "level at runtime. Requires the loggers actuator endpoint to be exposed.")
	public Mono<String> listLoggers(
			@McpToolParam(description = "The registered application name (case-sensitive)",
					required = true) String applicationName,
			@McpToolParam(description = "Optional case-insensitive substring; only logger names containing it are "
					+ "returned. Omit to return all loggers.", required = false) String filter) {
		return this.actuatorClient.query(applicationName, "/loggers", (app, body) -> formatLoggers(app, filter, body));
	}

	/**
	 * Retrieves the configured and effective log level for a single logger by calling the
	 * {@code /actuator/loggers/{loggerName}} endpoint.
	 * @param applicationName the registered application name (case-sensitive)
	 * @param loggerName the fully-qualified logger name (e.g.
	 * {@code com.example.MyService})
	 * @return plain-text level information, or an error message
	 */
	@McpTool(name = "get-logger",
			description = "Retrieve the configured and effective log level for a single logger in a registered "
					+ "Spring Boot application. Use list-loggers to discover logger names. "
					+ "Requires the loggers actuator endpoint to be exposed.")
	public Mono<String> getLogger(
			@McpToolParam(description = "The registered application name (case-sensitive)",
					required = true) String applicationName,
			@McpToolParam(description = "The fully-qualified logger name (e.g. com.example.MyService)",
					required = true) String loggerName) {
		return this.actuatorClient.query(applicationName,
				"/loggers/" + UriUtils.encodePathSegment(loggerName, StandardCharsets.UTF_8),
				(app, body) -> formatLogger(app, loggerName, body));
	}

	/**
	 * Changes the log level of a logger at runtime for the named application by calling
	 * {@code POST /actuator/loggers/{loggerName}}. Pass {@code null} or {@code "null"} as
	 * the level to reset to the inherited level.
	 * @param applicationName the registered application name (case-sensitive)
	 * @param loggerName the fully-qualified logger name (e.g.
	 * {@code com.example.MyService})
	 * @param level the log level to set (TRACE, DEBUG, INFO, WARN, ERROR, OFF) or null to
	 * reset
	 * @return confirmation message, or an error message
	 */
	@McpTool(name = "set-logger-level",
			description = "Change the log level of a logger at runtime for a registered Spring Boot application "
					+ "via POST /actuator/loggers/{loggerName}. Valid levels: TRACE, DEBUG, INFO, WARN, ERROR, OFF. "
					+ "Pass null to reset to the inherited level. Changes are lost on restart. "
					+ "Requires the loggers actuator endpoint to be exposed.")
	public Mono<String> setLoggerLevel(
			@McpToolParam(description = "The registered application name (case-sensitive)",
					required = true) String applicationName,
			@McpToolParam(description = "The fully-qualified logger name (e.g. com.example.MyService)",
					required = true) String loggerName,
			@McpToolParam(description = "The log level to set: TRACE, DEBUG, INFO, WARN, ERROR, OFF, or null to reset",
					required = true) String level) {
		return this.actuatorClient.withInstance(applicationName, (instance) -> {
			String url = instance.getRegistration().getManagementUrl() + "/loggers/"
					+ UriUtils.encodePathSegment(loggerName, StandardCharsets.UTF_8);
			String body = (level == null || "null".equalsIgnoreCase(level)) ? "{}"
					: "{\"configuredLevel\":\"" + level.toUpperCase(Locale.ROOT) + "\"}";
			return this.actuatorClient
				.postBodiless(instance, url, body, log, "set log level for " + loggerName + " in " + applicationName)
				.map((status) -> {
					if ((status >= 200) && (status < 300)) {
						String effectiveLevel = (level == null || "null".equalsIgnoreCase(level)) ? "inherited"
								: level.toUpperCase(Locale.ROOT);
						return "Log level for '" + loggerName + "' in " + applicationName + " set to " + effectiveLevel
								+ ".";
					}
					return "Setting log level returned unexpected status " + status + " for " + applicationName + ".";
				})
				.onErrorResume((ex) -> Mono.just("Error setting log level for '" + loggerName + "' in "
						+ applicationName + ": " + ex.getMessage()));
		});
	}

	@SuppressWarnings("unchecked")
	private String formatLoggers(String applicationName, String filter, Map<String, Object> body) {
		Object loggersObj = body.get("loggers");
		if (!(loggersObj instanceof Map<?, ?> loggersMap) || loggersMap.isEmpty()) {
			return "No loggers available for " + applicationName + ".";
		}

		boolean filtered = filter != null && !filter.isBlank();
		String needle = filtered ? filter.toLowerCase(Locale.ROOT) : null;

		List<String> levels = null;
		Object levelsObj = body.get("levels");
		if (levelsObj instanceof List<?> l) {
			levels = (List<String>) l;
		}

		StringBuilder sb = new StringBuilder("Loggers for ").append(applicationName);
		if (filtered) {
			sb.append(" (filtered by \"").append(filter).append("\")");
		}
		if (levels != null) {
			sb.append(" — available levels: ").append(levels);
		}
		sb.append(":\n");

		int count = 0;
		for (Map.Entry<?, ?> entry : loggersMap.entrySet()) {
			String name = String.valueOf(entry.getKey());
			if (filtered && !name.toLowerCase(Locale.ROOT).contains(needle)) {
				continue;
			}
			count++;
			sb.append("  ").append(name);
			if (entry.getValue() instanceof Map<?, ?> loggerInfo) {
				Object effective = loggerInfo.get("effectiveLevel");
				Object configured = loggerInfo.get("configuredLevel");
				sb.append(": effective=").append((effective != null) ? effective : "inherited");
				if (configured != null) {
					sb.append(", configured=").append(configured);
				}
			}
			sb.append("\n");
		}

		if (filtered && count == 0) {
			return "No loggers matching '" + filter + "' for " + applicationName + ".";
		}
		return sb.toString().trim();
	}

	private String formatLogger(String applicationName, String loggerName, Map<String, Object> body) {
		StringBuilder sb = new StringBuilder(applicationName).append(" — logger '").append(loggerName).append("':\n");
		Object effective = body.get("effectiveLevel");
		Object configured = body.get("configuredLevel");
		sb.append("  effectiveLevel: ").append((effective != null) ? effective : "inherited").append("\n");
		if (configured != null) {
			sb.append("  configuredLevel: ").append(configured).append("\n");
		}
		return sb.toString().trim();
	}

}
