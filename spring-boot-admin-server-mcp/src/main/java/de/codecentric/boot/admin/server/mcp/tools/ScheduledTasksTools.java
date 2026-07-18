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
 * MCP tools for inspecting scheduled tasks of registered Spring Boot applications.
 *
 * <ul>
 * <li>{@code get-scheduled-tasks} — lists all {@code @Scheduled} methods and their
 * configuration via the application's {@code /actuator/scheduledtasks} endpoint</li>
 * </ul>
 */
public class ScheduledTasksTools {

	private static final Logger log = LoggerFactory.getLogger(ScheduledTasksTools.class);

	private static final Duration TIMEOUT = Duration.ofMillis(450);

	private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE = new ParameterizedTypeReference<>() {
	};

	private final InstanceRepository instanceRepository;

	private final InstanceWebClient instanceWebClient;

	/**
	 * Creates a new {@code ScheduledTasksTools} instance.
	 * @param instanceRepository the repository used to look up registered instances
	 * @param instanceWebClient the client used to call actuator endpoints on instances
	 */
	public ScheduledTasksTools(InstanceRepository instanceRepository, InstanceWebClient instanceWebClient) {
		this.instanceRepository = instanceRepository;
		this.instanceWebClient = instanceWebClient;
	}

	/**
	 * Lists all scheduled tasks for the named application by calling its
	 * {@code /actuator/scheduledtasks} endpoint. Returns a summary of all
	 * {@code @Scheduled} methods, including their cron expressions, fixed-rate, and
	 * fixed-delay configurations.
	 * @param applicationName the registered application name (case-insensitive)
	 * @return plain-text listing of scheduled tasks, or an error message
	 */
	@McpTool(name = "get-scheduled-tasks",
			description = "List all scheduled tasks (@Scheduled methods) and their configuration for a registered "
					+ "Spring Boot application via its /actuator/scheduledtasks endpoint. Returns cron expressions, "
					+ "fixed-rate, and fixed-delay settings. Useful for verifying that batch jobs and cron tasks "
					+ "are configured as expected. Requires the scheduledtasks actuator endpoint to be exposed.")
	public Mono<String> getScheduledTasks(
			@McpToolParam(description = "The registered application name (case-insensitive)",
					required = true) String applicationName) {
		return this.instanceRepository.findByName(applicationName).next().flatMap((instance) -> {
			String url = instance.getRegistration().getManagementUrl() + "/scheduledtasks";
			return this.instanceWebClient.instance(instance)
				.get()
				.uri(url)
				.retrieve()
				.bodyToMono(RESPONSE_TYPE)
				.timeout(TIMEOUT)
				.map((body) -> formatScheduledTasks(applicationName, body))
				.doOnError((ex) -> log.warn("Failed to get scheduled tasks for {}", applicationName, ex))
				.onErrorResume((ex) -> Mono
					.just("Error retrieving scheduled tasks for " + applicationName + ": " + ex.getMessage()));
		}).switchIfEmpty(Mono.just("Application '" + applicationName + "' not found in registry."));
	}

	@SuppressWarnings("unchecked")
	private String formatScheduledTasks(String applicationName, Map<String, Object> body) {
		StringBuilder sb = new StringBuilder("Scheduled tasks for ").append(applicationName).append(":\n");
		int total = 0;

		// cron tasks
		Object cronObj = body.get("cron");
		if (cronObj instanceof List<?> cronTasks && !cronTasks.isEmpty()) {
			sb.append("\nCron tasks (").append(cronTasks.size()).append("):\n");
			for (Object taskObj : cronTasks) {
				if (!(taskObj instanceof Map<?, ?> task)) {
					continue;
				}
				total++;
				appendTaskRunnable(sb, task);
				Object expression = task.get("expression");
				if (expression != null) {
					sb.append("  expression: ").append(expression).append("\n");
				}
			}
		}

		// fixed-rate tasks
		Object fixedRateObj = body.get("fixedRate");
		if (fixedRateObj instanceof List<?> fixedRateTasks && !fixedRateTasks.isEmpty()) {
			sb.append("\nFixed-rate tasks (").append(fixedRateTasks.size()).append("):\n");
			for (Object taskObj : fixedRateTasks) {
				if (!(taskObj instanceof Map<?, ?> task)) {
					continue;
				}
				total++;
				appendTaskRunnable(sb, task);
				appendIntervalDetails(sb, task);
			}
		}

		// fixed-delay tasks
		Object fixedDelayObj = body.get("fixedDelay");
		if (fixedDelayObj instanceof List<?> fixedDelayTasks && !fixedDelayTasks.isEmpty()) {
			sb.append("\nFixed-delay tasks (").append(fixedDelayTasks.size()).append("):\n");
			for (Object taskObj : fixedDelayTasks) {
				if (!(taskObj instanceof Map<?, ?> task)) {
					continue;
				}
				total++;
				appendTaskRunnable(sb, task);
				appendIntervalDetails(sb, task);
			}
		}

		if (total == 0) {
			return "No scheduled tasks found for " + applicationName + ".";
		}
		return sb.toString().trim();
	}

	private void appendTaskRunnable(StringBuilder sb, Map<?, ?> task) {
		Object runnableObj = task.get("runnable");
		if (runnableObj instanceof Map<?, ?> runnable) {
			sb.append("  ").append(runnable.get("target")).append("\n");
		}
	}

	private void appendIntervalDetails(StringBuilder sb, Map<?, ?> task) {
		Object interval = task.get("interval");
		Object initialDelay = task.get("initialDelay");
		if (interval != null) {
			sb.append("  interval: ").append(interval).append("ms");
		}
		if (initialDelay != null) {
			sb.append(", initialDelay: ").append(initialDelay).append("ms");
		}
		if (interval != null || initialDelay != null) {
			sb.append("\n");
		}
	}

}
