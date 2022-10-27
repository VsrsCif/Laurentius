package si.vsrs.cif.filing.utils;

import org.xml.sax.SAXException;
import si.laurentius.commons.utils.SEDLogger;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class SchemaValidator {
    static final SEDLogger LOG = new SEDLogger(SchemaValidator.class);


    public static final String SCHEME_VLOGA = "/xsd/SplosnaVlogaV2.xsd";

    /**
     * thread safe validator
     */
    private static final ThreadLocal<Validator> validator = ThreadLocal.withInitial(() -> {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        URL xsdFilePath = SchemaValidator.class.getResource(SCHEME_VLOGA);
        try {
            Schema schema = schemaFactory.newSchema(xsdFilePath);
            Validator vaInstance = schema.newValidator();
            vaInstance.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            vaInstance.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            return vaInstance;
        } catch (SAXException e) {
            throw new IllegalStateException("Unable to initialize 'SplosnaVlogaV2' schema validator.", e);
        }
    });

    private static Validator getValidator() {
        return validator.get();
    }

    public boolean isValid(File file) {
        Validator validator = getValidator();
        try (InputStream stream = new FileInputStream(file)) {
            validator.validate(new StreamSource(stream));
            return true;
        } catch (SAXException | IOException e) {
            LOG.logError("Error occurred while validating the XML!" + e.getMessage(), e);
            return false;
        }
    }

    public boolean isValid(InputStream xmlStream) throws IOException {
        Validator validator = getValidator();
        try {
            validator.validate(new StreamSource(xmlStream));
            return true;
        } catch (SAXException e) {
            return false;
        }
    }

}
