package si.vsrs.cif.laurentius.plugin.eodlozisce.validation;

public interface ValidationErrorCode {
    void setCustomMessage(String message);
    String getCustomMessage();
    String asText();
}
