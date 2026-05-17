/*
 * Copyright 2014-2026 the original author or authors.
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

package de.codecentric.boot.admin.sample.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import de.codecentric.boot.admin.server.config.EnableAdminServer;

/**
 * Spring Boot Admin sample application combining the Web UI and MCP server.
 *
 * <p>
 * Start with the {@code insecure} profile for local development (default):
 * </p>
 *
 * <pre>
 * ./mvnw spring-boot:run -pl spring-boot-admin-samples/spring-boot-admin-sample-mcp
 * </pre>
 *
 * <p>
 * The MCP server is available at {@code http://localhost:8080/mcp} and the Web UI at
 * {@code http://localhost:8080}.
 * </p>
 *
 * <p>
 * To connect Claude Desktop, add the following to your
 * {@code claude_desktop_config.json}:
 * </p>
 *
 * <pre>
 * {
 *   "mcpServers": {
 *     "spring-boot-admin": {
 *       "url": "http://localhost:8080/mcp"
 *     }
 *   }
 * }
 * </pre>
 */
@SpringBootApplication
@EnableAdminServer
public class SpringBootAdminMcpApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootAdminMcpApplication.class, args);
	}

	@Bean
	@Profile("insecure")
	public SecurityWebFilterChain securityWebFilterChainPermitAll(ServerHttpSecurity http) {
		return http.authorizeExchange((authorizeExchange) -> authorizeExchange.anyExchange().permitAll())
			.csrf(ServerHttpSecurity.CsrfSpec::disable)
			.build();
	}

}
