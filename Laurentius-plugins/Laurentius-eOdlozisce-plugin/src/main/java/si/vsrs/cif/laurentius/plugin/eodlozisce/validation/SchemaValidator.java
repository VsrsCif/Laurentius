package si.vsrs.cif.laurentius.plugin.eodlozisce.validation;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;

public class SchemaValidator implements ValidationStage {

    private final Schema schema;

    public SchemaValidator(InputStream schemaStream) throws SAXException {
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        this.schema = sf.newSchema(new StreamSource(schemaStream));
    }

    public ValidationResult validate(InputStream data) {
        Validator validator = this.schema.newValidator();

        ValidationResult validationResult = new ValidationResult();
        validator.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException exception) throws SAXException {
                validationResult.add(new ValidationOutput(
                        ValidationOutput.ValidateionSeverity.WARNING,
                        exception.getMessage(),
                        exception.getLineNumber() + ":" + exception.getColumnNumber()));
            }

            @Override
            public void error(SAXParseException exception) throws SAXException {
                validationResult.add(new ValidationOutput(
                        ValidationOutput.ValidateionSeverity.ERROR,
                        exception.getMessage(),
                        exception.getLineNumber() + ":" + exception.getColumnNumber()));
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
                validationResult.add(new ValidationOutput(
                        ValidationOutput.ValidateionSeverity.ERROR,
                        exception.getMessage(),
                        exception.getLineNumber() + ":" + exception.getColumnNumber()));
            }
        });
        try {
            validator.validate(new StreamSource(data));
        } catch (SAXException | IOException e) {
            validationResult.add(new ValidationOutput(
                    ValidationOutput.ValidateionSeverity.ERROR,
                    e.getMessage(),
                    e.getStackTrace().toString()));
        }

        return validationResult;
    }
}
