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

import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import de.codecentric.boot.admin.model.Application;

/**
 * Scheduler that checks the registration of the application at the spring-boot-admin.
 */
public class SpringBootAdminRegistratorTask implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpringBootAdminRegistratorTask.class);

	@Autowired
	private Environment env;

	@PostConstruct
	public void check() {
		Assert.notNull(env.getProperty("spring.boot.admin.url"),
				"The URL of the spring-boot-admin application is mandatory");
		Assert.notNull(env.getProperty("server.port"), "The server port of the application is mandatory");
		Assert.notNull(env.getProperty("spring.application.name"), "The id of the application is mandatory");
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			String id = env.getProperty("info.id");
			int port = env.getProperty("server.port", Integer.class);
			String adminUrl = env.getProperty("spring.boot.admin.url");
			RestTemplate template = new RestTemplate();
			template.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
			ApplicationList list = template.getForObject(adminUrl + "/api/applications", ApplicationList.class);
			for (Application app : list) {
				if (id.equals(app.getId())) {
					// the application is already registered at the admin tool
					LOGGER.debug("Application already registered with ID '{}'", id);
					return;
				}
			}
			// register the application with the used URL and port
			String url = new URL("http", InetAddress.getLocalHost().getCanonicalHostName(), port, "").toString();
			Application app = new Application();
			app.setId(id);
			app.setUrl(url);
			template.postForObject(adminUrl + "/api/applications", app, String.class);
			LOGGER.info("Application registered itself at the admin application with ID '{}' and URL '{}'", id, url);
		} catch (Exception e) {
			LOGGER.warn("Failed to register application at spring-boot-admin, message={}", e.getMessage());
		}
	}

	private static class ApplicationList extends ArrayList<Application> {
		private static final long serialVersionUID = 1L;
	}

}
