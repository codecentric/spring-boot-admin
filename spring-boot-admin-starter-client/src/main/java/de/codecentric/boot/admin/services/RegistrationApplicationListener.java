package de.codecentric.boot.admin.services;

import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

public class RegistrationApplicationListener {
	private final ApplicationRegistrator registrator;
	private boolean autoDeregister = false;
	private final TaskExecutor executor;

	public RegistrationApplicationListener(ApplicationRegistrator registrator, TaskExecutor executor) {
		this.registrator = registrator;
		this.executor = executor;
	}

	public RegistrationApplicationListener(ApplicationRegistrator registrator) {
		this(registrator, new SimpleAsyncTaskExecutor());
	}

	@EventListener
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void onStartedEmbeddedServer(EmbeddedServletContainerInitializedEvent event) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				registrator.register();
			}
		});
	}

	@EventListener
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void onStartedDeployedWar(ContextRefreshedEvent event) {
		ApplicationContextEvent contextEvent = event;
		if (contextEvent.getApplicationContext() instanceof EmbeddedWebApplicationContext) {
			EmbeddedWebApplicationContext context = (EmbeddedWebApplicationContext) contextEvent
					.getApplicationContext();
			if (context.getEmbeddedServletContainer() == null) {
				executor.execute(new Runnable() {
					@Override
					public void run() {
						registrator.register();
					}
				});
			}
		}
	}

	@EventListener
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void onClosedContext(ContextClosedEvent event) {
		if (autoDeregister) {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					registrator.deregister();
				}
			});
		}
	}

	public void setAutoDeregister(boolean autoDeregister) {
		this.autoDeregister = autoDeregister;
	}

	public boolean isAutoDeregister() {
		return autoDeregister;
	}
}
