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

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import de.codecentric.boot.admin.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.event.ClientApplicationUnregisteredEvent;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.registry.store.ApplicationStore;

/**
 * Registry for all applications that should be managed/administrated by the Spring Boot Admin application.
 * Backed by an ApplicationStore for persistence and an ApplicationIdGenerator for id generation.
 */
public class ApplicationRegistry implements ApplicationContextAware {
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationRegistry.class);

	private final ApplicationStore store;
	private final ApplicationIdGenerator generator;
	private ApplicationContext context;

	public ApplicationRegistry(ApplicationStore store, ApplicationIdGenerator generator) {
		this.store = store;
		this.generator = generator;
	}

	/**
	 * Register application.
	 *
	 * @param app application to be registered.
	 * @return the registered application.
	 */
	public Application register(Application app) {
		Validate.notNull(app, "Application must not be null");
		Validate.notNull(app.getUrl(), "URL must not be null");
		Validate.isTrue(checkUrl(app.getUrl()), "URL is not valid");

		String applicationId = generator.generateId(app);
		Validate.notNull(applicationId, "ID must not be null");

		Application newApp = new Application(app.getUrl(), app.getName(), applicationId);
		Application oldApp = store.save(newApp);

		if (oldApp == null) {
			LOGGER.info("New Application {} registered ", newApp);
			context.publishEvent(new ClientApplicationRegisteredEvent(this, newApp));
		} else {
			if ((app.getUrl().equals(oldApp.getUrl()) && app.getName().equals(oldApp.getName()))) {
				LOGGER.debug("Application {} refreshed", newApp);
			} else {
				LOGGER.warn("Application {} replaced by Application {}", newApp, oldApp);
			}
		}
		return newApp;
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
	public Application unregister(String id) {
		Application app = store.delete(id);
		if (app != null) {
			LOGGER.info("Application {} unregistered ", app);
			context.publishEvent(new ClientApplicationUnregisteredEvent(this, app));
		}
		return app;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.context = applicationContext;
	}
}
