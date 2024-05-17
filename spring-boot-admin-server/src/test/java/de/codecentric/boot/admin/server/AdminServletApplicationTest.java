/*
 * Copyright 2014-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import de.codecentric.boot.admin.server.config.EnableAdminServer;

public class AdminServletApplicationTest extends AbstractAdminApplicationTest {

	private ConfigurableApplicationContext instance;

	@BeforeEach
	public void setUp() {
		this.instance = new SpringApplicationBuilder().sources(TestAdminApplication.class)
			.web(WebApplicationType.SERVLET)
			.run("--server.port=0", "--management.endpoints.web.base-path=/mgmt",
					"--management.endpoints.web.exposure.include=info,health", "--info.test=foobar");

		super.setUp(this.instance.getEnvironment().getProperty("local.server.port", Integer.class, 0));
	}

	@AfterEach
	public void shutdown() {
		this.instance.close();
	}

	@EnableAdminServer
	@EnableAutoConfiguration
	@SpringBootConfiguration
	public static class TestAdminApplication {

		@Configuration(proxyBeanMethods = false)
		public static class SecurityConfiguration {

			@Bean
			public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
				http.csrf(AbstractHttpConfigurer::disable)
					.authorizeHttpRequests((authz) -> authz.anyRequest().permitAll());
				return http.build();
			}

		}

	}

}
