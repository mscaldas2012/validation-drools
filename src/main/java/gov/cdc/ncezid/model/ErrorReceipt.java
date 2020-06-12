package gov.cdc.ncezid.model;

import lombok.Data;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class ErrorReceipt {
    private ERROR_CODES code;

    private String timestamp;
    private String description;
    private int status;
    private String path;
    private String exception;

    public ErrorReceipt() {
        this.timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT);
    }

    public ErrorReceipt(ERROR_CODES code, String description, int status, String path, String exception) {
        this();
        this.code = code;
        this.description = description;
        this.status = status;
        this.path = path;
        this.exception = exception;
    }

}


