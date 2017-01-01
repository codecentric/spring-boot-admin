package de.codecentric.boot.admin.client.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestClientApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT, properties = {
        "management.port=0", "spring.boot.admin.client.url=http://example.com"})
public class ClientApplicationTest {

    @Autowired
    private ClientProperties properties;

	@Test
	public void test_context() {
		assertThat(properties).isNotNull();
	}
}

@Configuration
@EnableAutoConfiguration
class TestClientApplication {
}
