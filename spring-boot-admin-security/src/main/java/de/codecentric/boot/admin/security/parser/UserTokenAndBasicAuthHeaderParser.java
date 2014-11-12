package de.codecentric.boot.admin.security.parser;

import com.google.common.base.Optional;
import de.codecentric.boot.admin.security.token.UserTokenAndBasicAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Robert Winkler
 *
 */
public class UserTokenAndBasicAuthHeaderParser extends BasicAuthHeaderParser {

    /**
     * Header key of the user token.
     */
    public static final String X_USERTOKEN_HEADER = "X-UserToken";

    /**
     * Parses a HTTP X-UserToken and Basic Authentication header into a UserTokenAndBasicAuthenticationToken.
     *
     * @param request the HttpServletRequest to parse
     * @param credentialsCharset the character set of the HTTP Basic Authentication header
     *
     * @throws org.springframework.security.authentication.BadCredentialsException if the Basic header is not valid Base64
     * @return an Optional of type <code>UserTokenAndBasicAuthenticationToken</code> or <code>null</code> if the X-UserToken header does not exist
     */
    @Override
    public Optional<Authentication> parse(HttpServletRequest request, String credentialsCharset) {
        String userToken = request.getHeader(X_USERTOKEN_HEADER);
        Authentication authenticationHeader = null;
        // If the X-UserToken header is not empty, then parse the authentication header
        if (StringUtils.hasText(userToken)) {
            Optional<Authentication> basicAuthenticationOptional = super.parse(request, credentialsCharset);
            if(basicAuthenticationOptional.isPresent()){
                UsernamePasswordAuthenticationToken basicAuthentication = (UsernamePasswordAuthenticationToken) basicAuthenticationOptional.get();
                authenticationHeader = new UserTokenAndBasicAuthenticationToken(basicAuthentication.getPrincipal().toString(), basicAuthentication.getCredentials().toString(), userToken);
                if (logger.isDebugEnabled()) {
                    logger.debug("X-UserToken and Basic Authentication header found in HTTP request of client '" + authenticationHeader.getName() + "'");
                }
            }
        }
        return Optional.fromNullable(authenticationHeader);
    }

}
