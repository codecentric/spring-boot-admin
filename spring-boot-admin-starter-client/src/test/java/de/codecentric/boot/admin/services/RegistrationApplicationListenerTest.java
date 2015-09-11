package de.codecentric.boot.admin.services;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.task.SyncTaskExecutor;

import de.codecentric.boot.admin.config.AdminProperties;

public class RegistrationApplicationListenerTest {

	private AdminProperties admin;

	@Before
	public void setup() {
		admin = new AdminProperties();
	}

	@Test
	public void test_register_embedded() {
		ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
		RegistrationApplicationListener listener = new RegistrationApplicationListener(admin,
				registrator, new SyncTaskExecutor());

		listener.onApplicationEvent(new EmbeddedServletContainerInitializedEvent(
				mock(EmbeddedWebApplicationContext.class), mock(EmbeddedServletContainer.class)));

		verify(registrator).register();
	}

	@Test
	public void test_register_war() {
		ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
		RegistrationApplicationListener listener = new RegistrationApplicationListener(admin,
				registrator, new SyncTaskExecutor());

		listener.onApplicationEvent(
				new ContextRefreshedEvent(mock(EmbeddedWebApplicationContext.class)));

		verify(registrator).register();
	}

	@Test
	public void test_no_register_war() {
		ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
		RegistrationApplicationListener listener = new RegistrationApplicationListener(admin,
				registrator, new SyncTaskExecutor());

		EmbeddedWebApplicationContext context = mock(EmbeddedWebApplicationContext.class);
		when(context.getEmbeddedServletContainer())
				.thenReturn(mock(EmbeddedServletContainer.class));
		listener.onApplicationEvent(new ContextRefreshedEvent(context));

		verify(registrator, never()).register();
	}

	@Test
	public void test_no_deregister() {
		ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
		RegistrationApplicationListener listener = new RegistrationApplicationListener(admin,
				registrator, new SyncTaskExecutor());

		listener.onApplicationEvent(
				new ContextClosedEvent(mock(EmbeddedWebApplicationContext.class)));

		verify(registrator, never()).deregister();
	}

	@Test
	public void test_deregister() {
		ApplicationRegistrator registrator = mock(ApplicationRegistrator.class);
		RegistrationApplicationListener listener = new RegistrationApplicationListener(admin,
				registrator);

		admin.setAutoDeregistration(true);

		listener.onApplicationEvent(
				new ContextClosedEvent(mock(EmbeddedWebApplicationContext.class)));

		verify(registrator).deregister();
	}

}
