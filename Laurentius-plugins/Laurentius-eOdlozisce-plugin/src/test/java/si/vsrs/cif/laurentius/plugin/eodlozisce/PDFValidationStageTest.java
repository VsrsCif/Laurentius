package si.vsrs.cif.laurentius.plugin.eodlozisce;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;
import si.vsrs.cif.laurentius.plugin.eodlozisce.validation.PDFValidationStage;
import si.vsrs.cif.laurentius.plugin.eodlozisce.validation.SchemaValidationStage;
import si.vsrs.cif.laurentius.plugin.eodlozisce.validation.ValidationResult;

import java.io.File;
import java.net.URL;

public class PDFValidationStageTest {
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void pdfAIsValid() throws SAXException {
        PDFValidationStage pdfValidationStage = new PDFValidationStage();

        URL resource = getClass().getClassLoader().getResource("pdf/pdfa.pdf");

        ValidationResult validated = pdfValidationStage.validate(new File(resource.getPath()));
        Assert.assertTrue(validated.getValidationOutputs().isEmpty());
    }
}
