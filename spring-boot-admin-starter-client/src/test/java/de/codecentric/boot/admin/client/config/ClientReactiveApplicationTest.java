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

import org.junit.After;
import org.junit.Before;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

public class ClientReactiveApplicationTest extends AbstractClientApplicationTest {
    private ConfigurableApplicationContext instance;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        instance = SpringApplication.run(TestClientApplication.class, "--spring.main.web-application-type=reactive",
                "--spring.application.name=Test-Client", "--server.port=0",
                "--management.endpoints.web.base-path=/mgmt", "--endpoints.health.enabled=true",
                "--spring.boot.admin.client.url=http://localhost:" + getWirmockPort());
    }

    @Override
    @After
    public void shutdown() {
        instance.close();
        super.shutdown();
    }

    @Override
    protected int getServerPort() {
        return instance.getEnvironment().getProperty("local.server.port", Integer.class, 0);
    }

    @Override
    protected int getManagementPort() {
        return instance.getEnvironment().getProperty("local.management.port", Integer.class, 0);

    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {WebMvcAutoConfiguration.class, ErrorMvcAutoConfiguration.class})
    public static class TestClientApplication {

    }

}

