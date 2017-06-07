package de.codecentric.boot.admin.registry;

import de.codecentric.boot.admin.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.model.Application;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.concurrent.SettableListenableFuture;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StatusUpdateApplicationListenerTest {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void test_start_stop() throws Exception {
		StatusUpdater statusUpdater = mock(StatusUpdater.class);
		ThreadPoolTaskScheduler scheduler = mock(ThreadPoolTaskScheduler.class);
		StatusUpdateApplicationListener listener = new StatusUpdateApplicationListener(
				statusUpdater, scheduler);

		ScheduledFuture task = mock(ScheduledFuture.class);
		when(scheduler.scheduleAtFixedRate(isA(Runnable.class), eq(10_000L))).thenReturn(task);

		listener.onApplicationReady(
				new ApplicationReadyEvent(mock(SpringApplication.class), null,
						mock(ConfigurableWebApplicationContext.class)));
		verify(scheduler).scheduleAtFixedRate(isA(Runnable.class), eq(10_000L));

		listener.onContextClosed(new ContextClosedEvent(mock(ServletWebServerApplicationContext.class)));
		verify(task).cancel(true);
	}

	@Test
	public void test_newApplication() throws Exception {
		StatusUpdater statusUpdater = mock(StatusUpdater.class);
		ThreadPoolTaskScheduler scheduler = mock(ThreadPoolTaskScheduler.class);
		when(scheduler.submit(any(Runnable.class))).then(new Answer<Future<?>>() {
			@Override
			public Future<?> answer(InvocationOnMock invocation) throws Throwable {
				((Runnable) invocation.getArgument(0)).run();
				SettableListenableFuture<?> future = new SettableListenableFuture<Void>();
				future.set(null);
				return future;
			}
		});

		StatusUpdateApplicationListener listener = new StatusUpdateApplicationListener(
				statusUpdater, scheduler);

		Application application = Application.create("test").withHealthUrl("http://example.com")
				.build();

		listener.onClientApplicationRegistered(new ClientApplicationRegisteredEvent(application));

		verify(statusUpdater).updateStatus(eq(application));
	}

}
