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
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Timer;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.primefaces.context.RequestContext;
import si.laurentius.cron.SEDCronJob;
import si.laurentius.cron.SEDTask;

import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.interfaces.SEDPluginManagerInterface;
import si.laurentius.commons.interfaces.SEDSchedulerInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.web.abst.AbstractAdminJSFView;
import si.laurentius.msh.web.gui.dlg.DialogExecute;

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
     if (cj==null){
       return false;
     }
    if (Utils.isEmptyString(cj.getName())) {
      addError("Name must not be null ");
      return false;
    }
    if (isEditableNew() && mdbLookups.getSEDCronJobByName(cj.getName()) != null) {
      addError("Name: '" + cj.getName() + "' already exists!");
      return false;
    }

/*    for (PluginPropertyModelItem tmi : mtpmPropertyModel.getPluginProperties()) {
      if (tmi.getPropertyDef().getMandatory() && Utils.isEmptyString(tmi.
              getValue())) {
        addError(
                "Property value: '" + tmi.getPropertyDef().getKey() + "' is required!");
        return false;
      }
    }*/

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

    
    /*
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
    }*/
    setNew(ecj);

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
  
  public DialogExecute getDlgExecute() {
    return (DialogExecute)getBean("executeDialog");
  }
  
  public void executeSelectedWithWarning(String updateTarget) {
    DialogExecute  dlg = getDlgExecute();
    dlg.setCurrentJSFView(this, updateTarget);
    RequestContext context = RequestContext.getCurrentInstance();
    context.execute("PF('executeDialog').show();");
    context.update("dlgexecute:executeDialog");
  };
  
   public String executeSelected() {
 
    SEDCronJob cj = getSelected();
    if (cj != null) {
     return mshScheduler.executeContJob(cj);
    }
    return null;
  }
  
  

  /**
   *
   */
  @Override
  public boolean persistEditable() {
    boolean bsuc = false;
    SEDCronJob ecj = getEditable();
    if (ecj != null) {
      mdbLookups.addSEDCronJob(ecj);
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

  
  
  public boolean addTaskToEditable(SEDTask spi) {
    boolean bsuc = false;
    SEDCronJob pr = getEditable();
    if (pr != null) {
      bsuc = pr.getSEDTasks().add(spi);
    } else {
      addError("No editable task!");
    }
    return bsuc;
  }

  public boolean removeTaskFromEditable(SEDTask spi) {
    boolean bsuc = false;
    SEDCronJob pr = getEditable();
    if (pr != null) {
      bsuc = pr.getSEDTasks().remove(spi);
    } else {
      addError("No editable task!");
    }
    return bsuc;
  }

  public boolean updateTaskFromEditable(SEDTask spiOld,
          SEDTask spiNew) {
    boolean bsuc = false;
    SEDCronJob pr = getEditable();
    if (pr != null) {
      int i = pr.getSEDTasks().indexOf(spiOld);
      pr.getSEDTasks().remove(i);
      pr.getSEDTasks().add(i, spiNew);
      bsuc = true;

    } else {
      addError("No editable process rule!");
    }
    return bsuc;
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
