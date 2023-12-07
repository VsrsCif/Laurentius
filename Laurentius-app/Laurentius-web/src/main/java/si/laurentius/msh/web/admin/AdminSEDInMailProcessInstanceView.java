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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.inject.Inject;
import javax.enterprise.context.SessionScoped;

import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.interfaces.SEDPluginManagerInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.web.abst.AbstractAdminJSFView;
import si.laurentius.msh.web.plugin.PluginPropertyModel;
import si.laurentius.msh.web.plugin.PluginPropertyModelItem;
import si.laurentius.plugin.def.Plugin;
import si.laurentius.plugin.processor.InMailProcessorDef;
import si.laurentius.process.SEDProcessorInstance;
import si.laurentius.process.SEDProcessorProperty;
import si.laurentius.process.SEDProcessor;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@Named("adminSEDInMailProcessInstanceView")
public class AdminSEDInMailProcessInstanceView extends AbstractAdminJSFView<SEDProcessorInstance> {

  private static final SEDLogger LOG = new SEDLogger(
          AdminSEDInMailProcessInstanceView.class);

  @EJB(mappedName = SEDJNDI.JNDI_PLUGIN)
  private SEDPluginManagerInterface mPlgManager;

  @Inject
  private AdminSEDInMailProcessView admProcView;

  PluginPropertyModel mtpmPropertyModel = new PluginPropertyModel();

  @Override
  public boolean validateData() {
    SEDProcessorInstance cj = getEditable();
    if (Utils.isEmptyString(cj.getPlugin())) {
      addError("Select plugin!");
      return false;
    }
    if (Utils.isEmptyString(cj.getType())) {
      addError("Select processor type!");
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

  @Override
  public void startEditSelected() {
    super.startEditSelected();
    updateEditableProcessorPropertyModel();
  }

  /**
   *
   */
  @Override
  public void createEditable() {
    SEDProcessor sps = admProcView.getEditable();
    if (sps != null) {
      SEDProcessorInstance spi = new SEDProcessorInstance();
      for (Plugin plg : mPlgManager.getRegistredPlugins()) {
        if (!plg.getInMailProcessorDeves().isEmpty()) {
          spi.setPlugin(plg.getType());
          spi.setPluginVersion(plg.getVersion());
          spi.setType(plg.getInMailProcessorDeves().get(0).getType());

          break;
        }
      }
      setNew(spi);
    }
    updateEditableProcessorPropertyModel();
  }

  /**
   *
   */
  @Override
  public boolean removeSelected() {
    boolean bSuc = false;
    SEDProcessorInstance ecj = getSelected();
    if (ecj != null) {
      bSuc = admProcView.removeInstanceFromEditable(ecj);
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

    SEDProcessorInstance ecj = getEditable();
    if (ecj != null) {
      setPropertyDataToEditableProcessInstance();
      bsuc = admProcView.addInstanceToEditable(ecj);
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
    SEDProcessorInstance ecj = getEditable();
    boolean bsuc = false;
    if (ecj != null) {
      setPropertyDataToEditableProcessInstance();
      bsuc = admProcView.updateInstanceFromEditable(getSelected(), ecj);

    }
    return bsuc;
  }

  /**
   *
   * @return
   */
  @Override
  public List<SEDProcessorInstance> getList() {
    long l = LOG.logStart();
    List<SEDProcessorInstance> lst = admProcView.getEditable() != null ? admProcView.
            getEditable().getSEDProcessorInstances() : null;
    LOG.logEnd(l, lst != null ? lst.size() : "null");
    return lst;
  }

  /**
   *
   * @return
   */
  public List<PluginPropertyModelItem> getSelectedProcessorItems() {
    return mtpmPropertyModel.getPluginProperties();
  }

  public void selectedInstanceToTop() {
    SEDProcessorInstance spi = getSelected();
    SEDProcessor epr = admProcView.getEditable();
    if (epr != null && getSelected() != null) {
      int idx = epr.getSEDProcessorInstances().indexOf(spi);
      if (idx > 0) {
        epr.getSEDProcessorInstances().remove(spi);
        epr.getSEDProcessorInstances().add(0, spi);
      }

    } else {
      addError("Select process instance!");
    }
  }

  public void selectedInstanceUp() {
    SEDProcessorInstance spi = getSelected();
    SEDProcessor epr = admProcView.getEditable();
    if (epr != null && spi != null) {
      int idx = epr.getSEDProcessorInstances().indexOf(spi);
      if (idx > 0) {
        epr.getSEDProcessorInstances().remove(spi);
        epr.getSEDProcessorInstances().add(--idx, spi);
      }

    } else {
      addError("Select process instance!");
    }
  }

  public void selectedInstanceDown() {
    SEDProcessorInstance spi = getSelected();
    SEDProcessor epr = admProcView.getEditable();
    if (epr != null && spi != null) {
      int idx = epr.getSEDProcessorInstances().indexOf(spi);
      if (idx < epr.getSEDProcessorInstances().size() - 1) {
        epr.getSEDProcessorInstances().remove(spi);
        epr.getSEDProcessorInstances().add(++idx, spi);
      }

    } else {
      addError("Select process instance!");
    }
  }

  public void selectedInstanceToBottom() {
    SEDProcessorInstance spi = getSelected();
    SEDProcessor epr = admProcView.getEditable();
    if (epr != null && spi != null) {
      int idx = epr.getSEDProcessorInstances().indexOf(spi);
      if (idx < epr.getSEDProcessorInstances().size() - 1) {
        epr.getSEDProcessorInstances().remove(spi);
        epr.getSEDProcessorInstances().add(spi);
      }

    } else {
      addError("Select process instance!");
    }
  }

  public List<InMailProcessorDef> selectedSPIProcessorList() {
    SEDProcessorInstance spi = getEditable();

    if (spi != null && !Utils.isEmptyString(spi.getPlugin())) {
      Plugin plg = mPlgManager.getPluginByType(spi.getPlugin());
      if (plg != null) {
        return plg.getInMailProcessorDeves();
      }
    }
    return Collections.emptyList();
  }

  public void setEditableProcessorType(String strInterceptorType) {
    SEDProcessorInstance spi = getEditable();
    if (spi != null && !Objects.equals(spi.getType(), strInterceptorType)) {
      spi.setType(strInterceptorType);
      updateEditableProcessorPropertyModel();
    } else {
      LOG.formatedWarning("No editable SEDProcessorInstance!");
    }
  }

  private void updateEditableProcessorPropertyModel() {
    long l = LOG.logStart();
    SEDProcessorInstance spi = getEditable();
    mtpmPropertyModel.clear();
    if (spi == null
            || Utils.isEmptyString(spi.getPlugin())
            || Utils.isEmptyString(spi.getType())) {
      LOG.formatedWarning("Null plugin or type!");
      return;
    }

    InMailProcessorDef ctd = mPlgManager.getInMailProcessor(spi.getPlugin(),
            spi.
                    getType());
    if (ctd == null) {
      LOG.formatedWarning("Plugin '%s' and processor type '%s' not found!", spi.
              getPlugin(), spi.getType());
      return;
    }
    Map<String, String> tpv = new HashMap<>();
    spi.getSEDProcessorProperties().forEach((tp) -> {
      tpv.put(tp.getKey(), tp.getValue());
    });

    mtpmPropertyModel.setPluginProperties(tpv, ctd.
            getMailProcessorPropertyDeves());
    LOG.logEnd(l);
  }

  /**
   *
   * @return
   */
  public String getEditableProcessorPluginType() {
    SEDProcessorInstance t = getEditable();
    return t == null ? null : t.getPlugin();
  }

  /**
   * Set plugin for editable task
   *
   * @param strPlugin
   */
  public void setEditableProcessorPluginType(String strPlugin) {
    SEDProcessorInstance t = getEditable();
    if (t != null && !Objects.equals(strPlugin, t.getPlugin())) {
      t.setPlugin(strPlugin);
      // set type
      Plugin plg = mPlgManager.getPluginByType(strPlugin);
      setEditableProcessorType(
              plg != null && !plg.getInMailProcessorDeves().isEmpty()
              ? plg.getInMailProcessorDeves().get(0).getType() : null);

    }
  }
  public String getEditableProcessorType() {
    SEDProcessorInstance spi = getEditable();
    return spi == null ? null : spi.getType();
  }

  public void setPropertyDataToEditableProcessInstance() {
    SEDProcessorInstance spi = getEditable();
    spi.getSEDProcessorProperties().clear();
    for (PluginPropertyModelItem tmi : mtpmPropertyModel.getPluginProperties()) {
      SEDProcessorProperty stp = new SEDProcessorProperty();
      stp.setKey(tmi.getPropertyDef().getKey());
      stp.setValue(tmi.getValue());
      spi.getSEDProcessorProperties().add(stp);
    }
  }

  public AdminSEDInMailProcessView getAdmProcView() {
    return admProcView;
  }

  public void setAdmProcView(AdminSEDInMailProcessView admRuleView) {
    this.admProcView = admRuleView;
  }



  @Override
  public String getSelectedDesc() {
    SEDProcessorInstance sel =  getSelected();
    if (sel!=null) {
      return sel.getPlugin() + ":" + sel.getType();
    }
    return  null;
  }

  
  
}
