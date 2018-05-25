/*
 * Copyright 2014-2018 the original author or authors.
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

package de.codecentric.boot.admin.server.services;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegistrationUpdatedEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EndpointDetectionTriggerTest {
    private final Instance instance = Instance.create(InstanceId.of("id-1"))
                                              .register(Registration.create("foo", "http://health-1").build());
    private TestPublisher<InstanceEvent> events = TestPublisher.create();
    private EndpointDetector detector = mock(EndpointDetector.class);
    private EndpointDetectionTrigger trigger;

    @Before
    public void setUp() throws Exception {
        when(detector.detectEndpoints(any(InstanceId.class))).thenReturn(Mono.empty());
        trigger = new EndpointDetectionTrigger(detector, events.flux());
        trigger.start();
        Thread.sleep(50L); //wait for subscription
    }

    @Test
    public void should_detect_on_status_changed() {
        //when status-change event is emitted
        events.next(new InstanceStatusChangedEvent(instance.getId(), instance.getVersion(), StatusInfo.ofDown()));
        //then should update
        verify(detector, times(1)).detectEndpoints(instance.getId());
    }

    @Test
    public void should_detect_on_registration_updated() {
        //when status-change event is emitted
        events.next(
            new InstanceRegistrationUpdatedEvent(instance.getId(), instance.getVersion(), instance.getRegistration()));
        //then should update
        verify(detector, times(1)).detectEndpoints(instance.getId());
    }

    @Test
    public void should_not_detect_on_non_relevant_event() {
        //when some non-status-change event is emitted
        events.next(new InstanceRegisteredEvent(instance.getId(), instance.getVersion(), instance.getRegistration()));
        //then should not update
        verify(detector, never()).detectEndpoints(instance.getId());
    }

    @Test
    public void should_not_detect_on_trigger_stopped() {
        //when registered event is emitted but the trigger has been stopped
        trigger.stop();
        clearInvocations(detector);
        events.next(new InstanceRegisteredEvent(instance.getId(), instance.getVersion(), instance.getRegistration()));
        //then should not update
        verify(detector, never()).detectEndpoints(instance.getId());
    }
}
