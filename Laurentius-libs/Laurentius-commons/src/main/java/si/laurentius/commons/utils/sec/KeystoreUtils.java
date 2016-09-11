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
package si.laurentius.commons.utils.sec;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import si.laurentius.cert.SEDCertStore;
import si.laurentius.cert.SEDCertificate;
import si.laurentius.commons.exception.SEDSecurityException;
import static si.laurentius.commons.exception.SEDSecurityException.SEDSecurityExceptionCode.CertificateException;
import static si.laurentius.commons.exception.SEDSecurityException.SEDSecurityExceptionCode.KeyStoreException;
import static si.laurentius.commons.exception.SEDSecurityException.SEDSecurityExceptionCode.NoSuchAlgorithm;
import static si.laurentius.commons.exception.SEDSecurityException.SEDSecurityExceptionCode.ReadWriteFileException;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StringFormater;
import si.laurentius.commons.utils.sec.tls.X509KeyManagerForAlias;
import static java.security.KeyStore.getInstance;
import java.util.Objects;
import si.laurentius.commons.utils.Utils;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public class KeystoreUtils {

  /**
   *
   */
  public static final String SEC_PROVIDER = "org.apache.ws.security.crypto.provider";

  /**
   *
   */
  public static final String SEC_PROIDER_MERLIN = "org.apache.wss4j.common.crypto.Merlin";

  /**
   *
   */
  public static final String SEC_MERLIN_KEYSTORE_FILE =
      "org.apache.ws.security.crypto.merlin.keystore.file";

  /**
   *
   */
  public static final String SEC_MERLIN_KEYSTORE_TYPE =
      "org.apache.ws.security.crypto.merlin.keystore.type";

  /**
   *
   */
  public static final String SEC_MERLIN_KEYSTORE_PASS =
      "org.apache.ws.security.crypto.merlin.keystore.password";

  /**
   *
   */
  public static final String SEC_MERLIN_KEYSTORE_ALIAS =
      "org.apache.ws.security.crypto.merlin.keystore.alias";

  /**
   *
   */
  public static final String SEC_MERLIN_TRUSTSTORE_FILE =
      "org.apache.ws.security.crypto.merlin.truststore.file";

  /**
   *
   */
  public static final String SEC_MERLIN_TRUSTSTORE_TYPE =
      "org.apache.ws.security.crypto.merlin.truststore.type";

  /**
   *
   */
  public static final String SEC_MERLIN_TRUSTSTORE_PASS =
      "org.apache.ws.security.crypto.merlin.truststore.password";

  /**
   *
   */
  public static final String SEC_MERLIN_TRUSTSTORE_ALIAS =
      "org.apache.ws.security.crypto.merlin.truststore.alias";

  /**
   *
   */
  protected final SEDLogger mlog = new SEDLogger(KeystoreUtils.class);

  /**
   *
   * @param sc
   * @return
   * @throws SEDSecurityException
   */
  public KeyStore getKeystore(SEDCertStore sc)
      throws SEDSecurityException {
    KeyStore keyStore = null;
    try (FileInputStream fis = new FileInputStream(
        StringFormater.replaceProperties(sc.getFilePath()))) {
      keyStore = getKeystore(fis, sc.getType(), sc.getPassword().toCharArray());
    } catch (IOException ex) {
      throw new SEDSecurityException(ReadWriteFileException, ex, "Read keystore from stream!" +
          ex.getMessage());
    }
    return keyStore;
  }

  /**
   *
   * @param sc
   * @return
   * @throws SEDSecurityException
   */
  public TrustManager[] getTrustManagers(SEDCertStore sc)
      throws SEDSecurityException {

    KeyStore keyStore = getKeystore(sc);

    TrustManagerFactory fac;
    try {
      fac = TrustManagerFactory.getInstance(TrustManagerFactory
				.getDefaultAlgorithm());
    } catch (NoSuchAlgorithmException ex) {
      
      throw new SEDSecurityException(NoSuchAlgorithm, ex, sc.getType(), ex.getMessage());
    }

    try {
      fac.init(keyStore);
    } catch (KeyStoreException ex) {
      throw new SEDSecurityException(KeyStoreException, ex,
          "Error init TrustManagerFactory for keystore: " + sc.getFilePath() + " Error: " +
          ex.getMessage());
    }

    return fac.getTrustManagers();

  }
 

  /**
   * Get key managers for SEDCertStore.
   *
   * @param sc
   * @return
   * @throws SEDSecurityException
   */
  public KeyManager[] getKeyManagers(SEDCertStore sc, String alias)
      throws SEDSecurityException {

    SEDCertificate cert = null;
    for (SEDCertificate c: sc.getSEDCertificates()){
      if (Objects.equals(c.getAlias(), alias)){
        cert = c;
        break;
      }
    }
    if (cert == null){
      throw new SEDSecurityException(SEDSecurityException.SEDSecurityExceptionCode.KeyForAliasNotExists, alias );
    }
    
    
    KeyStore keyStore = getKeystore(sc);
    String keyStorePassword = sc.getPassword();
    
    

    char[] keyStorePass = cert.getKeyPassword() != null ? cert.getKeyPassword().toCharArray() : null;

    KeyManagerFactory fac;
    try {
      fac = KeyManagerFactory.getInstance(KeyManagerFactory
				.getDefaultAlgorithm());
    } catch (NoSuchAlgorithmException ex) {
      throw new SEDSecurityException(NoSuchAlgorithm, ex, sc.getType());
    }

    try {
      fac.init(keyStore, "key1234".toCharArray());
    } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException ex) {
      throw new SEDSecurityException(KeyStoreException, ex,
          "Error init KeyManagerFactory for keystore: " + sc.getFilePath() + " Error: " +
          ex.getMessage());
    }
    return fac.getKeyManagers();
  }
  
   /**
   * Get key managers for SEDCertStore and alias.
   *
   * @param sc
   * @return
   * @throws SEDSecurityException
   */
  public KeyManager[] getKeyManagersForAlias(SEDCertStore sc, String alias)
      throws SEDSecurityException {

    KeyManager[] kmsres = null;
    KeyManager[] kms = getKeyManagers(sc, alias);
    if(kms!=null){
      List<KeyManager> kmarr  = new ArrayList<>();
      for (KeyManager km: kms){
        if (km instanceof X509KeyManager){
          kmarr.add(new X509KeyManagerForAlias((X509KeyManager)km, alias));
        }
      }
      kmsres = kmarr.toArray(new KeyManager[0]);
    }
    return kmsres;
  }

  /**
   *
   * @param isTrustStore
   * @param trustStoreType
   * @param password
   * @return
   * @throws SEDSecurityException
   */
  public KeyStore getKeystore(InputStream isTrustStore, String trustStoreType, char[] password)
      throws SEDSecurityException {
    KeyStore keyStore = null;
    try {
      keyStore = getInstance(trustStoreType);

      keyStore.load(isTrustStore, password);
    } catch (KeyStoreException ex) {
      throw new SEDSecurityException(KeyStoreException, ex, ex.getMessage());
    } catch (NoSuchAlgorithmException ex) {
      throw new SEDSecurityException(NoSuchAlgorithm, ex, ex.getMessage());
    } catch (CertificateException ex) {
      throw new SEDSecurityException(CertificateException, ex, ex.getMessage());
    } catch (IOException ex) {
      throw new SEDSecurityException(ReadWriteFileException, ex, "Read keystore from stream!" +
          ex.getMessage());
    }
    return keyStore;
  }

  /**
   *
   * @param ks
   * @param alias
   * @param passwd
   * @return
   * @throws SEDSecurityException
   */
  public KeyStore.PrivateKeyEntry getPrivateKeyEntryForAlias(KeyStore ks, String alias,
      String passwd)
      throws SEDSecurityException {

    if (alias == null) {
      throw new SEDSecurityException(CertificateException, "x.509 cert not found in keystore");
    }

    KeyStore.PrivateKeyEntry rsaKey;
    try {
      rsaKey =
          (KeyStore.PrivateKeyEntry) ks.getEntry(alias,
              new KeyStore.PasswordProtection(passwd.toCharArray()));

    } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException ex) {
      throw new SEDSecurityException(KeyStoreException, ex, ex.getMessage());
    } catch (UnrecoverableEntryException ex) {
      throw new SEDSecurityException(KeyStoreException, ex, ex.getMessage());
    }
    return rsaKey;
  }

  /**
   *
   * @param ks
   * @param alias
   * @param psswd
   * @return
   * @throws SEDSecurityException
   */
  public Key getPrivateKeyForAlias(KeyStore ks, String alias, String psswd)
      throws SEDSecurityException {

    if (alias == null) {
      throw new SEDSecurityException(CertificateException, "x.509 cert not found in keystore");
    }

    Key rsaKey;
    try {
      rsaKey = ks.getKey(alias, psswd.toCharArray());
    } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException ex) {
      throw new SEDSecurityException(KeyStoreException, ex, ex.getMessage());
    }
    return rsaKey;
  }

  /**
   *
   * @param lst
   * @param cert
   * @return
   * @throws SEDSecurityException
   */
  public Key getPrivateKeyForX509Cert(List<SEDCertStore> lst, X509Certificate cert)
      throws SEDSecurityException {

    // find alias
    String alias = null;
    Key k = null;
    for (SEDCertStore cs : lst) {
      KeyStore ks = getKeystore(cs);
      // get alias for private key
      alias = getPrivateKeyAliasForX509Cert(ks, cert);
      if (alias != null) {
        // get key password
        for (SEDCertificate c : cs.getSEDCertificates()) {
          if (c.getAlias().equals(alias)) {
            k = getPrivateKeyForAlias(ks, alias, c.getKeyPassword());
            if (k != null) {
              break;
            }
          }
        }
      }

    }
    return k;
  }

  /**
   *
   * @param ks
   * @param cert
   * @return
   * @throws SEDSecurityException
   */
  public String getPrivateKeyAliasForX509Cert(KeyStore ks, X509Certificate cert)
      throws SEDSecurityException {

    // find alias
    String alias = null;
    Enumeration<String> e;
    try {
      e = ks.aliases();
      while (e.hasMoreElements()) {
        String as = e.nextElement();
        X509Certificate rsaCert = (X509Certificate) ks.getCertificate(as);
        if (cert.equals(rsaCert) && ks.isKeyEntry(as)) {
          alias = as;
          break;
        }
      }

    } catch (KeyStoreException ex) {
      throw new SEDSecurityException(CertificateException, ex, ex.getMessage());
    }
    return alias;

  }

  /**
   *
   * @param ks
   * @param cert
   * @param psswd
   * @return
   * @throws SEDSecurityException
   */
  public Key getPrivateKeyForX509Cert(KeyStore ks, X509Certificate cert, String psswd)
      throws SEDSecurityException {

    // find alias
    String alias = null;
    Enumeration<String> e;
    try {
      e = ks.aliases();
      while (e.hasMoreElements()) {
        String as = e.nextElement();
        X509Certificate rsaCert = (X509Certificate) ks.getCertificate(as);
        if (cert.equals(rsaCert)) {
          alias = as;
          break;
        }
      }

    } catch (KeyStoreException ex) {
      throw new SEDSecurityException(CertificateException, ex, ex.getMessage());
    }
    return getPrivateKeyForAlias(ks, alias, psswd);

  }

  /**
   *
   * @param ks
   * @param alias
   * @return
   * @throws SEDSecurityException
   */
  public X509Certificate getTrustedCertForAlias(KeyStore ks, String alias)
      throws SEDSecurityException {
    X509Certificate cert = null;
    try {

      cert = (X509Certificate) ks.getCertificate(alias);

    } catch (KeyStoreException ex) {
      throw new SEDSecurityException(CertificateException, ex,
          "Exception occured when retrieving: '" + alias + "' cert!");
    }
    return cert;
  }

  /**
   *
   * @param filepath
   * @param type
   * @param password
   * @return
   * @throws SEDSecurityException
   */
  public KeyStore openKeyStore(String filepath, String type, char[] password)
      throws SEDSecurityException {
    try (FileInputStream fis = new FileInputStream(StringFormater.replaceProperties(filepath))) {
      return getKeystore(fis, type, password);
    } catch (IOException ex) {
      throw new SEDSecurityException(ReadWriteFileException, ex, "Read keystore: '" + filepath +
          "!");
    }
  }

  /**
   *
   * @param ks
   * @return
   * @throws SEDSecurityException
   */
  public List<String> getKeyStoreAliases(KeyStore ks)
      throws SEDSecurityException {
    List<String> lst = new ArrayList<>();
    try {
      Enumeration<String> e = ks.aliases();
      while (e.hasMoreElements()) {
        String alias = e.nextElement();
        lst.add(alias);
      }
    } catch (KeyStoreException ex) {
      throw new SEDSecurityException(ReadWriteFileException, ex, "Read keystore aliased: !");
    }
    return lst;
  }

  /**
   *
   * @param ks
   * @return
   * @throws SEDSecurityException
   */
  public List<SEDCertificate> getKeyStoreSEDCertificates(KeyStore ks)
      throws SEDSecurityException {
    List<SEDCertificate> lst = new ArrayList<>();
    try {
      Enumeration<String> e = ks.aliases();
      while (e.hasMoreElements()) {
        SEDCertificate ec = new SEDCertificate();

        String alias = e.nextElement();
        Certificate c = ks.getCertificate(alias);
        ec.setKeyEntry(ks.isKeyEntry(alias));
        ec.setAlias(alias);

        ec.setType(c.getType());
        if (c instanceof X509Certificate) {
          X509Certificate xc = (X509Certificate) c;
          ec.setValidFrom(xc.getNotBefore());
          ec.setValidTo(xc.getNotAfter());
          ec.setIssuerDN(xc.getIssuerDN().getName());
          ec.setSubjectDN(xc.getSubjectDN().getName());
          ec.setSerialNumber(xc.getSerialNumber()+"");

        }

        lst.add(ec);
      }
    } catch (KeyStoreException ex) {
      throw new SEDSecurityException(ReadWriteFileException, ex, "Read keystore aliased: !");
    }
    return lst;
  }

  /**
   *
   * @param alias
   * @param cs
   * @return
   */
  public static Properties getKeystoreProperties(String alias, SEDCertStore cs) {
    Properties signProperties = new Properties();
    signProperties.put(SEC_PROVIDER, SEC_PROIDER_MERLIN);
    signProperties.put(SEC_MERLIN_KEYSTORE_ALIAS, alias);
    signProperties.put(SEC_MERLIN_KEYSTORE_PASS, cs.getPassword());
    signProperties.put(SEC_MERLIN_KEYSTORE_FILE, StringFormater.replaceProperties(cs.getFilePath()));
    signProperties.put(SEC_MERLIN_KEYSTORE_TYPE, cs.getType());
    return signProperties;
  }

  /**
   *
   * @param alias
   * @param cs
   * @return
   */
  public static Properties getTruststoreProperties(String alias, SEDCertStore cs) {
    Properties signVerProperties = new Properties();
    signVerProperties.put(SEC_PROVIDER, SEC_PROIDER_MERLIN);
    signVerProperties.put(SEC_MERLIN_TRUSTSTORE_ALIAS, alias);
    signVerProperties.put(SEC_MERLIN_TRUSTSTORE_PASS, cs.getPassword());
    signVerProperties.put(SEC_MERLIN_TRUSTSTORE_FILE, StringFormater.replaceProperties(
        cs.getFilePath()));
    signVerProperties.put(SEC_MERLIN_TRUSTSTORE_TYPE, cs.getType());
    return signVerProperties;
  }


  /**
   *
   * @param alias
   * @param cs
   * @return
   * @throws SEDSecurityException
   */
  public KeyStore.PrivateKeyEntry getPrivateKeyEntryForAlias(String alias, SEDCertStore cs)
      throws SEDSecurityException {

    if (alias == null) {
      throw new SEDSecurityException(CertificateException, "x.509 cert not found in keystore");
    }

    KeyStore.PrivateKeyEntry rsaKey = null;
    try {
      for (SEDCertificate c : cs.getSEDCertificates()) {
        if (c.isKeyEntry() && c.getAlias().equalsIgnoreCase(alias)) {
          rsaKey =
              (KeyStore.PrivateKeyEntry) getKeystore(cs).getEntry(alias,
              new KeyStore.PasswordProtection(c.getKeyPassword().toCharArray()));
          break;
        }
      }

    } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException ex) {
      throw new SEDSecurityException(KeyStoreException, ex, ex.getMessage());
    } catch (UnrecoverableEntryException ex) {
      throw new SEDSecurityException(KeyStoreException, ex, ex.getMessage());
    }
    return rsaKey;
  }

}
