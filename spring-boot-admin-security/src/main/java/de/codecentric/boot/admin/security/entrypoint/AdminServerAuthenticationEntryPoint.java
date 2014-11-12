package de.codecentric.boot.admin.security.entrypoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/*
 * @author Robert Winkler
 */

/**
 * Used by the {@link org.springframework.security.web.access.ExceptionTranslationFilter} and {@link de.codecentric.boot.admin.security.filter.AdminServerAuthenticationFilter}.
 */
public class AdminServerAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        if(logger.isDebugEnabled()) {
            logger.debug("Authentication failed", authException);
        }
        super.commence(request,response, authException);
    }
}
