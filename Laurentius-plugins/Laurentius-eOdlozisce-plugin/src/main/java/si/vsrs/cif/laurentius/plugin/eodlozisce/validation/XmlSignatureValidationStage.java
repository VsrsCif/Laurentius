package si.vsrs.cif.laurentius.plugin.eodlozisce.validation;

import org.w3c.dom.Document;
import si.vsrs.cif.laurentius.plugin.eodlozisce.sig.XMLSignatureUtils;

import java.nio.file.Path;
import java.util.List;

public class XmlSignatureValidationStage implements ValidationStage<Path> {
    private final XMLSignatureUtils signatureUtils;

    public XmlSignatureValidationStage() {
        this.signatureUtils = new XMLSignatureUtils();

    }

    @Override
    public ValidationResult validate(Path partPath) {
        ValidationResult result = new ValidationResult();
        try {
            Document document = signatureUtils.parseDocument(partPath);
            List<ValidationOutput> validationOutputs = signatureUtils.validateXAdESEnvelopedSignature(document);
            result.addAll(validationOutputs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public enum ErrorCodes implements ValidationErrorCode {
        SIGNATURE_NOT_FOUND("Signature element not found"),
        INVALID_SIGNATURE("Signature not valid");

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
}
