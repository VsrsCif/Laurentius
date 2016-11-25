/*
 * Copyright 2016, Supreme Court Republic of Slovenia
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

import java.security.InvalidAlgorithmParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.keyinfo.X509IssuerSerial;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import org.etsi.uri._01903.v1_1.Any;
import org.etsi.uri._01903.v1_1.CertIDType;
import org.etsi.uri._01903.v1_1.DigestAlgAndValueType;
import org.etsi.uri._01903.v1_1.HashDataInfoType;
import org.etsi.uri._01903.v1_1.QualifyingProperties;
import org.etsi.uri._01903.v1_1.SignaturePolicyIdentifier;
import org.etsi.uri._01903.v1_1.SignatureProductionPlace;
import org.etsi.uri._01903.v1_1.SignedProperties;
import org.etsi.uri._01903.v1_1.SignedSignatureProperties;
import org.etsi.uri._01903.v1_1.SigningCertificate;
import org.etsi.uri._01903.v1_1.TimeStampType;
import org.etsi.uri._01903.v1_1.UnsignedProperties;
import org.etsi.uri._01903.v1_1.UnsignedSignatureProperties;
import org.w3._2000._09.xmldsig_.X509IssuerSerialType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import static org.w3c.dom.Node.ELEMENT_NODE;
import org.w3c.dom.NodeList;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.utils.Utils;
import si.laurentius.lce.DigestMethodCode;

/**
 * XASdES Builder. Builds XMLdSig and XASdES objects &lt;Signature ID?&gt; &lt;SignedInfo&gt;
 * &lt;CanonicalizationMethod/&gt; &lt;SignatureMethod/&gt; (&lt;Reference URI? &gt;
 * (&lt;Transforms&gt;)? &lt;DigestMethod&gt; &lt;DigestValue&gt; &lt;/Reference&gt;)+
 * &lt;/SignedInfo&gt; &lt;SignatureValue&gt; (&lt;KeyInfo&gt;)? (&lt;Object ID?&gt;)*
 * &lt;/Signature&gt;
 *
 * XML Advanced Electronic Signature (XAdES): Provides basic authentication and integrity protection
 * and satisfies the legal requirements for advanced electronic signatures as defined in the
 * European Directive [EU-DIR-ESIG]. But does not provide non-repudiation of its existence. This
 * form adds the following elements to [XMLDSIG]:
 * <ul><li><p>
 * QualifyingProperties</p>
 * <ul><li><p>
 * SignedProperties</p>
 * <ul><li><p>
 * SignedSignatureProperties</p>
 * <ul><li><p>
 * SigningTime</p></li>
 * <li><p>
 * SigningCertificate</p></li>
 * <li><p>
 * SignaturePolicyIdentifier</p></li>
 * <li><p>
 * SignatureProductionPlace?</p></li>
 * <li><p>
 * SignerRole?</p></li></ul></li>
 * <li><p>
 * SignedDataObjectProperties</p>
 * <ul><li><p>
 * DataObjectFormat*</p></li>
 * <li><p>
 * CommitmentTypeIndication*</p></li>
 * <li><p>
 * AllDataObjectsTimeStamp*</p></li>
 * <li><p>
 * IndividualDataObjectsTimeStamp*</p></li></ul></li></ul></li>
 * <li><p>
 * UnsignedProperties</p>
 * <ul><li><p>
 * UnsignedSignatureProperties</p>
 * <ul><li><p>
 * CounterSignature*</p></li>
 * <li><p>
 * SignatureTimeStamp*</p> - for xades-t</li>
 * </ul></li></ul></li></ul></li></ul>
 *
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public class XAdESignatureBuilder {

  private static final String ID_PREFIX_SIG_INF = "sig-inf";

  /**
   * Methods creates empty UnsignedProperties for Timestamp as part of XAdESQualifyingProperties.
   * Timestamp is setted in UnsignedProperties/UnsignedSignatureProperties/TimeStampType
   * Signature/Object/QualifyingProperties/*UnsignedProperties/UnsignedSignatureProperties/SignatureTimeStamp/XMLTimeStamp*
   *
   *
   * @param signUriId id for TimeStampType
   * @param timestamp - timestamp object
   * @return
   */
  public UnsignedProperties createUnsignedProperties(String signUriId, Any timestamp) {
    UnsignedProperties uns = new UnsignedProperties();
    uns.setUnsignedSignatureProperties(new UnsignedSignatureProperties());
    if (timestamp != null) {
      TimeStampType tt = new TimeStampType();
      uns.getUnsignedSignatureProperties().getSignatureTimeStamps().add(tt);
      HashDataInfoType ht = new HashDataInfoType();
      ht.setUri("#" + signUriId);
      tt.getHashDataInfos().add(ht);
      tt.setXMLTimeStamp(timestamp);
    }
    return uns;
  }

  /**
   * Method creates XMLDSig References elements
   *
   * @param lstIDS - list of ids to reference,
   * @param digestMethodAlg - digest metgod example: "http://www.w3.org/2000/09/xmldsig#sha1" from
   * DigestMethod.SHA1
   * @param fac - XMLSignatureFactory
   * @return List of XMLdSig references
   * @throws SEDSecurityException
   */
  public List<Reference> createReferenceList(List<String> lstIDS, String digestMethodAlg,
      XMLSignatureFactory fac)
      throws SEDSecurityException {
    List<Reference> lstRef = new ArrayList<>();
    try {
      DigestMethod dm = fac.newDigestMethod(digestMethodAlg, null);
      lstIDS.stream().forEach((String sId) -> {
        lstRef.add(fac.newReference("#" + sId, dm));
      });
    } catch (NoSuchAlgorithmException ex) {
      throw new SEDSecurityException(SEDSecurityException.SEDSecurityExceptionCode.NoSuchAlgorithm,
          ex, ex.getMessage());
    } catch (InvalidAlgorithmParameterException ex) {
      throw new SEDSecurityException(
          SEDSecurityException.SEDSecurityExceptionCode.CreateSignatureException,
          ex, ex.getMessage());
    }
    return lstRef;
  }

  /**
   * Method creates SignedInfo with CanonicalizationMethod
   * http://www.w3.org/TR/2001/REC-xml-c14n-20010315" and SignatureMethod:
   *
   * @param lstIDS - list of ids to reference,
   * @param refDigestMethodAlg - digest metgod example: "http://www.w3.org/2000/09/xmldsig#sha1"
   * from DigestMethod.SHA1
   * @param sigAlgorithm - the URI identifying the signature algorithm as
   * http://www.w3.org/2000/09/xmldsig#rsa-sha1 SignatureMethod.RSA_SHA1
   * @param fac - XMLSignatureFactory
   * @return
   * @throws SEDSecurityException
   */
  public SignedInfo createSignedInfo(List<String> lstIDS, String refDigestMethodAlg,
      String sigAlgorithm, XMLSignatureFactory fac)
      throws SEDSecurityException {
    SignedInfo si = null;
    try {
      List<Reference> lstRef = createReferenceList(lstIDS, refDigestMethodAlg, fac);
      si =
          fac.newSignedInfo(fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE,
              (C14NMethodParameterSpec) null),
              fac.newSignatureMethod(sigAlgorithm, null),
              lstRef, Utils.getUUID(ID_PREFIX_SIG_INF));
    } catch (NoSuchAlgorithmException ex) {
      throw new SEDSecurityException(SEDSecurityException.SEDSecurityExceptionCode.NoSuchAlgorithm,
          ex, ex.getMessage());
    } catch (InvalidAlgorithmParameterException ex) {
      throw new SEDSecurityException(
          SEDSecurityException.SEDSecurityExceptionCode.CreateSignatureException,
          ex, ex.getMessage());
    }
    return si;
  }

  /**
   * Method creates Signature/Object/QualifyingProperties/*SignedProperties* for signed certificate
   *
   * @param strSigPropId - signed properties id
   * @param cert, signing certificate
   * @param dmc digest method code (JCA provider code and W3c - URI)
   * @param signatureReason - value for: SignaturePolicyIdentifier/SignaturePolicyImplied - The
   * signature policy is a set of rules for the creation and validation ofan electronic signature,
   * under which the signature can be determined to be valid. A given legal/contractual context may
   * recognize a particular signature policy as meeting its requirements.
   * @param sigCity - city where signature was created
   * @param sigCountryName - country name where signature was created
   * @return XAdES data structure: SignedProperties
   * @throws si.laurentius.commons.exception.SEDSecurityException
   */
  public SignedProperties createSignedProperties(String strSigPropId, X509Certificate cert,
      DigestMethodCode dmc, String signatureReason, String sigCity, String sigCountryName)
      throws SEDSecurityException {
    SignedProperties sp = new SignedProperties();
    try {
      sp.setId(strSigPropId);
      SigningCertificate scert = new SigningCertificate();
      CertIDType sit = new CertIDType();
      DigestAlgAndValueType dt = new DigestAlgAndValueType();

      MessageDigest md = MessageDigest.getInstance(dmc.getJcaCode()); // "SHA-1"

      byte[] der = cert.getEncoded();
      md.update(der);
      dt.setDigestValue(md.digest());
      dt.setDigestMethod(new org.w3._2000._09.xmldsig_.DigestMethod());
      dt.getDigestMethod().setAlgorithm(dmc.getAlgorithmURI()); // uri for sha
      sit.setCertDigest(dt);
      sit.setIssuerSerial(new X509IssuerSerialType());
      sit.getIssuerSerial().setX509IssuerName(cert.getIssuerDN().getName());
      sit.getIssuerSerial().setX509SerialNumber(cert.getSerialNumber());
      SignedSignatureProperties ssp = new SignedSignatureProperties();
      ssp.setSigningTime(Calendar.getInstance().getTime());
      ssp.setSigningCertificate(scert);
      ssp.setSignaturePolicyIdentifier(new SignaturePolicyIdentifier());
      ssp.getSignaturePolicyIdentifier().setSignaturePolicyImplied(signatureReason);

      if (!Utils.isEmptyString(sigCity) || !Utils.isEmptyString(sigCountryName)) {
        ssp.setSignatureProductionPlace(new SignatureProductionPlace());
        ssp.getSignatureProductionPlace().setCity(sigCity);
        ssp.getSignatureProductionPlace().setCountryName(sigCountryName);
      }
      
      scert.getCerts().add(sit);
      sp.setSignedSignatureProperties(ssp);
    } catch (CertificateEncodingException | NoSuchAlgorithmException ex) {
      throw new SEDSecurityException(
          SEDSecurityException.SEDSecurityExceptionCode.CreateSignatureException,
          ex, ex.getMessage());
    }
    return sp;
  }

  /**
   * Method creates KeyInfo data from certificate used for signing
   *
   * @param cert - Signing certificate
   * @param fac - Signature implementation
   * @return KeyInfo object
   */
  public KeyInfo createXAdESKeyInfo(X509Certificate cert, XMLSignatureFactory fac) {
    KeyInfoFactory kif = fac.getKeyInfoFactory();
    // add certificate to signature:
    X509IssuerSerial x509IssuerSerial =
        kif.newX509IssuerSerial(cert.getIssuerDN().getName(), cert.getSerialNumber());
    List x509 = new ArrayList();
    x509.add(cert);
    x509.add(x509IssuerSerial);
    X509Data x509Data = kif.newX509Data(x509);
    // x509Data.getContent().add(x509IssuerSerial);
    List items = new ArrayList();
    items.add(x509Data);
    return kif.newKeyInfo(items);
  }

  /**
   * Method creates XAdESQualifyingProperties. Object QualifyingProperties must be stored into
   * XMLdSIg Signature/Object element.
   *
   * @param sigId - signature id to which QualifyingProperties targets
   * @param strSigValId - id for UnsignedProperties element.
   * @param strSigPropId - id for created SignedProperties (part of QualifyingProperties) which must
   * be signed
   * @param cert, signing certificate
   * @param dmc digest method code (JCA provider code and W3c - URI)
   * @param signaturePolicy - value for: SignaturePolicyIdentifier/SignaturePolicyImplied - The
   * signature policy is a set of rules for the creation and validation ofan electronic signature,
   * under which the signature can be determined to be valid. A given legal/contractual context may
   * recognize a particular signature policy as meeting its requirements.
   * @param sigCity - city where signature was created
   * @param sigCountryName - country name where signature was created
   *
   * @return
   * @throws SEDSecurityException
   */
  public QualifyingProperties createXAdESQualifyingProperties(String sigId, String strSigValId,
      String strSigPropId, X509Certificate cert, DigestMethodCode dmc, String signaturePolicy,
      String sigCity, String sigCountryName)
      throws SEDSecurityException {

    QualifyingProperties qt = new QualifyingProperties();

    qt.setTarget("#" + sigId);
    qt.setSignedProperties(createSignedProperties(strSigPropId, cert, dmc, signaturePolicy,
        sigCity, sigCountryName));
    qt.setUnsignedProperties(createUnsignedProperties(strSigValId, null));

    return qt;
  }

  /**
   * Sets all nodes Id parameter as ID attribute for XMLSignatureFactory to find.
   *
   * @param n - document node
   */
  public void setIdnessToElemetns(Node n) {
    if (n.getNodeType() == ELEMENT_NODE) {
      Element e = (Element) n;
      if (e.hasAttribute("Id")) {
        e.setIdAttribute("Id", true);
      } else if (e.hasAttribute("id")) {
        e.setIdAttribute("id", true);
      }
      if (e.hasAttribute("ID")) {
        e.setIdAttribute("ID", true);
      }
      NodeList l = e.getChildNodes();
      for (int i = 0; i < l.getLength(); i++) {
        setIdnessToElemetns(l.item(i));
      }
    }
  }

  public XMLStructure objectToXMLStructure(Document doc, Object obj)
      throws SEDSecurityException {
    XMLStructure xo = null;
    try {
      JAXBContext jc = JAXBContext.newInstance(obj.getClass());
      Marshaller m = jc.createMarshaller();
      Node el = doc.createElement("ROOT-JAXB-OBJECT");
      m.marshal(obj, el);
      setIdnessToElemetns(el);

      xo = new DOMStructure(el.getFirstChild());

    } catch (JAXBException ex) {
      throw new SEDSecurityException(
          SEDSecurityException.SEDSecurityExceptionCode.CreateSignatureException, ex,
          "Error occured while converting jaxb-object: '" + obj + "' to XMLStructure:" +
          ex.getMessage());
    }
    return xo;
  }

}
