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
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Condition that determines whether an individual MCP tool category bean should be
 * created, respecting the read-only switch, the global
 * {@code spring.boot.admin.mcp.tools.enabled} default and the per-category override.
 *
 * <p>
 * Resolution order:
 * </p>
 * <ol>
 * <li>If the bean is marked as {@link OnMcpToolEnabled#write() write} and read-only mode
 * is active, the bean is never created, regardless of any per-category flag. Read-only
 * mode is active when {@code spring.boot.admin.mcp.read-only=true} or when the deprecated
 * {@code spring.boot.admin.mcp.tools.operations=false} is set.</li>
 * <li>The bean's category and all {@link OnMcpToolEnabled#requires() required} categories
 * must be enabled. For each category, if the per-category property (e.g.
 * {@code spring.boot.admin.mcp.tools.health}) is <em>explicitly</em> set, that value
 * wins.</li>
 * <li>For the category of a write bean, an explicitly set deprecated
 * {@code spring.boot.admin.mcp.tools.operations} is used next.</li>
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

	static final String READ_ONLY_KEY = "spring.boot.admin.mcp.read-only";

	static final String GLOBAL_KEY = "spring.boot.admin.mcp.tools.enabled";

	static final String TOOL_PREFIX = "spring.boot.admin.mcp.tools.";

	/**
	 * Deprecated alias: {@code false} is equivalent to {@code read-only=true}.
	 */
	static final String OPERATIONS_KEY = TOOL_PREFIX + "operations";

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		var attrs = metadata.getAnnotationAttributes(OnMcpToolEnabled.class.getName());
		if (attrs == null) {
			return true;
		}
		var env = context.getEnvironment();
		boolean write = Boolean.TRUE.equals(attrs.get("write"));

		// Read-only mode is fail-closed: it overrides every per-category flag.
		if (write && isReadOnly(env)) {
			return false;
		}

		if (!isCategoryEnabled(env, (String) attrs.get("value"), write)) {
			return false;
		}
		for (String required : (String[]) attrs.get("requires")) {
			if (!isCategoryEnabled(env, required, false)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns whether read-only mode is active, either via
	 * {@code spring.boot.admin.mcp.read-only=true} or the deprecated
	 * {@code spring.boot.admin.mcp.tools.operations=false}. If either requests read-only
	 * mode, it is active.
	 * @param env the environment
	 * @return {@code true} if no write tool may be registered
	 */
	static boolean isReadOnly(Environment env) {
		return env.getProperty(READ_ONLY_KEY, Boolean.class, false)
				|| !env.getProperty(OPERATIONS_KEY, Boolean.class, true);
	}

	private static boolean isCategoryEnabled(Environment env, String category, boolean write) {
		// If the per-tool property is explicitly present, it takes precedence.
		String toolValue = env.getProperty(TOOL_PREFIX + category);
		if (toolValue != null) {
			return Boolean.parseBoolean(toolValue);
		}

		// Write categories fall back to the deprecated operations flag, if set.
		if (write) {
			String operationsValue = env.getProperty(OPERATIONS_KEY);
			if (operationsValue != null) {
				return Boolean.parseBoolean(operationsValue);
			}
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

		/**
		 * Additional categories that must also be enabled for the bean to be created.
		 * @return the required category keys
		 */
		String[] requires() default {};

		/**
		 * Whether the tools provided by the annotated bean change the state of monitored
		 * applications. Write beans are never created in read-only mode.
		 * @return {@code true} if the bean provides state-changing tools
		 */
		boolean write() default false;

	}

}
