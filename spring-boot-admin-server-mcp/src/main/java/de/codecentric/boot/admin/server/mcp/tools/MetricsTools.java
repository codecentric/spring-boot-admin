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
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.core.ParameterizedTypeReference;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;

/**
 * MCP tools for querying metrics of registered Spring Boot applications.
 *
 * <ul>
 * <li>{@code list-metrics} — lists all available metric names for an application</li>
 * <li>{@code get-metrics} — fetches the value of a specific metric</li>
 * </ul>
 */
public class MetricsTools {

	private static final Logger log = LoggerFactory.getLogger(MetricsTools.class);

	private static final Duration TIMEOUT = Duration.ofMillis(450);

	private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE = new ParameterizedTypeReference<>() {
	};

	private final InstanceRepository instanceRepository;

	private final InstanceWebClient instanceWebClient;

	/**
	 * Creates a new {@code MetricsTools} instance.
	 * @param instanceRepository the repository used to look up registered instances
	 * @param instanceWebClient the client used to call actuator endpoints on instances
	 */
	public MetricsTools(InstanceRepository instanceRepository, InstanceWebClient instanceWebClient) {
		this.instanceRepository = instanceRepository;
		this.instanceWebClient = instanceWebClient;
	}

	/**
	 * Lists all available metric names for the named application by calling its
	 * {@code /actuator/metrics} endpoint.
	 * @param applicationName the registered application name (case-insensitive)
	 * @return plain-text list of metric names, or an error message
	 */
	@McpTool(name = "list-metrics",
			description = "List all available metric names for a registered Spring Boot application. "
					+ "Use the returned names with get-metrics to fetch specific values.")
	public Mono<String> listMetrics(@McpToolParam(description = "The registered application name (case-insensitive)",
			required = true) String applicationName) {
		return this.instanceRepository.findByName(applicationName).next().flatMap((instance) -> {
			String url = instance.getRegistration().getManagementUrl() + "/metrics";
			return this.instanceWebClient.instance(instance)
				.get()
				.uri(url)
				.retrieve()
				.bodyToMono(RESPONSE_TYPE)
				.timeout(TIMEOUT)
				.map((body) -> formatMetricNames(applicationName, body))
				.doOnError((ex) -> log.warn("Failed to list metrics for {}", applicationName, ex))
				.onErrorResume(
						(ex) -> Mono.just("Error listing metrics for " + applicationName + ": " + ex.getMessage()));
		}).switchIfEmpty(Mono.just("Application '" + applicationName + "' not found in registry."));
	}

	/**
	 * Fetches the value of a specific metric for the named application by calling its
	 * {@code /actuator/metrics/{metricName}} endpoint.
	 * @param applicationName the registered application name (case-insensitive)
	 * @param metricName the metric name (e.g. {@code jvm.memory.used})
	 * @return plain-text metric value with unit, or an error message
	 */
	@McpTool(name = "get-metrics",
			description = "Fetch the current value of a specific metric for a registered Spring Boot application. "
					+ "Common metrics: jvm.memory.used, jvm.memory.max, system.cpu.usage, "
					+ "http.server.requests, jvm.threads.live. Use list-metrics to discover available names.")
	public Mono<String> getMetrics(
			@McpToolParam(description = "The registered application name (case-insensitive)",
					required = true) String applicationName,
			@McpToolParam(description = "The metric name (e.g. jvm.memory.used)", required = true) String metricName) {
		return this.instanceRepository.findByName(applicationName).next().flatMap((instance) -> {
			String url = instance.getRegistration().getManagementUrl() + "/metrics/" + metricName;
			return this.instanceWebClient.instance(instance)
				.get()
				.uri(url)
				.retrieve()
				.bodyToMono(RESPONSE_TYPE)
				.timeout(TIMEOUT)
				.map((body) -> formatMetricValue(applicationName, metricName, body))
				.doOnError((ex) -> log.warn("Failed to get metric {} for {}", metricName, applicationName, ex))
				.onErrorResume((ex) -> Mono.just("Error retrieving metric '" + metricName + "' for " + applicationName
						+ ": " + ex.getMessage()));
		}).switchIfEmpty(Mono.just("Application '" + applicationName + "' not found in registry."));
	}

	@SuppressWarnings("unchecked")
	private String formatMetricNames(String applicationName, Map<String, Object> body) {
		Object names = body.get("names");
		if (!(names instanceof List<?> nameList) || nameList.isEmpty()) {
			return "No metrics available for " + applicationName + ".";
		}
		StringBuilder sb = new StringBuilder("Available metrics for ").append(applicationName)
			.append(" (")
			.append(nameList.size())
			.append("):\n");
		nameList.forEach((name) -> sb.append("- ").append(name).append("\n"));
		return sb.toString().trim();
	}

	@SuppressWarnings("unchecked")
	private String formatMetricValue(String applicationName, String metricName, Map<String, Object> body) {
		StringBuilder sb = new StringBuilder(applicationName).append(" — ").append(metricName).append(":\n");
		Object measurements = body.get("measurements");
		if (measurements instanceof List<?> list) {
			for (Object item : list) {
				if (item instanceof Map<?, ?> m) {
					Object statistic = m.get("statistic");
					Object value = m.get("value");
					sb.append("  ").append(statistic).append(": ").append(value);
					Object baseUnit = body.get("baseUnit");
					if (baseUnit != null) {
						sb.append(" ").append(baseUnit);
					}
					sb.append("\n");
				}
			}
		}
		return sb.toString().trim();
	}

}
