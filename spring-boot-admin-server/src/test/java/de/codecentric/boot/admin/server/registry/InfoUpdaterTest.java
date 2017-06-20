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

import de.codecentric.boot.admin.server.event.ClientApplicationInfoChangedEvent;
import de.codecentric.boot.admin.server.model.Application;
import de.codecentric.boot.admin.server.model.ApplicationId;
import de.codecentric.boot.admin.server.model.Info;
import de.codecentric.boot.admin.server.model.Registration;
import de.codecentric.boot.admin.server.model.StatusInfo;
import de.codecentric.boot.admin.server.registry.store.SimpleApplicationStore;
import de.codecentric.boot.admin.server.web.client.ApplicationOperations;
import reactor.core.publisher.Mono;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InfoUpdaterTest {
    private ApplicationOperations applicationOps;
    private InfoUpdater updater;
    private SimpleApplicationStore store;
    private ApplicationEventPublisher publisher;

    @Before
    public void setup() {
        store = new SimpleApplicationStore();
        applicationOps = mock(ApplicationOperations.class);
        updater = new InfoUpdater(store, applicationOps);
        publisher = mock(ApplicationEventPublisher.class);
        updater.setApplicationEventPublisher(publisher);
    }

    @Test
    public void should_update_info_for_online_only() {
        Application application = Application.create(ApplicationId.of("onl"),
                Registration.create("foo", "http://health").build()).statusInfo(StatusInfo.ofUp()).build();
        store.save(application);

        Application offline = Application.copyOf(application)
                                         .id(ApplicationId.of("off"))
                                         .statusInfo(StatusInfo.ofOffline())
                                         .build();
        store.save(offline);
        Application unknown = Application.copyOf(application)
                                         .id(ApplicationId.of("unk"))
                                         .statusInfo(StatusInfo.ofUnknown())
                                         .build();
        store.save(unknown);
        when(applicationOps.getInfo(any(Application.class))).thenReturn(
                Mono.just(ResponseEntity.ok(singletonMap("foo", "bar"))));

        updater.updateInfo(offline.getId());
        verify(publisher, never()).publishEvent(isA(ClientApplicationInfoChangedEvent.class));

        updater.updateInfo(unknown.getId());
        verify(publisher, never()).publishEvent(isA(ClientApplicationInfoChangedEvent.class));

        updater.updateInfo(application.getId());
        assertThat(store.find(application.getId()).getInfo()).isEqualTo(Info.from(singletonMap("foo", "bar")));
        verify(publisher, times(1)).publishEvent(isA(ClientApplicationInfoChangedEvent.class));
    }

    @Test
    public void should_clear_info_on_http_error() {
        Application application = Application.create(ApplicationId.of("onl"),
                Registration.create("foo", "http://health").build())
                                             .statusInfo(StatusInfo.ofUp())
                                             .info(Info.from(singletonMap("foo", "bar")))
                                             .build();
        store.save(application);

        when(applicationOps.getInfo(any(Application.class))).thenReturn(Mono.just(ResponseEntity.status(500).build()));

        updater.updateInfo(application.getId());
        assertThat(store.find(application.getId()).getInfo()).isEqualTo(Info.empty());
        verify(publisher, times(1)).publishEvent(isA(ClientApplicationInfoChangedEvent.class));
    }

    @Test
    public void should_clear_info_on_exception() {
        Application application = Application.create(ApplicationId.of("onl"),
                Registration.create("foo", "http://health").build())
                                             .statusInfo(StatusInfo.ofUp())
                                             .info(Info.from(singletonMap("foo", "bar")))
                                             .build();
        store.save(application);

        when(applicationOps.getInfo(any(Application.class))).thenReturn(
                Mono.error(new ResourceAccessException("error")));

        updater.updateInfo(application.getId());
        assertThat(store.find(application.getId()).getInfo()).isEqualTo(Info.empty());
        verify(publisher, times(1)).publishEvent(isA(ClientApplicationInfoChangedEvent.class));
    }
}