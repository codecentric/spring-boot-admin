package de.codecentric.boot.admin.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;

import de.codecentric.boot.admin.model.Application;

@Service
public class ApplicationRegistry {

	private final List<Application> registry = new ArrayList<>();

	public void register(Application app) {
		Validate.notNull(app, "Application must not be null");
		Validate.notNull(app.getId(), "Application ID must not be null");
		Validate.notNull(app.getUrl(), "Application URL must not be null");
		registry.add(app);
	}

	public boolean isRegistered(Application app) {
		return registry.contains(app);
	}

	public List<Application> getApplications() {
		return Collections.unmodifiableList(registry);
	}

}
