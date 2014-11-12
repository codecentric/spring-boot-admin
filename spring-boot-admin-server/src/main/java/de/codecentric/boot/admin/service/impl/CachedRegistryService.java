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
package de.codecentric.boot.admin.service.impl;

import com.hazelcast.core.IdGenerator;
import de.codecentric.boot.admin.exception.ApplicationNotFoundException;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.service.RegistryService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import static de.codecentric.boot.admin.model.Application.*;

/**
 * Registry for all applications that should be managed/administrated by the spring-boot-admin application. This
 * registry is stored in a Hazelcast distributed memory cache.
 */
@Service
@ThreadSafe
public class CachedRegistryService implements RegistryService {

	private ConcurrentMap<Long, Application> registry;

    private IdGenerator idGenerator;

    public CachedRegistryService(ConcurrentMap registry, IdGenerator idGenerator){
        this.registry = registry;
        this.idGenerator = idGenerator;
    }

	/**
	 * Register application.
	 * 
	 * @param app The Application.
     * @return the created unique ID of the Application.
     *
	 */
    @Override
	public Long registerApplication(Application app) {
        validateApplication(app);
        long id = idGenerator.newId();
		registry.putIfAbsent(id, new ApplicationBuilder(app).withId(id).build());
        return id;
	}

    private void validateApplication(Application app) {
        Assert.notNull(app, "Application must not be null");
        Assert.notNull(app.getUrl(), "Application URL must not be null");
        Assert.notNull(app.getName(), "Application Name must not be null");
    }

    /**
     * Update a registered application
     *
     * @param app The Application.
     * @return the updated Application.
     *
     */
    @Override
    public Application updateApplication(Application app) {
        validateApplication(app);
        Assert.notNull(app.getId(), "Application ID must not be null");
        registry.replace(app.getId(), app);
        return app;
    }


    /**
     * Unregister application.
     *
     * @param id
     *            The ID of the application.
     */
    @Override
    public void unregisterApplicationById(Long id) {
        Assert.notNull(id, "Application ID must not be null");
        if (!isRegistered(id)) {
            throw new ApplicationNotFoundException("Application with ID " + id + " is not registered");
        }
        registry.remove(id);
    }

    /**
     * Get a list of all registered applications.
     *
     * @return List.
     */
    @Override
    public List<Application> getAllApplications() {
        return new ArrayList<>(registry.values());
    }

    /**
     * Get a specific application inside the registry.
     *
     * @param id Id of the application
     * @return the Application.
     */
    @Override
     public Application getApplicationById(Long id) {
        Assert.notNull(id, "Application ID must not be null");
        if (!isRegistered(id)) {
            throw new ApplicationNotFoundException("Application with ID " + id + " is not registered");
        }
        return registry.get(id);
    }

    /**
     * Remove all registered applications.
     */
    @Override
    public void removeAllApplications() {
        registry.clear();
    }

	/**
	 * Checks, if an application is already registered.
	 * 
	 * @param id
	 *            The application ID.
	 * @return exists?
	 */
	public boolean isRegistered(Long id) {
		return registry.containsKey(id);
	}


}
