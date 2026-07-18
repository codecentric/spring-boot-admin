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

import java.util.List;
import java.util.Map;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import reactor.core.publisher.Mono;

/**
 * MCP tools for querying metrics of registered Spring Boot applications.
 *
 * <ul>
 * <li>{@code list-metrics} — lists all available metric names for an application</li>
 * <li>{@code get-metrics} — fetches the value of a specific metric</li>
 * </ul>
 */
public class MetricsTools {

	private final ActuatorClient actuatorClient;

	/**
	 * Creates a new {@code MetricsTools} instance.
	 * @param actuatorClient the shared actuator call helper
	 */
	public MetricsTools(ActuatorClient actuatorClient) {
		this.actuatorClient = actuatorClient;
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
		return this.actuatorClient.query(applicationName, "/metrics", this::formatMetricNames);
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
		return this.actuatorClient.query(applicationName, "/metrics/" + metricName,
				(app, body) -> formatMetricValue(app, metricName, body));
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
