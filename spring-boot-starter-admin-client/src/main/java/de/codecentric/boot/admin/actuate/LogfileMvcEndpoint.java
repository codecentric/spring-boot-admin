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
package de.codecentric.boot.admin.actuate;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.actuate.endpoint.mvc.MvcEndpoint;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller that provides an API for logfiles, i.e. downloading the main logfile configured in environment property
 * 'logging.file' that is standard, but optional property for spring-boot applications.
 */
@ConfigurationProperties(prefix = "endpoints.logfile")
public class LogfileMvcEndpoint implements MvcEndpoint {

	private static final Logger LOGGER = LoggerFactory.getLogger(LogfileMvcEndpoint.class);

	@Value("${logging.file}")
	private String logfile;

	private String path = "/logfile";

	private boolean sensitive = true;

	private boolean enabled = true;

	@Override
	public boolean isSensitive() {
		return sensitive;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Class<? extends Endpoint> getEndpointType() {
		return null;
	}


	public void setLogfile(String logfile) {
		this.logfile = logfile;
	}
	public String getLogfile() {
		return logfile;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setSensitive(boolean sensitive) {
		this.sensitive = sensitive;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

	@RequestMapping(method = RequestMethod.GET)
	public void invoke(HttpServletResponse response) throws IOException {
		if (!isAvailable()) {
			response.setStatus(HttpStatus.NOT_FOUND.value());
			return;
		}

		Resource file = new FileSystemResource(logfile);
		response.setContentType(MediaType.TEXT_PLAIN_VALUE);
		FileCopyUtils.copy(file.getInputStream(), response.getOutputStream());
	}


	@RequestMapping(method = RequestMethod.HEAD)
	@ResponseBody
	public ResponseEntity<Void> available() {
		if (isAvailable()) {
			return new ResponseEntity<Void>(HttpStatus.OK);
		}
		else {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
	}

	private boolean isAvailable() {
		if (!enabled) {
			return false;
		}

		if (logfile == null) {
			LOGGER.error("Logfile download failed for missing property 'logging.file'");
			return false;
		}

		Resource file = new FileSystemResource(logfile);
		if (!file.exists()) {
			LOGGER.error("Logfile download failed for missing file at path={}", logfile);
			return false;
		}

		return true;
	}


}
