package de.codecentric.boot.admin.security.token;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Robert Winkler
 */
public class AdminServerWebAuthenticationDetails extends WebAuthenticationDetails {

    /**
     * Records the remote address and can be customized to recored more information.
     *
     * @param request that the authentication request was received from
     */
    public AdminServerWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
      }

}
