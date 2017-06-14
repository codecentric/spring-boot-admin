/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.server.registry;

import de.codecentric.boot.admin.server.event.ClientApplicationEvent;
import de.codecentric.boot.admin.server.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.server.event.ClientApplicationRegistrationUpdatedEvent;

import java.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.context.WebApplicationContext;

public class StatusUpdateApplicationListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatusUpdateApplicationListener.class);
    private final ThreadPoolTaskScheduler taskScheduler;
    private final StatusUpdater statusUpdater;
    private long updatePeriod = 10_000L;
    private ScheduledFuture<?> scheduledTask;

    public StatusUpdateApplicationListener(StatusUpdater statusUpdater, ThreadPoolTaskScheduler taskScheduler) {
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
    public void onClientApplicationRegistered(ClientApplicationEvent event) {
        if (event instanceof ClientApplicationRegisteredEvent ||
            event instanceof ClientApplicationRegistrationUpdatedEvent) {
            taskScheduler.submit(() -> statusUpdater.updateStatus(event.getApplication()));
        }
    }

    public void startStatusUpdate() {
        if (scheduledTask != null && !scheduledTask.isDone()) {
            return;
        }

        scheduledTask = taskScheduler.scheduleAtFixedRate(statusUpdater::updateStatusForAllApplications, updatePeriod);
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
