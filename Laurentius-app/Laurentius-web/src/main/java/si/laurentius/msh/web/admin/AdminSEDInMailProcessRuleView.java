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

import java.util.Collections;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.pmode.Service;
import si.laurentius.msh.web.abst.AbstractAdminJSFView;
import si.laurentius.process.SEDProcessorRule;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "adminSEDInMailProcessRuleView")
public class AdminSEDInMailProcessRuleView extends AbstractAdminJSFView<SEDProcessorRule> {

  private static final SEDLogger LOG = new SEDLogger(AdminSEDInMailProcessRuleView.class);

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  private SEDLookupsInterface mdbLookups;

  @EJB(mappedName = SEDJNDI.JNDI_PMODE)
  private PModeInterface mPMode;


  @Override
  public boolean validateData() {

    return true;
  }

  /**
   *
   */
  @Override
  public void createEditable() {
    SEDProcessorRule ecj = new SEDProcessorRule();
   
    setNew(ecj);

  }

  /**
   *
   */
  @Override
  public void removeSelected() {
    if (getSelected() != null) {
      mdbLookups.removeSEDProcessorRule(getSelected());
      setSelected(null);
    }
  }

  /**
   *
   */
  @Override
  public boolean persistEditable() {
    boolean bsuc = false;
    SEDProcessorRule ecj = getEditable();
    if (ecj != null) {
      mdbLookups.addSEDProcessorRule(ecj);
      
      bsuc = true;
    }
    return bsuc;

  }

  /**
   *
   */
  @Override
  public boolean updateEditable() {
    SEDProcessorRule ecj = getEditable();
    boolean bsuc = false;
    if (ecj != null) {
      mdbLookups.updateSEDProcessorRule(ecj);

    
      bsuc = true;
    }
    return bsuc;
  }

  /**
   *
   * @return
   */
  @Override
  public List<SEDProcessorRule> getList() {
    long l = LOG.logStart();
    List<SEDProcessorRule> lst = mdbLookups.getSEDProcessorRules();
    LOG.logEnd(l, lst != null ? lst.size() : "null");
    return lst;
  }

public List<Service.Action> getEditableServiceActionList() {
    if (getEditable()!= null &&
        !Utils.isEmptyString(getEditable().getService())) {
      String srvId = getEditable().getService();
      Service srv = mPMode.getServiceById(srvId);
      if (srv != null) {
        return srv.getActions();
      }
    }
    return Collections.emptyList();
  }

}
