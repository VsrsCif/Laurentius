package si.vsrs.cif.laurentius.plugin.eodlozisce;

import org.junit.Assert;
import org.junit.Test;
import si.vsrs.cif.laurentius.plugin.eodlozisce.validation.ValidationResult;
import si.vsrs.cif.laurentius.plugin.eodlozisce.validation.XmlSignatureValidationStage;

import java.nio.file.Path;
import java.nio.file.Paths;

public class XmlSignatureValidationStageTest {
    @Test
    public void xmlSignatureIsValid() {
        XmlSignatureValidationStage xmlSignatureValidationStage = new XmlSignatureValidationStage();
//        InputStream data = getClass().getClassLoader().getResourceAsStream("xml_sig_validation/signed.xml");
        Path path = Paths.get("src/test/resources/xml_sig_validation/signed.xml");


//        String dataString = new BufferedReader(new InputStreamReader(data, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
//        System.out.println(dataString);

        ValidationResult validationResult = xmlSignatureValidationStage.validate(path);

        validationResult.getValidationOutputs().forEach((error) -> {
            System.out.println("Validation output = [" + error.getSeverity() + "] " + error.getCode().getCustomMessage());
        });

        Assert.assertTrue(validationResult.getValidationOutputs().isEmpty());
    }
}
