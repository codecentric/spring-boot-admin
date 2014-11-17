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
		return store.putIfAbsent(app.getId(), app);
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
