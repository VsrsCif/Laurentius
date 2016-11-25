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
import static java.io.File.separator;
import java.io.FileOutputStream;
import java.io.IOException;
import static java.lang.System.getProperty;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Calendar;

import java.util.List;
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
import static si.laurentius.commons.SEDSystemProperties.SYS_PROP_HOME_DIR;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.interfaces.SEDCertCRLInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.lce.KeystoreUtils;
import si.laurentius.lce.crl.CRLVerifier;

/**
 * @author Jože Rihtaršič
 */
@Startup
@Singleton
@Local(SEDCertCRLInterface.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class SEDCertCRLBean implements SEDCertCRLInterface {

  /**
   *
   */
  protected static SEDLogger LOG = new SEDLogger(SEDCertCRLBean.class);
  // min, sec, milis.

  KeystoreUtils mku = new KeystoreUtils();
  /**
   *
   */
  public static final long S_UPDATE_TIMEOUT = 10 * 60 * 1000; // 10 minutes
  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  SEDLookupsInterface mdLookups;

  @Override
  public void refreshCrlLists() {

    List<SEDCertStore> scsList = mdLookups.getSEDCertStore();
    for (SEDCertStore cs : scsList) {

      try {
        KeyStore ks = mku.getKeystore(cs);

        for (SEDCertificate c : cs.getSEDCertificates()) {
          X509Certificate x509 = (X509Certificate) ks.getCertificate(c.getAlias());
          SEDCertCRL crl = CRLVerifier.getCrlData(x509);
          SEDCertCRL crlExists = mdLookups.getSEDCertCRLByIssuerDNAndUrl(crl.getIssuerDN(),
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

            File froot = getCRLFolder();

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
        }
      } catch (SEDSecurityException | KeyStoreException | CertificateException | IOException
          | CRLException | NamingException ex) {
        LOG.logError(ex.getMessage(), ex);
      }

    }

  }

  public File getCRLFolder() {

    String folder = getProperty(SYS_PROP_HOME_DIR) + separator +
        SEDSystemProperties.SYS_PROP_FOLDER_SECURITY_DEF + separator +
        SEDSystemProperties.SYS_PROP_FOLDER_CRL_DEF;

    File f = new File(folder);
    if (!f.exists()) {
      f.mkdirs();
    }
    return f;
  }

}
