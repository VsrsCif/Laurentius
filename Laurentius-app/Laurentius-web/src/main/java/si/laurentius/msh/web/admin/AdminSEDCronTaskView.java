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
import si.laurentius.cron.SEDCronJob;
import si.laurentius.cron.SEDTask;
import si.laurentius.cron.SEDTaskProperty;
import si.laurentius.msh.web.abst.AbstractAdminJSFView;
import si.laurentius.msh.web.plugin.PluginPropertyModel;
import si.laurentius.msh.web.plugin.PluginPropertyModelItem;
import si.laurentius.plugin.crontask.CronTaskDef;
import si.laurentius.plugin.def.Plugin;

/**
 *
 * @author Jože Rihtaršič
 */
@SessionScoped
@Named("adminSEDCronTaskView")
public class AdminSEDCronTaskView extends AbstractAdminJSFView<SEDTask> {

  private static final SEDLogger LOG = new SEDLogger(
          AdminSEDCronTaskView.class);

  @EJB(mappedName = SEDJNDI.JNDI_PLUGIN)
  private SEDPluginManagerInterface mPlgManager;

  @Inject
  private AdminSEDCronJobView admCronJobView;

  public AdminSEDCronJobView getAdmCronJobView() {
    return admCronJobView;
  }

  public void setAdmCronJobView(AdminSEDCronJobView admCronJobView) {
    this.admCronJobView = admCronJobView;
  }

  PluginPropertyModel mtpmPropertyModel = new PluginPropertyModel();

  @Override
  public boolean validateData() {
    SEDTask cj = getEditable();
    if (Utils.isEmptyString(cj.getPlugin())) {
      addError("Select plugin!");
      return false;
    }
    if (Utils.isEmptyString(cj.getType())) {
      addError("Select task type!");
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
    updateEditablePropertyModel();
  }

  /**
   *
   */
  @Override
  public void createEditable() {
    SEDCronJob cj = admCronJobView.getEditable();
    if (cj != null) {
      SEDTask spi = new SEDTask();
      for (Plugin plg : mPlgManager.getRegistredPlugins()) {
        if (!plg.getCronTaskDeves().isEmpty()) {
          spi.setPlugin(plg.getType());
          spi.setPluginVersion(plg.getVersion());
          spi.setType(plg.getCronTaskDeves().get(0).getType());

          break;
        }
      }
      setNew(spi);
    }
    updateEditablePropertyModel();
  }

  /**
   *
   */
  @Override
  public boolean removeSelected() {
    boolean bSuc = false;
    SEDTask ecj = getSelected();
    if (ecj != null) {
      bSuc = admCronJobView.removeTaskFromEditable(ecj);
      setSelected(null);

    } else {
      addError("Select task!");
    }
    return bSuc;
  }

  /**
   *
   */
  @Override
  public boolean persistEditable() {
    boolean bsuc = false;

    SEDTask ecj = getEditable();
    if (ecj != null) {
      setPropertyDataToEditableProcessInstance();
      bsuc = admCronJobView.addTaskToEditable(ecj);
    } else {
      addError("No editable task!");
    }
    return bsuc;
  }

  /**
   *
   */
  @Override
  public boolean updateEditable() {
    SEDTask ecj = getEditable();
    boolean bsuc = false;
    if (ecj != null) {
      setPropertyDataToEditableProcessInstance();
      bsuc = admCronJobView.updateTaskFromEditable(getSelected(), ecj);

    }
    return bsuc;
  }

  /**
   *
   * @return
   */
  @Override
  public List<SEDTask> getList() {
    long l = LOG.logStart();
    List<SEDTask> lst = admCronJobView.getEditable() != null ? admCronJobView.
            getEditable().getSEDTasks() : null;
    LOG.logEnd(l, lst != null ? lst.size() : "null");
    return lst;
  }

  /**
   *
   * @return
   */
  public List<PluginPropertyModelItem> getSelectedTaskItems() {
    return mtpmPropertyModel.getPluginProperties();
  }

  public void selectedTaskToTop() {
    SEDTask spi = getSelected();
    SEDCronJob epr = admCronJobView.getEditable();
    if (epr != null && getSelected() != null) {
      int idx = epr.getSEDTasks().indexOf(spi);
      if (idx > 0) {
        epr.getSEDTasks().remove(spi);
        epr.getSEDTasks().add(0, spi);
      }

    } else {
      addError("Select task!");
    }
  }

  public void selectedTaskUp() {
    SEDTask spi = getSelected();
    SEDCronJob epr = admCronJobView.getEditable();
    if (epr != null && spi != null) {
      int idx = epr.getSEDTasks().indexOf(spi);
      if (idx > 0) {
        epr.getSEDTasks().remove(spi);
        epr.getSEDTasks().add(--idx, spi);
      }

    } else {
      addError("Select task!");
    }
  }

  public void selectedTaskDown() {
    SEDTask spi = getSelected();
    SEDCronJob epr = admCronJobView.getEditable();
    if (epr != null && spi != null) {
      int idx = epr.getSEDTasks().indexOf(spi);
      if (idx < epr.getSEDTasks().size() - 1) {
        epr.getSEDTasks().remove(spi);
        epr.getSEDTasks().add(++idx, spi);
      }

    } else {
      addError("Select task!");
    }
  }

  public void selectedTaskToBottom() {
    SEDTask spi = getSelected();
    SEDCronJob epr = admCronJobView.getEditable();
    if (epr != null && spi != null) {
      int idx = epr.getSEDTasks().indexOf(spi);
      if (idx < epr.getSEDTasks().size() - 1) {
        epr.getSEDTasks().remove(spi);
        epr.getSEDTasks().add(spi);
      }

    } else {
      addError("Select task!");
    }
  }

  public List<CronTaskDef> selectedCronTaskList() {
    SEDTask spi = getEditable();

    if (spi != null && !Utils.isEmptyString(spi.getPlugin())) {
      Plugin plg = mPlgManager.getPluginByType(spi.getPlugin());
      if (plg != null) {
        return plg.getCronTaskDeves();
      }
    }
    return Collections.emptyList();
  }

  public void setEditableTaskType(String strType) {
    SEDTask spi = getEditable();
    if (spi != null && !Objects.equals(spi.getType(), strType)) {
      spi.setType(strType);
      updateEditablePropertyModel();
    } else {
      LOG.formatedWarning("No editable task!");
    }
  }

  private void updateEditablePropertyModel() {
    long l = LOG.logStart();
    SEDTask spi = getEditable();
    mtpmPropertyModel.clear();
    if (spi == null
            || Utils.isEmptyString(spi.getPlugin())
            || Utils.isEmptyString(spi.getType())) {
      LOG.formatedWarning("Null plugin or type!");
      return;
    }

    CronTaskDef ctd = mPlgManager.getCronTaskDef(spi.getPlugin(),
            spi.getType());
    if (ctd == null) {
      LOG.formatedWarning("Plugin '%s' and task type '%s' not found!", spi.
              getPlugin(), spi.getType());
      return;
    }
    Map<String, String> tpv = new HashMap<>();
    spi.getSEDTaskProperties().forEach((tp) -> {
      tpv.put(tp.getKey(), tp.getValue());
    });

    mtpmPropertyModel.setPluginProperties(tpv, ctd.
            getCronTaskPropertyDeves());
    LOG.logEnd(l);
  }

  /**
   *
   * @return
   */
  public String getEditablePluginType() {
    SEDTask t = getEditable();
    return t == null ? null : t.getPlugin();
  }

  /**
   * Set plugin for editable task
   *
   * @param strPlugin
   */
  public void setEditablePluginType(String strPlugin) {
    SEDTask t = getEditable();
    if (t != null && !Objects.equals(strPlugin, t.getPlugin())) {
      t.setPlugin(strPlugin);
      // set type
      Plugin plg = mPlgManager.getPluginByType(strPlugin);
      setEditableTaskType(
              plg != null && !plg.getCronTaskDeves().isEmpty()
              ? plg.getCronTaskDeves().get(0).getType() : null);

    }
  }

  public String getEditableTaskType() {
    SEDTask spi = getEditable();
    return spi == null ? null : spi.getType();
  }

  public void setPropertyDataToEditableProcessInstance() {
    SEDTask spi = getEditable();
    spi.getSEDTaskProperties().clear();
    for (PluginPropertyModelItem tmi : mtpmPropertyModel.getPluginProperties()) {
      SEDTaskProperty stp = new SEDTaskProperty();
      stp.setKey(tmi.getPropertyDef().getKey());
      stp.setValue(tmi.getValue());
      spi.getSEDTaskProperties().add(stp);
    }
  }



  @Override
  public String getSelectedDesc() {
    SEDTask sel = getSelected();
    if (sel != null) {
      return sel.getName() + "(" + sel.getPlugin() + ":" + sel.getType() + ")";
    }
    return null;
  }

}
