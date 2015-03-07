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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.util.FileCopyUtils;


public class LogfileMvcEndpointTest {

	private LogfileMvcEndpoint controller = new LogfileMvcEndpoint();

	@Test
	public void logfile_noProperty() throws IOException {
		assertEquals(HttpStatus.NOT_FOUND, controller.available().getStatusCode());

		MockHttpServletResponse response = new MockHttpServletResponse();
		controller.invoke(response);
		assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
	}

	@Test
	public void logfile_noFile() throws IOException {
		controller.setLogfile("does_not_exist.log");

		assertEquals(HttpStatus.NOT_FOUND, controller.available().getStatusCode());

		MockHttpServletResponse response = new MockHttpServletResponse();
		controller.invoke(response);
		assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
	}

	@Test
	public void logfile() throws IOException {
		try {
			FileCopyUtils.copy("--TEST--".getBytes(), new File("test.log"));
			controller.setLogfile("test.log");

			assertEquals(HttpStatus.OK, controller.available().getStatusCode());

			MockHttpServletResponse response = new MockHttpServletResponse();
			controller.invoke(response);
			assertEquals(HttpStatus.OK.value(), response.getStatus());
			assertEquals("--TEST--", response.getContentAsString());
		}
		finally {
			new File("test.log").delete();
		}
	}

}
