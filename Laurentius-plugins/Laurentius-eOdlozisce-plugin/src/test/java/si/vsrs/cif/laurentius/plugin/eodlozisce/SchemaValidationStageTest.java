package si.vsrs.cif.laurentius.plugin.eodlozisce;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;
import si.vsrs.cif.laurentius.plugin.eodlozisce.validation.SchemaValidationStage;
import si.vsrs.cif.laurentius.plugin.eodlozisce.validation.ValidationResult;

public class SchemaValidationStageTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void simpleXMLIsValid() throws SAXException {
        SchemaValidationStage schemaValidationStage = new SchemaValidationStage();
        ValidationResult validated = schemaValidationStage.validate(
                getClass().getClassLoader().getResourceAsStream("schema_validation/test1.xml"));
        Assert.assertTrue(validated.getValidationOutputs().isEmpty());
    }

    @Test
    public void xmlIsInvalid() throws SAXException {
        SchemaValidationStage schemaValidationStage = new SchemaValidationStage();
        ValidationResult validated = schemaValidationStage.validate(
                getClass().getClassLoader().getResourceAsStream("schema_validation/test2.xml"));
        Assert.assertFalse(validated.getValidationOutputs().isEmpty());
        Assert.assertTrue(validated.getValidationOutputs().size() == 1);
    }
}
