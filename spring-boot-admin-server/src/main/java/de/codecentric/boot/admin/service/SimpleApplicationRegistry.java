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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;

import de.codecentric.boot.admin.model.Application;

/**
 * This registry is just "in-memory", so that after a restart all applications have to be
 * registered again.
 */
@Service
public class SimpleApplicationRegistry implements ApplicationRegistry {

	private final Map<String, Application> store = new HashMap<>();

	@Override
	public void register(Application app) {
		Validate.notNull(app, "Application must not be null");
		Validate.notNull(app.getId(), "ID must not be null");
		Validate.notNull(app.getUrl(), "URL must not be null");
		Validate.isTrue(checkUrl(app.getUrl()), "URL is not valid");
		store.put(app.getId(), app);
	}

	/**
	 * Checks the syntax of the given URL.
	 *
	 * @param url
	 *            The URL.
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

	@Override
	public List<Application> getApplications() {
		return new ArrayList<>(store.values());
	}

	@Override
	public Application getApplication(String id) {
		return store.get(id);
	}

	@Override
	public Application unregister(String id) {
		return store.remove(id);
	}

}
