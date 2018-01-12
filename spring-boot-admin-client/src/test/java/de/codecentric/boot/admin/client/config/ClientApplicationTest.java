package de.codecentric.boot.admin.client.config;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import de.codecentric.boot.admin.client.config.AdminProperties;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestClientApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT, properties = {
		"management.port=0", "spring.boot.admin.url=http://example.com" })
public class ClientApplicationTest {

	@Autowired
	private AdminProperties properties;

	@Test
	public void test_context() {
		assertThat(properties, notNullValue());
	}
}

@Configuration
@EnableAutoConfiguration
class TestClientApplication {
}