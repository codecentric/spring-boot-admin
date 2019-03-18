/*
 * Copyright 2014-2019 the original author or authors.
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

import de.codecentric.boot.admin.server.config.AdminServerProperties;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Profile("secure")
// tag::configuration-spring-security[]
@Configuration
public class SecuritySecureConfig extends WebSecurityConfigurerAdapter {
    private final AdminServerProperties adminServer;

    public SecuritySecureConfig(AdminServerProperties adminServer) {
        this.adminServer = adminServer;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter("redirectTo");
        successHandler.setDefaultTargetUrl(this.adminServer.path("/"));

        http.authorizeRequests()
            .antMatchers(this.adminServer.path("/assets/**")).permitAll() // <1>
            .antMatchers(this.adminServer.path("/login")).permitAll()
            .anyRequest().authenticated() // <2>
            .and()
        .formLogin().loginPage(this.adminServer.path("/login")).successHandler(successHandler).and() // <3>
        .logout().logoutUrl(this.adminServer.path("/logout")).and()
        .httpBasic().and() // <4>
        .csrf()
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()) // <5>
            .ignoringAntMatchers(
                this.adminServer.path("/instances"), // <6>
                this.adminServer.path("/actuator/**") // <7>
            );
        // @formatter:on
    }
}
// end::configuration-spring-security[]
