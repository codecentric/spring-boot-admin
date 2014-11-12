package de.codecentric.boot.admin.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.mvc.EndpointHandlerMapping;
import org.springframework.boot.actuate.endpoint.mvc.MvcEndpoint;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Robert Winkler
 */
@Configuration
@Import(CommonSecurityConfig.class)
@Order(AdminServerSecurityProperties.MANAGEMENT_API_AUTH_ORDER)
public class ManagementSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private EndpointHandlerMapping endpointHandlerMapping;

    @Autowired
    private CommonSecurityConfig commonSecurityConfig;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.parentAuthenticationManager(commonSecurityConfig.authenticationManager());
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(getEndpointPaths(this.endpointHandlerMapping, false));
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // secure endpoints
        String[] securedPaths = getEndpointPaths(this.endpointHandlerMapping, true);
        if (securedPaths.length > 0) {
            http.requestMatchers().antMatchers(securedPaths).and().httpBasic();
            http.authorizeRequests().anyRequest().hasRole(AdminServerSecurityProperties.ROLE_ADMIN);
            http.exceptionHandling().authenticationEntryPoint(commonSecurityConfig.authenticationEntryPoint());
            http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            http.headers().disable();
            http.csrf().disable();
            http.logout().disable();
        }
    }

    private String[] getEndpointPaths(EndpointHandlerMapping endpointHandlerMapping, boolean secure) {
        Set<? extends MvcEndpoint> endpoints = endpointHandlerMapping.getEndpoints();
        List<String> paths = new ArrayList<>(endpoints.size());
        for (MvcEndpoint endpoint : endpoints) {
            if (endpoint.isSensitive() == secure) {
                String path = endpointHandlerMapping.getPrefix() + endpoint.getPath();
                paths.add(path);
            }
        }
        return paths.toArray(new String[paths.size()]);
    }
}

