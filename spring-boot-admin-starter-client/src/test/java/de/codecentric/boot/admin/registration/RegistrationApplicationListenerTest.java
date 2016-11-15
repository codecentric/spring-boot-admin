package de.codecentric.boot.admin.registration;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ScheduledFuture;

import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import de.codecentric.boot.admin.client.registration.ApplicationRegistrator;
import de.codecentric.boot.admin.client.registration.RegistrationApplicationListener;

public class RegistrationApplicationListenerTest {

	@Test
	public void test_register() throws Exception {
		ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
		TaskScheduler scheduler = mock(TaskScheduler.class);
		RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator,
				scheduler);

		listener.onApplicationReady(
				new ApplicationReadyEvent(mock(SpringApplication.class), null,
						mock(ConfigurableWebApplicationContext.class)));

		verify(scheduler).scheduleAtFixedRate(isA(Runnable.class), eq(10_000L));
	}

	@Test
	public void test_no_register() throws Exception {
		ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
		TaskScheduler scheduler = mock(TaskScheduler.class);
		RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator,
				scheduler);
		listener.setAutoRegister(false);

		listener.onApplicationReady(
				new ApplicationReadyEvent(mock(SpringApplication.class), null,
						mock(ConfigurableWebApplicationContext.class)));

		verify(scheduler, never()).scheduleAtFixedRate(isA(Runnable.class), eq(10_000L));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void test_no_register_after_close() throws Exception {
		ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
		TaskScheduler scheduler = mock(TaskScheduler.class);
		RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator,
				scheduler);

		ScheduledFuture task = mock(ScheduledFuture.class);
		when(scheduler.scheduleAtFixedRate(isA(Runnable.class), eq(10_000L))).thenReturn(task);

		listener.onApplicationReady(
				new ApplicationReadyEvent(mock(SpringApplication.class), null,
						mock(ConfigurableWebApplicationContext.class)));

		verify(scheduler).scheduleAtFixedRate(isA(Runnable.class), eq(10_000L));

		listener.onClosedContext(new ContextClosedEvent(mock(EmbeddedWebApplicationContext.class)));
		verify(task).cancel(true);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void test_start_stop() throws Exception {
		ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
		TaskScheduler scheduler = mock(TaskScheduler.class);
		RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator,
				scheduler);

		ScheduledFuture task = mock(ScheduledFuture.class);
		when(scheduler.scheduleAtFixedRate(isA(Runnable.class), eq(10_000L))).thenReturn(task);

		listener.startRegisterTask();
		verify(scheduler).scheduleAtFixedRate(isA(Runnable.class), eq(10_000L));

		listener.stopRegisterTask();
		verify(task).cancel(true);
	}

	@Test
	public void test_no_deregister() throws Exception {
		ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
		TaskScheduler scheduler = mock(TaskScheduler.class);
		RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator,
				scheduler);

		listener.onClosedContext(new ContextClosedEvent(mock(EmbeddedWebApplicationContext.class)));

		verify(registrator, never()).deregister();
	}

	@Test
	public void test_deregister() throws Exception {
		ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
		TaskScheduler scheduler = mock(TaskScheduler.class);
		RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator,
				scheduler);
		listener.setAutoDeregister(true);

		listener.onClosedContext(new ContextClosedEvent(mock(EmbeddedWebApplicationContext.class)));

		verify(registrator).deregister();
	}
}
