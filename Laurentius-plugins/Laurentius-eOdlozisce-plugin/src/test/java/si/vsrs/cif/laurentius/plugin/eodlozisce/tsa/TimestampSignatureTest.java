package si.vsrs.cif.laurentius.plugin.eodlozisce.tsa;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import si.src.setcce.sign.wsclient.VerifyResult;
import si.vsrs.cif.laurentius.plugin.eodlozisce.sig.XMLSignatureUtils;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.nio.file.Paths;

public class TimestampSignatureTest {

    @Test
    public void timestampTest() throws ParserConfigurationException, IOException, SAXException, TransformerException, TimestampException {
        final String serverUrl = "http://raweb-test.sigov.si:8080/proxsignMJU_WS/setcceSignServerService";
        final String signatureNamespace = "http://www.w3.org/2000/09/xmldsig#";
        final int timeout = 8000;
        TimeStampServiceImpl tsaService = new TimeStampServiceImpl(serverUrl, signatureNamespace, timeout);

        XMLSignatureUtils utils = new XMLSignatureUtils();
        Document doc = utils.parseDocument(Paths.get("src/test/resources/xml_sig_validation/signed.xml"));
        final String documentString = XMLSignatureUtils.documentToString(doc);

        // Run timestamping ...
        final String timestampedXml = tsaService.timeStampXml(documentString);
        System.out.println(timestampedXml);

        // Run verify timestamp
        final VerifyResult verifyResult = tsaService.verifyTimestampSignature(timestampedXml);
        System.out.printf("TSA verify result: resultCode: %s, errorMessage: %s, numberOfItems: %d%n",
                verifyResult.getResultCode(), verifyResult.getErrMsg(), verifyResult.getNumberItems());
    }
}
