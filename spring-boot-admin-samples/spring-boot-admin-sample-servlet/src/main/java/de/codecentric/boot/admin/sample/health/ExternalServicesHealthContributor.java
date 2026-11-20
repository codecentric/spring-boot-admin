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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.boot.health.contributor.CompositeHealthContributor;
import org.springframework.boot.health.contributor.HealthContributor;
import org.springframework.boot.health.contributor.HealthContributors;

/**
 * A {@link CompositeHealthContributor} that groups related external-dependency health
 * checks under a single {@code /actuator/health/externalServices} entry.
 *
 * <p>
 * Spring Boot aggregates the individual {@link HealthContributor}s and rolls up the worst
 * status to the composite level, so a single degraded dependency is immediately visible
 * both in the top-level health summary and in the drill-down view.
 *
 * <p>
 * The composite is registered as a bean named {@code externalServices} in
 * {@link HealthContributorConfig}, which causes Spring Boot Actuator to expose it at
 * {@code /actuator/health/externalServices}.
 */
// tag::composite-health-contributor[]
public class ExternalServicesHealthContributor implements CompositeHealthContributor {

	private final Map<String, HealthContributor> contributors = new LinkedHashMap<>();

	public ExternalServicesHealthContributor(ExternalApiHealthIndicator externalApi,
			MessageBrokerHealthIndicator messageBroker) {
		contributors.put("externalApi", externalApi);
		contributors.put("messageBroker", messageBroker);
	}

	@Override
	public HealthContributor getContributor(String name) {
		return contributors.get(name);
	}

	@Override
	public Stream<HealthContributors.Entry> stream() {
		return contributors.entrySet()
			.stream()
			.map((entry) -> new HealthContributors.Entry(entry.getKey(), entry.getValue()));
	}

}
// end::composite-health-contributor[]
