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

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class McpAutoConfigurationTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(McpAutoConfiguration.class))
		.withUserConfiguration(RequiredBeansConfiguration.class);

	@Test
	void mcpDisabled_noToolBeansCreated() {
		this.contextRunner.run((context) -> {
			assertThat(context).doesNotHaveBean(ApplicationTools.class);
			assertThat(context).doesNotHaveBean(HealthTools.class);
			assertThat(context).doesNotHaveBean(MetricsTools.class);
			assertThat(context).doesNotHaveBean(EnvTools.class);
			assertThat(context).doesNotHaveBean(LogsTools.class);
			assertThat(context).doesNotHaveBean(OperationsTools.class);
			assertThat(context).doesNotHaveBean(LoggersTools.class);
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
			assertThat(context).hasSingleBean(ApplicationTools.class);
			assertThat(context).hasSingleBean(HealthTools.class);
			assertThat(context).hasSingleBean(MetricsTools.class);
			assertThat(context).hasSingleBean(EnvTools.class);
			assertThat(context).hasSingleBean(LogsTools.class);
			assertThat(context).hasSingleBean(OperationsTools.class);
			assertThat(context).hasSingleBean(LoggersTools.class);
			assertThat(context).hasSingleBean(ThreadDumpTools.class);
			assertThat(context).hasSingleBean(HttpExchangesTools.class);
			assertThat(context).hasSingleBean(ScheduledTasksTools.class);
			assertThat(context).hasSingleBean(CachesTools.class);
			assertThat(context).hasSingleBean(BeansTools.class);
		});
	}

	@Test
	void operationsDisabled_operationsToolBeanAbsentOthersPresent() {
		this.contextRunner
			.withPropertyValues("spring.boot.admin.mcp.enabled=true", "spring.boot.admin.mcp.tools.operations=false")
			.run((context) -> {
				assertThat(context).doesNotHaveBean(OperationsTools.class);
				assertThat(context).hasSingleBean(ApplicationTools.class);
				assertThat(context).hasSingleBean(EnvTools.class);
			});
	}

	@Test
	void envDisabled_envToolBeanAbsentOthersPresent() {
		this.contextRunner
			.withPropertyValues("spring.boot.admin.mcp.enabled=true", "spring.boot.admin.mcp.tools.env=false")
			.run((context) -> {
				assertThat(context).doesNotHaveBean(EnvTools.class);
				assertThat(context).hasSingleBean(OperationsTools.class);
				assertThat(context).hasSingleBean(HealthTools.class);
			});
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
