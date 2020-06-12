package gov.cdc.ncezid.rest.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class S2SAuth {
    private final Logger logger = LoggerFactory.getLogger(S2SAuth.class);
    private S2SAuthConfig config;

    @Inject
    public S2SAuth( S2SAuthConfig config) {
        this.config = config;
    }

    public boolean checkS2SCredentials(String token) throws ServiceNotAuthorizedException {
        if (!config.getToken().equals(token)) {
            logger.error("Invalid token - Authentication Failed");
            throw new ServiceNotAuthorizedException("Service not Authorized to call method!");
        }
        return true;
    }
}