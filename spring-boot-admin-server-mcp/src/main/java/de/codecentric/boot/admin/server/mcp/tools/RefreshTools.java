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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import reactor.core.publisher.Mono;

/**
 * MCP write tool for refreshing the configuration of registered Spring Boot applications.
 *
 * <ul>
 * <li>{@code refresh-configuration} — refreshes the application configuration via
 * {@code /actuator/refresh}</li>
 * </ul>
 */
public class RefreshTools {

	private static final Logger log = LoggerFactory.getLogger(RefreshTools.class);

	private final ActuatorClient actuatorClient;

	/**
	 * Creates a new {@code RefreshTools} instance.
	 * @param actuatorClient the shared actuator call helper
	 */
	public RefreshTools(ActuatorClient actuatorClient) {
		this.actuatorClient = actuatorClient;
	}

	/**
	 * Refreshes the configuration of the named application by calling its
	 * {@code /actuator/refresh} endpoint. Requires Spring Cloud Context on the monitored
	 * application's classpath.
	 * @param applicationName the registered application name (case-sensitive)
	 * @return confirmation message listing refreshed keys, or an error message
	 */
	@McpTool(name = "refresh-configuration",
			annotations = @McpTool.McpAnnotations(readOnlyHint = false, destructiveHint = true, idempotentHint = false,
					openWorldHint = false),
			description = "Refresh the configuration of a registered Spring Boot application via its "
					+ "/actuator/refresh endpoint. Requires Spring Cloud Context (spring-cloud-starter) "
					+ "on the monitored application's classpath.")
	public Mono<String> refreshConfiguration(@McpToolParam(
			description = "The registered application name (case-sensitive)", required = true) String applicationName) {
		return this.actuatorClient.withInstance(applicationName, (instance) -> {
			String url = instance.getRegistration().getManagementUrl() + "/refresh";
			return this.actuatorClient.post(instance, url, "{}", log, "refresh of " + applicationName)
				.map((body) -> formatRefreshResponse(applicationName, body))
				.onErrorResume((ex) -> Mono
					.just("Error refreshing configuration for " + applicationName + ": " + ex.getMessage()));
		});
	}

	private String formatRefreshResponse(String applicationName, String body) {
		if ((body == null) || body.isBlank() || "[]".equals(body.trim())) {
			return "Configuration refreshed for " + applicationName + ". No properties changed.";
		}
		return "Configuration refreshed for " + applicationName + ". Changed keys: " + body;
	}

}
