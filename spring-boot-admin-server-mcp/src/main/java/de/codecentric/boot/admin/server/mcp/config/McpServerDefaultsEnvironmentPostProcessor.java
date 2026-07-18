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
 *
 * <p>
 * The MCP server API type defaults to {@value #DEFAULT_SERVER_TYPE}. Spring Boot Admin
 * runs on a reactive WebFlux/Netty stack and its actuator calls are non-blocking, so the
 * asynchronous MCP server is the natural fit. Spring AI's own default is {@code sync},
 * which would introduce blocking bridges on the reactive event loop. Contributing
 * {@code async} here means users only need to enable the MCP server via
 * {@code spring.boot.admin.mcp.enabled=true} without also setting any {@code spring.ai.*}
 * properties.
 * </p>
 *
 * <p>
 * The transport protocol defaults to {@value #DEFAULT_SERVER_PROTOCOL}. Although Spring
 * AI's {@code McpServerProperties} Java default is {@code STREAMABLE}, the transport
 * autoconfiguration selects the active transport via {@code @ConditionalOnProperty}
 * evaluated against the {@code Environment} — before the properties object is bound.
 * {@code McpServerSseWebFluxAutoConfiguration} carries {@code matchIfMissing=true} on its
 * SSE condition, so when {@code spring.ai.mcp.server.protocol} is absent from the
 * environment the legacy SSE transport wins and {@code /mcp} returns 404. Contributing
 * {@code streamable} here ensures the Streamable HTTP transport is active out of the box,
 * exposing the single {@code /mcp} endpoint that modern MCP clients expect.
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
	 * The property that selects the MCP server API type ({@code sync} or {@code async}).
	 */
	static final String TYPE_PROPERTY = "spring.ai.mcp.server.type";

	/**
	 * The property that selects the MCP transport protocol ({@code streamable},
	 * {@code stateless}, or the legacy {@code sse}).
	 */
	static final String PROTOCOL_PROPERTY = "spring.ai.mcp.server.protocol";

	/**
	 * Default server name advertised to MCP clients.
	 */
	static final String DEFAULT_SERVER_NAME = "Spring Boot Admin MCP Server";

	/**
	 * Default MCP server API type. Spring Boot Admin runs on a reactive WebFlux/Netty
	 * stack, so the asynchronous server is the natural fit.
	 */
	static final String DEFAULT_SERVER_TYPE = "async";

	/**
	 * Default MCP transport protocol. Must be set explicitly in the environment because
	 * Spring AI's SSE transport condition carries {@code matchIfMissing=true} and wins
	 * when the property is absent, even though {@code McpServerProperties} defaults to
	 * {@code STREAMABLE}.
	 */
	static final String DEFAULT_SERVER_PROTOCOL = "streamable";

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
		defaults.put(TYPE_PROPERTY, DEFAULT_SERVER_TYPE);
		defaults.put(PROTOCOL_PROPERTY, DEFAULT_SERVER_PROTOCOL);
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
