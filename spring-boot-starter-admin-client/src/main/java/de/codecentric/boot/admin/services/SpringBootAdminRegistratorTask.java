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
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
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
		try {
            String applicationName = env.getProperty("spring.application.name");
            int port = env.getProperty("server.port", Integer.class);
            URL adminUrl = new URL(env.getProperty("spring.boot.admin.url"));
            String managementPath = env.getProperty("management.contextPath", "");
            URL clientUrl = new URL("http", InetAddress.getLocalHost().getCanonicalHostName(), port, managementPath);
            Application application = new Application.ApplicationBuilder(applicationName, clientUrl).build();

			RestTemplate template = new RestTemplate();
			template.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

			ApplicationList list = template.getForObject(adminUrl + "/api/applications", ApplicationList.class);
            Long id = applicationId.get();
			for (Application app : list) {
				if (id.equals(app.getId())) {
					// the application is already registered at the admin tool
					LOGGER.debug("Application already registered with ID '{}'", id);
					return;
				}
			}
			// register the application with the used URL and port
			template.postForObject(adminUrl + "/api/applications", application, String.class);
			LOGGER.info("Application registered itself at the admin application with NAME '{}' and URL '{}'", applicationName, clientUrl);
		}
		catch (Exception e) {
			LOGGER.warn("Failed to register application at spring-boot-admin, message={}", e.getMessage());
		}
	}

	private static class ApplicationList extends ArrayList<Application> {
		private static final long serialVersionUID = 1L;
	}

}
