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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import si.laurentius.commons.enums.SEDRulePredicate;
import si.laurentius.commons.utils.ReflectUtils;

import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.interceptor.SEDInterceptor;
import si.laurentius.interceptor.SEDInterceptorRule;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.web.abst.AbstractAdminJSFView;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "adminSEDInterceptorRuleView")
public class AdminSEDInterceptorRuleView extends AbstractAdminJSFView<SEDInterceptorRule> {

  private static final SEDLogger LOG = new SEDLogger(
          AdminSEDInterceptorRuleView.class);

  @ManagedProperty(value = "#{adminSEDInterceptorView}")
  private AdminSEDInterceptorView admRuleView;

  public AdminSEDInterceptorView getAdmRuleView() {
    return admRuleView;
  }

  public void setAdmRuleView(AdminSEDInterceptorView admRuleView) {
    this.admRuleView = admRuleView;
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
    SEDInterceptor sps = admRuleView.getEditable();
    if (sps != null) {
      SEDInterceptorRule spi = new SEDInterceptorRule();

      setNew(spi);
    }

  }

  /**
   *
   */
  @Override
  public boolean removeSelected() {
    boolean bSuc = false;
    SEDInterceptorRule ecj = getSelected();
    if (ecj != null) {
      bSuc = admRuleView.removeRuleFromEditable(ecj);
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

    SEDInterceptorRule ecj = getEditable();
    if (ecj != null) {
      bsuc = admRuleView.addRuleToEditable(ecj);
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
    SEDInterceptorRule ecj = getEditable();
    boolean bsuc = false;
    if (ecj != null) {
      bsuc = admRuleView.updateRuleFromEditable(getSelected(), ecj);

    }
    return bsuc;
  }

  /**
   *
   * @return
   */
  @Override
  public List<SEDInterceptorRule> getList() {
    long l = LOG.logStart();
    List<SEDInterceptorRule> lst = admRuleView.getEditable() != null ? admRuleView.
            getEditable().getSEDInterceptorRules() : null;
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


  public List<SEDRulePredicate> getRulePredicatesForProperty(String prp) {
    if (Utils.isEmptyString(prp)) {
      return Arrays.asList(SEDRulePredicate.values());
    } else {
      List<SEDRulePredicate> slpr = new ArrayList<>();
      Class cls = ReflectUtils.getReturnTypeForProperty(MSHInMail.class, prp);
      for (SEDRulePredicate pr: SEDRulePredicate.values()){
        if (pr.getTypeClasses().contains(cls)){
          slpr.add(pr);
        }
      }
      if (slpr.isEmpty()){
        LOG.formatedWarning("No predicates for property: %s with type %s", prp, cls.getName());
      }
      return slpr;              
    }

  }

}
