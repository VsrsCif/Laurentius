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

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import si.laurentius.cert.SEDCertStore;
import si.laurentius.cert.SEDCertificate;
import si.laurentius.cron.SEDTaskType;
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

  /**
     *
     */
  @Override
  public void createEditable() {
    SEDCertStore cs = new SEDCertStore();

    setNew(cs);

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
      if (stringEquals(c.getAlias(), c.getAlias())
          && stringEquals(c.getIssuerDN(), sc.getIssuerDN())
          && stringEquals(c.getSubjectDN(), sc.getSubjectDN())
          && c.getSerialNumber().equals(sc.getSerialNumber())) {
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
    List<SEDCertStore> lst = mdbLookups.getSEDCertStore();
    LOG.logEnd(l, lst != null ? lst.size() : "null");
    return lst;
  }

  /**
   *
   * @return
   */
  public List<String> getTaskTypeList() {
    long l = LOG.logStart();
    List<String> rstLst = new ArrayList<>();
    List<SEDTaskType> lst = mdbLookups.getSEDTaskTypes();
    lst.stream().forEach((tsk) -> {
      rstLst.add(tsk.getType());
    });
    LOG.logEnd(l, "Task size: " + lst.size());
    return rstLst;
  }

  /**
     *
     */
  @Override
  public void persistEditable() {
    SEDCertStore ecj = getEditable();
    if (ecj != null) {
      mdbLookups.addSEDCertStore(ecj);
    }
  }

  /**
     *
     */
  @Override
  public void removeSelected() {
    if (getSelected() != null) {
      mdbLookups.removeEDCertStore(getSelected());
      setSelected(null);

    }
  }

  /**
     *
     */
  @Override
  public void updateEditable() {
    SEDCertStore ecj = getEditable();
    if (ecj != null) {
      mdbLookups.updateSEDCertStore(ecj);
    }
  }

}
