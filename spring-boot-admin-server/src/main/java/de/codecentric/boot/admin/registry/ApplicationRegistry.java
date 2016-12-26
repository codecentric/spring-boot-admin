/*
 * Copyright 2014 the original author or authors.
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import de.codecentric.boot.admin.event.ClientApplicationDeregisteredEvent;
import de.codecentric.boot.admin.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.registry.store.ApplicationStore;

/**
 * Registry for all applications that should be managed/administrated by the Spring Boot Admin
 * application. Backed by an ApplicationStore for persistence and an ApplicationIdGenerator for id
 * generation.
 */
public class ApplicationRegistry implements ApplicationEventPublisherAware {
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationRegistry.class);

	private final ApplicationStore store;
	private final ApplicationIdGenerator generator;
	private ApplicationEventPublisher publisher;

	public ApplicationRegistry(ApplicationStore store, ApplicationIdGenerator generator) {
		this.store = store;
		this.generator = generator;
	}

	/**
	 * Register application.
	 *
	 * @param application application to be registered.
	 * @return the registered application.
	 */
	public Application register(Application application) {
		Assert.notNull(application, "Application must not be null");
		Assert.hasText(application.getName(), "Name must not be null");
		Assert.hasText(application.getHealthUrl(), "Health-URL must not be null");
		Assert.isTrue(checkUrl(application.getHealthUrl()), "Health-URL is not valid");
		Assert.isTrue(
				StringUtils.isEmpty(application.getManagementUrl())
						|| checkUrl(application.getManagementUrl()), "URL is not valid");
		Assert.isTrue(
				StringUtils.isEmpty(application.getServiceUrl())
						|| checkUrl(application.getServiceUrl()), "URL is not valid");

		String applicationId = generator.generateId(application);
		Assert.notNull(applicationId, "ID must not be null");

		Application.Builder builder = Application.copyOf(application).withId(applicationId);
		Application existing = getApplication(applicationId);
		if (existing != null) {
			// Copy Status and Info from existing registration.
			builder.withStatusInfo(existing.getStatusInfo()).withInfo(existing.getInfo());
		}
		Application registering = builder.build();

		Application replaced = store.save(registering);
		if (replaced == null) {
			LOGGER.info("New Application {} registered ", registering);
			publisher.publishEvent(new ClientApplicationRegisteredEvent(registering));
		} else {
			if (registering.getId().equals(replaced.getId())) {
				LOGGER.debug("Application {} refreshed", registering);
			} else {
				LOGGER.warn("Application {} replaced by Application {}", registering, replaced);
			}
		}
		return registering;
	}


	/**
	 * Checks the syntax of the given URL.
	 *
	 * @param url The URL.
	 * @return true, if valid.
	 */
	private boolean checkUrl(String url) {
		try {
			new URL(url);
		} catch (MalformedURLException e) {
			return false;
		}
		return true;
	}

	/**
	 * Get a list of all registered applications.
	 *
	 * @return List of all applications.
	 */
	public Collection<Application> getApplications() {
		return store.findAll();
	}

	/**
	 * Get a list of all registered applications.
	 *
	 * @param name the name to search for.
	 * @return List of applications with the given name.
	 */
	public Collection<Application> getApplicationsByName(String name) {
		return store.findByName(name);
	}

	/**
	 * Get a specific application inside the registry.
	 *
	 * @param id Id.
	 * @return Application.
	 */
	public Application getApplication(String id) {
		return store.find(id);
	}

	/**
	 * Remove a specific application from registry
	 *
	 * @param id the applications id to unregister
	 * @return the unregistered Application
	 */
	public Application deregister(String id) {
		Application app = store.delete(id);
		if (app != null) {
			LOGGER.info("Application {} unregistered ", app);
			publisher.publishEvent(new ClientApplicationDeregisteredEvent(app));
		}
		return app;
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		publisher = applicationEventPublisher;
	}
}
