package de.codecentric.boot.admin.security.parser;

import com.google.common.base.Optional;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Robert Winkler
 *
 */

public interface AuthHeaderParser {

    /**
     * Indicates a class that can parse a HTTP authentication header into a Spring Security Authentication token {@link
     * org.springframework.security.core.Authentication}.
     *
     * @param request the HttpServletRequest to parse
     * @param credentialsCharset the character set of the HTTP Authentication header
     * @return an Optional of type <code>Authentication</code> if an Authentication header is present, otherwise an empty Optional
     */
      public Optional<Authentication> parse(HttpServletRequest request, String credentialsCharset);
}
