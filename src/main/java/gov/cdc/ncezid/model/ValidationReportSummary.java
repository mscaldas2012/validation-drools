package gov.cdc.ncezid.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import lombok.Data;

@Data
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ValidationReportSummary {

    private long numIssues;

    private long numErrors;

    private long numWarnings;

    private long numStructureIssues;

    private long numFormatIssues;

    private long numInvalidValueIssues;

    private long numQualityIssues;

    private long numInconsistentValueIssues;

    private long numMissingValueIssues;

    private long numFileIssues;

    private long numFieldIssues;

    private long numCrossFieldIssues;

    private long numCrossRowIssues;

    public ValidationReportSummary(List<ValidationIssue> issues) {
        if (issues != null) {
            this.numIssues = issues.size();

            this.numErrors = issues.stream()
                .filter(issue -> issue.getSeverityType() == SeverityType.ERROR)
                .count();

            this.numWarnings = issues.stream()
                .filter(issue -> issue.getSeverityType() == SeverityType.WARNING)
                .count();

            this.numStructureIssues = issues.stream()
                .filter(issue -> issue.getIssueType() == IssueType.STRUCTURE)
                .count();

            this.numFormatIssues = issues.stream()
                .filter(issue -> issue.getIssueType() == IssueType.FORMAT)
                .count();

            this.numInvalidValueIssues = issues.stream()
                .filter(issue -> issue.getIssueType() == IssueType.INVALID)
                .count();

            this.numQualityIssues = issues.stream()
                .filter(issue -> issue.getIssueType() == IssueType.QUALITY)
                .count();

            this.numInconsistentValueIssues = issues.stream()
                .filter(issue -> issue.getIssueType() == IssueType.INCONSISTENT)
                .count();

            this.numMissingValueIssues = issues.stream()
                .filter(issue -> issue.getIssueType() == IssueType.MISSING)
                .count();

            this.numFileIssues = issues.stream()
                .filter(issue -> issue.getIssueScopeType() == IssueScopeType.FILE)
                .count();

            this.numFieldIssues = issues.stream()
                .filter(issue -> issue.getIssueScopeType() == IssueScopeType.FIELD)
                .count();

            this.numCrossFieldIssues = issues.stream()
                .filter(issue -> issue.getIssueScopeType() == IssueScopeType.CROSS_FIELD)
                .count();

            this.numCrossRowIssues = issues.stream()
                .filter(issue -> issue.getIssueScopeType() == IssueScopeType.CROSS_ROW)
                .count();
        }
    }
}