package si.vsrs.cif.laurentius.plugin.eodlozisce.validation;

public class ValidationOutput {
    public enum ValidateionSeverity {
        ERROR, WARNING
    }
    private ValidateionSeverity severity;
    private ValidationErrorCode code;

    public ValidationOutput(ValidateionSeverity severity, ValidationErrorCode code) {
        this.severity = severity;
        this.code = code;
    }

    public ValidateionSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(ValidateionSeverity severity) {
        this.severity = severity;
    }

    public ValidationErrorCode getCode() {
        return code;
    }

    public void setCode(ValidationErrorCode code) {
        this.code = code;
    }

    @Override
    public int hashCode() {
        return this.code.hashCode() + this.severity.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this.code.equals(((ValidationOutput)obj).code) && this.severity.equals(((ValidationOutput)obj).severity);
    }
}