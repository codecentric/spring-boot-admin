/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.client.config;

import de.codecentric.boot.admin.client.registration.ApplicationRegistrator;

import org.junit.After;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(context.getBeansOfType(ApplicationRegistrator.class)).isEmpty();
    }

    @Test
    public void active() {
        load("spring.boot.admin.client.url:http://localhost:8081");
        context.getBean(ApplicationRegistrator.class);
    }

    @Test
    public void disabled() {
        load("spring.boot.admin.client.url:http://localhost:8081", "spring.boot.admin.client.enabled:false");
        assertThat(context.getBeansOfType(ApplicationRegistrator.class)).isEmpty();
    }

    @Test
    public void nonWebEnvironment() {
        load("spring.main.admin.url:http://localhost:8081", "spring.boot.admin.client.enabled:true",
                "spring.main.web-application-type:none");
        assertThat(context.getBeansOfType(ApplicationRegistrator.class).isEmpty());
    }

    private void load(final String... environment) {
        SpringApplication springApplication = new SpringApplication(TestClientApplication.class);
        springApplication.addInitializers(
                applicationContext -> TestPropertyValues.of(environment).applyTo(applicationContext));
        this.context = springApplication.run("--server.port=0");
    }

    @Configuration
    @EnableAutoConfiguration
    static class TestClientApplication {
    }
}
