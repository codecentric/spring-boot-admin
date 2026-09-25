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

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;

/**
 * A sample {@link HealthIndicator} that simulates checking an internal message broker. In
 * a real application this would verify queue depth, connection status, etc.
 */
public class MessageBrokerHealthIndicator implements HealthIndicator {

	@Override
	public Health health() {
		// Simulate a healthy message broker
		return Health.unknown().withDetail("broker", "in-memory-stub").withDetail("queueDepth", 0).build();
	}

}
