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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import org.primefaces.context.RequestContext;
import si.laurentius.cert.SEDCertificate;
import si.laurentius.commons.enums.CertStatus;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.interfaces.SEDCertStoreInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.lce.KeystoreUtils;
import si.laurentius.msh.web.abst.AbstractAdminJSFView;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "adminSEDKeystoreView")
public class AdminSEDKeystoreView extends AbstractAdminJSFView<SEDCertificate> {

  private static final SEDLogger LOG = new SEDLogger(AdminSEDKeystoreView.class);

  @EJB(mappedName = SEDJNDI.JNDI_DBCERTSTORE)
  SEDCertStoreInterface mCertStore;



  KeystoreUtils mku = new KeystoreUtils();
  private String password;

  X509Certificate editableCert;


  SEDCertificate selectedImportCertificate;



  /**
   *
   */
  @Override
  public void createEditable() {
    ;
  }



  @Override
  public boolean validateData() {
    SEDCertificate ed = getEditable();

    // test alias.
    if (Utils.isEmptyString(ed.getAlias())) {
      addError("Cert alias must not be empty!");
      return false;
    }
    String oldAlias = getSelected().getAlias();
    String newAlias = ed.getAlias();
    if (!Objects.equals(newAlias,oldAlias)) {

      List<SEDCertificate> sdcLst = mCertStore.getCertificates();
     
      for (SEDCertificate c : sdcLst) {
        if (! mku.isEqualCertificateDesc(c, ed) 
                && Objects.equals(c.getAlias(), newAlias)) {
          String msg = String.format("Alias: %s already exists in keystore!",
                  ed.getAlias());
          LOG.logWarn(msg, null);

          addError(msg);
          return false;
        }
      }
      
    }
   
    return true;
  }


  /**
   *
   */
  @Override
  public boolean removeSelected() {    
    boolean bSuc = false;
    SEDCertificate sc = getSelected();
    if (sc != null) {
      try {
        LOG.formatedWarning("Remove selected row %s", sc.getAlias());
        mCertStore.removeCertificateFromStore(sc);
        bSuc = true;
      } catch (SEDSecurityException ex) {
        String strMessage = String.format(
                "Error removing cert for alias %s from keystore! Err: %s", sc.
                        getAlias(), ex.getMessage());
        LOG.logError(strMessage, ex);
        addError(strMessage);        
      }

    }
    return bSuc;
  }

  /**
   *
   */
  @Override
  public boolean persistEditable() {
    return true;
  }

  /**
   *
   */
  @Override
  public boolean updateEditable() {
    boolean bsuc = false;
    String oldAlias = getSelected().getAlias();
    String newAlias = getEditable().getAlias();
    if (!Objects.equals(newAlias,oldAlias)) {
      
        try {
          mCertStore.changeAlias(oldAlias, newAlias);
          bsuc = true;
        } catch ( SEDSecurityException ex) {
          String msg = String.format(
                  "Error changing alias for cert '%s' to '%s'. Err %s",
                  oldAlias, newAlias, ex.getMessage());
          LOG.logError(msg, ex);
          addError(msg);
          return false;
        }
    }
    return bsuc;
  }

  /**
   *
   * @return
   */
  @Override
  public List<SEDCertificate> getList() {
    return mCertStore.getCertificates();
  }

  public String getRowClass(SEDCertificate crt) {
    String strClass = null;
    if (isCertInvalid(crt)) {
      strClass = "ui-datatable-cell-red";
    } else if (crt.isKeyEntry() && (CertStatus.MISSING_PASSWD.hasCode(crt.
            getStatus())
            || CertStatus.MISSING_PASSWD.hasCode(crt.getStatus()))) {
      strClass = "ui-datatable-cell-orange";
    }

    return strClass;

  }

  public boolean isCertInvalid(SEDCertificate crt) {

    Date currDate = Calendar.getInstance().getTime();
    return crt != null && (crt.getValidTo() == null
            || crt.getValidFrom() == null
            || currDate.before(crt.getValidFrom())
            || currDate.after(crt.getValidTo()));
  }

  @Override
  public String getSelectedDesc() {
    if (getSelected() != null) {
      return getSelected().getAlias();
    }
    return null;
  }

  @Override
  public String getUpdateTargetTable() {
    return ":forms:PanelKeystore:certPanel:keylist";
  };
   

  

  public SEDCertificate getSelectedImportCertificate() {
    return selectedImportCertificate;
  }

  public void setSelectedImportCertificate(
          SEDCertificate selectedImportCertificate) {
    this.selectedImportCertificate = selectedImportCertificate;
  }

  
  public void resetPassword() {
    LOG.formatedWarning("Reset password %s", password);
    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
            FacesMessage.SEVERITY_ERROR, "Password failed", ""));

    RequestContext.getCurrentInstance().addCallbackParam("failed", true);
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

}
