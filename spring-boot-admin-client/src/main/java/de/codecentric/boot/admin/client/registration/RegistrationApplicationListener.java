/*
 * Copyright 2014-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.client.registration;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Listener responsible for starting and stopping the registration task when the
 * application is ready.
 *
 * @author Johannes Edmeier
 */
public class RegistrationApplicationListener implements InitializingBean, DisposableBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationApplicationListener.class);

	private final ApplicationRegistrator registrator;

	private final ThreadPoolTaskScheduler taskScheduler;

	private boolean autoDeregister = false;

	private boolean autoRegister = true;

	private Duration registerPeriod = Duration.ofSeconds(10);

	@Nullable
	private volatile ScheduledFuture<?> scheduledTask;

	public RegistrationApplicationListener(ApplicationRegistrator registrator) {
		this(registrator, registrationTaskScheduler());
	}

	private static ThreadPoolTaskScheduler registrationTaskScheduler() {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setPoolSize(1);
		taskScheduler.setRemoveOnCancelPolicy(true);
		taskScheduler.setThreadNamePrefix("registrationTask");
		return taskScheduler;
	}

	RegistrationApplicationListener(ApplicationRegistrator registrator, ThreadPoolTaskScheduler taskScheduler) {
		this.registrator = registrator;
		this.taskScheduler = taskScheduler;
	}

	@EventListener
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void onApplicationReady(ApplicationReadyEvent event) {
		if (autoRegister) {
			startRegisterTask();
		}
	}

	@EventListener
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void onClosedContext(ContextClosedEvent event) {
		if (event.getApplicationContext().getParent() == null
				|| "bootstrap".equals(event.getApplicationContext().getParent().getId())) {
			stopRegisterTask();

			if (autoDeregister) {
				registrator.deregister();
			}
		}
	}

	public void startRegisterTask() {
		if (scheduledTask != null && !scheduledTask.isDone()) {
			return;
		}

		scheduledTask = taskScheduler.scheduleAtFixedRate(registrator::register, registerPeriod);
		LOGGER.debug("Scheduled registration task for every {}ms", registerPeriod.toMillis());
	}

	public void stopRegisterTask() {
		if (scheduledTask != null && !scheduledTask.isDone()) {
			scheduledTask.cancel(true);
			LOGGER.debug("Canceled registration task");
		}
	}

	public void setAutoDeregister(boolean autoDeregister) {
		this.autoDeregister = autoDeregister;
	}

	public void setAutoRegister(boolean autoRegister) {
		this.autoRegister = autoRegister;
	}

	public void setRegisterPeriod(Duration registerPeriod) {
		this.registerPeriod = registerPeriod;
	}

	@Override
	public void afterPropertiesSet() {
		taskScheduler.afterPropertiesSet();
	}

	@Override
	public void destroy() {
		taskScheduler.destroy();
	}

}
