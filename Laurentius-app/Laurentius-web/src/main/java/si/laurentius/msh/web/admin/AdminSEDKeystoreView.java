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
import java.io.InputStream;
import java.nio.file.Files;
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.security.cert.X509Certificate;
import java.util.Objects;
import javax.servlet.http.Part;
import si.laurentius.cert.SEDCertStore;
import si.laurentius.cert.SEDCertificate;
import si.laurentius.commons.CertStatus;
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

  X509Certificate editableCert;
   private Part file; // +getter+setter

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

    SEDCertStore sc;
    try {
      sc = mCertStore.getCertificateStore();
    } catch (SEDSecurityException ex) {
      String msg = String.format(
              "Error accesing keystore for alias: %s. Err: %s", ed.
                      getAlias(), ex.getMessage());
      LOG.logWarn(msg, ex);
      addError(msg);
      return false;
    }

    for (SEDCertificate c : sc.getSEDCertificates()) {
      if (!Objects.equals(c.getId(), ed.getId()) && Objects.equals(c.getAlias(),
              ed.getAlias())) {
        LOG.formatedWarning("C alias id %d, ed id: %d", c.getId(), ed.getId());
        String msg = String.format("Alias: %s already exists in keystore!", ed.
                getAlias());
        LOG.logWarn(msg, null);

        addError(msg);
        return false;
      }
    }
    String oldAlias = getSelected().getAlias();
    // tese key
    if (ed.isKeyEntry()) {
      if (Utils.isEmptyString(ed.getKeyPassword())) {
        addError("Password for key must not be empty!");
        ed.setStatus(CertStatus.MISSING_PASSWD.addCode(ed.getStatus()));
        return false;
      } else {
        try {
          boolean bsuc = mku.testAccessToKey(sc,
                  oldAlias, ed.getKeyPassword());
          if (!bsuc) {
            String msg = String.format(
                    "Error accesing key for old alias: %s: Key not exists!",
                    oldAlias);
            LOG.logWarn(msg, null);
            addError(msg);
            ed.setStatus(CertStatus.INVALID_PASSWD.addCode(ed.getStatus()));
            return false;
          } else {

            ed.setStatus(CertStatus.MISSING_PASSWD.removeCode(ed.getStatus()));
            ed.setStatus(CertStatus.INVALID_PASSWD.removeCode(ed.getStatus()));
          }
        } catch (SEDSecurityException ex) {
          ed.setStatus(CertStatus.INVALID_PASSWD.addCode(ed.getStatus()));
          String msg = String.format("Error accesing key: %s. Err: %s", ed.
                  getAlias(), ex.getMessage());
          LOG.logWarn(msg, ex);
          addError(msg);
          return false;
        }
      }
    }

    return true;
  }

  @Override
  public void startEditSelected() {
    // deep copy
    super.startEditSelected();
    LOG.
            formatedWarning("Selected id %d, editable idd %d", getSelected().
                    getId(), getEditable().getId());
    getEditable().setId(getSelected().getId());
    editableCert = null;

    SEDCertificate ed = getEditable();
    if (ed != null) {
      String oldAlias = getSelected().getAlias();
      try {
        editableCert = mku.getTrustedCertForAlias(mCertStore.
                getCertificateStore(),
                oldAlias);
      } catch (SEDSecurityException ex) {
        String strMessage = String.format(
                "Error retrieving cert for alias %s from keystore! Err: %s", ed.
                        getAlias(), ex.getMessage());
        LOG.logError(strMessage, ex);
        addError(strMessage);

      }
    }
  }

  /**
   *
   */
  @Override
  public void removeSelected() {
    if (getSelected() != null) {
      setSelected(null);
    }

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
    SEDCertificate ecj = getEditable();
    boolean bsuc = false;
    if (ecj != null) {
      String oldAlias = getSelected().getAlias();
      if (!Objects.equals(ecj.getAlias(), oldAlias)) {
        
        try {
          mku.changeAlias(mCertStore.getCertificateStore(), oldAlias, ecj);
        } catch (CertificateException | SEDSecurityException ex) {
          String msg = String.format(
                  "Error changing alias for cert '%s' to '%s'. Err %s",
                  oldAlias, ecj.getAlias(), ex.getMessage());
          LOG.logError(msg, ex);
          addError(msg);
          return false;
        }
      }
      try {
        mCertStore.updateKeystoreCertificate(ecj);
      } catch (SEDSecurityException ex) {
        String msg = String.format(
                "Error saving alias for cert '%s' to '%s'. Err %s",
                oldAlias, ecj.getAlias(), ex.getMessage());
        LOG.logError(msg, ex);
        addError(msg);
        return false;
      }

      bsuc = true;
    }
    return bsuc;
  }

  /**
   *
   * @return
   */
  @Override
  public List<SEDCertificate> getList() {
    try {
      return mCertStore.getCertificateStore().getSEDCertificates();
    } catch (SEDSecurityException ex) {
      String strMsg = String.format("Error retrieving keystore %s!", ex.
              getMessage());
      addError(strMsg);
      LOG.logError(strMsg, ex);
    }
    return null;
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

 

public void save() {
    try (InputStream input = file.getInputStream()) {
      //  Files.copy(input, new File(uploads, filename).toPath());
    }
    catch (IOException e) {
        // Show faces message?
    }
}

  public Part getFile() {
    return file;
  }

  public void setFile(Part file) {
    this.file = file;
  }

}
