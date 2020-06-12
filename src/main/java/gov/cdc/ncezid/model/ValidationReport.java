package gov.cdc.ncezid.model;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import lombok.Data;

@Data
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ValidationReport {

    private ValidationReportSummary summary;

    private List<ValidationIssue> details;

    public ValidationReport(List<ValidationIssue> errors) {
        this.summary = new ValidationReportSummary(errors);
        if (errors != null)
            // Make a deep copy
            this.details = errors.stream().map(val -> new ValidationIssue(val)).collect(Collectors.toList());
    }
}