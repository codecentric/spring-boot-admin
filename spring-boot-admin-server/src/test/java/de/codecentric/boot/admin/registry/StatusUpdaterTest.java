/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.codecentric.boot.admin.registry;

import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.model.StatusInfo;
import de.codecentric.boot.admin.registry.store.SimpleApplicationStore;
import de.codecentric.boot.admin.web.client.ApplicationOperations;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StatusUpdaterTest {

    private ApplicationOperations applicationOps;
    private StatusUpdater updater;
    private SimpleApplicationStore store;
    private ApplicationEventPublisher publisher;

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
                ResponseEntity.ok().body(Collections.<String, Serializable>singletonMap("status", "UP")));
        when(applicationOps.getInfo(isA(Application.class))).thenReturn(
                ResponseEntity.ok().body(Collections.<String, Serializable>singletonMap("foo", "bar")));

        updater.updateStatus(Application.create("foo").withId("id").withHealthUrl("health").build());

        Application app = store.find("id");

        assertThat(app.getStatusInfo().getStatus()).isEqualTo("UP");
        assertThat(app.getInfo().getValues()).containsEntry("foo", "bar");
        verify(publisher).publishEvent(isA(ClientApplicationStatusChangedEvent.class));
    }

    @Test
    public void test_update_statusUnchanged() {
        when(applicationOps.getHealth(any(Application.class))).thenReturn(
                ResponseEntity.ok(Collections.<String, Serializable>singletonMap("status", "UNKNOWN")));

        updater.updateStatus(Application.create("foo").withId("id").withHealthUrl("health").build());

        verify(publisher, never()).publishEvent(isA(ClientApplicationStatusChangedEvent.class));
        verify(applicationOps, never()).getInfo(isA(Application.class));
    }

    @Test
    public void test_update_up_noBody() {
        when(applicationOps.getHealth(any(Application.class))).thenReturn(
                ResponseEntity.ok((Map<String, Serializable>) null));

        updater.updateStatus(Application.create("foo").withId("id").withHealthUrl("health").build());

        assertThat(store.find("id").getStatusInfo().getStatus()).isEqualTo("UP");
    }

    @Test
    public void test_update_down() {
        when(applicationOps.getHealth(any(Application.class))).thenReturn(
                ResponseEntity.status(503).body(Collections.<String, Serializable>singletonMap("foo", "bar")));

        updater.updateStatus(Application.create("foo").withId("id").withHealthUrl("health").build());

        StatusInfo statusInfo = store.find("id").getStatusInfo();
        assertThat(statusInfo.getStatus()).isEqualTo("DOWN");
        assertThat(statusInfo.getDetails()).containsEntry("foo", "bar");
    }

    @Test
    public void test_update_down_noBody() {
        when(applicationOps.getHealth(any(Application.class))).thenReturn(
                ResponseEntity.status(503).body((Map<String, Serializable>) null));

        updater.updateStatus(Application.create("foo").withId("id").withHealthUrl("health").build());

        StatusInfo statusInfo = store.find("id").getStatusInfo();
        assertThat(statusInfo.getStatus()).isEqualTo("DOWN");
        assertThat(statusInfo.getDetails()).containsEntry("status", 503);
        assertThat(statusInfo.getDetails()).containsEntry("error", "Service Unavailable");
    }

    @Test
    public void test_update_offline() {
        when(applicationOps.getHealth(any(Application.class))).thenThrow(new ResourceAccessException("error"));

        Application app = Application.create("foo")
                                     .withId("id")
                                     .withHealthUrl("health")
                                     .withStatusInfo(StatusInfo.ofUp())
                                     .build();
        updater.updateStatus(app);

        StatusInfo statusInfo = store.find("id").getStatusInfo();
        assertThat(statusInfo.getStatus()).isEqualTo("OFFLINE");
        assertThat(statusInfo.getDetails()).containsEntry("message", "error");
        assertThat(statusInfo.getDetails()).containsEntry("exception",
                "org.springframework.web.client.ResourceAccessException");
    }

    @Test
    public void test_updateStatusForApplications() throws InterruptedException {
        updater.setStatusLifetime(100L);
        Application app1 = Application.create("foo").withId("id-1").withHealthUrl("health-1").build();
        store.save(app1);
        Thread.sleep(120L); // Let the StatusInfo of id-1 expire
        Application app2 = Application.create("foo").withId("id-2").withHealthUrl("health-2").build();
        store.save(app2);

        when(applicationOps.getHealth(eq(app1))).thenReturn(ResponseEntity.ok((Map<String, Serializable>) null));

        updater.updateStatusForAllApplications();

        assertThat(store.find("id-1").getStatusInfo().getStatus()).isEqualTo("UP");
        verify(applicationOps, never()).getHealth(eq(app2));
    }

}
