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
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.web.util.UriUtils;
import reactor.core.publisher.Mono;

/**
 * MCP write tool for changing log levels of registered Spring Boot applications at
 * runtime.
 *
 * <ul>
 * <li>{@code set-logger-level} — changes the log level of a logger at runtime</li>
 * </ul>
 *
 * <p>
 * Kept separate from the read-only {@link LoggersTools} so that logger inspection can
 * remain available while logger mutation is disabled.
 * </p>
 */
public class LoggerLevelTools {

	private static final Logger log = LoggerFactory.getLogger(LoggerLevelTools.class);

	private final ActuatorClient actuatorClient;

	/**
	 * Creates a new {@code LoggerLevelTools} instance.
	 * @param actuatorClient the shared actuator call helper
	 */
	public LoggerLevelTools(ActuatorClient actuatorClient) {
		this.actuatorClient = actuatorClient;
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
			annotations = @McpTool.McpAnnotations(readOnlyHint = false, destructiveHint = false, idempotentHint = true,
					openWorldHint = false),
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

}
