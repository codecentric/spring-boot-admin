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

package de.codecentric.boot.admin.sample.oauth2.config;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final Environment environment;
    private final AdminServerProperties adminServerProperties;
    private final OAuth2AuthenticationSuccessHandler authenticationSuccessHandler;

    public WebSecurityConfiguration(Environment environment,
                                    AdminServerProperties adminServerProperties,
                                    OAuth2AuthenticationSuccessHandler authenticationSuccessHandler) {
        this.environment = environment;
        this.adminServerProperties = adminServerProperties;
        this.authenticationSuccessHandler = authenticationSuccessHandler;
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        // @formatter:off
        httpSecurity
                .csrf()
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .ignoringRequestMatchers(
                        new AntPathRequestMatcher(
                                this.adminServerProperties.path("/instances"),
                                HttpMethod.POST.toString()),
                        new AntPathRequestMatcher(
                                this.adminServerProperties.path("/instances/*"),
                                HttpMethod.DELETE.toString()),
                        new AntPathRequestMatcher(
                                this.adminServerProperties.path("/actuator/**")))
                    .and()
                .authorizeRequests()
                    .antMatchers(adminServerProperties.path("/assets/**")).permitAll()
                    .antMatchers(adminServerProperties.path("/sw.js")).permitAll()
                    .antMatchers(adminServerProperties.path("/instances"))
                        .hasAnyAuthority("ROLE_client", "SCOPE_openid")
                    .anyRequest().hasAuthority("SCOPE_openid")
                    .and()
                .oauth2Login()
                    .successHandler(authenticationSuccessHandler)
                    .loginPage(adminServerProperties.path("/oauth2/authorization/" + getClientId()))
                        .and()
                    .logout()
                        .logoutSuccessUrl(getLogoutSuccessUrl())
                    .and()
                .oauth2Client()
                    .and()
                .httpBasic();
        // @formatter:on
    }

    @Bean
    @Override
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(
                User.withUsername(environment.getRequiredProperty("security.client.registration.username"))
                        .password(environment.getRequiredProperty("security.client.registration.password"))
                        .roles("client")
                        .build());
    }

    private String getClientId() {
        return environment.getRequiredProperty("spring.security.oauth2.client.registration.admin.client-id");
    }

    private String getLogoutSuccessUrl() {
        return environment.getRequiredProperty("security.logout.redirect-url");
    }

}
