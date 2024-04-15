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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import de.codecentric.boot.admin.server.config.AdminServerProperties;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.POST;

@Profile("insecure")
@Configuration(proxyBeanMethods = false)
public class SecurityPermitAllConfig {

	private final AdminServerProperties adminServer;

	public SecurityPermitAllConfig(AdminServerProperties adminServer) {
		this.adminServer = adminServer;
	}

	@Bean
	protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http.authorizeHttpRequests((authorizeRequest) -> authorizeRequest.anyRequest().permitAll());

		http.addFilterAfter(new CustomCsrfFilter(), BasicAuthenticationFilter.class)
			.csrf((csrf) -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
				.csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
				.ignoringRequestMatchers(
						new AntPathRequestMatcher(this.adminServer.path("/instances"), POST.toString()),
						new AntPathRequestMatcher(this.adminServer.path("/notifications/**"), POST.toString()),
						new AntPathRequestMatcher(this.adminServer.path("/notifications/**"), DELETE.toString()),
						new AntPathRequestMatcher(this.adminServer.path("/instances/*"), DELETE.toString()),
						new AntPathRequestMatcher(this.adminServer.path("/actuator/**"))));

		return http.build();

	}

}
