package de.codecentric.boot.admin.security.parser;

import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

/**
 * @author Robert Winkler
 */
abstract class BasicAuthHeaderParser implements AuthHeaderParser {

    public static final String BASICAUTH_HEADER = "Authorization";
    public static final String BASICAUTH_DELIMITER = ":";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Parses a HTTP Basic Authentication header into a Spring Security UsernamePasswordAuthenticationToken.
     *
     * @param request the HttpServletRequest to parse
     * @param credentialsCharset the character set of the HTTP Basic Authentication header
     *
     * @throws org.springframework.security.authentication.BadCredentialsException if the Basic header does not exist or is not valid Base64
     * @return an Optional of type <code>UsernamePasswordAuthenticationToken</code>
     */
    @Override
    public Optional<Authentication> parse(HttpServletRequest request, String credentialsCharset) {
        String basicAuthHeader = request.getHeader(BASICAUTH_HEADER);
        Authentication authenticationHeader;
        // If the Basic Auth header is not empty, then parse the authentication header
        if (StringUtils.hasText(basicAuthHeader)) {
            authenticationHeader = parseBasicAuthHeader(basicAuthHeader, credentialsCharset);
            if (logger.isDebugEnabled()) {
                logger.debug("Basic Authentication header found in HTTP request of client '" + authenticationHeader.getName() + "'");
            }
        }else{
            throw new BadCredentialsException("No Basic Authentication header found");
        }
        return Optional.fromNullable(authenticationHeader);
    }

    /**
     * Parses the Basic Auth header into a Authentication header.
     * @param basicAuthHeader Basic Authorization header
     * @param credentialsCharset the character set of the Basic Authentication header
     *
     * @throws org.springframework.security.authentication.BadCredentialsException if the Basic header not valid Base64
     * @return UsernamePasswordAuthenticationToken
     */
    private UsernamePasswordAuthenticationToken parseBasicAuthHeader(String basicAuthHeader, String credentialsCharset){
        byte[] decoded;
        String token;
        if(basicAuthHeader.startsWith("Basic ")){
            try {
                byte[] base64Token = basicAuthHeader.substring(6).getBytes(credentialsCharset);
                decoded = Base64.decode(base64Token);
                token = new String(decoded, credentialsCharset);
            } catch (IllegalArgumentException | UnsupportedEncodingException e ) {
                throw new BadCredentialsException("Failed to decode Basic Authentication token", e);
            }
            if(token.contains(BASICAUTH_DELIMITER)){
                String [] tokens = token.split(BASICAUTH_DELIMITER);
                if(tokens.length == 2){
                    return new UsernamePasswordAuthenticationToken(tokens[0], tokens[1]);
                }
                throw new BadCredentialsException("Invalid format of Basic Authentication token");
            }else{
                throw new BadCredentialsException("Invalid format of Basic Authentication token");
            }
        }else{
            throw new BadCredentialsException("Invalid format of Basic Authentication header");
        }
    }
}
