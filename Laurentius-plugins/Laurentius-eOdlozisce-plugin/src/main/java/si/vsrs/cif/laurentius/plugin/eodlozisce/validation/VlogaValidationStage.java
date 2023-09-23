package si.vsrs.cif.laurentius.plugin.eodlozisce.validation;

import si.sodisce.sheme.skupno.izmenjave.v1.ElektronskaOvojnica;
import si.sodisce.sheme.skupno.izmenjave.v1.ElektronskaPosiljkaTip;

import java.util.Optional;

public class VlogaValidationStage implements ValidationStage<ElektronskaOvojnica> {

    private enum ErrorCodes implements ValidationErrorCode {
        MISSING_COURT_CODE("Missing court code");

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

    @Override
    public ValidationResult validate(ElektronskaOvojnica data) {
        ValidationResult validationResult = new ValidationResult();
        ElektronskaPosiljkaTip posiljka = data.getPosiljka();
        if(posiljka == null) {
            validationResult.addError(ErrorCodes.MISSING_COURT_CODE);
            return validationResult;
        }


        return null;
    }
}
