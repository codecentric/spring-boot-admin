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
	 * Creates the {@link ApplicationTools} bean for listing registered applications.
	 * @param instanceRepository the repository used to look up registered instances
	 * @return the configured {@link ApplicationTools}
	 */
	@Bean
	@ConditionalOnProperty(prefix = "spring.boot.admin.mcp.tools", name = "applications", havingValue = "true",
			matchIfMissing = true)
	public ApplicationTools applicationTools(InstanceRepository instanceRepository) {
		return new ApplicationTools(instanceRepository);
	}

	/**
	 * Creates the {@link HealthTools} bean for querying application health and status.
	 * @param instanceRepository the repository used to look up registered instances
	 * @param instanceWebClientBuilder builder for the web client used to call actuator
	 * endpoints
	 * @return the configured {@link HealthTools}
	 */
	@Bean
	@ConditionalOnProperty(prefix = "spring.boot.admin.mcp.tools", name = "health", havingValue = "true",
			matchIfMissing = true)
	public HealthTools healthTools(InstanceRepository instanceRepository,
			InstanceWebClient.Builder instanceWebClientBuilder) {
		return new HealthTools(instanceRepository, instanceWebClientBuilder.build());
	}

	/**
	 * Creates the {@link MetricsTools} bean for querying application metrics.
	 * @param instanceRepository the repository used to look up registered instances
	 * @param instanceWebClientBuilder builder for the web client used to call actuator
	 * endpoints
	 * @return the configured {@link MetricsTools}
	 */
	@Bean
	@ConditionalOnProperty(prefix = "spring.boot.admin.mcp.tools", name = "metrics", havingValue = "true",
			matchIfMissing = true)
	public MetricsTools metricsTools(InstanceRepository instanceRepository,
			InstanceWebClient.Builder instanceWebClientBuilder) {
		return new MetricsTools(instanceRepository, instanceWebClientBuilder.build());
	}

	/**
	 * Creates the {@link EnvTools} bean for resolving application environment properties.
	 * @param instanceRepository the repository used to look up registered instances
	 * @param instanceWebClientBuilder builder for the web client used to call actuator
	 * endpoints
	 * @return the configured {@link EnvTools}
	 */
	@Bean
	@ConditionalOnProperty(prefix = "spring.boot.admin.mcp.tools", name = "env", havingValue = "true",
			matchIfMissing = true)
	public EnvTools envTools(InstanceRepository instanceRepository,
			InstanceWebClient.Builder instanceWebClientBuilder) {
		return new EnvTools(instanceRepository, instanceWebClientBuilder.build());
	}

	/**
	 * Creates the {@link LogsTools} bean for accessing application log output.
	 * @param instanceRepository the repository used to look up registered instances
	 * @param instanceWebClientBuilder builder for the web client used to call actuator
	 * endpoints
	 * @return the configured {@link LogsTools}
	 */
	@Bean
	@ConditionalOnProperty(prefix = "spring.boot.admin.mcp.tools", name = "logs", havingValue = "true",
			matchIfMissing = true)
	public LogsTools logsTools(InstanceRepository instanceRepository,
			InstanceWebClient.Builder instanceWebClientBuilder) {
		return new LogsTools(instanceRepository, instanceWebClientBuilder.build());
	}

	/**
	 * Creates the {@link OperationsTools} bean for performing write operations on
	 * applications.
	 * @param instanceRepository the repository used to look up registered instances
	 * @param instanceWebClientBuilder builder for the web client used to call actuator
	 * endpoints
	 * @return the configured {@link OperationsTools}
	 */
	@Bean
	@ConditionalOnProperty(prefix = "spring.boot.admin.mcp.tools", name = "operations", havingValue = "true",
			matchIfMissing = true)
	public OperationsTools operationsTools(InstanceRepository instanceRepository,
			InstanceWebClient.Builder instanceWebClientBuilder) {
		return new OperationsTools(instanceRepository, instanceWebClientBuilder.build());
	}

	/**
	 * Creates the {@link LoggersTools} bean for querying and configuring log levels.
	 * @param instanceRepository the repository used to look up registered instances
	 * @param instanceWebClientBuilder builder for the web client used to call actuator
	 * endpoints
	 * @return the configured {@link LoggersTools}
	 */
	@Bean
	@ConditionalOnProperty(prefix = "spring.boot.admin.mcp.tools", name = "loggers", havingValue = "true",
			matchIfMissing = true)
	public LoggersTools loggersTools(InstanceRepository instanceRepository,
			InstanceWebClient.Builder instanceWebClientBuilder) {
		return new LoggersTools(instanceRepository, instanceWebClientBuilder.build());
	}

	/**
	 * Creates the {@link ThreadDumpTools} bean for capturing thread dumps.
	 * @param instanceRepository the repository used to look up registered instances
	 * @param instanceWebClientBuilder builder for the web client used to call actuator
	 * endpoints
	 * @return the configured {@link ThreadDumpTools}
	 */
	@Bean
	@ConditionalOnProperty(prefix = "spring.boot.admin.mcp.tools", name = "thread-dump", havingValue = "true",
			matchIfMissing = true)
	public ThreadDumpTools threadDumpTools(InstanceRepository instanceRepository,
			InstanceWebClient.Builder instanceWebClientBuilder) {
		return new ThreadDumpTools(instanceRepository, instanceWebClientBuilder.build());
	}

	/**
	 * Creates the {@link HttpExchangesTools} bean for inspecting recent HTTP exchanges.
	 * @param instanceRepository the repository used to look up registered instances
	 * @param instanceWebClientBuilder builder for the web client used to call actuator
	 * endpoints
	 * @return the configured {@link HttpExchangesTools}
	 */
	@Bean
	@ConditionalOnProperty(prefix = "spring.boot.admin.mcp.tools", name = "http-exchanges", havingValue = "true",
			matchIfMissing = true)
	public HttpExchangesTools httpExchangesTools(InstanceRepository instanceRepository,
			InstanceWebClient.Builder instanceWebClientBuilder) {
		return new HttpExchangesTools(instanceRepository, instanceWebClientBuilder.build());
	}

	/**
	 * Creates the {@link ScheduledTasksTools} bean for inspecting scheduled tasks.
	 * @param instanceRepository the repository used to look up registered instances
	 * @param instanceWebClientBuilder builder for the web client used to call actuator
	 * endpoints
	 * @return the configured {@link ScheduledTasksTools}
	 */
	@Bean
	@ConditionalOnProperty(prefix = "spring.boot.admin.mcp.tools", name = "scheduled-tasks", havingValue = "true",
			matchIfMissing = true)
	public ScheduledTasksTools scheduledTasksTools(InstanceRepository instanceRepository,
			InstanceWebClient.Builder instanceWebClientBuilder) {
		return new ScheduledTasksTools(instanceRepository, instanceWebClientBuilder.build());
	}

	/**
	 * Creates the {@link CachesTools} bean for inspecting application caches.
	 * @param instanceRepository the repository used to look up registered instances
	 * @param instanceWebClientBuilder builder for the web client used to call actuator
	 * endpoints
	 * @return the configured {@link CachesTools}
	 */
	@Bean
	@ConditionalOnProperty(prefix = "spring.boot.admin.mcp.tools", name = "caches", havingValue = "true",
			matchIfMissing = true)
	public CachesTools cachesTools(InstanceRepository instanceRepository,
			InstanceWebClient.Builder instanceWebClientBuilder) {
		return new CachesTools(instanceRepository, instanceWebClientBuilder.build());
	}

	/**
	 * Creates the {@link BeansTools} bean for inspecting Spring application context
	 * beans.
	 * @param instanceRepository the repository used to look up registered instances
	 * @param instanceWebClientBuilder builder for the web client used to call actuator
	 * endpoints
	 * @return the configured {@link BeansTools}
	 */
	@Bean
	@ConditionalOnProperty(prefix = "spring.boot.admin.mcp.tools", name = "beans", havingValue = "true",
			matchIfMissing = true)
	public BeansTools beansTools(InstanceRepository instanceRepository,
			InstanceWebClient.Builder instanceWebClientBuilder) {
		return new BeansTools(instanceRepository, instanceWebClientBuilder.build());
	}

}
