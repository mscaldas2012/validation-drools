package gov.cdc.ncezid.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class JsonFile extends Metadata {

    @JsonIgnore
    private String jsonString; // so it can be parsed again in the rules
}