/*
 * Copyright 2015, Supreme Court Republic of Slovenia
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package si.laurentius.lce.sign.xml;

import java.io.*;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.*;
import static java.util.Collections.singletonList;
import javax.xml.crypto.*;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import si.laurentius.commons.exception.SEDSecurityException;
import static si.laurentius.commons.exception.SEDSecurityException.SEDSecurityExceptionCode.InitializeException;
import si.laurentius.commons.utils.Utils;
import org.etsi.uri._01903.v1_1.QualifyingProperties;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.lce.DigestMethodCode;
import static java.io.File.createTempFile;
import static java.lang.Class.forName;
import static java.lang.System.getProperty;
import static javax.xml.crypto.dsig.XMLSignatureFactory.getInstance;

/**
 * XML signature utils for XAdES-T signature
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public class XMLSignatureUtils {

  private static final String ID_PREFIX_REF = "ref";
  private static final String ID_PREFIX_SIG = "sig";
  private static final String ID_PREFIX_SIG_VAL = "sig-val";

  private static final String ID_PREFIX_SIG_PROP = "sig-prop";
  private static final String XADES_NS = "http://uri.etsi.org/01903/v1.1.1#";

  public static final String XML_SIGNATURE_PROVIDER_PROP = "jsr105Provider";
  public static final String XML_SIGNATURE_PROVIDER_VALUE_1 =
      "org.jcp.xml.dsig.internal.dom.XMLDSigRI";
  public static final String XML_SIGNATURE_PROVIDER_VALUE_2 =
      "org.apache.jcp.xml.dsig.internal.dom.XMLDSigRI";

  /**
   * Logger
   */
  private static final SEDLogger LOG = new SEDLogger(XMLSignatureUtils.class);
  private final XAdESignatureBuilder mXAdESBuilder = new XAdESignatureBuilder();

  XMLTimeStamp mTimeStampServer = null;
  // String mstrTimeStampServerUrl = "http://ts.si-tsa.sigov.si:80/verificationserver/timestamp";
  String mstrResultLogFolder = getProperty("java.io.tmpdir");
  String mstrTimeStampServerUrl = null;

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
   * @param certPrivateKey - signing key
   * @param sigParentElement - parent node where signature is stored
   * @param strIds - sign elements with given id in a list
   * @param digestMethodCode - signature digest method
   * @param sigMethod - signature algorithm - ex: http://www.w3.org/2000/09/xmldsig#rsa-sha1
   * (javax.xml.crypto.dsig.SignatureMethod.SHA1)
   * @throws SEDSecurityException
   */
  public void createXAdESEnvelopedSignature(KeyStore.PrivateKeyEntry certPrivateKey,
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
    } catch (MarshalException | XMLSignatureException ex) {
      throw new SEDSecurityException(
          SEDSecurityException.SEDSecurityExceptionCode.CreateSignatureException, ex,
          "Error signing document:" + ex.getMessage());
    }
    LOG.logEnd(t, strIds);
  }

  /**
   *
   * @param sigNode
   * @throws MarshalException
   * @throws XMLSignatureException
   * @throws SEDSecurityException / public void validateXAdESEnvelopedSignature(Node sigNode) throws
   * MarshalException, XMLSignatureException, SEDSecurityException { /* // Create a DOM
   * XMLSignatureFactory that will be used to unmarshal the // document containing the XMLSignature
   * XMLSignatureFactory fac = getXMLSignatureFactory();
   *
   * // Create a DOMValidateContext and specify a KeyValue KeySelector // and document context
   * DOMValidateContext valContext = new DOMValidateContext(new XAdESSigX509KeySelector(), ndVal);
   *
   * // unmarshal the XMLSignature XMLSignature signature = fac.unmarshalXMLSignature(valContext);
   * signature.getKeyInfo().getContent();
   *
   * // Validate the XMLSignature (generated above) //The REQUIRED steps of core validation include
   * // (1) reference validation, the verification of the digest contained in each Reference // in
   * SignedInfo, and // (2) the cryptographic signature validation of the signature calculated over
   * SignedInfo. boolean coreValidity = signature.validate(valContext);
   *
   * // Check core validation status if (!coreValidity) { StringWriter sw = new StringWriter();
   * sw.append("Core validation for signature is invalid!\n"); boolean sv =
   * signature.getSignatureValue().validate(valContext); sw.append("\tCryptographic validation of
   * the signature calculated over the SignedInfo is " + (sv ? "VALID" : "INVALID") + "!\n"); //
   * check the validation status of each Reference Iterator i =
   * signature.getSignedInfo().getReferences().iterator(); for (int j = 0; i.hasNext(); j++) {
   * Reference r = ((Reference) i.next()); boolean refValid = r.validate(valContext);
   * sw.append("\tRef[" + j + ", id: " + r.getURI() + "] validity status: " + refValid + "\n"); }
   * throw new XMLSignatureException(sw.toString()); }
   *
   * }
   *
   * private String calculateSignedValueDigest(String strSigValId, Document oDoc) throws
   * SEDSecurityException {
   *
   * String strDigest = null; try { Element el = oDoc.getElementById(strSigValId);
   *
   * Canonicalizer c = Canonicalizer.getInstance(INCLUSIVE); byte[] buff =
   * c.canonicalizeSubtree(el); MessageDigest md = MessageDigest.getInstance("SHA-1"); md.digest();
   * // reset digest strDigest = getEncoder().encodeToString(md.digest(buff)); } catch
   * (NoSuchAlgorithmException | CanonicalizationException | InvalidCanonicalizerException ex) {
   *
   * throw new SEDSecurityException(
   * SEDSecurityException.SEDSecurityExceptionCode.CreateSignatureException, ex, "Error Calculating
   * digest document:" + ex.getMessage());
   *
   * }
   * return strDigest; }
   *
   * /*
   * private String calculateSignedValueDigest(String strSigValId, XMLSignatureFactory fac, KeyInfo
   * ki, KeyStore.PrivateKeyEntry certPrivateKey, Document oDoc) { String strDigest = null;
   *
   * try {
   *
   * // todo calculate signature direct!! this si bad :> DocumentBuilderFactory dbf =
   * DocumentBuilderFactory.newInstance(); dbf.setNamespaceAware(true); Document doc =
   * dbf.newDocumentBuilder().newDocument(); Node n =
   * doc.adoptNode(oDoc.getDocumentElement().cloneNode(true)); doc.appendChild(n);
   * setIdnessToElemetns(n); Reference ref_TS = fac.newReference("#" + strSigValId,
   * fac.newDigestMethod(SHA1, null), null, null, null);
   *
   * List<Reference> lstRef1 = new ArrayList<>(); lstRef1.add(ref_TS);
   *
   * // Create the SignedInfo SignedInfo si = fac.newSignedInfo(
   * fac.newCanonicalizationMethod(INCLUSIVE, (C14NMethodParameterSpec) null),
   * fac.newSignatureMethod(RSA_SHA1, null), lstRef1,
   * "SignedInfo1-39EB3E08-97ED-48AF-969B-ABFD697FC5FA");
   *
   * XMLSignature sig2 = fac.newXMLSignature(si, ki);
   *
   * DOMSignContext dsc = new DOMSignContext(certPrivateKey.getPrivateKey(),
   * doc.getDocumentElement()); dsc.setProperty("javax.xml.crypto.dsig.cacheReference", TRUE);
   *
   * try {
   *
   * // Marshal, generate (and sign) the enveloped signature sig2.sign(dsc); strDigest =
   * getEncoder().encodeToString(ref_TS.getDigestValue());
   *
   * InputStream is = ref_TS.getDigestInputStream(); byte[] bf = new byte[is.available()];
   * is.read(bf);
   *
   * } catch (MarshalException | XMLSignatureException ex) { LOG.error("SvevSignatureUtils.", ex); }
   * catch (Exception ex) { LOG.error("SvevSignatureUtils.", ex); }
   *
   * } catch (ParserConfigurationException | NoSuchAlgorithmException |
   * InvalidAlgorithmParameterException ex) { LOG.error("SvevSignatureUtils.", ex); } return
   * strDigest; }
   *
   * private String calculateSignedValueDigest(String strSigValId, Document oDoc) {
   *
   * String strDigest = null; try { Element el = oDoc.getElementById(strSigValId);
   *
   * Canonicalizer c = Canonicalizer.getInstance(INCLUSIVE); byte[] buff =
   * c.canonicalizeSubtree(el); MessageDigest md = MessageDigest.getInstance("SHA-1"); md.digest();
   * // reset digest strDigest = getEncoder().encodeToString(md.digest(buff)); } catch
   * (NoSuchAlgorithmException ex) { LOG.error("NoSuchAlgorithmException.", ex); } catch
   * (CanonicalizationException ex) { LOG.error("CanonicalizationException.", ex); } catch
   * (InvalidCanonicalizerException ex) { LOG.error("InvalidCanonicalizerException.", ex); } return
   * strDigest; }
   *
   *
   * private boolean isSignatureTimestamp(Node sigNode) { return sigNode != null &&
   * sigNode.getParentNode() != null &&
   * XADES_XMLTimeStamp.equals(sigNode.getParentNode().getNodeName()) &&
   * XADES_NS.equals(sigNode.getParentNode().getNamespaceURI());
   *
   * }
   */
  /**
   *
   * @param fDoc
   * @throws SEDSecurityException
   * @throws XMLSignatureException
   * @throws MarshalException
   *
   * public void validateXmlDSigSignature(File fDoc) throws SEDSecurityException,
   * XMLSignatureException, MarshalException { FileInputStream fis = null; try { fis = new
   * FileInputStream(fDoc); validateXmlDSigSignature(fis); } catch (FileNotFoundException ex) {
   * logError("SvevSignatureUtils.validateXmlDSigSignature: FileNotFoundException", ex.getMessage(),
   * getTime(), ex); throw new SEDSecurityException(XMLParseException, ex, ex.getMessage()); }
   * finally { if (fis != null) { try { fis.close(); } catch (IOException ingore) { } } } }
   */
  /**
   *
   * @param is
   * @throws SEDSecurityException
   * @throws XMLSignatureException
   * @throws MarshalException
   *
   * public void validateXmlDSigSignature(InputStream is) throws SEDSecurityException,
   * XMLSignatureException, MarshalException { long t =
   * logStart("SvevSignatureUtils.validateXmlDSigSignature");
   *
   * // Instantiate the document to be validated DocumentBuilderFactory dbf =
   * DocumentBuilderFactory.newInstance(); dbf.setNamespaceAware(true); Document doc; try { doc =
   * dbf.newDocumentBuilder().parse(is); } catch (ParserConfigurationException ex) {
   * logError("SvevSignatureUtils.validateXmlDSigSignature: ParserConfigurationException",
   * ex.getMessage(), t, ex); throw new SEDSecurityException(XMLParseException, ex,
   * ex.getMessage()); } catch (SAXException ex) {
   * logError("SvevSignatureUtils.validateXmlDSigSignature: SAXException", ex.getMessage(), t, ex);
   * throw new SEDSecurityException(XMLParseException, ex, ex.getMessage()); } catch (IOException
   * ex) { logError("SvevSignatureUtils.validateXmlDSigSignature: IOException", ex.getMessage(), t,
   * ex); throw new SEDSecurityException(XMLParseException, ex, ex.getMessage()); }
   * setIdnessToElemetns(doc.getDocumentElement());
   *
   * // Find Signature element NodeList nl = doc.getElementsByTagNameNS(XMLNS, "Signature"); if
   * (nl.getLength() == 0) { logError("SvevSignatureUtils.validateXmlDSigSignature", "No signature
   * found", t, null); throw new SEDSecurityException(SignatureNotFound, "No signature found"); }
   * for (int index = 0; index < nl.getLength(); index++) {
   *
   * validateSignature(nl.item(index)); }
   *
   * }
   */
  /**
   *
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
}
