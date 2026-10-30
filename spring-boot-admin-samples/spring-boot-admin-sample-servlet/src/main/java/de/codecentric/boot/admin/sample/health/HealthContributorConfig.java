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

package de.codecentric.boot.admin.sample.health;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the individual health indicators and the composite contributor as Spring
 * beans.
 *
 * <p>
 * The bean name {@code externalServices} is used by Spring Boot Actuator as the path
 * segment, so the composite becomes available at
 * {@code /actuator/health/externalServices}.
 */
@Configuration(proxyBeanMethods = false)
public class HealthContributorConfig {

	@Bean
	public ExternalApiHealthIndicator externalApiHealthIndicator() {
		return new ExternalApiHealthIndicator();
	}

	@Bean
	public MessageBrokerHealthIndicator messageBrokerHealthIndicator() {
		return new MessageBrokerHealthIndicator();
	}

	// tag::composite-health-contributor-bean[]
	@Bean
	public ExternalServicesHealthContributor externalServices(ExternalApiHealthIndicator externalApi,
			MessageBrokerHealthIndicator messageBroker) {
		return new ExternalServicesHealthContributor(externalApi, messageBroker);
	}
	// end::composite-health-contributor-bean[]

}
