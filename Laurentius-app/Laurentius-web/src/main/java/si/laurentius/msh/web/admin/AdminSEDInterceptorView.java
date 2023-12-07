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


import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import si.laurentius.commons.enums.SEDInterceptorEvent;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.interfaces.SEDPluginManagerInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.interceptor.SEDInterceptor;
import si.laurentius.interceptor.SEDInterceptorInstance;
import si.laurentius.interceptor.SEDInterceptorProperty;
import si.laurentius.interceptor.SEDInterceptorRule;
import si.laurentius.msh.web.abst.AbstractAdminJSFView;
import si.laurentius.msh.web.plugin.PluginPropertyModel;
import si.laurentius.msh.web.plugin.PluginPropertyModelItem;
import si.laurentius.plugin.def.Plugin;
import si.laurentius.plugin.interceptor.MailInterceptorDef;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@Named("adminSEDInterceptorView")
public class AdminSEDInterceptorView extends AbstractAdminJSFView<SEDInterceptor> {

  private static final SEDLogger LOG = new SEDLogger(
          AdminSEDInterceptorView.class);

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  private SEDLookupsInterface mdbLookups;

  @EJB(mappedName = SEDJNDI.JNDI_PLUGIN)
  private SEDPluginManagerInterface mPlgManager;

 

  PluginPropertyModel mtpmPropertyModel = new PluginPropertyModel();

  /**
   *
   * @param id
   * @return
   */
  public SEDInterceptor getInterceptorRuleById(BigInteger id) {
    return mdbLookups.getSEDInterceptorById(id);
  }


  /**
   *
   * @return
   */
  @Override
  public boolean validateData() {
    SEDInterceptor cj = getEditable();
    if (Utils.isEmptyString(cj.getName())) {
      addError("Name must not be null ");
      return false;
    }
    if (isEditableNew() && mdbLookups.getSEDInterceptorByName(cj.getName()) != null) {
      addError("Name: '" + cj.getName() + "' already exists!");
      return false;
    }

    for (PluginPropertyModelItem tmi : mtpmPropertyModel.
            getPluginProperties()) {
      if (tmi.getPropertyDef().isMandatory() && Utils.isEmptyString(tmi.
              getValue())) {
        addError(
                "Property value: '" + tmi.getPropertyDef().getKey() + "' is required!");
        return false;
      }
    }

    return true;
  }

  /**
   *
   */
  @Override
  public void createEditable() {

    String sbname = "int_%03d";
    int i = 1;

    while (mdbLookups.getSEDInterceptorByName(String.format(sbname, i)) != null) {
      i++;
    }

    SEDInterceptor ecj = new SEDInterceptor();
    ecj.setName(String.format(sbname, i));
    ecj.setActive(true);
    ecj.setInterceptEvent(SEDInterceptorEvent.IN_MESSAGE.getValue());

    SEDInterceptorInstance isnt = new SEDInterceptorInstance();
    ecj.setSEDInterceptorInstance(isnt);

    // set  first cront task;
    List<Plugin> lstPlg = mPlgManager.getRegistredPlugins();
    for (Plugin p : lstPlg) {
      if (!p.getMailInterceptorDeves().isEmpty()) {
        MailInterceptorDef intDef = p.getMailInterceptorDeves().get(0);
        isnt.setPlugin(p.getType());
        isnt.setPluginVersion(p.getVersion());
        isnt.setType(intDef.getType());

        Map<String, String> tpv = new HashMap<>();
        isnt.getSEDInterceptorProperties().forEach((tp) -> {
          tpv.put(tp.getKey(), tp.getValue());
        });
        mtpmPropertyModel.setPluginProperties(tpv, intDef.
                getMailInterceptorPropertyDeves());
        break;
      }
    }
    setNew(ecj);

  }

  @Override
  public void setEditable(SEDInterceptor edtbl) {
    super.setEditable(edtbl);
    mtpmPropertyModel.clear();
    SEDInterceptorInstance t = getEditableInstance();
    if (t != null) {
      updateInterceptorPropertyModel(t);
    }

  }

  /**
   *
   * @return
   */
  @Override
  public boolean removeSelected() {
    boolean bSuc = false;
    if (getSelected() != null) {
      bSuc = mdbLookups.removeSEDInterceptor(getSelected());
      setSelected(null);
    }
    return bSuc;
  }

  /**
   *
   * @return
   */
  @Override
  public boolean persistEditable() {
    boolean bsuc = false;
    SEDInterceptor ecj = getEditable();
    if (ecj != null) {

      setPropertyDataToInterceptorInstance(ecj.getSEDInterceptorInstance());
      bsuc = mdbLookups.addSEDInterceptor(ecj);
    }
    return bsuc;

  }

  /**
   *
   * @return
   */
  @Override
  public boolean updateEditable() {
    SEDInterceptor ecj = getEditable();
    boolean bsuc = false;
    if (ecj != null) {
      setPropertyDataToInterceptorInstance(ecj.getSEDInterceptorInstance());
      bsuc = mdbLookups.updateSEDInterceptor(ecj);
    }
    return bsuc;
  }

  /**
   *
   * @return
   */
  @Override
  public List<SEDInterceptor> getList() {
    long l = LOG.logStart();
    List<SEDInterceptor> lst = mdbLookups.getSEDInterceptors();
    LOG.logEnd(l, lst != null ? lst.size() : "null");
    return lst;
  }

  /**
   *
   * @return
   */
  public String getEditableInterceptorPluginType() {
    SEDInterceptorInstance t = getEditableInstance();
    return t == null ? null : t.getPlugin();
  }

  /**
   * Set plugin for editable task
   *
   * @param strPlugin
   */
  public void setEditableInterceptorPluginType(String strPlugin) {
    SEDInterceptorInstance t = getEditableInstance();
    if (t != null && !Objects.equals(strPlugin, t.getPlugin())) {
      t.setPlugin(strPlugin);
      // set type
      Plugin plg = mPlgManager.getPluginByType(strPlugin);
      setEditableInterceptorType(
              plg != null && !plg.getMailInterceptorDeves().isEmpty()
              ? plg.getMailInterceptorDeves().get(0).getType() : null);

    }
  }

  /**
   *
   * @return
   */
  public List<MailInterceptorDef> getEditablePluginInterceptorDeves() {

    SEDInterceptorInstance t = getEditableInstance();
    Plugin plg = null;
    if (t != null && !Utils.
            isEmptyString(t.getPlugin())) {
      plg = mPlgManager.getPluginByType(t.getPlugin());
    }
    return plg != null ? plg.getMailInterceptorDeves() : Collections.emptyList();
  }

  /**
   * Set Interceptor type from selected plugin
   *
   * @param strInterceptorType
   */
  public void setEditableInterceptorType(String strInterceptorType) {
    SEDInterceptorInstance t = getEditableInstance();
    if (t != null && !Objects.equals(t.getType(), strInterceptorType)) {
      t.setType(strInterceptorType);
      updateInterceptorPropertyModel(t);

    }
  }

  /**
   *
   * @return
   */
  public String getEditableInterceptorType() {
    SEDInterceptorInstance t = getEditableInstance();
    return t == null ? null : t.getType();
  }

  private void updateInterceptorPropertyModel(SEDInterceptorInstance isnt) {
    mtpmPropertyModel.clear();
    if (isnt == null
            || Utils.isEmptyString(isnt.getPlugin())
            || Utils.isEmptyString(isnt.getType())) {
      LOG.formatedWarning("Null plugin or type!");
      return;
    }

    MailInterceptorDef ctd = mPlgManager.getMailInterceptoDef(isnt.getPlugin(),
            isnt.
                    getType());
    if (ctd == null) {
      LOG.formatedWarning("Plugin '%s' and interceptor type '%s' not found!",
              isnt.
                      getPlugin(), isnt.getType());
      return;
    }

    Map<String, String> tpv = new HashMap<>();
    for (SEDInterceptorProperty tp : isnt.
            getSEDInterceptorProperties()) {
      tpv.put(tp.getKey(), tp.getValue());
    }
    mtpmPropertyModel.setPluginProperties(tpv, ctd.
            getMailInterceptorPropertyDeves());

  }

  /**
   *
   * @return
   */
  public SEDInterceptorInstance getEditableInstance() {
    if (getEditable() != null) {
      if (getEditable().getSEDInterceptorInstance() == null) {
        getEditable().setSEDInterceptorInstance(new SEDInterceptorInstance());

      }
      return getEditable().getSEDInterceptorInstance();
    }
    return null;
  }

  /**
   *
   * @return
   */
  public List<PluginPropertyModelItem> getInterceptorItems() {
    return mtpmPropertyModel.getPluginProperties();
  }

  
  /**
   *
   * @return
   * 
  public List<Service.Action> getEditableServiceActionList() {
    if (getEditableDecisionRule() != null
            && !Utils.isEmptyString(getEditableDecisionRule().getService())) {
      String srvId = getEditableDecisionRule().getService();
      Service srv = mPMode.getServiceById(srvId);
      if (srv != null) {
        return srv.getActions();
      }
    }
    return Collections.emptyList();
  }
  
  public SEDDecisionRule getEditableDecisionRule(){
    SEDDecisionRule dr = null;
    if (getEditable() != null){
      if (getEditable().getSEDDecisionRule() == null){
        getEditable().setSEDDecisionRule(new SEDDecisionRule());
      }
      dr = getEditable().getSEDDecisionRule();
    }
    return dr;
    
  }*/

  /**
   *
   * @return
   */
  @Override
  public String getSelectedDesc() {
    if (getSelected() != null) {
      return getSelected().getName();
    }
    return null;
  }


  public void setPropertyDataToInterceptorInstance(SEDInterceptorInstance inst) {
    inst.getSEDInterceptorProperties().clear();
    for (PluginPropertyModelItem tmi : mtpmPropertyModel.getPluginProperties()) {
      SEDInterceptorProperty stp = new SEDInterceptorProperty();
      stp.setKey(tmi.getPropertyDef().getKey());
      stp.setValue(tmi.getValue());
      inst.getSEDInterceptorProperties().add(stp);
    }
  }
  
  public boolean addRuleToEditable(SEDInterceptorRule spi) {
    boolean bsuc = false;
    SEDInterceptor pr = getEditable();
    if (pr != null) {
      bsuc = pr.getSEDInterceptorRules().add(spi);
    } else {
      addError("No editable process rule!");
    }
    return bsuc;
  }

  public boolean removeRuleFromEditable(SEDInterceptorRule spi) {
    boolean bsuc = false;
    SEDInterceptor pr = getEditable();
    if (pr != null) {
      bsuc = pr.getSEDInterceptorRules().remove(spi);
    } else {
      addError("No editable process rule!");
    }
    return bsuc;
  }

  public boolean updateRuleFromEditable(SEDInterceptorRule spiOld,
          SEDInterceptorRule spiNew) {
    boolean bsuc = false;
    SEDInterceptor pr = getEditable();
    if (pr != null) {
      int i = pr.getSEDInterceptorRules().indexOf(spiOld);
      pr.getSEDInterceptorRules().remove(i);
      pr.getSEDInterceptorRules().add(i, spiNew);
      bsuc = true;

    } else {
      addError("No editable process rule!");
    }
    return bsuc;
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
