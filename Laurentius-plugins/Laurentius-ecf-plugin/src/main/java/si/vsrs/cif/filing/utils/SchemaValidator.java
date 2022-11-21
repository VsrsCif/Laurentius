package si.vsrs.cif.filing.utils;

import org.xml.sax.SAXException;
import si.laurentius.commons.utils.SEDLogger;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.net.URL;



public class SchemaValidator {
    static final SEDLogger LOG = new SEDLogger(SchemaValidator.class);


    public static final String SCHEME_VLOGA = "/schemas/SplosnaVlogaV2.xsd";

    /**
     * thread safe validator
     */
    private static final ThreadLocal<Validator> validator = ThreadLocal.withInitial(() -> {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        URL xsdFilePath = SchemaValidator.class.getResource(SCHEME_VLOGA);
        try {
            Schema schema = schemaFactory.newSchema(xsdFilePath);
            // to be compliant, completely disable DOCTYPE declaration:
            schemaFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            //schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, StringUtils.EMPTY);
            //schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, StringUtils.EMPTY);
            return schema.newValidator();
        } catch (SAXException e) {
            throw new IllegalStateException("Unable to initialize 'SplosnaVlogaV2' schema validator.", e);
        }
    });

    private static Validator getValidator() {
        return validator.get();
    }

    public void validateXMLBySplosnaVloga(File file) throws IOException, SAXException {
        Validator validator = getValidator();
        LOG.log("got Validator : " + file.getAbsolutePath());
        try (InputStream stream = new FileInputStream(file)) {
            validator.validate(new StreamSource(stream));
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
