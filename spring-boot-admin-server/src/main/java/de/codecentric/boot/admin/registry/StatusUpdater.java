/*
 * Copyright 2013-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.codecentric.boot.admin.registry;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.http.ResponseEntity;

import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.model.Info;
import de.codecentric.boot.admin.model.StatusInfo;
import de.codecentric.boot.admin.registry.store.ApplicationStore;
import de.codecentric.boot.admin.web.client.ApplicationOperations;

/**
 * The StatusUpdater is responsible for updating the status of all or a single application querying
 * the healthUrl.
 *
 * @author Johannes Edmeier
 */
public class StatusUpdater implements ApplicationEventPublisherAware {
	private static final Logger LOGGER = LoggerFactory.getLogger(StatusUpdater.class);

	private final ApplicationStore store;
	private final ApplicationOperations applicationOps;
	private ApplicationEventPublisher publisher;
	private long statusLifetime = 10_000L;

	public StatusUpdater(ApplicationStore store, ApplicationOperations applicationOps) {
		this.store = store;
		this.applicationOps = applicationOps;
	}

	public void updateStatusForAllApplications() {
		long now = System.currentTimeMillis();
		for (Application application : store.findAll()) {
			if (now - statusLifetime > application.getStatusInfo().getTimestamp()) {
				updateStatus(application);
			}
		}
	}

	public void updateStatus(Application application) {
		StatusInfo oldStatus = application.getStatusInfo();
		StatusInfo newStatus = queryStatus(application);
		boolean statusChanged = !newStatus.equals(oldStatus);

		Application.Builder builder = Application.copyOf(application).withStatusInfo(newStatus);

		if (statusChanged && !newStatus.isOffline() && !newStatus.isUnknown()) {
			builder.withInfo(queryInfo(application));
		}

		Application newState = builder.build();
		store.save(newState);

		if (statusChanged) {
			publisher.publishEvent(
					new ClientApplicationStatusChangedEvent(newState, oldStatus, newStatus));
		}
	}

	protected Info queryInfo(Application application) {
		try {
			ResponseEntity<Map<String, Serializable>> response = applicationOps
					.getInfo(application);
			if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
				return convertInfo(response);
			} else {
				LOGGER.info("Couldn't retrieve info for {}: {} - {}", application,
						response.getStatusCode(), response.getBody());
				return Info.empty();
			}
		} catch (Exception ex) {
			LOGGER.warn("Couldn't retrieve info for {}", application, ex);
			return convertInfo(ex);
		}
	}

	protected Info convertInfo(ResponseEntity<Map<String, Serializable>> response) {
		return Info.from(response.getBody());
	}

	protected Info convertInfo(Exception ex) {
		return Info.empty();
	}

	protected StatusInfo queryStatus(Application application) {
		LOGGER.trace("Updating status for {}", application);
		try {
			return convertStatusInfo(applicationOps.getHealth(application));
		} catch (Exception ex) {
			if ("OFFLINE".equals(application.getStatusInfo().getStatus())) {
				LOGGER.debug("Couldn't retrieve status for {}", application, ex);
			} else {
				LOGGER.info("Couldn't retrieve status for {}", application, ex);
			}
			return convertStatusInfo(ex);
		}
	}

	protected StatusInfo convertStatusInfo(ResponseEntity<Map<String, Serializable>> response) {
		if (response.hasBody() && response.getBody().get("status") instanceof String) {
			return StatusInfo.valueOf((String) response.getBody().get("status"),
					response.getBody());
		}
		if (response.getStatusCode().is2xxSuccessful()) {
			return StatusInfo.ofUp();
		}
		Map<String, Serializable> details = new HashMap<>();
		details.put("status", response.getStatusCodeValue());
		details.put("error", response.getStatusCode().getReasonPhrase());
		if (response.hasBody()) {
			details.putAll(response.getBody());
		}
		return StatusInfo.ofDown(details);
	}

	protected StatusInfo convertStatusInfo(Exception ex) {
		Map<String, Serializable> details = new HashMap<>();
		details.put("message", ex.getMessage());
		details.put("exception", ex.getClass().getName());
		return StatusInfo.ofOffline(details);
	}

	public void setStatusLifetime(long statusLifetime) {
		this.statusLifetime = statusLifetime;
	}

	public long getStatusLifetime() {
		return statusLifetime;
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}
}
