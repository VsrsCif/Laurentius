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

import java.math.BigInteger;
import java.util.Calendar;
import java.util.List;
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
import si.laurentius.plugin.crontask.CronTaskDef;

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
  private void construct() {
    List<SEDCronJob> lst = mdbLookups.getSEDCronJobs();
    for (SEDCronJob ecj : lst) {
      if (ecj.getActive() != null && ecj.getActive()) {
        ScheduleExpression se =
            new ScheduleExpression().second(ecj.getSecond()).minute(ecj.getMinute())
                .hour(ecj.getHour()).dayOfMonth(ecj.getDayOfMonth()).month(ecj.getMonth())
                .dayOfWeek(ecj.getDayOfWeek());
        TimerConfig checkTest = new TimerConfig(ecj.getId(), false);
        getServices().createCalendarTimer(se, checkTest);
      }
    }
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

    BigInteger bi = (BigInteger) (timer.getInfo());
    SEDTaskExecution te = new SEDTaskExecution();
    te.setCronId(bi);
    te.setStatus(SEDTaskStatus.INIT.getValue());
    te.setStartTimestamp(Calendar.getInstance().getTime());

    try {
      mdbDao.addExecutionTask(te);
    } catch (StorageException ex) {
      LOG.logEnd(l, "Error storing task: '" + te.getType() + "' ", ex);
      return;
    }
    

    // get cron job
    SEDCronJob mj = mdbLookups.getSEDCronJobById(bi);
    
    CronTaskDef ct = mpmPluginManager.getCronTaskDef(mj.getSEDTask().getPlugin(), mj.getSEDTask().getType());

    te.setName(ct.getName());
    te.setType(mj.getSEDTask().getType());
    if (!mj.getActive()) {
      te.setStatus(SEDTaskStatus.ERROR.getValue());
      te.setResult(String.format("Task cron id:  %d  not active!", bi));
      te.setEndTimestamp(Calendar.getInstance().getTime());
      try {
        mdbDao.updateExecutionTask(te);
      } catch (StorageException ex) {
        LOG.logEnd(l, "Error updating task: '" + te.getType() + "' ", ex);
      }
      return;
    }
    
    // plugin  and task type
    TaskExecutionInterface tproc = null;
    try {
      tproc = InitialContext.doLookup(ct.getJndi());
    } catch (NamingException ex) {
      te.setStatus(SEDTaskStatus.ERROR.getValue());
      te.setResult(String.format("Error getting taskexecutor: %s. ERROR: %s", ct.getJndi(),
              ex.getMessage()));
      te.setEndTimestamp(Calendar.getInstance().getTime());
      try {
        mdbDao.updateExecutionTask(te);
      } catch (StorageException ex2) {
        LOG.logEnd(l, "Error updating task: '" + te.getType() + "' ", ex2);
      }
      return;
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
      LOG.logEnd(l, "Error updating task: '" + te.getType() + "' ", ex2);
      return;
    }

    try {
      String result = tproc.executeTask(p);
      te.setStatus(SEDTaskStatus.SUCCESS.getValue());
      te.setResult(result);
      te.setEndTimestamp(Calendar.getInstance().getTime());

      try {
        mdbDao.updateExecutionTask(te);
      } catch (StorageException ex2) {
        LOG.logEnd(l, "Error updating task: '" + te.getType() + "' ", ex2);
        return;
      }
    } catch (TaskException ex) {

      te.setStatus(SEDTaskStatus.ERROR.getValue());
      te.setResult(String.format("TASK ERROR: %s. Err. desc: %s", ct.getJndi(), ex.getMessage()));
      te.setEndTimestamp(Calendar.getInstance().getTime());
      LOG.logEnd(l, "Error updating task: '" + te.getType() + "' ", ex);
      try {
        mdbDao.updateExecutionTask(te);
      } catch (StorageException ex2) {
        LOG.logEnd(l, "Error updating task: '" + te.getType() + "' ", ex2);
        return;
      }
    }
    LOG.logEnd(l);
  }

}
