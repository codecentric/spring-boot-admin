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

package de.codecentric.boot.admin.server.web.client.cookies;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.publisher.TestPublisher;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CookieStoreCleanupTriggerTest {

	private static final Instance INSTANCE = Instance.create(InstanceId.of("i"));

	private final TestPublisher<InstanceEvent> events = TestPublisher.create();

	private PerInstanceCookieStore cookieStore;

	private CookieStoreCleanupTrigger trigger;

	@BeforeEach
	void setUp() throws Exception {
		cookieStore = mock(PerInstanceCookieStore.class);
		trigger = new CookieStoreCleanupTrigger(this.events.flux(), cookieStore);
		trigger.start();
		await().until(this.events::wasSubscribed);
	}

	@Test
	void deregisterevent_should_trigger_cleanup_cookiestore() {
		this.events.next(new InstanceDeregisteredEvent(INSTANCE.getId(), 42L));

		verify(cookieStore).cleanupInstance(INSTANCE.getId());
	}

}
