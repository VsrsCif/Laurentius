package si.vsrs.cif.laurentius.plugin.eodlozisce.validation;

public class ValidationOutput {
    public enum Severity {
        ERROR, WARNING
    }
    private Severity severity;
    private ValidationErrorCode code;

    public ValidationOutput(Severity severity, ValidationErrorCode code) {
        this.severity = severity;
        this.code = code;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
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

    public boolean isError() {
        return this.severity.equals(Severity.ERROR);
    }

    public boolean isWarning() {
        return this.severity.equals(Severity.WARNING);
    }
}