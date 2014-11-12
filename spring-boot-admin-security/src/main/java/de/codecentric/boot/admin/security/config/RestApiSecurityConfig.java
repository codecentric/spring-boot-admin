package de.codecentric.boot.admin.security.config;

import de.codecentric.boot.admin.security.filter.AdminServerAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * @author Robert Winkler
 */
@Configuration
@Import(CommonSecurityConfig.class)
@Order(AdminServerSecurityProperties.REST_API_AUTH_ORDER)
public class RestApiSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private CommonSecurityConfig commonSecurityConfig;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.parentAuthenticationManager(commonSecurityConfig.authenticationManager());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        AuthenticationEntryPoint basicAuthenticationEntryPoint = commonSecurityConfig.authenticationEntryPoint();
        // Replace the BasicAuthenticationFilter with a custom AdminServerAuthenticationFilter
        http.requestMatchers().antMatchers(commonSecurityConfig.securityProperties().getRestApiPath());
        http.addFilter(new AdminServerAuthenticationFilter(commonSecurityConfig.authenticationManager(), basicAuthenticationEntryPoint, commonSecurityConfig.authHeaderParserManager()));
        http.authorizeRequests().anyRequest().hasRole(AdminServerSecurityProperties.ROLE_CLIENT);
        http.exceptionHandling().authenticationEntryPoint(basicAuthenticationEntryPoint);
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.headers().disable();
        http.csrf().disable();
        http.logout().disable();
    }
}
