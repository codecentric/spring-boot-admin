/*
 * Copyright 2014-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.client.registration;

/**
 * Interface for client application registration at spring-boot-admin-server
 */
public interface ApplicationRegistrator {

	/**
	 * Registers the client application at spring-boot-admin-server.
	 * @return true if successful registration on at least one admin server
	 */
	boolean register();

	/**
	 * Tries to deregister currently registered application
	 */
	void deregister();

	/**
	 * @return the id of this client as given by the admin server. Returns null if the
	 * client has not registered against the admin server yet.
	 */
	String getRegisteredId();

}
