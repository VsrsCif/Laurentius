package si.vsrs.cif.laurentius.plugin.eodlozisce.validation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import si.vsrs.cif.laurentius.plugin.eodlozisce.serialization.ValidationErrorCodeCustomDeserializer;
import si.vsrs.cif.laurentius.plugin.eodlozisce.serialization.ValidationErrorCodeCustomSerializer;

import java.util.ResourceBundle;

@JsonSerialize(using = ValidationErrorCodeCustomSerializer.class)
@JsonDeserialize(using = ValidationErrorCodeCustomDeserializer.class)
public interface ValidationErrorCode {
    @JsonProperty("message")
    void setCustomMessage(String message);
    @JsonProperty("message")
    String getCustomMessage();
    String name();
    String asText();
    void localize(ResourceBundle errorsBundle);
}
