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
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.HashMap;

import java.util.List;
import java.util.Objects;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.NamingException;
import si.laurentius.cert.crl.SEDCertCRL;

import si.laurentius.cert.SEDCertStore;
import si.laurentius.cert.SEDCertificate;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.lce.KeystoreUtils;
import si.laurentius.lce.crl.CRLVerifier;
import si.laurentius.commons.interfaces.SEDCertStoreInterface;
import si.laurentius.commons.utils.StringFormater;

/**
 * @author Jože Rihtaršič
 */
@Startup
@Singleton
@Local(SEDCertStoreInterface.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class SEDCertStoreBean implements SEDCertStoreInterface {

  private final HashMap<String, Long> mlstModifiedTime = new HashMap<>();

  public enum CertStatus {
    OK(0),
    NEW(1),
    DELETED(2),
    MISSING_PASSWD(4),
    INVALID_DATE(8),
    INVALID_ROOT(16),
    INVALID_CRL(32);

    int iCode;

    private CertStatus(int iCd) {
      iCode = iCd;
    }
  ;

  }

  public static final String KEYSTORE_NAME = "keystore";
  public static final String KEYSTORE_INIT_FILE = "${laurentius.home}/conf/security/%s.jks";
  public static final String ROOTCA_NAME = "rootCA";

  public static final String KS_INIT_KEYSTORE_PASSWD = "passwd1234";

  /**
   *
   */
  protected static SEDLogger LOG = new SEDLogger(SEDCertStoreBean.class);
  // min, sec, milis.

  KeystoreUtils mku = new KeystoreUtils();
  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  SEDLookupsInterface mdLookups;

  @Override
  public SEDCertStore getCertificateStore()
          throws SEDSecurityException {
    return getKeystore(KEYSTORE_NAME);
  }

  @Override
  public SEDCertStore getRootCACertificateStore()
          throws SEDSecurityException {
     return getKeystore(ROOTCA_NAME);
    
  }

  @Override
  public void refreshCrlLists() {

    List<SEDCertStore> scsList = mdLookups.getSEDCertStore();
    for (SEDCertStore cs : scsList) {

      try {
        KeyStore ks = mku.getKeystore(cs);

        for (SEDCertificate c : cs.getSEDCertificates()) {
          if (c.getClrId() == null) {
            X509Certificate x509 = (X509Certificate) ks.getCertificate(c.
                    getAlias());
            SEDCertCRL crl = CRLVerifier.getCrlData(x509);
            SEDCertCRL crlExists = mdLookups.getSEDCertCRLByIssuerDNAndUrl(crl.
                    getIssuerDN(),
                    crl.getHttp(), crl.getLdap());

            if (crlExists != null) {
              crl = crlExists;
            }

            X509CRL cres = null;
            if (!Utils.isEmptyString(crl.getHttp())) {
              cres = CRLVerifier.downloadCRL(crl.getHttp());
            } else if (!Utils.isEmptyString(crl.getLdap())) {
              cres = CRLVerifier.downloadCRL(crl.getLdap());
            }
            if (cres != null) {

              File froot = SEDSystemProperties.getCRLFolder();

              File f = File.createTempFile("CRL_", ".crl", froot);
              try (FileOutputStream fos = new FileOutputStream(f)) {
                fos.write(cres.getEncoded());
              }
              crl.setNextUpdateDate(cres.getNextUpdate());
              crl.setEffectiveDate(cres.getThisUpdate());
              crl.setRetrievedDate(Calendar.getInstance().getTime());
              crl.setFilePath(f.getName());
            }
            if (crl.getId() == null) {
              mdLookups.addSEDCertCRL(crl);
            } else {
              mdLookups.updateSEDCertCRL(crl);
            }

            c.setClrId(crl.getId());

          }
        }

      } catch (SEDSecurityException | KeyStoreException | CertificateException | IOException
              | CRLException | NamingException ex) {
        LOG.logError(ex.getMessage(), ex);
      }

    }

  }

  @Override
  public void updateKeystoreCertificate(SEDCertificate crt) throws SEDSecurityException {
    mdLookups.updateSEDCertificate(crt);
    
  }

  
  private SEDCertStore getKeystore(String name)
          throws SEDSecurityException {

    SEDCertStore cs = mdLookups.getSEDCertStoreByName(name);

    File f = new File(StringFormater.replaceProperties(cs.getFilePath()));
    // init if file modified with keytool
    if (!mlstModifiedTime.containsKey(name)
            || mlstModifiedTime.get(name) < f.lastModified()) {
      LOG.formatedlog("Init certificates for keystore %s from file %s", name, f.getAbsolutePath());
              
      if (cs == null) {
        cs = mku.createNewKeyStore(KS_INIT_KEYSTORE_PASSWD, String.format(
                KEYSTORE_INIT_FILE, name));
        cs.setName(name);

      }
      mku.refreshCertStore(cs); // refresh from keystore
      
      mdLookups.updateSEDCertStore(cs); // update data to  db
      // update value

      cs = mdLookups.getSEDCertStoreByName(name);
      mlstModifiedTime.put(name, f.lastModified());
    }
    return cs;
  }

}
