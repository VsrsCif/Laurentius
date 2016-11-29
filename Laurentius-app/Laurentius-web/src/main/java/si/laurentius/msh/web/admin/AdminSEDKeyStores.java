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
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import si.laurentius.cert.crl.SEDCertCRL;
import si.laurentius.cert.SEDCertStore;
import si.laurentius.cert.SEDCertificate;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.lce.KeystoreUtils;
import si.laurentius.commons.interfaces.SEDCertStoreInterface;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "adminSEDKeyStores")
public class AdminSEDKeyStores {

  private static final SEDLogger LOG = new SEDLogger(AdminSEDKeyStores.class);

  X509Certificate currentCert;
  List<SEDCertStore> mCertStoreList = null;
  SEDCertStore mImportStore = new SEDCertStore();

  @EJB(mappedName = SEDJNDI.JNDI_DBCERTSTORE)
  SEDCertStoreInterface mCertStore;

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  private SEDLookupsInterface mdbLookups;

  SEDCertStore mkeystore = null;
  SEDCertStore mRootCAStore = null;

  KeystoreUtils mku = new KeystoreUtils();

  String rootCAStore;

  SEDCertificate selectedImportCertificate;
  SEDCertificate selectedCertificate;
  SEDCertificate selectedRootCA;

  String selectedTab = "Keystore";

  String dlgMessage;
  String dlgTitle;
  boolean yesNoOption;
  String newAlias;

  public void addCurrentCertToKeystore() {
    try {
      mku.addCertificateToStore(getKeystore(), currentCert, "test", true);
      setCurrentCert(null);
    } catch (SEDSecurityException ex) {
      String errMsg = "Error loading certificate" + currentCert;
      LOG.logError(errMsg, ex);
    }
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

    currentCert = null;

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

  public List<SEDCertCRL> getCRLList() {
    long l = LOG.logStart();
    List<SEDCertCRL> lst = mdbLookups.getSEDCertCRLs();
    LOG.logEnd(l, lst);
    return lst;
  }

  public X509Certificate getCurrentCert() {
    return currentCert;
  }

  public SEDCertStore getImportStore() {
    return mImportStore;
  }

  public SEDCertStore getKeystore() {
    if (mkeystore == null) {
      try {
        mkeystore = mCertStore.getCertificateStore();
      } catch (SEDSecurityException ex) {
        Logger.getLogger(AdminSEDKeyStores.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    return mkeystore;
  }

  public SEDCertStore getRootCAStore() {
    if (mRootCAStore == null) {
      try {
        mRootCAStore = mCertStore.getRootCACertificateStore();
      } catch (SEDSecurityException ex) {
        Logger.getLogger(AdminSEDKeyStores.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    return mRootCAStore;
  }

  public List<SEDCertStore> getList() {
    return null;
  }

  public SEDCertificate getSelectedImportCertificate() {
    return selectedImportCertificate;
  }

  public SEDCertificate getSelectedCertificate() {
    return selectedCertificate;
  }

  public void handleKeystoreUpload(FileUploadEvent event) {
    long l = LOG.logStart();
    UploadedFile uf = event.getFile();
    String fileName = uf.getFileName();
    LOG.formatedWarning("Uploaded file: %s, content type : %s ", fileName, uf.getContentType());

    clearImportStore();
    try {

      File f = File.createTempFile("certstore", ".ks");
      Files.copy(uf.getInputstream(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
      mImportStore.setFilePath(f.getAbsolutePath());
      mImportStore.setName(uf.getFileName());

      RequestContext context = RequestContext.getCurrentInstance();
      context.execute("PF('certDialog').show();");
      context.update("dlgcertificate:certDialogForm");

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

  public void importSelectedCertificates() {
    boolean suc = true;
    for (SEDCertificate sc : mImportStore.getSEDCertificates()) {
      if (sc.isKeyEntry() && Utils.isEmptyString(sc.getKeyPassword())) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
            FacesMessage.SEVERITY_ERROR, "Empty password", "Enter password for key: '" +
            sc.getAlias() + "'."));
        suc = false;
      }
    }
    if (!suc) {
      return;
    }

    try {
      mku.mergeCertStores(mkeystore, mImportStore, false, false);
      mdbLookups.updateSEDCertStore(mkeystore);
      RequestContext.getCurrentInstance().execute("PF('certDialog').hide();");
    } catch (SEDSecurityException | CertificateException ex) {
      FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
          FacesMessage.SEVERITY_ERROR, "Import error", ex.getMessage()));

    }
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

  public boolean isCertInvalid(SEDCertificate crt) {

    Date currDate = Calendar.getInstance().getTime();
    return crt != null && (crt.getValidTo() == null ||
        crt.getValidFrom() == null ||
        currDate.before(crt.getValidFrom()) ||
        currDate.after(crt.getValidTo()));
  }

  public void refreshCRLList() {

    mCertStore.refreshCrlLists();

  }

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

        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
            FacesMessage.SEVERITY_ERROR, "Error", ex.getMessage()));
        LOG.logWarn(l, keystore.getFilePath(), ex);
      }
    }

  }

  public void deleteSelectedCertificate() {
    if (getSelectedCertificate() == null) {
      showAlert("Brisanje", "Za brisanje izberite certifikat", false);
    } else {
      showAlert("Izberite certifikat", "Ali želite izbrisati certifikat: " +
          getSelectedCertificate().getAlias(), true);
    }

  }

  public void renameSelectedCertificateAlias() {
    if (getSelectedCertificate() == null) {
      showAlert("Rename", "Za spremembo oznake izberite certifikat", false);
    } else {

      RequestContext context = RequestContext.getCurrentInstance();
      context.execute("PF('DlgCertRenameAlias').show();");
      context.update("forms:SettingsCertPanel:DlgCertRenameAlias");

    }
  }

  public void removeSelectedKey(SEDCertificate sc) {
    if (sc != null) {
      try {

        mku.removeCertificateFromStore(mkeystore, sc.getAlias());
        mdbLookups.updateSEDCertStore(mkeystore);
      } catch (SEDSecurityException ex) {
        LOG.logError(ex.getMessage(), ex);
      }
    } else {
      LOG.formatedWarning("NULL SC");
    }
  }

  public void renameSelectedKey(SEDCertificate sc) {
    if (sc != null) {
      LOG.formatedWarning("Selected old alias: %s ", sc.getAlias());
      LOG.formatedWarning("Selected new  alias: %s", newAlias);
      
      if (Utils.isEmptyString(getNewAlias())){
        showAlert("Certificate", "Vnesite novi", false);
      } else {

      try {

        mku.changeAlias(mkeystore, sc.getAlias(), newAlias);
        mdbLookups.updateSEDCertStore(mkeystore);
      } catch (SEDSecurityException ex) {
        LOG.logError(ex.getMessage(), ex);
      } catch (CertificateException ex) {
        Logger.getLogger(AdminSEDKeyStores.class.getName()).log(Level.SEVERE, null, ex);
      }
      RequestContext context = RequestContext.getCurrentInstance();
      context.execute("PF('DlgCertRenameAlias').hide();");
      }
       
    } else {
      LOG.formatedWarning("NULL SC");
    }
  }

  public void removeSelectedRootCA(SEDCertificate sc) {
    if (sc != null) {
      try {

        mku.removeCertificateFromStore(mRootCAStore, sc.getAlias());
        mdbLookups.updateSEDCertStore(mRootCAStore);
      } catch (SEDSecurityException ex) {
        LOG.logError(ex.getMessage(), ex);
      }
    } else {
      LOG.formatedWarning("NULL SC");
    }
  }

  public void setCurrentCert(X509Certificate cert) {
    this.currentCert = cert;
  }

  public void setImportStore(SEDCertStore mImportStore) {
    this.mImportStore = mImportStore;
  }

  public void setRootCAStore(String rootCAStore) {
    this.rootCAStore = rootCAStore;
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

  public void setSelectedCertificate(SEDCertificate sc) {
    this.selectedCertificate = sc;
  }

  public String getSelectedTab() {
    return selectedTab;
  }

  public void setSelectedTab(String selectedTab) {
    this.selectedTab = selectedTab;
  }

  public void tabChangeListener() {
    LOG.formatedWarning("tabChangeListener: %s", selectedTab);
  }

  public SEDCertificate getSelectedRootCA() {
    return selectedRootCA;
  }

  public void setSelectedRootCA(SEDCertificate selectedRootCA) {
    this.selectedRootCA = selectedRootCA;
  }

  public void showAlert(String title, String message, boolean val) {
    setDlgMessage(message);
    setDlgTitle(title);
    setYesNoOption(val);
    RequestContext context = RequestContext.getCurrentInstance();
    context.execute("PF('DlgDeleteCert').show();");
    context.update("forms:SettingsCertPanel:deleteDialog");
  }

  public String getDlgMessage() {
    return dlgMessage;
  }

  public void setDlgMessage(String dlgMessage) {
    this.dlgMessage = dlgMessage;
  }

  public String getDlgTitle() {
    return dlgTitle;
  }

  public void setDlgTitle(String dlgTitle) {
    this.dlgTitle = dlgTitle;
  }

  public boolean isYesNoOption() {
    return yesNoOption;
  }

  public void setYesNoOption(boolean yesNoOption) {
    this.yesNoOption = yesNoOption;
  }

  public String getNewAlias() {
    return newAlias;
  }

  public void setNewAlias(String newAlias) {
    this.newAlias = newAlias;
  }

}
