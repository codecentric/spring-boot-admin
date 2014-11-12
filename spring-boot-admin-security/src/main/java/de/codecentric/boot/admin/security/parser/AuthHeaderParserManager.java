package de.codecentric.boot.admin.security.parser;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Robert Winkler
 *
 */
public interface AuthHeaderParserManager {

    /**
     * Attempts to parse the passed {@link javax.servlet.http.HttpServletRequest} object in order to find an authentication header.
     * A list of {@link AuthHeaderParser}s will be successively tried until an
     * <code>AuthHeaderParser</code> is capable of finding and parsing a valid authentication header.
     * Returns a Spring Security Authentication token {@link org.springframework.security.core.Authentication}.
     *
     * @param request the HttpServletRequest to parse
     * @param credentialsCharset the character set of the HTTP Authentication header
     * @throws org.springframework.security.core.AuthenticationException if no <code>AuthHeaderParser</code> is capable of finding a valid authentication header
     * @return a <code>Authentication</code> if an Authentication header is present, otherwise throws an exception
     */
    public Authentication parse(HttpServletRequest request, String credentialsCharset) throws AuthenticationException;

}
