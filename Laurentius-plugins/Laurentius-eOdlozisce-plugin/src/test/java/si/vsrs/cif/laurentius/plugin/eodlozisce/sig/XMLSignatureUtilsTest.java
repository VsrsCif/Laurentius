package si.vsrs.cif.laurentius.plugin.eodlozisce.sig;

import org.junit.Test;
import org.w3c.dom.Document;
import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;

import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Files;
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

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc =
                dbf.newDocumentBuilder().parse(Files.newInputStream(Paths.get("src/test/resources/xml_sig_validation/test1.xml")));

        List<String> sigIds = new ArrayList<>();
        KeyStore.PrivateKeyEntry privateKeyEntry = new KeyStore.PrivateKeyEntry(keyGen.getPrivateKey(), chain);

        // SIGN DOCUMENT
        Document signedDocument = utils.createXAdESEnvelopedSignature(privateKeyEntry, doc.getDocumentElement(), sigIds, DIGEST_METHOD_CODE, SIGNATURE_ALGORITHM, SIGNATURE_REASON);


        // VALIDATE DOCUMENT
        utils.validateXAdESEnvelopedSignature(signedDocument);


    }
}