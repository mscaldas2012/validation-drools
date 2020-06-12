package gov.cdc.ncezid.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import lombok.Data;

@Data
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ValidationIssue {

    private SeverityType severityType;

    private IssueType issueType;

    private IssueScopeType issueScopeType;

    private Integer rowNumber;

    private String fieldName;

    private Object fieldValue;

    private String issueDescription;

    public ValidationIssue() {
        initialize(SeverityType.UNKNOWN, IssueType.UNKNOWN, IssueScopeType.UNKNOWN, null, "", "", "");
    }

    public ValidationIssue(SeverityType severityType, IssueType issueType, IssueScopeType issueScopeType, Integer rowNumber, String fieldName, Object fieldValue, String issueDescription) {
        initialize(severityType, issueType, issueScopeType, rowNumber, fieldName, fieldValue, issueDescription);
    }

    public ValidationIssue(ValidationIssue rhs) {
        initialize(rhs.severityType, rhs.issueType, rhs.issueScopeType, rhs.rowNumber, rhs.fieldName, rhs.fieldValue, rhs.issueDescription);
    }

    public void initialize(SeverityType severityType, IssueType issueType, IssueScopeType issueScopeType, Integer rowNumber, String fieldName, Object fieldValue, String issueDescription) {
        this.severityType = severityType;
        this.issueType = issueType;
        this.issueScopeType = issueScopeType;
        this.rowNumber = rowNumber;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.issueDescription = issueDescription;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Issue: severity=")
                .append(severityType)
                .append(", type=")
                .append(issueType)
                .append(", scope=")
                .append(issueScopeType)
                .append(", row=")
                .append(rowNumber)
                .append(", field=")
                .append(fieldName)
                .append(", issue=")
                .append(issueDescription)
                .append("\n");
        return sb.toString();
    }
}
