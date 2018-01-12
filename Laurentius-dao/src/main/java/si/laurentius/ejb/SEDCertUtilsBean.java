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
package si.laurentius.ejb;

import java.io.StringWriter;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import si.laurentius.cert.SEDCertPassword;
import si.laurentius.cert.SEDCertificate;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.enums.CertStatus;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.interfaces.SEDCertStoreInterface;
import si.laurentius.commons.interfaces.SEDCertUtilsInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.ejb.sec.CertKeyPasswordCallback;
import si.laurentius.lce.KeystoreUtils;
import si.laurentius.lce.tls.X509KeyManagerForAlias;
import si.laurentius.lce.tls.X509TrustManagerForAlias;
import si.laurentius.msh.pmode.References;
import si.laurentius.msh.pmode.X509;
import si.laurentius.msh.pmode.XPath;

/**
 *
 * @author Jože Rihtaršič
 */
@Stateless
@Local(SEDCertUtilsInterface.class)
public class SEDCertUtilsBean implements SEDCertUtilsInterface {

  public static final String SEC_MERLIN_KEYSTORE_ALIAS = "org.apache.ws.security.crypto.merlin.keystore.alias";
  /**
   *
   */
  public static final String SEC_MERLIN_KEYSTORE_FILE = "org.apache.ws.security.crypto.merlin.keystore.file";
  /**
   *
   */
  public static final String SEC_MERLIN_KEYSTORE_PASS = "org.apache.ws.security.crypto.merlin.keystore.password";
  /**
   *
   */
  public static final String SEC_MERLIN_KEYSTORE_TYPE = "org.apache.ws.security.crypto.merlin.keystore.type";
  /**
   *
   */
  public static final String SEC_MERLIN_TRUSTSTORE_ALIAS = "org.apache.ws.security.crypto.merlin.truststore.alias";
  /**
   *
   */
  public static final String SEC_MERLIN_TRUSTSTORE_FILE = "org.apache.ws.security.crypto.merlin.truststore.file";
  /**
   *
   */
  public static final String SEC_MERLIN_TRUSTSTORE_PASS = "org.apache.ws.security.crypto.merlin.truststore.password";
  /**
   *
   */
  public static final String SEC_MERLIN_TRUSTSTORE_TYPE = "org.apache.ws.security.crypto.merlin.truststore.type";
  /**
   *
   */
  public static final String SEC_PROIDER_MERLIN = "org.apache.wss4j.common.crypto.Merlin";
  /**
   *
   */
  public static final String SEC_PROVIDER = "org.apache.ws.security.crypto.provider";

  private static final SEDLogger LOG = new SEDLogger(SEDCertStoreBean.class);

  private final KeystoreUtils mku = new KeystoreUtils();

  @EJB(mappedName = SEDJNDI.JNDI_DBCERTSTORE)
  private SEDCertStoreInterface mdbCertStore;

  @Override
  public Properties getCXFKeystoreProperties(String alias) throws SEDSecurityException {
    long l = LOG.logStart();
    SEDCertificate aliasCrt = mdbCertStore.getSEDCertificatForAlias(alias);
    validateCertificate(aliasCrt);
    if (aliasCrt == null) {
      String msg = "Key for alias '" + alias + "' do not exists!";
      throw new SEDSecurityException(
              SEDSecurityException.SEDSecurityExceptionCode.CertificateException,
              msg);
    }

    SEDCertPassword cp = mdbCertStore.getKeyPassword(
            SEDCertStoreInterface.KEYSTORE_NAME);
    if (cp == null) {
      throw new IllegalArgumentException("Missing password for keystore!");
    }
    Properties signProperties = new Properties();
    signProperties.put(SEC_PROVIDER, SEC_PROIDER_MERLIN);
    signProperties.put(SEC_MERLIN_KEYSTORE_ALIAS, alias);
    signProperties.put(SEC_MERLIN_KEYSTORE_PASS, cp.getPassword());
    signProperties.put(SEC_MERLIN_KEYSTORE_FILE,
            SEDSystemProperties.getCertstoreFile().getAbsolutePath());
    signProperties.put(SEC_MERLIN_KEYSTORE_TYPE,  SEDSystemProperties.getCertstoreType());
    LOG.logEnd(l);
    return signProperties;
  }

  /**
   *
   * @param alias
   * @param cs
   * @return
   */
  @Override
  public Properties getCXFTruststoreProperties(String alias) throws SEDSecurityException {
    long l = LOG.logStart();

    SEDCertificate aliasCrt = mdbCertStore.getSEDCertificatForAlias(alias);
    validateCertificate(aliasCrt);

    SEDCertPassword cp = mdbCertStore.getKeyPassword(
            SEDCertStoreInterface.KEYSTORE_NAME);
    if (cp == null) {
      throw new IllegalArgumentException("Missing password for keystore!");
    }
    Properties signVerProperties = new Properties();
    signVerProperties.put(SEC_PROVIDER, SEC_PROIDER_MERLIN);
    signVerProperties.put(SEC_MERLIN_TRUSTSTORE_ALIAS, alias);
    signVerProperties.put(SEC_MERLIN_TRUSTSTORE_PASS, cp.getPassword());
    signVerProperties.put(SEC_MERLIN_TRUSTSTORE_FILE,
            SEDSystemProperties.getCertstoreFile().getAbsolutePath());
    signVerProperties.put(SEC_MERLIN_TRUSTSTORE_TYPE,  SEDSystemProperties.getCertstoreType());
    LOG.logEnd(l);

    return signVerProperties;
  }

  @Override
  public X509KeyManager[] getKeyManagerForAlias(String alias) throws SEDSecurityException {
    long l = LOG.logStart();
    SEDCertPassword cp = mdbCertStore.getKeyPassword(alias);
    if (cp == null) {
      throw new SEDSecurityException(
              SEDSecurityException.SEDSecurityExceptionCode.CertificateException,
              "Missing password for key with alias:" + alias);
    }
    KeyManagerFactory fac;
    try {
      fac = KeyManagerFactory.getInstance(KeyManagerFactory.
              getDefaultAlgorithm());
    } catch (NoSuchAlgorithmException ex) {
      throw new SEDSecurityException(
              SEDSecurityException.SEDSecurityExceptionCode.NoSuchAlgorithm,
              ex, ex.getMessage());
    }
    try {
      fac.init(mdbCertStore.getCertStore(), cp.getPassword().toCharArray());
    } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException ex) {
      throw new SEDSecurityException(
              SEDSecurityException.SEDSecurityExceptionCode.KeyStoreException,
              ex,
              "Error init KeyManagerFactory for keystore. Error: " + ex.
                      getMessage());
    }
    KeyManager[] kms = fac.getKeyManagers();
    ;
    X509KeyManager[] kmsres = null;
    // wrap keymanager to X509KeyManagerForAlias
    if (kms != null) {
      List<X509KeyManager> kmarr = new ArrayList<>();
      for (KeyManager km : kms) {
        if (km instanceof X509KeyManager) {
          kmarr.add(new X509KeyManagerForAlias((X509KeyManager) km, alias));
        }
      }
      kmsres = kmarr.toArray(new X509KeyManager[kmarr.size()]);
    }
    LOG.logEnd(l);
    return kmsres;
  }

  @Override
  public X509TrustManager getTrustManagerForAlias(String alias,
          boolean validateRootCA) throws SEDSecurityException {
    long l = LOG.logStart();
    X509TrustManagerForAlias tm = new X509TrustManagerForAlias(
            mdbCertStore.getX509CertForAlias(alias),
            validateRootCA ? mdbCertStore.getRootCA509Certs() : null);
    LOG.logEnd(l);
    return tm;
  }

  /**
   * Method creates signature property configuration for WSS4JOutInterceptor
   * inteceptor
   *
   * @param sig
   * @param keystore
   * @param key
   * @return
   */
  public Map<String, Object> createCXFSignatureConfiguration(X509.Signature sig,
          String sigAlias) throws SEDSecurityException {

    Properties cpKeyStore = getCXFKeystoreProperties(sigAlias);
    SEDCertPassword keyPasswd = mdbCertStore.getKeyPassword(sigAlias);

    Map<String, Object> prps = null;

    if (sig == null || sig.getReference() == null) {
      return prps;
    }
    References ref = sig.getReference();

    // create signature priperties
    String cpropname = "SIG." + UUID.randomUUID().toString();

    prps = new HashMap<>();
    prps.put(cpropname, cpKeyStore);
    // set wss properties

    prps.put(WSHandlerConstants.ACTION, WSHandlerConstants.SIGNATURE);
    prps.put(WSHandlerConstants.SIGNATURE_PARTS,
            createCXFReferenceString(ref));
    prps.put(WSHandlerConstants.SIGNATURE_USER, sigAlias);
    prps.put(WSHandlerConstants.PW_CALLBACK_REF,
            new CertKeyPasswordCallback(keyPasswd));
    prps.put(WSHandlerConstants.SIG_PROP_REF_ID, cpropname);

    if (sig.getAlgorithm() != null || !sig.getAlgorithm().isEmpty()) {
      prps.put(WSHandlerConstants.SIG_ALGO, sig.getAlgorithm());
    }
    if (sig.getHashFunction() != null || !sig.getHashFunction().isEmpty()) {
      prps.put(WSHandlerConstants.SIG_DIGEST_ALGO, sig.getHashFunction());
    }
    if (sig.getKeyIdentifierType() != null && !sig.getKeyIdentifierType().
            isEmpty()) {
      prps.put(WSHandlerConstants.SIG_KEY_ID, sig.getKeyIdentifierType());
    }
    return prps;
  }

  /**
   * Method creates encryption property configuration for WSS4JOutInterceptor
   * inteceptor
   *
   * @param enc
   * @param trustore
   * @param cert
   * @return
   */
  public Map<String, Object> createCXFEncryptionConfiguration(
          X509.Encryption enc,
          String alias) throws SEDSecurityException {
    Map<String, Object> prps = null;

    Properties cpTrust = getCXFTruststoreProperties(alias);

    if (enc == null || enc.getReference() == null) {
      return prps;
    }
    References ref = enc.getReference();
    // create signature priperties
    String cpropname = "ENC." + UUID.randomUUID().toString();

    prps = new HashMap<>();
    prps.put(cpropname, cpTrust);
    // set wss properties
    prps.put(WSHandlerConstants.ACTION, WSHandlerConstants.ENCRYPT);
    prps.put(WSHandlerConstants.ENCRYPTION_PARTS,
            createCXFReferenceString(ref));
    prps.put(WSHandlerConstants.ENCRYPTION_USER, alias);
    prps.put(WSHandlerConstants.ENC_PROP_REF_ID, cpropname);

    if (enc.getAlgorithm() != null && !enc.getAlgorithm().isEmpty()) {
      prps.put(WSHandlerConstants.ENC_SYM_ALGO, enc.getAlgorithm());
    }

    if (enc.getKeyTransport() != null && !enc.getKeyTransport().isEmpty()) {
      prps.put(WSHandlerConstants.ENC_KEY_TRANSPORT, enc.getKeyTransport());

      if (enc.getDigest() != null && !enc.getDigest().isEmpty()) {
        prps.put(WSHandlerConstants.ENC_DIGEST_ALGO, enc.getDigest());
      }

      if (enc.getMgf1Algorithm() != null && !enc.getMgf1Algorithm().isEmpty()) {
        prps.put(WSHandlerConstants.ENC_MGF_ALGO, enc.getMgf1Algorithm());
      }
    }

    if (enc.getKeyIdentifierType() != null && !enc.getKeyIdentifierType().
            isEmpty()) {
      prps.put(WSHandlerConstants.ENC_KEY_ID, enc.getKeyIdentifierType());
    }

    return prps;
  }

  public String createCXFReferenceString(References ref) {

    StringWriter elmWr = new StringWriter();
    if (ref.getElements() != null
            && ref.getElements().getXPaths().size() > 0) {

      for (XPath el : ref.getElements().getXPaths()) {
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
              for (XPath.Namespace n : el.getNamespaces()) {
                if (n.getPrefix().equals(nslst[0])) {
                  elmWr.write(n.getNamespace());
                  elmWr.write("}");
                }
              }
              elmWr.write(nslst[1]);
              elmWr.write(";");
              break;
            default:
              LOG.formatedWarning("Bad xpath definition: %s, element: '%s' ",
                      el.getXpath(), xpath);
          }
        }
      }
    }
    if (ref.getAllAttachments() != null && ref.getAllAttachments()) {
      elmWr.write("{}cid:Attachments;");
    }
    return elmWr.toString();
  }

  @Override
  public Map<String, Object> createCXFDecryptionConfiguration(
          X509.Encryption enc,
          String decAlias) throws SEDSecurityException {

    Properties ksProp = getCXFKeystoreProperties(decAlias);
    SEDCertPassword keyPasswd = mdbCertStore.getKeyPassword(decAlias);

    Map<String, Object> prps = null;
    if (enc == null || enc.getReference() == null) {
      return prps;
    }

    // create signature priperties
    String cpropname = "DEC." + UUID.randomUUID().toString();

    prps = new HashMap<>();
    prps.put(cpropname, ksProp);
    // set wss properties
    prps.put(WSHandlerConstants.ACTION, WSHandlerConstants.ENCRYPT);
    prps.put(WSHandlerConstants.PW_CALLBACK_REF,
            new CertKeyPasswordCallback(keyPasswd));
    prps.put(WSHandlerConstants.DEC_PROP_REF_ID, cpropname);

    if (enc.getAlgorithm() != null || !enc.getAlgorithm().isEmpty()) {
      prps.put(WSHandlerConstants.ENC_SYM_ALGO, enc.getAlgorithm());
    }
    if (enc.getKeyIdentifierType() != null && !enc.getKeyIdentifierType().
            isEmpty()) {
      prps.put(WSHandlerConstants.ENC_KEY_ID, enc.getKeyIdentifierType());
    }

    return prps;
  }

  @Override
  public Map<String, Object> createCXFSignatureValidationConfiguration(
          X509.Signature sig,
          String sigAliasProp) throws SEDSecurityException {

    Map<String, Object> prps = null;

    Properties tstCP = getCXFTruststoreProperties(sigAliasProp);

    if (sig == null || sig.getReference() == null) {
      return prps;
    }
    // create signature priperties
    String cpropname = "SIG-VAL." + UUID.randomUUID().toString();

    // Properties cp = KeystoreUtils.getTruststoreProperties(crt.getAlias(), truststore);
    prps = new HashMap<>();
    prps.put(cpropname, tstCP);
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
    if (sig.getKeyIdentifierType() != null && !sig.getKeyIdentifierType().
            isEmpty()) {
      prps.put(WSHandlerConstants.SIG_KEY_ID, sig.getKeyIdentifierType());
    }

    return prps;
  }

  private void validateCertificate(SEDCertificate sc) throws SEDSecurityException {
    String alias = sc.getAlias();
    if (CertStatus.INVALID_BY_ROOTCA.containsCode(sc.getStatus())) {
      String msg = "Certificate for alias '" + alias + "' is not signed by trusted RootCA!";
      throw new SEDSecurityException(
              SEDSecurityException.SEDSecurityExceptionCode.CertificateException,
              msg);
    }

    if (CertStatus.INVALID_BY_DATE.containsCode(sc.getStatus())) {
      String msg = "Certificate for alias '" + alias + "' is expired or not valid yet!";
      throw new SEDSecurityException(
              SEDSecurityException.SEDSecurityExceptionCode.CertificateException,
              msg);
    }

    if (CertStatus.CRL_REVOKED.containsCode(sc.getStatus())) {
      String msg = "Certificate for alias '" + alias + "' is revoked!";
      throw new SEDSecurityException(
              SEDSecurityException.SEDSecurityExceptionCode.CertificateException,
              msg);
    }

    if (!KeystoreUtils.isCertValid(sc)) {
      String msg = "Certificate for alias '" + alias + " is not valid!";
      throw new SEDSecurityException(
              SEDSecurityException.SEDSecurityExceptionCode.CertificateException,
              msg);
    }

  }
}
