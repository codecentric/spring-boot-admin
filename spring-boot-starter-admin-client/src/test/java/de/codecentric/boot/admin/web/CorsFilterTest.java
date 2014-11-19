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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import de.codecentric.boot.admin.web.CorsFilterTest.TestAdminApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestAdminApplication.class)
@WebAppConfiguration
@IntegrationTest({ "server.port=0", "management.port=0", "spring.boot.admin.url=http://localhost:65000" })
public class CorsFilterTest {

	RestTemplate restTemplate = new TestRestTemplate();

	@Value("${local.server.port}")
	private int serverPort = 0;

	@Value("${local.management.port}")
	private int managementPort = 0;

	@Test
	@SuppressWarnings("rawtypes")
	public void testCORS_endpoint() {
		// DO serve CORS-Headers on management-endpoints
		ResponseEntity<Map> info = new TestRestTemplate().getForEntity("http://localhost:" + managementPort + "/info",
				Map.class);
		assertEquals(Arrays.asList("*"), info.getHeaders().get("Access-Control-Allow-Origin"));
		assertEquals(Arrays.asList("Origin, X-Requested-With, Content-Type, Accept"),
				info.getHeaders().get("Access-Control-Allow-Headers"));
	}

	@Test
	public void testCORS_application() {
		// DO NOT serve CORS-Headers on application-endpoints
		ResponseEntity<String> hello = new TestRestTemplate().getForEntity("http://localhost:" + serverPort + "/hello",
				String.class);
		assertEquals(null, hello.getHeaders().get("Access-Control-Allow-Origin"));
		assertEquals(null, hello.getHeaders().get("Access-Control-Allow-Headers"));
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