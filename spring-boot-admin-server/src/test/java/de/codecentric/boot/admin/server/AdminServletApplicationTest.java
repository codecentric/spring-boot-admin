/*
 * Copyright 2014-2018 the original author or authors.
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

package de.codecentric.boot.admin.server;

import de.codecentric.boot.admin.server.config.EnableAdminServer;

import org.junit.After;
import org.junit.Before;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

public class AdminServletApplicationTest extends AbstractAdminApplicationTest {
    private ConfigurableApplicationContext instance;

    @Before
    public void setUp() {
        instance = new SpringApplicationBuilder().sources(TestAdminApplication.class)
                                                 .web(WebApplicationType.SERVLET)
                                                 .run("--server.port=0", "--management.endpoints.web.base-path=/mgmt",
                                                     "--info.test=foobar", "--eureka.client.enabled=false");

        super.setUp(instance.getEnvironment().getProperty("local.server.port", Integer.class, 0));
    }

    @After
    public void shutdown() {
        instance.close();
    }

    @EnableAdminServer
    @EnableAutoConfiguration
    @SpringBootConfiguration
    public static class TestAdminApplication {
        @Configuration
        public static class SecurityConfiguration extends WebSecurityConfigurerAdapter {
            @Override
            protected void configure(HttpSecurity http) throws Exception {
                http.authorizeRequests().anyRequest().permitAll()//
                    .and().csrf().disable();
            }
        }
    }
}
