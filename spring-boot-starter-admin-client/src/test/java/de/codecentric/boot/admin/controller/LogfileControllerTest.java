package de.codecentric.boot.admin.controller;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.util.FileCopyUtils;

public class LogfileControllerTest {

	private LogfileController controller = new LogfileController();

	@Test
	public void logfile_noProperty() {
		assertEquals(HttpStatus.NOT_FOUND, controller.hasLogfile().getStatusCode());

		MockHttpServletResponse response = new MockHttpServletResponse();
		controller.getLogfile(response);
		assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
	}

	@Test
	public void logfile_noFile() {
		controller.setLogfile("does_not_exist.log");

		assertEquals(HttpStatus.NOT_FOUND, controller.hasLogfile().getStatusCode());

		MockHttpServletResponse response = new MockHttpServletResponse();
		controller.getLogfile(response);
		assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
	}

	@Test
	public void logfile() throws IOException {
		try {
			FileCopyUtils.copy("--TEST--".getBytes(), new File("test.log"));
			controller.setLogfile("test.log");

			assertEquals(HttpStatus.OK, controller.hasLogfile().getStatusCode());

			MockHttpServletResponse response = new MockHttpServletResponse();
			controller.getLogfile(response);
			assertEquals(HttpStatus.OK.value(), response.getStatus());
			assertEquals("--TEST--", response.getContentAsString());
		} finally {
			new File("test.log").delete();
		}
	}

}
