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
package de.codecentric.boot.admin.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
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

	/**
	 * Register an application within this admin application.
	 * 
	 * @param app
	 *            The application infos.
	 * @return The registered application.
	 */
	@RequestMapping(value = "/api/applications", method = RequestMethod.POST)
	@ResponseBody
	public Application register(@RequestBody Application app) {
		LOGGER.info("Register application with ID '{}' and URL '{}'", app.getId(), app.getUrl());
		return registry.register(app);
	}

	/**
	 * Get a single application out of the registry.
	 * 
	 * @param id
	 *            The application identifier.
	 * @return The registered application.
	 */
	@RequestMapping(value = "/api/application/{id}", method = RequestMethod.GET)
	@ResponseBody
	public Application get(@PathVariable String id) {
		LOGGER.debug("Deliver registered application with ID '{}'", id);
		return registry.getApplication(id);
	}

	/**
	 * List all registered applications.
	 * 
	 * @return List.
	 */
	@RequestMapping(value = "/api/applications", method = RequestMethod.GET)
	@ResponseBody
	public List<Application> applications() {
		LOGGER.debug("Deliver all registered applications");
		return registry.getApplications();
	}

}
