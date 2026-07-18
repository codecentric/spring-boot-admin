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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;

/**
 * MCP tools for querying registered Spring Boot applications.
 *
 * <p>
 * Exposes the {@code list-applications} tool which returns a plain-text summary of all
 * instances currently registered with Spring Boot Admin.
 * </p>
 */
public class ApplicationTools {

	private static final Logger log = LoggerFactory.getLogger(ApplicationTools.class);

	private final InstanceRepository instanceRepository;

	/**
	 * Creates a new {@code ApplicationTools} instance.
	 * @param instanceRepository the repository used to look up registered instances
	 */
	public ApplicationTools(InstanceRepository instanceRepository) {
		this.instanceRepository = instanceRepository;
	}

	/**
	 * Lists all Spring Boot applications currently registered with Spring Boot Admin,
	 * including their name, instance ID, status, and management URL.
	 * @return a plain-text list of registered applications, or a message indicating none
	 * are registered
	 */
	@McpTool(name = "list-applications",
			description = "List all Spring Boot applications currently registered with Spring Boot Admin. "
					+ "Returns name, status (UP/DOWN/UNKNOWN), instance ID and management URL for each instance.")
	public Mono<String> listApplications() {
		return this.instanceRepository.findAll().collectList().map((instances) -> {
			if (instances.isEmpty()) {
				return "No applications are currently registered.";
			}
			StringBuilder sb = new StringBuilder("Registered applications (").append(instances.size()).append("):\n");
			for (Instance instance : instances) {
				String name = instance.getRegistration().getName();
				String status = instance.getStatusInfo().getStatus();
				String id = instance.getId().getValue();
				String managementUrl = (instance.getRegistration().getManagementUrl() != null)
						? instance.getRegistration().getManagementUrl() : "N/A";
				sb.append("- ")
					.append(name)
					.append(" | id: ")
					.append(id)
					.append(" | status: ")
					.append(status)
					.append(" | management: ")
					.append(managementUrl)
					.append("\n");
			}
			return sb.toString().trim();
		})
			.doOnError((ex) -> log.warn("Failed to list applications", ex))
			.onErrorReturn("Error retrieving registered applications.");
	}

}
