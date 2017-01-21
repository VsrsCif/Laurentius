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

import generated.SedLookups;
import java.io.File;
import java.io.IOException;
import static java.lang.String.format;
import java.math.BigInteger;
import static java.nio.file.Files.move;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
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
import si.laurentius.cert.crl.SEDCertCRL;
import si.laurentius.cert.SEDCertStore;
import si.laurentius.cert.SEDCertificate;
import si.laurentius.cron.SEDCronJob;
import si.laurentius.ebox.SEDBox;
import si.laurentius.user.SEDUser;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.interfaces.DBSettingsInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.commons.utils.xml.XMLUtils;
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
  protected static SEDLogger LOG = new SEDLogger(SEDLookups.class);
  // min, sec, milis.

  /**
   *
   */
  public static final long S_UPDATE_TIMEOUT = 10 * 60 * 1000; // 10 minutes
  @EJB(mappedName = SEDJNDI.JNDI_DBSETTINGS)
  private DBSettingsInterface mdbSettings;

  public static final String FILE_INIT_DATA = "init-data.xml";

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
  public boolean addSEDUser(SEDUser sb) {
    return add(sb);
  }

  @Override
  public boolean addSEDProcessorSet(SEDProcessorSet sb) {
     return add(sb);
  }

  @Override
  public boolean addSEDProcessorRule(SEDProcessorRule sb) {
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
    slps.setSEDUsers(new SedLookups.SEDUsers());
    slps.setSEDCertStores(new SedLookups.SEDCertStores());
    slps.setSEDCertCRLs(new SedLookups.SEDCertCRLs());
    slps.setSEDProcessorRules(new SedLookups.SEDProcessorRules());
    slps.setSEDProcessorSets(new SedLookups.SEDProcessorSets());

    slps.getSEDBoxes().getSEDBoxes().addAll(getSEDBoxes());
    slps.getSEDCronJobs().getSEDCronJobs().addAll(getSEDCronJobs());
    slps.getSEDProperties().getSEDProperties().addAll(mdbSettings.
            getSEDProperties());

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
    slps.getSEDCertCRLs().getSEDCertCRLs().addAll(getSEDCertCRLs());
    try {

      File fdata = new File(f, FILE_INIT_DATA);
      int i = 1;
      String fileFormat = fdata.getAbsolutePath() + ".%03d";
      File fileTarget = new File(format(fileFormat, i++));

      while (fileTarget.exists()) {
        fileTarget = new File(format(fileFormat, i++));
      }

      move(fdata.toPath(), fileTarget.toPath(), REPLACE_EXISTING);

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
      throw new IllegalArgumentException(String.format(
              "KeyStore id is null"));
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
      throw new IllegalArgumentException(String.format(
              "KeyStore name is null"));
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

    if (cs == null) {
      throw new IllegalArgumentException(String.format(
              "Null 'SEDCertStore'!"));
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
    return getSEDCertificatForAlias(alias, getSEDCertStoreByName(storeName),
            isKey);
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

  @Override
  public SEDProcessorSet getSEDProcessorSet(BigInteger id) {
    if (id != null) {
      List<SEDProcessorSet> lst = getSEDProcessorSets();
      for (SEDProcessorSet sb : lst) {
        if (id.equals(sb.getId())) {
          return sb;
        }
      }
    }
    return null;
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
  public List<SEDProcessorSet> getSEDProcessorSets() {
    return getLookup(SEDProcessorSet.class);
  }

  @Override
  public List<SEDProcessorRule> getSEDProcessorRules() {
    return getLookup(SEDProcessorRule.class);
  }
  
  
  

  @PostConstruct
  void init() {
    long l = LOG.logStart();
    if ( SEDSystemProperties.isInitData()) {

      File f = new File(
              SEDSystemProperties.getInitFolder().getAbsolutePath()
              + File.separator + FILE_INIT_DATA );
      LOG.log("Update data from database: " + f.getAbsolutePath());
      try {
        SedLookups cls = (SedLookups) XMLUtils.deserialize(f,
                SedLookups.class);
        if (cls.getSEDBoxes() != null && !cls.getSEDBoxes().
                getSEDBoxes().isEmpty()) {
          cls.getSEDBoxes().getSEDBoxes().stream().forEach((cb) -> {
            if (getSEDBoxByLocalName(cb.getLocalBoxName()) == null) {
              

              addSEDBox(cb);
            } else {
              LOG.formatedWarning(
                      "Sedbox %s already exist in lookup", cb.
                              getLocalBoxName());
            }
          });
        }

        if (cls.getSEDCertStores() != null && !cls.getSEDCertStores().
                getSEDCertStores().isEmpty()) {
          cls.getSEDCertStores().getSEDCertStores().stream().forEach(
                  (cb) -> {
                    cb.setId(null);
                    cb.getSEDCertificates().stream().forEach(
                            (c) -> {
                              c.setId(null);
                            });
                    add(cb);
                  });
        }

        if (cls.getSEDCertCRLs() != null && !cls.getSEDCertCRLs().
                getSEDCertCRLs().isEmpty()) {
          cls.getSEDCertCRLs().getSEDCertCRLs().stream().forEach(
                  (cb) -> {
                    cb.setId(null);
                    add(cb);
                  });
        }
        
         if (cls.getSEDProcessorSets() != null && !cls.getSEDProcessorSets()
                 .getSEDProcessorSets().isEmpty()) {
          cls.getSEDProcessorSets().getSEDProcessorSets().stream().forEach(
                  (cb) -> {
                    cb.setId(null);
                    add(cb);
                  });
        }
         
         if (cls.getSEDProcessorRules() != null && !cls.getSEDProcessorRules()
                 .getSEDProcessorRules().isEmpty()) {
          cls.getSEDProcessorRules().getSEDProcessorRules().stream().forEach(
                  (cb) -> {
                    cb.setId(null);
                    add(cb);
                  });
        }

        if (cls.getSEDCronJobs() != null && !cls.getSEDCronJobs().
                getSEDCronJobs().isEmpty()) {
          cls.getSEDCronJobs().getSEDCronJobs().stream().forEach(
                  (cb) -> {
                    cb.setId(null);
                    if (cb.getSEDTask() != null) {
                      cb.getSEDTask().getSEDTaskProperties().
                              stream().forEach((c) -> {
                                c.setId(null);
                              });
                    }
                    add(cb);
                  });
        }

        if (cls.getSEDUsers() != null && !cls.getSEDUsers().
                getSEDUsers().isEmpty()) {
          cls.getSEDUsers().getSEDUsers().stream().forEach((cb) -> {
            if (getSEDUserByUserId(cb.getUserId()) == null) {
              add(cb);
            }
          });
        }

        if (cls.getSEDProperties() != null && !cls.getSEDProperties().
                getSEDProperties().isEmpty()) {
          mdbSettings.setSEDProperties(cls.getSEDProperties().
                  getSEDProperties());

        }

        if (System.getProperties().containsKey(
                SEDSystemProperties.S_PROP_LAU_DOMAIN)) {
          mdbSettings.setSEDProperty(
                  SEDSystemProperties.S_PROP_LAU_DOMAIN, System.
                          getProperty(
                                  SEDSystemProperties.S_PROP_LAU_DOMAIN),
                  "SYSTEM");
        }

      } catch (JAXBException ex) {
        LOG.logError(l, ex);
      }

    }

    if (!System.getProperties().containsKey(
            SEDSystemProperties.S_PROP_LAU_DOMAIN)) {
      System.setProperty(SEDSystemProperties.S_PROP_LAU_DOMAIN,
              SEDSystemProperties.getLocalDomain());
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
  public boolean removeSEDCertStore(SEDCertStore sb) {
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
  public boolean removeSEDUser(SEDUser sb) {
    return remove(sb);
  }

  @Override
  public boolean removeSEDProcessorSet(SEDProcessorSet sb) {
     return remove(sb);
  }

  @Override
  public boolean removeSEDProcessorRule(SEDProcessorRule sb) {
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
      return update(sb, st.getSEDTask().getSEDTaskProperties());
      /*  // remove exiting task properties.
      for (SEDTaskProperty tp : st.getSEDTask().getSEDTaskProperties()) {
        remove(tp);
      }*/
    } else {
      return update(sb);
    }
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

  @Override
  public boolean addSEDCertCRL(SEDCertCRL sb) {
    return add(sb);
  }

  @Override
  public List<SEDCertCRL> getSEDCertCRLs() {
    return getLookup(SEDCertCRL.class);
  }

  @Override
  public SEDCertCRL getSEDCertCRLById(BigInteger id) {
    if (id != null) {

      List<SEDCertCRL> lst = getSEDCertCRLs();
      for (SEDCertCRL sb : lst) {
        if (sb.getId().equals(id)) {
          return sb;
        }
      }
    }
    return null;
  }

  @Override
  public SEDCertCRL getSEDCertCRLByIssuerDNAndUrl(String issuerDn, String http,
          String ldap) {
    if (Utils.isEmptyString(issuerDn)) {
      return null;
    }

    List<SEDCertCRL> lst = getSEDCertCRLs();
    for (SEDCertCRL sb : lst) {
      if (Objects.equals(issuerDn, sb.getIssuerDN())
              && (Objects.equals(http, sb.getHttp())
              || Objects.equals(ldap, sb.getLdap()))) {
        return sb;
      }
    }

    return null;
  }

  @Override
  public boolean removeSEDCertCRL(SEDCertCRL sb) {
    return remove(sb);
  }

  /**
   *
   * @param sb
   * @return
   */
  @Override
  public boolean updateSEDCertCRL(SEDCertCRL sb) {
    return update(sb);
  }

  @Override
  public boolean updateSEDProcessorSet(SEDProcessorSet sb) {
    return update(sb);
  }

  @Override
  public boolean updateSEDProcessorRule(SEDProcessorRule sb) {
   return update(sb);
  }

}
