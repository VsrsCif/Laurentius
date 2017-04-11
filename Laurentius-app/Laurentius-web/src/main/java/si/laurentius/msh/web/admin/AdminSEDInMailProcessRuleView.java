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

import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.msh.web.abst.AbstractAdminJSFView;
import si.laurentius.process.SEDProcessor;
import si.laurentius.process.SEDProcessorRule;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "adminSEDInMailProcessRuleView")
public class AdminSEDInMailProcessRuleView extends AbstractAdminJSFView<SEDProcessorRule> {

  private static final SEDLogger LOG = new SEDLogger(
          AdminSEDInMailProcessRuleView.class);

  @ManagedProperty(value = "#{adminSEDInMailProcessView}")
  private AdminSEDInMailProcessView admProcessorView;

  public AdminSEDInMailProcessView getAdmProcessorView() {
    return admProcessorView;
  }

  public void setAdmProcessorView(AdminSEDInMailProcessView admProcessorView) {
    this.admProcessorView = admProcessorView;
  }



  @Override
  public boolean validateData() {
    

    return true;
  }

  /**
   *
   */
  @Override
  public void createEditable() {
    SEDProcessor sps = admProcessorView.getEditable();
    // test is exists selected processor
    if (sps != null) {
      SEDProcessorRule spi = new SEDProcessorRule();

      setNew(spi);
    }

  }

  /**
   *
   */
  @Override
  public boolean removeSelected() {
    boolean bSuc = false;
    SEDProcessorRule ecj = getSelected();
    if (ecj != null) {
      bSuc = admProcessorView.removeRuleFromEditable(ecj);
      setSelected(null);

    } else {
      addError("Select process instance!");
    }
    return bSuc;
  }

  /**
   *
   */
  @Override
  public boolean persistEditable() {
    boolean bsuc = false;

    SEDProcessorRule ecj = getEditable();
    if (ecj != null) {
      bsuc = admProcessorView.addRuleToEditable(ecj);
    } else {
      addError("No editable process instance!");
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
      bsuc = admProcessorView.updateRuleFromEditable(getSelected(), ecj);

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
    List<SEDProcessorRule> lst = admProcessorView.getEditable() != null ? admProcessorView.
            getEditable().getSEDProcessorRules() : null;
    LOG.logEnd(l, lst != null ? lst.size() : "null");
    return lst;
  }

  /**
   *
   * @return
   */
  @Override
  public String getSelectedDesc() {
    if (getSelected() != null) {
      return getSelected().getProperty() + ":" + getSelected().getValue();
    }
    return null;
  }


}
