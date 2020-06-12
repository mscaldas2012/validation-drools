package gov.cdc.ncezid.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
@JsonSerialize(using = CustomReportSerializer.class)
public class CustomReport {

    public Object content;

}