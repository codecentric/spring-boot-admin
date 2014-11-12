package de.codecentric.boot.admin.security.token;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import java.util.Collection;

/**
 *
 * @author Robert Winkler
 *
 */
public class UserTokenAndBasicAuthenticationToken extends AdminServerAuthenticationToken {

    /**
     * X-UserToken HTTP header
     */
    private final String userToken;

    /**
     * This constructor can be safely used by any code that wishes to create a
     * <code>UserTokenAndBasicAuthenticationToken</code>, as the {@link
     * #isAuthenticated()} will return <code>false</code>.
     *
     * @param appId the client id
     * @param appSecret the client secret
     * @param userToken the X-UserToken
     *
     */
    public UserTokenAndBasicAuthenticationToken(String appId, String appSecret, String userToken) {
        super(appId, appSecret);
        Assert.hasLength(userToken, "userToken must not be empty");
        this.userToken = userToken;
    }

    /**
     * This constructor must only be used by <code>AuthenticationManager</code> or <code>AuthenticationProvider</code>
     * implementations that are satisfied with producing a trusted (i.e. {@link #isAuthenticated()} = <code>true</code>)
     * authentication token.

     * @param principal that should be the principal in the returned object
     * @param authenticatedToken that was presented to the provider for validation
     * @param authorities the collection of <tt>GrantedAuthority</tt>s for the principal represented by this authentication object.
     */
    public UserTokenAndBasicAuthenticationToken(Object principal, UserTokenAndBasicAuthenticationToken authenticatedToken,
                                    Collection<? extends GrantedAuthority> authorities) {
        super(principal, authenticatedToken, authorities);
        this.userToken = authenticatedToken.getUserToken();
    }

    public String getUserToken() {
        return userToken;
    }
}
