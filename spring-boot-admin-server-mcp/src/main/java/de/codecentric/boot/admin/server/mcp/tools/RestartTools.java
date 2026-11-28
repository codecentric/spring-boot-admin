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
 * MCP write tool for restarting registered Spring Boot applications.
 *
 * <ul>
 * <li>{@code restart-application} — restarts the application via
 * {@code /actuator/restart}</li>
 * </ul>
 */
public class RestartTools {

	private static final Logger log = LoggerFactory.getLogger(RestartTools.class);

	private final ActuatorClient actuatorClient;

	/**
	 * Creates a new {@code RestartTools} instance.
	 * @param actuatorClient the shared actuator call helper
	 */
	public RestartTools(ActuatorClient actuatorClient) {
		this.actuatorClient = actuatorClient;
	}

	/**
	 * Restarts the named application by calling its {@code /actuator/restart} endpoint.
	 * Requires the actuator restart endpoint to be enabled and exposed in the monitored
	 * application.
	 * @param applicationName the registered application name (case-sensitive)
	 * @return confirmation message or an error message
	 */
	@McpTool(name = "restart-application",
			annotations = @McpTool.McpAnnotations(readOnlyHint = false, destructiveHint = true, idempotentHint = false,
					openWorldHint = false),
			description = "Restart a registered Spring Boot application via its /actuator/restart endpoint. "
					+ "Requires management.endpoint.restart.enabled=true in the monitored application.")
	public Mono<String> restartApplication(@McpToolParam(
			description = "The registered application name (case-sensitive)", required = true) String applicationName) {
		return this.actuatorClient.withInstance(applicationName, (instance) -> {
			String url = instance.getRegistration().getManagementUrl() + "/restart";
			return this.actuatorClient.postBodiless(instance, url, "{}", log, "restart of " + applicationName)
				.map((status) -> {
					if ((status >= 200) && (status < 300)) {
						return applicationName + " restart initiated successfully.";
					}
					return "Restart returned unexpected status " + status + " for " + applicationName + ".";
				})
				.onErrorResume((ex) -> Mono.just("Error restarting " + applicationName + ": " + ex.getMessage()));
		});
	}

}
