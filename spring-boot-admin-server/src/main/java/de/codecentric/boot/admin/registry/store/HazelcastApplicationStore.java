package de.codecentric.boot.admin.registry.store;

import java.util.ArrayList;
import java.util.List;

import com.hazelcast.core.IMap;

import de.codecentric.boot.admin.model.Application;

public class HazelcastApplicationStore implements ApplicationStore {

	private IMap<String, Application> store;

	public HazelcastApplicationStore(IMap<String, Application> store) {
		this.store = store;
	}

	@Override
	public Application put(Application app) {
		return store.putIfAbsent(app.getId(), app);
	}

	@Override
	public List<Application> getAll() {
		return new ArrayList<Application>(store.values());
	}

	@Override
	public Application get(String id) {
		return store.get(id);
	}

	@Override
	public Application remove(String id) {
		return store.remove(id);
	}

}
