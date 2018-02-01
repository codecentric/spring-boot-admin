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

package de.codecentric.boot.admin.client.registration;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RegistrationApplicationListenerTest {

    @Test
    public void test_register() {
        ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
        TaskScheduler scheduler = mock(TaskScheduler.class);
        RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator, scheduler);

        listener.onApplicationReady(new ApplicationReadyEvent(mock(SpringApplication.class), null,
                mock(ConfigurableWebApplicationContext.class)));

        verify(scheduler).scheduleAtFixedRate(isA(Runnable.class), eq(Duration.ofSeconds(10)));
    }

    @Test
    public void test_no_register() {
        ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
        TaskScheduler scheduler = mock(TaskScheduler.class);
        RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator, scheduler);
        listener.setAutoRegister(false);

        listener.onApplicationReady(new ApplicationReadyEvent(mock(SpringApplication.class), null,
                mock(ConfigurableWebApplicationContext.class)));

        verify(scheduler, never()).scheduleAtFixedRate(isA(Runnable.class), eq(Duration.ofSeconds(10)));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void test_no_register_after_close() {
        ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
        TaskScheduler scheduler = mock(TaskScheduler.class);
        RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator, scheduler);

        ScheduledFuture task = mock(ScheduledFuture.class);
        when(scheduler.scheduleAtFixedRate(isA(Runnable.class), eq(Duration.ofSeconds(10)))).thenReturn(task);

        listener.onApplicationReady(new ApplicationReadyEvent(mock(SpringApplication.class), null,
                mock(ConfigurableWebApplicationContext.class)));

        verify(scheduler).scheduleAtFixedRate(isA(Runnable.class), eq(Duration.ofSeconds(10)));

        listener.onClosedContext(new ContextClosedEvent(mock(WebApplicationContext.class)));
        verify(task).cancel(true);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void test_start_stop() {
        ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
        TaskScheduler scheduler = mock(TaskScheduler.class);
        RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator, scheduler);

        ScheduledFuture task = mock(ScheduledFuture.class);
        when(scheduler.scheduleAtFixedRate(isA(Runnable.class), eq(Duration.ofSeconds(10)))).thenReturn(task);

        listener.startRegisterTask();
        verify(scheduler).scheduleAtFixedRate(isA(Runnable.class), eq(Duration.ofSeconds(10)));

        listener.stopRegisterTask();
        verify(task).cancel(true);
    }

    @Test
    public void test_no_deregister() {
        ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
        TaskScheduler scheduler = mock(TaskScheduler.class);
        RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator, scheduler);

        listener.onClosedContext(new ContextClosedEvent(mock(WebApplicationContext.class)));

        verify(registrator, never()).deregister();
    }

    @Test
    public void test_deregister() {
        ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
        TaskScheduler scheduler = mock(TaskScheduler.class);
        RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator, scheduler);
        listener.setAutoDeregister(true);

        listener.onClosedContext(new ContextClosedEvent(mock(WebApplicationContext.class)));

        verify(registrator).deregister();
    }
}
