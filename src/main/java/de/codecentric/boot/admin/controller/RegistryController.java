package de.codecentric.boot.admin.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.service.ApplicationRegistry;

/**
 * REST controller for controlling registration of managed applications.
 */
@Controller
public class RegistryController {

	private static final Logger LOGGER = LoggerFactory.getLogger(RegistryController.class);

	@Autowired
	private ApplicationRegistry registry;

	@RequestMapping(value = "/api/applications", method = RequestMethod.POST)
	@ResponseBody
	public void register(@RequestBody Application app) {
		LOGGER.info("Register application with ID '{}' and URL '{}'", app.getId(), app.getUrl());
		registry.register(app);
	}

	@RequestMapping(value = "/api/application", method = RequestMethod.GET)
	@ResponseBody
	public Application get(@RequestBody String id) {
		LOGGER.debug("Deliver registered application with ID '{}'", id);
		return registry.getApplication(id);
	}

	@RequestMapping(value = "/api/applications", method = RequestMethod.GET)
	@ResponseBody
	public List<Application> applications() {
		LOGGER.debug("Deliver all registered applications");
		return registry.getApplications();
	}

}
