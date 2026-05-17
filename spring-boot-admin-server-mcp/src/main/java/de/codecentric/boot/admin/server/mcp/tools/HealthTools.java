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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.core.ParameterizedTypeReference;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.values.Endpoint;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;

/**
 * MCP tools for querying the health of registered Spring Boot applications.
 *
 * <p>
 * Exposes two tools:
 * <ul>
 * <li>{@code get-health} — fetches full health details from the application's
 * {@code /actuator/health} endpoint</li>
 * <li>{@code get-status} — returns the cached status (UP/DOWN/UNKNOWN) from the registry
 * without an actuator call</li>
 * </ul>
 */
public class HealthTools {

	private static final Logger log = LoggerFactory.getLogger(HealthTools.class);

	private static final Duration TIMEOUT = Duration.ofMillis(450);

	private static final ParameterizedTypeReference<Map<String, Object>> HEALTH_RESPONSE_TYPE = new ParameterizedTypeReference<>() {
	};

	private final InstanceRepository instanceRepository;

	private final InstanceWebClient instanceWebClient;

	/**
	 * Creates a new {@code HealthTools} instance.
	 * @param instanceRepository the repository used to look up registered instances
	 * @param instanceWebClient the client used to call actuator endpoints on instances
	 */
	public HealthTools(InstanceRepository instanceRepository, InstanceWebClient instanceWebClient) {
		this.instanceRepository = instanceRepository;
		this.instanceWebClient = instanceWebClient;
	}

	/**
	 * Fetches the full health details for the named application by calling its
	 * {@code /actuator/health} endpoint. Returns a plain-text summary including the
	 * overall status and individual health component statuses.
	 * @param applicationName the registered application name (case-insensitive)
	 * @return plain-text health summary, or an error message if the app is not found or
	 * the actuator call fails
	 */
	@McpTool(name = "get-health",
			description = "Fetch health details for a registered Spring Boot application by calling its "
					+ "/actuator/health endpoint. Returns overall status and per-component breakdown.")
	public Mono<String> getHealth(@McpToolParam(description = "The registered application name (case-insensitive)",
			required = true) String applicationName) {
		return this.instanceRepository.findByName(applicationName)
			.next()
			.flatMap((instance) -> this.instanceWebClient.instance(instance)
				.get()
				.uri(Endpoint.HEALTH)
				.retrieve()
				.bodyToMono(HEALTH_RESPONSE_TYPE)
				.timeout(TIMEOUT)
				.map((body) -> formatHealthResponse(applicationName, body))
				.doOnError((ex) -> log.warn("Failed to get health for {}", applicationName, ex))
				.onErrorResume(
						(ex) -> Mono.just("Error retrieving health for " + applicationName + ": " + ex.getMessage())))
			.switchIfEmpty(Mono.just("Application '" + applicationName + "' not found in registry."));
	}

	/**
	 * Returns the cached status for the named application from the registry without
	 * making an actuator call. This is a fast, lightweight alternative to
	 * {@code get-health}.
	 * @param applicationName the registered application name (case-insensitive)
	 * @return plain-text status line, or an error message if the app is not found
	 */
	@McpTool(name = "get-status",
			description = "Return the cached health status (UP/DOWN/UNKNOWN/etc.) for a registered "
					+ "Spring Boot application from the registry — no actuator call is made. "
					+ "Use get-health for full component details.")
	public Mono<String> getStatus(@McpToolParam(description = "The registered application name (case-insensitive)",
			required = true) String applicationName) {
		return this.instanceRepository.findByName(applicationName)
			.next()
			.map((instance) -> applicationName + " status: " + instance.getStatusInfo().getStatus())
			.switchIfEmpty(Mono.just("Application '" + applicationName + "' not found in registry."))
			.doOnError((ex) -> log.warn("Failed to get status for {}", applicationName, ex))
			.onErrorReturn("Error retrieving status for " + applicationName + ".");
	}

	@SuppressWarnings("unchecked")
	private String formatHealthResponse(String applicationName, Map<String, Object> body) {
		StringBuilder sb = new StringBuilder();
		Object status = body.get("status");
		sb.append(applicationName).append(" health: ").append((status != null) ? status : "UNKNOWN").append("\n");

		Object components = body.get("components");
		if (components instanceof Map<?, ?> componentMap) {
			for (Map.Entry<?, ?> entry : componentMap.entrySet()) {
				String componentName = String.valueOf(entry.getKey());
				Object componentValue = entry.getValue();
				if (componentValue instanceof Map<?, ?> componentDetails) {
					Object componentStatus = componentDetails.get("status");
					sb.append("  ")
						.append(componentName)
						.append(": ")
						.append((componentStatus != null) ? componentStatus : "UNKNOWN")
						.append("\n");
				}
			}
		}
		return sb.toString().trim();
	}

}
