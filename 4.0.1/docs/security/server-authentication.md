# Server Authentication

Secure your Spring Boot Admin Server using Spring Security to protect the UI and API endpoints.

## Overview[​](#overview "Direct link to Overview")

A secured Admin Server requires:

1. **Spring Security dependency**
2. **SecurityFilterChain configuration**
3. **User credentials** (in-memory, database, LDAP, OAuth2, etc.)
4. **CSRF protection** with exemptions for client registration

***

## Quick Start[​](#quick-start "Direct link to Quick Start")

### 1. Add Spring Security Dependency[​](#1-add-spring-security-dependency "Direct link to 1. Add Spring Security Dependency")

**Maven**:

```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

**Gradle**:

```
implementation 'org.springframework.boot:spring-boot-starter-security'
```

### 2. Basic Configuration[​](#2-basic-configuration "Direct link to 2. Basic Configuration")

**Minimal security** with default Spring Boot user:

```
spring:
  security:
    user:
      name: admin
      password: ${ADMIN_PASSWORD}
```

This provides:

* Form login at `/login`
* HTTP Basic authentication for API
* Single user with username `admin`

### 3. Access the UI[​](#3-access-the-ui "Direct link to 3. Access the UI")

Navigate to `http://localhost:8080`, and you'll be redirected to the login page.

***

## Complete Security Configuration[​](#complete-security-configuration "Direct link to Complete Security Configuration")

For more control, use a custom `SecurityFilterChain`:

```
package com.example.admin;

import java.util.UUID;

import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import de.codecentric.boot.admin.server.config.AdminServerProperties;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.POST;

@Configuration
public class SecurityConfig {

    private final AdminServerProperties adminServer;

    public SecurityConfig(AdminServerProperties adminServer) {
        this.adminServer = adminServer;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Redirect to login after successful authentication
        SavedRequestAwareAuthenticationSuccessHandler successHandler =
            new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter("redirectTo");
        successHandler.setDefaultTargetUrl(adminServer.path("/"));

        http
            .authorizeHttpRequests(auth -> auth
                // Permit access to static resources
                .requestMatchers(PathPatternRequestMatcher.withDefaults()
                    .matcher(adminServer.path("/assets/**")))
                .permitAll()
                // Permit access to login page
                .requestMatchers(PathPatternRequestMatcher.withDefaults()
                    .matcher(adminServer.path("/login")))
                .permitAll()
                // Permit Admin Server's own actuator endpoints
                .requestMatchers(PathPatternRequestMatcher.withDefaults()
                    .matcher(adminServer.path("/actuator/info")))
                .permitAll()
                .requestMatchers(PathPatternRequestMatcher.withDefaults()
                    .matcher(adminServer.path("/actuator/health")))
                .permitAll()
                // Require authentication for all other requests
                .anyRequest().authenticated()
            )
            // Form login for UI
            .formLogin(formLogin -> formLogin
                .loginPage(adminServer.path("/login"))
                .successHandler(successHandler)
            )
            // Logout configuration
            .logout(logout -> logout
                .logoutUrl(adminServer.path("/logout"))
            )
            // HTTP Basic for API clients
            .httpBasic(Customizer.withDefaults());

        // CSRF configuration (see CSRF Protection section)
        http.addFilterAfter(new CustomCsrfFilter(), BasicAuthenticationFilter.class)
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                .ignoringRequestMatchers(
                    // Exempt client registration endpoints
                    PathPatternRequestMatcher.withDefaults()
                        .matcher(POST, adminServer.path("/instances")),
                    PathPatternRequestMatcher.withDefaults()
                        .matcher(DELETE, adminServer.path("/instances/*")),
                    // Exempt Admin Server's own actuator
                    PathPatternRequestMatcher.withDefaults()
                        .matcher(adminServer.path("/actuator/**"))
                )
            );

        // Remember-me functionality
        http.rememberMe(rememberMe -> rememberMe
            .key(UUID.randomUUID().toString())
            .tokenValiditySeconds(1209600) // 2 weeks
        );

        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.builder()
            .username("admin")
            .password(passwordEncoder.encode(System.getenv("ADMIN_PASSWORD")))
            .roles("ADMIN")
            .build();

        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### Custom CSRF Filter[​](#custom-csrf-filter "Direct link to Custom CSRF Filter")

Required to make CSRF token available to JavaScript:

```
package com.example.admin;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

public class CustomCsrfFilter extends OncePerRequestFilter {

    public static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        if (csrf != null) {
            Cookie cookie = WebUtils.getCookie(request, CSRF_COOKIE_NAME);
            String token = csrf.getToken();

            if (cookie == null || token != null && !token.equals(cookie.getValue())) {
                cookie = new Cookie(CSRF_COOKIE_NAME, token);
                cookie.setPath("/");
                response.addCookie(cookie);
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

***

## Configuration Options[​](#configuration-options "Direct link to Configuration Options")

### Context Path[​](#context-path "Direct link to Context Path")

If your Admin Server uses a custom context path:

```
spring:
  boot:
    admin:
      context-path: /admin
```

Adjust security matchers:

```
.requestMatchers(PathPatternRequestMatcher.withDefaults()
    .matcher(adminServer.path("/assets/**")))
.permitAll()
```

The `adminServer.path()` method handles context path automatically.

### Remember-Me[​](#remember-me "Direct link to Remember-Me")

Enable persistent sessions:

```
http.rememberMe(rememberMe -> rememberMe
    .key(UUID.randomUUID().toString())           // Unique key
    .tokenValiditySeconds(1209600)               // 2 weeks
    .rememberMeParameter("remember-me")          // Form parameter name
)
```

**Note**: Remember-me requires a `UserDetailsService` bean.

### Session Management[​](#session-management "Direct link to Session Management")

Configure session behavior:

```
http.sessionManagement(session -> session
    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
    .maximumSessions(1)                          // Max 1 session per user
    .maxSessionsPreventsLogin(false)             // Invalidate old session
)
```

***

## User Management[​](#user-management "Direct link to User Management")

### In-Memory Users[​](#in-memory-users "Direct link to In-Memory Users")

Simple for development or small deployments:

```
@Bean
public InMemoryUserDetailsManager userDetailsService(PasswordEncoder encoder) {
    UserDetails admin = User.builder()
        .username("admin")
        .password(encoder.encode(System.getenv("ADMIN_PASSWORD")))
        .roles("ADMIN")
        .build();

    UserDetails viewer = User.builder()
        .username("viewer")
        .password(encoder.encode(System.getenv("VIEWER_PASSWORD")))
        .roles("USER")
        .build();

    return new InMemoryUserDetailsManager(admin, viewer);
}
```

### Database Users[​](#database-users "Direct link to Database Users")

Use `JdbcUserDetailsManager` for database-backed users:

```
@Bean
public JdbcUserDetailsManager userDetailsService(DataSource dataSource,
                                                  PasswordEncoder encoder) {
    JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);

    // Create default admin if not exists
    if (!manager.userExists("admin")) {
        UserDetails admin = User.builder()
            .username("admin")
            .password(encoder.encode(System.getenv("ADMIN_PASSWORD")))
            .roles("ADMIN")
            .build();
        manager.createUser(admin);
    }

    return manager;
}
```

**Database Schema**:

```
CREATE TABLE users (
    username VARCHAR(50) NOT NULL PRIMARY KEY,
    password VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL
);

CREATE TABLE authorities (
    username VARCHAR(50) NOT NULL,
    authority VARCHAR(50) NOT NULL,
    FOREIGN KEY (username) REFERENCES users(username)
);

CREATE UNIQUE INDEX ix_auth_username ON authorities (username, authority);
```

### LDAP Authentication[​](#ldap-authentication "Direct link to LDAP Authentication")

Authenticate against an LDAP server:

```
@Bean
public SecurityFilterChain filterChain(HttpSecurity http,
                                       AdminServerProperties adminServer) throws Exception {
    http
        .authorizeHttpRequests(/* ... */)
        .formLogin(/* ... */)
        .logout(/* ... */)
        .httpBasic(Customizer.withDefaults());

    return http.build();
}

@Configuration
public static class LdapConfig {

    @Bean
    public EmbeddedLdapServerContextSourceFactoryBean contextSourceFactoryBean() {
        EmbeddedLdapServerContextSourceFactoryBean factory =
            new EmbeddedLdapServerContextSourceFactoryBean();
        factory.setPort(8389);
        return factory;
    }

    @Bean
    public AuthenticationManager authenticationManager(BaseLdapPathContextSource contextSource) {
        LdapBindAuthenticationManagerFactory factory =
            new LdapBindAuthenticationManagerFactory(contextSource);
        factory.setUserDnPatterns("uid={0},ou=people");
        factory.setUserDetailsContextMapper(new PersonContextMapper());
        return factory.createAuthenticationManager();
    }
}
```

**Configuration**:

```
spring:
  ldap:
    urls: ldap://ldap.company.com:389
    base: dc=company,dc=com
    username: cn=admin,dc=company,dc=com
    password: ${LDAP_PASSWORD}
```

### OAuth2 / OIDC[​](#oauth2--oidc "Direct link to OAuth2 / OIDC")

Use OAuth2 for Single Sign-On (SSO):

**Dependencies**:

```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

**Configuration**:

```
spring:
  security:
    oauth2:
      client:
        registration:
          keycloak:
            client-id: spring-boot-admin
            client-secret: ${OAUTH2_CLIENT_SECRET}
            scope: openid,profile,email
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
        provider:
          keycloak:
            issuer-uri: https://keycloak.company.com/realms/main
```

**Security Configuration**:

```
@Bean
public SecurityFilterChain filterChain(HttpSecurity http,
                                       AdminServerProperties adminServer) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(PathPatternRequestMatcher.withDefaults()
                .matcher(adminServer.path("/assets/**")))
            .permitAll()
            .requestMatchers(PathPatternRequestMatcher.withDefaults()
                .matcher(adminServer.path("/login")))
            .permitAll()
            .anyRequest().authenticated()
        )
        .oauth2Login(oauth2 -> oauth2
            .loginPage(adminServer.path("/login"))
        )
        .logout(logout -> logout
            .logoutUrl(adminServer.path("/logout"))
            .logoutSuccessUrl(adminServer.path("/"))
        );

    // CSRF and other configurations...

    return http.build();
}
```

***

## Role-Based Access Control[​](#role-based-access-control "Direct link to Role-Based Access Control")

Restrict access by roles:

```
@Bean
public SecurityFilterChain filterChain(HttpSecurity http,
                                       AdminServerProperties adminServer) throws Exception {
    http.authorizeHttpRequests(auth -> auth
            .requestMatchers(PathPatternRequestMatcher.withDefaults()
                .matcher(adminServer.path("/assets/**")))
            .permitAll()
            .requestMatchers(PathPatternRequestMatcher.withDefaults()
                .matcher(adminServer.path("/login")))
            .permitAll()
            // Only ADMIN can delete instances
            .requestMatchers(PathPatternRequestMatcher.withDefaults()
                .matcher(DELETE, adminServer.path("/instances/*")))
            .hasRole("ADMIN")
            // Only ADMIN can access logfile endpoint
            .requestMatchers(PathPatternRequestMatcher.withDefaults()
                .matcher(adminServer.path("/instances/*/actuator/logfile")))
            .hasRole("ADMIN")
            // USER and ADMIN can view everything else
            .anyRequest().hasAnyRole("USER", "ADMIN")
        )
        .formLogin(formLogin -> formLogin.loginPage(adminServer.path("/login")))
        .httpBasic(Customizer.withDefaults());

    return http.build();
}
```

**Create users with different roles**:

```
@Bean
public InMemoryUserDetailsManager userDetailsService(PasswordEncoder encoder) {
    UserDetails admin = User.builder()
        .username("admin")
        .password(encoder.encode(System.getenv("ADMIN_PASSWORD")))
        .roles("ADMIN")
        .build();

    UserDetails viewer = User.builder()
        .username("viewer")
        .password(encoder.encode(System.getenv("VIEWER_PASSWORD")))
        .roles("USER")
        .build();

    return new InMemoryUserDetailsManager(admin, viewer);
}
```

***

## HTTP vs HTTPS[​](#http-vs-https "Direct link to HTTP vs HTTPS")

### Local Development (HTTP)[​](#local-development-http "Direct link to Local Development (HTTP)")

For local development, HTTP is acceptable:

```
server:
  port: 8080
```

### HTTPS Configuration[​](#https-configuration "Direct link to HTTPS Configuration")

Enable HTTPS for secure communication:

```
server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: spring-boot-admin
```

**Generate keystore**:

```
keytool -genkeypair -alias spring-boot-admin \
  -keyalg RSA -keysize 2048 \
  -storetype PKCS12 \
  -keystore keystore.p12 \
  -validity 3650 \
  -storepass changeit
```

**Update Admin Client configuration**:

```
spring:
  boot:
    admin:
      client:
        url: https://admin-server:8443
```

***

## Reverse Proxy Setup[​](#reverse-proxy-setup "Direct link to Reverse Proxy Setup")

### Behind Nginx[​](#behind-nginx "Direct link to Behind Nginx")

**Nginx Configuration**:

```
server {
    listen 80;
    server_name admin.company.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

**Admin Server Configuration**:

```
server:
  forward-headers-strategy: native

spring:
  boot:
    admin:
      ui:
        public-url: https://admin.company.com
```

### Behind Apache[​](#behind-apache "Direct link to Behind Apache")

**Apache Configuration**:

```
<VirtualHost *:80>
    ServerName admin.company.com

    ProxyPreserveHost On
    ProxyPass / http://localhost:8080/
    ProxyPassReverse / http://localhost:8080/

    RequestHeader set X-Forwarded-Proto "https"
    RequestHeader set X-Forwarded-Port "443"
</VirtualHost>
```

***

## Security Headers[​](#security-headers "Direct link to Security Headers")

Add security headers to protect against common attacks:

```
@Bean
public SecurityFilterChain filterChain(HttpSecurity http,
                                       AdminServerProperties adminServer) throws Exception {
    http
        .headers(headers -> headers
            // Content Security Policy
            .contentSecurityPolicy(csp -> csp
                .policyDirectives("default-src 'self'; " +
                    "script-src 'self' 'unsafe-inline'; " +
                    "style-src 'self' 'unsafe-inline'; " +
                    "img-src 'self' data:; " +
                    "font-src 'self' data:")
            )
            // Frame options
            .frameOptions(frame -> frame.sameOrigin())
            // XSS protection
            .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
            // HSTS
            .httpStrictTransportSecurity(hsts -> hsts
                .includeSubDomains(true)
                .maxAgeInSeconds(31536000)
            )
        );

    // Other configurations...

    return http.build();
}
```

***

## Multiple Authentication Methods[​](#multiple-authentication-methods "Direct link to Multiple Authentication Methods")

Support both form login and HTTP Basic:

```
@Bean
public SecurityFilterChain filterChain(HttpSecurity http,
                                       AdminServerProperties adminServer) throws Exception {
    http
        .authorizeHttpRequests(/* ... */)
        .formLogin(formLogin -> formLogin
            .loginPage(adminServer.path("/login"))
        )
        .httpBasic(Customizer.withDefaults())
        .logout(logout -> logout
            .logoutUrl(adminServer.path("/logout"))
        );

    return http.build();
}
```

* **Form login**: For browser-based UI access
* **HTTP Basic**: For API clients, scripts, monitoring tools

***

## Troubleshooting[​](#troubleshooting "Direct link to Troubleshooting")

### Issue: Login page not loading[​](#issue-login-page-not-loading "Direct link to Issue: Login page not loading")

**Cause**: Assets blocked by security configuration.

**Solution**: Permit `/assets/**`:

```
.requestMatchers(PathPatternRequestMatcher.withDefaults()
    .matcher(adminServer.path("/assets/**")))
.permitAll()
```

### Issue: Infinite redirect loop[​](#issue-infinite-redirect-loop "Direct link to Issue: Infinite redirect loop")

**Cause**: Login page requires authentication.

**Solution**: Permit `/login`:

```
.requestMatchers(PathPatternRequestMatcher.withDefaults()
    .matcher(adminServer.path("/login")))
.permitAll()
```

### Issue: Clients cannot register[​](#issue-clients-cannot-register "Direct link to Issue: Clients cannot register")

**Cause**: CSRF protection blocking `/instances` endpoint.

**Solution**: Exempt client registration endpoints:

```
.csrf(csrf -> csrf
    .ignoringRequestMatchers(
        PathPatternRequestMatcher.withDefaults()
            .matcher(POST, adminServer.path("/instances")),
        PathPatternRequestMatcher.withDefaults()
            .matcher(DELETE, adminServer.path("/instances/*"))
    )
)
```

### Issue: Remember-me not working[​](#issue-remember-me-not-working "Direct link to Issue: Remember-me not working")

**Cause**: No `UserDetailsService` configured.

**Solution**: Add `UserDetailsService` bean:

```
@Bean
public InMemoryUserDetailsManager userDetailsService(PasswordEncoder encoder) {
    // ...
}
```

### Issue: 401 on API requests[​](#issue-401-on-api-requests "Direct link to Issue: 401 on API requests")

**Cause**: API client not providing credentials.

**Solution**: Use HTTP Basic authentication:

```
curl -u admin:password http://localhost:8080/instances
```

***

## Complete Example[​](#complete-example "Direct link to Complete Example")

```
package com.example.admin;

import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import de.codecentric.boot.admin.server.config.EnableAdminServer;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.POST;

@EnableAdminServer
@Configuration
public class AdminServerConfig {

    private final AdminServerProperties adminServer;

    public AdminServerConfig(AdminServerProperties adminServer) {
        this.adminServer = adminServer;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        SavedRequestAwareAuthenticationSuccessHandler successHandler =
            new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter("redirectTo");
        successHandler.setDefaultTargetUrl(adminServer.path("/"));

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PathPatternRequestMatcher.withDefaults()
                    .matcher(adminServer.path("/assets/**")))
                .permitAll()
                .requestMatchers(PathPatternRequestMatcher.withDefaults()
                    .matcher(adminServer.path("/login")))
                .permitAll()
                .requestMatchers(PathPatternRequestMatcher.withDefaults()
                    .matcher(adminServer.path("/actuator/info")))
                .permitAll()
                .requestMatchers(PathPatternRequestMatcher.withDefaults()
                    .matcher(adminServer.path("/actuator/health")))
                .permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(formLogin -> formLogin
                .loginPage(adminServer.path("/login"))
                .successHandler(successHandler)
            )
            .logout(logout -> logout
                .logoutUrl(adminServer.path("/logout"))
            )
            .httpBasic(Customizer.withDefaults());

        http.addFilterAfter(new CustomCsrfFilter(), BasicAuthenticationFilter.class)
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                .ignoringRequestMatchers(
                    PathPatternRequestMatcher.withDefaults()
                        .matcher(POST, adminServer.path("/instances")),
                    PathPatternRequestMatcher.withDefaults()
                        .matcher(DELETE, adminServer.path("/instances/*")),
                    PathPatternRequestMatcher.withDefaults()
                        .matcher(adminServer.path("/actuator/**"))
                )
            );

        http.rememberMe(rememberMe -> rememberMe
            .key(UUID.randomUUID().toString())
            .tokenValiditySeconds(1209600)
        );

        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails admin = User.builder()
            .username("admin")
            .password(passwordEncoder.encode(System.getenv("ADMIN_PASSWORD")))
            .roles("ADMIN")
            .build();

        UserDetails viewer = User.builder()
            .username("viewer")
            .password(passwordEncoder.encode(System.getenv("VIEWER_PASSWORD")))
            .roles("USER")
            .build();

        return new InMemoryUserDetailsManager(admin, viewer);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

**application.yml**:

```
spring:
  application:
    name: spring-boot-admin-server

  boot:
    admin:
      context-path: /admin
      ui:
        title: "Production Monitor"

server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${KEYSTORE_PASSWORD}
    key-store-type: PKCS12
```

***

## See Also[​](#see-also "Direct link to See Also")

* [Actuator Security](/4.0.1/docs/security/actuator-security.md) - Secure client actuator endpoints
* [CSRF Protection](/4.0.1/docs/security/csrf-protection.md) - Detailed CSRF configuration
