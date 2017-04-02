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
import java.io.StringWriter;
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
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import si.laurentius.cert.SEDCertificate;
import si.laurentius.commons.exception.SEDSecurityException;
import static si.laurentius.commons.exception.SEDSecurityException.SEDSecurityExceptionCode.CertificateException;
import static si.laurentius.commons.exception.SEDSecurityException.SEDSecurityExceptionCode.KeyStoreException;
import static si.laurentius.commons.exception.SEDSecurityException.SEDSecurityExceptionCode.NoSuchAlgorithm;
import static si.laurentius.commons.exception.SEDSecurityException.SEDSecurityExceptionCode.ReadWriteFileException;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StringFormater;
import static java.security.KeyStore.getInstance;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import javax.net.ssl.X509KeyManager;
import si.laurentius.commons.utils.Utils;
import si.laurentius.lce.tls.X509KeyManagerForAlias;



/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public class KeystoreUtils {

  public static final String CF_X509 = "X.509";
  /**
   *
   */
  protected final static SEDLogger LOG = new SEDLogger(KeystoreUtils.class);
  /**
   *
   */
  


  static public boolean isCertValid(SEDCertificate crt) {
    Date currDate = Calendar.getInstance().getTime();
    return crt.getValidTo() != null
            && crt.getValidFrom() != null
            & currDate.after(crt.getValidFrom())
            && currDate.before(crt.getValidTo());
  }

  public String addCertificateToStore(KeyStore ks, Certificate cert,
          String alias, boolean overwrite)
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

  public void addKeyToStore(KeyStore ksTarget, String alias, Key privateKey,
          Certificate[] certs,
          char[] passwd, boolean overwrite)
          throws SEDSecurityException {
    String als = alias;
    try {
      List<String> aliases = Collections.list(ksTarget.aliases());
      if (aliases.contains(alias)) {
        if (overwrite) {
          ksTarget.deleteEntry(alias);
        } else {
          String ind = "_%03d";
          int i = 0;
          do {
            i++;
            als = alias + String.format(ind, i);

          } while (aliases.contains(als));
        }
      }

      ksTarget.setKeyEntry(als, privateKey, passwd, certs);
    } catch (KeyStoreException ex) {
      throw new SEDSecurityException(KeyStoreException, ex, ex.getMessage());
    }
  }

  public void changeAlias(KeyStore ks, String oldAlias,
          String newAlias, String keyPasswd)
          throws SEDSecurityException {

    if (Objects.equals(oldAlias, newAlias)) {
      // nothing to change
      return;
    }
    try {

      if (ks.isKeyEntry(oldAlias)) {
        if (Utils.isEmptyString(keyPasswd)) {
          throw new SEDSecurityException(KeyStoreException, String.format(
                  "Could not change alias from %s to %s because certifikate is key but no password is given.",
                  oldAlias, newAlias));

        }
        Key privateKey = ks.getKey(oldAlias, keyPasswd.toCharArray());
        Certificate[] certs = ks.getCertificateChain(oldAlias);
        ks.setKeyEntry(newAlias, privateKey, keyPasswd.toCharArray(), certs);
      } else {
        Certificate cert = ks.getCertificate(oldAlias);
        ks.setCertificateEntry(newAlias, cert);
      }
      ks.deleteEntry(oldAlias);

    } catch (NoSuchAlgorithmException | KeyStoreException
            | UnrecoverableKeyException ex) {
      throw new SEDSecurityException(ReadWriteFileException, ex, ex.getMessage());
    }

  }

  public KeyStore createNewKeyStore(String initPasswd, String initFilePath) throws SEDSecurityException {
    KeyStore ks = null;
    try (FileOutputStream fos = new FileOutputStream(StringFormater.
            replaceProperties(initFilePath))) {

      ks = KeyStore.getInstance(KeyStore.getDefaultType());
      ks.load(null, initPasswd.toCharArray());
      ks.store(fos, initPasswd.toCharArray());
    } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException ex) {
      throw new SEDSecurityException(KeyStoreException, ex, ex.getMessage());
    } catch (IOException ex) {
      throw new SEDSecurityException(ReadWriteFileException, ex, ex.getMessage());
    }

    return ks;

  }

  public SEDCertificate existsCertInList(List<SEDCertificate> lst,
          SEDCertificate sc) {
    for (SEDCertificate c : lst) {
      if (isEqualCertificateDesc(sc, c)) {
        return c;
      }
    }
    return null;
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
   * Get key managers for SEDCertStore.
   *
   * @param keyStore
   * @param keyStorePass
   * @param alias
   * @return
   * @throws SEDSecurityException
   */
  public KeyManager[] getKeyManagers(KeyStore keyStore, String alias,
          char[] keyStorePass)
          throws SEDSecurityException {

    KeyManagerFactory fac;
    try {
      fac = KeyManagerFactory.getInstance(KeyManagerFactory
              .getDefaultAlgorithm());
    } catch (NoSuchAlgorithmException ex) {
      throw new SEDSecurityException(NoSuchAlgorithm, ex, ex.getMessage());
    }

    try {
      fac.init(keyStore, keyStorePass);
    } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException ex) {
      throw new SEDSecurityException(KeyStoreException, ex,
              "Error init KeyManagerFactory for keystore. Error: "
              + ex.getMessage());
    }
    KeyManager[] kms = fac.getKeyManagers();;
    KeyManager[] kmsres = null;
    // wrap keymanager to X509KeyManagerForAlias
    if (kms != null) {
      List<KeyManager> kmarr = new ArrayList<>();
      for (KeyManager km : kms) {
        if (km instanceof X509KeyManager) {
          kmarr.add(new X509KeyManagerForAlias((X509KeyManager) km, alias));
        }
      }

      kmsres
              = kmarr.toArray(new KeyManager[0]);
    }
    return kmsres;
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
      throw new SEDSecurityException(ReadWriteFileException, ex,
              "Read keystore aliased: !");
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
          ec.setSerialNumber(xc.getSerialNumber() + "");
          ec.setHexSHA1Digest(DigestUtils.getHexSha1Digest(xc.getEncoded()));
        }

        lst.add(ec);
      }
    } catch (KeyStoreException ex) {
      throw new SEDSecurityException(ReadWriteFileException, ex,
              "Read keystore aliased: !");
    } catch (CertificateEncodingException ex) {
      throw new SEDSecurityException(ReadWriteFileException, ex,
              "Error calculating sha digest for certificate!");
    }
    return lst;
  }

  /**
   *
   * @param ks
   * @return
   * @throws SEDSecurityException
   */
  public List<X509Certificate> getKeyStoreX509Certificates(KeyStore ks)
          throws SEDSecurityException {

    List<X509Certificate> lst = new ArrayList<>();
    try {
      Enumeration<String> e = ks.aliases();
      while (e.hasMoreElements()) {
        String alias = e.nextElement();
        Certificate c = ks.getCertificate(alias);
        if (c instanceof X509Certificate) {
          X509Certificate xc = (X509Certificate) c;
          lst.add(xc);
        } else {
          LOG.formatedWarning("Certificate %s is not X509Certificate", alias);
        }
      }
    } catch (KeyStoreException ex) {
      throw new SEDSecurityException(ReadWriteFileException, ex,
              "Read keystore aliased: !");
    }
    return lst;
  }

  /**
   *
   * @param fpath
   * @param type
   * @param pswd
   * @return
   * @throws SEDSecurityException
   */
  public KeyStore getKeystore(File fpath, String type, char[] pswd)
          throws SEDSecurityException {
    KeyStore keyStore = null;
    try (FileInputStream fis = new FileInputStream(fpath)) {
      keyStore = getKeystore(fis, type, pswd);
    } catch (IOException ex) {
      throw new SEDSecurityException(ReadWriteFileException, ex,
              "Read keystore from stream!"
              + ex.getMessage());
    }

    return keyStore;
  }

  public KeyStore getKeystore(File fpath, char[] pswd)
          throws SEDSecurityException {
    return getKeystore(fpath, "JKS", pswd);

  }

  /**
   *
   * @param isTrustStore
   * @param trustStoreType
   * @param password
   * @return
   * @throws SEDSecurityException
   */
  public KeyStore getKeystore(InputStream isTrustStore, String trustStoreType,
          char[] password)
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
      throw new SEDSecurityException(ReadWriteFileException, ex,
              "Read keystore from stream!"
              + ex.getMessage());
    }
    return keyStore;
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
   * @param alias
   * @param passwd
   * @return
   * @throws SEDSecurityException
   */
  public KeyStore.PrivateKeyEntry getPrivateKeyEntryForAlias(KeyStore ks,
          String alias,
          String passwd)
          throws SEDSecurityException {

    if (alias == null) {
      throw new SEDSecurityException(CertificateException,
              "x.509 cert not found in keystore");
    }

    KeyStore.PrivateKeyEntry rsaKey;
    try {
      rsaKey
              = (KeyStore.PrivateKeyEntry) ks.getEntry(alias,
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
      throw new SEDSecurityException(CertificateException,
              "x.509 cert not found in keystore");
    }

    Key rsaKey;
    try {
      rsaKey = ks.getKey(alias,psswd!=null? psswd.toCharArray():null);
      if (rsaKey == null) {
        StringWriter sw = new StringWriter();
        sw.append("No key for alias ");
        sw.append(alias);
        sw.append("in keystore with aliases {");
        Enumeration<String> lst = ks.aliases();
        while (lst.hasMoreElements()) {
          sw.append(lst.nextElement());
          sw.append(",");

        }
        sw.append("}");
        throw new SEDSecurityException(CertificateException, sw.toString());

      }
    } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException ex) {
      throw new SEDSecurityException(KeyStoreException, ex, ex.getMessage());
    }
    return rsaKey;
  }

  /**
   *
   * @param ks
   * @param cert
   * @param psswd
   * @return
   * @throws SEDSecurityException
   */
  public Key getPrivateKeyForX509Cert(KeyStore ks, X509Certificate cert,
          String psswd)
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
   * @return
   * @throws SEDSecurityException
   */
  public TrustManager[] getTrustManagers(KeyStore keyStore)
          throws SEDSecurityException {

    TrustManagerFactory fac;
    try {
      fac = TrustManagerFactory.getInstance(TrustManagerFactory
              .getDefaultAlgorithm());
    } catch (NoSuchAlgorithmException ex) {

      throw new SEDSecurityException(NoSuchAlgorithm, ex, "JKS", ex.
              getMessage());
    }

    try {
      fac.init(keyStore);
    } catch (KeyStoreException ex) {
      throw new SEDSecurityException(KeyStoreException, ex,
              "Error init TrustManagerFactory for keystore. Error: "
              + ex.getMessage());
    }

    return fac.getTrustManagers();

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
   * @param ks
   * @return
   * @throws SEDSecurityException
   *
   */
  public List<X509Certificate> getTrustedCertsFromKeystore(KeyStore ks)
          throws SEDSecurityException {

    List<X509Certificate> lstCr = new ArrayList<>();
    try {
      Enumeration<String> e = ks.aliases();
      while (e.hasMoreElements()) {
        String alias = e.nextElement();
        Certificate c = ks.getCertificate(alias);
        if (c instanceof X509Certificate) {
          lstCr.add((X509Certificate) c);
        }
      }
    } catch (KeyStoreException ex) {
      throw new SEDSecurityException(ReadWriteFileException, ex,
              "Read keystore aliased: !");
    }
    return lstCr;
  }

  public boolean isEqualCertificateDesc(SEDCertificate sc, SEDCertificate other) {
    if (sc == null || other == null) {
      throw new IllegalArgumentException("Compare SED certificate is null!");
    }

    return Objects.equals(sc.getIssuerDN(), other.getIssuerDN())
            && Objects.equals(sc.getSerialNumber(), other.getSerialNumber())
            && Objects.equals(sc.getSubjectDN(), other.getSubjectDN())
            && Objects.equals(sc.getValidFrom(), other.getValidFrom())
            && Objects.equals(sc.getValidTo(), other.getValidTo());

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
    String fp = StringFormater.
            replaceProperties(filepath);
    try (FileInputStream fis = new FileInputStream(fp)) {

      KeyStore keyStore = getInstance(type);

      keyStore.load(fis, password);
      return keyStore;
    } catch (KeyStoreException ex) {
      String msgERR = String.format(
              "KeyStoreException reading keystore: %s! Msg: %s", fp, ex.
                      getMessage());
      throw new SEDSecurityException(KeyStoreException, ex, msgERR);
    } catch (NoSuchAlgorithmException ex) {
      String msgERR = String.format(
              "NoSuchAlgorithm reading keystore: %s! Msg: %s", fp, ex.
                      getMessage());
      throw new SEDSecurityException(NoSuchAlgorithm, ex, msgERR);
    } catch (CertificateException ex) {
      String msgERR = String.format(
              "CertificateException reading keystore: %s! Msg: %s", fp, ex.
                      getMessage());
      throw new SEDSecurityException(CertificateException, ex, msgERR);
    } catch (IOException ex) {
      String msgERR = String.format(
              "ReadWriteFileException reading keystore: %s! Msg: %s", fp, ex.
                      getMessage());
      LOG.logError(msgERR, null);
      throw new SEDSecurityException(ReadWriteFileException, ex, msgERR);
    }
  }

  public boolean testAccessToKey(KeyStore ks, String alias, String passwd) throws SEDSecurityException {

    Key k = getPrivateKeyForAlias(ks, alias, passwd);
    return k != null;
  }

}
