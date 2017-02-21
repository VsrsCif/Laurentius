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

import java.security.cert.X509Certificate;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.primefaces.context.RequestContext;
import si.laurentius.cert.crl.SEDCertCRL;
import si.laurentius.cert.SEDCertificate;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.utils.SEDLogger;
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

  @EJB(mappedName = SEDJNDI.JNDI_DBCERTSTORE)
  SEDCertStoreInterface mCertStore;

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  private SEDLookupsInterface mdbLookups;

  KeystoreUtils mku = new KeystoreUtils();

  String rootCAStore;

  SEDCertificate selectedRootCA;

  String selectedTab = "Keystore";

  String dlgMessage;
  String dlgTitle;
  boolean yesNoOption;
  String newAlias;

  public void addCurrentCertToKeystore() {/*
    try {
      mku.addCertificateToStore(getKeystore(), currentCert, "test", true);
      setCurrentCert(null);
    } catch (SEDSecurityException ex) {
      String errMsg = "Error loading certificate" + currentCert;
      LOG.logError(errMsg, ex);
    }*/
  }

  /**
   *
   * @param lst
   * @param sc
   * @return
   */
  public List<SEDCertCRL> getCRLList() {
    long l = LOG.logStart();
    List<SEDCertCRL> lst = mCertStore.getSEDCertCRLs();
    LOG.logEnd(l, lst);
    return lst;
  }

  public void importStoreRowSelectionChanged() {
    /*
    if (this.selectedImportCertificate != null) {
      try {
        currentCert = mku.getTrustedCertForAlias(mImportStore,
                selectedImportCertificate.getAlias());
      } catch (SEDSecurityException ex) {
        Logger.getLogger(AdminSEDKeyStores.class.getName()).log(Level.SEVERE,
                null, ex);
      }
    } else {
      currentCert = null;
    }
     */
  }

  public void refreshCRLList() {

    mCertStore.refreshCrlLists();

  }

  public void removeSelectedKey(SEDCertificate sc) {
    /* if (sc != null) {
      try {

        mku.removeCertificateFromStore(mkeystore, sc.getAlias());
        mdbLookups.updateSEDCertStore(mkeystore);
      } catch (SEDSecurityException ex) {
        LOG.logError(ex.getMessage(), ex);
      }
    } else {
      LOG.formatedWarning("NULL SC");
    }*/
  }

  public void saveSelectedCertificates() {

  }

  public void removeSelectedRootCA(SEDCertificate sc) {

  }

  public void setCurrentCert(X509Certificate cert) {
//    this.currentCert = cert;
  }

  public void setRootCAStore(String rootCAStore) {
    this.rootCAStore = rootCAStore;
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
