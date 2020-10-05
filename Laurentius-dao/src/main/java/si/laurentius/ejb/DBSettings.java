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

import si.laurentius.ejb.cache.SimpleListCache;
import java.util.List;
import java.util.Objects;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import si.laurentius.property.SEDProperty;
import si.laurentius.commons.interfaces.DBSettingsInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;

/**
 *
 * @author Jože Rihtaršič
 */
@Startup
@Singleton
@Local(DBSettingsInterface.class)
@AccessTimeout(value = 60000)
@TransactionManagement(TransactionManagementType.BEAN)
@DependsOn("SEDInitData")
public class DBSettings implements DBSettingsInterface {

  

  protected static final SEDLogger LOG = new SEDLogger(DBSettings.class);

  SimpleListCache mListCache = new SimpleListCache();
  /**
   *
   */
  @PersistenceContext(unitName = "ebMS_LAU_PU", name = "ebMS_LAU_PU")
  public EntityManager memEManager;

  /**
   *
   */
  @Resource
  public UserTransaction mutUTransaction;

  @PostConstruct
  void init() {
    // init system properties
    getSEDProperties();
  }

  /**
   *
   * @return
   */
  @Override
  public List<SEDProperty> getSEDProperties() {
    List<SEDProperty> t;
    Class c = SEDProperty.class;
    if (mListCache.cacheListTimeout(c)) {
      TypedQuery<SEDProperty> query = memEManager.createNamedQuery(
              c.getName() + ".getAll", c);
      t = query.getResultList();

      t.forEach((sd) -> {
        String key = sd.getKey();
        String val = sd.getValue();
        String part = sd.getGroup();
        if (!Utils.isEmptyString(key) && val != null && SYSTEM_SETTINGS.equals(part)) {
          System.setProperty(key, val);
        }
      });
      mListCache.cacheList(t, c);
    } else {
      t = mListCache.getFromCachedList(c);
    }
    return t;
  }

  /**
   *
   * @param o
   * @return
   */
  public boolean replaceSEDProperty(SEDProperty o) {
    long l = LOG.logStart();
    boolean suc = false;
    try {
      mutUTransaction.begin();
      memEManager.merge(o);
      mutUTransaction.commit();
      mListCache.clearCachedList(o.getClass());
      if (SYSTEM_SETTINGS.equalsIgnoreCase(o.getGroup())) {
        System.setProperty(o.getKey(), o.getValue());
      }

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

  @Lock(LockType.WRITE)
  private void setData(String key, String value, String group) {
    if (key == null || key.trim().isEmpty()) {
      return;
    }
    String strKey = key.trim();
    String strGroup = group.trim();
    String strValue = value != null ? value.trim() : null;

    SEDProperty p = getSEDProperty(strKey, strGroup);
    if (p == null) {
      p = new SEDProperty();
      p.setKey(strKey);
      p.setGroup(strGroup);
      p.setValue(strValue);
      addSEDProperty(p);
    } else if (!Objects.equals(p.getValue(), value)) {
      p.setValue(value);
      replaceSEDProperty(p);

    }
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

  /**
   *
   * @param key
   * @param value
   * @param group
   */
  @Override
  public void setSEDProperty(String key, String value, String group) {
    setData(key, value, group);
  }

  @Override
  public void removeSEDProperty(String key, String group) {
    SEDProperty p = getSEDProperty(key, group);
    if (p != null) {
      removeSEDProperty(p);
    }
  }

  @Override
  public SEDProperty getSEDProperty(String key, String group) {
    List<SEDProperty> lst = getSEDProperties();
    SEDProperty pRes = null;
    for (SEDProperty p : lst) {
      if (Objects.equals(key, p.getKey()) && Objects.equals(group, p.getGroup())) {
        pRes = p;
        break;
      }
    }
    return pRes;
  }

  private boolean addSEDProperty(SEDProperty o) {
    long l = LOG.logStart();
    boolean suc = false;
    try {
      mutUTransaction.begin();
      memEManager.persist(o);
      mutUTransaction.commit();
      mListCache.clearCachedList(o.getClass());
      if (Objects.equals(o.getGroup(), SYSTEM_SETTINGS)) {
        System.setProperty(o.getKey(), o.getValue());
      }
      suc = true;
    } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException
            | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
      String msg
              = "Error storing property: '" + o.getKey() + "', Value: '" + o.
              getValue() + "', group: '" + o.getGroup() + "' ";
      try {
        LOG.logError(l, ex.getMessage(), ex);
        mutUTransaction.rollback();
      } catch (IllegalStateException | SecurityException | SystemException ex1) {
        LOG.logWarn(l, "Rollback failed", ex1);
      }
    }
    return suc;
  }

  private boolean removeSEDProperty(SEDProperty o) {
    long l = LOG.logStart();
    boolean suc = false;
    try {
      mutUTransaction.begin();
      memEManager.remove(memEManager.contains(o) ? o : memEManager.
              merge(o));
      mutUTransaction.commit();
      if (Objects.equals(o.getGroup(), SYSTEM_SETTINGS)) {
        System.getProperties().remove(o.getKey());
      }
      mListCache.clearCachedList(o.getClass());
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

}
