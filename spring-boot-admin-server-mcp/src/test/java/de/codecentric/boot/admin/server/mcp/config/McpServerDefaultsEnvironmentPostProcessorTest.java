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

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class McpServerDefaultsEnvironmentPostProcessorTest {

	private final McpServerDefaultsEnvironmentPostProcessor postProcessor = new McpServerDefaultsEnvironmentPostProcessor();

	@Test
	void shouldContributeDefaultServerName() {
		MockEnvironment environment = new MockEnvironment();

		this.postProcessor.postProcessEnvironment(environment, null);

		assertThat(environment.getProperty(McpServerDefaultsEnvironmentPostProcessor.NAME_PROPERTY))
			.isEqualTo("Spring Boot Admin MCP Server");
	}

	@Test
	void shouldNotOverrideUserProvidedName() {
		MockEnvironment environment = new MockEnvironment();
		environment.getPropertySources()
			.addFirst(new MapPropertySource("userConfig", Collections
				.singletonMap(McpServerDefaultsEnvironmentPostProcessor.NAME_PROPERTY, "My Custom Server")));

		this.postProcessor.postProcessEnvironment(environment, null);

		assertThat(environment.getProperty(McpServerDefaultsEnvironmentPostProcessor.NAME_PROPERTY))
			.isEqualTo("My Custom Server");
	}

	@Test
	void shouldNotOverrideUserProvidedVersion() {
		MockEnvironment environment = new MockEnvironment();
		environment.getPropertySources()
			.addFirst(new MapPropertySource("userConfig",
					Collections.singletonMap(McpServerDefaultsEnvironmentPostProcessor.VERSION_PROPERTY, "9.9.9")));

		this.postProcessor.postProcessEnvironment(environment, null);

		assertThat(environment.getProperty(McpServerDefaultsEnvironmentPostProcessor.VERSION_PROPERTY))
			.isEqualTo("9.9.9");
	}

	@Test
	void defaultsPropertySourceShouldHaveLowestPrecedence() {
		MockEnvironment environment = new MockEnvironment();
		environment.getPropertySources().addLast(new MapPropertySource("dummy", Collections.emptyMap()));

		this.postProcessor.postProcessEnvironment(environment, null);

		// The contributed source must always be added last (fallback).
		assertThat(environment.getPropertySources().stream().reduce((first, second) -> second).orElseThrow().getName())
			.isEqualTo(McpServerDefaultsEnvironmentPostProcessor.PROPERTY_SOURCE_NAME);
	}

}
