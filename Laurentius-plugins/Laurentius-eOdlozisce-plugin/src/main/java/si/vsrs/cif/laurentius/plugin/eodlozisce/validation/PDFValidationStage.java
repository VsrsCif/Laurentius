package si.vsrs.cif.laurentius.plugin.eodlozisce.validation;

import org.apache.pdfbox.preflight.Format;
import org.apache.pdfbox.preflight.PreflightDocument;
import org.apache.pdfbox.preflight.parser.PreflightParser;
import si.laurentius.lce.sign.pdf.ValidateSignatureUtils;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.util.Objects;

public class PDFValidationStage implements ValidationStage<File> {

    public enum ErrorCodes implements ValidationErrorCode {
        NOT_PDF_A("Not a PDF/A file"),
        PDF_NOT_SIGNED("The leading PDF file is not signed"),
        PDF_SIGNATURES_NOT_VALID("The leading PDF file has invalid signatures"),
        UNSUPPORTED_PDF_SIGNATURE("The leading PDF file has an unsupported signature");

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

    public PDFValidationStage() {
    }

    @Override
    public ValidationResult validate(File data) {


        ValidationResult validationResult = new ValidationResult();
        PreflightDocument preflightDocument = null;
        try {
            PreflightParser parser = new PreflightParser(data);
            parser.parse();
            preflightDocument = parser.getPreflightDocument();
            preflightDocument.validate();
            Format specification = preflightDocument.getSpecification();
            org.apache.pdfbox.preflight.ValidationResult result = preflightDocument.getResult();

            ValidateSignatureUtils validateSignatureUtils = new ValidateSignatureUtils();

            if(!specification.getFname().startsWith("PDF/A")) {
                validationResult.addWarning(ErrorCodes.NOT_PDF_A);
            }

            if(preflightDocument.getSignatureDictionaries().size() < 1) {
                validationResult.addWarning(ErrorCodes.PDF_NOT_SIGNED);
            }

            if(validateSignatureUtils.validateSignatures(data).stream().filter(Objects::isNull).mapToInt(p -> 1).sum() > 0) {
                validationResult.addWarning(ErrorCodes.UNSUPPORTED_PDF_SIGNATURE);
            }

            if(validateSignatureUtils.validateSignatures(data).stream().filter(Objects::nonNull).filter(p -> !p.isIsSignatureValid()).mapToInt(p -> 1).sum() > 0) {
                validationResult.addWarning(ErrorCodes.PDF_SIGNATURES_NOT_VALID);
            }

        } catch (IOException | CertificateException | SignatureException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                preflightDocument.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return validationResult;
    }
}
