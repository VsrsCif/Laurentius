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

import si.laurentius.ejb.cache.SimpleListCache;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.CRLException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Lock;
import static javax.ejb.LockType.READ;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.NamingException;
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
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.enums.CertStatus;
import si.laurentius.commons.exception.SEDSecurityException;
import static si.laurentius.commons.exception.SEDSecurityException.SEDSecurityExceptionCode.CertificateException;
import static si.laurentius.commons.exception.SEDSecurityException.SEDSecurityExceptionCode.ReadWriteFileException;
import si.laurentius.commons.interfaces.SEDCertStoreInterface;
import si.laurentius.commons.interfaces.SEDNetworkUtilsInterface;
import si.laurentius.commons.utils.FileUtils;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.lce.DigestUtils;
import si.laurentius.lce.KeystoreUtils;
import si.laurentius.lce.crl.CRLVerifier;

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

  @EJB(mappedName = SEDJNDI.JNDI_NETWORK)
  private SEDNetworkUtilsInterface mdNetUtils;

  @PersistenceContext(unitName = "ebMS_LAU_PU", name = "ebMS_LAU_PU")
  public EntityManager memEManager;
  private final KeystoreUtils mku = new KeystoreUtils();
  Map<String, SEDCertCRL> mlstCertCRL = new HashMap<>();
  SimpleListCache mscCacheList = new SimpleListCache();

  private String mstrCrlUpdateMessage = null;
  private KeyStore mRootCAStore = null;
  private KeyStore mKeyStore = null;
  String mKeyStoreOpenFilePath = null;
  

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
    long l = LOG.logStart();
    KeyStore ks = getCertStore();
    mku.addCertificateToStore(ks, crt, alias, false);
    saveKeystore(ks, KEYSTORE_NAME);
    LOG.logEnd(l, alias);
  }

  /**
   *
   * @param crt
   * @param alias
   * @throws SEDSecurityException
   */
  @Override
  public void addCertToRootCA(X509Certificate crt, String alias) throws SEDSecurityException {
    long l = LOG.logStart();
    KeyStore ks = getRootCAStore();
    mku.addCertificateToStore(ks, crt, alias, false);
    saveKeystore(ks, ROOTCA_NAME);
    validateCertificates();

    LOG.logEnd(l, alias);
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
    long l = LOG.logStart();
    KeyStore ks = getCertStore();
    mku.addKeyToStore(ks, alias, privateKey, certs, passwd != null ? passwd.
            toCharArray() : null, false);
    addPassword(alias, passwd);
    saveKeystore(ks, KEYSTORE_NAME);
    LOG.logEnd(l, alias);
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

    TypedQuery<SEDCertPassword> tg
            = memEManager.createNamedQuery(
                    SEDCertPassword.class.getName() + ".getByAlias",
                    SEDCertPassword.class);
    tg.setParameter("alias", alias);
    SEDCertPassword dbCP = null;
    try {
      dbCP = tg.getSingleResult();
    } catch (NoResultException ignore) {
      LOG.formatedlog("No key found for %s, key data will be added to db!", alias);
    }

    if (dbCP == null) {
      setPasswordToDB(cp, false);
    } else if (!Objects.equals(pswd, dbCP.getPassword())) {
      dbCP.setPassword(pswd);
      setPasswordToDB(dbCP, true);

    }

    LOG.logEnd(l, alias);

  }

  private void setPasswordToDB(SEDCertPassword cp, boolean update) throws SEDSecurityException {
    long l = LOG.logStart(cp.getAlias());

    try {
      mutUTransaction.begin();
      if (update) {
        memEManager.merge(cp);
      } else {
        memEManager.persist(cp);
      }
      mutUTransaction.commit();
      refreshPasswords();
    } catch (NotSupportedException | SystemException | RollbackException
            | HeuristicMixedException
            | HeuristicRollbackException
            | SecurityException
            | IllegalStateException ex) {
      String msg = String.format(
              "Error occured while saving passwd for alias %s. Err %s", cp.
                      getAlias(),
              ex.getMessage());
      LOG.logError(l, msg, ex);

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
    long l = LOG.logStart(oldAlias, newAlias);
    List<SEDCertificate> lstCert = getCertificates();
    boolean suc = false;
    for (SEDCertificate c : lstCert) {
      if (Objects.equals(c.getAlias(), oldAlias)) {
        changeAliasForCertificate(c, newAlias);
        suc = true;
        break;
      }
    }
    if (!suc) {
      throw new SEDSecurityException(
              SEDSecurityException.SEDSecurityExceptionCode.CertificateException,
              String.format("Certificate for alias %s not exist!", oldAlias)
      );
    }
    LOG.logEnd(l, oldAlias, newAlias);
  }

  /**
   *
   * @param cert
   * @param oldAlias
   * @param newAlias
   * @throws SEDSecurityException
   */
  public void changeAliasForCertificate(SEDCertificate cert,
          final String newAlias) throws SEDSecurityException {
    long l = LOG.logStart(cert.getAlias(), newAlias);
    KeyStore ks = getCertStore();

    if (cert.isKeyEntry()) {
      String oldAlias = cert.getAlias();
      try {
        SEDCertPassword pswd = getKeyPassword(oldAlias);
        if (pswd == null) {
          String msg = String.format(
                  "Could not change alias from %s to %s. Cause: no password for key %s!",
                  oldAlias, newAlias, oldAlias);
          LOG.logError(l, msg, null);
          throw new SEDSecurityException(
                  SEDSecurityException.SEDSecurityExceptionCode.KeyPasswordError,
                  msg);
        }

        String paswd = pswd.getPassword();

        mutUTransaction.begin();

        memEManager.remove(memEManager.contains(pswd) ? pswd
                : memEManager.merge(pswd));

        SEDCertPassword cp = new SEDCertPassword();
        cp.setAlias(newAlias);
        cp.setPassword(paswd);
        memEManager.persist(cp);
        // change alias in keystore
        mku.changeAlias(ks, cert.getAlias(), newAlias, paswd);
        saveKeystore(ks, KEYSTORE_NAME);
        mutUTransaction.commit();

        // update cached values or update cache!
        mscCacheList.clearCachedList(SEDCertPassword.class);
        mscCacheList.clearCachedList(KEYSTORE_NAME);

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
    LOG.logEnd(l, cert.getAlias(), newAlias);
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
   * @param oldAlias
   * @param newAlias
   * @throws SEDSecurityException
   */
  @Override
  public void changeRootCAAlias(String oldAlias, String newAlias) throws SEDSecurityException {
    long l = LOG.logStart();
    KeyStore ks = getRootCAStore();
    mku.changeAlias(ks, oldAlias, newAlias, null);
    saveKeystore(ks, ROOTCA_NAME);
    LOG.logEnd(l, oldAlias, newAlias);
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
   * Method opens cert store. If cert store not exists it creates new one.
   *
   * @return KeyStore
   * @throws SEDSecurityException
   */
  @Override
  @Lock(READ)
  public KeyStore getCertStore() throws SEDSecurityException {
    File fStore = SEDSystemProperties.getCertstoreFile();
    //if (mKeyStore == null || !fStore.exists() ||  !Objects.equals(mKeyStoreOpenFilePath, fStore.getAbsoluteFile())) {
      
      SEDCertPassword cp = getKeyPassword(KEYSTORE_NAME);
      mKeyStore = openKeystore(SEDSystemProperties.getCertstoreType(), fStore,
              KEYSTORE_NAME, cp != null && !Utils.
                      isEmptyString(
                              cp.getPassword()) ? cp.getPassword().toCharArray() : null);
      
     // mKeyStoreOpenFilePath= fStore.getAbsolutePath();
      
    //}
    return mKeyStore;
  }

  /**
   *
   * @return
   */
  @Override
  @Lock(READ)
  public List<SEDCertificate> getCertificates() throws SEDSecurityException {
    long l = LOG.logStart();
    List<SEDCertificate> lstRes = null;
    File fStore = SEDSystemProperties.getCertstoreFile();
    long lastModified = fStore.lastModified();

    if (mscCacheList.cacheListTimeout(KEYSTORE_NAME, lastModified)) {
      refreshCertificates();
    }
    lstRes = mscCacheList.getFromCachedList(KEYSTORE_NAME);
    LOG.logEnd(l);
    return lstRes;

  }

  @Lock(READ)
  public SEDCertCRL getCrlForCert(X509Certificate x509, boolean forceRefresh) {
    long l = LOG.logStart();

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
      if ((crlExists == null
              || Utils.isEmptyString(crlExists.getFilePath()))
              && updateCrlCache(crl)) {

        mlstCertCRL.put(crl.getIssuerDigestValue(), crl);
        crlExists = crl;

      }
    } catch (CertificateParsingException | IOException ex) {
      LOG.logError("Error updating CRL cache", ex);
    }
    LOG.logEnd(l);
    return crlExists;

  }
  @Lock(READ)
  public String getCurrentUpdateCRLErrorlMessage() {
    return mstrCrlUpdateMessage;
  }

  @Override
  @Lock(READ)
  public SEDCertPassword getKeyPassword(String alias) {
    long l = LOG.logStart(alias);
    assert !Utils.isEmptyString(alias) : "Alias must not be null!";
    SEDCertPassword cpRes = null;
    if (mscCacheList.cacheListTimeout(SEDCertPassword.class)) {
      refreshPasswords();
    }
    List<SEDCertPassword> lst = mscCacheList.getFromCachedList(
            SEDCertPassword.class);

    for (SEDCertPassword cp : lst) {
      if (Objects.equals(cp.getAlias(), alias)) {
        cpRes = cp;
        break;
      }
    }
    LOG.logEnd(l, alias);
    return cpRes;

  }

  /**
   *
   * @param onlyKeys
   * @return
   */
  @Override
  @Lock(READ)
  public List<String> getKeystoreAliases(boolean onlyKeys) {

    long l = LOG.logStart();
    List<String> lst = Collections.emptyList();
    try {
      List<SEDCertificate> lc = getCertificates();
      lst = new ArrayList<>();
      for (SEDCertificate c : lc) {
        if (onlyKeys) {
          if (c.isKeyEntry()) {
            lst.add(c.getAlias());
          }
        } else {
          lst.add(c.getAlias());
        }

      }
    } catch (SEDSecurityException ex) {
      LOG.logError("Error occured while reading keystore.Error: " + ex.
              getMessage(), ex);
    }
    LOG.logEnd(l);
    return lst;
  }

  /**
   *
   * @return @throws si.laurentius.commons.exception.SEDSecurityException
   */
  @Override
  @Lock(READ)
  public List<X509Certificate> getRootCA509Certs() throws SEDSecurityException {
    long l = LOG.logStart();
    List<X509Certificate> lst = mku.
            getKeyStoreX509Certificates(getRootCAStore());
    LOG.logEnd(l);
    return lst;

  }

  @Override  
  @Lock(READ)
  public List<SEDCertificate> getRootCACertificates() {
    long l = LOG.logStart();
    List<SEDCertificate> lstRes = null;
    File fStore = SEDSystemProperties.getRootCAStoreFile();
    if (mscCacheList.cacheListTimeout(ROOTCA_NAME, fStore.lastModified())) {
      refreshRootCACertificates();
    }
    lstRes = mscCacheList.getFromCachedList(ROOTCA_NAME);
    LOG.logEnd(l);
    return lstRes;

  }

  /**
   * Method opens Root CA store. If store not exists it creates new one.
   *
   * @return KeyStore
   * @throws SEDSecurityException
   */
  @Lock(READ)
  protected KeyStore getRootCAStore() throws SEDSecurityException {
    if (mRootCAStore == null) {
      File fStore = SEDSystemProperties.getRootCAStoreFile();
      SEDCertPassword cp = getKeyPassword(ROOTCA_NAME);
      mRootCAStore = openKeystore(SEDSystemProperties.getRootCAStoreType(),
              fStore, ROOTCA_NAME, cp != null && !Utils.isEmptyString(
                      cp.getPassword()) ? cp.getPassword().toCharArray() : null);
    }
    return mRootCAStore;

  }

  /**
   *
   * @return
   */
  @Override
  @Lock(READ)
  public List<SEDCertCRL> getSEDCertCRLs() {
    return new ArrayList(mlstCertCRL.values());
  }

  /**
   *
   * @param alias
   * @return
   */
  @Override
  @Lock(READ)
  public SEDCertificate getSEDCertificatForAlias(String alias) {
    long l = LOG.logStart();
    SEDCertificate crt = null;
    try {
      List<SEDCertificate> lst = getCertificates();
      for (SEDCertificate sc : lst) {
        if (Objects.equals(sc.getAlias(), alias)) {
          crt = sc;
          break;
        }
      }
    } catch (SEDSecurityException ex) {
      LOG.logError(
              "Error occured while retrieving cert list from keystore. Error: " + ex.
                      getMessage(), ex);
    }
    LOG.logEnd(l);
    return crt;
  }

  /**
   *
   * @param alias
   * @return
   */
  @Override
  @Lock(READ)
  public X509Certificate getX509CertForAlias(String alias) throws SEDSecurityException {
    long l = LOG.logStart();
    KeyStore ks = getCertStore();
    X509Certificate xc = mku.getTrustedCertForAlias(ks, alias);
    LOG.logEnd(l);
    return xc;
  }

  public void validateCertificate(X509Certificate x509Cert, SEDCertificate sc) {
    sc.setStatus(0);
    List<String> lstMsg = new ArrayList<>();
    // check if cert is "date" valid
    try {
      x509Cert.checkValidity();
    } catch (CertificateNotYetValidException ex) {
      lstMsg.add("Certificate is not yet valid");
      LOG.formatedWarning("Certificate %s is not yet valid!", sc.getAlias());
      sc.setStatus(CertStatus.INVALID_BY_DATE.addCode(sc.getStatus()));
    } catch (CertificateExpiredException ex) {
      lstMsg.add("Certificat is expired");
      LOG.formatedWarning("Certificate %s is expired!", sc.getAlias());
      sc.setStatus(CertStatus.INVALID_BY_DATE.addCode(sc.getStatus()));
    }
    try {
      // check if cert is signed
      if (isRootCAInvalid(x509Cert)) {
        lstMsg.add("Certificat is not signed by trusted root CA!");
        LOG.formatedWarning("Certificate %s  not signed by trusted root CA!",
                sc.getAlias());
        sc.setStatus(CertStatus.INVALID_BY_ROOTCA.addCode(sc.getStatus()));
      }
    } catch (SEDSecurityException ex) {
      lstMsg.add("Could not open trusted root CA store!");
      sc.setStatus(CertStatus.INVALID_BY_ROOTCA.addCode(sc.getStatus()));
      LOG.logError("Error opening ROOT CA store!", ex);
    }

    // check root ca
    // validate by CRL List
    Boolean bR = isCertificateRevoked(x509Cert);
    if (bR == null) {
      sc.setStatus(CertStatus.CRL_NOT_CHECKED.addCode(sc.getStatus()));
      lstMsg.add("Could not check CRL!");
      LOG.formatedWarning("Could not check CRL! for certificate %s!", sc.
              getAlias());
    } else if (bR) {
      sc.setStatus(CertStatus.CRL_REVOKED.addCode(sc.getStatus()));
      lstMsg.add("Certificate is revoked");
      LOG.formatedWarning("Certificate %s is revoked", sc.getAlias());
    }
    sc.setStatusMessage(String.join(",", lstMsg));
  }

  @Lock(READ)
  public boolean isRootCAInvalid(X509Certificate crt) throws SEDSecurityException {
    boolean suc = true;

    List<X509Certificate> rootCALst = getRootCA509Certs();
    for (X509Certificate rca : rootCALst) {

      try {
        crt.verify(rca.getPublicKey());
        suc = false;
        break;
      } catch (CertificateException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException | SignatureException ex) {
        LOG.formatedDebug("Cert: %s is not signed by CA cert %s. Error; %s",
                crt.getSubjectX500Principal().getName(), rca.
                getIssuerX500Principal(), ex.
                        getMessage());
      }
    }
    return suc;
  }

  @Lock(READ)
  public Boolean isCertificateRevoked(X509Certificate x509Cert) {
    long l = LOG.logStart();

    assert x509Cert != null : "Null certificat parameter";
    Boolean bRes = null;
    SEDCertCRL crlData = getCrlForCert(x509Cert, false);
    X509CRL crl = null;
    if (crlData == null) {
      LOG.formatedWarning("Certificate %s does not have CLR extension!",
              x509Cert.getSubjectX500Principal().getName());
      return false;
    }
    crl = CRLVerifier.getCRLFromFile(crlData.getFilePath());

    if (crl != null) {
      bRes = crl.isRevoked(x509Cert);
    } else {
      LOG.formatedWarning(
              "Could not validate CRL for certificate %s, serial %d.", x509Cert.
                      getSubjectX500Principal().getName(), x509Cert.
                      getSerialNumber());
    }
    LOG.logEnd(l);
    return bRes;

  }

  private KeyStore openKeystore(String storetype, File fStore, String alias,
          char[] psswd) throws SEDSecurityException {
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
        ks = mku.getKeystore(fStore, storetype, psswd);
      }
    }

    return ks;
  }

  private void refreshCertificates() throws SEDSecurityException {
    long l = LOG.logStart();
    KeyStore ks = getCertStore();

    List<SEDCertificate> lst = new ArrayList<>();
    try {
      Enumeration<String> e = ks.aliases();
      while (e.hasMoreElements()) {
        SEDCertificate ec = new SEDCertificate();
        String alias = e.nextElement();
        Certificate c = ks.getCertificate(alias);
        if (c instanceof X509Certificate) {
          ec.setKeyEntry(ks.isKeyEntry(alias));
          ec.setAlias(alias);
          ec.setType(c.getType());
          ec.setHexSHA1Digest(DigestUtils.getHexSha1Digest(c.getEncoded()));

          X509Certificate xc = (X509Certificate) c;
          ec.setValidFrom(xc.getNotBefore());
          ec.setValidTo(xc.getNotAfter());
          ec.setIssuerDN(xc.getIssuerDN().getName());
          ec.setSubjectDN(xc.getSubjectDN().getName());
          ec.setSerialNumber(xc.getSerialNumber() + "");
          validateCertificate(xc, ec);
          lst.add(ec);
        } else {
          LOG.formatedWarning(
                  "Invalid certificate type for alias %s in keystore!", alias);
        }

      }
    } catch (KeyStoreException ex) {
      throw new SEDSecurityException(ReadWriteFileException, ex,
              "Read keystore aliased: !");
    } catch (CertificateEncodingException ex) {
      throw new SEDSecurityException(ReadWriteFileException, ex,
              "Error calculating sha digest for certificate!");
    }
    // set new keystore list
    mscCacheList.cacheList(lst, KEYSTORE_NAME);
    LOG.logEnd(l);
  }
  
  @Lock(READ)
  private void validateCertificates() throws SEDSecurityException {
    long l = LOG.logStart();
    List<SEDCertificate> lst = getCertificates();
    for (SEDCertificate c : lst) {
      X509Certificate xc = getX509CertForAlias(c.getAlias());
      validateCertificate(xc, c);
    }

    LOG.logEnd(l);
  }

  /**
   *
   */
  @Override
  public void refreshCrlLists() {
    long l = LOG.logStart();
    mlstCertCRL.clear();

    try {
      KeyStore ks = getCertStore();
      Enumeration<String> alsEnum = ks.aliases();
      while (alsEnum.hasMoreElements()) {
        String alias = alsEnum.nextElement();
        X509Certificate x509 = (X509Certificate) ks.getCertificate(alias);
        getCrlForCert(x509, false);
      }

    } catch (SEDSecurityException | KeyStoreException ex) {
      LOG.logError(ex.getMessage(), ex);
    }
    LOG.logEnd(l);
  }

  public void refreshPasswords() {
    long l = LOG.logStart();

    TypedQuery<SEDCertPassword> cpq = memEManager.createNamedQuery(
            SEDCertPassword.class
                    .getName() + ".getAll", SEDCertPassword.class
    );
    List<SEDCertPassword> lst = cpq.getResultList();
    mscCacheList
            .cacheList(lst, SEDCertPassword.class
            );
    LOG.logEnd(l);
  }

  private void refreshRootCACertificates() {
    long l = LOG.logStart();
    try {
      KeyStore ks = getRootCAStore();
      mscCacheList.cacheList(mku.getKeyStoreSEDCertificates(ks), ROOTCA_NAME);
    } catch (SEDSecurityException ex) {
      LOG.logError("Error opening keystore", ex);
    }
    LOG.logEnd(l);
  }

  /**
   *
   * @param crt
   * @throws SEDSecurityException
   */
  @Override
  public void removeCertificateFromRootCAStore(SEDCertificate crt) throws SEDSecurityException {
    long l = LOG.logStart();
    KeyStore ks = getRootCAStore();
    try {
      ks.deleteEntry(crt.getAlias());
    } catch (KeyStoreException ex) {
      throw new SEDSecurityException(CertificateException, ex, ex.getMessage());
    }

    saveKeystore(ks, ROOTCA_NAME);
    validateCertificates();
    LOG.logEnd(l);
  }

  /**
   *
   * @param crt
   * @throws SEDSecurityException
   */
  @Override
  public void removeCertificateFromStore(SEDCertificate crt) throws SEDSecurityException {
    long l = LOG.logStart();

    KeyStore ks = getCertStore();
    try {

      ks.deleteEntry(crt.getAlias());
    } catch (KeyStoreException ex) {
      throw new SEDSecurityException(CertificateException, ex, ex.getMessage());
    }

    SEDCertPassword cp = getKeyPassword(crt.getAlias());
    if (cp != null) {
      try {
        mutUTransaction.begin();
        memEManager.remove(memEManager.contains(cp) ? cp
                : memEManager.merge(cp));
        saveKeystore(ks, KEYSTORE_NAME);
        mutUTransaction.commit();
      } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
        LOG.logError("Error deleting password from  database!",
                ex);
        try {
          mutUTransaction.rollback();
        } catch (IllegalStateException | SecurityException | SystemException ex1) {
          LOG.logWarn("Rollback error!", ex);
        }
      }
    } else {
      saveKeystore(ks, KEYSTORE_NAME);
    }
    LOG.logEnd(l);
  }

  private void saveKeystore(KeyStore ks, String alias) throws SEDSecurityException {
    File fStore = null;
    SEDCertPassword cp = getKeyPassword(alias);
    if (cp == null) {
      throw new SEDSecurityException(CertificateException,
              "Missing password for keystore with alias:" + alias);
    }

    String psswd = cp.getPassword();

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
    switch (alias) {
      case KEYSTORE_NAME:
        refreshCertificates();
        break;
      case ROOTCA_NAME:
        refreshRootCACertificates();
        break;
      default:
        LOG.formatedWarning("Unknown keystore alias", alias);
    }

  }

  public boolean updateCrlCache(SEDCertCRL crl) {
    X509CRL cres = null;
    boolean bSuc = false;

    if (mdNetUtils != null && !mdNetUtils.isConnectedToNetwork()) {
      String msg = "Could not retrieve CRL list. Network is not connected";
      LOG.logError(msg, null);
      return false;
    }

    if (mdNetUtils != null && !mdNetUtils.isConnectedToInternet()) {
      String msg = "Could not retrieve CRL list. Internet is not reachable!";
      LOG.logError(msg, null);
      return false;
    }

    for (SEDCertCRL.Url u : crl.getUrls()) {
      try {

        /* some CA does not allow ping!??
        URL ur = new URL(u.getValue());
        if (!isReachable(ur.getHost())) {
          LOG.formatedWarning("CRL list %s  (host: %s) is not reachable!", u.getValue(), ur.getHost());
          continue;
        }*/
        LOG.formatedWarning("Download CRL list %s.", u.getValue());
        cres = CRLVerifier.downloadCRL(u.getValue());

        /*
        List<Proxy> prList = ProxySelector.getDefault().select(new URI(u.
                getValue()));
          cres = CRLVerifier.downloadCRL(u.getValue(), prList.size() > 0 ? prList.
                get(0) : null);*/
      } catch (IOException | CertificateException | CRLException | NamingException ex) {
        String msg = String.format(
                "Error retrieving CRL Cache for %s url %s: error %s",
                crl.getIssuerDN(), u.getValue(), ex.getMessage());
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

  /**
   *
   * @param alias
   * @return
   * @throws si.laurentius.commons.exception.SEDSecurityException
   */
  @Override
  public PrivateKey getPrivateKeyForAlias(String alias) throws SEDSecurityException {
    long l = LOG.logStart();
    SEDCertPassword cp = getKeyPassword(alias);
    if (cp == null) {
      throw new SEDSecurityException(CertificateException,
              "Missing password for key with alias:" + alias);
    }
    PrivateKey pk = (PrivateKey) mku.
            getPrivateKeyForAlias(getCertStore(), alias,
                    cp.getPassword());
    LOG.logEnd(l);
    return pk;

  }

  /**
   *
   * @param xrc
   * @return
   */
  @Override
  public PrivateKey getPrivateKeyForX509Cert(X509Certificate xrc) throws SEDSecurityException {
    long l = LOG.logStart();
    KeyStore ks = getCertStore();
    String alias = mku.getPrivateKeyAliasForX509Cert(ks, xrc);
    PrivateKey pk = getPrivateKeyForAlias(alias);
    LOG.logEnd(l);
    return pk;
  }

  public boolean isReachable(String host) {
    int timeout = 5000;
    boolean bsuc = false;
    InetAddress[] addresses;
    try {
      addresses = InetAddress.getAllByName(host);
    } catch (UnknownHostException ex) {
      LOG.formatedWarning(
              "Testing reachangle host: %s throws UnknownHostException: %s",
              host, ex.getMessage());
      return false;
    }
    for (InetAddress address : addresses) {
      try {
        if (address.isReachable(timeout)) {
          bsuc = true;
        }
      } catch (IOException ex) {
        LOG.
                formatedWarning(
                        "Testing reachangle host: %s throws IOException: %s",
                        host, ex.getMessage());
      }
    }
    return bsuc;
  }

}
