/*
 * Copyright 2013-2014 the original author or authors.
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
package de.codecentric.boot.admin.registry.store;

import java.util.Collection;

import de.codecentric.boot.admin.model.Application;

/**
 * Responsible for storing applications.
 */
public interface ApplicationStore {

	/**
	 * Inserts a new Application into the store. If the Id is already present in the store the old
	 * Application is replaced.
	 *
	 * @param app Application to store
	 * @return the Application associated previosly with the applications id.
	 */
	Application save(Application app);

	/**
	 * @return all Applications in the store;
	 */
	Collection<Application> findAll();

	/**
	 * @param id the applications id
	 * @return the Application with the specified id;
	 */
	Application find(String id);

	/**
	 * @param name the applications name
	 * @return all Applications with the specified name;
	 */
	Collection<Application> findByName(String name);

	/**
	 * @param id id of the Application to be removed
	 * @return the Application associated previosly with the applications id.
	 */
	Application delete(String id);
}
