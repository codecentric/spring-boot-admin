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
package de.codecentric.boot.admin.service;

import java.util.List;

import de.codecentric.boot.admin.model.Application;

/**
 * Registry for all applications that should be managed/administrated by the
 * spring-boot-admin application.
 */
public interface ApplicationRegistry {

	/**
	 * Register application.
	 *
	 * @param app The Application.
	 */
	void register(Application app);

	/**
	 * Get a list of all registered applications.
	 *
	 * @return List.
	 */
	List<Application> getApplications();

	/**
	 * Get a specific application inside the registry.
	 *
	 * @param id Id.
	 * @return Application.
	 */
	Application getApplication(String id);

	/**
	 * Remove a specific application from registry
	 * @param id
	 * @return the unregistered Application
	 */
	Application unregister(String id);

}