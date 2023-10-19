package si.vsrs.cif.laurentius.plugin.eodlozisce.validation;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ValidationResult {
    private List<ValidationOutput> validationOutputs = new LinkedList<>();

    public void add(ValidationOutput validationOutput) {
        this.validationOutputs.add(validationOutput);
    }

    public void addAll(List<ValidationOutput> validationOutputs) {
        this.validationOutputs.addAll(validationOutputs);
    }
    public void addError(ValidationErrorCode code) {
        this.validationOutputs.add(new ValidationOutput(ValidationOutput.Severity.ERROR, code));
    }
    public void addWarning(ValidationErrorCode code) {
        this.validationOutputs.add(new ValidationOutput(ValidationOutput.Severity.WARNING, code));
    }
    public List<ValidationOutput> getValidationOutputs() {
        return this.validationOutputs;
    }

    public ValidationResult chain(ValidationResult validationResult) {
        final List<ValidationOutput> newValidationOutputs = validationResult.validationOutputs
                .stream().filter(validationOutput -> !this.validationOutputs.contains(validationOutput))
                .collect(Collectors.toList());
        this.validationOutputs.addAll(newValidationOutputs);
        return this;
    }

    public boolean hasValidationIssues() {
        return !this.validationOutputs.isEmpty();
    }
}
