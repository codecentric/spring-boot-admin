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

package de.codecentric.boot.admin.client.config;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * This condition checks if the client should be enabled. Two properties are checked:
 * spring.boot.admin.client.enabled and spring.boot.admin.client.url. The following table
 * shows under which conditions the client is active. <pre>
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
	public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata annotatedTypeMetadata) {
		ClientProperties clientProperties = getClientProperties(context);

		if (!clientProperties.isEnabled()) {
			return ConditionOutcome
				.noMatch("Spring Boot Client is disabled, because 'spring.boot.admin.client.enabled' is false.");
		}

		if (clientProperties.getUrl().length == 0) {
			return ConditionOutcome
				.noMatch("Spring Boot Client is disabled, because 'spring.boot.admin.client.url' is empty.");
		}

		return ConditionOutcome.match();
	}

	private ClientProperties getClientProperties(ConditionContext context) {
		ClientProperties clientProperties = new ClientProperties();
		Binder.get(context.getEnvironment()).bind("spring.boot.admin.client", Bindable.ofInstance(clientProperties));
		return clientProperties;
	}

}
