package de.codecentric.boot.admin.registry;

import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.context.WebApplicationContext;

import de.codecentric.boot.admin.event.ClientApplicationRegisteredEvent;

public class StatusUpdateApplicationListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(StatusUpdateApplicationListener.class);
	private final ThreadPoolTaskScheduler taskScheduler;
	private final StatusUpdater statusUpdater;
	private long updatePeriod = 10_000L;
	private ScheduledFuture<?> scheduledTask;

	public StatusUpdateApplicationListener(StatusUpdater statusUpdater,
			ThreadPoolTaskScheduler taskScheduler) {
		this.statusUpdater = statusUpdater;
		this.taskScheduler = taskScheduler;
	}

	@EventListener
	public void onApplicationReady(ApplicationReadyEvent event) {
		if (event.getApplicationContext() instanceof WebApplicationContext) {
			startStatusUpdate();
		}
	}

	@EventListener
	public void onContextClosed(ContextClosedEvent event) {
		if (event.getApplicationContext() instanceof WebApplicationContext) {
			stopStatusUpdate();
		}
	}

	@EventListener
	public void onClientApplicationRegistered(final ClientApplicationRegisteredEvent event) {
		taskScheduler.submit(new Runnable() {
			@Override
			public void run() {
				statusUpdater.updateStatus(event.getApplication());
			}
		});
	}

	public void startStatusUpdate() {
		if (scheduledTask != null && !scheduledTask.isDone()) {
			return;
		}

		scheduledTask = taskScheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				statusUpdater.updateStatusForAllApplications();
			}
		}, updatePeriod);
		LOGGER.debug("Scheduled status-updater task for every {}ms", updatePeriod);

	}

	public void stopStatusUpdate() {
		if (scheduledTask != null && !scheduledTask.isDone()) {
			scheduledTask.cancel(true);
			LOGGER.debug("Canceled status-updater task");
		}
	}

	public void setUpdatePeriod(long updatePeriod) {
		this.updatePeriod = updatePeriod;
	}
}
