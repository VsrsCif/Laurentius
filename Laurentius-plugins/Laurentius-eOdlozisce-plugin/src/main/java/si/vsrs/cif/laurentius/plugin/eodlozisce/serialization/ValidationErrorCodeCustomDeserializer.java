package si.vsrs.cif.laurentius.plugin.eodlozisce.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import si.vsrs.cif.laurentius.plugin.eodlozisce.validation.ValidationErrorCode;

import java.io.IOException;
import java.util.Arrays;

public class ValidationErrorCodeCustomDeserializer extends JsonDeserializer<ValidationErrorCode> {
    @Override
    public ValidationErrorCode deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        try {
            Class<ValidationErrorCode> clazz = (Class<ValidationErrorCode>) Class.forName(node.get("@class").asText());
            String name = node.get("name").asText();
            String message = node.get("message").asText();

            final ValidationErrorCode validationErrorCode = Arrays.stream(clazz.getEnumConstants())
                    .map(clazz::cast)
                    .filter(o -> o.name().equals(name))
                    .findAny()
                    .orElseThrow(() -> new IllegalArgumentException("unknown enum " + name + " for class " + clazz.getName()));
            validationErrorCode.setCustomMessage(message);

            return validationErrorCode;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
