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
import java.util.Locale;
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
 * MCP tools for inspecting the Spring application context beans of registered Spring Boot
 * applications.
 *
 * <ul>
 * <li>{@code list-beans} — lists all beans in the application context, with optional
 * filtering by name or type</li>
 * </ul>
 */
public class BeansTools {

	private static final Logger log = LoggerFactory.getLogger(BeansTools.class);

	private static final Duration TIMEOUT = Duration.ofMillis(450);

	private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE = new ParameterizedTypeReference<>() {
	};

	private final InstanceRepository instanceRepository;

	private final InstanceWebClient instanceWebClient;

	/**
	 * Creates a new {@code BeansTools} instance.
	 * @param instanceRepository the repository used to look up registered instances
	 * @param instanceWebClient the client used to call actuator endpoints on instances
	 */
	public BeansTools(InstanceRepository instanceRepository, InstanceWebClient instanceWebClient) {
		this.instanceRepository = instanceRepository;
		this.instanceWebClient = instanceWebClient;
	}

	/**
	 * Lists all beans in the Spring application context of the named application by
	 * calling its {@code /actuator/beans} endpoint. An optional filter restricts the
	 * result to bean names or types containing the given text.
	 * @param applicationName the registered application name (case-insensitive)
	 * @param filter optional case-insensitive substring; only beans whose name or type
	 * contains it are returned. When {@code null} or blank, all beans are returned.
	 * @return plain-text listing of beans with their types, scopes, and dependencies, or
	 * an error message
	 */
	@McpTool(name = "list-beans",
			description = "List all beans in the Spring application context of a registered Spring Boot application "
					+ "via its /actuator/beans endpoint. Provide an optional 'filter' to return only beans whose "
					+ "name or type contains that text (case-insensitive). Returns bean name, type, scope, and "
					+ "resource. Useful for inspecting which beans are active in production. "
					+ "Requires the beans actuator endpoint to be exposed.")
	public Mono<String> listBeans(
			@McpToolParam(description = "The registered application name (case-insensitive)",
					required = true) String applicationName,
			@McpToolParam(description = "Optional case-insensitive substring; only beans whose name or type contain "
					+ "it are returned. Omit to return all beans.", required = false) String filter) {
		return this.instanceRepository.findByName(applicationName).next().flatMap((instance) -> {
			String url = instance.getRegistration().getManagementUrl() + "/beans";
			return this.instanceWebClient.instance(instance)
				.get()
				.uri(url)
				.retrieve()
				.bodyToMono(RESPONSE_TYPE)
				.timeout(TIMEOUT)
				.map((body) -> formatBeans(applicationName, filter, body))
				.doOnError((ex) -> log.warn("Failed to list beans for {}", applicationName, ex))
				.onErrorResume(
						(ex) -> Mono.just("Error listing beans for " + applicationName + ": " + ex.getMessage()));
		}).switchIfEmpty(Mono.just("Application '" + applicationName + "' not found in registry."));
	}

	@SuppressWarnings("unchecked")
	private String formatBeans(String applicationName, String filter, Map<String, Object> body) {
		Object contextsObj = body.get("contexts");
		if (!(contextsObj instanceof Map<?, ?> contexts) || contexts.isEmpty()) {
			return "No beans available for " + applicationName + ".";
		}

		boolean filtered = filter != null && !filter.isBlank();
		String needle = filtered ? filter.toLowerCase(Locale.ROOT) : null;

		StringBuilder sb = new StringBuilder("Beans for ").append(applicationName);
		if (filtered) {
			sb.append(" (filtered by \"").append(filter).append("\")");
		}
		sb.append(":\n");

		int totalMatches = 0;

		for (Map.Entry<?, ?> contextEntry : contexts.entrySet()) {
			String contextId = String.valueOf(contextEntry.getKey());
			if (!(contextEntry.getValue() instanceof Map<?, ?> contextDetails)) {
				continue;
			}
			Object beansObj = contextDetails.get("beans");
			if (!(beansObj instanceof Map<?, ?> beans)) {
				continue;
			}

			StringBuilder contextSb = new StringBuilder();
			int contextMatches = 0;

			for (Map.Entry<?, ?> beanEntry : beans.entrySet()) {
				String beanName = String.valueOf(beanEntry.getKey());
				if (!(beanEntry.getValue() instanceof Map<?, ?> beanDetails)) {
					continue;
				}
				Object typeObj = beanDetails.get("type");
				String type = (typeObj != null) ? String.valueOf(typeObj) : "";

				if (filtered && !beanName.toLowerCase(Locale.ROOT).contains(needle)
						&& !type.toLowerCase(Locale.ROOT).contains(needle)) {
					continue;
				}
				contextMatches++;

				contextSb.append("  ").append(beanName).append("\n");
				if (!type.isEmpty()) {
					contextSb.append("    type: ").append(type).append("\n");
				}
				Object scope = beanDetails.get("scope");
				if (scope != null && !"singleton".equals(scope)) {
					contextSb.append("    scope: ").append(scope).append("\n");
				}
				Object dependencies = beanDetails.get("dependencies");
				if (dependencies instanceof List<?> deps && !deps.isEmpty()) {
					contextSb.append("    dependencies: ").append(deps).append("\n");
				}
			}

			if (contextMatches > 0) {
				sb.append("\n[context: ").append(contextId).append("] (").append(contextMatches).append(" beans):\n");
				sb.append(contextSb);
				totalMatches += contextMatches;
			}
		}

		if (filtered && totalMatches == 0) {
			return "No beans matching '" + filter + "' for " + applicationName + ".";
		}
		if (totalMatches == 0) {
			return "No beans available for " + applicationName + ".";
		}
		return sb.toString().trim();
	}

}
