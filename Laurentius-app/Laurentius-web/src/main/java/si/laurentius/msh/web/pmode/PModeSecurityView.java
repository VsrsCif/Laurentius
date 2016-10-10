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
package si.laurentius.msh.web.pmode;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.msh.pmode.Security;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "pModeSecurityView")
public class PModeSecurityView extends AbstractPModeJSFView<Security> {

  /**
   *
   */
  public static SEDLogger LOG = new SEDLogger(PModeSecurityView.class);

  @EJB(mappedName = SEDJNDI.JNDI_PMODE)
  PModeInterface mPModeInteface;


  /**
   *
   */
  @PostConstruct
  public void init() {

  }

  /**
   *
   */
  @Override
  public void createEditable() {
    Security pmodeService = new Security();
    setNew(pmodeService);

  }

  /**
   *
   */
  @Override
  public void removeSelected() {

    long l = LOG.logStart();

    Security srv = getSelected();
    if (srv != null) {
      mPModeInteface.removeSecurity(srv);;
    }

  }

   @Override
  public boolean validateData() {
    
    return true;
  }
  /**
   *
   */
  @Override
  public boolean persistEditable() {
    long l = LOG.logStart();
    boolean bsuc = false;
    Security sv = getEditable();
    if (sv != null) {      
      mPModeInteface.addSecurity(sv);
      setEditable(null);
      return bsuc;
    }
    return bsuc;
  }

  /**
   *
   */
  @Override
  public boolean updateEditable() {
    long l = LOG.logStart();
    boolean bsuc = false;
    Security sv = getEditable();
    if (sv != null) {      
      mPModeInteface.updateSecurity(sv);
      setEditable(null);
      bsuc = true;
    }
    return bsuc;
  }

  /**
   *
   * @return
   */
  @Override
  public List<Security> getList() {
    long l = LOG.logStart();
    List<Security> lst = mPModeInteface.getSecurities();
    LOG.logEnd(l);
    return lst;

  }


}
