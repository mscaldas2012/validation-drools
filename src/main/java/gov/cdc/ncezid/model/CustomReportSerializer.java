package gov.cdc.ncezid.model;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class CustomReportSerializer extends JsonSerializer<CustomReport> 
{
    @Override
    public void serialize(CustomReport value, JsonGenerator jgen, 
                    SerializerProvider provider) 
                    throws IOException, JsonProcessingException 
    {
        jgen.writeObject(value.content);
    }

}