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
import si.laurentius.application.SEDApplication;
import si.laurentius.cert.SEDCertPassword;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.DBSettingsInterface;
import si.laurentius.commons.interfaces.SEDInitDataInterface;
import si.laurentius.commons.interfaces.SEDPluginManagerInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.commons.utils.xml.XMLUtils;
import si.laurentius.cron.SEDCronJob;
import si.laurentius.ebox.SEDBox;
import si.laurentius.interceptor.SEDInterceptor;
import si.laurentius.plugin.def.Plugin;
import si.laurentius.plugin.interfaces.PluginDescriptionInterface;
import si.laurentius.process.SEDProcessor;
import si.laurentius.property.SEDProperty;
import si.laurentius.user.SEDUser;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.*;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import static java.lang.String.format;
import static java.nio.file.Files.move;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Class initialize "only" application and plugins  data/properties at server startup. It does not initialize ejb plugins,
 * cron tasks, etc. If system property 'laurentius.init' is set to true, then data initialization is done using the
 * '${laurentius.init.dir}/init-data.xml'
 * Application initialization is done after this class starts by the EJB SEDInitApplication.
 *
 * @author Joze Rihtarsic
 */
@Startup
@Singleton
@Local(SEDInitDataInterface.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class SEDInitData implements SEDInitDataInterface {

    public static final String FILE_INIT_DATA = "init-data.xml";
    protected static final SEDLogger LOG = new SEDLogger(SEDInitData.class);

    @EJB(mappedName = SEDJNDI.JNDI_PLUGIN)
    SEDPluginManagerInterface mPluginManager;

    @PersistenceContext(unitName = "ebMS_LAU_PU", name = "ebMS_LAU_PU")
    EntityManager memEManager;

    @Resource
    UserTransaction mutUTransaction;


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
     * @param f
     * @param saveCertPasswords
     */
    @Override
    public void exportLookups(File f, boolean saveCertPasswords) throws StorageException {
        long l = LOG.logStart();
        SedLookups slps = new SedLookups();
        slps.setExportDate(Calendar.getInstance().getTime());
        SedLookups.SEDBoxes sedBoxes = new SedLookups.SEDBoxes();
        SedLookups.SEDCronJobs sedCronJobs = new SedLookups.SEDCronJobs();
        SedLookups.SEDProperties sedProperties = new SedLookups.SEDProperties();
        SedLookups.SEDUsers sedDUsers = new SedLookups.SEDUsers();
        SedLookups.SEDApplications sedApplications = new SedLookups.SEDApplications();
        SedLookups.SEDProcessors sedProcessors = new SedLookups.SEDProcessors();
        SedLookups.SEDInterceptors sedInterceptors = new SedLookups.SEDInterceptors();
        SedLookups.SEDCertPassword sedCertPassword = new SedLookups.SEDCertPassword();
        SedLookups.SEDCertCRLs sedCertCRLs = new SedLookups.SEDCertCRLs();
        // set properties

        sedProperties.getSEDProperties().addAll(getDatabaseObjects(SEDProperty.class));

        sedBoxes.getSEDBoxes().addAll(getDatabaseObjects(SEDBox.class));
        sedDUsers.getSEDUsers().addAll(getDatabaseObjects(SEDUser.class));
        sedApplications.getSEDApplications().addAll(getDatabaseObjects(SEDApplication.class));
        sedCronJobs.getSEDCronJobs().addAll(getDatabaseObjects(SEDCronJob.class));
        sedInterceptors.getSEDInterceptors().addAll(getDatabaseObjects(SEDInterceptor.class));
        sedProcessors.getSEDProcessors().addAll(getDatabaseObjects(SEDProcessor.class));
        // save passwords
        if (saveCertPasswords) {
            sedCertPassword.getSEDCertPasswords().addAll(getDatabaseObjects(SEDCertPassword.class));
        }
        //sedCertCRLs.getSEDCertCRLs().addAll(mdbCertStore.getSEDCertCRLs());

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
            String strMsg = "Error occured while serializing init data. Error:" + ex.getMessage();
            LOG.logError(l, strMsg, ex);
            throw new StorageException(strMsg, ex);
        }

        slps.setSEDBoxes(sedBoxes);
        slps.setSEDCronJobs(sedCronJobs);
        slps.setSEDProperties(sedProperties);
        slps.setSEDUsers(sedDUsers);
        slps.setSEDApplications(sedApplications);
        slps.setSEDProcessors(sedProcessors);
        slps.setSEDInterceptors(sedInterceptors);
        slps.setSEDCertPassword(sedCertPassword);
        slps.setSEDCertCRLs(sedCertCRLs);

        exportPluginData(f, saveCertPasswords);

        LOG.logEnd(l);
    }

    /**
     * Method retrieve database entities for give class. It execute prepared named query 'cls.getName() + ".getAll"'
     * For method to work named query must be defined in entity class!
     *
     * @param cls - database entity class
     * @param <T> - returned list type
     * @return - returned list
     */
    <T> List<T> getDatabaseObjects(Class cls) {
        TypedQuery<T> query = memEManager.createNamedQuery(
                cls.getName() + ".getAll", cls);
        return query.getResultList();
    }


    /**
     * Init lookups
     *
     * @param cls
     */
    public void initLookups(SedLookups cls) {
        try {
            mutUTransaction.begin();


            if (cls.getSEDBoxes() != null && !cls.getSEDBoxes().
                    getSEDBoxes().isEmpty()) {
                cls.getSEDBoxes().getSEDBoxes().stream().forEach((cb) -> {
                    memEManager.persist(cb);
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

                            memEManager.persist(cb);
                        });
            }

            if (cls.getSEDCronJobs() != null && !cls.getSEDCronJobs().
                    getSEDCronJobs().isEmpty()) {


                cls.getSEDCronJobs().getSEDCronJobs().stream().forEach(
                        (cronJob) -> {
                            cronJob.setId(null);
                            cronJob.getSEDTasks().forEach(cronTask -> {
                                        cronTask.setId(null);
                                        cronTask.getSEDTaskProperties().
                                                stream().forEach((cronTaskProperty) -> {
                                            cronTaskProperty.setId(null);
                                        });
                                    }
                            );
                            LOG.log("Persist: " + cronJob);
                            memEManager.persist(cronJob);
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
                            memEManager.persist(cb);
                        });
            }

            if (cls.getSEDUsers() != null && !cls.getSEDUsers().
                    getSEDUsers().isEmpty()) {
                cls.getSEDUsers().getSEDUsers().stream().forEach((cb) -> {
                    memEManager.persist(cb);
                });
            }

            if (cls.getSEDApplications() != null && !cls.getSEDApplications().
                    getSEDApplications().isEmpty()) {
                cls.getSEDApplications().getSEDApplications().stream().forEach((cb) -> {
                    memEManager.persist(cb);
                });
            }

            if (cls.getSEDCertPassword() != null && !cls.getSEDCertPassword().
                    getSEDCertPasswords().isEmpty()) {
                cls.getSEDCertPassword().getSEDCertPasswords().stream().forEach((cb) -> {
                    memEManager.persist(cb);
                });
            }

            // update system properties from init file
            if (cls.getSEDProperties() != null && !cls.getSEDProperties().
                    getSEDProperties().isEmpty()) {

                cls.getSEDProperties().getSEDProperties().stream().forEach((cb) -> {
                    updateSystemPropertyValue(cb);
                    memEManager.persist(cb);
                });
            }
            mutUTransaction.commit();
        } catch (NotSupportedException | SystemException | HeuristicMixedException | HeuristicRollbackException | RollbackException e) {
            e.printStackTrace();
        }
    }

    /**
     * If system property exists for given property, then property is updated. if property does not exists
     * then method sets the property as System.setProperty
     *
     * @param property
     */
    public void updateSystemPropertyValue(SEDProperty property) {
        if (DBSettingsInterface.SYSTEM_SETTINGS.equalsIgnoreCase(property.getGroup())) {
            String sysProperty = System.getProperty(property.getKey());
            if (!Utils.isEmptyString(sysProperty)) {
                property.setValue(sysProperty);
            } else {
                System.setProperty(property.getKey(), property.getValue());
            }

        }
    }

    public void exportPluginData(File initFolder, boolean savePasswords) throws StorageException {
        long l = LOG.logStart();

        for (Plugin p : mPluginManager.getRegistredPlugins()) {
            try {
                PluginDescriptionInterface plg = InitialContext.doLookup(p.getJndi());
                plg.exportData(initFolder, savePasswords);

            } catch (NamingException ex) {
                String errmsg = String.format(
                        "NamingException occurred while exporting data for plugin '%s' JNDI '%s'. Error '%s'. ",
                        p.getName(), p.getJndi(),
                        ex.getMessage());

                LOG.logError(l, errmsg, ex);

                throw new StorageException(errmsg, ex);
            } catch (Throwable ex) {
                String errmsg = String.format(
                        "Throwable error occurred while exporting data for  plugin '%s' JNDI '%s'. Error '%s'. ",
                        p.getName(), p.getJndi(), ex.getMessage());
                LOG.logError(l, errmsg, ex);
                throw new StorageException(errmsg, ex);
            }
        }
    }
}
