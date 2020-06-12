package gov.cdc.ncezid.rest.security;

public class ServiceNotAuthorizedException extends Exception {

    public ServiceNotAuthorizedException(String message) {

        super(message);
    }
}

