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

package de.codecentric.boot.admin.sample;

import java.net.URI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import de.codecentric.boot.admin.server.config.EnableAdminServer;
import de.codecentric.boot.admin.server.notify.Notifier;

@SpringBootApplication
@EnableAdminServer
public class SpringBootAdminReactiveApplication {

	private final AdminServerProperties adminServer;

	public SpringBootAdminReactiveApplication(AdminServerProperties adminServer) {
		this.adminServer = adminServer;
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringBootAdminReactiveApplication.class, args);
	}

	@Bean
	@Profile("insecure")
	public SecurityWebFilterChain securityWebFilterChainPermitAll(ServerHttpSecurity http) {
		return http.authorizeExchange((authorizeExchange) -> authorizeExchange.anyExchange().permitAll())
			.csrf(ServerHttpSecurity.CsrfSpec::disable)
			.build();
	}

	@Bean
	@Profile("secure")
	public SecurityWebFilterChain securityWebFilterChainSecure(ServerHttpSecurity http) {
		return http
			.authorizeExchange(
					(authorizeExchange) -> authorizeExchange.pathMatchers(this.adminServer.path("/assets/**"))
						.permitAll()
						.pathMatchers("/actuator/health/**")
						.permitAll()
						.pathMatchers(this.adminServer.path("/login"))
						.permitAll()
						.anyExchange()
						.authenticated())
			.formLogin((formLogin) -> formLogin.loginPage(this.adminServer.path("/login"))
				.authenticationSuccessHandler(loginSuccessHandler(this.adminServer.path("/"))))
			.logout((logout) -> logout.logoutUrl(this.adminServer.path("/logout"))
				.logoutSuccessHandler(logoutSuccessHandler(this.adminServer.path("/login?logout"))))
			.httpBasic(Customizer.withDefaults())
			.csrf(ServerHttpSecurity.CsrfSpec::disable)
			.build();
	}

	// The following two methods are only required when setting a custom base-path (see
	// 'basepath' profile in application.yml)
	private ServerLogoutSuccessHandler logoutSuccessHandler(String uri) {
		RedirectServerLogoutSuccessHandler successHandler = new RedirectServerLogoutSuccessHandler();
		successHandler.setLogoutSuccessUrl(URI.create(uri));
		return successHandler;
	}

	private ServerAuthenticationSuccessHandler loginSuccessHandler(String uri) {
		RedirectServerAuthenticationSuccessHandler successHandler = new RedirectServerAuthenticationSuccessHandler();
		successHandler.setLocation(URI.create(uri));
		return successHandler;
	}

	@Bean
	public Notifier notifier() {
		return (e) -> Mono.empty();
	}

}
