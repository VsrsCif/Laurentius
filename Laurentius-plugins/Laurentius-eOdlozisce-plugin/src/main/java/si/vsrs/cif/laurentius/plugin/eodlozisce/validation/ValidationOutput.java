package si.vsrs.cif.laurentius.plugin.eodlozisce.validation;

public class ValidationOutput {
    public enum ValidateionSeverity {
        ERROR, WARNING
    }
    private String message;
    private String location;
    private ValidateionSeverity severity;

    public ValidationOutput(ValidateionSeverity severity, String message, String location) {
        this.message = message;
        this.location = location;
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ValidateionSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(ValidateionSeverity severity) {
        this.severity = severity;
    }
}