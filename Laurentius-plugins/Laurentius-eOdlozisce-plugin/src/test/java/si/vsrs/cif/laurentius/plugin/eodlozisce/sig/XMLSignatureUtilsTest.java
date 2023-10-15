package si.vsrs.cif.laurentius.plugin.eodlozisce.sig;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import si.vsrs.cif.laurentius.plugin.eodlozisce.validation.ValidationOutput;
import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class XMLSignatureUtilsTest {

    private static final String SIGNATURE_ALGORITHM = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";
    private static final DigestMethodCode DIGEST_METHOD_CODE = DigestMethodCode.SHA256;
    private static final String SIGNATURE_REASON = "Prispela vloga";

    @Test
    public void signAndValidateXML() throws Exception {
        // Get utils
        XMLSignatureUtils utils = new XMLSignatureUtils();

        CertAndKeyGen keyGen = new CertAndKeyGen("RSA", "SHA256WithRSA", null);
        keyGen.generate(1024);

        X509Certificate[] chain = new X509Certificate[1];
        chain[0] = keyGen.getSelfCertificate(new X500Name("CN=ROOT"), (long) 365 * 24 * 3600);

        Document doc = utils.parseDocument(Paths.get("src/test/resources/xml_sig_validation/test1.xml"));

        List<String> sigIds = new ArrayList<>();
        KeyStore.PrivateKeyEntry privateKeyEntry = new KeyStore.PrivateKeyEntry(keyGen.getPrivateKey(), chain);

        // SIGN DOCUMENT
        Document signedDocument = utils.createXAdESEnvelopedSignature(privateKeyEntry, doc.getDocumentElement(), sigIds, DIGEST_METHOD_CODE, SIGNATURE_ALGORITHM, SIGNATURE_REASON);

        try (FileOutputStream output = new FileOutputStream("src/test/resources/xml_sig_validation/test1_signed.xml")) {
            writeXml(signedDocument, output);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // VALIDATE DOCUMENT
        List<ValidationOutput> validationOutputs = utils.validateXAdESEnvelopedSignature(signedDocument);
        System.out.println(validationOutputs.size());

        // VALIDATE DOCUMENT IN WRITTEN FILE
        Document docFromFile = utils.parseDocument(Paths.get("src/test/resources/xml_sig_validation/test1_signed.xml"));
        List<ValidationOutput> validationOutputsFromFile = utils.validateXAdESEnvelopedSignature(docFromFile);
        System.out.println(validationOutputsFromFile.size());

        System.out.println("DOCUMENT FROM MEM:\n");
        System.out.println(XMLSignatureUtils.getNiceLyFormattedXMLDocument(signedDocument));

        System.out.println("DOCUMENT FROM FILE:\n");
        System.out.println(XMLSignatureUtils.getNiceLyFormattedXMLDocument(docFromFile));

        Assert.assertTrue(validationOutputs.isEmpty());
        Assert.assertTrue(validationOutputsFromFile.isEmpty());
    }

    private void writeXml(Document doc, OutputStream out) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(out);

        transformer.transform(source, result);
    }
}