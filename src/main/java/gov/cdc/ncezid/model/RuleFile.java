package gov.cdc.ncezid.model;

import lombok.Data;

@Data
public class RuleFile {

    private String filename;

    private String content;

    public RuleFile(String filename, String content) {
        this.filename = filename;
        this.content = content;
    }

}