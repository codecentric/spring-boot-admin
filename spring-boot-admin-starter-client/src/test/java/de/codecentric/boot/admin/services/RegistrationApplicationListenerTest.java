package de.codecentric.boot.admin.services;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.task.SyncTaskExecutor;

public class RegistrationApplicationListenerTest {

	@Test
	public void test_register_embedded() {
		ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
		RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator,
				new SyncTaskExecutor());

		listener.onApplicationReady(
				new ApplicationReadyEvent(mock(SpringApplication.class), null, null));

		verify(registrator).register();
	}

	@Test
	public void test_register_war() {
		ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
		RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator,
				new SyncTaskExecutor());

		listener.onApplicationReady(
				new ApplicationReadyEvent(mock(SpringApplication.class), null, null));

		verify(registrator).register();
	}

	@Test
	public void test_no_deregister() {
		ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
		RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator,
				new SyncTaskExecutor());

		listener.onClosedContext(new ContextClosedEvent(mock(EmbeddedWebApplicationContext.class)));

		verify(registrator, never()).deregister();
	}

	@Test
	public void test_deregister() {
		ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
		RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator,
				new SyncTaskExecutor());
		listener.setAutoDeregister(true);

		listener.onClosedContext(new ContextClosedEvent(mock(EmbeddedWebApplicationContext.class)));

		verify(registrator).deregister();
	}
}
