/*
 * Copyright 2014-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.server.config;

import java.time.Duration;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.services.InstanceRegistry;

/**
 * the default implementation for InstanceEventFilter: It follows the instance filter
 * logic of {@link InstanceRegistry}, which filter instance by
 * {@link AdminServerInstanceFilter}
 *
 * @author Kyrie-yx-Irving
 */
public class DefaultAdminServerInstanceEventFilter implements AdminServerInstanceEventFilter {

	private static final Duration DEFAULT_DURATION = Duration.ofMillis(100);

	private final InstanceRegistry instanceRegistry;

	public DefaultAdminServerInstanceEventFilter(InstanceRegistry instanceRegistry) {
		this.instanceRegistry = instanceRegistry;
	}

	/**
	 * filter the event of instance which obey the filter logic of InstanceEvent
	 * instanceEvent
	 * @param instanceEvent the event of the instance which registered in spring boot
	 * admin server
	 * @return true if the event don't have to filter
	 */
	@Override
	public final boolean filterForEvent(InstanceEvent instanceEvent) {
		Instance instance = instanceRegistry.getInstance(instanceEvent.getInstance()).block(DEFAULT_DURATION);
		return instance != null;
	}

}
