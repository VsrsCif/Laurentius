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
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import si.laurentius.cron.SEDCronJob;
import si.laurentius.ebox.SEDBox;
import si.laurentius.user.SEDUser;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.process.SEDProcessorRule;
import si.laurentius.process.SEDProcessorSet;

/**
 * @author Jože Rihtaršič
 */
@Startup
@Singleton
@Local(SEDLookupsInterface.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class SEDLookups implements SEDLookupsInterface {

  /**
   *
   */
  protected static final SEDLogger LOG = new SEDLogger(SEDLookups.class);

  /**
   * lookup update time .
   */
  public static final long S_UPDATE_TIMEOUT = 10 * 60 * 1000; // 10 minutes

  /**
   *
   */
  @PersistenceContext(unitName = "ebMS_LAU_PU", name = "ebMS_LAU_PU")
  public EntityManager memEManager;
  private final HashMap<Class, List<?>> mlstCacheLookup = new HashMap<>();

  private final HashMap<Class, Long> mlstTimeOut = new HashMap<>();

  /**
   *
   */
  @Resource
  public UserTransaction mutUTransaction;

  /**
   *
   * @param <T>
   * @param o
   * @return
   */
  public <T> boolean add(T o) {
    long l = LOG.logStart();
    boolean suc = false;
    try {
      mutUTransaction.begin();
      memEManager.persist(o);
      mutUTransaction.commit();
      mlstTimeOut.remove(o.getClass()); // remove timeout to refresh lookup at next call
      suc = true;
    } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException
            | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
      try {
        LOG.logError(l, ex.getMessage(), ex);
        mutUTransaction.rollback();
      } catch (IllegalStateException | SecurityException | SystemException ex1) {
        LOG.logWarn(l, "Rollback failed", ex1);
      }
    }
    return suc;
  }

  /**
   *
   * @param sb
   * @return
   */
  @Override
  public boolean addSEDBox(SEDBox sb) {
    return add(sb);
  }

  /**
   *
   * @param sb
   * @return
   */
  @Override
  public boolean addSEDCronJob(SEDCronJob sb) {
    return add(sb);
  }
  @Override
  public boolean addSEDProcessorRule(SEDProcessorRule sb) {
    return add(sb);
  }

  @Override
  public boolean addSEDProcessorSet(SEDProcessorSet sb) {
    return add(sb);
  }
  /**
   *
   * @param sb
   * @return
   */
  @Override
  public boolean addSEDUser(SEDUser sb) {
    return add(sb);
  }

  private <T> void cacheLookup(List<T> lst, Class<T> c) {
    if (mlstCacheLookup.containsKey(c)) {
      mlstCacheLookup.get(c).clear();
      mlstCacheLookup.replace(c, lst);
    } else {
      mlstCacheLookup.put(c, lst);
    }

    if (mlstTimeOut.containsKey(c)) {
      mlstTimeOut.replace(c, Calendar.getInstance().getTimeInMillis());
    } else {
      mlstTimeOut.put(c, Calendar.getInstance().getTimeInMillis());
    }
  }

  private <T> List<T> getFromCache(Class<T> c) {
    return mlstCacheLookup.containsKey(c) ? (List<T>) mlstCacheLookup.get(c) : null;
  }

  private <T> List<T> getLookup(Class<T> c) {
    List<T> t;
    if (updateLookup(c)) {
      TypedQuery<T> query = memEManager.createNamedQuery(
              c.getName() + ".getAll", c);
      t = query.getResultList();
      cacheLookup(t, c);
    } else {
      t = getFromCache(c);
    }
    return t;
  }


  /**
   *
   * @param strname
   * @return
   */
  @Override
  public SEDBox getSEDBoxByAddressName(String strname) {
    if (strname != null && !strname.trim().isEmpty()) {
      String sedBox = strname.trim();
      String domain = SEDSystemProperties.getLocalDomain();
      if (Utils.isEmptyString(domain)) {
        String msg
                = "Missing domain parameter in configuration. Did you init application with domain parameter?";
        LOG.logError(msg, null);
        throw new RuntimeException(msg);
      }
      domain = "@" + domain;
      
      if (!sedBox.toLowerCase().endsWith(domain.toLowerCase())) {
        LOG.formatedWarning(
                "Local sedbox %s has wrong domain. Local domain is %s",
                sedBox, domain);
        return null;
      }
      List<SEDBox> lst = getSEDBoxes();
      for (SEDBox sb : lst) {
        if (strname.equalsIgnoreCase(sb.getLocalBoxName() + domain)) {
          return sb;
        }
      }
    }
    return null;
  }
  @Override
  public SEDBox getSEDBoxByLocalName(String strname) {
    if (strname != null && !strname.trim().isEmpty()) {
      String localName = strname.trim();
      
      List<SEDBox> lst = getSEDBoxes();
      for (SEDBox sb : lst) {
        if (localName.equalsIgnoreCase(sb.getLocalBoxName())) {
          return sb;
        }
      }
    }
    return null;
  }

  /**
   *
   * @return
   */
  @Override
  public List<SEDBox> getSEDBoxes() {
    return getLookup(SEDBox.class);
  }

  /**
   *
   * @param id
   * @return
   */
  @Override
  public SEDCronJob getSEDCronJobById(BigInteger id) {
    if (id != null) {

      List<SEDCronJob> lst = getSEDCronJobs();
      for (SEDCronJob sb : lst) {
        if (id.equals(sb.getId())) {
          return sb;
        }
      }
    }
    return null;
  }

  @Override
  public SEDCronJob getSEDCronJobByName(String name) {
    if (name != null) {

      List<SEDCronJob> lst = getSEDCronJobs();
      for (SEDCronJob sb : lst) {
        if (Objects.equals(name, sb.getName())) {
          return sb;
        }
      }
    }
    return null;
  }

  /**
   *
   * @return
   */
  @Override
  public List<SEDCronJob> getSEDCronJobs() {
    return getLookup(SEDCronJob.class);
  }
  @Override
  public SEDProcessorRule getSEDProcessorRule(BigInteger id) {
    if (id != null) {
      List<SEDProcessorRule> lst = getSEDProcessorRules();
      for (SEDProcessorRule sb : lst) {
        if (id.equals(sb.getId())) {
          return sb;
        }
      }
    }
    return null;
  }
  @Override
  public List<SEDProcessorRule> getSEDProcessorRules() {
    return getLookup(SEDProcessorRule.class);
  }

  @Override
  public SEDProcessorSet getSEDProcessorSet(String code) {
    if (code != null) {
      List<SEDProcessorSet> lst = getSEDProcessorSets();
      for (SEDProcessorSet sb : lst) {
        if (Objects.equals(code, sb.getCode())) {
          return sb;
        }
      }
    }
    return null;
  }


  @Override
  public List<SEDProcessorSet> getSEDProcessorSets() {
    return getLookup(SEDProcessorSet.class);
  }
  /**
   *
   * @param userId
   * @return
   */
  @Override
  public SEDUser getSEDUserByUserId(String userId) {
    if (userId != null && !userId.trim().isEmpty()) {
      String ui = userId.trim();
      List<SEDUser> lst = getSEDUsers();
      for (SEDUser sb : lst) {
        if (sb.getUserId().equalsIgnoreCase(ui)) {
          return sb;
        }
      }
    }
    return null;
  }
  /**
   *
   * @return
   */
  @Override
  public List<SEDUser> getSEDUsers() {
    return getLookup(SEDUser.class);
  }

  /**
   *
   * @param <T>
   * @param o
   * @return
   */
  public <T> boolean remove(T o) {
    long l = LOG.logStart();
    boolean suc = false;
    try {
      mutUTransaction.begin();
      memEManager.remove(memEManager.contains(o) ? o : memEManager.
              merge(o));
      mutUTransaction.commit();
      mlstTimeOut.remove(o.getClass()); // remove timeout to refresh lookup at next call
      suc = true;
    } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException
            | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
      try {
        LOG.logError(l, ex.getMessage(), ex);
        mutUTransaction.rollback();
      } catch (IllegalStateException | SecurityException | SystemException ex1) {
        LOG.logWarn(l, "Rollback failed", ex1);
      }
    }
    return suc;
  }

  /**
   *
   * @param sb
   * @return
   */
  @Override
  public boolean removeSEDBox(SEDBox sb) {
   
    
    return remove(sb);
  }

  /**
   *
   * @param sb
   * @return
   */
  @Override
  public boolean removeSEDCronJob(SEDCronJob sb) {
    return remove(sb);
  }
  @Override
  public boolean removeSEDProcessorRule(SEDProcessorRule sb) {
    return remove(sb);
  }

  @Override
  public boolean removeSEDProcessorSet(SEDProcessorSet sb) {
    return remove(sb);
  }
  /**
   *
   * @param sb
   * @return
   */
  @Override
  public boolean removeSEDUser(SEDUser sb) {
    return remove(sb);
  }

  /**
   *
   * @param <T>
   * @param o
   * @return
   */
  public <T> boolean update(T o) {
    long l = LOG.logStart();
    boolean suc = false;
    try {
      mutUTransaction.begin();
      memEManager.merge(o);
      mutUTransaction.commit();
      mlstTimeOut.remove(o.getClass()); // remove timeout to refresh lookup at next call
      suc = true;
    } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException
            | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
      try {
        LOG.logError(l, ex.getMessage(), ex);
        mutUTransaction.rollback();
      } catch (IllegalStateException | SecurityException | SystemException ex1) {
        LOG.logWarn(l, "Rollback failed", ex1);
      }
    }
    return suc;
  }

  public <T> boolean update(T o, List linkedDelList) {
    long l = LOG.logStart();
    boolean suc = false;
    try {
      mutUTransaction.begin();
      // remove linked list
      for (Object lnkObj : linkedDelList) {

        memEManager.remove(memEManager.contains(lnkObj) ? lnkObj : memEManager.
                merge(lnkObj));
      }
      // update basic object 
      memEManager.merge(o);
      mutUTransaction.commit();
      mlstTimeOut.remove(o.getClass()); // remove timeout to refresh lookup at next call
      suc = true;
    } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException
            | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
      try {
        LOG.logError(l, ex.getMessage(), ex);
        mutUTransaction.rollback();
      } catch (IllegalStateException | SecurityException | SystemException ex1) {
        LOG.logWarn(l, "Rollback failed", ex1);
      }
    }
    return suc;
  }

  private <T> boolean updateLookup(Class<T> c) {
    return !mlstTimeOut.containsKey(c)
            || (Calendar.getInstance().getTimeInMillis() - mlstTimeOut.
            get(c)) > S_UPDATE_TIMEOUT;
  }

  /**
   *
   * @param sb
   * @return
   */
  @Override
  public boolean updateSEDBox(SEDBox sb) {
    return update(sb);
  }

  /**
   *
   * @param sb
   * @return
   */
  @Override
  public boolean updateSEDCronJob(SEDCronJob sb) {
    SEDCronJob st = getSEDCronJobById(sb.getId());
    if (st.getSEDTask() != null) {
      return update(sb, st.getSEDTask().getSEDTaskProperties());
    } else {
      return update(sb);
    }
  }
  @Override
  public boolean updateSEDProcessorRule(SEDProcessorRule sb) {
    return update(sb);
  }

  @Override
  public boolean updateSEDProcessorSet(SEDProcessorSet sb) {
    return update(sb);
  }
  /**
   *
   * @param sb
   * @return
   */
  @Override
  public boolean updateSEDUser(SEDUser sb) {
    return update(sb);
  }

}
