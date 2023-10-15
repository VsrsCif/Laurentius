package si.vsrs.cif.laurentius.plugin.eodlozisce.validation;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;

public class SchemaValidationStage implements ValidationStage<InputStream> {

    private final Schema schema;

    public enum ErrorCodes implements ValidationErrorCode {
        MISSING_PART("Missing part"),
        INVALID_XML("Invalid XML");

        private String message;

        ErrorCodes(String message) {
            this.message = message;
        }

        @Override
        public void setCustomMessage(String message) {
            this.message = message;
        }

        @Override
        public String getCustomMessage() {
            return this.message;
        }

        @Override
        public String asText() {
            return message;
        }
    }

    public SchemaValidationStage(InputStream schemaStream) throws SAXException {
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        this.schema = sf.newSchema(new StreamSource(schemaStream));
    }

    public SchemaValidationStage(Source[] schemas) throws SAXException {
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        this.schema = sf.newSchema(schemas);
    }

    @Override
    public ValidationResult validate(InputStream data) {
        Validator validator = this.schema.newValidator();

        ValidationResult validationResult = new ValidationResult();
        validator.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException exception) throws SAXException {
                validationResult.add(new ValidationOutput(
                        ValidationOutput.Severity.WARNING, ErrorCodes.INVALID_XML));
            }

            @Override
            public void error(SAXParseException exception) throws SAXException {
                validationResult.add(new ValidationOutput(
                        ValidationOutput.Severity.ERROR, ErrorCodes.INVALID_XML));
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
                validationResult.add(new ValidationOutput(
                        ValidationOutput.Severity.ERROR, ErrorCodes.INVALID_XML));
            }
        });
        try {
            validator.validate(new StreamSource(data));
        } catch (SAXException | IOException e) {
            validationResult.add(new ValidationOutput(
                    ValidationOutput.Severity.ERROR, ErrorCodes.INVALID_XML));
        }

        return validationResult;
    }
}
