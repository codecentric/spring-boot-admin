package de.codecentric.boot.admin.security.parser;

import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 *
 * @author Robert Winkler
 *
 */
public class DefaultAuthHeaderParserManager implements AuthHeaderParserManager {

    private List<AuthHeaderParser> authenticationHeaderParsers;
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public DefaultAuthHeaderParserManager(List<AuthHeaderParser> authenticationHeaderParserList) throws AuthenticationException {
        Assert.notEmpty(authenticationHeaderParsers, "authenticationHeaderParsers must not be empty");
        this.authenticationHeaderParsers = authenticationHeaderParserList;
    }

    @Override
    public Authentication parse(HttpServletRequest request, String credentialsCharset) {
        final boolean debug = logger.isDebugEnabled();
        Optional<Authentication> authenticationTokenOptional = Optional.absent();
        for (AuthHeaderParser parser : authenticationHeaderParsers) {
            if (debug) {
                logger.debug("Parsing attempt using " + parser.getClass().getName());
            }
            authenticationTokenOptional = parser.parse(request, credentialsCharset);
            if(authenticationTokenOptional.isPresent()){
                break;
            }
        }
        Authentication authenticationToken;
        if(authenticationTokenOptional.isPresent()){
             authenticationToken = authenticationTokenOptional.get();
        }else{
            throw new BadCredentialsException("No authentication header found in HTTP request");
        }
        return authenticationToken;
    }
}
