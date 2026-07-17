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

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

/**
 * Contributes sensible defaults for the Spring AI MCP server metadata reported to
 * clients, namely {@code spring.ai.mcp.server.name} and
 * {@code spring.ai.mcp.server.version}.
 *
 * <p>
 * The defaults are added as a low-precedence {@link MapPropertySource} placed at the end
 * of the property source list. As a result, any explicit configuration in a user's
 * {@code application.yml}, {@code application.properties}, environment variables, or
 * command-line arguments takes precedence and overrides these defaults.
 * </p>
 *
 * <p>
 * The server name defaults to {@value #DEFAULT_SERVER_NAME}. The version is read from the
 * JAR manifest via {@link Package#getImplementationVersion()}, so it tracks the running
 * Spring Boot Admin version automatically. When the classes are not packaged in a JAR
 * (for example during tests or when running from an IDE) the manifest attribute is
 * absent; in that case no version default is contributed and Spring AI's own fallback
 * applies.
 * </p>
 */
public class McpServerDefaultsEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

	/**
	 * The property that reports the MCP server name to clients.
	 */
	static final String NAME_PROPERTY = "spring.ai.mcp.server.name";

	/**
	 * The property that reports the MCP server version to clients.
	 */
	static final String VERSION_PROPERTY = "spring.ai.mcp.server.version";

	/**
	 * Default server name advertised to MCP clients.
	 */
	static final String DEFAULT_SERVER_NAME = "Spring Boot Admin MCP Server";

	/**
	 * Name of the property source contributing the defaults.
	 */
	static final String PROPERTY_SOURCE_NAME = "springBootAdminMcpServerDefaults";

	/**
	 * Creates a new {@code McpServerDefaultsEnvironmentPostProcessor}.
	 */
	public McpServerDefaultsEnvironmentPostProcessor() {
		// NOOP
	}

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		Map<String, Object> defaults = new HashMap<>();
		defaults.put(NAME_PROPERTY, DEFAULT_SERVER_NAME);
		String version = resolveVersion();
		if (StringUtils.hasText(version)) {
			defaults.put(VERSION_PROPERTY, version);
		}
		// addLast -> lowest precedence, so user configuration always wins.
		environment.getPropertySources().addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, defaults));
	}

	/**
	 * Resolves the Spring Boot Admin implementation version from the JAR manifest.
	 * @return the implementation version, or {@code null} when unavailable (e.g. not
	 * running from a packaged JAR)
	 */
	private String resolveVersion() {
		Package pkg = McpServerDefaultsEnvironmentPostProcessor.class.getPackage();
		return (pkg != null) ? pkg.getImplementationVersion() : null;
	}

	@Override
	public int getOrder() {
		// Run late so the defaults are only ever contributed as a fallback.
		return Ordered.LOWEST_PRECEDENCE;
	}

}
