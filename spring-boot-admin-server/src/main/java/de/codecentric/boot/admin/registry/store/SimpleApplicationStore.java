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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import de.codecentric.boot.admin.model.Application;

/**
 * Simple ApplicationStore backed by a ConcurrentHashMap.
 */
public class SimpleApplicationStore implements ApplicationStore {

	private final ConcurrentMap<String, Application> map = new ConcurrentHashMap<>();

	@Override
	public Application save(Application app) {
		return map.put(app.getId(), app);
	}

	@Override
	public Collection<Application> findAll() {
		return map.values();
	}

	@Override
	public Application find(String id) {
		return map.get(id);
	}

	@Override
	public Collection<Application> findByName(String name) {
		List<Application> result = new ArrayList<>();
		for (Application a : map.values()) {
			if (name.equals(a.getName())) {
				result.add(a);
			}
		}
		return result;
	}

	@Override
	public Application delete(String id) {
		return map.remove(id);
	}

}
