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

import java.io.StringWriter;
import java.security.KeyStore;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import si.laurentius.cert.SEDCertStore;
import si.laurentius.cert.SEDCertificate;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.sec.KeystoreUtils;
import si.laurentius.msh.web.abst.AbstractAdminJSFView;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "adminSEDKeyStores")
public class AdminSEDKeyStores extends AbstractAdminJSFView<SEDCertStore> {

  private static final SEDLogger LOG = new SEDLogger(AdminSEDKeyStores.class);

  KeystoreUtils mku = new KeystoreUtils();

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  private SEDLookupsInterface mdbLookups;

  List<SEDCertStore> mCertStoreList = null;

  /**
   *
   */
  @Override
  public void createEditable() {
    SEDCertStore cs = new SEDCertStore();

    setNew(cs);

  }

   @Override
  public boolean validateData() {
    
    return true;
  }
  /**
   *
   */
  public void refreshCurrentKeystore() {
    long l = LOG.logStart();

    if (getEditable() != null) {

      List<SEDCertificate> src = getEditable().getSEDCertificates();

      try {
        KeyStore ks =
            mku.openKeyStore(getEditable().getFilePath(), getEditable().getType(), getEditable()
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
        getEditable().setStatus("SUCCESS");
      } catch (SEDSecurityException ex) {
        getEditable().setStatus("ERROR");
        LOG.logWarn(l, getEditable().getFilePath(), ex);
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
      if (stringEquals(c.getAlias(), c.getAlias()) &&
          stringEquals(c.getIssuerDN(), sc.getIssuerDN()) &&
          stringEquals(c.getSubjectDN(), sc.getSubjectDN()) &&
          c.getSerialNumber().equals(sc.getSerialNumber())) {
        return c;
      }
    }
    return null;
  }

  /**
   *
   * @param s1
   * @param s2
   * @return
   */
  public boolean stringEquals(String s1, String s2) {
    return s1 != null && s2 != null && s1.equals(s2) || s2 == null && s2 == null;
  }

  /**
   *
   * @return
   */
  @Override
  public List<SEDCertStore> getList() {
    long l = LOG.logStart();
    List<SEDCertStore> lst  = mdbLookups.getSEDCertStore();
    LOG.logEnd(l, lst);
    return lst;
  }

  public void refreshCertStoreList() {
    mCertStoreList = mdbLookups.getSEDCertStore();
    StringWriter sw = new StringWriter();
    for (SEDCertStore cs : mCertStoreList) {
      for (SEDCertificate c : cs.getSEDCertificates()) {
        if (c.getValidTo().before(Calendar.getInstance().getTime())) {
          sw.append(String.format("Certificate '%s' in keystore '%s' in invalid!\n", c.getAlias(),
              cs.getName()));
        }
      }
    }
    String wrn = sw.toString();
    if (wrn.isEmpty()) {
      facesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
          "Invalid certificate", wrn));
    }
  }

  public boolean isCertInvalid(SEDCertificate crt) {
    
    Date currDate = Calendar.getInstance().getTime();
    return crt!= null && (crt.getValidTo() == null ||
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

  /**
   *
   * @return
   *
   * public List<String> getTaskTypeList() { long l = LOG.logStart(); List<String> rstLst = new
   * ArrayList<>(); List<SEDTaskType> lst = mdbLookups.getSEDTaskTypes(); lst.stream().forEach((tsk)
   * -> { rstLst.add(tsk.getType()); }); LOG.logEnd(l, "Task size: " + lst.size()); return rstLst; }
   */
  /**
   *
   */
  @Override
  public boolean persistEditable() {
    boolean bsuc = false;
    SEDCertStore ecj = getEditable();
    if (ecj != null) {
      mdbLookups.addSEDCertStore(ecj);
      bsuc = true;
      refreshCertStoreList();
      
    }
    return bsuc;
  }

  /**
   *
   */
  @Override
  public void removeSelected() {
    if (getSelected() != null) {
      mdbLookups.removeEDCertStore(getSelected());
      setSelected(null);
      refreshCertStoreList();
    }
  }

  /**
   *
   */
  @Override
  public boolean updateEditable() {
    SEDCertStore ecj = getEditable();
    boolean bsuc = false;
    if (ecj != null) {
      mdbLookups.updateSEDCertStore(ecj);
      bsuc = true;
      refreshCertStoreList();
    }
    return bsuc;
  }

}
