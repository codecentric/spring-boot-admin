package de.codecentric.boot.admin.services;

import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import de.codecentric.boot.admin.config.AdminProperties;

@Order(Ordered.LOWEST_PRECEDENCE)
public class RegistrationApplicationListener implements ApplicationListener<ApplicationEvent> {
	private final AdminProperties admin;
	private final ApplicationRegistrator registrator;
	private final TaskExecutor executor;

	public RegistrationApplicationListener(AdminProperties admin,
			ApplicationRegistrator registrator, TaskExecutor executor) {
		this.admin = admin;
		this.registrator = registrator;
		this.executor = executor;
	}

	public RegistrationApplicationListener(AdminProperties admin,
			ApplicationRegistrator registrator) {
		this(admin, registrator, new SimpleAsyncTaskExecutor());
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (startedDeployedWar(event) || startedEmbeddedServer(event)) {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					registrator.register();
				}
			});
		} else if (admin.isAutoDeregistration() && event instanceof ContextClosedEvent) {
			registrator.deregister();
		}
	}

	private boolean startedEmbeddedServer(ApplicationEvent event) {
		return event instanceof EmbeddedServletContainerInitializedEvent;
	}

	private boolean startedDeployedWar(ApplicationEvent event) {
		if (event instanceof ContextRefreshedEvent) {
			ApplicationContextEvent contextEvent = (ApplicationContextEvent) event;
			if (contextEvent.getApplicationContext() instanceof EmbeddedWebApplicationContext) {
				EmbeddedWebApplicationContext context = (EmbeddedWebApplicationContext) contextEvent
						.getApplicationContext();
				return context.getEmbeddedServletContainer() == null;
			}
		}
		return false;
	}
}
