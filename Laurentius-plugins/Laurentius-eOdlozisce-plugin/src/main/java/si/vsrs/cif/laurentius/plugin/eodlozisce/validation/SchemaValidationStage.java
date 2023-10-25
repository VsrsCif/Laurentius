package si.vsrs.cif.laurentius.plugin.eodlozisce.validation;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import si.vsrs.cif.laurentius.plugin.eodlozisce.EOdlozisceTask;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;

public class SchemaValidationStage implements ValidationStage<InputStream> {

    private static final Source[] schemas = new Source[]{
            new StreamSource(EOdlozisceTask.class.getClassLoader().getResource("schemas/CivilniElementi.xsd").toExternalForm()),
            new StreamSource(EOdlozisceTask.class.getClassLoader().getResource("schemas/CivilniSkupnoTipi.xsd").toExternalForm()),
            new StreamSource(EOdlozisceTask.class.getClassLoader().getResource("schemas/KazenskiElementi.xsd").toExternalForm()),
            new StreamSource(EOdlozisceTask.class.getClassLoader().getResource("schemas/KazenskiSkupnoTipi.xsd").toExternalForm()),
            new StreamSource(EOdlozisceTask.class.getClassLoader().getResource("schemas/SkupnoElementi.xsd").toExternalForm()),
            new StreamSource(EOdlozisceTask.class.getClassLoader().getResource("schemas/SkupnoIzmenjaveTipi.xsd").toExternalForm()),
            new StreamSource(EOdlozisceTask.class.getClassLoader().getResource("schemas/SkupnoSkupnoTipi.xsd").toExternalForm()),
            new StreamSource(EOdlozisceTask.class.getClassLoader().getResource("schemas/SkupnoSplosnoTipi.xsd").toExternalForm()),
            new StreamSource(EOdlozisceTask.class.getClassLoader().getResource("schemas/XAdES-1.1.1.xsd").toExternalForm()),
            new StreamSource(EOdlozisceTask.class.getClassLoader().getResource("schemas/xmldsig-core-schema.xsd").toExternalForm())
    };
    private final Schema schema;

    public enum ErrorCodes implements ValidationErrorCode {
        MISSING_METADATA_XML("Missing metadata XML"),
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

        @Override
        public void localize(ResourceBundle errorsBundle) {
            this.message = errorsBundle.getString(this.name());
        }
    }

    public SchemaValidationStage() throws SAXException {
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
