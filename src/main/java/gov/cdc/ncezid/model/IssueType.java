package gov.cdc.ncezid.model;

public enum IssueType {
    UNKNOWN,
    STRUCTURE,    // issues with the structure of the file; e.g. wrong number of columns
    FORMAT,       // issues with the format of a field; e.g. expected a Date, but found a number
    INVALID,      // field has a valid format, but has an invalid value; e.g. expected range of the field is exceeded
    QUALITY,      // field value is valid, but suspicious
    INCONSISTENT, // field value is inconsistent with other values; e.g. cross row validation expects value of 1 on all rows, but found a 2
    MISSING       // field value is expected, but missing
}
