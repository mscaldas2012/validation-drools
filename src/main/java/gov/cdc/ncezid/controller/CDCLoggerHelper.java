package gov.cdc.ncezid.controller;

import gov.cdc.ncezid.model.CDCLogEntry;
import gov.cdc.ncezid.service.CDCLoggerService;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.PrintWriter;
import java.io.StringWriter;

@Singleton
public class CDCLoggerHelper {
    private final Logger logger = LoggerFactory.getLogger(CDCLoggerHelper.class);

    private  CDCLoggerService cdcLoggerService;

    @Value("${cdc-logging.enabled}")
    private boolean enabled;
    @Value("${cdc-logging.env}")
    private String environment;


    @Inject
    public CDCLoggerHelper(CDCLoggerService service) {
        this.cdcLoggerService = service;
    }

    public void logErrorToCDC(Exception e, HttpRequest req) {
        if (enabled) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stackTrace = sw.toString();

            CDCLogEntry logEntry = new CDCLogEntry(environment, "DroolValidator_01", req.getHeaders().get("Host") + req.getPath(),  "CDCLoggerHelper", e.getMessage(), stackTrace);
            cdcLoggerService.sendError(logEntry);
        } else{
            logger.warn("CDC Logging Disabled");
        }
    }
}
