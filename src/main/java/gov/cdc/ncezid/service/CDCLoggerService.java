package gov.cdc.ncezid.service;


import gov.cdc.ncezid.model.CDCLogEntry;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;

@Client(value = "${cdc-logging.url}")
public interface CDCLoggerService {

    @Post()
    void sendError( @Body CDCLogEntry logEntry);

}