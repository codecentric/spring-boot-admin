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

package de.codecentric.boot.admin.server.mcp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Spring Boot Admin MCP server integration.
 *
 * <p>
 * MCP support is disabled by default. To enable it, set
 * {@code spring.boot.admin.mcp.enabled=true} in your application configuration.
 * </p>
 *
 * <p>
 * Example configuration:
 * </p>
 *
 * <pre>
 * spring:
 *   boot:
 *     admin:
 *       mcp:
 *         enabled: true
 *   ai:
 *     mcp:
 *       server:
 *         type: ASYNC
 *         protocol: STREAMABLE
 * </pre>
 */
@lombok.Data
@ConfigurationProperties(prefix = "spring.boot.admin.mcp")
public class McpProperties {

	/**
	 * Creates a new {@code McpProperties} instance with default values.
	 */
	public McpProperties() {
		// NOOP
	}

	/**
	 * Whether the MCP server integration is enabled. Defaults to {@code false} to ensure
	 * zero impact on existing Spring Boot Admin deployments.
	 */
	private boolean enabled = false;

	/**
	 * Server-side toggles controlling which MCP tool categories are registered and
	 * advertised to clients. These operate on the Spring Boot Admin server itself and are
	 * independent of the monitored applications' {@code management.endpoint.*} settings
	 * (which are enforced at runtime by the target application). All categories default
	 * to {@code true}.
	 */
	private Tools tools = new Tools();

	/**
	 * Category-level enablement flags for the exposed MCP tools. Disabling a category
	 * prevents the corresponding tool bean from being created, so its tools are never
	 * advertised. Changes take effect on server restart.
	 */
	@lombok.Data
	public static class Tools {

		/**
		 * Whether the {@code list-applications} tool is available.
		 */
		private boolean applications = true;

		/**
		 * Whether the {@code get-health} and {@code get-status} tools are available.
		 */
		private boolean health = true;

		/**
		 * Whether the {@code list-metrics} and {@code get-metrics} tools are available.
		 */
		private boolean metrics = true;

		/**
		 * Whether the {@code get-env} and {@code list-env} tools are available.
		 */
		private boolean env = true;

		/**
		 * Whether the {@code get-logs} tool is available.
		 */
		private boolean logs = true;

		/**
		 * Whether the write operation tools ({@code restart-application} and
		 * {@code refresh-configuration}) are available.
		 */
		private boolean operations = true;

		/**
		 * Whether the {@code list-loggers}, {@code get-logger}, and
		 * {@code set-logger-level} tools are available.
		 */
		private boolean loggers = true;

		/**
		 * Whether the {@code get-thread-dump} tool is available.
		 */
		private boolean threadDump = true;

		/**
		 * Whether the {@code get-http-exchanges} tool is available.
		 */
		private boolean httpExchanges = true;

		/**
		 * Whether the {@code get-scheduled-tasks} tool is available.
		 */
		private boolean scheduledTasks = true;

		/**
		 * Whether the {@code list-caches} tool is available.
		 */
		private boolean caches = true;

		/**
		 * Whether the {@code list-beans} tool is available.
		 */
		private boolean beans = true;

	}

}
