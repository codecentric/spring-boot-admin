/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package de.codecentric.boot.admin;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.EndpointRequest;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.StaticResourceRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import de.codecentric.boot.admin.config.EnableAdminServer;
import de.codecentric.boot.admin.notify.LoggingNotifier;
import de.codecentric.boot.admin.notify.Notifier;
import de.codecentric.boot.admin.notify.RemindingNotifier;
import de.codecentric.boot.admin.notify.filter.FilteringNotifier;

@Configuration
@EnableAutoConfiguration
@EnableAdminServer
public class SpringBootAdminApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SpringBootAdminApplication.class, args);
    }

    // tag::configuration-spring-security[]
    @Profile("secure")
    @Configuration
    public static class CredentialsConfig {
        @Value("${user.default.name}")
        private String username;
        
        @Value("${user.default.password}")
        private String password;
        
        @Bean
        public UserDetailsService userDetailsService() throws Exception {
            InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
            manager.createUser(
                    User.withDefaultPasswordEncoder()
                        .username(username).password(password).roles("USER", "ACTUATOR").build());
            return manager;
        }
    }
    
    @Profile("secure")
    @Configuration
    public static class SecurityConfig extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                // Page with login form is served as /login.html and does a POST on /login
                .formLogin()
                    .loginPage("/login.html")
                    .loginProcessingUrl("/login")
                    .permitAll()
                .and()
                // The UI does a POST on /logout on logout
                    .logout()
                        .logoutUrl("/logout")
                .and()
                // The UI currently doesn't support csrf
                    .csrf()
                        .disable()
                .authorizeRequests()
                    .requestMatchers(EndpointRequest.to("health")).permitAll()
                    .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ACTUATOR")
                // Requests for the login page and the static assets are allowed
                    .requestMatchers(StaticResourceRequest.toCommonLocations()).permitAll()
                    .antMatchers("/login.html", "/**/*.css", "/img/**", "/third-party/**").permitAll()
                // ... and any other request needs to be authorized
                    .antMatchers("/**").hasRole("USER")
                .and()
                // Enable so that the clients can authenticate via HTTP basic for registering
                    .httpBasic();
        }
    }
    // end::configuration-spring-security[]

    @Configuration
    public static class NotifierConfig {
        @Bean
        @Primary
        public RemindingNotifier remindingNotifier() {
            RemindingNotifier notifier = new RemindingNotifier(filteringNotifier(loggerNotifier()));
            notifier.setReminderPeriod(TimeUnit.SECONDS.toMillis(10));
            return notifier;
        }

        @Scheduled(fixedRate = 1_000L)
        public void remind() {
            remindingNotifier().sendReminders();
        }

        @Bean
        public FilteringNotifier filteringNotifier(Notifier delegate) {
            return new FilteringNotifier(delegate);
        }

        @Bean
        public LoggingNotifier loggerNotifier() {
            return new LoggingNotifier();
        }
    }
}
