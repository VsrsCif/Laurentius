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
import si.laurentius.cron.SEDCronJob;
import si.laurentius.msh.web.abst.AbstractAdminJSFView;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "adminSEDCRLView")
public class AdminSEDCRLView extends AbstractAdminJSFView<SEDCertCRL> {

  private static final SEDLogger LOG = new SEDLogger(AdminSEDCRLView.class);

  @EJB(mappedName = SEDJNDI.JNDI_DBCERTSTORE)
  SEDCertStoreInterface mCertStore;


  @Override
  public void createEditable() {}


  @Override
  public List<SEDCertCRL> getList() {
   long l = LOG.logStart();
    List<SEDCertCRL> lst = mCertStore.getSEDCertCRLs();
    LOG.logEnd(l, lst);
    return lst;
  }

  @Override
  public boolean persistEditable() {
    return false;
  }


  public void refreshCRLList() {
    mCertStore.refreshCrlLists();
  }

  @Override
  public boolean removeSelected() {
    return false;
  }

  @Override
  public boolean updateEditable() {
    return false;
  }

  @Override
  public boolean validateData() {
    return false;
  }
}
