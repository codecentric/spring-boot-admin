/*
 * Copyright 2014-2017 the original author or authors.
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
package de.codecentric.boot.admin.server.registry;

import de.codecentric.boot.admin.server.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.server.model.Application;
import de.codecentric.boot.admin.server.model.ApplicationId;
import de.codecentric.boot.admin.server.model.Registration;
import de.codecentric.boot.admin.server.model.StatusInfo;
import de.codecentric.boot.admin.server.registry.store.SimpleApplicationStore;
import de.codecentric.boot.admin.server.web.client.ApplicationOperations;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StatusUpdaterTest {
    private ApplicationOperations applicationOps;
    private StatusUpdater updater;
    private SimpleApplicationStore store;
    private ApplicationEventPublisher publisher;
    private final Application application = Application.create(ApplicationId.of("id"),
            Registration.create("foo", "http://health").build()).build();

    @Before
    public void setup() {
        store = new SimpleApplicationStore();
        applicationOps = mock(ApplicationOperations.class);
        updater = new StatusUpdater(store, applicationOps);
        publisher = mock(ApplicationEventPublisher.class);
        updater.setApplicationEventPublisher(publisher);
    }

    @Test
    public void test_update_statusChanged() {
        when(applicationOps.getHealth(isA(Application.class))).thenReturn(
                ResponseEntity.ok().body(singletonMap("status", "UP")));

        updater.updateStatus(application);

        Application app = store.find(ApplicationId.of("id"));

        assertThat(app.getStatusInfo().getStatus()).isEqualTo("UP");
        verify(publisher).publishEvent(isA(ClientApplicationStatusChangedEvent.class));
    }

    @Test
    public void test_update_statusUnchanged() {
        when(applicationOps.getHealth(any(Application.class))).thenReturn(
                ResponseEntity.ok(singletonMap("status", "UNKNOWN")));

        updater.updateStatus(application);

        verify(publisher, never()).publishEvent(isA(ClientApplicationStatusChangedEvent.class));
        verify(applicationOps, never()).getInfo(isA(Application.class));
    }

    @Test
    public void test_update_up_noBody() {
        when(applicationOps.getHealth(any(Application.class))).thenReturn(ResponseEntity.ok().build());

        updater.updateStatus(application);

        assertThat(store.find(ApplicationId.of("id")).getStatusInfo().getStatus()).isEqualTo("UP");
    }

    @Test
    public void test_update_down() {
        when(applicationOps.getHealth(any(Application.class))).thenReturn(
                ResponseEntity.status(503).body(singletonMap("foo", "bar")));

        updater.updateStatus(application);

        StatusInfo statusInfo = store.find(ApplicationId.of("id")).getStatusInfo();
        assertThat(statusInfo.getStatus()).isEqualTo("DOWN");
        assertThat(statusInfo.getDetails()).containsEntry("foo", "bar");
    }

    @Test
    public void test_update_down_noBody() {
        when(applicationOps.getHealth(any(Application.class))).thenReturn(ResponseEntity.status(503).body(null));

        updater.updateStatus(application);

        StatusInfo statusInfo = store.find(ApplicationId.of("id")).getStatusInfo();
        assertThat(statusInfo.getStatus()).isEqualTo("DOWN");
        assertThat(statusInfo.getDetails()).containsEntry("status", 503);
        assertThat(statusInfo.getDetails()).containsEntry("error", "Service Unavailable");
    }

    @Test
    public void test_update_offline() {
        when(applicationOps.getHealth(any(Application.class))).thenThrow(new ResourceAccessException("error"));

        updater.updateStatus(application);

        StatusInfo statusInfo = store.find(ApplicationId.of("id")).getStatusInfo();
        assertThat(statusInfo.getStatus()).isEqualTo("OFFLINE");
        assertThat(statusInfo.getDetails()).containsEntry("message", "error");
        assertThat(statusInfo.getDetails()).containsEntry("exception",
                "org.springframework.web.client.ResourceAccessException");
    }

    @Test
    public void test_updateStatusForApplications() throws InterruptedException {
        updater.setStatusLifetime(100L);
        Application app1 = Application.create(ApplicationId.of("id-1"),
                Registration.create("foo", "http://health-1").build()).build();
        store.save(app1);
        Application app2 = Application.create(ApplicationId.of("id-2"),
                Registration.create("foo", "http://health-2").build()).build();
        store.save(app2);

        when(applicationOps.getHealth(eq(app1))).thenReturn(ResponseEntity.ok().build());
        when(applicationOps.getHealth(eq(app2))).thenReturn(ResponseEntity.ok().build());

        Thread.sleep(120L); //let both statuses expire
        updater.updateStatus(app2); //and refresh it for app2
        reset(applicationOps);
        when(applicationOps.getHealth(eq(app1))).thenReturn(ResponseEntity.ok().build());

        updater.updateStatusForAllApplications();

        verify(applicationOps, times(1)).getHealth(eq(app1));
        verify(applicationOps, never()).getHealth(eq(app2));
    }

}
