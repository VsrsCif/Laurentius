package si.vsrs.cif.laurentius.plugin.eodlozisce.validation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import si.sodisce.sheme.skupno.izmenjave.v1.ElektronskaOvojnica;
import si.sodisce.sheme.skupno.izmenjave.v1.ElektronskaPosiljkaTip;
import si.vsrs.cif.laurentius.plugin.eodlozisce.codes.CourtType;
import si.vsrs.cif.laurentius.plugin.eodlozisce.codes.FieldOfLawType;
import si.vsrs.cif.laurentius.plugin.eodlozisce.codes.RegisterType;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ApplicationValidationStage implements ValidationStage<ElektronskaOvojnica> {

    public enum ErrorCodes implements ValidationErrorCode {
        MISSING_COURT_CODE("Missing court code"),
        INVALID_COURT_CODE("Invalid court code"),
        INVALID_FIELD_OF_LAW("Invalid field of law"),
        INVALID_REGISTER("Invalid register"),
        MISSING_JOB_NUMBER("Missing job number"),
        MISSING_ISSUE_NUMBER("Missing issue number");

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
    }

    private List<CourtType> courtTypes;
    private List<String> courtCodes;
    private List<RegisterType> registerTypes;
    private List<String> registerCodes;
    private List<FieldOfLawType> fieldOfLawTypes;
    private List<String> fieldOfLawCodes;

    public ApplicationValidationStage() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.courtTypes = mapper.readValue(getClass().getClassLoader().getResourceAsStream("codes/sifrant-sodisca.json"), new TypeReference<List<CourtType>>() {});
            this.registerTypes = mapper.readValue(getClass().getClassLoader().getResourceAsStream("codes/sifrant-vpisniki.json"), new TypeReference<List<RegisterType>>() {});
            this.fieldOfLawTypes = mapper.readValue(getClass().getClassLoader().getResourceAsStream("codes/sifrant-pravna-podrocja.json"), new TypeReference<List<FieldOfLawType>>() {});
            this.courtCodes = this.courtTypes.stream().map(CourtType::getCode).collect(Collectors.toList());
            this.registerCodes = this.registerTypes.stream().map(RegisterType::getId).collect(Collectors.toList());
            this.fieldOfLawCodes = this.fieldOfLawTypes.stream().map(FieldOfLawType::getCode).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ValidationResult validate(ElektronskaOvojnica data) {
        ValidationResult validationResult = new ValidationResult();
        ElektronskaPosiljkaTip posiljka = data.getPosiljka();
        if(posiljka == null) {
            validationResult.addError(ErrorCodes.MISSING_COURT_CODE);
            return validationResult;
        }
        if(posiljka.getZadeva() == null) {
            validationResult.addError(ErrorCodes.MISSING_COURT_CODE);
            return validationResult;
        }
        if(posiljka.getZadeva().getSodisce() == null) {
            validationResult.addError(ErrorCodes.MISSING_COURT_CODE);
            return validationResult;
        }

        if(!courtCodes.contains(posiljka.getZadeva().getSodisce().getSifra())) {
            validationResult.addError(ErrorCodes.INVALID_COURT_CODE);
            return validationResult;
        }

        if(!registerCodes.contains(posiljka.getZadeva().getVpisnik())) {
            validationResult.addWarning(ErrorCodes.INVALID_REGISTER);
            return validationResult;
        }

        if(posiljka.getZadeva().getPravnoPodpodrocje() != null && !fieldOfLawCodes.contains(posiljka.getZadeva().getPravnoPodrocje().getSifra())) {
            validationResult.addWarning(ErrorCodes.INVALID_FIELD_OF_LAW);
            return validationResult;
        }

        if(posiljka.getZadeva().getOpravilnaStevilka() == null) {
            validationResult.addWarning(ErrorCodes.MISSING_COURT_CODE);
        }

        if(posiljka.getZadeva().getStevilka() == null) {
            validationResult.addWarning(ErrorCodes.MISSING_ISSUE_NUMBER);
        }

        return validationResult;
    }
}
