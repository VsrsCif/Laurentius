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
package si.laurentius.plg.db;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import javax.annotation.PostConstruct;
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
import javax.xml.bind.JAXBException;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.commons.utils.xml.XMLUtils;
import si.laurentius.plugin.imp.IMPXslt;
import si.laurentius.plugin.imp.Namespace;
import si.laurentius.plugin.imp.PlgBasicInit;
import si.laurentius.plugin.imp.XSLTRule;

/**
 * @author Jože Rihtaršič
 */
@Startup
@Singleton
@Local(IMPDBInterface.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class IMPDatabase implements IMPDBInterface {

  public static final String FILE_INIT_DATA = "plg-basic-init.xml";
  /**
   *
   */
  private static final SEDLogger LOG = new SEDLogger(IMPDatabase.class);
  // min, sec, milis.
  public static final long S_UPDATE_TIMEOUT = 10 * 60 * 1000; // 10 minutes
  /**
   *
   */
  @PersistenceContext(unitName = "ebMS_PLG_BASIC_PU", name = "ebMS_PLG_BASIC_PU")
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
  public boolean addXSLT(IMPXslt sb) {
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
   */
  @Override
  public void exportInitData(File f) {
    long l = LOG.logStart();
    PlgBasicInit slps = new PlgBasicInit();
    slps.setExportDate(Calendar.getInstance().getTime());

    slps.setIMPXslts(new PlgBasicInit.IMPXslts());

    slps.getIMPXslts().getIMPXslts().addAll(getXSLTs());

    try {

      File fdata = new File(f, FILE_INIT_DATA);
      if (fdata.exists()) {
        int i = 1;
        String fileFormat = fdata.getAbsolutePath() + ".%03d";
        File fileTarget = new File(String.format(fileFormat, i++));

        while (fileTarget.exists()) {
          fileTarget = new File(String.format(fileFormat, i++));
        }
        Files.move(fdata.toPath(), fileTarget.toPath(),
                StandardCopyOption.REPLACE_EXISTING);
      }
      XMLUtils.serialize(slps, fdata);
    } catch (JAXBException | IOException ex) {
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
      TypedQuery<T> query = memEManager.createNamedQuery(
              c.getName() + ".getAll", c);
      t = query.getResultList();
      cacheLookup(t, c);
    } else {
      t = getFromCache(c);
    }
    return t;
  }

  @Override
  public IMPXslt getXSLT(String instance) {
    if (!Utils.isEmptyString(instance)) {

      List<IMPXslt> lst = getXSLTs();
      for (IMPXslt sb : lst) {
        if (instance.equalsIgnoreCase(sb.getInstance())) {
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
  public List<IMPXslt> getXSLTs() {
    return getLookup(IMPXslt.class);
  }

  @PostConstruct
  void init() {
    long l = LOG.logStart();

    if (SEDSystemProperties.isInitData()) {

      File f = new File(SEDSystemProperties.getInitFolder(),
              FILE_INIT_DATA);
      LOG.log("Update data from database: " + f.getAbsolutePath());
      try {
        PlgBasicInit cls = (PlgBasicInit) XMLUtils.deserialize(f,
                PlgBasicInit.class);

        if (cls.getIMPXslts() != null && !cls.getIMPXslts().
                getIMPXslts().isEmpty()) {
          cls.getIMPXslts().getIMPXslts().stream().forEach((cb) -> {
            for (XSLTRule xpr : cb.getXSLTRules()) {
              xpr.setId(null);
              if (xpr.getXPath() != null) {
                for (Namespace ns : xpr.getXPath().getNamespaces()) {
                  ns.setId(null);
                }
              }
            }
            addXSLT(cb);
          });
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
  public boolean removeXSLT(IMPXslt sb) {
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
  public boolean updateXSLT(IMPXslt sb) {
    return update(sb);
  }

}
