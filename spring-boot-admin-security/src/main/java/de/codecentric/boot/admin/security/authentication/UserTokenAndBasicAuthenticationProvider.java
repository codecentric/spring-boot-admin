package de.codecentric.boot.admin.security.authentication;


import de.codecentric.boot.admin.security.token.UserTokenAndBasicAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

/**
 *
 * @author Robert Winkler
 *
 */

/**
 * Spring Security Authentication Provider which processes the <code>UserTokenAndBasicAuthenticationToken</code> of the incoming request.
 * <p>
 * It has two distinct, but related functions:
 * <p>
 * 1) Authenticate the client, i.e., check that the given Authentication token is valid.
 * 2) Extract the client information and set up the requestcontext based on this information.
 */
public class UserTokenAndBasicAuthenticationProvider extends AdminServerAuthenticationProvider {


    public UserTokenAndBasicAuthenticationProvider() {
        super();
    }

    @Override
    public Authentication authenticate(Authentication authenticationToken) throws AuthenticationException {
        Assert.isInstanceOf(UserTokenAndBasicAuthenticationToken.class, authenticationToken,
                "Only a UserTokenAndBasicAuthenticationToken is supported by UserTokenAndBasicAuthenticationProvider");
        // Authenticate AppId and AppSec. If the authentication fails, an AuthenticationException will be thrown
        UserTokenAndBasicAuthenticationToken successfulAuthenticatedToken = (UserTokenAndBasicAuthenticationToken) super.authenticate(authenticationToken);
        // Validate and extract userInfo from userToken
        boolean valid = validateUserTokenAndExtractUserInfo(successfulAuthenticatedToken);
        // Load the requestContext based on X-UserToken userInfo
        return successfulAuthenticatedToken;
    }

    public boolean validateUserTokenAndExtractUserInfo(UserTokenAndBasicAuthenticationToken successfulAuthenticatedToken){
        //TODO
        return true;
    }

    /**
     * Creates a successful {@link org.springframework.security.core.Authentication} object.
     *
     * @param principal that should be the principal in the returned object (defined by the {@link
     *        #isForcePrincipalAsString()} method)
     * @param authenticationToken that was presented to the provider for validation
     * @param user that was loaded by the implementation
     *
     * @return the successful authentication token
     */
    @Override
    protected Authentication createSuccessAuthentication(Object principal, Authentication authenticationToken,
                                                         UserDetails user) {
        Assert.isInstanceOf(UserTokenAndBasicAuthenticationToken.class, authenticationToken,
                "Only a UserTokenAndBasicAuthenticationToken is supported by UserTokenAndBasicAuthenticationProvider");
        // Ensure we return the original credentials the user supplied,
        // so subsequent attempts are successful even with encoded passwords.
        // Also ensure we return the original getDetails(), so that future
        // authentication events after cache expiry contain the details
        UserTokenAndBasicAuthenticationToken originalAuthenticationToken = (UserTokenAndBasicAuthenticationToken) authenticationToken;
        UserTokenAndBasicAuthenticationToken result = new UserTokenAndBasicAuthenticationToken(principal,
                originalAuthenticationToken, user.getAuthorities());
        result.setDetails(originalAuthenticationToken.getDetails());
        return result;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UserTokenAndBasicAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
