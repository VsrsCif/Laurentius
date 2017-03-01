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

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import si.laurentius.commons.SEDInterceptorEvents;
import si.laurentius.commons.SEDInboxMailStatus;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDOutboxMailStatus;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.interfaces.SEDCertStoreInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.interfaces.SEDPluginManagerInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.ebox.SEDBox;
import si.laurentius.interceptor.SEDInterceptorRule;
import si.laurentius.interceptor.SEDInterceptorInstance;
import si.laurentius.interceptor.SEDInterceptorProperty;
import si.laurentius.msh.pmode.Service;
import si.laurentius.msh.web.abst.AbstractAdminJSFView;
import si.laurentius.plugin.def.Plugin;
import si.laurentius.plugin.interceptor.MailInterceptorDef;
import si.laurentius.plugin.interceptor.MailInterceptorPropertyDef;
import si.laurentius.plugin.interfaces.PropertyListType;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@ManagedBean(name = "adminSEDInterceptorView")
public class AdminSEDInterceptorView extends AbstractAdminJSFView<SEDInterceptorRule> {

  private static final SEDLogger LOG = new SEDLogger(
          AdminSEDInterceptorView.class);

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  private SEDLookupsInterface mdbLookups;

  @EJB(mappedName = SEDJNDI.JNDI_PLUGIN)
  private SEDPluginManagerInterface mPlgManager;

  @EJB(mappedName = SEDJNDI.JNDI_PMODE)
  private PModeInterface mPMode;
  @EJB(mappedName = SEDJNDI.JNDI_DBCERTSTORE)
  private SEDCertStoreInterface mdbCertStore;

  InterceptorPropertyModel mtpmPropertyModel = new InterceptorPropertyModel();

  /**
   *
   * @param id
   * @return
   */
  public SEDInterceptorRule getInterceptorRuleById(BigInteger id) {
    return mdbLookups.getSEDInterceptorRuleById(id);
  }

  
    public List<SEDInterceptorEvents> getInterceptEvents(){
        return Arrays.asList(SEDInterceptorEvents.values());    
    }
    
  @Override
  public boolean validateData() {
    SEDInterceptorRule cj = getEditable();
    if (Utils.isEmptyString(cj.getName())) {
      addError("Name must not be null ");
      return false;
    }
    if (isEditableNew() && mdbLookups.getSEDInterceptorRuleByName(cj.getName()) != null) {
      addError("Name: '" + cj.getName() + "' already exists!");
      return false;
    }

    for (InterceptorPropertyModelItem tmi : mtpmPropertyModel.
            getInterceptorItems()) {
      if (tmi.getInterceptorDef().getMandatory() && Utils.isEmptyString(tmi.
              getValue())) {
        addError(
                "Property value: '" + tmi.getInterceptorDef().getKey() + "' is required!");
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

    while (mdbLookups.getSEDCronJobByName(String.format(sbname, i)) != null) {
      i++;
    }

    SEDInterceptorRule ecj = new SEDInterceptorRule();
    ecj.setName(String.format(sbname, i));
    ecj.setActive(true);

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
        mtpmPropertyModel.update(isnt, intDef);
        break;
      }
    }
    setNew(ecj);

  }

  @Override
  public void setEditable(SEDInterceptorRule edtbl) {
    super.setEditable(edtbl);
    mtpmPropertyModel.clear();
    SEDInterceptorInstance t = getEditableInstance();
    if (t != null) {
      updateInterceptorPropertyModel(t);
    }

  }

  /**
   *
   */
  @Override
  public void removeSelected() {
    if (getSelected() != null) {
      mdbLookups.removeSEDInterceptorRule(getSelected());
      setSelected(null);
    }
  }

  /**
   *
   */
  @Override
  public boolean persistEditable() {
    boolean bsuc = false;
    SEDInterceptorRule ecj = getEditable();
    if (ecj != null) {
      mtpmPropertyModel.setDataToInterceptor();
      bsuc = mdbLookups.addSEDInterceptorRule(ecj);
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
      mtpmPropertyModel.setDataToInterceptor();
      bsuc = mdbLookups.updateSEDInterceptorRule(ecj);
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
    List<SEDInterceptorRule> lst = mdbLookups.getSEDInterceptorRules();
    LOG.logEnd(l, lst != null ? lst.size() : "null");
    return lst;
  }

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

  public String getEditableInterceptorType() {
    SEDInterceptorInstance t = getEditableInstance();
    return t == null ? null : t.getType();
  }

  private void updateInterceptorPropertyModel(SEDInterceptorInstance t) {
    mtpmPropertyModel.clear();
    if (t == null
            || Utils.isEmptyString(t.getPlugin())
            || Utils.isEmptyString(t.getType())) {
      LOG.formatedWarning("Null plugin or type!");
      return;
    }

    MailInterceptorDef ctd = mPlgManager.getMailInterceptoDef(t.getPlugin(), t.
            getType());
    if (ctd == null) {
      LOG.formatedWarning("Plugin '%s' and interceptor type '%s' not found!", t.
              getPlugin(), t.getType());
      return;
    }
    mtpmPropertyModel.update(t, ctd);
  }

  public SEDInterceptorInstance getEditableInstance() {
    if (getEditable() != null) {
      if (getEditable().getSEDInterceptorInstance() == null) {
        getEditable().setSEDInterceptorInstance(new SEDInterceptorInstance());

      }
      return getEditable().getSEDInterceptorInstance();
    }
    return null;
  }

  public List<InterceptorPropertyModelItem> getInterceptorItems() {
    return mtpmPropertyModel.getInterceptorItems();
  }

  public List<Service.Action> getEditableServiceActionList() {
    if (getEditable() != null
            && !Utils.isEmptyString(getEditable().getService())) {
      String srvId = getEditable().getService();
      Service srv = mPMode.getServiceById(srvId);
      if (srv != null) {
        return srv.getActions();
      }
    }
    return Collections.emptyList();
  }

  public class InterceptorPropertyModel implements Serializable {

    MailInterceptorDef mctdInterceptorDef = null;
    SEDInterceptorInstance mSedInterceptor = null;
    private final List<InterceptorPropertyModelItem> taskItems = new ArrayList<>();

    public void clear() {
      taskItems.clear();
      mctdInterceptorDef = null;
      mSedInterceptor = null;
    }

    public void update(SEDInterceptorInstance task, MailInterceptorDef taskDef) {
      clear();
      mSedInterceptor = task;
      mctdInterceptorDef = taskDef;
      if (mSedInterceptor == null || taskDef == null) {
        return;
      }

      Map<String, String> tpv = new HashMap<>();
      for (SEDInterceptorProperty tp : mSedInterceptor.
              getSEDInterceptorProperties()) {
        tpv.put(tp.getKey(), tp.getValue());
      }

      for (MailInterceptorPropertyDef stp : mctdInterceptorDef.
              getMailInterceptorPropertyDeves()) {

        String key = stp.getKey();
        taskItems.add(new InterceptorPropertyModelItem(stp, tpv.get(key)));
      }
    }

    public void setDataToInterceptor() {
      mSedInterceptor.getSEDInterceptorProperties().clear();
      for (InterceptorPropertyModelItem tmi : taskItems) {
        SEDInterceptorProperty stp = new SEDInterceptorProperty();
        stp.setKey(tmi.getInterceptorDef().getKey());
        stp.setValue(tmi.getValue());
        mSedInterceptor.getSEDInterceptorProperties().add(stp);
      }

    }

    public List<InterceptorPropertyModelItem> getInterceptorItems() {
      return taskItems;
    }
  }

  @Override
  public String getSelectedDesc() {
    if (getSelected() != null) {
      return getSelected().getName();
    }
    return null;
  }

  @Override
  public String getUpdateTargetTable() {
    return ":forms:PanelKeystore:keylist";
  }

  ;

  public class InterceptorPropertyModelItem implements Serializable {

    MailInterceptorPropertyDef mInterceptorPropDef;
    String mValue;

    public InterceptorPropertyModelItem(MailInterceptorPropertyDef ctp,
            String val) {
      mValue = val;
      mInterceptorPropDef = ctp;

    }

    public MailInterceptorPropertyDef getInterceptorDef() {
      return mInterceptorPropDef;
    }

    public String getValue() {
      return mValue;
    }

    public void setValue(String v) {
      this.mValue = v;
    }

    public Integer getIntValue() {
      return mValue != null ? new Integer(mValue) : null;
    }

    public void setIntValue(Integer v) {

      this.mValue = v != null ? v.toString() : null;

    }

    public Boolean getBooleanValue() {
      return mValue != null ? mValue.equalsIgnoreCase("true") : null;
    }

    public void setBooleanValue(Boolean v) {
      this.mValue = v ? "true" : "false";
    }

    public List<String> getListValues() {
      String lst = getInterceptorDef().getValueList();
      List<String> lstArr = new ArrayList<>();
      if (Utils.isEmptyString(lst)) {
        return Collections.emptyList();
      } else if (lst.equalsIgnoreCase(PropertyListType.LocalBoxes.getType())) {
        List<SEDBox> sblst = mdbLookups.getSEDBoxes();
        sblst.forEach(sb -> {
          lstArr.add(sb.getLocalBoxName());
        });
      } else if (lst.
              equalsIgnoreCase(PropertyListType.KeystoreCertAll.getType())) {
        lstArr.addAll(mdbCertStore.getKeystoreAliases(false));
      } else if (lst.equalsIgnoreCase(PropertyListType.KeystoreCertKeys.
              getType())) {
        lstArr.addAll(mdbCertStore.getKeystoreAliases(true));

      } else if (lst.equalsIgnoreCase(PropertyListType.InMailStatus.getType())) {
        for (SEDInboxMailStatus st : SEDInboxMailStatus.values()) {
          lstArr.add(st.getValue());

        }
      } else if (lst.equalsIgnoreCase(PropertyListType.OutMailStatus.getType())) {
        for (SEDOutboxMailStatus st : SEDOutboxMailStatus.values()) {
          lstArr.add(st.getValue());

        }
      }
      return lstArr;
    }
  }

}
