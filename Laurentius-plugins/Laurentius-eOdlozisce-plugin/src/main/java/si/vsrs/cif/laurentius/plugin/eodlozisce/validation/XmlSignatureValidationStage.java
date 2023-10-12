package si.vsrs.cif.laurentius.plugin.eodlozisce.validation;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import si.vsrs.cif.laurentius.plugin.eodlozisce.sig.XMLSignatureUtils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class XmlSignatureValidationStage implements ValidationStage<InputStream> {
    private final XMLSignatureUtils signatureUtils;

    public XmlSignatureValidationStage() {
        this.signatureUtils = new XMLSignatureUtils();

    }

    @Override
    public ValidationResult validate(InputStream data) {
        ValidationResult result = new ValidationResult();
        try {
            Document document = parseInputStreamToDocument(data);
            List<ValidationOutput> validationOutputs = signatureUtils.validateXAdESEnvelopedSignature(document);
            result.addAll(validationOutputs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private Document parseInputStreamToDocument(InputStream data) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();

        return documentBuilder.parse(data);
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
