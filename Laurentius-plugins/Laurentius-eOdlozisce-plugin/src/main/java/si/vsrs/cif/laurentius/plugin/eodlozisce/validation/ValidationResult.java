package si.vsrs.cif.laurentius.plugin.eodlozisce.validation;

import java.util.LinkedList;
import java.util.List;

public class ValidationResult {
    private List<ValidationOutput> validationOutputs = new LinkedList<>();

    public void add(ValidationOutput validationOutput) {
        this.validationOutputs.add(validationOutput);
    }

    public List<ValidationOutput> getValidationOutputs() {
        return this.validationOutputs;
    }

    public ValidationResult chain(ValidationResult validationResult) {
        this.validationOutputs.addAll(validationResult.validationOutputs);
        return this;
    }
}
