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
package de.codecentric.boot.admin.web;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import de.codecentric.boot.admin.web.CorsFilterOnSamePortsTest.TestAdminApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestAdminApplication.class)
@WebAppConfiguration
@IntegrationTest({ "server.port=0", "spring.boot.admin.url=http://localhost:65000" })
public class CorsFilterOnSamePortsTest {

	RestTemplate restTemplate = new TestRestTemplate();

	@Value("${local.server.port}")
	private int serverPort = 0;

	@Test
	@SuppressWarnings("rawtypes")
	public void testCORS_GET_info_endpoint() {
		// DO serve CORS-Headers on management-endpoints
		ResponseEntity<Map> info = new TestRestTemplate().getForEntity("http://localhost:" + serverPort + "/info",
				Map.class);
		assertEquals(HttpStatus.OK, info.getStatusCode());
		assertEquals(Arrays.asList("*"), info.getHeaders().get("Access-Control-Allow-Origin"));
		assertEquals(Arrays.asList("Origin, X-Requested-With, Content-Type, Accept"),
				info.getHeaders().get("Access-Control-Allow-Headers"));
	}

	@Test
	public void testCORS_OPTIONS_jolokia_endpoint() {
		// DO serve CORS-Headers on management-endpoints
		ResponseEntity<Void> options = new TestRestTemplate().exchange("http://localhost:" + serverPort
				+ "/jolokia",
				HttpMethod.OPTIONS, HttpEntity.EMPTY, Void.class);

		assertEquals(HttpStatus.OK, options.getStatusCode());
		assertEquals(Arrays.asList("*"), options.getHeaders().get("Access-Control-Allow-Origin"));
		assertEquals(Arrays.asList("Origin, X-Requested-With, Content-Type, Accept"),
				options.getHeaders().get("Access-Control-Allow-Headers"));
	}

	@Test
	public void testCORS_GET_application() {
		// DO NOT serve CORS-Headers on application-endpoints
		ResponseEntity<String> hello = new TestRestTemplate().getForEntity("http://localhost:" + serverPort + "/hello",
				String.class);
		assertEquals(HttpStatus.OK, hello.getStatusCode());
		assertEquals(null, hello.getHeaders().get("Access-Control-Allow-Origin"));
		assertEquals(null, hello.getHeaders().get("Access-Control-Allow-Headers"));
	}

	@Test
	public void testCORS_OPTIONS_application() {
		// DO NOT serve CORS-Headers on application-endpoints
		ResponseEntity<Void> options = new TestRestTemplate().exchange("http://localhost:" + serverPort + "/hello",
				HttpMethod.OPTIONS, HttpEntity.EMPTY, Void.class);
		assertEquals(HttpStatus.OK, options.getStatusCode());
		assertEquals(null, options.getHeaders().get("Access-Control-Allow-Origin"));
		assertEquals(null, options.getHeaders().get("Access-Control-Allow-Headers"));
	}

	@Configuration
	@EnableAutoConfiguration
	@RestController
	public static class TestAdminApplication {
		@RequestMapping("/hello")
		public String hello() {
			return "hello world!";
		}
	}
}