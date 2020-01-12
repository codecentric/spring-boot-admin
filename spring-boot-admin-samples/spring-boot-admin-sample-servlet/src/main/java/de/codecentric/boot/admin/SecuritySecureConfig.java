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

import java.util.UUID;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import de.codecentric.boot.admin.server.config.AdminServerProperties;

@Profile("secure")
// tag::configuration-spring-security[]
@Configuration(proxyBeanMethods = false)
public class SecuritySecureConfig extends WebSecurityConfigurerAdapter {

	private final AdminServerProperties adminServer;

	public SecuritySecureConfig(AdminServerProperties adminServer) {
		this.adminServer = adminServer;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
		successHandler.setTargetUrlParameter("redirectTo");
		successHandler.setDefaultTargetUrl(this.adminServer.path("/"));

		http.authorizeRequests(
				(authorizeRequests) -> authorizeRequests.antMatchers(this.adminServer.path("/assets/**")).permitAll() // <1>
						.antMatchers(this.adminServer.path("/login")).permitAll().anyRequest().authenticated() // <2>
		).formLogin(
				(formLogin) -> formLogin.loginPage(this.adminServer.path("/login")).successHandler(successHandler).and() // <3>
		).logout((logout) -> logout.logoutUrl(this.adminServer.path("/logout"))).httpBasic(Customizer.withDefaults()) // <4>
				.csrf((csrf) -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()) // <5>
						.ignoringRequestMatchers(
								new AntPathRequestMatcher(this.adminServer.path("/instances"),
										HttpMethod.POST.toString()), // <6>
								new AntPathRequestMatcher(this.adminServer.path("/instances/*"),
										HttpMethod.DELETE.toString()), // <6>
								new AntPathRequestMatcher(this.adminServer.path("/actuator/**")) // <7>
						))
				.rememberMe((rememberMe) -> rememberMe.key(UUID.randomUUID().toString()).tokenValiditySeconds(1209600));
	}

	// Required to provide UserDetailsService for "remember functionality"
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.inMemoryAuthentication().withUser("user").password("{noop}password").roles("USER");
	}

}
// end::configuration-spring-security[]
