/*
 * Copyright 2014-2023 the original author or authors.
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

package de.codecentric.boot.admin.server.config;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * This condition checks if the sever should be enabled. Property
 * spring.boot.admin.server.enabled is checked.
 */
public class SpringBootAdminServerEnabledCondition extends SpringBootCondition {

	@Override
	public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata annotatedTypeMetadata) {
		AdminServerProperties serverProperties = getClientProperties(context);

		if (!serverProperties.getServer().isEnabled()) {
			return ConditionOutcome
				.noMatch("Spring Boot Server is disabled, because 'spring.boot.admin.server.enabled' is false.");
		}

		return ConditionOutcome.match();
	}

	private AdminServerProperties getClientProperties(ConditionContext context) {
		AdminServerProperties serverProperties = new AdminServerProperties();
		Binder.get(context.getEnvironment()).bind("spring.boot.admin", Bindable.ofInstance(serverProperties));
		return serverProperties;
	}

}
