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
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static org.springframework.security.oauth2.client.OAuth2AuthorizationContext.PASSWORD_ATTRIBUTE_NAME;
import static org.springframework.security.oauth2.client.OAuth2AuthorizationContext.USERNAME_ATTRIBUTE_NAME;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final Environment environment;
    private final AdminServerProperties adminServerProperties;

    public WebSecurityConfiguration(Environment environment,
                                    AdminServerProperties adminServerProperties) {
        this.environment = environment;
        this.adminServerProperties = adminServerProperties;
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
                                this.adminServerProperties.path("/actuator/**"))
                    ).and()
                .authorizeRequests()
                    .antMatchers(adminServerProperties.path("/assets/**")).permitAll()
                    .antMatchers(adminServerProperties.path("/login")).permitAll()
                .antMatchers(adminServerProperties.path("/sw.js")).permitAll()
                    .anyRequest().authenticated()
                    .and()
                .formLogin()
                    .loginPage(adminServerProperties.path("/login"))
                    .and()
                .logout()
                    .logoutUrl(adminServerProperties.path("/logout"))
                    .and()
                .httpBasic();
        // @formatter:on
    }

    @Bean
    public OAuth2AuthorizedClient authorizedClient(AuthorizedClientServiceOAuth2AuthorizedClientManager clientManager) {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId("my-client")
                .principal(new UsernamePasswordAuthenticationToken("subject", "password"))
                .attribute(USERNAME_ATTRIBUTE_NAME, "subject")
                .attribute(PASSWORD_ATTRIBUTE_NAME, "password")
                .build();
        return clientManager.authorize(authorizeRequest);
    }

    @Bean
    public AuthorizedClientServiceOAuth2AuthorizedClientManager clientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {

        AuthorizedClientServiceOAuth2AuthorizedClientManager manager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository,
                        authorizedClientService);

        manager.setAuthorizedClientProvider(
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .password()
                        .build());

        manager.setContextAttributesMapper(new PasswordGrantTypeContextAttributesMapper());

        return manager;
    }

    @Bean
    @Override
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(User
                        .withUsername(environment.getProperty("spring.security.user.name"))
                        .password("{noop}" + environment.getProperty("spring.security.user.password"))
                        .roles("USER")
                        .build());
    }

}
