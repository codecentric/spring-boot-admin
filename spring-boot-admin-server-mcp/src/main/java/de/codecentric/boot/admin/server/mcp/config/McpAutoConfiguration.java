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

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.mcp.config.ConditionalOnMcpToolEnabled.OnMcpToolEnabled;
import de.codecentric.boot.admin.server.mcp.tools.ActuatorClient;
import de.codecentric.boot.admin.server.mcp.tools.ApplicationTools;
import de.codecentric.boot.admin.server.mcp.tools.BeansTools;
import de.codecentric.boot.admin.server.mcp.tools.CachesTools;
import de.codecentric.boot.admin.server.mcp.tools.EnvTools;
import de.codecentric.boot.admin.server.mcp.tools.HealthTools;
import de.codecentric.boot.admin.server.mcp.tools.HttpExchangesTools;
import de.codecentric.boot.admin.server.mcp.tools.LoggersTools;
import de.codecentric.boot.admin.server.mcp.tools.LogsTools;
import de.codecentric.boot.admin.server.mcp.tools.MetricsTools;
import de.codecentric.boot.admin.server.mcp.tools.OperationsTools;
import de.codecentric.boot.admin.server.mcp.tools.ScheduledTasksTools;
import de.codecentric.boot.admin.server.mcp.tools.ThreadDumpTools;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;

/**
 * Auto-configuration for the Spring Boot Admin MCP server integration.
 *
 * <p>
 * Activates only when {@code spring.boot.admin.mcp.enabled=true}. When inactive, no MCP
 * beans are created and the application context is unaffected.
 * </p>
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "spring.boot.admin.mcp", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(McpProperties.class)
public class McpAutoConfiguration {

	/**
	 * Creates the shared {@link ActuatorClient} bean used by all tool classes to call
	 * actuator endpoints on registered applications.
	 * @param instanceRepository the repository used to look up registered instances
	 * @param instanceWebClientBuilder builder for the web client used to call actuator
	 * endpoints
	 * @param mcpProperties the MCP configuration properties
	 * @return the configured {@link ActuatorClient}
	 */
	@Bean
	public ActuatorClient actuatorClient(InstanceRepository instanceRepository,
			InstanceWebClient.Builder instanceWebClientBuilder, McpProperties mcpProperties) {
		return new ActuatorClient(instanceRepository, instanceWebClientBuilder.build(), mcpProperties.getTimeout());
	}

	/**
	 * Creates the {@link ApplicationTools} bean for listing registered applications.
	 * @param instanceRepository the repository used to look up registered instances
	 * @return the configured {@link ApplicationTools}
	 */
	@Bean
	@OnMcpToolEnabled("applications")
	public ApplicationTools applicationTools(InstanceRepository instanceRepository) {
		return new ApplicationTools(instanceRepository);
	}

	/**
	 * Creates the {@link HealthTools} bean for querying application health and status.
	 * @param actuatorClient the shared actuator call helper
	 * @return the configured {@link HealthTools}
	 */
	@Bean
	@OnMcpToolEnabled("health")
	public HealthTools healthTools(ActuatorClient actuatorClient) {
		return new HealthTools(actuatorClient);
	}

	/**
	 * Creates the {@link MetricsTools} bean for querying application metrics.
	 * @param actuatorClient the shared actuator call helper
	 * @return the configured {@link MetricsTools}
	 */
	@Bean
	@OnMcpToolEnabled("metrics")
	public MetricsTools metricsTools(ActuatorClient actuatorClient) {
		return new MetricsTools(actuatorClient);
	}

	/**
	 * Creates the {@link EnvTools} bean for resolving application environment properties.
	 * @param actuatorClient the shared actuator call helper
	 * @return the configured {@link EnvTools}
	 */
	@Bean
	@OnMcpToolEnabled("env")
	public EnvTools envTools(ActuatorClient actuatorClient) {
		return new EnvTools(actuatorClient);
	}

	/**
	 * Creates the {@link LogsTools} bean for accessing application log output.
	 * @param actuatorClient the shared actuator call helper
	 * @return the configured {@link LogsTools}
	 */
	@Bean
	@OnMcpToolEnabled("logs")
	public LogsTools logsTools(ActuatorClient actuatorClient) {
		return new LogsTools(actuatorClient);
	}

	/**
	 * Creates the {@link OperationsTools} bean for performing write operations on
	 * applications.
	 * @param actuatorClient the shared actuator call helper
	 * @return the configured {@link OperationsTools}
	 */
	@Bean
	@OnMcpToolEnabled("operations")
	public OperationsTools operationsTools(ActuatorClient actuatorClient) {
		return new OperationsTools(actuatorClient);
	}

	/**
	 * Creates the {@link LoggersTools} bean for querying and configuring log levels.
	 * @param actuatorClient the shared actuator call helper
	 * @return the configured {@link LoggersTools}
	 */
	@Bean
	@OnMcpToolEnabled("loggers")
	public LoggersTools loggersTools(ActuatorClient actuatorClient) {
		return new LoggersTools(actuatorClient);
	}

	/**
	 * Creates the {@link ThreadDumpTools} bean for capturing thread dumps.
	 * @param actuatorClient the shared actuator call helper
	 * @param mcpProperties the MCP configuration properties
	 * @return the configured {@link ThreadDumpTools}
	 */
	@Bean
	@OnMcpToolEnabled("thread-dump")
	public ThreadDumpTools threadDumpTools(ActuatorClient actuatorClient, McpProperties mcpProperties) {
		return new ThreadDumpTools(actuatorClient, mcpProperties.getThreadDumpTimeout());
	}

	/**
	 * Creates the {@link HttpExchangesTools} bean for inspecting recent HTTP exchanges.
	 * @param actuatorClient the shared actuator call helper
	 * @return the configured {@link HttpExchangesTools}
	 */
	@Bean
	@OnMcpToolEnabled("http-exchanges")
	public HttpExchangesTools httpExchangesTools(ActuatorClient actuatorClient) {
		return new HttpExchangesTools(actuatorClient);
	}

	/**
	 * Creates the {@link ScheduledTasksTools} bean for inspecting scheduled tasks.
	 * @param actuatorClient the shared actuator call helper
	 * @return the configured {@link ScheduledTasksTools}
	 */
	@Bean
	@OnMcpToolEnabled("scheduled-tasks")
	public ScheduledTasksTools scheduledTasksTools(ActuatorClient actuatorClient) {
		return new ScheduledTasksTools(actuatorClient);
	}

	/**
	 * Creates the {@link CachesTools} bean for inspecting application caches.
	 * @param actuatorClient the shared actuator call helper
	 * @return the configured {@link CachesTools}
	 */
	@Bean
	@OnMcpToolEnabled("caches")
	public CachesTools cachesTools(ActuatorClient actuatorClient) {
		return new CachesTools(actuatorClient);
	}

	/**
	 * Creates the {@link BeansTools} bean for inspecting Spring application context
	 * beans.
	 * @param actuatorClient the shared actuator call helper
	 * @return the configured {@link BeansTools}
	 */
	@Bean
	@OnMcpToolEnabled("beans")
	public BeansTools beansTools(ActuatorClient actuatorClient) {
		return new BeansTools(actuatorClient);
	}

}
