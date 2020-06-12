package gov.cdc.ncezid.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class CSVFile extends Metadata {

    private List<UnformattedRow> rows = new ArrayList<UnformattedRow>();

    public void addRow(UnformattedRow row) {
        rows.add(row);
    }

}
