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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultApplicationRegistrator implements ApplicationRegistrator {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultApplicationRegistrator.class);

	private final ConcurrentHashMap<String, LongAdder> attempts = new ConcurrentHashMap<>();

	private final ConcurrentHashMap<String, String> registeredIds = new ConcurrentHashMap<>();

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
	public synchronized boolean register() {
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
			String id = this.registrationClient.register(adminUrl, application);
			if (this.registeredIds.put(registrationKey(adminUrl), id) == null) {
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
						application, this.adminUrls, ex.getMessage(), ex);
			}
			else {
				LOGGER.debug("Failed to register application as {} at spring-boot-admin ({}): {}", application,
						this.adminUrls, ex.getMessage(), ex);
			}
			return false;
		}
	}

	@Override
	public synchronized void deregister() {
		for (String adminUrl : this.adminUrls) {
			String key = registrationKey(adminUrl);
			String id = this.registeredIds.get(key);
			if (id == null) {
				continue;
			}
			try {
				this.registrationClient.deregister(adminUrl, id);
				this.registeredIds.remove(key, id);
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
		for (String adminUrl : this.adminUrls) {
			String id = this.registeredIds.get(adminUrl);
			if (id != null) {
				return id;
			}
		}
		return null;
	}

	private String registrationKey(String adminUrl) {
		// With register-once, all URLs are failover targets for the same registration.
		return this.registerOnce ? this.adminUrls[0] : adminUrl;
	}

}
