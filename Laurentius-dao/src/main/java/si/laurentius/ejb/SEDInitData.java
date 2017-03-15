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
import static java.nio.file.Files.move;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.Calendar;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.AccessTimeout;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TimerConfig;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.xml.bind.JAXBException;
import si.laurentius.cert.SEDCertPassword;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.interfaces.DBSettingsInterface;
import si.laurentius.commons.interfaces.SEDCertStoreInterface;
import si.laurentius.commons.interfaces.SEDInitDataInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.interfaces.SEDPluginManagerInterface;
import si.laurentius.commons.interfaces.SEDSchedulerInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.xml.XMLUtils;
import si.laurentius.plugin.def.Plugin;
import si.laurentius.plugin.interfaces.PluginDescriptionInterface;

/**
 *
 * @author Jože Rihtaršič
 */
@Startup
@Singleton
@Local(SEDInitDataInterface.class)
@AccessTimeout(value = 60000)
public class SEDInitData implements SEDInitDataInterface {

  public static final String FILE_INIT_DATA = "init-data.xml";
  protected static final SEDLogger LOG = new SEDLogger(SEDInitData.class);
  @EJB(mappedName = SEDJNDI.JNDI_DBCERTSTORE)
  private SEDCertStoreInterface mdbCertStore;

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  private SEDLookupsInterface mdbLookups;

  @EJB(mappedName = SEDJNDI.JNDI_DBSETTINGS)
  private DBSettingsInterface mdbSettings;

  @EJB(mappedName = SEDJNDI.JNDI_PLUGIN)
  private SEDPluginManagerInterface mPluginManager;

  @EJB(mappedName = SEDJNDI.JNDI_SEDSCHEDLER)
  private SEDSchedulerInterface mshScheduler;

  @PersistenceContext(unitName = "ebMS_LAU_PU", name = "ebMS_LAU_PU")
  private EntityManager memEManager;

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
    slps.setSEDProcessors(new SedLookups.SEDProcessors());
    slps.setSEDInterceptors(new SedLookups.SEDInterceptors());
    slps.setSEDCertPassword(new SedLookups.SEDCertPassword());
    slps.setSEDCertCRLs(new SedLookups.SEDCertCRLs());

    slps.getSEDBoxes().getSEDBoxes().addAll(mdbLookups.getSEDBoxes());
    slps.getSEDUsers().getSEDUsers().addAll(mdbLookups.getSEDUsers());
    slps.getSEDCronJobs().getSEDCronJobs().addAll(mdbLookups.getSEDCronJobs());
    slps.getSEDInterceptors().getSEDInterceptors().addAll(mdbLookups.
            getSEDInterceptors());
    slps.getSEDProcessors().getSEDProcessors().addAll(mdbLookups.
            getSEDProcessors());
    // save passwords 
    if (saveCertPasswords) {
      TypedQuery<SEDCertPassword> query = memEManager.createNamedQuery(
              SEDCertPassword.class.getName() + ".getAll", SEDCertPassword.class);
      slps.getSEDCertPassword().getSEDCertPasswords().addAll(query.
              getResultList());
    }
    slps.getSEDCertCRLs().getSEDCertCRLs().addAll(mdbCertStore.getSEDCertCRLs());

    try {
      File fdata = new File(f, FILE_INIT_DATA);
      if (fdata.exists()) {
        int i = 1;
        String fileFormat = fdata.getAbsolutePath() + ".%03d";
        File fileTarget = new File(format(fileFormat, i++));

        while (fileTarget.exists()) {
          fileTarget = new File(format(fileFormat, i++));
        }

        move(fdata.toPath(), fileTarget.toPath(), REPLACE_EXISTING);
      }
      XMLUtils.serialize(slps, fdata);
    } catch (JAXBException | IOException ex) {
      LOG.logError(l, ex.getMessage(), ex);
    }
    exportPluginData(f, saveCertPasswords);
    LOG.logEnd(l);
  }

  @PostConstruct
  void init() {
    long l = LOG.logStart();
    if (SEDSystemProperties.isInitData()) {
      File initFolder = SEDSystemProperties.getInitFolder();
      File f = new File(initFolder, FILE_INIT_DATA);
      try {
        SedLookups cls = (SedLookups) XMLUtils.deserialize(f,
                SedLookups.class);

        initLookups(cls);
        initSettings(cls);
        initSEDKeystores(cls);

      } catch (JAXBException ex) {
        String msgErr = String.format(
                "An error occurred while  parsing init file %s!", f.
                        getAbsolutePath());
        LOG.logError(l, msgErr, ex);
      }
    }

    LOG.logEnd(l);
  }

  /**
   * Init lookups
   *
   * @param cls
   */
  private void initLookups(SedLookups cls) {

    if (cls.getSEDBoxes() != null && !cls.getSEDBoxes().
            getSEDBoxes().isEmpty()) {
      cls.getSEDBoxes().getSEDBoxes().stream().forEach((cb) -> {
        if (mdbLookups.getSEDBoxByLocalName(cb.getLocalBoxName()) == null) {
          mdbLookups.addSEDBox(cb);
        } else {
          LOG.formatedWarning(
                  "Sedbox %s already exist in lookup", cb.
                          getLocalBoxName());
        }
      });
    }

    if (cls.getSEDProcessors() != null && !cls.getSEDProcessors()
            .getSEDProcessors().isEmpty()) {
      cls.getSEDProcessors().getSEDProcessors().stream().forEach(
              (cb) -> {
                cb.setId(null);

                cb.getSEDProcessorRules().forEach(pr -> {
                  pr.setId(null);
                });
                cb.getSEDProcessorInstances().forEach(pr -> {
                  pr.setId(null);
                  pr.getSEDProcessorProperties().forEach(prp -> {
                    prp.setId(null);
                  });
                });

                mdbLookups.addSEDProcessor(cb);
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
                mdbLookups.addSEDCronJob(cb);
                if (cb.getActive() != null && cb.getActive()) {
                  mshScheduler.activateCronJob(cb);

                }
              });
    }

    if (cls.getSEDInterceptors() != null && !cls.getSEDInterceptors().
            getSEDInterceptors().isEmpty()) {
      cls.getSEDInterceptors().getSEDInterceptors().stream().forEach(
              (cb) -> {

                cb.setId(null);
                cb.getSEDInterceptorRules().forEach(pr -> {
                  pr.setId(null);
                });

                if (cb.getSEDInterceptorInstance() != null) {
                  cb.getSEDInterceptorInstance().getSEDInterceptorProperties().
                          stream().forEach((c) -> {
                            c.setId(null);
                          });
                }
                mdbLookups.addSEDInterceptor(cb);
              });
    }

    if (cls.getSEDUsers() != null && !cls.getSEDUsers().
            getSEDUsers().isEmpty()) {
      cls.getSEDUsers().getSEDUsers().stream().forEach((cb) -> {
        if (mdbLookups.getSEDUserByUserId(cb.getUserId()) == null) {
          mdbLookups.addSEDUser(cb);
        }
      });
    }
  }

  private void initSEDKeystores(SedLookups cls) {

    if (cls.getSEDCertPassword() != null && !cls.getSEDCertPassword().
            getSEDCertPasswords().isEmpty()) {

      List<SEDCertPassword> cslst = cls.getSEDCertPassword().
              getSEDCertPasswords();
      for (SEDCertPassword cs : cslst) {
        try {
          mdbCertStore.addPassword(cs.getAlias(), cs.getPassword());
        } catch (SEDSecurityException ex) {
          LOG.logError(ex.getMessage(), ex);
        }
      }
    }
  }

  /**
   * Init looukups
   *
   * @param cls
   */
  private void initSettings(SedLookups cls) {
    if (cls.getSEDProperties() != null && !cls.getSEDProperties().
            getSEDProperties().isEmpty()) {
      mdbSettings.setSEDProperties(cls.getSEDProperties().
              getSEDProperties());

    }

    if (System.getProperties().containsKey(
            SEDSystemProperties.SYS_PROP_LAU_DOMAIN)) {
      mdbSettings.setSEDProperty(SEDSystemProperties.SYS_PROP_LAU_DOMAIN,
              System.
                      getProperty(SEDSystemProperties.SYS_PROP_LAU_DOMAIN),
              "SYSTEM");
    }
    if (!System.getProperties().containsKey(
            SEDSystemProperties.SYS_PROP_LAU_DOMAIN)) {
      System.setProperty(SEDSystemProperties.SYS_PROP_LAU_DOMAIN,
              SEDSystemProperties.getLocalDomain());
    }
  }

  public void exportPluginData(File initFolder, boolean savePasswords) {
    long l = LOG.logStart();

    for (Plugin p : mPluginManager.getRegistredPlugins()) {
      try {
        PluginDescriptionInterface plg = InitialContext.doLookup(p.getJndi());
        plg.exportData(initFolder, savePasswords);

      } catch (NamingException ex) {
        String errmsg = String.format(
                "NamingException occured while exporting data for plugin '%s' JNDI '%s'. Error '%s'. ",
                p.getName(), p.getJndi(),
                ex.getMessage());
        LOG.logError(l, errmsg, ex);
      } catch (Throwable ex) {
        String errmsg = String.format(
                "Throwable error occured while exporting data for  plugin '%s' JNDI '%s'. Error '%s'. ",
                p.getName(), p.getJndi(), ex.getMessage());
        LOG.logError(l, errmsg, ex);
      }
    }
  }
}
