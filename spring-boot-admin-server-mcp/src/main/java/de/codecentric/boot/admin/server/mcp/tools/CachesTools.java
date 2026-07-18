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
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;

/**
 * MCP tools for inspecting caches of registered Spring Boot applications.
 *
 * <ul>
 * <li>{@code list-caches} — lists all caches registered in the application's
 * {@code CacheManager} via the {@code /actuator/caches} endpoint</li>
 * </ul>
 */
public class CachesTools {

	private static final Logger log = LoggerFactory.getLogger(CachesTools.class);

	private static final Duration TIMEOUT = Duration.ofMillis(450);

	private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE = new ParameterizedTypeReference<>() {
	};

	private final InstanceRepository instanceRepository;

	private final InstanceWebClient instanceWebClient;

	/**
	 * Creates a new {@code CachesTools} instance.
	 * @param instanceRepository the repository used to look up registered instances
	 * @param instanceWebClient the client used to call actuator endpoints on instances
	 */
	public CachesTools(InstanceRepository instanceRepository, InstanceWebClient instanceWebClient) {
		this.instanceRepository = instanceRepository;
		this.instanceWebClient = instanceWebClient;
	}

	/**
	 * Lists all caches for the named application by calling its {@code /actuator/caches}
	 * endpoint. Returns cache names grouped by their {@code CacheManager}, which is
	 * useful for identifying stale or oversized caches.
	 * @param applicationName the registered application name (case-insensitive)
	 * @return plain-text listing of caches grouped by cache manager, or an error message
	 */
	@McpTool(name = "list-caches",
			description = "List all caches registered in the CacheManager(s) of a registered Spring Boot application "
					+ "via its /actuator/caches endpoint. Returns cache names grouped by their CacheManager. "
					+ "Useful for identifying stale or oversized caches. "
					+ "Requires the caches actuator endpoint to be exposed.")
	public Mono<String> listCaches(@McpToolParam(description = "The registered application name (case-insensitive)",
			required = true) String applicationName) {
		return this.instanceRepository.findByName(applicationName).next().flatMap((instance) -> {
			String url = instance.getRegistration().getManagementUrl() + "/caches";
			return this.instanceWebClient.instance(instance)
				.get()
				.uri(url)
				.retrieve()
				.bodyToMono(RESPONSE_TYPE)
				.timeout(TIMEOUT)
				.map((body) -> formatCaches(applicationName, body))
				.doOnError((ex) -> log.warn("Failed to list caches for {}", applicationName, ex))
				.onErrorResume(
						(ex) -> Mono.just("Error listing caches for " + applicationName + ": " + ex.getMessage()));
		}).switchIfEmpty(Mono.just("Application '" + applicationName + "' not found in registry."));
	}

	@SuppressWarnings("unchecked")
	private String formatCaches(String applicationName, Map<String, Object> body) {
		Object cacheManagersObj = body.get("cacheManagers");
		if (!(cacheManagersObj instanceof Map<?, ?> cacheManagers) || cacheManagers.isEmpty()) {
			return "No caches available for " + applicationName + ".";
		}

		int totalCaches = 0;
		StringBuilder sb = new StringBuilder("Caches for ").append(applicationName).append(":\n");

		for (Map.Entry<?, ?> managerEntry : cacheManagers.entrySet()) {
			String managerName = String.valueOf(managerEntry.getKey());
			sb.append("\n[").append(managerName).append("]:\n");

			if (managerEntry.getValue() instanceof Map<?, ?> managerDetails) {
				Object cachesObj = managerDetails.get("caches");
				if (cachesObj instanceof Map<?, ?> caches) {
					for (Map.Entry<?, ?> cacheEntry : caches.entrySet()) {
						totalCaches++;
						String cacheName = String.valueOf(cacheEntry.getKey());
						sb.append("  - ").append(cacheName);
						if (cacheEntry.getValue() instanceof Map<?, ?> cacheDetails) {
							Object target = cacheDetails.get("target");
							if (target != null) {
								sb.append(" (").append(target).append(")");
							}
						}
						sb.append("\n");
					}
				}
			}
		}

		if (totalCaches == 0) {
			return "No caches available for " + applicationName + ".";
		}
		return sb.toString().trim();
	}

}
