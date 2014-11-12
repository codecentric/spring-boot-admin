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
package de.codecentric.boot.admin.config;

import de.codecentric.boot.admin.cache.config.AdminServerCacheConfig;
import de.codecentric.boot.admin.controller.RegistryController;
import de.codecentric.boot.admin.exceptionhandler.ControllerExceptionHandler;
import de.codecentric.boot.admin.service.impl.CachedRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(AdminServerCacheConfig.class)
public class AdminServerConfig{

    @Autowired
    private AdminServerCacheConfig adminServerCacheConfig;

	/**
	 * Controller with REST-API for spring-boot applications to register itself.
	 */
	@Bean
	public RegistryController registryController() {
		return new RegistryController();
	}

	/**
	 * Registry for all registered application.
	 */
	@Bean
	public CachedRegistryService applicationRegistry() {
		return new CachedRegistryService(adminServerCacheConfig.applicationRegistry(), adminServerCacheConfig.applicationIdGenerator());
	}

    /**
     * Global exception handler which handles exceptions thrown by {@link org.springframework.stereotype.Controller}.
     */
    @Bean
    public ControllerExceptionHandler controllerExceptionHandler() {
        return new ControllerExceptionHandler();
    }

}
