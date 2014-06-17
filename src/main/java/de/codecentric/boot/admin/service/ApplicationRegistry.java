package de.codecentric.boot.admin.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;

import de.codecentric.boot.admin.model.Application;

/**
 * Registry for all applications that should be managed/administrated by the spring-boot-admin application.
 */
@Service
public class ApplicationRegistry {

	private final List<Application> registry = new ArrayList<>();

	/**
	 * Register application.
	 * 
	 * @param app
	 *            The Application.
	 */
	public void register(Application app) {
		Validate.notNull(app, "Application must not be null");
		Validate.notNull(app.getId(), "Application ID must not be null");
		Validate.notNull(app.getUrl(), "Application URL must not be null");
		registry.add(app);
	}

	/**
	 * Checks, if an application is already registerd.
	 * 
	 * @param app
	 *            The application.
	 * @return exists?
	 */
	public boolean isRegistered(Application app) {
		return registry.contains(app);
	}

	/**
	 * Get a list of all registered applications.
	 * 
	 * @return List.
	 */
	public List<Application> getApplications() {
		return Collections.unmodifiableList(registry);
	}

}
