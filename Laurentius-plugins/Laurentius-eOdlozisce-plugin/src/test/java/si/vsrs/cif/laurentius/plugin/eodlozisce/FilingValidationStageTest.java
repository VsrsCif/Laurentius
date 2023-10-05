package si.vsrs.cif.laurentius.plugin.eodlozisce;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import si.sodisce.sheme.skupno.izmenjave.v1.ElektronskaOvojnica;
import si.sodisce.sheme.skupno.skupno.v1.SifrantTip;
import si.vsrs.cif.laurentius.plugin.eodlozisce.validation.FilingValidationStage;
import si.vsrs.cif.laurentius.plugin.eodlozisce.validation.ValidationOutput;
import si.vsrs.cif.laurentius.plugin.eodlozisce.validation.ValidationResult;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class FilingValidationStageTest {

    private ElektronskaOvojnica elektronskaOvojnica = new ElektronskaOvojnica();

    private JAXBContext jc = null;
    private Unmarshaller unmarshaller = null;
    private ElektronskaOvojnica elektronskaOvojnica1 = null;

    @Before
    public void setUp() throws Exception {
        this.jc = JAXBContext.newInstance(ElektronskaOvojnica.class);
        this.unmarshaller = this.jc.createUnmarshaller();
        this.elektronskaOvojnica = (ElektronskaOvojnica) this.unmarshaller.unmarshal(getClass().getClassLoader().getResourceAsStream("application_validation/test1.xml"));
        SifrantTip sifrantTip = new SifrantTip();
        sifrantTip.setSifra(this.fInput);
        this.elektronskaOvojnica.getPosiljka().getZadeva().setSodisce(sifrantTip);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Parameterized.Parameters
    public static Collection casesToTest() {
        return Arrays.asList(new Object[][] {
                { "S01", new ArrayList<ValidationOutput>()},
                { "S02", new ArrayList<ValidationOutput>()},
                { "S99", Arrays.asList(new ValidationOutput[]{new ValidationOutput(ValidationOutput.ValidateionSeverity.ERROR, FilingValidationStage.ErrorCodes.INVALID_COURT_CODE)})}
        });
    }

    @Parameterized.Parameter()
    public String fInput;

    @Parameterized.Parameter(1)
    public List<ValidationOutput> fExpected;

    // This test will run 4 times since we have 5 parameters defined
    @Test
    public void testPrimeNumberChecker() {
        FilingValidationStage applicationValidationStage = new FilingValidationStage();
        ValidationResult validated = applicationValidationStage.validate(this.elektronskaOvojnica);
        Assert.assertEquals(this.fExpected, validated.getValidationOutputs());
    }
}
