# Foster Security

## Securing Spring Boot Admin Server

Since there are several approaches on solving authentication and authorization in distributed web applications Spring Boot Admin doesn’t ship a default one. By default `spring-boot-admin-server-ui` provides a login page and a logout button.

A Spring Security configuration for your server could look like this:

```java title="SecuritySecureConfig.java"
@Configuration(proxyBeanMethods = false)
public class SecuritySecureConfig {

    private final AdminServerProperties adminServer;

    private final SecurityProperties security;

    public SecuritySecureConfig(AdminServerProperties adminServer, SecurityProperties security) {
        this.adminServer = adminServer;
        this.security = security;
    }

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter("redirectTo");
        successHandler.setDefaultTargetUrl(this.adminServer.path("/"));

        http.authorizeHttpRequests((authorizeRequests) -> authorizeRequests //
            .requestMatchers(new AntPathRequestMatcher(this.adminServer.path("/assets/**")))
            .permitAll() // (1)
            .requestMatchers(new AntPathRequestMatcher(this.adminServer.path("/actuator/info")))
            .permitAll()
            .requestMatchers(new AntPathRequestMatcher(adminServer.path("/actuator/health")))
            .permitAll()
            .requestMatchers(new AntPathRequestMatcher(this.adminServer.path("/login")))
            .permitAll()
            .dispatcherTypeMatchers(DispatcherType.ASYNC)
            .permitAll() // https://github.com/spring-projects/spring-security/issues/11027
            .anyRequest()
            .authenticated()) // (2)
            .formLogin(
                    (formLogin) -> formLogin.loginPage(this.adminServer.path("/login")).successHandler(successHandler)) // (3)
            .logout((logout) -> logout.logoutUrl(this.adminServer.path("/logout")))
            .httpBasic(Customizer.withDefaults()); // (4)

        http.addFilterAfter(new CustomCsrfFilter(), BasicAuthenticationFilter.class) // (5)
            .csrf((csrf) -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                .ignoringRequestMatchers(
                        new AntPathRequestMatcher(this.adminServer.path("/instances"), POST.toString()), // (6)
                        new AntPathRequestMatcher(this.adminServer.path("/instances/*"), DELETE.toString()), // (6)
                        new AntPathRequestMatcher(this.adminServer.path("/actuator/**")) // (7)
                ));

        http.rememberMe((rememberMe) -> rememberMe.key(UUID.randomUUID().toString()).tokenValiditySeconds(1209600));

        return http.build();

    }

    // Required to provide UserDetailsService for "remember functionality"
    @Bean
    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.withUsername("user").password(passwordEncoder.encode("password")).roles("USER").build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
```

1. Grants public access to all static assets and the login page.
2. Every other request must be authenticated.
3. Configures login and logout.
4. Enables HTTP-Basic support. This is needed for the Spring Boot Admin Client to register.
5. Enables CSRF-Protection using Cookies
6. Disables CSRF-Protection for the endpoint the Spring Boot Admin Client uses to (de-)register.
7. Disables CSRF-Protection for the actuator endpoints.

In case you use the Spring Boot Admin Client, it needs the credentials for accessing the server:

```yaml title="application.yml"
spring.boot.admin.client:
   username: sba-client
   password: s3cret
```

For a complete sample look at [spring-boot-admin-sample-servlet](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples/spring-boot-admin-sample-servlet/).

:::note
If you protect the /instances endpoint don’t forget to configure the username and password on your SBA-Client using spring.boot.admin.client.username and spring.boot.admin.client.password.
:::

## Securing Client Actuator Endpoints

When the actuator endpoints are secured using HTTP Basic authentication the SBA Server needs credentials to access them. You can submit the credentials in the metadata when registering the application. The `BasicAuthHttpHeaderProvider` then uses this metadata to add the `Authorization` header to access your application’s actuator endpoints. You can provide your own `HttpHeadersProvider` to alter the behaviour (e.g. add some decryption) or add extra headers.

:::note
The SBA Server masks certain metadata in the HTTP interface to prevent leaking of sensitive information.
:::

:::warning
You should configure HTTPS for your SBA Server or (service registry) when transferring credentials via the metadata.
:::

:::warning
When using Spring Cloud Discovery, you must be aware that anybody who can query your service registry can obtain the credentials.
:::

:::tip
When using this approach the SBA Server decides whether or not the user can access the registered applications. There are more complex solutions possible (using OAuth2) to let the clients decide if the user can access the endpoints. For that please have a look at the samples in [joshiste/spring-boot-admin-samples](https://github.com/joshiste/spring-boot-admin-samples).
:::

### SBA Client

```yaml title="application.yml"
spring.boot.admin.client:
    url: http://localhost:8080
    instance:
      metadata:
        user.name: ${spring.security.user.name}
        user.password: ${spring.security.user.password}
```

### SBA Server

You can specify credentials via configuration properties in your admin server.

:::tip
You can use this in conjuction with [spring-cloud-kubernetes](https://cloud.spring.io/spring-cloud-kubernetes/1.1.x/reference/html/#secrets-propertysource) to pull credentials from [secrets](https://kubernetes.io/docs/concepts/configuration/secret/).
:::

To enable pulling credentials from properties the `spring.boot.admin.instance-auth.enabled` property must be `true` (default).

:::note
If your clients provide credentials via metadata (i.e., via service annotations), that metadata will be used instead of the properites.
:::

You can provide a default username and password by setting `spring.boot.admin.instance-auth.default-user-name` and `spring.boot.admin.instance-auth.default-user-password`. Optionally you can provide credentials for specific services (by name) using the `spring.boot.admin.instance-auth.service-map.*.user-name` pattern, replacing `*` with the service name.

```yaml title="application.yml"
spring.boot.admin:
  instance-auth:
    enabled: true
    default-user-name: "${some.user.name.from.secret}"
    default-password: "${some.user.password.from.secret}"
    service-map:
      my-first-service-to-monitor:
        user-name: "${some.user.name.from.secret}"
        user-password: "${some.user.password.from.secret}"
      my-second-service-to-monitor:
        user-name: "${some.user.name.from.secret}"
        user-password: "${some.user.password.from.secret}"
```

### Eureka
```yaml title="application.yml"
eureka:
  instance:
    metadata-map:
      user.name: ${spring.security.user.name}
      user.password: ${spring.security.user.password}
```

### Consul
```yaml title="application.yml"
spring.cloud.consul:
  discovery:
    metadata:
        user-name: ${spring.security.user.name}
        user-password: ${spring.security.user.password}
```

:::warning
Consul does not allow dots (".") in metadata keys, use dashes instead.
:::

## CSRF on Actuator Endpoints

Some of the actuator endpoints (e.g. `/loggers`) support POST requests. When using Spring Security you need to ignore the actuator endpoints for CSRF-Protection as the Spring Boot Admin Server currently lacks support.

```java title="SecuritySecureConfig.java"
@Bean
protected SecurityFilterChain filterChain(HttpSecurity http) {
    return http.csrf(c -> c.ignoringRequestMatchers("/actuator/**")).build();
}
```

## Using Mutual TLS

SBA Server can also use client certificates to authenticate when accessing the actuator endpoints. If a custom configured `ClientHttpConnector` bean is present, Spring Boot will automatically configure a `WebClient.Builder` using it, which will be used by Spring Boot Admin.

```java title="CustomHttpClientConfig.java"
@Bean
public ClientHttpConnector customHttpClient() {
    SslContextBuilder sslContext = SslContextBuilder.forClient();
    //Your sslContext customizations go here
    HttpClient httpClient = HttpClient.create().secure(
        ssl -> ssl.sslContext(sslContext)
    );
    return new ReactorClientHttpConnector(httpClient);
}
```
