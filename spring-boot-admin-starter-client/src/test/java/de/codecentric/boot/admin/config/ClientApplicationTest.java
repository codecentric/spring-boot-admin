package de.codecentric.boot.admin.config;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestClientApplication.class)
@WebIntegrationTest(randomPort = true, value = { "management.port=0",
		"spring.boot.admin.url=http://example.com" })
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