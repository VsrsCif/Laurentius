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

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Local;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import si.laurentius.property.SEDProperty;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.interfaces.DBSettingsInterface;
import static si.laurentius.commons.utils.abst.ASettings.newProperties;

/**
 *
 * @author sluzba
 */
@Startup
@Singleton
@Local(DBSettingsInterface.class)
@AccessTimeout(value = 60000)
@TransactionManagement(TransactionManagementType.BEAN)
public class DBSettings implements DBSettingsInterface {

  /**
     *
     */
  @Resource
  public UserTransaction mutUTransaction;

  /**
     *
     */
  @PersistenceContext(unitName = "ebMS_LAU_PU", name = "ebMS_LAU_PU")
  public EntityManager memEManager;

  /**
     *
     */
  protected SEDLogger mlog = new SEDLogger(DBSettings.class);

  /**
     *
     */
  final protected Properties mprpProperties = newProperties();

  /**
     *
     */
  protected TimerTask mRefreshTask;

  /**
     *
     */
  protected Timer mtTimer = new Timer(true);

  /**
     *
     */
  protected long m_iLastRefreshTime = 0;

  /**
     *
     */
  protected long m_iRefreshInterval = 1800 * 1000; // 30 min

  /**
     *
     */
  protected static final String SYSTEM_SETTINGS = "SYSTEM";

  /**
     *
     */
  protected static final String LAU_SETTINGS = "SED";

  /**
     *
     */
  protected static final String PMODE_FILE = "pmode-conf.xml";

  /**
     *
     */
  protected static final String KEY_PASSWD_FILE = "key-passwords.properties";

  /**
     *
     */
  protected static final String SEC_CONF_FILE = "security-conf.properties";

  /**
     *
     */
  protected static final String LOG_CONF_FILE = "sed-log4j.properties";

  /**
     *
     */
  public static String S_PROPERTY_FILE = "config.xml";

  /**
     *
     */
  protected static final String S_PROP_LAU_DOMAIN = "sed.domain";

  /**
     *
     */
  protected static final String S_PROP_LAU_DOMAIN_DEF = "sed-domain.org";

  /**
     *
     */
  public DBSettings() {
    this.mRefreshTask = new TimerTask() {
      @Override
      public void run() {
        refreshData();
      }
    };
  }

  @PostConstruct
  private void startup() {
    refreshData();
    initialize();
    // mtTimer.scheduleAtFixedRate(mRefreshTask, m_iRefreshInterval, m_iRefreshInterval);

  }

  /**
     *
     */
  final protected void refreshData() {
    long l = mlog.logStart();
    // ----------------------------------
    // SEDProperty

    List<SEDProperty> lst = getSEDProperties();
    synchronized (mprpProperties) {
      mprpProperties.clear();
      for (SEDProperty sd : lst) {
        String key = sd.getKey();
        String val = sd.getValue();
        String part = sd.getGroup();
        if (val == null) {
          mlog.log("PROPERTY: '" + key + "' is null;");
          continue;
        }
        mlog.log("PROPERTY: '" + key + "' => '" + val + "'");
        mprpProperties.put(key, val);
        if (SYSTEM_SETTINGS.equals(part)) {
          System.setProperty(key, val);
        }
      }
    }
    mlog.logEnd(l);
  }

  /**
     *
     */
  @Override
  public void initialize() {
    // set system properties
    // System.setProperty(SEDSystemProperties.SYS_PROP_JNDI_PREFIX, JNDI_PREFIX);
    // System.setProperty(SEDSystemProperties.SYS_PROP_JNDI_JMS_PREFIX, JNDI_PREFIX);

    if (!mprpProperties.containsKey(S_PROP_LAU_DOMAIN)) {
      synchronized (mprpProperties) {
        setData(S_PROP_LAU_DOMAIN, S_PROP_LAU_DOMAIN_DEF, LAU_SETTINGS);
      }
    }

    if (!mprpProperties.containsKey(SEDSystemProperties.SYS_PROP_PMODE)) {
      synchronized (mprpProperties) {
        setData(SEDSystemProperties.SYS_PROP_PMODE, SEDSystemProperties.SYS_PROP_PMODE_DEF,
            SYSTEM_SETTINGS);
      }
    }

  }

  /**
   *
   * @return
   */
  @Lock(LockType.READ)
  @Override
  public String getPModeFileName() {
    return getData(SEDSystemProperties.SYS_PROP_PMODE);
  }

  /**
   *
   * @return
   */
  @Lock(LockType.READ)
  @Override
  public String getDomain() {
    return getData(S_PROP_LAU_DOMAIN);
  }

  /**
   *
   * @return
   */
  @Lock(LockType.READ)
  @Override
  public String getHomeFolderPath() {
    return System.getProperty(SEDSystemProperties.SYS_PROP_HOME_DIR,
        SEDSystemProperties.SYS_PROP_HOME_DIR_DEF);
  }

  /**
   *
   * @return
   */
  @Lock(LockType.READ)
  @Override
  public String getSecurityFolderPath() {
    return getHomeFolderPath() + File.separator + SEDSystemProperties.SYS_PROP_FOLDER_SECURITY_DEF;
  }

  private String getData(String strKey) {
    String strVal = null;
    if (mprpProperties != null) {
      strVal = mprpProperties.getProperty(strKey);
    }
    return strVal;
  }

  private void setData(String key, String value) {
    setData(key, value, null);
  }

  @Lock(LockType.WRITE)
  private void setData(String key, String value, String group) {
    if (key == null || key.trim().isEmpty()) {
      return;
    }
    String strKey = key.trim();
    String strValue = value != null ? value.trim() : null;

    if (mprpProperties.containsKey(key)) {
      if (strValue == null) {
        mprpProperties.remove(strValue);
        removeProperty(strValue);
      } else if (mprpProperties.get(strKey) != null && !mprpProperties.get(strKey).equals(strValue)) {
        mprpProperties.setProperty(strKey, strValue);
        replaceProperty(strKey, strValue, group);
      }
    } else if (strValue != null) {
      mprpProperties.setProperty(strKey, strValue);
      storeProperty(strKey, strValue, group);
    }
  }

  /**
   *
   * @param key
   */
  protected void removeProperty(String key) {
    long l = mlog.logStart(key);
    try {

      TypedQuery<SEDProperty> tq =
          getEntityManager().createNamedQuery("SEDProperty.getByKey", SEDProperty.class);
      tq.setParameter("key", key);
      getUserTransaction().begin();
      SEDProperty sp = tq.getSingleResult();
      getEntityManager().remove(sp);
      getUserTransaction().commit();

    } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException
        | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
      String msg = "Error removing property: '" + key + "'";
      mlog.logError(l, msg, ex);
      try {
        if (getUserTransaction().getStatus() == Status.STATUS_ACTIVE) {
          getUserTransaction().rollback();
        }
      } catch (SystemException ex1) {
        mlog.logWarn(l, "Error rollback transaction", ex1);
      }
    }
    mlog.logEnd(l, key);
  }

  /**
   *
   * @param key
   * @param value
   * @param group
   */
  protected void replaceProperty(String key, String value, String group) {
    long l = mlog.logStart(key);
    try {

      TypedQuery<SEDProperty> tq =
          getEntityManager().createNamedQuery("SEDProperty.getByKey", SEDProperty.class);
      tq.setParameter("key", key);
      getUserTransaction().begin();
      SEDProperty sp = tq.getSingleResult();
      sp.setValue(value);
      if (group != null) {
        sp.setGroup(group);
      }
      getEntityManager().merge(sp);
      getUserTransaction().commit();

    } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException
        | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
      String msg = "Error replacing property: '" + key + "' with value: " + value;
      mlog.logError(l, msg, ex);
      try {
        if (getUserTransaction().getStatus() == Status.STATUS_ACTIVE) {
          getUserTransaction().rollback();
        }
      } catch (SystemException ex1) {
        mlog.logWarn(l, "Error rollback transaction", ex1);
      }
    }
    mlog.logEnd(l, key);
  }

  /**
   *
   * @param key
   * @param value
   * @param group
   */
  protected void storeProperty(String key, String value, String group) {
    long l = mlog.logStart(key);
    try {
      SEDProperty sp = new SEDProperty();
      sp.setKey(key);
      sp.setValue(value);
      sp.setGroup(group);

      getUserTransaction().begin();
      getEntityManager().persist(sp);
      getUserTransaction().commit();

    } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException
        | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
      String msg =
          "Error storing property: '" + key + "', Value: '" + value + "', group: '" + group + "' ";
      mlog.logError(l, msg, ex);
      try {
        if (getUserTransaction().getStatus() == Status.STATUS_ACTIVE) {
          getUserTransaction().rollback();
        }
      } catch (SystemException ex1) {
        mlog.logWarn(l, "Error rollback transaction", ex1);
      }
    }
    mlog.logEnd(l, key);
  }

  private EntityManager getEntityManager() {
    // for jetty
    if (memEManager == null) {
      try {
        InitialContext ic = new InitialContext();
        memEManager = (EntityManager) ic.lookup(getJNDIPrefix() + "ebMS_PU");

      } catch (NamingException ex) {
        mlog.logError(mlog.getTime(), "Error retrieving EntityManager", ex);
      }

    }
    return memEManager;
  }

  private UserTransaction getUserTransaction() {
    // for jetty
    if (mutUTransaction == null) {
      try {
        InitialContext ic = new InitialContext();

        mutUTransaction = (UserTransaction) ic.lookup("UserTransaction");

      } catch (NamingException ex) {
        mlog.logError(mlog.getTime(), "Error retrieving EntityManager", ex);
      }

    }
    return mutUTransaction;
  }

  private String getJNDIPrefix() {
    return System.getProperty(SEDSystemProperties.SYS_PROP_JNDI_PREFIX, "java:/jboss/");
  }

  /**
   *
   * @return
   */
  @Override
  public Properties getProperties() {
    return mprpProperties;
  }

  /**
   *
   * @return
   */
  @Override
  public List<SEDProperty> getSEDProperties() {
    TypedQuery<SEDProperty> q =
        getEntityManager().createNamedQuery("SEDProperty.getAll", SEDProperty.class);
    return q.getResultList();
  }

  /**
   *
   * @param prps
   */
  @Override
  public void setSEDProperties(List<SEDProperty> prps) {
    if (prps != null && !prps.isEmpty()) {
      prps.stream().forEach((sp) -> {
        setData(sp.getKey(), sp.getValue(), sp.getGroup());
      });
    }
  }

}
