package si.vsrs.cif.laurentius.plugin.eodlozisce;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import si.vsrs.cif.laurentius.plugin.eodlozisce.validation.ValidationOutput;
import si.vsrs.cif.laurentius.plugin.eodlozisce.validation.ValidationResult;

import static org.hamcrest.CoreMatchers.is;
import static si.vsrs.cif.laurentius.plugin.eodlozisce.validation.FilingValidationStage.ErrorCodes.MISSING_COURT_CODE;
import static si.vsrs.cif.laurentius.plugin.eodlozisce.validation.SchemaValidationStage.ErrorCodes.INVALID_XML;
import static si.vsrs.cif.laurentius.plugin.eodlozisce.validation.SchemaValidationStage.ErrorCodes.MISSING_METADATA_XML;

public class JsonErrorReportTest {

    ObjectMapper objectMapper = new ObjectMapper();

    private final ValidationOutput invalidXmlValidationOutput = new ValidationOutput(ValidationOutput.Severity.ERROR, INVALID_XML);
    private final ValidationOutput missingMetadataValidationOutput = new ValidationOutput(ValidationOutput.Severity.WARNING, MISSING_METADATA_XML);
    private final ValidationOutput missingCourtCodeValidationOutput = new ValidationOutput(ValidationOutput.Severity.WARNING, MISSING_COURT_CODE);
    private final ValidationResult validationResult = new ValidationResult();

    @Test
    public void serializeAndDeserializeReport() throws JsonProcessingException {
        validationResult.add(invalidXmlValidationOutput);
        validationResult.add(missingMetadataValidationOutput);
        validationResult.add(missingCourtCodeValidationOutput);

        final String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(validationResult);
        final ValidationResult deserializedValidationResult = objectMapper.readValue(json, ValidationResult.class);

        Assert.assertEquals(validationResult.getValidationOutputs().size(), deserializedValidationResult.getValidationOutputs().size());
        Assert.assertThat(deserializedValidationResult.getValidationOutputs().contains(invalidXmlValidationOutput), is(true));
        Assert.assertThat(deserializedValidationResult.getValidationOutputs().contains(missingMetadataValidationOutput), is(true));
        Assert.assertThat(deserializedValidationResult.getValidationOutputs().contains(missingCourtCodeValidationOutput), is(true));

    }

    @Test
    public void ignoreDuplicatedValidationOutputs() {
        validationResult.add(invalidXmlValidationOutput);
        validationResult.add(missingMetadataValidationOutput);
        validationResult.add(missingCourtCodeValidationOutput);
        final int validationOutputsCount = validationResult.getValidationOutputs().size();

        final ValidationOutput anotherInvalidXmlValidationOutput = new ValidationOutput(ValidationOutput.Severity.ERROR, INVALID_XML);
        final ValidationResult anotherValidationResult = new ValidationResult();
        anotherValidationResult.add(anotherInvalidXmlValidationOutput);
        validationResult.chain(anotherValidationResult);
        Assert.assertThat(validationResult.getValidationOutputs().size(), is(validationOutputsCount));
    }
}
