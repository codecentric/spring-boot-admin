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

package de.codecentric.boot.admin.server.mcp;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import de.codecentric.boot.admin.server.config.EnableAdminServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Starts a Spring Boot Admin server with MCP enabled on a random port and queries the
 * advertised tools via a real MCP client ({@code initialize} + {@code tools/list}) over
 * the Streamable HTTP transport at {@code /mcp}.
 */
class McpToolsListIntegrationTest {

	private static final Set<String> WRITE_TOOLS = Set.of("restart-application", "refresh-configuration",
			"set-logger-level");

	private static final Set<String> READ_TOOLS = Set.of("list-applications", "get-health", "get-status",
			"list-metrics", "get-metrics", "get-env", "list-env", "get-logs", "list-loggers", "get-logger",
			"get-thread-dump", "get-http-exchanges", "get-scheduled-tasks", "list-caches", "list-beans");

	static Stream<Arguments> readOnlyConfigurations() {
		return Stream.of(arguments("read-only", List.of("spring.boot.admin.mcp.read-only=true")),
				arguments("deprecated operations=false", List.of("spring.boot.admin.mcp.tools.operations=false")),
				arguments("read-only with all write flags re-enabled",
						List.of("spring.boot.admin.mcp.read-only=true", "spring.boot.admin.mcp.tools.operations=true",
								"spring.boot.admin.mcp.tools.restart=true", "spring.boot.admin.mcp.tools.refresh=true",
								"spring.boot.admin.mcp.tools.logger-levels=true")),
				arguments("operations=false with all write flags re-enabled",
						List.of("spring.boot.admin.mcp.tools.operations=false",
								"spring.boot.admin.mcp.tools.restart=true", "spring.boot.admin.mcp.tools.refresh=true",
								"spring.boot.admin.mcp.tools.logger-levels=true",
								"spring.boot.admin.mcp.read-only=false")));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("readOnlyConfigurations")
	void readOnlyConfiguration_advertisesNoWriteTools(String name, List<String> properties) {
		Map<String, McpSchema.Tool> tools = listTools(properties);

		assertThat(tools.keySet()).doesNotContainAnyElementsOf(WRITE_TOOLS)
			.containsExactlyInAnyOrderElementsOf(READ_TOOLS);
		assertThat(tools.values())
			.allSatisfy((tool) -> assertThat(tool.annotations().readOnlyHint()).as(tool.name()).isTrue());
	}

	@Test
	void defaultConfiguration_advertisesAllToolsWithAccurateHints() {
		Map<String, McpSchema.Tool> tools = listTools(List.of());

		assertThat(tools.keySet()).containsAll(READ_TOOLS)
			.containsAll(WRITE_TOOLS)
			.hasSize(READ_TOOLS.size() + WRITE_TOOLS.size());
		assertThat(tools.values()).allSatisfy((tool) -> assertThat(tool.annotations().readOnlyHint()).as(tool.name())
			.isEqualTo(!WRITE_TOOLS.contains(tool.name())));
		assertThat(tools.get("restart-application").annotations().destructiveHint()).isTrue();
		assertThat(tools.get("refresh-configuration").annotations().destructiveHint()).isTrue();
	}

	@Test
	void loggerLevelsDisabled_keepsLoggerInspection() {
		Map<String, McpSchema.Tool> tools = listTools(List.of("spring.boot.admin.mcp.tools.logger-levels=false"));

		assertThat(tools.keySet())
			.contains("list-loggers", "get-logger", "restart-application", "refresh-configuration")
			.doesNotContain("set-logger-level");
	}

	private static Map<String, McpSchema.Tool> listTools(List<String> properties) {
		String[] args = Stream
			.concat(Stream.of("--server.port=0", "--spring.boot.admin.mcp.enabled=true",
					"--spring.boot.admin.monitor.status-interval=60s"), properties.stream().map((p) -> "--" + p))
			.toArray(String[]::new);
		try (ConfigurableApplicationContext context = new SpringApplicationBuilder(TestAdminApplication.class)
			.web(WebApplicationType.REACTIVE)
			.run(args)) {
			String port = context.getEnvironment().getProperty("local.server.port");
			var transport = HttpClientStreamableHttpTransport.builder("http://localhost:" + port)
				.endpoint("/mcp")
				.build();
			try (McpSyncClient client = McpClient.sync(transport).requestTimeout(Duration.ofSeconds(10)).build()) {
				client.initialize();
				return client.listTools()
					.tools()
					.stream()
					.collect(Collectors.toMap(McpSchema.Tool::name, (tool) -> tool));
			}
		}
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@EnableAdminServer
	static class TestAdminApplication {

	}

}
