package si.vsrs.cif.laurentius.plugin.eodlozisce.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import si.vsrs.cif.laurentius.plugin.eodlozisce.validation.ValidationErrorCode;

import java.io.IOException;

public class ValidationErrorCodeCustomSerializer extends JsonSerializer<ValidationErrorCode> {

    @Override
    public void serialize(ValidationErrorCode obj, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("@class");
        gen.writeString(obj.getClass().getName());
        gen.writeFieldName("name");
        gen.writeString(obj.name());
        gen.writeFieldName("message");
        gen.writeString(obj.getCustomMessage());
        gen.writeEndObject();
    }
}
