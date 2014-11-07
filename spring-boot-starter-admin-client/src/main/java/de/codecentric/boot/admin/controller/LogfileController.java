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

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller that provides an API for logfiles, i.e. downloading the main logfile configured in environment property
 * 'logging.file' that is standard, but optional property for spring-boot applications.
 */
@Controller
public class LogfileController {

	private static final Logger LOGGER = LoggerFactory.getLogger(LogfileController.class);

	@Autowired
	private Environment env;

	@RequestMapping("/logfile")
	@ResponseBody
	public String getLogfile(HttpServletResponse response) {
		String path = env.getProperty("logging.file");
		if (path == null) {
			LOGGER.error("Logfile download failed for missing property 'logging.file'");
			return "Logfile download failed for missing property 'logging.file'";
		}
		Resource file = new FileSystemResource(path);
		if (!file.exists()) {
			LOGGER.error("Logfile download failed for missing file at path=" + path);
			return "Logfile download failed for missing file at path=" + path;
		}
		response.setContentType(MediaType.TEXT_PLAIN_VALUE);
		response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getFilename() + "\"");

		try {
			FileCopyUtils.copy(file.getInputStream(), response.getOutputStream());
		} catch (IOException e) {
			LOGGER.error("Logfile download failed for path=" + path);
			return "Logfile download failed for path=" + path;
		}
		return null;
	}

}
