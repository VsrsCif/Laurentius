/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package si.laurentius.lce.sign.pdf;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.StoreException;
import si.laurentius.lce.exception.UnsupportedSignatureException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is an PDF signature utils inspired by Ben Litchfield
 */
public final class ValidateSignatureUtils {

  //public static final String SIG_SUB_FILTER_DETACHED= "adbe.pkcs7.detached";
  //public static final String SIG_SUB_FILTER_SHA1= "adbe.pkcs7.sha1";
  //public static final String SIG_SUB_FILTER_RSA_SHA1= "adbe.x509.rsa_sha1";
  public static final String SIG_SUB_FILTER_DETACHED = PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED.
          getName();

  public static final String SIG_SUB_FILTER_SHA1 = PDSignature.SUBFILTER_ADBE_PKCS7_SHA1.
          getName();
  public static final String SIG_SUB_FILTER_RSA_SHA1 = PDSignature.SUBFILTER_ADBE_X509_RSA_SHA1.
          getName();

  public static final String CERT_OBJ = "Cert";

  public static final String MD_SHA1 = "SHA1";

  public enum SubFilterType {
    ADBE_PKCS7_DETACHED("adbe.pkcs7.detached"),
    ADBE_PKCS7_SHA1("adbe.pkcs7.sha1"),
    ADBE_X509_RSA_SHA1("adbe.x509.rsa_sha1"),
    ETSI_CADES_DETACHED("ETSI.CAdES.detached"),
    ETSI_RFC3161("ETSI.RFC3161");

    private String subfiltername;

    SubFilterType(String subfiltername) {
      this.subfiltername = subfiltername;
    }

    public String getSubfiltername() {
      return subfiltername;
    }

    public static SubFilterType fromString(String subfiltername) throws UnsupportedSignatureException {
      for (SubFilterType sft : SubFilterType.values()) {
        if (sft.getSubfiltername().equals(subfiltername)) {
          return sft;
        }
      }
      throw new UnsupportedSignatureException(subfiltername);
    }
  }

  public ValidateSignatureUtils() {

  }

  public List<SignatureInfo> validateSignatures(File pdfFile) throws IOException, CertificateException, SignatureException, NoSuchAlgorithmException {
    List<SignatureInfo> lstSig = null;

    try (PDDocument document = PDDocument.load(pdfFile)) {
      lstSig = new ArrayList<>();

      for (PDSignature sig : document.getSignatureDictionaries()) {
        SignatureInfo sigInfo = null;

        COSDictionary sigDict = sig.getCOSObject();
        COSString contents = (COSString) sigDict.getDictionaryObject(
                COSName.CONTENTS);

        // download the signed content
        byte[] buf = null;
        try (FileInputStream fis = new FileInputStream(pdfFile)) {
          buf = sig.getSignedContent(fis);
        }

        SubFilterType subFilter = SubFilterType.fromString(sig.getSubFilter());

        switch (subFilter) {
          case ADBE_PKCS7_DETACHED:
            sigInfo = getSignatureInfo(buf, contents, sig, subFilter);
            break;
          case ADBE_PKCS7_SHA1:
            // example: PDFBOX-1452.pdf
            COSString certString = (COSString) sigDict.getDictionaryObject(
                    COSName.CONTENTS);
            byte[] certData = certString.getBytes();
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            ByteArrayInputStream certStream = new ByteArrayInputStream(certData);
            Collection<? extends Certificate> certs = factory.
                    generateCertificates(certStream);
            byte[] hash = MessageDigest.getInstance(MD_SHA1).digest(buf);
            sigInfo = getSignatureInfo(hash, contents, sig, subFilter);
            break;
          case ADBE_X509_RSA_SHA1:
            sigInfo = getSignatureInfo(buf, contents, sig, subFilter);
            break;
          case ETSI_CADES_DETACHED:
            sigInfo = getSignatureInfo(buf, contents, sig, subFilter);
            break;
          case ETSI_RFC3161:
            sigInfo = getSignatureInfo(buf, contents, sig, subFilter);
            break;
        }

        lstSig.add(sigInfo);
      }
    } catch (CMSException | OperatorCreationException ex) {
      throw new IOException(ex);
    } catch (UnsupportedSignatureException e) {
      throw new RuntimeException(e);
    }

    return lstSig;
  }

  public List<X509Certificate> getSignatureCerts(String infile) throws IOException, CertificateException,
          NoSuchAlgorithmException, InvalidKeyException,
          NoSuchProviderException, SignatureException {
    return getSignatureCerts(new File(infile));
  }

  public List<X509Certificate> getSignatureCerts(File pdfFile)
          throws IOException, CertificateException,
          NoSuchAlgorithmException, InvalidKeyException,
          NoSuchProviderException, SignatureException {

    List<X509Certificate> lstCerts = new ArrayList<>();
    PDDocument document = null;
    try {
      document = PDDocument.load(pdfFile);
      for (PDSignature sig : document.getSignatureDictionaries()) {
        COSDictionary sigDict = sig.getCOSObject();
        COSString contents = (COSString) sigDict.getDictionaryObject(
                COSName.CONTENTS);

        // download the signed content
        FileInputStream fis = new FileInputStream(pdfFile);
        byte[] buf = null;
        try {
          buf = sig.getSignedContent(fis);
        } finally {
          fis.close();
        }
        String subFilter = sig.getSubFilter();

        if (subFilter != null) {
          if (subFilter.equals(SIG_SUB_FILTER_DETACHED)) {
            X509Certificate xc = getSignerCert(buf, contents, sig);
            lstCerts.add(xc);
          } /*else if (subFilter.equals(SIG_SUB_FILTER_SHA1)) {
            // example: PDFBOX-1452.pdf
            COSString certString = (COSString) sigDict.getDictionaryObject(
                COSName.CONTENTS);
            byte[] certData = certString.getBytes();
            
            CertificateFactory factory = CertificateFactory.getInstance(CF_X509);
            ByteArrayInputStream certStream = new ByteArrayInputStream(certData);
            Collection<? extends Certificate> certs = factory.generateCertificates(certStream);
            System.out.println("certs=" + certs);

            byte[] hash = MessageDigest.getInstance(MD_SHA1).digest(buf);
            verifyPKCS7(hash, contents, sig);

            //TODO check certificate chain, revocation lists, timestamp...
          } else if (subFilter.equals(SIG_SUB_FILTER_RSA_SHA1)) {
            
            // example: PDFBOX-2693.pdf
            COSString certString = (COSString) sigDict.getDictionaryObject(
                COSName.getPDFName(CERT_OBJ));
            byte[] certData = certString.getBytes();
            CertificateFactory factory = CertificateFactory.getInstance(CF_X509);
            ByteArrayInputStream certStream = new ByteArrayInputStream(certData);
            Collection<? extends Certificate> certs = factory.generateCertificates(certStream);
            System.out.println("certs=" + certs);

            //TODO verify signature
          }*/ else {
            System.err.println("Unknown certificate type: " + subFilter);
          }
        } else {
          throw new IOException("Missing subfilter for cert dictionary");
        }
      }
    } catch (CMSException | OperatorCreationException ex) {
      throw new IOException(ex);
    } finally {
      if (document != null) {
        document.close();
      }

    }
    return lstCerts;
  }

  /**
   * Verify a PKCS7 signature.
   *
   * @param byteArray the byte sequence that has been signed
   * @param contents the /Contents field as a COSString
   * @param sig the PDF signature (the /V dictionary)
   * @throws CertificateException
   * @throws CMSException
   * @throws StoreException
   * @throws OperatorCreationException
   */
  private X509Certificate getSignerCert(byte[] byteArray, COSString contents,
          PDSignature sig)
          throws CMSException, CertificateException, StoreException, OperatorCreationException {
    // inspiration:
    // http://stackoverflow.com/a/26702631/535646
    // http://stackoverflow.com/a/9261365/535646
    CMSProcessable signedContent = new CMSProcessableByteArray(byteArray);
    CMSSignedData signedData = new CMSSignedData(signedContent, contents.getBytes());
    Store certificatesStore = signedData.getCertificates();
    Collection<SignerInformation> signers = signedData.getSignerInfos().getSigners();
    SignerInformation signerInformation = signers.iterator().next();
    Collection matches = certificatesStore.getMatches(signerInformation.getSID());
    X509CertificateHolder certificateHolder = (X509CertificateHolder) matches.iterator().next();
    X509Certificate certFromSignedData = new JcaX509CertificateConverter().getCertificate(certificateHolder);

    return certFromSignedData;
  }

  private SignatureInfo getSignatureInfo(byte[] byteArray, COSString contents,
          PDSignature sig, SubFilterType subFilter)
          throws CMSException, CertificateException, StoreException, OperatorCreationException {
    // inspiration:
    // http://stackoverflow.com/a/26702631/535646
    // http://stackoverflow.com/a/9261365/535646
    SignatureInfo info = new SignatureInfo();
    info.setDate(sig.getSignDate());

    CMSProcessable signedContent = new CMSProcessableByteArray(byteArray);
    CMSSignedData signedData = new CMSSignedData(signedContent, contents.getBytes());
    Store certificatesStore = signedData.getCertificates();
    Collection<SignerInformation> signers = signedData.getSignerInfos().getSigners();
    SignerInformation signerInformation = signers.iterator().next();
    Collection matches = certificatesStore.getMatches(signerInformation.getSID());
    X509CertificateHolder certificateHolder = (X509CertificateHolder) matches.iterator().next();
    X509Certificate certFromSignedData = new JcaX509CertificateConverter().getCertificate(certificateHolder);

    info.setSignerCert(certFromSignedData);
    SignerInformationVerifier verifier = new JcaSimpleSignerInfoVerifierBuilder().
            build(certFromSignedData);

    if (subFilter == SubFilterType.ETSI_RFC3161) {
      if (signerInformation.getContentType().getId().equals(PKCSObjectIdentifiers.id_ct_TSTInfo.getId())) {
        try {
          ASN1Primitive primitive = ASN1Primitive.fromByteArray(signedData.getEncoded());
          byte[] derEncoded = primitive.getEncoded(ASN1Encoding.DER);
          CMSSignedData cmsSignedData = new CMSSignedData(derEncoded);

          TimeStampToken timeStampToken = new TimeStampToken(cmsSignedData);
          info.setIsSignatureValid(timeStampToken.isSignatureValid(verifier));
        } catch (IOException | TSPException e) {
          throw new RuntimeException(e);
        }
      } else {
        if (signerInformation.getUnsignedAttributes() != null) {
          ASN1EncodableVector attributes = signerInformation.getUnsignedAttributes().getAll(PKCSObjectIdentifiers.id_aa_signatureTimeStampToken);
          if (attributes.size() == 1) {
            try {
              Attribute attribute = (Attribute) attributes.get(0);
              ASN1Object attributeValue = (ASN1Object) attribute.getAttrValues().getObjectAt(0);
              CMSSignedData signedTSTData = new CMSSignedData(attributeValue.toASN1Primitive().getEncoded());
              TimeStampToken timeStampToken = new TimeStampToken(signedTSTData);
              info.setIsSignatureValid(timeStampToken.isSignatureValid(verifier));
            } catch (IOException | TSPException e) {
              throw new RuntimeException(e);
            }

          } else {
            throw new RuntimeException("Unknown content type: " + signerInformation.getContentType().getId());
          }
        }
      }
    } else {
      try {
        info.setIsSignatureValid(signerInformation.verify(verifier));
      } catch (CMSVerifierCertificateNotValidException ex) {
        info.getErrorMessages().add(
                "Certificate was no valid at signing time!" + ex);
      }
    }

    return info;
  }

  private SignatureInfo getSignatureInfoForEtsiSignature(byte[] signedContentBytes, COSString contents, PDSignature signatureDictionary)
          throws CMSException, CertificateException, StoreException, OperatorCreationException {
    SignatureInfo info = new SignatureInfo();

    CMSProcessable signedContent = new CMSProcessableByteArray(signedContentBytes);
    CMSSignedData signedData = new CMSSignedData(signedContent, contents.getBytes());
    Store certificatesStore = signedData.getCertificates();
    Collection<SignerInformation> signers = signedData.getSignerInfos().getSigners();
    SignerInformation signerInformation = signers.iterator().next();
    Collection matches = certificatesStore.getMatches(signerInformation.getSID());
    X509CertificateHolder certificateHolder = (X509CertificateHolder) matches.iterator().next();
    X509Certificate certFromSignedData = new JcaX509CertificateConverter().getCertificate(certificateHolder);

    boolean verified = signerInformation.verify(new JcaSimpleSignerInfoVerifierBuilder().build(certFromSignedData));
    info.setIsSignatureValid(verified);
    info.setDate(signatureDictionary.getSignDate());
    info.setSignerCert(certFromSignedData);

    return info;
  }

}
