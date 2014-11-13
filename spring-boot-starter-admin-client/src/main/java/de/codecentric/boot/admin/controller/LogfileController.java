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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller that provides an API for logfiles, i.e. downloading the main logfile configured in environment property
 * 'logging.file' that is standard, but optional property for spring-boot applications.
 */
@RestController
public class LogfileController {

	private static final Logger LOGGER = LoggerFactory.getLogger(LogfileController.class);

	@Value("${logging.file}")
	private String logfile;

	@RequestMapping(value = "/logfile", method = { RequestMethod.GET })
	public String getLogfile(HttpServletResponse response) {
		if (logfile == null) {
			LOGGER.error("Logfile download failed for missing property 'logging.file'");
			response.setStatus(HttpStatus.NOT_FOUND.value());
			return "Logfile download failed for missing property 'logging.file'";
		}

		Resource file = new FileSystemResource(logfile);
		if (!file.exists()) {
			LOGGER.error("Logfile download failed for missing file at path={}", logfile);
			response.setStatus(HttpStatus.NOT_FOUND.value());
			return "Logfile download failed for missing file at path=" + logfile;
		}
		response.setContentType(MediaType.TEXT_PLAIN_VALUE);
		response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getFilename() + "\"");

		try {
			FileCopyUtils.copy(file.getInputStream(), response.getOutputStream());
		} catch (IOException ex) {
			LOGGER.error("Logfile download failed for path={}. Reasond: {}", logfile, ex.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return "Logfile download failed for path=" + logfile;
		}
		return null;
	}

	@RequestMapping(value = "/logfile", method = { RequestMethod.HEAD })
	public ResponseEntity<?> hasLogfile() {
		if (logfile == null) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}

		Resource file = new FileSystemResource(logfile);
		if (!file.exists()) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	public void setLogfile(String logfile) {
		this.logfile = logfile;
	}
}
