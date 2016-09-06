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
package si.laurentius.commons.utils;

import generated.SedLookups;
import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
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
import javax.xml.bind.JAXBException;
import si.laurentius.cert.SEDCertStore;
import si.laurentius.cert.SEDCertificate;
import si.laurentius.cron.SEDCronJob;
import si.laurentius.cron.SEDTaskProperty;
import si.laurentius.cron.SEDTaskType;
import si.laurentius.cron.SEDTaskTypeProperty;
import si.laurentius.ebox.SEDBox;
import si.laurentius.plugin.SEDPlugin;
import si.laurentius.user.SEDUser;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.interfaces.DBSettingsInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.utils.xml.XMLUtils;

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
  protected static SEDLogger LOG = new SEDLogger(SEDLookups.class);
  // min, sec, milis.

  /**
   *
   */
  public static final long S_UPDATE_TIMEOUT = 10 * 60 * 1000; // 10 minutes
  @EJB(mappedName = SEDJNDI.JNDI_DBSETTINGS)
  private DBSettingsInterface mdbSettings;

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
    } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException |
        HeuristicRollbackException | SecurityException | IllegalStateException ex) {
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
  public boolean addSEDCertStore(SEDCertStore sb) {
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

  /**
   *
   * @param sb
   * @return
   */
  @Override
  public boolean addSEDPlugin(SEDPlugin sb) {
    return add(sb);
  }

  /**
   *
   * @param sb
   * @return
   */
  @Override
  public boolean addSEDTaskType(SEDTaskType sb) {
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

  /**
   *
   * @param f
   * @param saveCertPasswords
   */
  @Override
  public void exportLookups(File f, boolean saveCertPasswords) {
    long l = LOG.logStart();
    SedLookups slps = new SedLookups();
    slps.setExportDate(Calendar.getInstance().getTime());

    slps.setSEDBoxes(new SedLookups.SEDBoxes());
    slps.setSEDCronJobs(new SedLookups.SEDCronJobs());
    slps.setSEDProperties(new SedLookups.SEDProperties());
    slps.setSEDTaskTypes(new SedLookups.SEDTaskTypes());
    slps.setSEDUsers(new SedLookups.SEDUsers());
    slps.setSEDCertStores(new SedLookups.SEDCertStores());
    slps.setSEDPlugins(new SedLookups.SEDPlugins());

    slps.getSEDBoxes().getSEDBoxes().addAll(getSEDBoxes());
    slps.getSEDCronJobs().getSEDCronJobs().addAll(getSEDCronJobs());
    slps.getSEDProperties().getSEDProperties().addAll(mdbSettings.getSEDProperties());
    slps.getSEDTaskTypes().getSEDTaskTypes().addAll(getSEDTaskTypes());

    slps.getSEDUsers().getSEDUsers().addAll(getSEDUsers());
    List<SEDCertStore> lst = getSEDCertStore();
    if (!saveCertPasswords) {

      lst.stream().map((cs) -> {
        cs.setPassword("****");
        return cs;
      }).forEach((cs) -> {
        for (SEDCertificate c : cs.getSEDCertificates()) {
          c.setKeyPassword("****");
        }
      });
      // refresh data
      mlstCacheLookup.remove(SEDCertStore.class);
    }
    slps.getSEDCertStores().getSEDCertStores().addAll(lst);
    slps.getSEDPlugins().getSEDPlugins().addAll(getSEDPlugin());
    try {
      XMLUtils.serialize(slps, new File(f, "sed-settings.xml"));
    } catch (JAXBException | FileNotFoundException ex) {
      LOG.logError(l, ex.getMessage(), ex);
    }
    LOG.logEnd(l);
  }

  private <T> List<T> getFromCache(Class<T> c) {
    return mlstCacheLookup.containsKey(c) ? (List<T>) mlstCacheLookup.get(c) : null;
  }

  private <T> List<T> getLookup(Class<T> c) {
    List<T> t;
    if (updateLookup(c)) {
      TypedQuery<T> query = memEManager.createNamedQuery(c.getName() + ".getAll", c);
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
  public SEDBox getSEDBoxByName(String strname, boolean ignoreDomain) {
    if (strname != null && !strname.trim().isEmpty()) {
      String sedBox = strname.trim();
      List<SEDBox> lst = getSEDBoxes();
      for (SEDBox sb : lst) {
        if (ignoreDomain && sb.getBoxName().startsWith(sedBox + "@") ||
            sb.getBoxName().equalsIgnoreCase(sedBox)) {
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
   * @return
   */
  @Override
  public List<SEDCertStore> getSEDCertStore() {
    return getLookup(SEDCertStore.class);
  }

  /**
   *
   * @param id Certificate storeID ID
   * @throw IllegalArgumentException if storname is null or empty
   * @@return SEDCertStore if not found return null
   */
  @Override
  public SEDCertStore getSEDCertStoreById(BigInteger id) {
    if (id == null) {
      throw new IllegalArgumentException(String.format("KeyStore id is null"));
    }
    List<SEDCertStore> lst = getSEDCertStore();
    for (SEDCertStore cs : lst) {
      if (id.equals(cs.getId())) {
        return cs;
      }
    }
    return null;
  }

  /**
   *
   * @param storeName Certificate store name
   * @throw IllegalArgumentException if storname is null or empty
   * @return SEDCertStore if not found return null
   */
  @Override
  public SEDCertStore getSEDCertStoreByName(String storeName) {
    if (Utils.isEmptyString(storeName)) {
      throw new IllegalArgumentException(String.format("KeyStore name is null"));
    }

    List<SEDCertStore> lst = getSEDCertStore();
    for (SEDCertStore cs : lst) {
      if (storeName.equals(cs.getName())) {
        return cs;
      }
    }

    return null;
  }


  @Override
  public SEDCertificate getSEDCertificatForAlias(String alias,
      SEDCertStore cs, boolean isKey) {

    if (cs == null){
      throw new IllegalArgumentException(String.format("Null 'SEDCertStore'!"));
    }
    
    if (alias == null) {
      throw new IllegalArgumentException(String.format("Null 'alias'!"));
    }

    for (SEDCertificate c : cs.getSEDCertificates()) {
      if (c.getAlias().equalsIgnoreCase(alias)) {
        if (!isKey || c.isKeyEntry() == isKey) {
          return c;
        }
      }
    }
    return null;
  }

  @Override
  public SEDCertificate getSEDCertificatForAlias(String alias,
      String storeName, boolean isKey) {

    if (Utils.isEmptyString(alias) || Utils.isEmptyString(storeName)) {
      return null;
    }
    return getSEDCertificatForAlias(alias, getSEDCertStoreByName(storeName), isKey);
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

  /**
   *
   * @return
   */
  @Override
  public List<SEDCronJob> getSEDCronJobs() {
    return getLookup(SEDCronJob.class);
  }

  /**
   *
   * @return
   */
  @Override
  public List<SEDPlugin> getSEDPlugin() {
    return getLookup(SEDPlugin.class);
  }

  /**
   *
   * @param type
   * @return
   */
  @Override
  public SEDTaskType getSEDTaskTypeByType(String type
  ) {
    if (type != null) {

      List<SEDTaskType> lst = getSEDTaskTypes();
      for (SEDTaskType sb : lst) {
        if (type.equals(sb.getType())) {
          return sb;
        }
      }
    }
    return null;
  }

  /**
   *
   * @param type
   * @return
   */
  @Override
  public SEDPlugin getSEDPluginByType(String type
  ) {
    if (type != null) {

      List<SEDPlugin> lst = getSEDPlugin();
      for (SEDPlugin sb : lst) {
        if (type.equals(sb.getType())) {
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
  public List<SEDTaskType> getSEDTaskTypes() {
    return getLookup(SEDTaskType.class);
  }

  /**
   *
   * @param userId
   * @return
   */
  @Override
  public SEDUser getSEDUserByUserId(String userId
  ) {
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

  @PostConstruct
  void init() {
    long l = LOG.logStart();
    LOG.log("System property: " + SEDSystemProperties.SYS_PROP_INIT_LOOKUPS + " exists: " +
        System.getProperties().containsKey(SEDSystemProperties.SYS_PROP_INIT_LOOKUPS));
    if (System.getProperties().containsKey(SEDSystemProperties.SYS_PROP_INIT_LOOKUPS)) {

      File f = new File(System.getProperty(SEDSystemProperties.SYS_PROP_INIT_LOOKUPS));
      LOG.log("Update data from database: " + f.getAbsolutePath());
      try {
        SedLookups cls = (SedLookups) XMLUtils.deserialize(f, SedLookups.class);
        if (cls.getSEDBoxes() != null && !cls.getSEDBoxes().getSEDBoxes().isEmpty()) {
          cls.getSEDBoxes().getSEDBoxes().stream().forEach((cb) -> {
            if (getSEDBoxByName(cb.getBoxName(), false) == null) {
              addSEDBox(cb);
            }
          });
        }

        if (cls.getSEDCertStores() != null && !cls.getSEDCertStores().getSEDCertStores().isEmpty()) {
          cls.getSEDCertStores().getSEDCertStores().stream().forEach((cb) -> {
            cb.setId(null);
            cb.getSEDCertificates().stream().forEach((c) -> {
              c.setId(null);
            });
            add(cb);
          });
        }

        if (cls.getSEDCronJobs() != null && !cls.getSEDCronJobs().getSEDCronJobs().isEmpty()) {
          cls.getSEDCronJobs().getSEDCronJobs().stream().forEach((cb) -> {
            cb.setId(null);
            if (cb.getSEDTask() != null) {
              cb.getSEDTask().getSEDTaskProperties().stream().forEach((c) -> {
                c.setId(null);
              });
            }
            add(cb);
          });
        }

        if (cls.getSEDPlugins() != null && !cls.getSEDPlugins().getSEDPlugins().isEmpty()) {
          cls.getSEDPlugins().getSEDPlugins().stream().forEach((cb) -> {
            if (getSEDUserByUserId(cb.getType()) == null) {
              add(cb);
            }

          });
        }

        if (cls.getSEDTaskTypes() != null && !cls.getSEDTaskTypes().getSEDTaskTypes().isEmpty()) {
          cls.getSEDTaskTypes().getSEDTaskTypes().stream().forEach((cb) -> {
            if (getSEDTaskTypeByType(cb.getType()) == null) {
              for (SEDTaskTypeProperty c : cb.getSEDTaskTypeProperties()) {
                c.setId(null);
              }
              add(cb);
            }
          });
        }

        if (cls.getSEDUsers() != null && !cls.getSEDUsers().getSEDUsers().isEmpty()) {
          cls.getSEDUsers().getSEDUsers().stream().forEach((cb) -> {
            if (getSEDUserByUserId(cb.getUserId()) == null) {
              add(cb);
            }
          });
        }

        if (cls.getSEDProperties() != null && !cls.getSEDProperties().getSEDProperties().isEmpty()) {
          mdbSettings.setSEDProperties(cls.getSEDProperties().getSEDProperties());
        }

      } catch (JAXBException ex) {
        LOG.logError(l, ex);
      }

    }

    LOG.logEnd(l);
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
      memEManager.remove(memEManager.contains(o) ? o : memEManager.merge(o));
      mutUTransaction.commit();
      mlstTimeOut.remove(o.getClass()); // remove timeout to refresh lookup at next call
      suc = true;
    } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException |
        HeuristicRollbackException | SecurityException | IllegalStateException ex) {
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
  public boolean removeEDCertStore(SEDCertStore sb) {
    return remove(sb);
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

  /**
   *
   * @param sb
   * @return
   */
  @Override
  public boolean removeSEDPlugin(SEDPlugin sb) {
    return remove(sb);
  }

  /**
   *
   * @param sb
   * @return
   */
  @Override
  public boolean removeSEDTaskType(SEDTaskType sb) {
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
    } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException |
        HeuristicRollbackException | SecurityException | IllegalStateException ex) {
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
    return !mlstTimeOut.containsKey(c) ||
        (Calendar.getInstance().getTimeInMillis() - mlstTimeOut.get(c)) > S_UPDATE_TIMEOUT;
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
  public boolean updateSEDCertStore(SEDCertStore sb) {
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
      for (SEDTaskProperty tp : st.getSEDTask().getSEDTaskProperties()) {
        remove(tp);
      }
    }
    return update(sb);
  }

  /**
   *
   * @param sb
   * @return
   */
  @Override
  public boolean updateSEDPlugin(SEDPlugin sb) {
    return update(sb);
  }

  /**
   *
   * @param sb
   * @return
   */
  @Override
  public boolean updateSEDTaskType(SEDTaskType sb) {
    SEDTaskType st = getSEDTaskTypeByType(sb.getType());
    for (SEDTaskTypeProperty tp : st.getSEDTaskTypeProperties()) {
      remove(tp);
    }
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
