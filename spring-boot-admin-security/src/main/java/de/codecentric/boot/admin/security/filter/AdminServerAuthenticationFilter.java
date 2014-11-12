package de.codecentric.boot.admin.security.filter;


import de.codecentric.boot.admin.security.parser.AuthHeaderParserManager;
import de.codecentric.boot.admin.security.token.AdminServerAuthenticationToken;
import de.codecentric.boot.admin.security.token.AdminServerWebAuthenticationDetailsSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Processes a HTTP request's authorization headers, putting the result into the <code>SecurityContextHolder</code>.
 *
 * <p>
 * If authentication fails and <code>ignoreFailure</code> is <code>false</code> (the default), an {@link
 * org.springframework.security.web.AuthenticationEntryPoint} implementation is called (unless the <tt>ignoreFailure</tt> property is set to
 * <tt>true</tt>). Usually this should be {@link org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint}, which will prompt the user to
 * authenticate again via BASIC authentication.
 *
 * @author Robert Winkler
 */
public class AdminServerAuthenticationFilter extends BasicAuthenticationFilter {

    private final AuthenticationDetailsSource<HttpServletRequest,?> authenticationDetailsSource = new AdminServerWebAuthenticationDetailsSource();
    private final AuthHeaderParserManager authHeaderParserManager;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Creates an instance which will authenticate against the supplied {@code AuthenticationManager} and
     * use the supplied {@link de.codecentric.boot.admin.security.entrypoint.AdminServerAuthenticationEntryPoint} to handle authentication failures.
     *
     * @param authenticationManager the bean to submit authentication requests to
     * @param authenticationEntryPoint will be invoked when authentication fails.
     * @param authHeaderParserManager an authentication header parser manager
     * {@link org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint}.
     */
    public AdminServerAuthenticationFilter(AuthenticationManager authenticationManager,
                                           AuthenticationEntryPoint authenticationEntryPoint, AuthHeaderParserManager authHeaderParserManager) {
        super(authenticationManager, authenticationEntryPoint);
        this.authHeaderParserManager = authHeaderParserManager;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        final boolean debug = logger.isDebugEnabled();
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        try {
            AdminServerAuthenticationToken authenticationToken = (AdminServerAuthenticationToken) authHeaderParserManager.parse(request, getCredentialsCharset(request));
            authenticationToken.setDetails(authenticationDetailsSource.buildDetails(request));
            Authentication authResult = getAuthenticationManager().authenticate(authenticationToken);
            if (debug) {
                logger.debug("Authentication successful for client '" + authenticationToken.getPrincipal().toString() + "'");
            }
            SecurityContextHolder.getContext().setAuthentication(authResult);
        } catch (AuthenticationException failed) {
            SecurityContextHolder.clearContext();
            if (isIgnoreFailure()) {
                if (debug) {
                    logger.debug("Authentication failed, but was ignored", failed);
                }
                chain.doFilter(request, response);
            } else {
                getAuthenticationEntryPoint().commence(request, response, failed);
            }
            return;
        }
        chain.doFilter(request, response);
    }
}
