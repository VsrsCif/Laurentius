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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.ejb.EJB;
import javax.ejb.Timer;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import si.laurentius.cron.SEDCronJob;
import si.laurentius.cron.SEDTask;
import si.laurentius.cron.SEDTaskProperty;

import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.interfaces.SEDPluginManagerInterface;
import si.laurentius.commons.interfaces.SEDSchedulerInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
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
@ManagedBean(name = "adminSEDCronJobView")
public class AdminSEDCronJobView extends AbstractAdminJSFView<SEDCronJob> {

  private static final SEDLogger LOG = new SEDLogger(AdminSEDCronJobView.class);

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  private SEDLookupsInterface mdbLookups;

  @EJB(mappedName = SEDJNDI.JNDI_SEDSCHEDLER)
  private SEDSchedulerInterface mshScheduler;

  @EJB(mappedName = SEDJNDI.JNDI_PLUGIN)
  private SEDPluginManagerInterface mPlgManager;

  PluginPropertyModel mtpmPropertyModel = new PluginPropertyModel();

  /**
   *
   * @param id
   * @return
   */
  public SEDCronJob getMSHCronJobByName(BigInteger id) {
    return mdbLookups.getSEDCronJobById(id);
  }

  @Override
  public boolean validateData() {
    SEDCronJob cj = getEditable();
    if (Utils.isEmptyString(cj.getName())) {
      addError("Name must not be null ");
      return false;
    }
    if (isEditableNew() && mdbLookups.getSEDCronJobByName(cj.getName()) != null) {
      addError("Name: '" + cj.getName() + "' already exists!");
      return false;
    }

    for (PluginPropertyModelItem tmi : mtpmPropertyModel.getPluginProperties()) {
      if (tmi.getPropertyDef().getMandatory() && Utils.isEmptyString(tmi.
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

    String sbname = "task_%03d";
    int i = 1;

    while (mdbLookups.getSEDCronJobByName(String.format(sbname, i)) != null) {
      i++;
    }

    SEDCronJob ecj = new SEDCronJob();
    ecj.setName(String.format(sbname, i));
    ecj.setActive(true);
    ecj.setSecond("0");
    ecj.setMinute("*/5");
    ecj.setHour("*");
    ecj.setDayOfMonth("*");
    ecj.setMonth("*");
    ecj.setDayOfWeek("*");

    SEDTask tsk = new SEDTask();
    ecj.setSEDTask(tsk);
    // set  first cront task;
    List<Plugin> lstPlg = mPlgManager.getRegistredPlugins();
    for (Plugin p : lstPlg) {
      if (!p.getCronTaskDeves().isEmpty()) {
        CronTaskDef taskDef = p.getCronTaskDeves().get(0);
        tsk.setPlugin(p.getType());
        tsk.setPluginVersion(p.getVersion());
        tsk.setType(taskDef.getType());

        Map<String, String> tpv = new HashMap<>();
        tsk.getSEDTaskProperties().forEach((tp) -> {
          tpv.put(tp.getKey(), tp.getValue());
        });
        mtpmPropertyModel.setPluginProperties(tpv, taskDef.
                getCronTaskPropertyDeves());
        break;
      }
    }
    setNew(ecj);

  }

  @Override
  public void setEditable(SEDCronJob edtbl) {
    super.setEditable(edtbl);
    mtpmPropertyModel.clear();
    SEDTask t = getEditableSEDTask();
    if (t != null) {
      updateTaskPropertyModel(t);
    }

  }

  /**
   *
   */
  @Override
  public boolean removeSelected() {
    boolean bSuc = false;
    SEDCronJob cj = getSelected();
    if (cj != null) {
      mshScheduler.stopCronJob(cj);
      bSuc = mdbLookups.removeSEDCronJob(cj);
      setSelected(null);
    }
    return bSuc;
  }

  /**
   *
   */
  @Override
  public boolean persistEditable() {
    boolean bsuc = false;
    SEDCronJob ecj = getEditable();
    if (ecj != null) {
      setPropertyDataToTaskInstance(ecj.getSEDTask());

      mdbLookups.addSEDCronJob(ecj);
      if (ecj.getActive() != null && ecj.getActive()) {
        mshScheduler.activateCronJob(ecj);
      }
      bsuc = true;
    }
    return bsuc;

  }

  /**
   *
   */
  @Override
  public boolean updateEditable() {
    SEDCronJob ecj = getEditable();
    boolean bsuc = false;
    if (ecj != null) {
      setPropertyDataToTaskInstance(ecj.getSEDTask());
      bsuc = mdbLookups.updateSEDCronJob(ecj);
      if (bsuc) {
        mshScheduler.stopCronJob(ecj);
        if (ecj.getActive() != null && ecj.getActive()) {
          mshScheduler.activateCronJob(ecj);
        }
      }
    }
    return bsuc;
  }

  /**
   *
   * @return
   */
  @Override
  public List<SEDCronJob> getList() {
    long l = LOG.logStart();
    List<SEDCronJob> lst = mdbLookups.getSEDCronJobs();
    LOG.logEnd(l, lst != null ? lst.size() : "null");
    return lst;
  }

  public String getEditableTaskPluginType() {
    SEDTask t = getEditableSEDTask();
    return t == null ? null : t.getPlugin();
  }

  /**
   * Set plugin for editable task
   *
   * @param strPlugin
   */
  public void setEditableTaskPluginType(String strPlugin) {
    SEDTask t = getEditableSEDTask();
    if (t != null && !Objects.equals(strPlugin, t.getPlugin())) {
      t.setPlugin(strPlugin);
      // set type
      Plugin plg = mPlgManager.getPluginByType(strPlugin);
      setEditableTaskType(
              plg != null && !plg.getCronTaskDeves().isEmpty()
              ? plg.getCronTaskDeves().get(0).getType() : null);

    }
  }

  public List<CronTaskDef> getEditablePluginCronTaskDeves() {

    SEDTask t = getEditableSEDTask();
    Plugin plg = null;
    if (t != null && !Utils.
            isEmptyString(t.getPlugin())) {
      plg = mPlgManager.getPluginByType(t.getPlugin());
    }
    return plg != null ? plg.getCronTaskDeves() : Collections.emptyList();
  }

  /**
   * Set Task type from selected plugin
   *
   * @param strTaskType
   */
  public void setEditableTaskType(String strTaskType) {
    SEDTask t = getEditableSEDTask();
    if (t != null && !Objects.equals(t.getType(), strTaskType)) {
      t.setType(strTaskType);
      updateTaskPropertyModel(t);

    }
  }

  public String getEditableTaskType() {
    SEDTask t = getEditableSEDTask();
    return t == null ? null : t.getType();
  }

  private void updateTaskPropertyModel(SEDTask t) {
    mtpmPropertyModel.clear();
    if (t == null
            || Utils.isEmptyString(t.getPlugin())
            || Utils.isEmptyString(t.getType())) {
      LOG.formatedWarning("Null plugin or type!");
      return;
    }

    CronTaskDef ctd = mPlgManager.getCronTaskDef(t.getPlugin(), t.getType());
    if (ctd == null) {
      LOG.formatedWarning("Plugin '%s' and cron type '%s' not found!", t.
              getPlugin(), t.getType());
      return;
    }

    Map<String, String> tpv = new HashMap<>();
    t.getSEDTaskProperties().forEach((tp) -> {
      tpv.put(tp.getKey(), tp.getValue());
    });
    mtpmPropertyModel.setPluginProperties(tpv, ctd.
            getCronTaskPropertyDeves());

  }

  public SEDTask getEditableSEDTask() {
    if (getEditable() != null) {
      if (getEditable().getSEDTask() == null) {
        getEditable().setSEDTask(new SEDTask());

      }
      return getEditable().getSEDTask();
    }
    return null;
  }

  public List<PluginPropertyModelItem> getTaskItems() {
    return mtpmPropertyModel.getPluginProperties();
  }

  public void setPropertyDataToTaskInstance(SEDTask inst) {
    inst.getSEDTaskProperties().clear();
    for (PluginPropertyModelItem tmi : mtpmPropertyModel.getPluginProperties()) {
      SEDTaskProperty stp = new SEDTaskProperty();
      stp.setKey(tmi.getPropertyDef().getKey());
      stp.setValue(tmi.getValue());
      inst.getSEDTaskProperties().add(stp);
    }
  }

  public Collection<Timer> getRegisredTimers() {
    return mshScheduler.getServices().getAllTimers();
  }


  @Override
  public String getSelectedDesc() {
    SEDCronJob sel = getSelected();
    if (sel != null) {
      return sel.getName();
    }
    return null;
  }
}
