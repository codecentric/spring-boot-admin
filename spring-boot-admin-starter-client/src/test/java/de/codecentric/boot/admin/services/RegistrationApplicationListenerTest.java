package de.codecentric.boot.admin.services;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;

public class RegistrationApplicationListenerTest {

	@Test
	public void test_register() throws Exception {
		ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
		RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator);

		listener.onApplicationReady(
				new ApplicationReadyEvent(mock(SpringApplication.class), null, null));

		Thread.sleep(500);
		verify(registrator).register();
	}

	@Test
	public void test_no_register() throws Exception {
		ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
		RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator);
		listener.setAutoRegister(false);

		listener.onApplicationReady(
				new ApplicationReadyEvent(mock(SpringApplication.class), null, null));

		Thread.sleep(500);
		verify(registrator, never()).register();
	}

	@Test
	public void test_no_deregister() throws Exception {
		ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
		RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator);

		listener.onClosedContext(new ContextClosedEvent(mock(EmbeddedWebApplicationContext.class)));

		verify(registrator, never()).deregister();
	}

	@Test
	public void test_deregister() throws Exception {
		ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
		RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator);
		listener.setAutoDeregister(true);

		listener.onClosedContext(new ContextClosedEvent(mock(EmbeddedWebApplicationContext.class)));

		verify(registrator).deregister();
	}
}
