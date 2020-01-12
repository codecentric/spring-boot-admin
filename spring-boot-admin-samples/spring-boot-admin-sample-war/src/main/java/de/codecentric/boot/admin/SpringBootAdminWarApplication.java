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

package de.codecentric.boot.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import de.codecentric.boot.admin.server.config.EnableAdminServer;

@Configuration(proxyBeanMethods = false)
@EnableAutoConfiguration
@EnableAdminServer
public class SpringBootAdminWarApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootAdminWarApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application;
	}

	@Profile("insecure")
	@Configuration(proxyBeanMethods = false)
	public static class SecurityPermitAllConfig extends WebSecurityConfigurerAdapter {

		private final AdminServerProperties adminServer;

		public SecurityPermitAllConfig(AdminServerProperties adminServer) {
			this.adminServer = adminServer;
		}

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.authorizeRequests((authorizeRequests) -> authorizeRequests.anyRequest().permitAll())
					.csrf((csrf) -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
							.ignoringRequestMatchers(
									new AntPathRequestMatcher(this.adminServer.path("/instances"),
											HttpMethod.POST.toString()),
									new AntPathRequestMatcher(this.adminServer.path("/instances/*"),
											HttpMethod.DELETE.toString()),
									new AntPathRequestMatcher(this.adminServer.path("/actuator/**"))));
		}

	}

	@Profile("secure")
	@Configuration(proxyBeanMethods = false)
	public static class SecuritySecureConfig extends WebSecurityConfigurerAdapter {

		private final AdminServerProperties adminServer;

		public SecuritySecureConfig(AdminServerProperties adminServer) {
			this.adminServer = adminServer;
		}

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
			successHandler.setTargetUrlParameter("redirectTo");
			successHandler.setDefaultTargetUrl(this.adminServer.path("/"));

			http.authorizeRequests((authorizeRequests) -> authorizeRequests
					.antMatchers(this.adminServer.path("/assets/**")).permitAll()
					.antMatchers(this.adminServer.path("/login")).permitAll().anyRequest().authenticated())

					.formLogin((formLogin) -> formLogin.loginPage(this.adminServer.path("/login"))
							.successHandler(successHandler))
					.logout((logout) -> logout.logoutUrl(this.adminServer.path("/logout")))
					.httpBasic(Customizer.withDefaults())
					.csrf((csrf) -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
							.ignoringRequestMatchers(
									new AntPathRequestMatcher(this.adminServer.path("/instances"),
											HttpMethod.POST.toString()),
									new AntPathRequestMatcher(this.adminServer.path("/instances/*"),
											HttpMethod.DELETE.toString()),
									new AntPathRequestMatcher(this.adminServer.path("/actuator/**"))));
		}

	}

}
