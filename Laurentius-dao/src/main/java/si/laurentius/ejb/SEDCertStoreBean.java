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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Proxy;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CRLException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.NamingException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import si.laurentius.cert.SEDCertPassword;
import si.laurentius.cert.SEDCertificate;
import si.laurentius.cert.crl.SEDCertCRL;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.exception.SEDSecurityException;
import static si.laurentius.commons.exception.SEDSecurityException.SEDSecurityExceptionCode.CertificateException;
import static si.laurentius.commons.exception.SEDSecurityException.SEDSecurityExceptionCode.KeyStoreException;
import static si.laurentius.commons.exception.SEDSecurityException.SEDSecurityExceptionCode.NoSuchAlgorithm;
import static si.laurentius.commons.exception.SEDSecurityException.SEDSecurityExceptionCode.ReadWriteFileException;
import si.laurentius.commons.interfaces.SEDCertStoreInterface;
import si.laurentius.commons.utils.FileUtils;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.lce.DigestUtils;
import si.laurentius.lce.KeystoreUtils;
import si.laurentius.lce.crl.CRLVerifier;
import si.laurentius.lce.tls.X509KeyManagerForAlias;
import si.laurentius.lce.tls.X509TrustManagerForAlias;

/**
 * @author Jože Rihtaršič
 */
@Startup
@Singleton
@Local(SEDCertStoreInterface.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class SEDCertStoreBean implements SEDCertStoreInterface {

  public static final String KS_INIT_KEYSTORE_PASSWD = "passwd1234";

  private static final SEDLogger LOG = new SEDLogger(SEDCertStoreBean.class);

  @PersistenceContext(unitName = "ebMS_LAU_PU", name = "ebMS_LAU_PU")
  public EntityManager memEManager;
  private final Map<String, String> mhmPswd = new HashMap<>();

  private KeystoreUtils mku = new KeystoreUtils();
  private long mlastRefreshTime = 0;
  List<SEDCertificate> mlstCertificates = new ArrayList<>();
  List<SEDCertificate> mlstRootCA = new ArrayList<>();

  Map<String, SEDCertCRL> mlstCertCRL = new Hashtable<>();

  KeyStore mCertStore = null;
  KeyStore mRootCAStore = null;

  public static final String SEC_MERLIN_KEYSTORE_ALIAS
          = "org.apache.ws.security.crypto.merlin.keystore.alias";

  /**
   *
   */
  public static final String SEC_MERLIN_KEYSTORE_FILE
          = "org.apache.ws.security.crypto.merlin.keystore.file";

  /**
   *
   */
  public static final String SEC_MERLIN_KEYSTORE_PASS
          = "org.apache.ws.security.crypto.merlin.keystore.password";
  /**
   *
   */
  public static final String SEC_MERLIN_KEYSTORE_TYPE
          = "org.apache.ws.security.crypto.merlin.keystore.type";
  /**
   *
   */
  public static final String SEC_MERLIN_TRUSTSTORE_ALIAS
          = "org.apache.ws.security.crypto.merlin.truststore.alias";

  /**
   *
   */
  public static final String SEC_MERLIN_TRUSTSTORE_FILE
          = "org.apache.ws.security.crypto.merlin.truststore.file";

  /**
   *
   */
  public static final String SEC_MERLIN_TRUSTSTORE_PASS
          = "org.apache.ws.security.crypto.merlin.truststore.password";
  /**
   *
   */
  public static final String SEC_MERLIN_TRUSTSTORE_TYPE
          = "org.apache.ws.security.crypto.merlin.truststore.type";
  /**
   *
   */
  public static final String SEC_PROIDER_MERLIN = "org.apache.wss4j.common.crypto.Merlin";
  /**
   *
   */
  public static final String SEC_PROVIDER = "org.apache.ws.security.crypto.provider";

  /**
   *
   */
  @Resource
  public UserTransaction mutUTransaction;

  /**
   *
   * @param crt
   * @param alias
   * @throws SEDSecurityException
   */
  @Override
  public void addCertToCertStore(X509Certificate crt, String alias) throws SEDSecurityException {
    KeyStore ks = getCertStore();
    mku.addCertificateToStore(ks, crt, alias, false);
    saveKeystore(ks, KEYSTORE_NAME);
    refreshCertificates();
  }

  /**
   *
   * @param crt
   * @param alias
   * @throws SEDSecurityException
   */
  @Override
  public void addCertToRootCA(X509Certificate crt, String alias) throws SEDSecurityException {
    KeyStore ks = getRootCAStore();
    mku.addCertificateToStore(ks, crt, alias, false);
    saveKeystore(ks, ROOTCA_NAME);
    refreshRootCACertificates();
  }

  /**
   *
   * @param ksTarget
   * @param alias
   * @param privateKey
   * @param certs
   * @param passwd
   * @throws SEDSecurityException
   */
  @Override
  public void addKeyToToCertStore(String alias, Key privateKey,
          Certificate[] certs, String passwd)
          throws SEDSecurityException {
    KeyStore ks = getCertStore();
    mku.addKeyToStore(ks, alias, privateKey, certs, passwd != null ? passwd.
            toCharArray() : null, false);
    addPassword(alias, passwd);
    saveKeystore(ks, KEYSTORE_NAME);
    refreshCertificates();
  }

  /**
   *
   * @param alias
   * @param pswd
   * @throws SEDSecurityException
   */
  @Override
  public void addPassword(String alias, String pswd) throws SEDSecurityException {
    long l = LOG.logStart(alias);

    SEDCertPassword cp = new SEDCertPassword();
    cp.setAlias(alias);
    cp.setPassword(pswd);
    boolean suc = false;
    try {
      TypedQuery<SEDCertPassword> tg
              = memEManager.createNamedQuery(
                      SEDCertPassword.class.getName() + ".getByAlias",
                      SEDCertPassword.class);
      tg.setParameter("alias", alias);
      try {
        SEDCertPassword dbCP = tg.getSingleResult();
        if (!Objects.equals(pswd, dbCP.getPassword())) {
          dbCP.setPassword(pswd);
          mutUTransaction.begin();
          memEManager.merge(cp);
          mutUTransaction.commit();
          mhmPswd.put(alias, pswd);
        }

      } catch (NoResultException nre) {
        mutUTransaction.begin();
        memEManager.persist(cp);
        mutUTransaction.commit();
        mhmPswd.put(alias, pswd);

      }

      suc = true;
    } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException
            | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
      String msg = String.format(
              "Error occured whole saving passwd for alias %s. Err %s", alias,
              ex.getMessage());
      LOG.logError(l, msg, null);
      try {

        mutUTransaction.rollback();
      } catch (IllegalStateException | SecurityException | SystemException ex1) {
        LOG.logWarn(l, "Rollback failed", ex1);
      }

      throw new SEDSecurityException(
              SEDSecurityException.SEDSecurityExceptionCode.InitializeException,
              ex,
              msg);
    }

  }

  /**
   *
   * @param oldAlias
   * @param newAlias
   * @throws SEDSecurityException
   */
  @Override
  public void changeAlias(String oldAlias, String newAlias)
          throws SEDSecurityException {

    List<SEDCertificate> lstCert = getCertificates();
    for (SEDCertificate c : lstCert) {
      if (Objects.equals(c.getAlias(), oldAlias)) {
        changeAliasForCertificate(c, newAlias);
        return;
      }
    }
    throw new SEDSecurityException(
            SEDSecurityException.SEDSecurityExceptionCode.CertificateException,
            String.format("Certificate for alias %s not exist!", oldAlias)
    );
  }

  public void changeCertAlias(String oldAlias, String newAlias) {

  }

  /**
   *
   * @param oldAlias
   * @param newAlias
   * @throws SEDSecurityException
   */
  public void changeAliasForCertificate(SEDCertificate cert,
          final String newAlias) throws SEDSecurityException {
    long l = LOG.logStart(cert.getAlias());
    KeyStore ks = getCertStore();

    if (cert.isKeyEntry()) {
      String oldAlias = cert.getAlias();
      try {
        String paswd = mhmPswd.get(oldAlias);
        TypedQuery<SEDCertPassword> tg
                = memEManager.createNamedQuery(
                        SEDCertPassword.class.getName() + ".getByAlias",
                        SEDCertPassword.class);
        tg.setParameter("alias", cert.getAlias());

        mutUTransaction.begin();
        SEDCertPassword dbCP = null;
        try {
          dbCP = tg.getSingleResult();
          dbCP.setAlias(newAlias);
          memEManager.merge(dbCP);
        } catch (NoResultException nre) {
          LOG.formatedWarning("Passoword for alias %s not exist in database!",
                  oldAlias);
          SEDCertPassword cp = new SEDCertPassword();
          cp.setAlias(newAlias);
          cp.setPassword(mhmPswd.get(oldAlias));
          memEManager.persist(cp);
        }
        // change alias in keystore
        mku.changeAlias(ks, cert.getAlias(), newAlias, paswd);
        saveKeystore(ks, KEYSTORE_NAME);
        mutUTransaction.commit();

        // change password in cache
        mhmPswd.remove(oldAlias);
        mhmPswd.put(newAlias, paswd);
        cert.setAlias(newAlias);

      } catch (SEDSecurityException | NotSupportedException | SystemException | RollbackException | HeuristicMixedException
              | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
        String msg = String.format(
                "Error occured whole saving passwd for alias %s. Err %s",
                oldAlias,
                ex.getMessage());
        LOG.logError(l, msg, null);
        try {

          mutUTransaction.rollback();
        } catch (IllegalStateException | SecurityException | SystemException ex1) {
          LOG.logWarn(l, "Rollback failed", ex1);
        }

        throw new SEDSecurityException(
                SEDSecurityException.SEDSecurityExceptionCode.InitializeException,
                ex,
                msg);
      }

    } else {
      // change alias in keystore
      mku.changeAlias(ks, cert.getAlias(), newAlias, null);
      saveKeystore(ks, KEYSTORE_NAME);
      cert.setAlias(newAlias);

    }

    
  }

  /**
   *
   * @param oldAlias
   * @param newAlias
   * @throws SEDSecurityException
   */
  @Override
  public void changeRootCAAlias(String oldAlias, String newAlias) throws SEDSecurityException {
    KeyStore ks = getRootCAStore();
    mku.changeAlias(ks, oldAlias, newAlias, null);
    saveKeystore(ks, ROOTCA_NAME);
  }

  /**
   *
   * @param newPasswd
   * @throws SEDSecurityException
   */
  @Override
  public void changeKeystorePassword(String newPasswd) throws SEDSecurityException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  /**
   *
   * @param newPasswd
   * @throws SEDSecurityException
   */
  @Override
  public void changeRootCAPassword(String newPasswd) throws SEDSecurityException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  /**
   *
   * @return
   */
  @Override
  public List<SEDCertificate> getCertificates() {
    refreshData();
    return mlstCertificates;

  }

  @Override
  public SEDCertPassword getKeyPassword(String alias) {

    if (Utils.isEmptyString(alias)) {
      throw new IllegalArgumentException("Alias must not be null!");
    }

    if (!mhmPswd.containsKey(alias)) {
      throw new IllegalArgumentException(
              String.format(
                      "Certificate for alias %s does not have defined password!",
                      alias));
    }
    SEDCertPassword cp = new SEDCertPassword();
    cp.setAlias(alias);
    cp.setPassword(mhmPswd.get(alias));
    return cp;

  }

  @Override
  public List<SEDCertificate> getRootCACertificates() {
    refreshData();
    return mlstRootCA;

  }

  /**
   *
   * @param onlyKeys
   * @return
   */
  @Override
  public List<String> getKeystoreAliases(boolean onlyKeys) {
    List<String> lst = new ArrayList<>();
    List<SEDCertificate> lc = getCertificates();
    for (SEDCertificate c : lc) {
      if (onlyKeys) {
        if (c.isKeyEntry()) {
          lst.add(c.getAlias());
        }
      } else {
        lst.add(c.getAlias());
      }

    }
    return lst;
  }

  /**
   *
   * @param alias
   * @return
   */
  @Override
  public PrivateKey getPrivateKeyForAlias(String alias) throws SEDSecurityException {
    return (PrivateKey) mku.getPrivateKeyForAlias(getCertStore(), alias,
            mhmPswd.get(alias));

  }

  /**
   *
   * @param xrc
   * @return
   */
  @Override
  public PrivateKey getPrivateKeyForX509Cert(X509Certificate xrc) throws SEDSecurityException {
    KeyStore ks = getCertStore();
    String alias = mku.getPrivateKeyAliasForX509Cert(ks, xrc);
    return (PrivateKey) mku.getPrivateKeyForAlias(ks, alias, mhmPswd.get(alias));
  }

  /**
   *
   * @return @throws si.laurentius.commons.exception.SEDSecurityException
   */
  @Override
  public List<X509Certificate> getRootCA509Certs() throws SEDSecurityException {
    return mku.getKeyStoreX509Certificates(getRootCAStore());

  }

  /**
   *
   * @return
   */
  @Override
  public List<SEDCertCRL> getSEDCertCRLs() {
    return new ArrayList(mlstCertCRL.values());
  }

  /**
   *
   * @param alias
   * @return
   */
  @Override
  public SEDCertificate getSEDCertificatForAlias(String alias) {
    for (SEDCertificate sc : mlstCertificates) {
      if (Objects.equals(sc.getAlias(), alias)) {
        return sc;
      }
    }
    return null;
  }

  @Override
  public X509TrustManager getTrustManagerForAlias(String alias,
          boolean validateRootCA) throws SEDSecurityException {
    return new X509TrustManagerForAlias(
            getX509CertForAlias(alias),
            validateRootCA ? getRootCA509Certs() : null);
  }

  @Override
  public X509KeyManager[] getKeyManagerForAlias(String alias) throws SEDSecurityException {
    KeyManagerFactory fac;
    try {
      fac = KeyManagerFactory.getInstance(KeyManagerFactory
              .getDefaultAlgorithm());
    } catch (NoSuchAlgorithmException ex) {
      throw new SEDSecurityException(NoSuchAlgorithm, ex, ex.getMessage());
    }

    try {
      fac.init(getCertStore(), mhmPswd.get(alias).toCharArray());
    } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException ex) {
      throw new SEDSecurityException(KeyStoreException, ex,
              "Error init KeyManagerFactory for keystore. Error: "
              + ex.getMessage());
    }

    KeyManager[] kms = fac.getKeyManagers();;
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
    return kmsres;

  }

  ;

  /**
   *
   * @param alias
   * @return
   */
  @Override
  public X509Certificate getX509CertForAlias(String alias) throws SEDSecurityException {
    KeyStore ks = getCertStore();
    return mku.getTrustedCertForAlias(ks, alias);
  }

  public void initPasswords() {

    TypedQuery<SEDCertPassword> cpq = memEManager.createNamedQuery(
            SEDCertPassword.class
                    .getName() + ".getAll", SEDCertPassword.class
    );
    List<SEDCertPassword> lst = cpq.getResultList();
    mhmPswd.clear();
    for (SEDCertPassword cp : lst) {
      mhmPswd.put(cp.getAlias(), cp.getAlias());
    }

  }

  /**
   *
   */
  @Override
  public void refreshCrlLists() {
    mlstCertCRL.clear();

    try {
      KeyStore ks = getCertStore();
      Enumeration<String> alsEnum = ks.aliases();
      while (alsEnum.hasMoreElements()) {
        String alias = alsEnum.nextElement();
        X509Certificate x509 = (X509Certificate) ks.getCertificate(alias);
        getCrlForCert(x509, true);
      }

    } catch (SEDSecurityException | KeyStoreException ex) {
      LOG.logError(ex.getMessage(), ex);
    }
  }

  public SEDCertCRL getCrlForCert(X509Certificate x509, boolean forceRefresh) {
    SEDCertCRL crlExists = null;
    try {
      SEDCertCRL crl = CRLVerifier.getCrlData(x509);

      if (!forceRefresh && mlstCertCRL.containsKey(crl.getIssuerDigestValue())) {
        SEDCertCRL cacheTest = mlstCertCRL.get(crl.getIssuerDigestValue());
        if (cacheTest.getNextUpdateDate().after(Calendar.getInstance().
                getTime())) {

          // check if at least one url match
          for (SEDCertCRL.Url uex : cacheTest.getUrls()) {
            for (SEDCertCRL.Url u : crl.getUrls()) {
              if (Objects.equals(uex.getValue(), u.getValue())) {
                crlExists = cacheTest;
                break;
              }
            }
            if (crlExists != null) {
              break;
            }
          }
        }
      }
      if (crlExists == null || Utils.isEmptyString(crlExists.getFilePath())) {
        if (updateCrlCache(crl)) {
          mlstCertCRL.put(crl.getIssuerDigestValue(), crl);
          crlExists = crl;
        }
      }
    } catch (CertificateParsingException | IOException ex) {
      LOG.logError("Error updating CRL cache", ex);
    }
    return crlExists;

  }

  public Boolean isCertificateRevoked(X509Certificate x509Cert) {

    assert x509Cert != null : "Null certificat parameter";

    SEDCertCRL crlData = getCrlForCert(x509Cert, false);
    X509CRL crl = null;
    if (crlData != null) {
      crl = CRLVerifier.getCRLFromFile(crlData.getFilePath());
    }

    if (crl != null) {
      return crl.isRevoked(x509Cert);
    } else {
      LOG.formatedWarning(
              "Could not validate CRL for certificate %s, serial %d.", x509Cert.
                      getSubjectX500Principal().getName(), x509Cert.
                      getSerialNumber());
    }
    return null;

  }

  /**
   * Refresh data from keystore and trusted root ca store.
   */
  private void refreshData() {
    File fStore = SEDSystemProperties.getCertstoreFile();
    File fRootCA = SEDSystemProperties.getRootCAStoreFile();

    if (mlastRefreshTime < fStore.lastModified() || mlastRefreshTime < fRootCA.
            lastModified()) {
      refreshCertificates();
      refreshRootCACertificates();
      refreshCrlLists();
      // refresh keystore

      mlastRefreshTime = Calendar.getInstance().getTimeInMillis();

    }

  }

  private void refreshCertificates() {
    mlstCertificates.clear();
    try {
      KeyStore ks = getCertStore();
      mlstCertificates.addAll(mku.getKeyStoreSEDCertificates(ks));
    } catch (SEDSecurityException ex) {
      LOG.logError("Error opening keystore", ex);
    }
  }

  private void refreshRootCACertificates() {
    mlstRootCA.clear();
    try {
      KeyStore ksRCA = getRootCAStore();
      mlstRootCA.addAll(mku.getKeyStoreSEDCertificates(ksRCA));
    } catch (SEDSecurityException ex) {
      LOG.logError("Error opening keystore", ex);
    }
  }

  /**
   *
   * @param crt
   * @throws SEDSecurityException
   */
  @Override
  public void removeCertificateFromRootCAStore(SEDCertificate crt) throws SEDSecurityException {
    KeyStore ks = getRootCAStore();
    try {
      ks.deleteEntry(crt.getAlias());
    } catch (KeyStoreException ex) {
      throw new SEDSecurityException(CertificateException, ex, ex.getMessage());
    }
    saveKeystore(ks, ROOTCA_NAME);
    refreshRootCACertificates();
  }

  /**
   *
   * @param crt
   * @throws SEDSecurityException
   */
  @Override
  public void removeCertificateFromStore(SEDCertificate crt) throws SEDSecurityException {

    KeyStore ks = getCertStore();
    try {

      ks.deleteEntry(crt.getAlias());
    } catch (KeyStoreException ex) {
      throw new SEDSecurityException(CertificateException, ex, ex.getMessage());
    }
    // remove password
    if (mhmPswd.containsKey(crt.getAlias())) {
      removePasswordForAlias(crt.getAlias());
    }

    saveKeystore(ks, KEYSTORE_NAME);
    refreshCertificates();

  }

  private void removePasswordForAlias(String alias) {
    try {
      TypedQuery<SEDCertPassword> tg
              = memEManager.createNamedQuery(
                      SEDCertPassword.class
                              .getName() + ".getByAlias",
                      SEDCertPassword.class
              );
      tg.setParameter("alias", alias);

      try {
        mutUTransaction.begin();
        SEDCertPassword dbCP = tg.getSingleResult();

        memEManager.remove(memEManager.contains(dbCP) ? dbCP
                : memEManager.merge(dbCP));
        mutUTransaction.commit();

      } catch (NoResultException nre) {
        LOG.formatedWarning("Password for alias %s not exist in database!",
                alias);

      }
    } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
      LOG.logError("Error deleting password from  database!",
              ex);
      try {
        mutUTransaction.rollback();
      } catch (IllegalStateException | SecurityException | SystemException ex1) {
        LOG.logWarn("Rollback error!", ex);
      }
    }
    if (mhmPswd.containsKey(alias)) {
      mhmPswd.remove(alias);
    }
  }

  /**
   * Method opens cert store. If cert store not exists it creates new one.
   *
   * @return KeyStore
   * @throws SEDSecurityException
   */
  protected KeyStore getCertStore() throws SEDSecurityException {

    if (mCertStore == null) {
      File fStore = SEDSystemProperties.getCertstoreFile();
      String psswd = mhmPswd.get(KEYSTORE_NAME);
      mCertStore = openKeystore(fStore, KEYSTORE_NAME, Utils.isEmptyString(
              psswd) ? null : psswd.toCharArray());

    }

    return mCertStore;
  }

  /**
   * Method opens Root CA store. If store not exists it creates new one.
   *
   * @return KeyStore
   * @throws SEDSecurityException
   */
  protected KeyStore getRootCAStore() throws SEDSecurityException {
    if (mRootCAStore == null) {
      File fStore = SEDSystemProperties.getRootCAStoreFile();
      String psswd = mhmPswd.get(ROOTCA_NAME);
      mRootCAStore = openKeystore(fStore, ROOTCA_NAME, Utils.isEmptyString(
              psswd) ? null : psswd.toCharArray());

    }
    return mRootCAStore;
  }

  private KeyStore openKeystore(File fStore, String alias, char[] psswd) throws SEDSecurityException {
    KeyStore ks = null;
    if (!fStore.exists()) {
      LOG.formatedWarning(
              "Keystore %s not exists! Keystore with 'def password' will be created!",
              fStore.getAbsolutePath());
      try {
        ks = mku.createNewKeyStore(KS_INIT_KEYSTORE_PASSWD, fStore.
                getAbsolutePath());
        addPassword(alias, KS_INIT_KEYSTORE_PASSWD);

      } catch (SEDSecurityException ex) {
        LOG.formatedWarning(
                "Error occured while creating keystore %s, Error: %s!",
                fStore.getAbsolutePath(), ex.getMessage());
        throw ex;
      }
    } else {
      if (psswd == null) {
        LOG.formatedWarning("Keystore password for alias %s is null! "
                + "Could not open keystore. Backup keystore and create new keystore!",
                alias);
        try {
          // create backup -file 
          FileUtils.backupFile(fStore);
          // create new keystore
          ks = mku.createNewKeyStore(KS_INIT_KEYSTORE_PASSWD, fStore.
                  getAbsolutePath());
          addPassword(alias, KS_INIT_KEYSTORE_PASSWD);

        } catch (IOException ex) {
          LOG.formatedWarning(
                  "Error occured while creating backup %s, error: %s!",
                  fStore.getAbsolutePath(), ex.getMessage());
          throw new SEDSecurityException(ReadWriteFileException, ex, ex.
                  getMessage());
        }

      } else {
        ks = mku.getKeystore(fStore, psswd);
      }
    }

    return ks;
  }

  private void saveKeystore(KeyStore ks, String alias) throws SEDSecurityException {
    File fStore = null;
    String psswd = mhmPswd.get(alias);

    if (alias.equals(KEYSTORE_NAME)) {
      fStore = SEDSystemProperties.getCertstoreFile();
    } else if (alias.equals(ROOTCA_NAME)) {
      fStore = SEDSystemProperties.getRootCAStoreFile();
    } else {
      throw new IllegalArgumentException(String.format(
              "Uknown certstore alias %s", alias));
    }

    try {
      FileUtils.backupFile(fStore);
    } catch (IOException ex) {
      throw new IllegalArgumentException(String.format(
              "Uknown certstore alias %s", alias));
    }

    try (FileOutputStream keyStoreOutputStream
            = new FileOutputStream(fStore)) {
      ks.store(keyStoreOutputStream, psswd.toCharArray());

    } catch (IOException | NoSuchAlgorithmException | KeyStoreException
            | CertificateException ex) {
      throw new SEDSecurityException(ReadWriteFileException, ex, ex.getMessage());
    }
  }

  public boolean updateCrlCache(SEDCertCRL crl) {
    X509CRL cres = null;
    boolean bSuc = false;
    for (SEDCertCRL.Url u : crl.getUrls()) {
      try {
        cres = CRLVerifier.downloadCRL(u.getValue(), null);
      } catch (IOException | CertificateException | CRLException | NamingException ex) {
        String msg = String.format("Error retrieving CRL Cache for %s url:",
                crl.getIssuerDN(), u.getValue());
        LOG.logError(msg, ex);
      }
      if (cres != null) {
        break;
      }
    }
    if (cres != null) {

      try {

        File froot = SEDSystemProperties.getCRLFolder();
        File f = File.createTempFile("CRL_", ".crl", froot);
        try (FileOutputStream fos = new FileOutputStream(f)) {
          fos.write(cres.getEncoded());
        }
        crl.setNextUpdateDate(cres.getNextUpdate());
        crl.setEffectiveDate(cres.getThisUpdate());
        crl.setRetrievedDate(Calendar.getInstance().getTime());
        crl.setFilePath(f.getAbsolutePath());
        bSuc = true;
      } catch (CRLException | IOException ex) {
        String msg = String.format("Error saving CRL cache for %s. Error %s",
                crl.getIssuerDN(), ex.getMessage());
        LOG.logError(msg, ex);
      }

    }
    return bSuc;
  }

  @Override
  public Properties getCXFKeystoreProperties(String alias) throws SEDSecurityException {

    SEDCertificate aliasCrt = getSEDCertificatForAlias(alias);
    if (aliasCrt == null) {
      String msg = "Key for alias '" + alias + "' do not exists!";
      throw new SEDSecurityException(CertificateException, msg);

    }

    if (!KeystoreUtils.isCertValid(aliasCrt)) {
      String msg = "Key for alias '" + alias + " is not valid!";

      throw new SEDSecurityException(CertificateException, msg);
    }

    Properties signProperties = new Properties();
    signProperties.put(SEC_PROVIDER, SEC_PROIDER_MERLIN);
    signProperties.put(SEC_MERLIN_KEYSTORE_ALIAS, alias);
    signProperties.put(SEC_MERLIN_KEYSTORE_PASS, mhmPswd.get(KEYSTORE_NAME));
    signProperties.put(SEC_MERLIN_KEYSTORE_FILE,
            SEDSystemProperties.getCertstoreFile().getAbsolutePath());
    signProperties.put(SEC_MERLIN_KEYSTORE_TYPE, "JKS");
    return signProperties;
  }

  /**
   *
   * @param alias
   * @param cs
   * @return
   */
  @Override
  public Properties getCXFTruststoreProperties(String alias) {
    Properties signVerProperties = new Properties();
    signVerProperties.put(SEC_PROVIDER, SEC_PROIDER_MERLIN);
    signVerProperties.put(SEC_MERLIN_TRUSTSTORE_ALIAS, alias);
    signVerProperties.
            put(SEC_MERLIN_TRUSTSTORE_PASS, mhmPswd.get(KEYSTORE_NAME));
    signVerProperties.put(SEC_MERLIN_TRUSTSTORE_FILE,
            SEDSystemProperties.getCertstoreFile().getAbsolutePath());
    signVerProperties.put(SEC_MERLIN_TRUSTSTORE_TYPE, "JKS");
    return signVerProperties;
  }

}
