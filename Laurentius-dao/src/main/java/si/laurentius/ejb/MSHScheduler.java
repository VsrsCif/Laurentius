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
package si.laurentius.ejb;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import si.laurentius.cron.SEDCronJob;
import si.laurentius.cron.SEDTaskExecution;
import si.laurentius.cron.SEDTaskProperty;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.enums.SEDTaskStatus;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.interfaces.SEDPluginManagerInterface;
import si.laurentius.commons.interfaces.SEDSchedulerInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.plugin.crontask.CronTaskDef;
import si.laurentius.plugin.def.Plugin;

import si.laurentius.plugin.interfaces.TaskExecutionInterface;
import si.laurentius.plugin.interfaces.exception.TaskException;

/**
 *
 * @author Jože Rihtaršič
 */
@Singleton
@Local(SEDSchedulerInterface.class)
@Lock(LockType.READ)
@Startup
public class MSHScheduler implements SEDSchedulerInterface {

  private static final SEDLogger LOG = new SEDLogger(MSHScheduler.class);
  @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
  private SEDDaoInterface mdbDao;

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  private SEDLookupsInterface mdbLookups;

  @EJB(mappedName = SEDJNDI.JNDI_PLUGIN)
  private SEDPluginManagerInterface mpmPluginManager;

  @Resource
  private TimerService timerService;
  
  @PostConstruct
  private void initCronJobs(){
    List<SEDCronJob> lst = mdbLookups.getSEDCronJobs();
    for (SEDCronJob cj: lst){
      if (cj.getActive()!=null && cj.getActive()){
        activateCronJob(cj);
      }
    }
  }
  

  @Override
  public boolean activateCronJob(SEDCronJob cb) {
    LOG.formatedDebug("Register timer to TimerService %d name %s", cb.getId(),
            cb.getName());
    ScheduleExpression se
            = new ScheduleExpression().second(cb.getSecond()).
                    minute(cb.getMinute())
                    .hour(cb.getHour()).dayOfMonth(cb.
                    getDayOfMonth()).month(cb.getMonth())
                    .dayOfWeek(cb.getDayOfWeek());
    TimerConfig checkTest = new TimerConfig(cb.getName(), false);
    getServices().createCalendarTimer(se, checkTest);
    return true;
  }

  @Override
  public boolean stopCronJob(SEDCronJob cb) {
    LOG.formatedDebug("Stop timer to TimerService %d name %s", cb.getId(),
            cb.getName());
    boolean bScu = false;
    for (Timer t : getServices().getAllTimers()) {
      if (t.getInfo() != null && t.getInfo().equals(cb.getName())) {
        t.cancel();
        bScu = true;
        break;
      }
    }
    return bScu;
  }

  /**
   *
   * @return
   */
  @Override
  public TimerService getServices() {
    return timerService;
  }

  /**
   *
   * @param timer
   */
  @Timeout
  @Override
  public void timeout(Timer timer) {
    long l = LOG.logStart();
    String name = (String) (timer.getInfo());
    // get cron job
    SEDCronJob mj = mdbLookups.getSEDCronJobByName(name);
    if (mj == null) {
      String logMsg = String.format(
              "Timeout for cron job %s , but cronjob not exists!!", name);
      LOG.logError(l, logMsg, null);
    } else if (!mj.getActive()) {
      LOG.formatedDebug("Cron job %s  not active!", name);
    } else {
      executeContJob(mj);
    }
    LOG.logEnd(l, String.format("Timeout for Cron job %s !'", name));
  }

  @Override
  public String executeContJob(SEDCronJob mj) {
    long l = LOG.logStart();
    String result = "";
    SEDTaskExecution te = new SEDTaskExecution();
    te.setCronId(mj.getId());
    te.setName(mj.getName());
    te.setType(mj.getSEDTask().getType());
    te.setPlugin(mj.getSEDTask().getPlugin());
    te.setPluginVersion(mj.getSEDTask().getPluginVersion());
    te.setStatus(SEDTaskStatus.INIT.getValue());
    te.setStartTimestamp(Calendar.getInstance().getTime());

    try {
      mdbDao.addExecutionTask(te);
    } catch (StorageException ex) {
      result = String.format(
              "Error occurred while executing task type %s!. Error: %s", te.
                      getType(), ex.getMessage());
      LOG.logEnd(l, result, ex);
      return result; 
    }

    Plugin plg = mpmPluginManager.getPluginByType(mj.getSEDTask().getPlugin());

    if (plg == null) {
      result =  String.format(
              "Not plugin %s!", mj.getSEDTask().
                      getPlugin(), mj.getSEDTask().
                      getType());
      te.setStatus(SEDTaskStatus.ERROR.getValue());
      te.setResult(result);
      te.setEndTimestamp(Calendar.getInstance().getTime());
      try {
        mdbDao.updateExecutionTask(te);
      } catch (StorageException ex) {
        LOG.logEnd(l, "Error updating task: '" + te.getType() + "' ", ex);
      }
      return result;
    } else if (!Objects.equals(plg.getVersion(), mj.getSEDTask().
            getPluginVersion())) {
      LOG.formatedWarning("Plugin version mismatch for cron task execution %s",
              mj.getName());
      te.setPluginVersion(plg.getVersion());
    }

    CronTaskDef ct = mpmPluginManager.
            getCronTaskDef(mj.getSEDTask().getPlugin(), mj.getSEDTask().
                    getType());

    if (ct == null) {
      result = String.format(
              "Not task processor for plugin %s and  task %s!", mj.getSEDTask().
                      getPlugin(), mj.getSEDTask().
                      getType());
      te.setStatus(SEDTaskStatus.ERROR.getValue());
      te.setResult(result);
      te.setEndTimestamp(Calendar.getInstance().getTime());
      try {
        mdbDao.updateExecutionTask(te);
      } catch (StorageException ex) {
        LOG.logEnd(l, "Error updating task: '" + te.getType() + "' ", ex);
      }
      return result;
    }

    // plugin  and task type
    TaskExecutionInterface tproc = null;
    try {
      tproc = InitialContext.doLookup(ct.getJndi());
    } catch (NamingException ex) {
      result = String.format("Error getting taskexecutor: %s. ERROR: %s",
              ct.getJndi(),
              ex.getMessage());
      te.setStatus(SEDTaskStatus.ERROR.getValue());
      te.setResult(result);
      te.setEndTimestamp(Calendar.getInstance().getTime());
      try {
        mdbDao.updateExecutionTask(te);
      } catch (StorageException ex2) {
        LOG.logEnd(l, "Error updating task: '" + te.getType() + "' ", ex2);
      }
      return result;
    }
    Properties p = new Properties();
    for (SEDTaskProperty tp : mj.getSEDTask().getSEDTaskProperties()) {
      if (tp.getValue() != null) {
        p.setProperty(tp.getKey(), tp.getValue());
      }
    }

    te.setStatus(SEDTaskStatus.PROGRESS.getValue());
    try {
      mdbDao.updateExecutionTask(te);
    } catch (StorageException ex2) {
       result = "Error updating task: '" + te.getType() + "' ";
      LOG.logEnd(l, result, ex2);
      return result;
    }

    try {
      result = tproc.executeTask(p);
      LOG.formatedDebug(result);
      
      te.setStatus(SEDTaskStatus.SUCCESS.getValue());
      te.setResult(result);
      te.setEndTimestamp(Calendar.getInstance().getTime());

      try {
        mdbDao.updateExecutionTask(te);
      } catch (StorageException ex2) {
        result = String.format(
              "Error occurred while updating task  type %s!. Error: %s. Task executed with result:", te.
                      getType(), ex2.getMessage(), result);
        LOG.logEnd(l, result, ex2);
        return result;
      }
    } catch (TaskException ex) {

      te.setStatus(SEDTaskStatus.ERROR.getValue());
      te.setResult(String.format("TASK ERROR: %s. Err. desc: %s", ct.getJndi(),
              ex.getMessage()));
      te.setEndTimestamp(Calendar.getInstance().getTime());
      LOG.logEnd(l, "Error updating task: '" + te.getType() + "' ", ex);
      try {
        mdbDao.updateExecutionTask(te);
      } catch (StorageException ex2) {
        result = String.format(
              "Error occurred while updating task  type %s!. Error: %s. Task executed with result:", te.
                      getType(), ex2.getMessage(), result);
        LOG.logEnd(l, result, ex2);
        return result;
      }
    }
    LOG.logEnd(l);
    return result;
  }
  

}