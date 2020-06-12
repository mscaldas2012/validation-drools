package gov.cdc.ncezid.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder(value ={ "timestamp", "overallStatus", "minioStatus", "minioErrorMessage"})
public class HealthReceipt {
    private String minioErrorMessage;
    private HEALTH_STATUS overallStatus;
    private HEALTH_STATUS minioStatus;
    private String timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT);

}
