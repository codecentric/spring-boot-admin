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
package de.codecentric.boot.admin.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Refresh the application context i.e. to reload changed properties files.
 */
@Controller
public class RefreshController {

	private static final Logger LOGGER = LoggerFactory.getLogger(RefreshController.class);

	@Autowired
	private ConfigurableApplicationContext context;

	@RequestMapping(value = "/refresh", method = RequestMethod.POST)
	@ResponseBody
	public String refresh() {
		// Doesn't work in spring-boot at the moment ... (v 1.1.0)
		LOGGER.warn("Refreshing application doesn't work at the moment");
		return "Refreshing application doesn't work at the moment";
		// context.refresh();
	}

}
