package gov.cdc.ncezid.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CDCLogEntry {
    private final String appName = "DPRP Data Submission Portal";
    private final String source = "Validation-Drools";
    private  String environment;

    private String token;
    private String url;
    private String targetSite;
    private String exceptionMessage;
    private String stackTrace;

    public CDCLogEntry(String env, String token, String url, String targetSite, String exceptionMessage, String stackTrace) {
        this.environment = env;
        this.token = token;
        this.url = url;
        this.targetSite = targetSite;
        this.exceptionMessage = exceptionMessage;
        this.stackTrace = stackTrace;
    }
}
