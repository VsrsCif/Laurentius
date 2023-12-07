/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.jrc.msh.plugin.tc.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import si.jrc.msh.plugin.tc.TestCaseInInterceptor;
import si.jrc.msh.plugin.tc.TestCasePluginDescription;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.enums.SEDInterceptorEvent;
import si.laurentius.commons.enums.SEDInterceptorRole;
import si.laurentius.commons.enums.SEDRulePredicate;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.interceptor.SEDInterceptor;
import si.laurentius.interceptor.SEDInterceptorInstance;
import si.laurentius.interceptor.SEDInterceptorRule;

@SessionScoped
@Named("tcReliablityTests")
public class TCReliablityTests extends TestCaseAbstract implements Serializable {

  private static final SEDLogger LOG = new SEDLogger(TCReliablityTests.class);

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  private SEDLookupsInterface mdbLookups;
  
  SEDInterceptor selected = null;
  

  public SEDInterceptor getSelected() {
    return selected;
  }

  public void setSelected(SEDInterceptor selected) {
    this.selected = selected;
  }
  
  

  public List<SEDInterceptor> getList() {
    List<SEDInterceptor> lstInt = new ArrayList<>();
    for (SEDInterceptor si : mdbLookups.getSEDInterceptors()) {
      if (si.isActive() && si.getSEDInterceptorInstance() != null
              && Objects.equals(si.getSEDInterceptorInstance().getPlugin(),
                      TestCasePluginDescription.S_PLUGIN_TYPE)
              && Objects.equals(si.getSEDInterceptorInstance().getType(),
                      TestCaseInInterceptor.S_INTERCEPTOR_TYPE)) {
        lstInt.add(si);
      }
    }
    return lstInt;
  }

  public void createEditable() {

    String sbname = "st_%03d";
    int i = 1;

    while (mdbLookups.getSEDInterceptorByName(String.format(sbname, i)) != null) {
      i++;
    }

    SEDInterceptor ecj = new SEDInterceptor();
    ecj.setName(String.format(sbname, i));
    ecj.setActive(true);
    ecj.setInterceptEvent(SEDInterceptorEvent.IN_MESSAGE.getValue());
    ecj.setInterceptRole(SEDInterceptorRole.ALL.getValue());

    SEDInterceptorInstance isnt = new SEDInterceptorInstance();
    ecj.setSEDInterceptorInstance(isnt);

    // set  first cront task;
    isnt.setPlugin(TestCasePluginDescription.S_PLUGIN_TYPE);
    isnt.setPluginVersion(TestCasePluginDescription.S_PLUGIN_VERSION);
    isnt.setType(TestCaseInInterceptor.S_INTERCEPTOR_TYPE);
    
    
    SEDInterceptorRule sirSrv = new SEDInterceptorRule();
    sirSrv.setProperty("Service");
    sirSrv.setPredicate(SEDRulePredicate.EQUALS.getValue());
    sirSrv.setValue(getTestService());
    ecj.getSEDInterceptorRules().add(sirSrv);
    
    SEDInterceptorRule sirAct = new SEDInterceptorRule();
    sirAct.setProperty("Action");
    sirAct.setPredicate(SEDRulePredicate.EQUALS.getValue());
    sirAct.setValue(getTestAction());    
    ecj.getSEDInterceptorRules().add(sirAct);
    
    SEDInterceptorRule sirBox = new SEDInterceptorRule();
    sirBox.setProperty("ReceiverEBox");
    sirBox.setPredicate(SEDRulePredicate.EQUALS.getValue());
    sirBox.setValue(getTestReceiverEBox());    
    ecj.getSEDInterceptorRules().add(sirBox);
    
    mdbLookups.addSEDInterceptor(ecj);

  }
  
  public void removeSelected(){
    if (selected!=null){
      mdbLookups.removeSEDInterceptor(selected);
      selected=null;
    }
  }

  public String getRuleDesc(SEDInterceptor pr) {
    String strVal = "";
    if (pr != null && pr.getSEDInterceptorRules().size() > 0) {
      strVal = pr.getSEDInterceptorRules().stream().
              map((prr) -> prr.getProperty() + " " + prr.getPredicate() + " " + prr.
              getValue() + ",").
              reduce(strVal,
                      String::concat);
    }
    return strVal;

  }
}
