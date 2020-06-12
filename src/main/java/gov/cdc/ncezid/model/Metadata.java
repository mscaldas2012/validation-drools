package gov.cdc.ncezid.model;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class Metadata {
    
    private Map<String, Object> metadata = new HashMap<>();

    public void attach(String key, Object val) {
        metadata.put(key, val);
    }

    public <T> T get(String key, Class<T> clazz) {
        Object value = metadata.get(key);
        if (value != null) {
            try {
                if (value instanceof Double) {
                    Double val = (Double)(value);
                    if (clazz == Integer.class)
                        return clazz.cast(val.intValue());
                    else if (clazz == Long.class)
                        return clazz.cast(val.longValue());
                } else if (clazz == Date.class) {
                    try {
                        Date date = Date.from(Instant.parse((String) value));
                        return clazz.cast(date);
                    } catch (DateTimeParseException e) {
                    }
                }
                return clazz.cast(value);
            } catch(ClassCastException e) {
            }
        }
        return null;
    }
}