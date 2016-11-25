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
package si.laurentius.msh.web.admin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import si.laurentius.cert.crl.SEDCertCRL;
import si.laurentius.cert.SEDCertStore;
import si.laurentius.cert.SEDCertificate;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.interfaces.SEDCertCRLInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.lce.KeystoreUtils;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "adminSEDKeyStores")
public class AdminSEDKeyStores {

  private static final SEDLogger LOG = new SEDLogger(AdminSEDKeyStores.class);

  X509Certificate currentCert;
  SEDCertStore mImportStore = new SEDCertStore();

  KeystoreUtils mku = new KeystoreUtils();

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  private SEDLookupsInterface mdbLookups;

  @EJB(mappedName = SEDJNDI.JNDI_SEDCRL)
  SEDCertCRLInterface mcrlUtils;

  List<SEDCertStore> mCertStoreList = null;

  String rootCAStore;

  SEDCertStore mkeystore = null;
  SEDCertStore mtruststore = null;

  SEDCertificate selectedKeyCertificate;
  SEDCertificate selectedTrustCertificate;
  SEDCertificate selectedImportCertificate;

  /**
   *
   * @param keystore
   */
  public void refreshCurrentKeystore(SEDCertStore keystore) {
    long l = LOG.logStart();

    if (keystore != null) {

      List<SEDCertificate> src = keystore.getSEDCertificates();

      try {
        KeyStore ks =
            mku.openKeyStore(keystore.getFilePath(), keystore.getType(), keystore
                .getPassword().toCharArray());
        List<SEDCertificate> lstals = mku.getKeyStoreSEDCertificates(ks);

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
      } catch (SEDSecurityException ex) {
        keystore.setStatus("ERROR");
        LOG.logWarn(l, keystore.getFilePath(), ex);
      }
    }

  }

  /**
   *
   * @param lst
   * @param sc
   * @return
   */
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

  public boolean isCertInvalid(SEDCertificate crt) {

    Date currDate = Calendar.getInstance().getTime();
    return crt != null && (crt.getValidTo() == null ||
        crt.getValidFrom() == null ||
        currDate.before(crt.getValidFrom()) ||
        currDate.after(crt.getValidTo()));
  }

  public boolean hasInvalidCerts(SEDCertStore keyStore) {
    if (keyStore != null) {
      for (SEDCertificate crt : keyStore.getSEDCertificates()) {
        if (isCertInvalid(crt)) {
          return true;
        }
      }
    }
    return false;

  }

  public List<SEDCertCRL> getCRLList() {
    long l = LOG.logStart();
    List<SEDCertCRL> lst = mdbLookups.getSEDCertCRLs();
    LOG.logEnd(l, lst);
    return lst;
  }

  public void refreshCRLList() {

    mcrlUtils.refreshCrlLists();

  }

  public String getRootCAStore() {
    return rootCAStore;
  }

  public void setRootCAStore(String rootCAStore) {
    this.rootCAStore = rootCAStore;
  }

  public SEDCertStore getKeystore() {
    if (mkeystore == null) {
      mkeystore = mdbLookups.getSEDCertStoreByName("keystore");
    }
    return mkeystore;
  }

  public SEDCertStore getTruststore() {
    if (mtruststore == null) {
      mtruststore = mdbLookups.getSEDCertStoreByName("truststore");      
    }
    return mtruststore;
  }

  public void handleKeystoreUpload(FileUploadEvent event) {
    long l = LOG.logStart();
    UploadedFile uf = event.getFile();
    String fileName = uf.getFileName();

    clearImportStore();
    try {

      File f = File.createTempFile("certstore", ".ks");
      Files.copy(uf.getInputstream(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
      mImportStore.setFilePath(f.getAbsolutePath());
      mImportStore.setName(fileName);
      RequestContext context = RequestContext.getCurrentInstance();
      context.update(":dlgcertificate:certDialogForm:certData");
      context.execute("PF('certDialog').show();");

    } catch (IOException ex) {
      String errMsg = "Error loading certificate" + fileName;
      LOG.logError(errMsg, ex);

    }

  }

  public void handleNewCertKeyUpload(FileUploadEvent event) {
    long l = LOG.logStart();
    UploadedFile uf = event.getFile();

    String fileName = uf.getFileName();

    try {
      X509Certificate c = mku.getCertFromInputStream(uf.getInputstream());
      LOG.formatedWarning("got certificate %s", c);
      setCurrentCert(c);
      RequestContext context = RequestContext.getCurrentInstance();
      context.update(":dlgcertificate:certDialogForm:certData");
      context.execute("PF('certDialog').show();");

    } catch (IOException | SEDSecurityException ex) {
      String errMsg = "Error loading certificate" + fileName;
      LOG.logError(errMsg, ex);

    }

  }

  public void addCurrentCertToKeystore() {
    try {
      mku.addCertificateToStore(getKeystore(), currentCert, "test", true);
      setCurrentCert(null);
    } catch (SEDSecurityException ex) {
      String errMsg = "Error loading certificate" + currentCert;
      LOG.logError(errMsg, ex);
    }
  }

  public List<SEDCertStore> getList() {
    return null;
  }

  public X509Certificate getCurrentCert() {
    return currentCert;
  }

  public void setCurrentCert(X509Certificate cert) {
    this.currentCert = cert;
  }

  public SEDCertificate getSelectedKeyCertificate() {
    return selectedKeyCertificate;
  }

  public void setSelectedKeyCertificate(SEDCertificate selecedKeyCertificate) {
    this.selectedKeyCertificate = selecedKeyCertificate;
  }

  public SEDCertificate getSelectedTrustCertificate() {
    return selectedTrustCertificate;
  }

  public void setSelectedTrustCertificate(SEDCertificate selecedTrustCertificate) {
    this.selectedTrustCertificate = selecedTrustCertificate;
  }

  public SEDCertStore getImportStore() {
    return mImportStore;
  }

  public void setImportStore(SEDCertStore mImportStore) {
    this.mImportStore = mImportStore;
  }

  public void clearImportStore() {
    if (!Utils.isEmptyString(mImportStore.getFilePath())) {
      File f = new File(mImportStore.getFilePath());
      if (f.exists()) {
        f.delete();
      }
    }
    mImportStore.getSEDCertificates().clear();
    mImportStore.setFilePath(null);
    mImportStore.setId(null);
    mImportStore.setName(null);
    mImportStore.setPassword(null);
    mImportStore.setStatus(null);
    mImportStore.setType("JKS");

  }

  public SEDCertificate getSelectedImportCertificate() {
    return selectedImportCertificate;
  }

  public void setSelectedImportCertificate(SEDCertificate selecedImportCertificate) {
    this.selectedImportCertificate = selecedImportCertificate;
    if (this.selectedImportCertificate != null) {
      try {
        currentCert = mku.getTrustedCertForAlias(mImportStore, selecedImportCertificate.getAlias());
      } catch (SEDSecurityException ex) {
        Logger.getLogger(AdminSEDKeyStores.class.getName()).log(Level.SEVERE, null, ex);
      }
    } else {
      currentCert = null;
    }
    RequestContext context = RequestContext.getCurrentInstance();
    context.update(":dlgcertificate:certDialogForm:certData");
  }

  public void importStoreRowSelectionChanged() {

    if (this.selectedImportCertificate != null) {
      try {
        currentCert = mku.getTrustedCertForAlias(mImportStore, selectedImportCertificate.getAlias());
      } catch (SEDSecurityException ex) {
        Logger.getLogger(AdminSEDKeyStores.class.getName()).log(Level.SEVERE, null, ex);
      }
    } else {
      currentCert = null;
    }

  }

  public void importSelectedCertificates() {
    try {
      mku.mergeCertStores(mkeystore, mImportStore);
      mdbLookups.updateSEDCertStore(mkeystore);
    } catch (SEDSecurityException | CertificateException ex) {
      LOG.logError(ex.getMessage(), ex);
    }
  }
  
  public void removeSelectedKey(SEDCertificate sc) {
    if (sc!= null) {
    try {
      
      mku.removeCertificateFromStore(mkeystore, sc.getAlias());
      mdbLookups.updateSEDCertStore(mkeystore);
    } catch (SEDSecurityException ex) {
      LOG.logError(ex.getMessage(), ex);
    }
    }else {
      LOG.formatedWarning("NULL SC");
    }
  }

}
