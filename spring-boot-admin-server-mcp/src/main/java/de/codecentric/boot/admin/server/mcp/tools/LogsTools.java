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
import java.util.Arrays;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;

/**
 * MCP tools for accessing logs of registered Spring Boot applications.
 *
 * <ul>
 * <li>{@code get-logs} — fetches the last N lines from the application's logfile</li>
 * </ul>
 */
public class LogsTools {

	private static final Logger log = LoggerFactory.getLogger(LogsTools.class);

	private static final Duration TIMEOUT = Duration.ofMillis(450);

	private static final int DEFAULT_LINES = 50;

	private static final int MAX_LINES = 500;

	private final InstanceRepository instanceRepository;

	private final InstanceWebClient instanceWebClient;

	/**
	 * Creates a new {@code LogsTools} instance.
	 * @param instanceRepository the repository used to look up registered instances
	 * @param instanceWebClient the client used to call actuator endpoints on instances
	 */
	public LogsTools(InstanceRepository instanceRepository, InstanceWebClient instanceWebClient) {
		this.instanceRepository = instanceRepository;
		this.instanceWebClient = instanceWebClient;
	}

	/**
	 * Fetches the last N lines from the logfile of the named application by calling its
	 * {@code /actuator/logfile} endpoint.
	 * @param applicationName the registered application name (case-insensitive)
	 * @param lines number of lines to return from the end of the log (default 50, max
	 * 500)
	 * @return plain-text log tail, or an error message
	 */
	@McpTool(name = "get-logs",
			description = "Fetch the last N lines from the logfile of a registered Spring Boot application. "
					+ "Requires logging.file.name or logging.file.path to be configured in the application. "
					+ "Default is 50 lines, maximum is 500.")
	public Mono<String> getLogs(
			@McpToolParam(description = "The registered application name (case-insensitive)",
					required = true) String applicationName,
			@McpToolParam(description = "Number of lines to return from the end of the log (default 50, max 500)",
					required = false) Integer lines) {
		int lineCount = (lines != null) ? Math.min(Math.max(lines, 1), MAX_LINES) : DEFAULT_LINES;

		return this.instanceRepository.findByName(applicationName).next().flatMap((instance) -> {
			String url = instance.getRegistration().getManagementUrl() + "/logfile";
			return this.instanceWebClient.instance(instance)
				.get()
				.uri(url)
				.retrieve()
				.bodyToMono(String.class)
				.defaultIfEmpty("")
				.timeout(TIMEOUT)
				.map((body) -> formatLogTail(applicationName, body, lineCount))
				.doOnError((ex) -> log.warn("Failed to get logs for {}", applicationName, ex))
				.onErrorResume(
						(ex) -> Mono.just("Error retrieving logs for " + applicationName + ": " + ex.getMessage()));
		}).switchIfEmpty(Mono.just("Application '" + applicationName + "' not found in registry."));
	}

	private String formatLogTail(String applicationName, String body, int lineCount) {
		if (body == null || body.isBlank()) {
			return "No log content available for " + applicationName + ".";
		}
		String[] allLines = body.split("\n");
		int from = Math.max(0, allLines.length - lineCount);
		String tail = Arrays.stream(allLines, from, allLines.length).collect(Collectors.joining("\n"));
		return "Last " + Math.min(lineCount, allLines.length) + " lines of " + applicationName + " log:\n" + tail;
	}

}
