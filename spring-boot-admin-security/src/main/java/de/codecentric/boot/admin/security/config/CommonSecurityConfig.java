package de.codecentric.boot.admin.security.config;

import de.codecentric.boot.admin.security.authentication.AdminServerAuthenticationProvider;
import de.codecentric.boot.admin.security.authentication.UserTokenAndBasicAuthenticationProvider;
import de.codecentric.boot.admin.security.entrypoint.AdminServerAuthenticationEntryPoint;
import de.codecentric.boot.admin.security.parser.AuthHeaderParser;
import de.codecentric.boot.admin.security.parser.AuthHeaderParserManager;
import de.codecentric.boot.admin.security.parser.DefaultAuthHeaderParserManager;
import de.codecentric.boot.admin.security.parser.UserTokenAndBasicAuthHeaderParser;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Robert Winkler
 */
@Configuration
@EnableConfigurationProperties()
public class CommonSecurityConfig {

    @Bean
    public AuthHeaderParser userTokenAndBasicAuthHeaderParser(){
        AuthHeaderParser authHeaderParser = new UserTokenAndBasicAuthHeaderParser();
        return authHeaderParser;
    }

    @Bean
    @ConditionalOnMissingBean(name = "authenticationHeaderParsers")
    public List<AuthHeaderParser> authenticationHeaderParsers() {
        List<AuthHeaderParser> authenticationHeaderParsers = new ArrayList<>();
        authenticationHeaderParsers.add(userTokenAndBasicAuthHeaderParser());
        return authenticationHeaderParsers;
    }

    @Bean
    public AuthHeaderParserManager authHeaderParserManager(){
        AuthHeaderParserManager authHeaderParserManager = new DefaultAuthHeaderParserManager(authenticationHeaderParsers());
        return authHeaderParserManager;
    }

    @Bean
    public AuthenticationProvider userTokenAndBasicAuthenticationProvider() throws Exception{
        AdminServerAuthenticationProvider authenticationProvider = new UserTokenAndBasicAuthenticationProvider();
        authenticationProvider.setUserDetailsService(restUserDetailsService());
        return authenticationProvider;
    }

    @Bean
    public AuthenticationProvider basicAuthenticationProvider() throws Exception {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(restUserDetailsService());
        return authenticationProvider;
    }

    @Bean
    @ConditionalOnMissingBean(name = "authenticationProviders")
    public List<AuthenticationProvider> authenticationProviders() throws Exception {
        // The order of the AuthenticationProvider is important
        List<AuthenticationProvider> authenticationProviders= new ArrayList<>();
        authenticationProviders.add(userTokenAndBasicAuthenticationProvider());
        authenticationProviders.add(basicAuthenticationProvider());
        return authenticationProviders;
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception{
        ProviderManager authenticationManager = new ProviderManager(authenticationProviders());
        authenticationManager.setAuthenticationEventPublisher(authenticationEventPublisher());
        return authenticationManager;
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        BasicAuthenticationEntryPoint entryPoint = new AdminServerAuthenticationEntryPoint();
        entryPoint.setRealmName(securityProperties().getBasic().getRealm());
        return entryPoint;
    }

    @Bean
    public AuthenticationEventPublisher authenticationEventPublisher() {
        return new DefaultAuthenticationEventPublisher();
    }

    @Bean
    public AdminServerSecurityProperties securityProperties() {
        return new AdminServerSecurityProperties();
    }

    @Bean
    public UserDetailsService restUserDetailsService() throws IOException {
        Properties clientAuth = PropertiesLoaderUtils.loadAllProperties("clientAuth.properties");
        return new InMemoryUserDetailsManager(clientAuth);
    }
}
