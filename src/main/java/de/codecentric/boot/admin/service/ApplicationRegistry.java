package de.codecentric.boot.admin.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;

import de.codecentric.boot.admin.model.Application;

/**
 * Registry for all applications that should be managed/administrated by the spring-boot-admin application. This
 * registry is just "in-memory", so that after a restart all applications have to be registered again.
 */
@Service
public class ApplicationRegistry {

	private final Map<String, Application> registry = new HashMap<>();

	/**
	 * Register application.
	 * 
	 * @param app
	 *            The Application.
	 */
	public Application register(Application app) {
		Validate.notNull(app, "Application must not be null");
		Validate.notNull(app.getId(), "Application ID must not be null");
		Validate.notNull(app.getUrl(), "Application URL must not be null");
		return registry.put(app.getId(), app);
	}

	/**
	 * Checks, if an application is already registerd.
	 * 
	 * @param id
	 *            The application ID.
	 * @return exists?
	 */
	public boolean isRegistered(String id) {
		return registry.containsKey(id);
	}

	/**
	 * Get a list of all registered applications.
	 * 
	 * @return List.
	 */
	public List<Application> getApplications() {
		return new ArrayList<>(registry.values());
	}

	/**
	 * Get a specific application inside the registry.
	 * 
	 * @param id
	 *            Id.
	 * @return Application.
	 */
	public Application getApplication(String id) {
		if (!isRegistered(id)) {
			throw new IllegalArgumentException("Application with ID " + id + " is not registered");
		}
		return registry.get(id);
	}

}
