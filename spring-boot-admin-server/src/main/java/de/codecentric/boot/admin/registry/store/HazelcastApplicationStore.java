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
package de.codecentric.boot.admin.registry.store;

import java.util.Collection;

import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicates;

import de.codecentric.boot.admin.model.Application;

public class HazelcastApplicationStore implements ApplicationStore {

	private IMap<String, Application> store;

	public HazelcastApplicationStore(IMap<String, Application> store) {
		this.store = store;
	}

	@Override
	public Application save(Application app) {
		return store.put(app.getId(), app);
	}

	@Override
	public Collection<Application> findAll() {
		return store.values();
	}

	@Override
	public Application find(String id) {
		return store.get(id);
	}

	@Override
	public Collection<Application> findByName(String name) {
		return store.values(Predicates.equal("name", name));
	}

	@Override
	public Application delete(String id) {
		return store.remove(id);
	}

}
