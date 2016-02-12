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
package de.codecentric.boot.admin.config;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * This condition checks if the Client should be enabled. Two properties are checked:
 * spring.boot.admin.client.enabled and spring.boot.admin.url. The following table shows under which
 * conditions the Client should be enabled or disabled. enabled = false enabled = true (default) url
 * empty (default) disabled disabled url set disabled enabled
 */
public class SpringBootAdminClientEnablerCondition extends SpringBootCondition {
	@Override
	public ConditionOutcome getMatchOutcome(ConditionContext context,
			AnnotatedTypeMetadata annotatedTypeMetadata) {
		boolean enabled = context.getEnvironment().getProperty("spring.boot.admin.client.enabled",
				Boolean.class, Boolean.TRUE);
		String url = context.getEnvironment().getProperty("spring.boot.admin.url", "");

		if (!enabled) {
			return ConditionOutcome.noMatch(
					"Spring Boot Client is disabled, because 'spring.boot.admin.client.enabled' is false.");
		} else if (StringUtils.isEmpty(url)) {
			return ConditionOutcome.noMatch(
					"Spring Boot Client is disabled, because 'spring.boot.admin.url' is empty.");
		} else {
			return ConditionOutcome.match();
		}
	}
}
