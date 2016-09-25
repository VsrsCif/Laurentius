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
package si.jrc.msh.client.sec;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import org.apache.cxf.ws.security.wss4j.CryptoCoverageChecker;
import org.apache.cxf.ws.security.wss4j.CryptoCoverageUtil;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import si.laurentius.msh.pmode.References;
import si.laurentius.msh.pmode.Security;
import si.laurentius.msh.pmode.X509;
import si.laurentius.cert.SEDCertStore;
import si.laurentius.cert.SEDCertificate;
import si.jrc.msh.exception.EBMSError;
import si.jrc.msh.interceptor.EBMSOutInterceptor;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.sec.KeystoreUtils;

/**
 *
 * @author Jože Rihtaršič
 */
public class SecurityUtils {

  protected final static SEDLogger LOG = new SEDLogger(EBMSOutInterceptor.class);


  /**
   * Method creates encryption property configuration for WSS4JOutInterceptor inteceptor
   *
   * @param enc
   * @param key
   * @param keystore
   * @return
   */
  public static Map<String, Object> createDecryptionConfiguration(X509.Encryption enc,
      SEDCertStore keystore, SEDCertificate key)
      throws EBMSError {

    Map<String, Object> prps = null;
    if (enc == null || enc.getReference() == null) {
      return prps;
    }

    // create signature priperties
    String cpropname = "DEC." + UUID.randomUUID().toString();

    Properties cp = KeystoreUtils.getKeystoreProperties(key.getAlias(), keystore);

    prps = new HashMap<>();
    prps.put(cpropname, cp);
    // set wss properties
    prps.put(WSHandlerConstants.ACTION, WSHandlerConstants.ENCRYPT);
    prps.put(WSHandlerConstants.PW_CALLBACK_REF,
        new MSHKeyPasswordCallback(key));
    prps.put(WSHandlerConstants.DEC_PROP_REF_ID, cpropname);

    if (enc.getAlgorithm() != null || !enc.getAlgorithm().isEmpty()) {
      prps.put(WSHandlerConstants.ENC_SYM_ALGO, enc.getAlgorithm());
    }
    if (enc.getKeyIdentifierType() != null && !enc.getKeyIdentifierType().isEmpty()) {
      prps.put(WSHandlerConstants.ENC_KEY_ID, enc.getKeyIdentifierType());
    }

    return prps;
  }
  /**
   * Method creates encryption property configuration for WSS4JOutInterceptor inteceptor
   *
   * @param enc
   * @param trustore
   * @param cert
   * @return
   */
  public static Map<String, Object> createEncryptionConfiguration(X509.Encryption enc,
      SEDCertStore trustore, SEDCertificate cert)
      throws EBMSError {
    Map<String, Object> prps = null;
    
    if (enc == null || enc.getReference() == null) {
      return prps;
    }
    References ref = enc.getReference();
    // create signature priperties
    String cpropname = "ENC." + UUID.randomUUID().toString();
    
    Properties cp = KeystoreUtils.getTruststoreProperties(cert.getAlias(), trustore);
    
    prps = new HashMap<>();
    prps.put(cpropname, cp);
    // set wss properties
    prps.put(WSHandlerConstants.ACTION, WSHandlerConstants.ENCRYPT);
    prps.put(WSHandlerConstants.ENCRYPTION_PARTS,
        SecurityUtils.createReferenceString(ref));
    prps.put(WSHandlerConstants.ENCRYPTION_USER, cert.getAlias());
    prps.put(WSHandlerConstants.ENC_PROP_REF_ID, cpropname);
    
    if (enc.getAlgorithm() != null || !enc.getAlgorithm().isEmpty()) {
      prps.put(WSHandlerConstants.ENC_SYM_ALGO, enc.getAlgorithm());
    }
    if (enc.getKeyIdentifierType() != null && !enc.getKeyIdentifierType().isEmpty()) {
      prps.put(WSHandlerConstants.ENC_KEY_ID, enc.getKeyIdentifierType());
    }

    return prps;
  }
  /**
   *
   * @param ref
   * @return
   * @throws EBMSError
   */
  public static String createReferenceString(References ref)
      throws EBMSError {
    
    StringWriter elmWr = new StringWriter();
    if (ref.getElements() != null &&
        ref.getElements().getXPaths().size() > 0) {
      
      for (References.Elements.XPath el : ref.getElements().getXPaths()) {
        String[] lst = el.getXpath().split("/");
        if (lst.length > 0) {
          String xpath = lst[lst.length - 1];
          String[] nslst = xpath.split(":");
          switch (nslst.length) {
            case 1:
              elmWr.write(";");
              elmWr.write("{Element}");
              elmWr.write(nslst[0]);
              elmWr.write(";");
              break;
            case 2:
              elmWr.write("{Element}");
              elmWr.write("{");
              for (References.Elements.XPath.Namespace n : el.getNamespaces()) {
                if (n.getPrefix().equals(nslst[0])) {
                  elmWr.write(n.getNamespace());
                  elmWr.write("}");
                }
              }
              elmWr.write(nslst[1]);
              elmWr.write(";");
              break;
            default:
              LOG.formatedWarning("Bad xpath definition: %s, element: '%s' ", el.getXpath(), xpath);
          }
        }
      }
    }
    if (ref.getAllAttachments()) {
      elmWr.write("{}cid:Attachments;");
    }
    return elmWr.toString();
  }

  /**
   * Method creates signature property configuration for WSS4JOutInterceptor inteceptor
   *
   * @param sig
   * @param keystore
   * @param key
   * @return
   */
  public static Map<String, Object> createSignatureConfiguration(X509.Signature sig,
      SEDCertStore keystore,
      SEDCertificate key)
      throws EBMSError {

    Map<String, Object> prps = null;

    if (sig == null || sig.getReference() == null) {
      return prps;
    }
    References ref = sig.getReference();

    // create signature priperties
    String cpropname = "SIG." + UUID.randomUUID().toString();

    Properties cp = KeystoreUtils.getKeystoreProperties(key.getAlias(), keystore);

    prps = new HashMap<>();
    prps.put(cpropname, cp);
    // set wss properties
    
    prps.put(WSHandlerConstants.ACTION, WSHandlerConstants.SIGNATURE  );
    prps.put(WSHandlerConstants.SIGNATURE_PARTS,
        SecurityUtils.createReferenceString(ref));
    prps.put(WSHandlerConstants.SIGNATURE_USER, key.getAlias());
    prps.put(WSHandlerConstants.PW_CALLBACK_REF,
        new MSHKeyPasswordCallback(key));
    prps.put(WSHandlerConstants.SIG_PROP_REF_ID, cpropname);

    if (sig.getAlgorithm() != null || !sig.getAlgorithm().isEmpty()) {
      prps.put(WSHandlerConstants.SIG_ALGO, sig.getAlgorithm());
    }
    if (sig.getHashFunction() != null || !sig.getHashFunction().isEmpty()) {
      prps.put(WSHandlerConstants.SIG_DIGEST_ALGO, sig.getHashFunction());
    }
    if (sig.getKeyIdentifierType() != null && !sig.getKeyIdentifierType().isEmpty()) {
      prps.put(WSHandlerConstants.SIG_KEY_ID, sig.getKeyIdentifierType());
    }
    return prps;
  }
  /**
   * Method creates signature property configuration for WSS4JOutInterceptor inteceptor
   *
   * @param sig
   * @param truststore
   * @param crt
   * @return
   */
  public static Map<String, Object> createSignatureValidationConfiguration(X509.Signature sig,
      SEDCertStore truststore, SEDCertificate crt)
      throws EBMSError {
    Map<String, Object> prps = null;
    
    if (sig == null || sig.getReference() == null) {
      return prps;
    }
    // create signature priperties
    String cpropname = "SIG-VAL." + UUID.randomUUID().toString();
    
    Properties cp = KeystoreUtils.getTruststoreProperties(crt.getAlias(), truststore);
    
    prps = new HashMap<>();
    prps.put(cpropname, cp);
    // set wss properties
    prps.put(WSHandlerConstants.ACTION, WSHandlerConstants.SIGNATURE);
    // prps.put(WSHandlerConstants.SIGNATURE_PARTS, strReference);
    prps.put(WSHandlerConstants.SIG_VER_PROP_REF_ID, cpropname);
    
    if (sig.getAlgorithm() != null || !sig.getAlgorithm().isEmpty()) {
      prps.put(WSHandlerConstants.SIG_ALGO, sig.getAlgorithm());
    }
    if (sig.getHashFunction() != null || !sig.getHashFunction().isEmpty()) {
      prps.put(WSHandlerConstants.SIG_DIGEST_ALGO, sig.getHashFunction());
    }
    if (sig.getKeyIdentifierType() != null && !sig.getKeyIdentifierType().isEmpty()) {
      prps.put(WSHandlerConstants.SIG_KEY_ID, sig.getKeyIdentifierType());
    }
    
    return prps;
  }
  
  
  public static final CryptoCoverageChecker configureCryptoCoverageCheckerInterceptors(Security sc) {
    CryptoCoverageChecker cc;

    if (sc == null || sc.getX509() == null) {
      return null;
    }
    Map<String, String> prefixes = new HashMap<>();
    List<CryptoCoverageChecker.XPathExpression> xpaths = new ArrayList<>();

    if (sc.getX509().getSignature() != null &&
         sc.getX509().getSignature().getReference() != null &&
         sc.getX509().getSignature().getReference().getElements() != null) {
      X509.Signature snc = sc.getX509().getSignature();
      X509.Signature.Reference rsgn = snc.getReference();

      for (References.Elements.XPath el : rsgn.getElements().getXPaths()) {
        for (References.Elements.XPath.Namespace ns : el.getNamespaces()) {
          prefixes.put(ns.getPrefix(), ns.getNamespace());
        }
        xpaths.add(new CryptoCoverageChecker.XPathExpression(el.getXpath(),
            CryptoCoverageUtil.CoverageType.SIGNED, CryptoCoverageUtil.CoverageScope.ELEMENT));
      }
    }

    if (sc.getX509().getEncryption() != null &&
         sc.getX509().getEncryption().getReference() != null &&
         sc.getX509().getEncryption().getReference().getElements() != null) {
      X509.Encryption snc = sc.getX509().getEncryption();
      X509.Encryption.Reference rsgn = snc.getReference();

      for (References.Elements.XPath el : rsgn.getElements().getXPaths()) {
        for (References.Elements.XPath.Namespace ns : el.getNamespaces()) {
          prefixes.put(ns.getPrefix(), ns.getNamespace());
        }
        xpaths.add(new CryptoCoverageChecker.XPathExpression(el.getXpath(),
            CryptoCoverageUtil.CoverageType.ENCRYPTED, CryptoCoverageUtil.CoverageScope.ELEMENT));
      }
    }

    cc = new CryptoCoverageChecker(prefixes, xpaths);
    return cc;
  }
  
  


}
