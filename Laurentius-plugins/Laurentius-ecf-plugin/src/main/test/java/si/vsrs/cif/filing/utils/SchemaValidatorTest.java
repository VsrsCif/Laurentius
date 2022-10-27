package si.vsrs.cif.filing.utils;

import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.junit.Assert.*;

public class SchemaValidatorTest {
    SchemaValidator testInstance = new SchemaValidator();
    @Test
    public void isValidSimple() throws IOException, SAXException {
        boolean result = testInstance.isValid(SchemaValidatorTest.class.getResourceAsStream("/xml/vloga.xml"));
        assertTrue(result);
    }

    @Test
    public void isValidSigned() throws IOException, SAXException {
        boolean result = testInstance.isValid(SchemaValidatorTest.class.getResourceAsStream("/xml/vloga-signed.xml"));
        assertTrue(result);
    }
    @Test
    public void isValidInvalid() throws IOException, SAXException {
        boolean result = testInstance.isValid(SchemaValidatorTest.class.getResourceAsStream("/xml/vloga-invalid.xml"));
        assertFalse(result);
    }

    @Test
    public void isValidMissingElement() throws IOException, SAXException {
        boolean result = testInstance.isValid(SchemaValidatorTest.class.getResourceAsStream("/xml/vloga-missing-element.xml"));
        assertFalse(result);
    }

}