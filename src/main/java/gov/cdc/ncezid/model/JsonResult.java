package gov.cdc.ncezid.model;

import lombok.Data;

@Data
public class JsonResult {

    private String result;

    public JsonResult(String result) {
        this.result = result;
    }
}