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

import static java.util.Arrays.asList;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.model.StatusInfo;
import de.codecentric.boot.admin.registry.store.ApplicationStore;
import de.codecentric.boot.admin.web.client.HttpHeadersProvider;

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
	private final HttpHeadersProvider httpHeadersProvider;
	private ApplicationEventPublisher publisher;
	private long statusLifetime = 10_000L;

	public StatusUpdater(RestTemplate restTemplate, ApplicationStore store,
			HttpHeadersProvider httpHeadersProvider) {
		this.restTemplate = restTemplate;
		this.store = store;
		this.httpHeadersProvider = httpHeadersProvider;
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

		Application newState = Application.create(application).withStatusInfo(newStatus).build();
		store.save(newState);

		if (!newStatus.equals(oldStatus)) {
			publisher.publishEvent(
					new ClientApplicationStatusChangedEvent(newState, oldStatus, newStatus));
		}
	}

	protected StatusInfo queryStatus(Application application) {
		LOGGER.trace("Updating status for {}", application);

		try {
			ResponseEntity<Map<String, Serializable>> response = doGetStatus(application);

			if (response.hasBody() && response.getBody().get("status") instanceof String) {
				return StatusInfo.valueOf((String) response.getBody().get("status"),
						response.getBody());
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
			return StatusInfo.ofOffline(toDetails(ex));
		}
	}

	private ResponseEntity<Map<String, Serializable>> doGetStatus(Application application) {
		@SuppressWarnings("unchecked")
		Class<Map<String, Serializable>> responseType = (Class<Map<String, Serializable>>) (Class<?>) Map.class;

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(asList(MediaType.APPLICATION_JSON));
		headers.putAll(httpHeadersProvider.getHeaders(application));

		ResponseEntity<Map<String, Serializable>> response = restTemplate.exchange(
				application.getHealthUrl(), HttpMethod.GET, new HttpEntity<Void>(headers),
				responseType);

		LOGGER.debug("/health for {} responded with {}", application, response);
		return response;
	}

	protected Map<String, Serializable> toDetails(Exception ex) {
		Map<String, Serializable> details = new HashMap<>();
		details.put("message", ex.getMessage());
		details.put("exception", ex.getClass().getName());
		return details;
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
