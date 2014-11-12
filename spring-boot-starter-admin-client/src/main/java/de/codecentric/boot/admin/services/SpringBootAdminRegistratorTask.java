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
package de.codecentric.boot.admin.services;

import de.codecentric.boot.admin.model.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Scheduler that checks the registration of the application at the spring-boot-admin.
 */
public class SpringBootAdminRegistratorTask implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpringBootAdminRegistratorTask.class);

	@Autowired
	private Environment env;

    private AtomicLong applicationId = new AtomicLong(-1);

	@PostConstruct
	public void check() {
        Assert.notNull(env.getProperty("server.port"), "The server port of the application is mandatory. Please customize server.port");
        Assert.notNull(env.getProperty("spring.application.name"), "The name of the application is mandatory. Please customize spring.application.name");
        Assert.notNull(env.getProperty("spring.boot.admin.url"),
                "The URL of the spring-boot-admin application is mandatory. Please customize spring.boot.admin.url");
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
        String applicationName = null;
        URL adminUrl = null;
        URL clientUrl = null;
        Application application = null;
        RestTemplate template = new RestTemplate();
        try {
            applicationName = env.getProperty("spring.application.name");
            int port = env.getProperty("server.port", Integer.class);
            adminUrl = new URL(env.getProperty("spring.boot.admin.url"));
            String managementPath = env.getProperty("management.contextPath", "");
            clientUrl = new URL("http", InetAddress.getLocalHost().getCanonicalHostName(), port, managementPath);
            application = new Application.ApplicationBuilder(applicationName, clientUrl).build();

            // Check if Application is already registered
            // If the current ID is still -1, the AdminServer returns HTTP Code 404 (NotFound) and the application must be registered
            Long currentId = applicationId.get();
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance().uri(adminUrl.toURI());
            URI registryUrl = uriBuilder.path("/api/application/{id}").buildAndExpand(currentId).toUri();
            ResponseEntity<Application> responseEntity = template.getForEntity(registryUrl, Application.class);
            // If an application is already registered with the same ID, check that the returned URL is the URL of this application
            if(responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody().getUrl().equals(application.getUrl())) {
                LOGGER.debug("Application already registered with ID '{}'", currentId);
            } else{
                // If the URL is different, register the application. This can happen when the Admin Server was restarted
                Long newId = registerApplication(applicationName, adminUrl, clientUrl, application, template);
                applicationId.set(newId);
            }
        }
        catch(HttpClientErrorException clientErrorException){
            // If the Status Code is 404 (NotFound)
            if(clientErrorException.getStatusCode() == HttpStatus.NOT_FOUND) {
                // register the application and store the ID
                Long newId = registerApplication(applicationName, adminUrl, clientUrl, application, template);
                applicationId.set(newId);
            }else{
                LOGGER.warn("Failed to register application at spring-boot-admin, message={}", clientErrorException.getMessage());
            }
        }
		catch (Exception e) {
			LOGGER.warn("Failed to register application at spring-boot-admin, message={}", e.getMessage());
		}
	}

    private Long registerApplication(String applicationName, URL adminUrl, URL clientUrl, Application application, RestTemplate template) {
        Long id = template.postForObject(adminUrl + "/api/applications", application, Long.class);
        LOGGER.info("Application registered itself at the admin application with ID '{}' Name '{}' and URL '{}'", id, applicationName, clientUrl);
        return id;

    }

    private static class ApplicationList extends ArrayList<Application> {
		private static final long serialVersionUID = 1L;
	}

}
