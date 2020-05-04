/*
 * Copyright 2014-2020 the original author or authors.
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

package de.codecentric.boot.admin.server.ui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import de.codecentric.boot.admin.server.config.EnableAdminServer;

public class AdminUiReactiveApplicationTest extends AbstractAdminUiApplicationTest {

	private ConfigurableApplicationContext instance;

	@BeforeEach
	public void setUp() {
		this.instance = new SpringApplicationBuilder().sources(TestAdminApplication.class)
				.web(WebApplicationType.REACTIVE).run("--server.port=0",
						"--spring.boot.admin.ui.extension-resource-locations=classpath:/META-INF/test-extensions/",
						"--spring.boot.admin.ui.available-languages=de");

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

		@Bean
		public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
			return http.authorizeExchange().anyExchange().permitAll().and().csrf().disable().build();
		}

	}

}
