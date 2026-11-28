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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.mcp.tools.ActuatorClient;
import de.codecentric.boot.admin.server.mcp.tools.ApplicationTools;
import de.codecentric.boot.admin.server.mcp.tools.BeansTools;
import de.codecentric.boot.admin.server.mcp.tools.CachesTools;
import de.codecentric.boot.admin.server.mcp.tools.EnvTools;
import de.codecentric.boot.admin.server.mcp.tools.HealthTools;
import de.codecentric.boot.admin.server.mcp.tools.HttpExchangesTools;
import de.codecentric.boot.admin.server.mcp.tools.LoggerLevelTools;
import de.codecentric.boot.admin.server.mcp.tools.LoggersTools;
import de.codecentric.boot.admin.server.mcp.tools.LogsTools;
import de.codecentric.boot.admin.server.mcp.tools.MetricsTools;
import de.codecentric.boot.admin.server.mcp.tools.RefreshTools;
import de.codecentric.boot.admin.server.mcp.tools.RestartTools;
import de.codecentric.boot.admin.server.mcp.tools.ScheduledTasksTools;
import de.codecentric.boot.admin.server.mcp.tools.ThreadDumpTools;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class McpAutoConfigurationTest {

	private static final Set<String> WRITE_TOOLS = Set.of("restart-application", "refresh-configuration",
			"set-logger-level");

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(McpAutoConfiguration.class))
		.withUserConfiguration(RequiredBeansConfiguration.class);

	@Test
	void mcpDisabled_noToolBeansCreated() {
		this.contextRunner.run((context) -> {
			assertThat(context).doesNotHaveBean(ActuatorClient.class);
			assertThat(context).doesNotHaveBean(ApplicationTools.class);
			assertThat(context).doesNotHaveBean(HealthTools.class);
			assertThat(context).doesNotHaveBean(MetricsTools.class);
			assertThat(context).doesNotHaveBean(EnvTools.class);
			assertThat(context).doesNotHaveBean(LogsTools.class);
			assertThat(context).doesNotHaveBean(RestartTools.class);
			assertThat(context).doesNotHaveBean(RefreshTools.class);
			assertThat(context).doesNotHaveBean(LoggersTools.class);
			assertThat(context).doesNotHaveBean(LoggerLevelTools.class);
			assertThat(context).doesNotHaveBean(ThreadDumpTools.class);
			assertThat(context).doesNotHaveBean(HttpExchangesTools.class);
			assertThat(context).doesNotHaveBean(ScheduledTasksTools.class);
			assertThat(context).doesNotHaveBean(CachesTools.class);
			assertThat(context).doesNotHaveBean(BeansTools.class);
		});
	}

	@Test
	void mcpEnabled_allToolBeansCreatedByDefault() {
		this.contextRunner.withPropertyValues("spring.boot.admin.mcp.enabled=true").run((context) -> {
			assertThat(context).hasSingleBean(ActuatorClient.class);
			assertThat(context).hasSingleBean(ApplicationTools.class);
			assertThat(context).hasSingleBean(HealthTools.class);
			assertThat(context).hasSingleBean(MetricsTools.class);
			assertThat(context).hasSingleBean(EnvTools.class);
			assertThat(context).hasSingleBean(LogsTools.class);
			assertThat(context).hasSingleBean(RestartTools.class);
			assertThat(context).hasSingleBean(RefreshTools.class);
			assertThat(context).hasSingleBean(LoggersTools.class);
			assertThat(context).hasSingleBean(LoggerLevelTools.class);
			assertThat(context).hasSingleBean(ThreadDumpTools.class);
			assertThat(context).hasSingleBean(HttpExchangesTools.class);
			assertThat(context).hasSingleBean(ScheduledTasksTools.class);
			assertThat(context).hasSingleBean(CachesTools.class);
			assertThat(context).hasSingleBean(BeansTools.class);
		});
	}

	@Test
	void operationsDisabled_allWriteToolBeansAbsentOthersPresent() {
		this.contextRunner
			.withPropertyValues("spring.boot.admin.mcp.enabled=true", "spring.boot.admin.mcp.tools.operations=false")
			.run((context) -> {
				assertThat(context).doesNotHaveBean(RestartTools.class);
				assertThat(context).doesNotHaveBean(RefreshTools.class);
				assertThat(context).doesNotHaveBean(LoggerLevelTools.class);
				assertThat(context).hasSingleBean(LoggersTools.class);
				assertThat(context).hasSingleBean(ApplicationTools.class);
				assertThat(context).hasSingleBean(EnvTools.class);
				assertThat(advertisedTools(context)).doesNotContainAnyElementsOf(WRITE_TOOLS)
					.contains("list-loggers", "get-logger");
			});
	}

	@Test
	void operationsDisabled_cannotBeOverriddenByOtherFlags() {
		this.contextRunner
			.withPropertyValues("spring.boot.admin.mcp.enabled=true", "spring.boot.admin.mcp.tools.operations=false",
					"spring.boot.admin.mcp.tools.enabled=true", "spring.boot.admin.mcp.tools.loggers=true",
					"spring.boot.admin.mcp.tools.logger-levels=true", "spring.boot.admin.mcp.read-only=false")
			.run((context) -> {
				assertThat(context).doesNotHaveBean(RestartTools.class);
				assertThat(context).doesNotHaveBean(RefreshTools.class);
				assertThat(context).doesNotHaveBean(LoggerLevelTools.class);
				assertThat(mcpTools(context)).isNotEmpty()
					.allSatisfy((tool) -> assertThat(tool.annotations().readOnlyHint()).as(tool.name()).isTrue());
			});
	}

	@Test
	void readOnly_noWriteToolsAdvertised() {
		this.contextRunner
			.withPropertyValues("spring.boot.admin.mcp.enabled=true", "spring.boot.admin.mcp.read-only=true")
			.run((context) -> {
				assertThat(context).doesNotHaveBean(RestartTools.class);
				assertThat(context).doesNotHaveBean(RefreshTools.class);
				assertThat(context).doesNotHaveBean(LoggerLevelTools.class);
				assertThat(context).hasSingleBean(LoggersTools.class);
				assertThat(advertisedTools(context)).doesNotContainAnyElementsOf(WRITE_TOOLS)
					.contains("list-loggers", "get-logger");
			});
	}

	@Test
	void readOnly_overridesExplicitWriteToolFlags() {
		this.contextRunner
			.withPropertyValues("spring.boot.admin.mcp.enabled=true", "spring.boot.admin.mcp.read-only=true",
					"spring.boot.admin.mcp.tools.operations=true", "spring.boot.admin.mcp.tools.loggers=true",
					"spring.boot.admin.mcp.tools.logger-levels=true")
			.run((context) -> {
				assertThat(context).doesNotHaveBean(RestartTools.class);
				assertThat(context).doesNotHaveBean(RefreshTools.class);
				assertThat(context).doesNotHaveBean(LoggerLevelTools.class);
				assertThat(advertisedTools(context)).doesNotContainAnyElementsOf(WRITE_TOOLS);
			});
	}

	@Test
	void operationsDisabled_cannotBeOverriddenByWriteCategoryFlags() {
		this.contextRunner
			.withPropertyValues("spring.boot.admin.mcp.enabled=true", "spring.boot.admin.mcp.tools.operations=false",
					"spring.boot.admin.mcp.tools.restart=true", "spring.boot.admin.mcp.tools.refresh=true",
					"spring.boot.admin.mcp.tools.logger-levels=true")
			.run((context) -> {
				assertThat(context).doesNotHaveBean(RestartTools.class);
				assertThat(context).doesNotHaveBean(RefreshTools.class);
				assertThat(context).doesNotHaveBean(LoggerLevelTools.class);
				assertThat(advertisedTools(context)).doesNotContainAnyElementsOf(WRITE_TOOLS);
			});
	}

	@Test
	void restartDisabled_onlyRestartToolAbsent() {
		this.contextRunner
			.withPropertyValues("spring.boot.admin.mcp.enabled=true", "spring.boot.admin.mcp.tools.restart=false")
			.run((context) -> {
				assertThat(context).doesNotHaveBean(RestartTools.class);
				assertThat(context).hasSingleBean(RefreshTools.class);
				assertThat(context).hasSingleBean(LoggerLevelTools.class);
			});
	}

	@Test
	void refreshDisabled_onlyRefreshToolAbsent() {
		this.contextRunner
			.withPropertyValues("spring.boot.admin.mcp.enabled=true", "spring.boot.admin.mcp.tools.refresh=false")
			.run((context) -> {
				assertThat(context).doesNotHaveBean(RefreshTools.class);
				assertThat(context).hasSingleBean(RestartTools.class);
				assertThat(context).hasSingleBean(LoggerLevelTools.class);
			});
	}

	@Test
	void toolsEnabledFalse_deprecatedOperationsTrueReenablesWriteTools() {
		this.contextRunner
			.withPropertyValues("spring.boot.admin.mcp.enabled=true", "spring.boot.admin.mcp.tools.enabled=false",
					"spring.boot.admin.mcp.tools.operations=true")
			.run((context) -> {
				assertThat(context).hasSingleBean(RestartTools.class);
				assertThat(context).hasSingleBean(RefreshTools.class);
				assertThat(context).doesNotHaveBean(LoggerLevelTools.class);
				assertThat(context).doesNotHaveBean(HealthTools.class);
			});
	}

	@Test
	void toolsEnabledFalse_deprecatedOperationsTrueAndLoggersReenablesLoggerMutation() {
		this.contextRunner
			.withPropertyValues("spring.boot.admin.mcp.enabled=true", "spring.boot.admin.mcp.tools.enabled=false",
					"spring.boot.admin.mcp.tools.operations=true", "spring.boot.admin.mcp.tools.loggers=true")
			.run((context) -> {
				assertThat(context).hasSingleBean(LoggersTools.class);
				assertThat(context).hasSingleBean(LoggerLevelTools.class);
			});
	}

	@Test
	void toolsEnabledFalse_restartReenablesOnlyRestart() {
		this.contextRunner
			.withPropertyValues("spring.boot.admin.mcp.enabled=true", "spring.boot.admin.mcp.tools.enabled=false",
					"spring.boot.admin.mcp.tools.restart=true")
			.run((context) -> {
				assertThat(context).hasSingleBean(RestartTools.class);
				assertThat(context).doesNotHaveBean(RefreshTools.class);
				assertThat(context).doesNotHaveBean(LoggerLevelTools.class);
			});
	}

	@Test
	void deprecatedOperationsTrue_explicitCategoryFlagWins() {
		this.contextRunner
			.withPropertyValues("spring.boot.admin.mcp.enabled=true", "spring.boot.admin.mcp.tools.operations=true",
					"spring.boot.admin.mcp.tools.restart=false")
			.run((context) -> {
				assertThat(context).doesNotHaveBean(RestartTools.class);
				assertThat(context).hasSingleBean(RefreshTools.class);
			});
	}

	@Test
	void loggerLevelsDisabled_loggerInspectionStillAvailable() {
		this.contextRunner
			.withPropertyValues("spring.boot.admin.mcp.enabled=true", "spring.boot.admin.mcp.tools.logger-levels=false")
			.run((context) -> {
				assertThat(context).hasSingleBean(LoggersTools.class);
				assertThat(context).doesNotHaveBean(LoggerLevelTools.class);
				assertThat(context).hasSingleBean(RestartTools.class);
				assertThat(context).hasSingleBean(RefreshTools.class);
			});
	}

	@Test
	void loggersDisabled_allLoggerToolBeansAbsent() {
		this.contextRunner
			.withPropertyValues("spring.boot.admin.mcp.enabled=true", "spring.boot.admin.mcp.tools.loggers=false")
			.run((context) -> {
				assertThat(context).doesNotHaveBean(LoggersTools.class);
				assertThat(context).doesNotHaveBean(LoggerLevelTools.class);
				assertThat(context).hasSingleBean(RestartTools.class);
				assertThat(context).hasSingleBean(RefreshTools.class);
			});
	}

	@Test
	void toolsEnabledFalse_reenablingLoggersDoesNotReenableLoggerMutation() {
		this.contextRunner
			.withPropertyValues("spring.boot.admin.mcp.enabled=true", "spring.boot.admin.mcp.tools.enabled=false",
					"spring.boot.admin.mcp.tools.loggers=true")
			.run((context) -> {
				assertThat(context).hasSingleBean(LoggersTools.class);
				assertThat(context).doesNotHaveBean(LoggerLevelTools.class);
			});
	}

	@Test
	void toolAnnotations_readOnlyHintMatchesWriteClassification() {
		this.contextRunner.withPropertyValues("spring.boot.admin.mcp.enabled=true").run((context) -> {
			var tools = mcpTools(context);
			assertThat(tools).extracting(McpTool::name).containsAll(WRITE_TOOLS);
			assertThat(tools).allSatisfy((tool) -> assertThat(tool.annotations().readOnlyHint()).as(tool.name())
				.isEqualTo(!WRITE_TOOLS.contains(tool.name())));
		});
	}

	@Test
	void envDisabled_envToolBeanAbsentOthersPresent() {
		this.contextRunner
			.withPropertyValues("spring.boot.admin.mcp.enabled=true", "spring.boot.admin.mcp.tools.env=false")
			.run((context) -> {
				assertThat(context).doesNotHaveBean(EnvTools.class);
				assertThat(context).hasSingleBean(RestartTools.class);
				assertThat(context).hasSingleBean(RefreshTools.class);
				assertThat(context).hasSingleBean(HealthTools.class);
			});
	}

	@Test
	void toolsEnabledFalse_allToolBeansAbsent() {
		this.contextRunner
			.withPropertyValues("spring.boot.admin.mcp.enabled=true", "spring.boot.admin.mcp.tools.enabled=false")
			.run((context) -> {
				assertThat(context).doesNotHaveBean(ApplicationTools.class);
				assertThat(context).doesNotHaveBean(HealthTools.class);
				assertThat(context).doesNotHaveBean(MetricsTools.class);
				assertThat(context).doesNotHaveBean(EnvTools.class);
				assertThat(context).doesNotHaveBean(LogsTools.class);
				assertThat(context).doesNotHaveBean(RestartTools.class);
				assertThat(context).doesNotHaveBean(RefreshTools.class);
				assertThat(context).doesNotHaveBean(LoggersTools.class);
				assertThat(context).doesNotHaveBean(LoggerLevelTools.class);
				assertThat(context).doesNotHaveBean(ThreadDumpTools.class);
				assertThat(context).doesNotHaveBean(HttpExchangesTools.class);
				assertThat(context).doesNotHaveBean(ScheduledTasksTools.class);
				assertThat(context).doesNotHaveBean(CachesTools.class);
				assertThat(context).doesNotHaveBean(BeansTools.class);
			});
	}

	@Test
	void toolsEnabledFalse_perToolOverrideReenable() {
		this.contextRunner
			.withPropertyValues("spring.boot.admin.mcp.enabled=true", "spring.boot.admin.mcp.tools.enabled=false",
					"spring.boot.admin.mcp.tools.health=true")
			.run((context) -> {
				assertThat(context).hasSingleBean(HealthTools.class);
				assertThat(context).doesNotHaveBean(ApplicationTools.class);
				assertThat(context).doesNotHaveBean(MetricsTools.class);
				assertThat(context).doesNotHaveBean(EnvTools.class);
				assertThat(context).doesNotHaveBean(LogsTools.class);
				assertThat(context).doesNotHaveBean(RestartTools.class);
				assertThat(context).doesNotHaveBean(RefreshTools.class);
				assertThat(context).doesNotHaveBean(LoggersTools.class);
				assertThat(context).doesNotHaveBean(LoggerLevelTools.class);
				assertThat(context).doesNotHaveBean(ThreadDumpTools.class);
				assertThat(context).doesNotHaveBean(HttpExchangesTools.class);
				assertThat(context).doesNotHaveBean(ScheduledTasksTools.class);
				assertThat(context).doesNotHaveBean(CachesTools.class);
				assertThat(context).doesNotHaveBean(BeansTools.class);
			});
	}

	private static List<McpTool> mcpTools(ApplicationContext context) {
		return Arrays.stream(context.getBeanDefinitionNames())
			.map(context::getBean)
			.flatMap((bean) -> Arrays.stream(bean.getClass().getDeclaredMethods()))
			.map((method) -> method.getAnnotation(McpTool.class))
			.filter(Objects::nonNull)
			.toList();
	}

	private static Set<String> advertisedTools(ApplicationContext context) {
		return mcpTools(context).stream().map(McpTool::name).collect(Collectors.toSet());
	}

	@Configuration(proxyBeanMethods = false)
	static class RequiredBeansConfiguration {

		@Bean
		InstanceRepository instanceRepository() {
			return mock(InstanceRepository.class);
		}

		@Bean
		InstanceWebClient.Builder instanceWebClientBuilder() {
			return InstanceWebClient.builder();
		}

	}

}
