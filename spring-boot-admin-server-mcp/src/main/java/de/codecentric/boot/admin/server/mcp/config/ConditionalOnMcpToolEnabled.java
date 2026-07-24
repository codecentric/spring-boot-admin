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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Condition that determines whether an individual MCP tool category bean should be
 * created, respecting both the global {@code spring.boot.admin.mcp.tools.enabled} default
 * and the per-category override.
 *
 * <p>
 * Resolution order:
 * </p>
 * <ol>
 * <li>If the per-category property (e.g. {@code spring.boot.admin.mcp.tools.health}) is
 * <em>explicitly</em> set, that value wins.</li>
 * <li>Otherwise, the global {@code spring.boot.admin.mcp.tools.enabled} default is
 * used.</li>
 * </ol>
 *
 * <p>
 * This allows disabling all tools at once while selectively re-enabling individual
 * categories:
 * </p>
 *
 * <pre>
 * spring.boot.admin.mcp.tools.enabled=false
 * spring.boot.admin.mcp.tools.health=true   # only health tools are registered
 * </pre>
 */
public class ConditionalOnMcpToolEnabled implements Condition {

	static final String GLOBAL_KEY = "spring.boot.admin.mcp.tools.enabled";

	static final String TOOL_PREFIX = "spring.boot.admin.mcp.tools.";

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		var attrs = metadata.getAnnotationAttributes(OnMcpToolEnabled.class.getName());
		if (attrs == null) {
			return true;
		}
		String toolKey = TOOL_PREFIX + attrs.get("value");
		var env = context.getEnvironment();

		// If the per-tool property is explicitly present, it takes precedence.
		String toolValue = env.getProperty(toolKey);
		if (toolValue != null) {
			return Boolean.parseBoolean(toolValue);
		}

		// Fall back to the global tools.enabled flag (defaults to true).
		return env.getProperty(GLOBAL_KEY, Boolean.class, true);
	}

	/**
	 * Meta-annotation used on each tool bean method in {@link McpAutoConfiguration}.
	 *
	 * <p>
	 * {@code value} must be the kebab-case property segment after
	 * {@code spring.boot.admin.mcp.tools.} (e.g. {@code "health"},
	 * {@code "thread-dump"}).
	 * </p>
	 */
	@Target({ ElementType.METHOD, ElementType.TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@Conditional(ConditionalOnMcpToolEnabled.class)
	public @interface OnMcpToolEnabled {

		/**
		 * The kebab-case tool-category key, e.g. {@code "health"} or
		 * {@code "thread-dump"}.
		 * @return the tool category key
		 */
		String value();

	}

}
