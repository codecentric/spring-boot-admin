/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.codecentric.boot.admin.client.config;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * This condition checks if the client should be enabled. Two properties are checked:
 * spring.boot.admin.client.enabled and spring.boot.admin.url. The following table shows under which
 * conditions the client is active.
 *
 * <pre>
 *           | enabled: false | enabled: true (default) |
 * --------- | -------------- | ----------------------- |
 * url empty | inactive       | inactive                |
 * (default) |                |                         |
 * --------- | -------------- | ----------------------- |
 * url set   | inactive       | active                  |
 * </pre>
 */
public class SpringBootAdminClientEnabledCondition extends SpringBootCondition {
	@Override
	public ConditionOutcome getMatchOutcome(ConditionContext context,
			AnnotatedTypeMetadata annotatedTypeMetadata) {

		if (!isEnabled(context.getEnvironment())) {
			return ConditionOutcome.noMatch(
					"Spring Boot Client is disabled, because 'spring.boot.admin.client.enabled' is false.");
		}

		if (isUrlEmpty(context.getEnvironment())) {
			return ConditionOutcome.noMatch(
					"Spring Boot Client is disabled, because 'spring.boot.admin.url' is empty.");
		}

		return ConditionOutcome.match();
	}

	private boolean isEnabled(Environment env) {
		RelaxedPropertyResolver resolver = new RelaxedPropertyResolver(env,
				"spring.boot.admin.client.");
		return resolver.getProperty("enabled", Boolean.class, Boolean.TRUE);
	}

	private boolean isUrlEmpty(Environment env) {
		RelaxedPropertyResolver resolver = new RelaxedPropertyResolver(env, "spring.boot.admin.");
		return StringUtils.isEmpty(resolver.getProperty("url", ""));
	}
}
