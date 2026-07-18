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
 * MCP tools for inspecting recent HTTP exchanges of registered Spring Boot applications.
 *
 * <ul>
 * <li>{@code get-http-exchanges} — retrieves recent HTTP request/response records from
 * the application's {@code /actuator/httpexchanges} endpoint</li>
 * </ul>
 */
public class HttpExchangesTools {

	private static final Logger log = LoggerFactory.getLogger(HttpExchangesTools.class);

	private static final Duration TIMEOUT = Duration.ofMillis(450);

	private static final int DEFAULT_LIMIT = 20;

	private static final int MAX_LIMIT = 100;

	private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE = new ParameterizedTypeReference<>() {
	};

	private final InstanceRepository instanceRepository;

	private final InstanceWebClient instanceWebClient;

	/**
	 * Creates a new {@code HttpExchangesTools} instance.
	 * @param instanceRepository the repository used to look up registered instances
	 * @param instanceWebClient the client used to call actuator endpoints on instances
	 */
	public HttpExchangesTools(InstanceRepository instanceRepository, InstanceWebClient instanceWebClient) {
		this.instanceRepository = instanceRepository;
		this.instanceWebClient = instanceWebClient;
	}

	/**
	 * Retrieves recent HTTP exchanges for the named application by calling its
	 * {@code /actuator/httpexchanges} endpoint. Returns a summary of recent HTTP requests
	 * and their responses including method, URI, status code, and duration.
	 * @param applicationName the registered application name (case-insensitive)
	 * @param limit maximum number of exchanges to return (default 20, max 100)
	 * @return plain-text listing of recent HTTP exchanges, or an error message
	 */
	@McpTool(name = "get-http-exchanges",
			description = "Retrieve recent HTTP exchanges (requests and responses) for a registered Spring Boot "
					+ "application via its /actuator/httpexchanges endpoint. Returns method, URI, status code, "
					+ "and duration for each exchange. Useful for tracing requests and debugging client errors. "
					+ "Requires management.httpexchanges.recording.enabled=true and the httpexchanges actuator "
					+ "endpoint to be exposed (Spring Boot 3.x).")
	public Mono<String> getHttpExchanges(
			@McpToolParam(description = "The registered application name (case-insensitive)",
					required = true) String applicationName,
			@McpToolParam(description = "Maximum number of exchanges to return (default 20, max 100)",
					required = false) Integer limit) {
		int effectiveLimit = (limit != null) ? Math.min(Math.max(limit, 1), MAX_LIMIT) : DEFAULT_LIMIT;

		return this.instanceRepository.findByName(applicationName).next().flatMap((instance) -> {
			String url = instance.getRegistration().getManagementUrl() + "/httpexchanges";
			return this.instanceWebClient.instance(instance)
				.get()
				.uri(url)
				.retrieve()
				.bodyToMono(RESPONSE_TYPE)
				.timeout(TIMEOUT)
				.map((body) -> formatExchanges(applicationName, body, effectiveLimit))
				.doOnError((ex) -> log.warn("Failed to get HTTP exchanges for {}", applicationName, ex))
				.onErrorResume((ex) -> Mono
					.just("Error retrieving HTTP exchanges for " + applicationName + ": " + ex.getMessage()));
		}).switchIfEmpty(Mono.just("Application '" + applicationName + "' not found in registry."));
	}

	@SuppressWarnings("unchecked")
	private String formatExchanges(String applicationName, Map<String, Object> body, int limit) {
		Object exchangesObj = body.get("exchanges");
		if (!(exchangesObj instanceof List<?> exchanges) || exchanges.isEmpty()) {
			return "No HTTP exchanges recorded for " + applicationName + ".";
		}

		int total = exchanges.size();
		int from = Math.max(0, total - limit);
		List<?> recent = exchanges.subList(from, total);

		StringBuilder sb = new StringBuilder("Recent HTTP exchanges for ").append(applicationName)
			.append(" (showing ")
			.append(recent.size())
			.append(" of ")
			.append(total)
			.append("):\n");

		for (Object exchangeObj : recent) {
			if (!(exchangeObj instanceof Map<?, ?> exchange)) {
				continue;
			}
			Object tsObj = exchange.get("timestamp");
			String timestamp = (tsObj != null) ? String.valueOf(tsObj) : "";
			Object requestObj = exchange.get("request");
			Object responseObj = exchange.get("response");
			Object timeTaken = exchange.get("timeTaken");

			String method = "";
			String uri = "";
			if (requestObj instanceof Map<?, ?> request) {
				Object methodObj = request.get("method");
				Object uriObj = request.get("uri");
				method = (methodObj != null) ? String.valueOf(methodObj) : "";
				uri = (uriObj != null) ? String.valueOf(uriObj) : "";
			}

			String status = "";
			if (responseObj instanceof Map<?, ?> response) {
				Object statusObj = response.get("status");
				status = (statusObj != null) ? String.valueOf(statusObj) : "";
			}

			sb.append("  ").append(method).append(" ").append(uri).append(" -> ").append(status);
			if (timeTaken != null) {
				sb.append(" (").append(timeTaken).append(")");
			}
			if (!timestamp.isEmpty()) {
				sb.append(" [").append(timestamp).append("]");
			}
			sb.append("\n");
		}
		return sb.toString().trim();
	}

}
