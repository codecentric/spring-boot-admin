package de.codecentric.boot.admin.security.token;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import java.util.Collection;

/**
 * @author Robert Winkler
 */
public abstract class AdminServerAuthenticationToken extends UsernamePasswordAuthenticationToken {

    /**
     * This constructor can be safely used by any code that wishes to create a
     * Authentication token, as the {@link
     * #isAuthenticated()} will return <code>false</code>.
     *
     * @param appId the client id
     * @param appSecret the client secret
     *
     */
    public AdminServerAuthenticationToken(String appId, String appSecret) {
        super(appId, appSecret);
        Assert.hasLength(appId, "appId must not be empty");
        Assert.hasLength(appSecret, "appSecret must not be empty");
    }

    /**
     * This constructor must only be used by <code>AuthenticationManager</code> or <code>AuthenticationProvider</code>
     * implementations that are satisfied with producing a trusted (i.e. {@link #isAuthenticated()} = <code>true</code>)
     * authentication token.

     * @param principal that should be the principal in the returned object
     * @param authenticatedToken that was presented to the provider for validation
     * @param authorities the collection of <tt>GrantedAuthority</tt>s for the principal represented by this authentication object.
     */
    public AdminServerAuthenticationToken(Object principal, Authentication authenticatedToken,
                                          Collection<? extends GrantedAuthority> authorities) {
        super(principal, authenticatedToken.getCredentials(), authorities);
    }


    public String getAppId() {
        return getPrincipal().toString();
    }
}
