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

import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final Environment environment;

    public WebSecurityConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        // @formatter:off
        httpSecurity
                .authorizeRequests()
                    .antMatchers("/actuator/health", "/actuator/info")
                        .hasAnyAuthority("ROLE_sba_server", "SCOPE_openid")
                    .antMatchers("/actuator/**").hasAuthority("SCOPE_openid")
                    .anyRequest().hasAuthority("ROLE_user")
                    .and()
                .httpBasic()
                    .and()
                .oauth2ResourceServer()
                    .jwt();
        // @formatter:off
    }

    @Bean
    @Override
    protected UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(
                User.withUsername(environment.getRequiredProperty("security.healthcheck.username"))
                        .password(environment.getRequiredProperty("security.healthcheck.password"))
                        .roles("sba_server")
                        .build(),
                User.withUsername("admin")
                        .password("{noop}password")
                        .roles("user")
                        .build());
    }

}
