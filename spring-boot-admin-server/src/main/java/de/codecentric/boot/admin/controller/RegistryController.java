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

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.service.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.concurrent.ThreadSafe;
import java.net.URI;
import java.util.List;

/**
 * REST controller for controlling registration of managed applications.
 */
@Api(value = "registry-api", description = "Registry REST API for controlling registration of managed applications.")
@RestController
@ThreadSafe
public class RegistryController {

	private static final Logger LOGGER = LoggerFactory.getLogger(RegistryController.class);

	@Autowired
	private RegistryService registry;

	/**
	 * Register an application within this admin application.
	 * 
	 * @param app The application to register.
     * @return the created unique ID of the Application.
	 */
    @ApiOperation(value = "Register an application.", notes =  "Register an application.")
	@RequestMapping(value = "/api/applications", method = RequestMethod.POST)
	public ResponseEntity<Long> registerApplication(@RequestBody Application app, UriComponentsBuilder builder) {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Register application with name '{}' and URL '{}'", app.getName(), app.getUrl());
        }
		Long id = registry.registerApplication(app);

        final URI location = builder.path("/api/application/{id}").buildAndExpand(id).toUri();
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Created an instance with ID '{}' of application with name '{}' under location '{}'", id, app.getName(), location);
        }
        final HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(id, headers, HttpStatus.CREATED);
    }

    /**
     * Update a registered application.
     *
     * @param app
     *            The application infos.
     * @return The updated application.
     */
    @ApiOperation(value = "Update a registered application.", notes =  "Update a registered application.")
    @RequestMapping(value = "/api/applications", method = RequestMethod.PUT)
    public Application updateApplication(@RequestBody Application app) {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Update application with ID '{}' and name '{}'", app.getId(), app.getName());
        }
        return registry.updateApplication(app);
    }

	/**
	 * Get a registered application by ID.
	 * 
	 * @param id The application identifier.
	 * @return The registered application.
	 */
    @ApiOperation(value = "Get a registered application by ID.", notes = "Get a registered application by ID.")
	@RequestMapping(value = "/api/application/{id}", method = RequestMethod.GET)
	public Application getApplicationById(@PathVariable Long id) {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Deliver registered application with ID '{}'", id);
        }
        return registry.getApplicationById(id);
	}

    /**
     * Unregister an application.
     *
     * @param id The application identifier.
     */
    @ApiOperation(value = "Unregister an application by ID.", notes = "Unregister an application by ID.")
    @RequestMapping(value = "/api/application/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unregisterApplicationById(@PathVariable Long id) {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Unregister application with ID '{}'", id);
        }
        registry.unregisterApplicationById(id);
    }

	/**
     * List all registered applications.
     *
     * @return List.
     */
    @ApiOperation(value = "Get all registered applications.", notes = "Get all registered applications.")
    @RequestMapping(value = "/api/applications", method = RequestMethod.GET)
    public List<Application> getAllApplications() {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Deliver all registered applications");
        }
        return registry.getAllApplications();
    }

    /**
     * Remove all registered applications.
     *
     * @return List.
     */
    @ApiOperation(value = "Remove all registered applications.", notes = "Remove all registered applications.")
    @RequestMapping(value = "/api/applications", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeAllApplications() {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Remove all registered applications");
        }
        registry.removeAllApplications();
    }

}
