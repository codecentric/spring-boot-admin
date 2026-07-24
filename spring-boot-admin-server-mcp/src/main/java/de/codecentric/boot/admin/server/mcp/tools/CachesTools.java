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

import java.util.Map;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import reactor.core.publisher.Mono;

/**
 * MCP tools for inspecting caches of registered Spring Boot applications.
 *
 * <ul>
 * <li>{@code list-caches} — lists all caches registered in the application's
 * {@code CacheManager} via the {@code /actuator/caches} endpoint</li>
 * </ul>
 */
public class CachesTools {

	private final ActuatorClient actuatorClient;

	/**
	 * Creates a new {@code CachesTools} instance.
	 * @param actuatorClient the shared actuator call helper
	 */
	public CachesTools(ActuatorClient actuatorClient) {
		this.actuatorClient = actuatorClient;
	}

	/**
	 * Lists all caches for the named application by calling its {@code /actuator/caches}
	 * endpoint. Returns cache names grouped by their {@code CacheManager}, which is
	 * useful for identifying stale or oversized caches.
	 * @param applicationName the registered application name (case-sensitive)
	 * @return plain-text listing of caches grouped by cache manager, or an error message
	 */
	@McpTool(name = "list-caches",
			description = "List all caches registered in the CacheManager(s) of a registered Spring Boot application "
					+ "via its /actuator/caches endpoint. Returns cache names grouped by their CacheManager. "
					+ "Useful for identifying stale or oversized caches. "
					+ "Requires the caches actuator endpoint to be exposed.")
	public Mono<String> listCaches(@McpToolParam(description = "The registered application name (case-sensitive)",
			required = true) String applicationName) {
		return this.actuatorClient.query(applicationName, "/caches", this::formatCaches);
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
