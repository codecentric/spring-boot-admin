package de.codecentric.boot.admin.client.config;

import de.codecentric.boot.admin.client.registration.ApplicationRegistrator;

import org.junit.After;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

import static org.junit.Assert.assertTrue;

public class SpringBootAdminClientAutoConfigurationTest {

    private ConfigurableApplicationContext context;

    @After
    public void close() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    public void not_active() {
        load();
        assertTrue(context.getBeansOfType(ApplicationRegistrator.class).isEmpty());
    }

    @Test
    public void active() {
        load("spring.boot.admin.url:http://localhost:8081");
        context.getBean(ApplicationRegistrator.class);
    }

    @Test
    public void disabled() {
        load("spring.boot.admin.url:http://localhost:8081", "spring.boot.admin.client.enabled:false");
        assertTrue(context.getBeansOfType(ApplicationRegistrator.class).isEmpty());
    }

    @Test
    public void nonWebEnvironment() {
        load("spring.main.admin.url:http://localhost:8081", "spring.boot.admin.client.enabled:true",
            "spring.main.web-environment:false"
            );
        assertTrue(context.getBeansOfType(ApplicationRegistrator.class).isEmpty());
    }

    private void load(final String... environment) {
        SpringApplication springApplication = new SpringApplication(TestClientApplication.class);
        springApplication.addInitializers(new ApplicationContextInitializer<ConfigurableApplicationContext>() {
            @Override
            public void initialize(ConfigurableApplicationContext applicationContext) {
                EnvironmentTestUtils.addEnvironment(applicationContext, environment);
            }
        });
        this.context = springApplication.run("--server.port=0");
    }


    @Configuration
    @EnableAutoConfiguration
    static class TestClientApplication {
    }

}
