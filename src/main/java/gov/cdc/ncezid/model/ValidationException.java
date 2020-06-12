package gov.cdc.ncezid.model;

import io.micronaut.http.HttpStatus;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ValidationException extends Exception {

    private static final long serialVersionUID = 1L;

    private HttpStatus httpStatus;

    private ValidationException(String reason, HttpStatus httpStatus) {
        super(reason);
        this.httpStatus = httpStatus;
    }

    public static ValidationException badRequest(String reason) {
        return new ValidationException(reason, HttpStatus.BAD_REQUEST);
    }

    // Add more here as needed...
    
}