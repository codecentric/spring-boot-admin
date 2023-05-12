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

package de.codecentric.boot.admin.client.registration;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import static java.time.Duration.ZERO;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RegistrationApplicationListenerTest {

	@Test
	public void should_schedule_register_task() {
		ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
		ThreadPoolTaskScheduler scheduler = mock(ThreadPoolTaskScheduler.class);
		RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator, scheduler);

		listener.onApplicationReady(new ApplicationReadyEvent(mock(SpringApplication.class), null,
				mock(ConfigurableWebApplicationContext.class), ZERO));

		verify(scheduler).scheduleAtFixedRate(isA(Runnable.class), eq(Duration.ofSeconds(10)));
	}

	@Test
	public void should_no_schedule_register_task_when_not_autoRegister() {
		ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
		ThreadPoolTaskScheduler scheduler = mock(ThreadPoolTaskScheduler.class);
		RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator, scheduler);
		listener.setAutoRegister(false);

		listener.onApplicationReady(new ApplicationReadyEvent(mock(SpringApplication.class), null,
				mock(ConfigurableWebApplicationContext.class), ZERO));

		verify(scheduler, never()).scheduleAtFixedRate(isA(Runnable.class), eq(Duration.ofSeconds(10)));
	}

	@Test
	public void should_cancel_register_task_on_context_close() {
		ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
		ThreadPoolTaskScheduler scheduler = mock(ThreadPoolTaskScheduler.class);
		RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator, scheduler);

		ScheduledFuture<?> task = mock(ScheduledFuture.class);
		when(scheduler.scheduleAtFixedRate(isA(Runnable.class), eq(Duration.ofSeconds(10)))).then((invocation) -> task);

		listener.onApplicationReady(new ApplicationReadyEvent(mock(SpringApplication.class), null,
				mock(ConfigurableWebApplicationContext.class), ZERO));
		verify(scheduler).scheduleAtFixedRate(isA(Runnable.class), eq(Duration.ofSeconds(10)));

		listener.onClosedContext(new ContextClosedEvent(mock(WebApplicationContext.class)));
		verify(task).cancel(true);
	}

	@Test
	public void should_start_and_cancel_task_on_request() {
		ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
		ThreadPoolTaskScheduler scheduler = mock(ThreadPoolTaskScheduler.class);
		RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator, scheduler);

		ScheduledFuture<?> task = mock(ScheduledFuture.class);
		when(scheduler.scheduleAtFixedRate(isA(Runnable.class), eq(Duration.ofSeconds(10)))).then((invocation) -> task);

		listener.startRegisterTask();
		verify(scheduler).scheduleAtFixedRate(isA(Runnable.class), eq(Duration.ofSeconds(10)));

		listener.stopRegisterTask();
		verify(task).cancel(true);
	}

	@Test
	public void should_not_deregister_when_not_autoDeregister() {
		ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
		ThreadPoolTaskScheduler scheduler = mock(ThreadPoolTaskScheduler.class);
		RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator, scheduler);

		listener.onClosedContext(new ContextClosedEvent(mock(WebApplicationContext.class)));

		verify(registrator, never()).deregister();
	}

	@Test
	public void should_deregister_when_autoDeregister() {
		ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
		ThreadPoolTaskScheduler scheduler = mock(ThreadPoolTaskScheduler.class);
		RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator, scheduler);
		listener.setAutoDeregister(true);

		listener.onClosedContext(new ContextClosedEvent(mock(ApplicationContext.class)));

		verify(registrator).deregister();
	}

	@Test
	public void should_deregister_when_autoDeregister_and_parent_is_bootstrap_contex() {
		ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
		ThreadPoolTaskScheduler scheduler = mock(ThreadPoolTaskScheduler.class);
		RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator, scheduler);
		listener.setAutoDeregister(true);

		ApplicationContext parentContext = mock(ApplicationContext.class);
		when(parentContext.getId()).thenReturn("bootstrap");
		ApplicationContext mockContext = mock(ApplicationContext.class);
		when(mockContext.getParent()).thenReturn(parentContext);
		listener.onClosedContext(new ContextClosedEvent(mockContext));

		verify(registrator).deregister();
	}

	@Test
	public void should_not_deregister_when_autoDeregister_and_not_root() {
		ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
		ThreadPoolTaskScheduler scheduler = mock(ThreadPoolTaskScheduler.class);
		RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator, scheduler);
		listener.setAutoDeregister(true);

		ApplicationContext mockContext = mock(ApplicationContext.class);
		when(mockContext.getParent()).thenReturn(mock(ApplicationContext.class));
		listener.onClosedContext(new ContextClosedEvent(mockContext));

		verify(registrator, never()).deregister();
	}

	@Test
	public void should_init_and_shutdown_taskScheduler() {
		ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
		ThreadPoolTaskScheduler scheduler = mock(ThreadPoolTaskScheduler.class);
		RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator, scheduler);

		listener.afterPropertiesSet();
		verify(scheduler, times(1)).afterPropertiesSet();

		listener.destroy();
		verify(scheduler, times(1)).destroy();
	}

}
