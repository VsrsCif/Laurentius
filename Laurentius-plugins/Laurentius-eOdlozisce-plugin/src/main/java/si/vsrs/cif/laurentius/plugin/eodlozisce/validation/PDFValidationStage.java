package si.vsrs.cif.laurentius.plugin.eodlozisce.validation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.preflight.Format;
import org.apache.pdfbox.preflight.PreflightDocument;
import org.apache.pdfbox.preflight.parser.PreflightParser;
import si.laurentius.lce.sign.pdf.SignatureInfo;
import si.laurentius.lce.sign.pdf.ValidateSignatureUtils;
import si.sodisce.sheme.skupno.izmenjave.v1.ElektronskaOvojnica;
import si.sodisce.sheme.skupno.izmenjave.v1.ElektronskaPosiljkaTip;
import si.vsrs.cif.laurentius.plugin.eodlozisce.codes.CourtType;
import si.vsrs.cif.laurentius.plugin.eodlozisce.codes.FieldOfLawType;
import si.vsrs.cif.laurentius.plugin.eodlozisce.codes.RegisterType;

import javax.activation.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.stream.Collectors;

public class PDFValidationStage implements ValidationStage<File> {

    public enum ErrorCodes implements ValidationErrorCode {
        NOT_PDF_A("Not a PDF/A file");

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
            result.isValid();

            ValidateSignatureUtils validateSignatureUtils = new ValidateSignatureUtils();

            List<PDSignature> signatureDictionaries = preflightDocument.getSignatureDictionaries();

            boolean isPdfA = specification.getFname().startsWith("PDF/A");
            List<SignatureInfo> signatureInfos = validateSignatureUtils.validateSignatures(data);

            System.out.println("signatureInfos = " + signatureInfos);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
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
