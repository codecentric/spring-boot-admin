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

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.DeprecatedConfigurationProperty;

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
	 * Timeout for actuator calls made by MCP tools. Applies to all standard actuator
	 * endpoints. Defaults to {@code 450ms}. The thread dump endpoint uses
	 * {@code threadDumpTimeout} instead.
	 */
	private Duration timeout = Duration.ofMillis(450);

	/**
	 * Timeout for {@code /actuator/threaddump} calls, which can be slower than standard
	 * actuator endpoints. Defaults to {@code 10s}.
	 */
	private Duration threadDumpTimeout = Duration.ofSeconds(10);

	/**
	 * Whether the MCP server runs in read-only mode. When {@code true}, no tool that
	 * changes the state of a monitored application ({@code restart-application},
	 * {@code refresh-configuration}, {@code set-logger-level}) is registered, regardless
	 * of the individual {@code tools.*} flags. Defaults to {@code false}. The deprecated
	 * {@code tools.operations=false} has the same effect.
	 */
	private boolean readOnly = false;

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
	 *
	 * <p>
	 * {@code enabled} acts as a global default for all categories. When set to
	 * {@code false}, every category is disabled unless its individual flag is explicitly
	 * set to {@code true}. When {@code true} (the default), each category flag controls
	 * whether that category is registered.
	 * </p>
	 *
	 * <p>
	 * Example — disable all tools except health:
	 * </p>
	 *
	 * <pre>
	 * spring.boot.admin.mcp.tools.enabled=false
	 * spring.boot.admin.mcp.tools.health=true
	 * </pre>
	 */
	@lombok.Data
	public static class Tools {

		/**
		 * Global default for all tool categories. When {@code false}, every category is
		 * disabled unless its individual flag explicitly overrides it to {@code true}.
		 * Defaults to {@code true}.
		 */
		private boolean enabled = true;

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
		 * Whether the write tools are available.
		 * @deprecated since 4.1.6 for removal, use
		 * {@code spring.boot.admin.mcp.read-only} instead. {@code false} is equivalent to
		 * {@code read-only=true}; an explicit {@code true} acts as the default for the
		 * {@code restart}, {@code refresh} and {@code logger-levels} categories.
		 */
		@Deprecated(since = "4.1.6", forRemoval = true)
		private Boolean operations;

		/**
		 * Whether the write tool {@code restart-application} is available.
		 */
		private boolean restart = true;

		/**
		 * Whether the write tool {@code refresh-configuration} is available.
		 */
		private boolean refresh = true;

		/**
		 * Whether the logger tools are available. Disabling it removes
		 * {@code list-loggers}, {@code get-logger} and {@code set-logger-level}.
		 */
		private boolean loggers = true;

		/**
		 * Whether the write tool {@code set-logger-level} is available. Also requires
		 * {@code loggers} to be enabled.
		 */
		private boolean loggerLevels = true;

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

		/**
		 * Returns the deprecated {@code operations} flag.
		 * @return the configured value, or {@code null} when not set
		 * @deprecated since 4.1.6 for removal, use
		 * {@code spring.boot.admin.mcp.read-only} instead
		 */
		@Deprecated(since = "4.1.6", forRemoval = true)
		@DeprecatedConfigurationProperty(replacement = "spring.boot.admin.mcp.read-only",
				reason = "Use read-only=true to disable all write tools, or the restart, refresh and "
						+ "logger-levels flags to control individual write tools.",
				since = "4.1.6")
		public Boolean getOperations() {
			return this.operations;
		}

		/**
		 * Sets the deprecated {@code operations} flag.
		 * @param operations the value to set
		 * @deprecated since 4.1.6 for removal, use
		 * {@code spring.boot.admin.mcp.read-only} instead
		 */
		@Deprecated(since = "4.1.6", forRemoval = true)
		public void setOperations(Boolean operations) {
			this.operations = operations;
		}

	}

}
