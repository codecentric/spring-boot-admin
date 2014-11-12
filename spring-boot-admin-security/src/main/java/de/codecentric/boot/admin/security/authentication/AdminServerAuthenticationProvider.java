package de.codecentric.boot.admin.security.authentication;

import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

/**
 * @author Robert Winkler
 */
public abstract class AdminServerAuthenticationProvider extends DaoAuthenticationProvider {

    public AdminServerAuthenticationProvider() {
        this.setForcePrincipalAsString(true);
    }
}
