package si.vsrs.cif.laurentius.plugin.eodlozisce.validation;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import si.src.setcce.sign.wsclient.VerifyResult;
import si.vsrs.cif.laurentius.plugin.eodlozisce.sig.XMLSignatureUtils;
import si.vsrs.cif.laurentius.plugin.eodlozisce.tsa.TimeStampService;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.nio.file.Path;

public class TimestampValidationStage implements ValidationStage<Path> {
    private final TimeStampService timeStampService;
    private final XMLSignatureUtils signatureUtils;


    public TimestampValidationStage(final TimeStampService timeStampService, final XMLSignatureUtils signatureUtils) {
        this.timeStampService = timeStampService;
        this.signatureUtils = signatureUtils;
    }

    @Override
    public ValidationResult validate(final Path partPath) {
        try {
            Document document = signatureUtils.parseDocument(partPath);
            final String documentString = XMLSignatureUtils.documentToString(document);
            final VerifyResult verifyResult = timeStampService.verifyTimestampSignature(documentString);

            if (verifyResult.getResultCode() == 200) {
                // this is pure guesswork
            } else {
                // what
            }
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }

        // TODO
        return new ValidationResult();
    }
}
