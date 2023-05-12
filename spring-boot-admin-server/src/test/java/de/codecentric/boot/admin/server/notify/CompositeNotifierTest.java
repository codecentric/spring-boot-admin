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

package de.codecentric.boot.admin.server.notify;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CompositeNotifierTest {

	private static final InstanceEvent APP_DOWN = new InstanceStatusChangedEvent(InstanceId.of("-"), 0L,
			StatusInfo.ofDown());

	@Test
	public void should_throw_for_invariants() {
		assertThatThrownBy(() -> new CompositeNotifier(null)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void should_trigger_all_notifiers() {
		TestNotifier notifier1 = new TestNotifier();
		TestNotifier notifier2 = new TestNotifier();
		CompositeNotifier compositeNotifier = new CompositeNotifier(Arrays.asList(notifier1, notifier2));

		StepVerifier.create(compositeNotifier.notify(APP_DOWN)).verifyComplete();

		assertThat(notifier1.getEvents()).containsOnly(APP_DOWN);
		assertThat(notifier2.getEvents()).containsOnly(APP_DOWN);
	}

	@Test
	public void should_continue_on_exception() {
		Notifier notifier1 = (ev) -> Mono.error(new IllegalStateException("Test"));
		TestNotifier notifier2 = new TestNotifier();
		CompositeNotifier compositeNotifier = new CompositeNotifier(Arrays.asList(notifier1, notifier2));

		StepVerifier.create(compositeNotifier.notify(APP_DOWN)).verifyComplete();

		assertThat(notifier2.getEvents()).containsOnly(APP_DOWN);
	}

}
