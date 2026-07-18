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
import reactor.core.publisher.Mono;

/**
 * MCP tools for capturing thread dumps from registered Spring Boot applications.
 *
 * <ul>
 * <li>{@code get-thread-dump} — retrieves a thread dump from the application's
 * {@code /actuator/threaddump} endpoint</li>
 * </ul>
 */
public class ThreadDumpTools {

	private static final Logger log = LoggerFactory.getLogger(ThreadDumpTools.class);

	private final ActuatorClient actuatorClient;

	private final Duration timeout;

	/**
	 * Creates a new {@code ThreadDumpTools} instance.
	 * @param actuatorClient the shared actuator call helper
	 * @param timeout the request timeout for thread dump calls (can be slower than
	 * standard actuator endpoints)
	 */
	public ThreadDumpTools(ActuatorClient actuatorClient, Duration timeout) {
		this.actuatorClient = actuatorClient;
		this.timeout = timeout;
	}

	/**
	 * Retrieves a thread dump for the named application by calling its
	 * {@code /actuator/threaddump} endpoint. Returns a human-readable summary of all
	 * threads including their state and stack traces. Useful for diagnosing deadlocks,
	 * hung threads, and thread pool saturation.
	 * @param applicationName the registered application name (case-insensitive)
	 * @return plain-text thread dump summary, or an error message
	 */
	@McpTool(name = "get-thread-dump",
			description = "Retrieve a thread dump from a registered Spring Boot application via its "
					+ "/actuator/threaddump endpoint. Returns all threads with their state and stack traces. "
					+ "Useful for diagnosing deadlocks, hung threads, and thread pool saturation. "
					+ "Requires the threaddump actuator endpoint to be exposed.")
	public Mono<String> getThreadDump(@McpToolParam(description = "The registered application name (case-insensitive)",
			required = true) String applicationName) {
		return this.actuatorClient.withInstance(applicationName, (instance) -> {
			String url = instance.getRegistration().getManagementUrl() + "/threaddump";
			return this.actuatorClient.fetch(instance, url, this.timeout, log, "thread dump for " + applicationName)
				.map((body) -> formatThreadDump(applicationName, body))
				.onErrorResume((ex) -> Mono
					.just("Error retrieving thread dump for " + applicationName + ": " + ex.getMessage()));
		});
	}

	@SuppressWarnings("unchecked")
	private String formatThreadDump(String applicationName, Map<String, Object> body) {
		Object threadsObj = body.get("threads");
		if (!(threadsObj instanceof List<?> threads) || threads.isEmpty()) {
			return "No thread dump available for " + applicationName + ".";
		}

		java.util.Map<String, Integer> stateCounts = new java.util.LinkedHashMap<>();
		StringBuilder detail = new StringBuilder();

		for (Object threadObj : threads) {
			if (!(threadObj instanceof Map<?, ?> thread)) {
				continue;
			}
			Object nameObj = thread.get("threadName");
			Object stateObj = thread.get("threadState");
			String threadName = (nameObj != null) ? String.valueOf(nameObj) : "unknown";
			String threadState = (stateObj != null) ? String.valueOf(stateObj) : "UNKNOWN";
			boolean blocked = Boolean.TRUE.equals(thread.get("blocked"));
			boolean suspended = Boolean.TRUE.equals(thread.get("suspended"));

			stateCounts.merge(threadState, 1, Integer::sum);

			detail.append("\n[").append(threadState);
			if (blocked) {
				detail.append(", BLOCKED");
			}
			if (suspended) {
				detail.append(", SUSPENDED");
			}
			detail.append("] ").append(threadName).append("\n");

			Object stackTrace = thread.get("stackTrace");
			if (stackTrace instanceof List<?> frames && !frames.isEmpty()) {
				int limit = Math.min(frames.size(), 8);
				for (int i = 0; i < limit; i++) {
					Object frame = frames.get(i);
					if (frame instanceof Map<?, ?> f) {
						Object classNameObj = f.get("className");
						Object methodNameObj = f.get("methodName");
						String className = (classNameObj != null) ? String.valueOf(classNameObj) : "";
						String methodName = (methodNameObj != null) ? String.valueOf(methodNameObj) : "";
						Object lineNumber = f.get("lineNumber");
						detail.append("  at ").append(className).append(".").append(methodName);
						if (lineNumber != null) {
							detail.append("(line:").append(lineNumber).append(")");
						}
						detail.append("\n");
					}
				}
				if (frames.size() > limit) {
					detail.append("  ... ").append(frames.size() - limit).append(" more\n");
				}
			}
		}

		StringBuilder sb = new StringBuilder("Thread dump for ").append(applicationName)
			.append(" (")
			.append(threads.size())
			.append(" threads):\n");
		sb.append("Thread states: ");
		stateCounts.forEach((state, count) -> sb.append(state).append("=").append(count).append(" "));
		sb.append("\n");
		sb.append(detail);
		return sb.toString().trim();
	}

}
