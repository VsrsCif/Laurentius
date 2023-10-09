package si.vsrs.cif.laurentius.plugin.eodlozisce.sig;

import org.w3c.dom.Document;

import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collections;

public class XmlSignatureExample {
    public static void main(String[] args) throws Exception {
        // Load the XML document
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(Files.newInputStream(Paths.get("src/test/resources/xml_sig_validation/test1.xml")));
//        Document document = db.parse(Files.newInputStream(Paths.get("test1.xml")));

        // Create a KeyStore for loading the private key and certificate
        KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(Files.newInputStream(Paths.get("src/test/resources/xml_sig_validation/keystore.jks")), "laurentius_test".toCharArray());

        // Get the private key and certificate from the KeyStore
        KeyStore.PrivateKeyEntry keyEntry = (KeyStore.PrivateKeyEntry) keystore.getEntry("laurentius_test", new KeyStore.PasswordProtection("laurentius_test".toCharArray()));
        PrivateKey privateKey = keyEntry.getPrivateKey();
        X509Certificate cert = (X509Certificate) keyEntry.getCertificate();

        // Create a DOMSignContext specifying the signing key and document
        DOMSignContext dsc = new DOMSignContext(privateKey, document.getDocumentElement());

        // Create a XMLSignatureFactory
        XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");

        // Create a Reference to the whole document
        Reference ref = factory.newReference("", factory.newDigestMethod(DigestMethod.SHA1, null),
                Collections.singletonList(factory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)), null, null);

        // Create a SignedInfo with the above Reference
        SignedInfo si = factory.newSignedInfo(factory.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE,
                        (C14NMethodParameterSpec) null), factory.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
                Collections.singletonList(ref));

        // Create a KeyInfo containing the X.509 certificate
        KeyInfoFactory kif = factory.getKeyInfoFactory();
        X509Data xd = kif.newX509Data(Collections.singletonList(cert));
        KeyInfo ki = kif.newKeyInfo(Collections.singletonList(xd));

        // Create the XMLSignature
        XMLSignature signature = factory.newXMLSignature(si, ki);

        // Sign the document
        signature.sign(dsc);

        // Serialize the signed document to a file
        OutputStream os = Files.newOutputStream(Paths.get("src/test/resources/xml_sig_validation/signed.xml"));
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer trans = tf.newTransformer();
        trans.transform(new DOMSource(document), new StreamResult(os));

        System.out.println("XML document signed successfully.");

        // Now, let's validate the signature
        DOMValidateContext valContext = new DOMValidateContext(new X509KeySelector(), document.getDocumentElement());
        XMLSignature signatureToValidate = factory.unmarshalXMLSignature(valContext);

        // Validate the signature
        boolean isValid = signatureToValidate.validate(valContext);

        if (isValid) {
            System.out.println("Signature is valid.");
        } else {
            System.out.println("Signature is NOT valid.");
        }
    }
}