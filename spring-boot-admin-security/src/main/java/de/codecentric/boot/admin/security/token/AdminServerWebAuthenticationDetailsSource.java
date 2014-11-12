package de.codecentric.boot.admin.security.token;

import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Robert Winkler
 */

/**
 * Implementation of {@link org.springframework.security.authentication.AuthenticationDetailsSource} which builds the details object from
 * an <tt>HttpServletRequest</tt> object, creating a {@code WebAuthenticationDetails}.
 *
 * @author Ben Alex
 */
public class AdminServerWebAuthenticationDetailsSource implements AuthenticationDetailsSource<HttpServletRequest, WebAuthenticationDetails> {

    /**
     * @param context the {@code HttpServletRequest} object.
     * @return the {@code SpicaWebAuthenticationDetails} containing information about the current request
     */
    public WebAuthenticationDetails buildDetails(HttpServletRequest context) {
        return new AdminServerWebAuthenticationDetails(context);
    }
}
