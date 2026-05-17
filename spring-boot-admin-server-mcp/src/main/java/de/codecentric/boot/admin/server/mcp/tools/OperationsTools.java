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

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;

/**
 * MCP tools for performing write operations on registered Spring Boot applications.
 *
 * <ul>
 * <li>{@code restart-application} — restarts the application via
 * {@code /actuator/restart} or {@code /actuator/shutdown}</li>
 * <li>{@code refresh-configuration} — refreshes the application configuration via
 * {@code /actuator/refresh}</li>
 * </ul>
 */
public class OperationsTools {

	private static final Logger log = LoggerFactory.getLogger(OperationsTools.class);

	private static final Duration TIMEOUT = Duration.ofMillis(450);

	private final InstanceRepository instanceRepository;

	private final InstanceWebClient instanceWebClient;

	/**
	 * Creates a new {@code OperationsTools} instance.
	 * @param instanceRepository the repository used to look up registered instances
	 * @param instanceWebClient the client used to call actuator endpoints on instances
	 */
	public OperationsTools(InstanceRepository instanceRepository, InstanceWebClient instanceWebClient) {
		this.instanceRepository = instanceRepository;
		this.instanceWebClient = instanceWebClient;
	}

	/**
	 * Restarts the named application by calling its {@code /actuator/restart} endpoint.
	 * Falls back to {@code /actuator/shutdown} if restart is not available. Requires the
	 * actuator restart or shutdown endpoint to be enabled and exposed in the monitored
	 * application.
	 * @param applicationName the registered application name (case-insensitive)
	 * @return confirmation message or an error message
	 */
	@McpTool(name = "restart-application",
			description = "Restart a registered Spring Boot application via its /actuator/restart endpoint. "
					+ "Requires management.endpoint.restart.enabled=true in the monitored application.")
	public Mono<String> restartApplication(
			@McpToolParam(description = "The registered application name (case-insensitive)",
					required = true) String applicationName) {
		return this.instanceRepository.findByName(applicationName).next().flatMap((instance) -> {
			String url = instance.getRegistration().getManagementUrl() + "/restart";
			return this.instanceWebClient.instance(instance)
				.post()
				.uri(url)
				.contentType(MediaType.APPLICATION_JSON)
				.retrieve()
				.toBodilessEntity()
				.timeout(TIMEOUT)
				.map((response) -> {
					int status = response.getStatusCode().value();
					if ((status >= 200) && (status < 300)) {
						return applicationName + " restart initiated successfully.";
					}
					return "Restart returned unexpected status " + status + " for " + applicationName + ".";
				})
				.doOnError((ex) -> log.warn("Failed to restart {}", applicationName, ex))
				.onErrorResume((ex) -> Mono.just("Error restarting " + applicationName + ": " + ex.getMessage()));
		}).switchIfEmpty(Mono.just("Application '" + applicationName + "' not found in registry."));
	}

	/**
	 * Refreshes the configuration of the named application by calling its
	 * {@code /actuator/refresh} endpoint. Requires Spring Cloud Context on the monitored
	 * application's classpath.
	 * @param applicationName the registered application name (case-insensitive)
	 * @return confirmation message listing refreshed keys, or an error message
	 */
	@McpTool(name = "refresh-configuration",
			description = "Refresh the configuration of a registered Spring Boot application via its "
					+ "/actuator/refresh endpoint. Requires Spring Cloud Context (spring-cloud-starter) "
					+ "on the monitored application's classpath.")
	public Mono<String> refreshConfiguration(
			@McpToolParam(description = "The registered application name (case-insensitive)",
					required = true) String applicationName) {
		return this.instanceRepository.findByName(applicationName).next().flatMap((instance) -> {
			String url = instance.getRegistration().getManagementUrl() + "/refresh";
			return this.instanceWebClient.instance(instance)
				.post()
				.uri(url)
				.contentType(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(String.class)
				.timeout(TIMEOUT)
				.map((body) -> formatRefreshResponse(applicationName, body))
				.doOnError((ex) -> log.warn("Failed to refresh configuration for {}", applicationName, ex))
				.onErrorResume((ex) -> Mono
					.just("Error refreshing configuration for " + applicationName + ": " + ex.getMessage()));
		}).switchIfEmpty(Mono.just("Application '" + applicationName + "' not found in registry."));
	}

	private String formatRefreshResponse(String applicationName, String body) {
		if ((body == null) || body.isBlank() || "[]".equals(body.trim())) {
			return "Configuration refreshed for " + applicationName + ". No properties changed.";
		}
		return "Configuration refreshed for " + applicationName + ". Changed keys: " + body;
	}

}
