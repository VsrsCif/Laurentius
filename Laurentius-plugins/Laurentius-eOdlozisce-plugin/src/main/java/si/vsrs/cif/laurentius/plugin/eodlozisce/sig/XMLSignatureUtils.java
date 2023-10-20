package si.vsrs.cif.laurentius.plugin.eodlozisce.sig;

import org.etsi.uri._01903.v1_1.QualifyingProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.vsrs.cif.laurentius.plugin.eodlozisce.validation.ValidationOutput;
import si.vsrs.cif.laurentius.plugin.eodlozisce.validation.XmlSignatureValidationStage;

import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.security.KeyException;
import java.security.KeyStore;
import java.security.Provider;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static java.io.File.createTempFile;
import static java.lang.Class.forName;
import static java.lang.System.getProperty;
import static java.util.Collections.singletonList;
import static javax.xml.crypto.dsig.XMLSignatureFactory.getInstance;
import static si.laurentius.commons.exception.SEDSecurityException.SEDSecurityExceptionCode.InitializeException;

public class XMLSignatureUtils {


    private static final String ID_PREFIX_SIG = "sig";
    private static final String ID_PREFIX_SIG_VAL = "sig-val";

    private static final String ID_PREFIX_SIG_PROP = "sig-prop";

    public static final String XML_SIGNATURE_PROVIDER_PROP = "jsr105Provider";
    public static final String XML_SIGNATURE_PROVIDER_VALUE_1 =
            "org.jcp.xml.dsig.internal.dom.XMLDSigRI";
    public static final String XML_SIGNATURE_PROVIDER_VALUE_2 =
            "org.apache.jcp.xml.dsig.internal.dom.XMLDSigRI";


    public static final String SHA256_WITH_RSA_URI = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";
    private static final DigestMethodCode DIGEST_METHOD_CODE = DigestMethodCode.SHA256;
    private static final String SIGNATURE_REASON = "Prispela vloga";
    /**
     * Logger
     */
    private static final SEDLogger LOG = new SEDLogger(XMLSignatureUtils.class);
    private final XAdESignatureBuilder mXAdESBuilder = new XAdESignatureBuilder();

//    XMLTimeStamp mTimeStampServer = null;
//    // String mstrTimeStampServerUrl = "http://ts.si-tsa.sigov.si:80/verificationserver/timestamp";
//    String mstrResultLogFolder = getProperty("java.io.tmpdir");
//    String mstrTimeStampServerUrl = null;

    /**
     * Method returs XMLSignatureFactory for providerName
     *
     * @param providerName - provider name:
     * @return XMLSignatureFactory or null if class for provider name not exists
     * @throws SEDSecurityException - if error occurs while init provider factory
     */
    public XMLSignatureFactory getXMLSignatureFactory(String providerName)
            throws SEDSecurityException {
        long l = LOG.logStart(providerName);

        XMLSignatureFactory fac = null;
        if (providerName == null || providerName.isEmpty()) {
            LOG.logWarn(l, "Null XMLSignatureFactory provider!", null);
            return fac;
        }

        Class c;
        try {
            c = forName(providerName);
        } catch (ClassNotFoundException ex) {
            LOG.formatedWarning("XMLSignatureFactory for '%s'. Error: '%s'", providerName, ex.getMessage());
            return fac;
        }

        try {
            fac = getInstance("DOM", (Provider) c.newInstance());
        } catch (InstantiationException | IllegalAccessException ex) {
            String msg = "Error occured while initializing XMLSignatureFactory for: '" + providerName +
                    "'.";
            throw new SEDSecurityException(InitializeException, ex, msg);
        }
        return fac;
    }

    /**
     * Method returs XMLSignatureFactory for name defined in system property 'jsr105Provider'. If
     * systempropery is not defined "org.apache.jcp.xml.dsig.internal.dom.XMLDSigRI" is setted. If
     * factory for org.apache.jcp.xml.dsig.internal.dom.XMLDSigRI is not found next try is:
     * 'org.jcp.xml.dsig.internal.dom.XMLDSigRI'
     *
     * @return @throws SEDSecurityException
     */
    public XMLSignatureFactory getXMLSignatureFactory()
            throws SEDSecurityException {
        long l = LOG.logStart();

        XMLSignatureFactory fac = null;
        String providerName = getProperty(XML_SIGNATURE_PROVIDER_PROP);
        if (providerName != null) {
            fac = getXMLSignatureFactory(providerName);
        }
        // try org.jcp.xml.dsig.internal.dom.XMLDSigRI
        if (fac == null) {
            fac = getXMLSignatureFactory(XML_SIGNATURE_PROVIDER_VALUE_1);
        }
        // try org.apache.jcp.xml.dsig.internal.dom.XMLDSigRI
        if (fac == null) {
            fac = getXMLSignatureFactory(XML_SIGNATURE_PROVIDER_VALUE_2);
        }
        LOG.logEnd(l);
        return fac;
    }

    /**
     * Method signs elements given in list strIds. Enveloped signature object is stored in
     * sigParentElement
     *
     * @param certPrivateKey   - signing key
     * @param sigParentElement - parent node where signature is stored
     * @param strIds           - sign elements with given id in a list
     * @param digestMethodCode - signature digest method
     * @param sigMethod        - signature algorithm - ex: http://www.w3.org/2000/09/xmldsig#rsa-sha1
     *                         (javax.xml.crypto.dsig.SignatureMethod.SHA1)
     * @return
     * @throws SEDSecurityException
     */
    public Document createXAdESEnvelopedSignature(KeyStore.PrivateKeyEntry certPrivateKey,
                                                  Element sigParentElement, List<String> strIds, DigestMethodCode digestMethodCode,
                                                  String sigMethod, String signatureReason)
            throws SEDSecurityException {
        long t = LOG.logStart(strIds);

        // get XMLSignatureFactory implemenation
        XMLSignatureFactory fac = getXMLSignatureFactory();
        // generate signature id's
        String strSigId = Utils.getUUID(ID_PREFIX_SIG);
        String strSigValId = Utils.getUUID(ID_PREFIX_SIG_VAL);
        String strSigPropId = Utils.getUUID(ID_PREFIX_SIG_PROP);
        // add  XAdES Signed properties id to list for signing
        strIds.add(strSigPropId);

        // get certificate
        X509Certificate cert = (X509Certificate) certPrivateKey.getCertificate();
        // Create the XAdES QualifyingProperties
        QualifyingProperties qp = mXAdESBuilder.createXAdESQualifyingProperties(strSigId, strSigValId,
                strSigPropId, cert, digestMethodCode, signatureReason, null, null);
        // add signature propertis to sig
        Document doc = sigParentElement.getOwnerDocument();
        XMLStructure content = mXAdESBuilder.objectToXMLStructure(doc, qp);
        XMLObject xoQualifyingProperties = fac.newXMLObject(Collections.singletonList(content),
                null, null, null);

        // Create the SignedInfo
        SignedInfo si = mXAdESBuilder.createSignedInfo(strIds, digestMethodCode.getAlgorithmURI(),
                sigMethod, fac);
        // Create the KeyInfo
        KeyInfo ki = mXAdESBuilder.createXAdESKeyInfo(cert, fac);

        // Create the XMLSignature (but don't sign it yet)
        XMLSignature signature =
                fac.newXMLSignature(si, ki, singletonList(xoQualifyingProperties), strSigId, strSigValId);
        // Create the DOMSignContext
        DOMSignContext dsc = new DOMSignContext(certPrivateKey.getPrivateKey(), sigParentElement);

        // Marshal, generate (and sign) the enveloped signature
        mXAdESBuilder.setIdnessToElemetns(doc.getDocumentElement());
        try {
            signature.sign(dsc);
            LOG.logEnd(t, strIds);
            return doc;
        } catch (MarshalException | XMLSignatureException ex) {
            throw new SEDSecurityException(
                    SEDSecurityException.SEDSecurityExceptionCode.CreateSignatureException, ex,
                    "Error signing document:" + ex.getMessage());
        }
    }


//    Document signedDocument = utils.createXAdESEnvelopedSignature(privateKeyEntry, doc.getDocumentElement(), sigIds, DIGEST_METHOD_CODE, SIGNATURE_ALGORITHM, SIGNATURE_REASON);
    public Document signXmlDocument(KeyStore.PrivateKeyEntry certPrivateKey,
                                    Document xmlDocument) throws SEDSecurityException {
        List<String> sigIds = new ArrayList<>();
        return createXAdESEnvelopedSignature(certPrivateKey, xmlDocument.getDocumentElement(), sigIds, DIGEST_METHOD_CODE, SHA256_WITH_RSA_URI, SIGNATURE_REASON);
    }


    public List<ValidationOutput> validateXAdESEnvelopedSignature(Document doc) throws Exception {
        List<ValidationOutput> validationOutputs = new ArrayList<>();
        NodeList nl =
                doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        if (nl.getLength() == 0) {
            validationOutputs.add(new ValidationOutput(ValidationOutput.Severity.WARNING, XmlSignatureValidationStage.ErrorCodes.SIGNATURE_NOT_FOUND));
            return validationOutputs;
        }
        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
        DOMValidateContext valContext = new DOMValidateContext(new KeyValueKeySelector(), nl.item(0));

        NodeList signedProperties = doc.getElementsByTagName("SignedProperties");
        if (signedProperties.getLength() > 0) {
            valContext.setIdAttributeNS((Element) signedProperties.item(0), null, "Id");
        }

        XMLSignature signature = fac.unmarshalXMLSignature(valContext);

        boolean coreValidity = signature.validate(valContext);

        if (!coreValidity) {
            XmlSignatureValidationStage.ErrorCodes invalidSignatureError = XmlSignatureValidationStage.ErrorCodes.INVALID_SIGNATURE;
            boolean sv = signature.getSignatureValue().validate(valContext);
            invalidSignatureError.setCustomMessage(invalidSignatureError.getCustomMessage() + " Signature validation status: " + sv);
            Iterator i = signature.getSignedInfo().getReferences().iterator();
            for (int j = 0; i.hasNext(); j++) {
                boolean refValid =
                        ((Reference) i.next()).validate(valContext);
                invalidSignatureError.setCustomMessage(invalidSignatureError.getCustomMessage() + "ref[" + j + "] validity status: " + refValid);
            }
            validationOutputs.add(new ValidationOutput(ValidationOutput.Severity.ERROR, invalidSignatureError));
        }
        return validationOutputs;
    }


    /**
     * @param in
     * @param logFolder
     * @param fileNamePrefix
     * @param fileNameSuffix
     * @return
     */
    public File writeToFile(InputStream in, String logFolder, String fileNamePrefix,
                            String fileNameSuffix) {
        long l = LOG.getTime();
        FileOutputStream out = null;
        File f = null;
        try {

            f = createTempFile(fileNamePrefix, fileNameSuffix, new File(logFolder));
            out = new FileOutputStream(f);
            byte[] buffer = new byte[1024];
            int len = in.read(buffer);
            while (len != -1) {
                out.write(buffer, 0, len);
                len = in.read(buffer);
            }
        } catch (IOException ex) {
            String strMessage =
                    "Error write to: '" + (f != null ? f.getAbsolutePath() : "null-file") + "' exception:" +
                            ex.getMessage();
            LOG.logError(l, strMessage, ex);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    String strMsg =
                            "Error closing file; '" + (f != null ? f.getAbsolutePath() : "null-file") +
                                    "' exception:" + ex.getMessage();
                    LOG.logWarn(l, strMsg, null);
                }
            }
        }
        return f;
    }

    private String getValueByTypeFromPrincipalDN(String dnRFC2253, String attributeType) {
        String[] dnParts = dnRFC2253.split(",");
        for (String dnSplit : dnParts) {
            if (dnSplit.trim().startsWith(attributeType)) {
                String[] cnSplits = dnSplit.trim().split("=");
                if (cnSplits[1] != null) {
                    return cnSplits[1].trim();
                }
            }
        }
        return "";
    }


    // TODO currently just copied from java example, can surely be optimized, moved to external class, etc.
    private static class KeyValueKeySelector extends KeySelector {
        public KeySelectorResult select(KeyInfo keyInfo, KeySelector.Purpose purpose, AlgorithmMethod method, XMLCryptoContext context) throws KeySelectorException {
            if (keyInfo == null) {
                throw new KeySelectorException("Null KeyInfo object!");
            }

            List list = keyInfo.getContent();
            for (int i = 0; i < list.size(); i++) {
                XMLStructure xmlStructure = (XMLStructure) list.get(i);
                PublicKey pk = null;
                if (xmlStructure instanceof KeyValue) {
                    try {
                        pk = ((KeyValue) xmlStructure).getPublicKey();
                    } catch (KeyException ke) {
                        throw new KeySelectorException(ke);
                    }
                } else if (xmlStructure instanceof X509Data) {
                    for (Object data : ((X509Data) xmlStructure).getContent()) {
                        if (data instanceof X509Certificate) {
                            pk = ((X509Certificate) data).getPublicKey();
                            if (pk == null) {
                                throw new KeySelectorException("No public key in certificate");
                            }
                        }
                    }
                }
                SignatureMethod sm = (SignatureMethod) method;
                if (pk != null && algEquals(sm.getAlgorithm(), pk.getAlgorithm())) {
                    return new SimpleKeySelectorResult(pk);
                }
            }
            throw new KeySelectorException("No KeyValue element found!");
        }

        //@@@FIXME: this should also work for key types other than DSA/RSA
        static boolean algEquals(String algURI, String algName) {
            if (algName.equalsIgnoreCase("DSA") &&
                    algURI.equalsIgnoreCase(SignatureMethod.DSA_SHA1)) {
                return true;
            } else if (algName.equalsIgnoreCase("RSA") &&
                    algURI.equalsIgnoreCase(SignatureMethod.RSA_SHA1)) {
                return true;
            } else if (algName.equalsIgnoreCase("RSA") && algURI.equalsIgnoreCase(SHA256_WITH_RSA_URI)) {
                return true;
            } else {
                return false;
            }
        }
    }

    private static class SimpleKeySelectorResult implements KeySelectorResult {
        private PublicKey pk;

        SimpleKeySelectorResult(PublicKey pk) {
            this.pk = pk;
        }

        public Key getKey() {
            return pk;
        }
    }

    public static String documentToString(Document doc) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        Writer stringWriter = new StringWriter();
        StreamResult streamResult = new StreamResult(stringWriter);
        transformer.transform(new DOMSource(doc), streamResult);
        return stringWriter.toString();
    }

    public Document parseDocument(Path documentPath) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        return dbf.newDocumentBuilder().parse(Files.newInputStream(documentPath));
    }

}
