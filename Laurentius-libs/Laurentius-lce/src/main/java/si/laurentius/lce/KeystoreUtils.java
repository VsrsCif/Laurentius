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
package si.laurentius.lce;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import si.laurentius.lce.tls.X509KeyManagerForAlias;
import static java.security.KeyStore.getInstance;
import java.security.cert.CertificateFactory;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import si.laurentius.commons.utils.xml.XMLUtils;

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

  public static final String CF_X509 = "X.509";

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
    for (SEDCertificate c : sc.getSEDCertificates()) {
      if (Objects.equals(c.getAlias(), alias)) {
        cert = c;
        break;
      }
    }
    if (cert == null) {
      throw new SEDSecurityException(
          SEDSecurityException.SEDSecurityExceptionCode.KeyForAliasNotExists, alias);
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
    if (kms != null) {
      List<KeyManager> kmarr = new ArrayList<>();
      for (KeyManager km : kms) {
        if (km instanceof X509KeyManager) {
          kmarr.add(new X509KeyManagerForAlias((X509KeyManager) km, alias));
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
      k = getPrivateKeyForX509Cert(cs, cert);
      if (k != null) {
        break;
      }
    }
    return k;
  }

  public Key getPrivateKeyForX509Cert(SEDCertStore cs, X509Certificate cert)
      throws SEDSecurityException {

    // find alias
    String alias = null;
    Key k = null;

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
   * @param sc
   * @param alias
   * @return
   * @throws SEDSecurityException
   */
  public X509Certificate getTrustedCertForAlias(SEDCertStore sc, String alias)
      throws SEDSecurityException {
    return getTrustedCertForAlias(getKeystore(sc), alias);
  }

  /**
   *
   * @param certStream
   * @return
   * @throws SEDSecurityException
   */
  public X509Certificate getCertFromInputStream(InputStream certStream)
      throws SEDSecurityException {

    X509Certificate c = null;
    try {
      CertificateFactory cf = CertificateFactory.getInstance(CF_X509);
      c = (X509Certificate) cf.generateCertificate(certStream);
      certStream.close();
    } catch (CertificateException | IOException ex) {
      throw new SEDSecurityException(CertificateException, ex, ex.getMessage());
    }
    return c;
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

  public SEDCertStore getSEDCertstore(File fstore, String keystoreType, String password)
      throws SEDSecurityException {
    SEDCertStore sc = new SEDCertStore();
    sc.setFilePath(fstore.getAbsolutePath());
    sc.setType(keystoreType);
    sc.setPassword(password);
    KeyStore ks = getKeystore(sc);
    sc.getSEDCertificates().addAll(getKeyStoreSEDCertificates(ks));
    return sc;
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
          ec.setSerialNumber(xc.getSerialNumber() + "");

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

  static public boolean isCertValid(SEDCertificate crt) {
    Date currDate = Calendar.getInstance().getTime();
    return crt.getValidTo() != null &&
        crt.getValidFrom() != null &
        currDate.after(crt.getValidFrom()) &&
        currDate.before(crt.getValidTo());
  }

  public SEDCertificate addCertificateToStore(SEDCertStore sc, InputStream certStream, String alias,
      boolean overwrite)
      throws SEDSecurityException {

    SEDCertificate ec = null;
    try {

      CertificateFactory cf = CertificateFactory.getInstance(CF_X509);
      Certificate c = cf.generateCertificate(certStream);
      certStream.close();
      ec = addCertificateToStore(sc, c, alias, overwrite);
    } catch (IOException | CertificateException ex) {
      throw new SEDSecurityException(CertificateException, ex, ex.getMessage());
    }
    return ec;
  }

  public SEDCertificate addCertificateToStore(SEDCertStore sc, Certificate c, String alias,
      boolean overwrite)
      throws SEDSecurityException {

    SEDCertificate ec = null;
    KeyStore ks = getKeystore(sc);
    try {

      SEDCertificate existsCert = null;
      for (SEDCertificate ct : sc.getSEDCertificates()) {
        if (Objects.equals(alias, ct.getAlias())) {
          existsCert = ct;
          break;
        }
      }

      // add to keystore
      String als = addCertificateToStore(ks, c, alias, overwrite);

      ec = existsCert != null && overwrite ? existsCert : new SEDCertificate();

      ec.setKeyEntry(ks.isKeyEntry(alias));
      ec.setAlias(als);
      ec.setType(c.getType());
      if (c instanceof X509Certificate) {
        X509Certificate xc = (X509Certificate) c;
        ec.setValidFrom(xc.getNotBefore());
        ec.setValidTo(xc.getNotAfter());
        ec.setIssuerDN(xc.getIssuerX500Principal().getName());
        ec.setSubjectDN(xc.getSubjectX500Principal().getName());
        ec.setSerialNumber(xc.getSerialNumber() + "");
      } else {
        ec.setValidFrom(null);
        ec.setValidTo(null);
        ec.setIssuerDN(null);
        ec.setSubjectDN(null);
        ec.setSerialNumber(null);
      }

      // Write out the keystore
      try (
          FileOutputStream keyStoreOutputStream =
          new FileOutputStream(StringFormater.replaceProperties(sc.getFilePath()))) {
        ks.store(keyStoreOutputStream, sc.getPassword().toCharArray());

      }
      if (ec != existsCert) {
        sc.getSEDCertificates().add(ec);
      }

    } catch (CertificateException ex) {
      throw new SEDSecurityException(CertificateException, ex, ex.getMessage());
    } catch (IOException | KeyStoreException ex) {
      throw new SEDSecurityException(ReadWriteFileException, ex, ex.getMessage());
    } catch (NoSuchAlgorithmException ex) {
      throw new SEDSecurityException(NoSuchAlgorithm, ex, ex.getMessage());
    }
    return ec;
  }

  public String addCertificateToStore(KeyStore ks, Certificate cert, String alias, boolean overwrite)
      throws SEDSecurityException {

    String als = alias;
    try {

      List<String> aliases = Collections.list(ks.aliases());
      if (aliases.contains(alias)) {
        if (overwrite) {
          ks.deleteEntry(alias);
        } else {
          String ind = "_%03d";
          int i = 0;
          do {
            i++;
            als = alias + String.format(ind, i);

          } while (aliases.contains(als));
        }
      }
      ks.setCertificateEntry(als, cert);
    } catch (KeyStoreException ex) {
      throw new SEDSecurityException(KeyStoreException, ex, ex.getMessage());
    }
    return als;
  }

  public SEDCertificate removeCertificateFromStore(SEDCertStore sc, String alias)
      throws SEDSecurityException {

    SEDCertificate existsCert = null;
    for (SEDCertificate ct : sc.getSEDCertificates()) {
      if (Objects.equals(alias, ct.getAlias())) {
        existsCert = ct;
        break;
      }
    }
    KeyStore ks = getKeystore(sc);
    try {
      ks.deleteEntry(alias);
      for (SEDCertificate ct : sc.getSEDCertificates()) {
        if (Objects.equals(alias, ct.getAlias())) {
          existsCert = ct;
          sc.getSEDCertificates().remove(existsCert);
          try (
              FileOutputStream keyStoreOutputStream =
              new FileOutputStream(StringFormater.replaceProperties(sc.getFilePath()))) {
            ks.store(keyStoreOutputStream, sc.getPassword().toCharArray());
          }

          break;
        }
      }
    } catch (CertificateException ex) {
      throw new SEDSecurityException(CertificateException, ex, ex.getMessage());
    } catch (IOException | KeyStoreException ex) {
      throw new SEDSecurityException(ReadWriteFileException, ex, ex.getMessage());
    } catch (NoSuchAlgorithmException ex) {
      throw new SEDSecurityException(NoSuchAlgorithm, ex, ex.getMessage());
    }
    return existsCert;
  }

  public SEDCertificate changeAlias(SEDCertStore sc, String oldAlias, String newAlias)
      throws SEDSecurityException, CertificateException {

    SEDCertificate existsCert = null;
    for (SEDCertificate ct : sc.getSEDCertificates()) {
      if (Objects.equals(oldAlias, ct.getAlias())) {
        existsCert = ct;
        break;
      }
    }
    if (existsCert == null) {
      return existsCert;
    }

    if (Objects.equals(oldAlias, newAlias)) {
      return existsCert;
    }

    try {
      KeyStore ks = getKeystore(sc);
      if (existsCert.isKeyEntry()) {
        Key privateKey = ks.getKey(oldAlias, existsCert.getKeyPassword().toCharArray());
        Certificate[] certs = ks.getCertificateChain(oldAlias);
        ks.setKeyEntry(newAlias, privateKey, existsCert.getKeyPassword().toCharArray(), certs);
      } else {
        Certificate cert = ks.getCertificate(oldAlias);
        ks.setCertificateEntry(newAlias, cert);
      }
      ks.deleteEntry(oldAlias);

      try (
          FileOutputStream keyStoreOutputStream =
          new FileOutputStream(StringFormater.replaceProperties(sc.getFilePath()))) {
        ks.store(keyStoreOutputStream, sc.getPassword().toCharArray());
      }
    } catch (IOException | NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException ex) {
      throw new SEDSecurityException(ReadWriteFileException, ex, ex.getMessage());
    }
    return existsCert;
  }

  public void mergeCertStores(SEDCertStore target, SEDCertStore source, boolean onlyKeys,
      boolean onlyPublicKeys)
      throws SEDSecurityException, CertificateException {

    try {
      KeyStore ksTarget = getKeystore(target);
      KeyStore ksSrc = getKeystore(source);

      for (SEDCertificate c : source.getSEDCertificates()) {
        if (onlyKeys && !c.isKeyEntry()) {
          continue;
        }
        String alias = c.getAlias();
        String als = alias;
        String ind = "_%03d";
        int i = 0;
        while (ksTarget.containsAlias(als)) {
          i++;
          als = alias + String.format(ind, i);

        }

        if (!onlyPublicKeys && ksSrc.isKeyEntry(c.getAlias())) {
          Key privateKey = ksSrc.getKey(alias, c.getKeyPassword().toCharArray());
          Certificate[] certs = ksSrc.getCertificateChain(alias);
          ksTarget.setKeyEntry(als, privateKey, c.getKeyPassword().toCharArray(), certs);
        } else {
          Certificate cert = ksSrc.getCertificate(alias);
          ksTarget.setCertificateEntry(als, cert);
        }
        SEDCertificate sc = XMLUtils.deepCopyJAXB(c);
        sc.setAlias(als);
        target.getSEDCertificates().add(sc);
      }

      try (
          FileOutputStream keyStoreOutputStream =
          new FileOutputStream(StringFormater.replaceProperties(target.getFilePath()))) {
        ksTarget.store(keyStoreOutputStream, target.getPassword().toCharArray());
      }
    } catch (IOException | NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException ex) {
      throw new SEDSecurityException(ReadWriteFileException, ex, ex.getMessage());
    }
  }

  public void refreshCertStore(SEDCertStore keystore)
      throws SEDSecurityException {

    if (keystore != null) {

      List<SEDCertificate> src = keystore.getSEDCertificates();

      KeyStore ks =
          openKeyStore(keystore.getFilePath(), keystore.getType(), keystore
              .getPassword().toCharArray());
      List<SEDCertificate> lstals = getKeyStoreSEDCertificates(ks);

      lstals.stream().forEach((ksc) -> {
        SEDCertificate sc = existsCertInList(src, ksc);
        if (sc != null) {
          sc.setStatus("OK");
        } else {
          ksc.setStatus("NEW");
          src.add(ksc);
        }
      });
      src.stream().forEach((sc) -> {
        SEDCertificate ksc = existsCertInList(src, sc);
        if (ksc == null) {
          sc.setStatus("DEL");
        }
      });
      keystore.setStatus("SUCCESS");

    }

  }

  public SEDCertificate existsCertInList(List<SEDCertificate> lst, SEDCertificate sc) {
    for (SEDCertificate c : lst) {
      if (Objects.equals(c.getAlias(), c.getAlias()) &&
          Objects.equals(c.getIssuerDN(), sc.getIssuerDN()) &&
          Objects.equals(c.getSubjectDN(), sc.getSubjectDN()) &&
          c.getSerialNumber().equals(sc.getSerialNumber())) {
        return c;
      }
    }
    return null;
  }
}
