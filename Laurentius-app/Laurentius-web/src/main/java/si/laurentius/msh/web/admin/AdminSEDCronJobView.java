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
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.ScheduleExpression;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import si.laurentius.cron.SEDCronJob;
import si.laurentius.cron.SEDTask;
import si.laurentius.cron.SEDTaskProperty;
import si.laurentius.cron.SEDTaskType;
import si.laurentius.cron.SEDTaskTypeProperty;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.interfaces.SEDSchedulerInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.msh.web.abst.AbstractAdminJSFView;

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


  /*
   * public SEDTaskProperty getEditableProperty(String name) { if (getEditable() != null &&
   * getEditable().getSEDTask() != null) { for (SEDTaskProperty mp :
   * getEditable().getSEDTask().getSEDTaskProperties()) { if (mp.getKey().equalsIgnoreCase(name)) {
   * return mp; } } } return null; }
   * 
   * public void setEditableProperty(String name, String val) { SEDTaskProperty pp = null; if
   * (getEditable() != null) { return; } if (getEditable().getSEDTask() != null) { for
   * (SEDTaskProperty mp : getEditable().getSEDTask().getSEDTaskProperties()) { if
   * (mp.getKey().equalsIgnoreCase(name)) { pp = mp; break; } } } else {
   * getEditable().setSEDTask(new SEDTask()); } if (pp == null) { pp = new SEDTaskProperty();
   * pp.setKey(val); getEditable().getSEDTask().getSEDTaskProperties().add(pp); } pp.setValue(val);
   * 
   * }
   */
  /**
   *
   * @param id
   * @return
   */
  public SEDCronJob getMSHCronJobByName(BigInteger id) {
    return mdbLookups.getSEDCronJobById(id);
  }

  /**
     *
     */
  @Override
  public void createEditable() {
    SEDCronJob ecj = new SEDCronJob();
    ecj.setActive(true);
    ecj.setSecond("*/20");
    ecj.setMinute("*");
    ecj.setHour("*");
    ecj.setDayOfMonth("*");
    ecj.setMonth("*");
    ecj.setDayOfWeek("*");

    SEDTask tsk = new SEDTask();
    List<SEDTaskType> stkst = mdbLookups.getSEDTaskTypes();
    String firstTask = null;
    if (stkst.size() > 0) {
      firstTask = stkst.get(0).getType();
    }

    ecj.setSEDTask(tsk);

    setNew(ecj);
    setEditableTask(firstTask);

  }

  /**
     *
     */
  @Override
  public void removeSelected() {
    if (getSelected() != null) {
      mdbLookups.removeSEDCronJob(getSelected());
      setSelected(null);

    }
  }

  /**
     *
     */
  @Override
  public void persistEditable() {
    SEDCronJob ecj = getEditable();
    if (ecj != null) {
      mdbLookups.addSEDCronJob(ecj);
      if (ecj.getActive() != null && ecj.getActive()) {
        LOG.log("Register timer to TimerService");
        ScheduleExpression se =
            new ScheduleExpression().second(ecj.getSecond()).minute(ecj.getMinute())
                .hour(ecj.getHour()).dayOfMonth(ecj.getDayOfMonth()).month(ecj.getMonth())
                .dayOfWeek(ecj.getDayOfWeek());
        TimerConfig checkTest = new TimerConfig(ecj.getId(), false);
        mshScheduler.getServices().createCalendarTimer(se, checkTest);

      }
    }

  }

  /**
     *
     */
  @Override
  public void updateEditable() {
    SEDCronJob ecj = getEditable();
    if (ecj != null) {
      mdbLookups.updateSEDCronJob(ecj);
      for (Timer t : mshScheduler.getServices().getAllTimers()) {
        if (t.getInfo().equals(ecj.getId())) {
          t.cancel();
          break;
        }
      }
      if (ecj.getActive() != null && ecj.getActive()) {
        LOG.log("Register timer to TimerService");
        ScheduleExpression se =
            new ScheduleExpression().second(ecj.getSecond()).minute(ecj.getMinute())
                .hour(ecj.getHour()).dayOfMonth(ecj.getDayOfMonth()).month(ecj.getMonth())
                .dayOfWeek(ecj.getDayOfWeek());
        TimerConfig checkTest = new TimerConfig(ecj.getId(), false);
        mshScheduler.getServices().createCalendarTimer(se, checkTest);
      }

    }
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

  /**
   *
   * @return
   */
  public List<String> getTaskTypeList() {
    long l = LOG.logStart();
    List<String> rstLst = new ArrayList<>();
    List<SEDTaskType> lst = mdbLookups.getSEDTaskTypes();
    lst.stream().forEach((tsk) -> {
      rstLst.add(tsk.getType());
    });
    LOG.logEnd(l, lst.size());
    return rstLst;
  }

  /**
   *
   * @param task
   */
  public void setEditableTask(String task) {

    SEDCronJob scj = getEditable();
    if (scj != null) {
      if (scj.getSEDTask() == null || scj.getSEDTask().getTaskType() == null
          || !scj.getSEDTask().getTaskType().equals(task)) {
        SEDTaskType sdt = mdbLookups.getSEDTaskTypeByType(task);
        if (sdt != null) {
          SEDTask tsk = new SEDTask();
          tsk.setTaskType(sdt.getType());
          for (SEDTaskTypeProperty p : sdt.getSEDTaskTypeProperties()) {
            SEDTaskProperty tp = new SEDTaskProperty();
            tp.setKey(p.getKey());
            tsk.getSEDTaskProperties().add(tp);
          }
          scj.setSEDTask(tsk);
        }
      }
    }
  }

  /**
   *
   * @return
   */
  public String getEditableTask() {
    if (getEditable() != null && getEditable().getSEDTask() != null) {
      return getEditable().getSEDTask().getTaskType();
    }
    return null;
  }

  /**
   *
   * @param key
   * @return
   */
  public String getTypeForEditableTaskProperty(String key) {
    String strType = "string";
    String task = getEditableTask();
    if (task != null && key != null) {
      SEDTaskType st = mdbLookups.getSEDTaskTypeByType(task);
      for (SEDTaskTypeProperty tp : st.getSEDTaskTypeProperties()) {
        if (tp.getKey().equals(key)) {
          strType = tp.getType();
          break;
        }
      }
    }
    return strType;
  }

  /**
   *
   * @param key
   * @param bVal
   */
  public void setBooleanValueForEditableTaskProperty(String key, boolean bVal) {

    if (getEditable() != null) {
      return;
    }
    if (getEditable().getSEDTask() != null) {
      for (SEDTaskProperty mp : getEditable().getSEDTask().getSEDTaskProperties()) {
        if (mp.getKey().equalsIgnoreCase(key)) {
          mp.setValue(bVal ? "true" : "false");
          break;
        }
      }
    }

  }

  /**
   *
   * @param key
   * @return
   */
  public boolean getBooleanValueForEditableTaskProperty(String key) {
    if (getEditable() != null) {
      return false;
    }
    if (getEditable().getSEDTask() != null) {
      for (SEDTaskProperty mp : getEditable().getSEDTask().getSEDTaskProperties()) {
        if (mp.getKey().equalsIgnoreCase(key)) {
          return mp.getValue() != null && mp.getValue().trim().equalsIgnoreCase("true");
        }
      }
    }
    return false;
  }
}
