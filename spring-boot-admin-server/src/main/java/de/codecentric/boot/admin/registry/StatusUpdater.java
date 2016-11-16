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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.model.StatusInfo;
import de.codecentric.boot.admin.registry.store.ApplicationStore;

/**
 * The StatusUpdater is responsible for updating the status of all or a single application querying
 * the healthUrl.
 *
 * @author Johannes Edmeier
 */
public class StatusUpdater implements ApplicationEventPublisherAware {
	private static final Logger LOGGER = LoggerFactory.getLogger(StatusUpdater.class);

	private final ApplicationStore store;
	private final RestTemplate restTemplate;
	private ApplicationEventPublisher publisher;
	private long statusLifetime = 10_000L;

	public StatusUpdater(RestTemplate restTemplate, ApplicationStore store) {
		this.restTemplate = restTemplate;
		this.store = store;
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
		Map<String, Map<String, Object>> healthIndicatorsMap = new LinkedHashMap<String, Map<String, Object>>();
		StatusInfo newStatus = queryStatus(application, healthIndicatorsMap);

		Application newState = Application.create(application).withStatusInfo(newStatus).build();
		store.save(newState);

		if (!newStatus.equals(oldStatus)) {
			publisher.publishEvent(new ClientApplicationStatusChangedEvent(newState, oldStatus,
					newStatus, healthIndicatorsMap));
		}
	}

	protected StatusInfo queryStatus(Application application,
			Map<String, Map<String, Object>> healthMap) {
		LOGGER.trace("Updating status for {}", application);

		try {
			@SuppressWarnings("unchecked")
			ResponseEntity<Map<String, Object>> response = restTemplate.getForEntity(
					application.getHealthUrl(), (Class<Map<String, Object>>) (Class<?>) Map.class);
			LOGGER.debug("/health for {} responded with {}", application, response);

			if (response.hasBody() && response.getBody().get("status") instanceof String) {
				StatusInfo newStatus = StatusInfo
						.valueOf((String) response.getBody().get("status"));
				// if no status change from previous, spare the search for failing indicator
				if (!newStatus.equals(application.getStatusInfo())) {
					for (Entry<String, Object> bodyEntry : response.getBody().entrySet()) {
						LOGGER.debug("health indicator {}: {}", bodyEntry.getKey(),
								bodyEntry.getValue());
						if (bodyEntry.getValue() instanceof Map) {
							@SuppressWarnings("unchecked")
							Map<String, Object> map = (Map<String, Object>) bodyEntry.getValue();
							if (map.containsKey("status")) {
								StatusInfo indicatorStatus = StatusInfo.valueOf((String) map.get("status"));
								if (newStatus.equals(indicatorStatus)) {// we got one
									map.remove("status");
									healthMap.put(bodyEntry.getKey(), map);
								}
							}
						}
					}
				}
				return newStatus;
			} else if (response.getStatusCode().is2xxSuccessful()) {
				return StatusInfo.ofUp();
			} else {
				return StatusInfo.ofDown();
			}

		} catch (Exception ex) {
			if ("OFFLINE".equals(application.getStatusInfo().getStatus())) {
				LOGGER.debug("Couldn't retrieve status for {}", application, ex);
			} else {
				LOGGER.warn("Couldn't retrieve status for {}", application, ex);
			}
			Map<String, Object> indicator = new LinkedHashMap<String, Object>();
			indicator.put("exception", (null == ex.getMessage()
					? (ex instanceof NullPointerException ? "NPE" : ex.getClass().getSimpleName())
					: ex.getMessage()));
			healthMap.put("error", indicator);
			return StatusInfo.ofOffline();
		}
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
