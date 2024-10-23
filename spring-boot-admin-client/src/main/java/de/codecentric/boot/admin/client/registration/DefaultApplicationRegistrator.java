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

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultApplicationRegistrator implements ApplicationRegistrator {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationRegistrator.class);

	private final ConcurrentHashMap<String, LongAdder> attempts = new ConcurrentHashMap<>();

	private final AtomicReference<String> registeredId = new AtomicReference<>();

	private final ApplicationFactory applicationFactory;

	private final String[] adminUrls;

	private final boolean registerOnce;

	private final RegistrationClient registrationClient;

	public DefaultApplicationRegistrator(ApplicationFactory applicationFactory, RegistrationClient registrationClient,
			String[] adminUrls, boolean registerOnce) {
		this.applicationFactory = applicationFactory;
		this.adminUrls = adminUrls;
		this.registerOnce = registerOnce;
		this.registrationClient = registrationClient;
	}

	/**
	 * Registers the client application at spring-boot-admin-server.
	 * @return true if successful registration on at least one admin server
	 */
	@Override
	public boolean register() {
		Application application = this.applicationFactory.createApplication();
		boolean isRegistrationSuccessful = false;

		for (String adminUrl : this.adminUrls) {
			LongAdder attempt = this.attempts.computeIfAbsent(adminUrl, (k) -> new LongAdder());
			boolean successful = register(application, adminUrl, attempt.intValue() == 0);

			if (!successful) {
				attempt.increment();
			}
			else {
				attempt.reset();
				isRegistrationSuccessful = true;
				if (this.registerOnce) {
					break;
				}
			}
		}

		return isRegistrationSuccessful;
	}

	protected boolean register(Application application, String adminUrl, boolean firstAttempt) {
		try {
			Optional<String> response = this.registrationClient.register(adminUrl, application);
			if (response.isEmpty()) {
				LOGGER.debug("Request was no successful");
				return false;
			}
			String id = response.get();
			if (this.registeredId.compareAndSet(null, id)) {
				LOGGER.info("Application registered itself as {}", id);
			}
			else {
				LOGGER.debug("Application refreshed itself as {}", id);
			}
			return true;
		}
		catch (Exception ex) {
			if (firstAttempt) {
				LOGGER.warn(
						"Failed to register application as {} at spring-boot-admin ({}): {}. Further attempts are logged on DEBUG level",
						application, this.adminUrls, ex.getMessage());
			}
			else {
				LOGGER.debug("Failed to register application as {} at spring-boot-admin ({}): {}", application,
						this.adminUrls, ex.getMessage());
			}
			return false;
		}
	}

	@Override
	public void deregister() {
		String id = this.registeredId.get();
		if (id == null) {
			return;
		}

		for (String adminUrl : this.adminUrls) {
			try {
				this.registrationClient.deregister(adminUrl, id);
				this.registeredId.compareAndSet(id, null);
				if (this.registerOnce) {
					break;
				}
			}
			catch (Exception ex) {
				LOGGER.warn("Failed to deregister application (id={}) at spring-boot-admin ({}): {}", id, adminUrl,
						ex.getMessage());
			}
		}
	}

	@Override
	public String getRegisteredId() {
		return this.registeredId.get();
	}

}
