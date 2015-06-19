package de.codecentric.boot.admin.services;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.task.SyncTaskExecutor;

public class RegistrationApplicationListenerTest {

	@Test
	public void test_register_embedded() {
		ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
		RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator, new SyncTaskExecutor());

		listener.onStartedEmbeddedServer(new EmbeddedServletContainerInitializedEvent(
				mock(EmbeddedWebApplicationContext.class), mock(EmbeddedServletContainer.class)));

		verify(registrator).register();
	}

	@Test
	public void test_register_war() {
		ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
		RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator, new SyncTaskExecutor());

		listener.onStartedDeployedWar(new ContextRefreshedEvent(
				mock(EmbeddedWebApplicationContext.class)));

		verify(registrator).register();
	}

	@Test
	public void test_no_register_war() {
		ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
		RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator, new SyncTaskExecutor());

		EmbeddedWebApplicationContext context = mock(EmbeddedWebApplicationContext.class);
		when(context.getEmbeddedServletContainer())
				.thenReturn(mock(EmbeddedServletContainer.class));
		listener.onStartedDeployedWar(new ContextRefreshedEvent(context));
		verify(registrator, never()).register();
	}

	@Test
	public void test_no_deregister() {
		ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
		RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator, new SyncTaskExecutor());

		listener.onClosedContext(new ContextClosedEvent(mock(EmbeddedWebApplicationContext.class)));

		verify(registrator, never()).deregister();
	}

	@Test
	public void test_deregister() {
		ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
		RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator, new SyncTaskExecutor());
		listener.setAutoDeregister(true);

		listener.onClosedContext(new ContextClosedEvent(mock(EmbeddedWebApplicationContext.class)));

		verify(registrator).deregister();
	}
}
