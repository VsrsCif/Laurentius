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

import java.io.File;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.xml.bind.JAXBException;
import si.laurentius.msh.inbox.event.MSHInEvent;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.outbox.event.MSHOutEvent;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.cron.SEDTaskExecution;
import si.laurentius.commons.enums.SEDInboxMailStatus;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.enums.SEDOutboxMailStatus;
import si.laurentius.commons.SEDSystemProperties;
import static si.laurentius.commons.SEDSystemProperties.SYS_PROP_JNDI_JMS_PREFIX;
import static si.laurentius.commons.SEDSystemProperties.SYS_PROP_JNDI_PREFIX;
import si.laurentius.commons.enums.SEDTaskStatus;
import si.laurentius.commons.SEDValues;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.enums.SEDMailPartSource;
import si.laurentius.commons.exception.PModeException;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.JMSManagerInterface;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.Utils;
import si.laurentius.commons.utils.xml.XMLUtils;
import si.laurentius.lce.DigestUtils;
import si.laurentius.msh.inbox.payload.MSHInPart;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.laurentius.msh.pmode.PMode;

/**
 *
 * @author Jože Rihtaršič
 */
@Stateless
@Local(SEDDaoInterface.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class SEDDaoBean implements SEDDaoInterface {

    private static final String MSG_ERR_ROLLBACK = "Rollback failed";
    private static final String MSG_ERR_STATUS = "Status %s not setted to class: %s, id  %d. Effected %d rows!  Mail not exists or id duplicates?";
    private static final String MSG_ERR_PESIST_PARTS = "Error occured while persisting parts (%s) for class %s, id %d. Error %s";
    private static final String MSG_ERR_COMMIT_PARTS = "Error occured while commit parts (%s) for class %s, id %d. Error %s";
    private static final String MSG_ERR_REMOVE = "Error occured while removing class %s, id %d. Error %s";
    private static final String MSG_ERR_JMS_SUBMIT = "Error occured while submitting mail (id:  %d) to JMS %s. Error %s";

    /**
     *
     */
    protected static final SEDLogger LOG = new SEDLogger(SEDDaoBean.class);

    @EJB(mappedName = SEDJNDI.JNDI_JMSMANAGER)
    JMSManagerInterface mJMS;

    @EJB(mappedName = SEDJNDI.JNDI_PMODE)
    PModeInterface mpModeManager;

    /**
     *
     */
    @PersistenceContext(unitName = "ebMS_LAU_PU", name = "ebMS_LAU_PU")
    public EntityManager memEManager;

    /**
     *
     */
    protected Queue mqMSHQueue = null;

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
    protected <T> void add(T o) throws StorageException {
        long l = LOG.logStart();
        try {
            mutUTransaction.begin();
            memEManager.persist(o);
            mutUTransaction.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException
                | HeuristicRollbackException | SecurityException | IllegalStateException ex) {

            // get cause of an error, because usually contains a good description
            // of sql error.
            String erMsg = "Error occured while adding object: "
                    + o.getClass().getName() + ". Root error: " + getExceptioRootCase(
                    ex);
            // do not log error it is thrown on .. 
            LOG.logError(l, erMsg, null);
            try {

                mutUTransaction.rollback();
            } catch (IllegalStateException | SecurityException | SystemException ex1) {
                LOG.logWarn(l, MSG_ERR_ROLLBACK, ex1);
            }
            throw new StorageException(erMsg, ex);

        }
    }

    @Override
    public boolean addInMailPayload(MSHInMail mail, List<MSHInPart> lstParts,
            SEDInboxMailStatus status, String statusdesc, String userId,
            String applicationId) throws StorageException {
        // persits parts
        long l = LOG.logStart();
        boolean suc = false;
        try {
            mutUTransaction.begin();
            for (MSHInPart ip : lstParts) {
                ip.setMailId(mail.getId());
                memEManager.persist(ip);
            }
        } catch (NotSupportedException | SystemException ex) {
            String msg = String.format(MSG_ERR_PESIST_PARTS, "Add:" + lstParts.size(),
                    mail.getClass().getName(), mail.getId(), ex.getMessage());
            LOG.logError(l, msg, null);
            try {
                mutUTransaction.rollback();
            } catch (IllegalStateException | SecurityException | SystemException ex1) {
                LOG.logWarn(l, ex1.getMessage(), ex1);
            }
            throw new StorageException(msg, ex);

        }

        mail.setStatus(status.getValue());
        mail.setStatusDate(Calendar.getInstance().getTime());

        Query updq = memEManager.createNamedQuery(SEDNamedQueries.UPDATE_INMAIL);
        updq.setParameter(SEDNamedQueries.QUERY_PARAM_ID, mail.getId());
        updq.setParameter(SEDNamedQueries.QUERY_PARAM_STATUS_DATE, mail.
                getStatusDate());
        updq.setParameter(SEDNamedQueries.QUERY_PARAM_STATUS, mail.getStatus());

        int iVal = updq.executeUpdate();
        if (iVal != 1) {
            try {
                mutUTransaction.rollback();
            } catch (IllegalStateException | SecurityException | SystemException ex1) {
                LOG.logWarn(l, MSG_ERR_ROLLBACK, ex1);
            }
            String msg = String.format(MSG_ERR_STATUS, mail.getStatus(), mail.
                    getClass().getName(), mail.getId(), iVal);
            LOG.logError(l, msg, null);
            throw new StorageException(msg, null);
        }

        // persist mail event
        MSHInEvent me = new MSHInEvent();
        me.setMailId(mail.getId());
        me.setDescription(statusdesc);
        me.setStatus(mail.getStatus());
        me.setDate(mail.getStatusDate());
        me.setUserId(userId);
        me.setApplicationId(applicationId);
        try {
            memEManager.persist(me);

            mutUTransaction.commit();
            suc = true;
        } catch (SystemException | RollbackException | HeuristicMixedException
                | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            String msg = String.format(MSG_ERR_COMMIT_PARTS, "Add:" + lstParts.size(),
                    mail.getClass().getName(), mail.getId(), ex.getMessage());
            LOG.logError(l, msg, null);
            try {
                LOG.logError(l, ex.getMessage(), ex);
                mutUTransaction.rollback();
            } catch (IllegalStateException | SecurityException | SystemException ex1) {
                LOG.logWarn(l, MSG_ERR_ROLLBACK, ex1);
            }
            throw new StorageException(msg, ex);

        }

        return suc;
    }

    @Override
    public boolean addOutMailPayload(MSHOutMail mail, List<MSHOutPart> lstParts,
            SEDOutboxMailStatus status, String statusdesc, String userId,
            String applicationId) throws StorageException {

        return updateOutMailPayload(mail, lstParts, Collections.emptyList(),
                Collections.emptyList(), status,
                statusdesc, userId, applicationId);
    }

    @Override
    public boolean updateInMailPayload(MSHInMail mail, List<MSHInPart> lstAddParts, List<MSHInPart> lstUpdateParts, List<MSHInPart> lstDeleteParts, SEDInboxMailStatus status, String statusdesc, String userId, String applicationId) throws StorageException {
        // persits parts
        long l = LOG.logStart();
        boolean suc = false;
        String strMsg = String.format("a: %d, u: %d, d %d", lstAddParts.size(),
                lstUpdateParts.size(), lstDeleteParts.size());
        try {
            mutUTransaction.begin();
            for (MSHInPart ip : lstAddParts) {
                ip.setMailId(mail.getId());
                File f = StorageUtils.getFile(ip.getFilepath());
                ip.setSize(BigInteger.valueOf(f.length()));
                ip.setSha256Value(DigestUtils.getBase64Sha256Digest(f));
                if (Utils.isEmptyString(ip.getEbmsId())) {
                    ip.setEbmsId(Utils.getUUIDWithLocalDomain());
                }
                memEManager.persist(ip);
            }

            for (MSHInPart ip : lstUpdateParts) {
                ip.setMailId(mail.getId());
                File f = StorageUtils.getFile(ip.getFilepath());
                ip.setSize(BigInteger.valueOf(f.length()));
                ip.setSha256Value(DigestUtils.getBase64Sha256Digest(f));
                if (Utils.isEmptyString(ip.getEbmsId())) {
                    ip.setEbmsId(Utils.getUUIDWithLocalDomain());
                }
                memEManager.merge(ip);
            }

            for (MSHInPart ip : lstDeleteParts) {
                ip.setMailId(mail.getId());
                memEManager.remove(l);
            }

        } catch (NotSupportedException | SystemException ex) {
            String msg = String.format(MSG_ERR_PESIST_PARTS, strMsg, mail.getClass().
                    getName(), mail.getId(), ex.getMessage());
            LOG.logError(l, msg, null);
            try {
                mutUTransaction.rollback();
            } catch (IllegalStateException | SecurityException | SystemException ex1) {
                LOG.logWarn(l, ex1.getMessage(), ex1);
            }
            throw new StorageException(msg, ex);

        }

        // if merge cause locking
        mail.setStatusDate(Calendar.getInstance().getTime());
        mail.setStatus(status.getValue());

        Query updq = memEManager.createNamedQuery(SEDNamedQueries.UPDATE_INMAIL);
        updq.setParameter(SEDNamedQueries.QUERY_PARAM_ID, mail.getId());
        updq.setParameter(SEDNamedQueries.QUERY_PARAM_STATUS_DATE, mail.
                getStatusDate());
        updq.setParameter(SEDNamedQueries.QUERY_PARAM_STATUS, mail.getStatus());

        int iVal = updq.executeUpdate();
        if (iVal != 1) {
            try {
                mutUTransaction.rollback();
            } catch (IllegalStateException | SecurityException | SystemException ex1) {
                LOG.logWarn(l, ex1.getMessage(), ex1);
            }
            String msg = String.format(MSG_ERR_STATUS, mail.getStatus(), mail.
                    getClass().getName(), mail.getId(), iVal);
            LOG.logError(l, msg, null);
            throw new StorageException(msg, null);
        }

        // persist mail event
        MSHInEvent me = new MSHInEvent();
        me.setMailId(mail.getId());
        me.setDescription(statusdesc + " " + strMsg);
        me.setStatus(mail.getStatus());
        me.setDate(mail.getStatusDate());
        me.setUserId(userId);
        me.setApplicationId(applicationId);
        try {
            memEManager.persist(me);
            mutUTransaction.commit();

            suc = true;
        } catch (SystemException | RollbackException | HeuristicMixedException
                 | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            String msg = String.format(MSG_ERR_COMMIT_PARTS, strMsg, mail.getClass().
                    getName(), mail.getId(), ex.getMessage());
            LOG.logError(l, msg, null);
            try {
                mutUTransaction.rollback();
            } catch (IllegalStateException | SecurityException | SystemException ex1) {
                LOG.logWarn(l, MSG_ERR_ROLLBACK, ex1);
            }
            throw new StorageException(msg, ex);
        }

        return suc;
    }

    @Override
    public boolean updateOutMailPayload(MSHOutMail mail,
            List<MSHOutPart> lstAddParts, List<MSHOutPart> lstUpdateParts,
            List<MSHOutPart> lstDeleteParts,
            SEDOutboxMailStatus status, String statusdesc, String userId,
            String applicationId) throws StorageException {
        // persits parts
        long l = LOG.logStart();
        boolean suc = false;
        String strMsg = String.format("a: %d, u: %d, d %d", lstAddParts.size(),
                lstUpdateParts.size(), lstDeleteParts.size());
        try {
            mutUTransaction.begin();
            for (MSHOutPart ip : lstAddParts) {
                ip.setMailId(mail.getId());
                File f = StorageUtils.getFile(ip.getFilepath());
                ip.setSize(BigInteger.valueOf(f.length()));
                ip.setSha256Value(DigestUtils.getBase64Sha256Digest(f));
                if (Utils.isEmptyString(ip.getEbmsId())) {
                    ip.setEbmsId(Utils.getUUIDWithLocalDomain());
                }
                memEManager.persist(ip);
            }

            for (MSHOutPart ip : lstUpdateParts) {
                ip.setMailId(mail.getId());
                File f = StorageUtils.getFile(ip.getFilepath());
                ip.setSize(BigInteger.valueOf(f.length()));
                ip.setSha256Value(DigestUtils.getBase64Sha256Digest(f));
                if (Utils.isEmptyString(ip.getEbmsId())) {
                    ip.setEbmsId(Utils.getUUIDWithLocalDomain());
                }
                if (Utils.isEmptyString(ip.getEbmsId())) {
                    ip.setEbmsId(Utils.getUUIDWithLocalDomain());
                }
                memEManager.merge(ip);
            }

            for (MSHOutPart ip : lstDeleteParts) {
                ip.setMailId(mail.getId());
                memEManager.remove(l);
            }

        } catch (NotSupportedException | SystemException ex) {
            String msg = String.format(MSG_ERR_PESIST_PARTS, strMsg, mail.getClass().
                    getName(), mail.getId(), ex.getMessage());
            LOG.logError(l, msg, null);
            try {
                mutUTransaction.rollback();
            } catch (IllegalStateException | SecurityException | SystemException ex1) {
                LOG.logWarn(l, ex1.getMessage(), ex1);
            }
            throw new StorageException(msg, ex);

        }

        // if merge cause locking 
        mail.setStatusDate(Calendar.getInstance().getTime());
        mail.setStatus(status.getValue());

        Query updq = memEManager.createNamedQuery(SEDNamedQueries.UPDATE_OUTMAIL);
        updq.setParameter(SEDNamedQueries.QUERY_PARAM_ID, mail.getId());
        updq.setParameter(SEDNamedQueries.QUERY_PARAM_STATUS_DATE, mail.
                getStatusDate());
        updq.setParameter(SEDNamedQueries.QUERY_PARAM_STATUS, mail.getStatus());

        int iVal = updq.executeUpdate();
        if (iVal != 1) {
            try {
                mutUTransaction.rollback();
            } catch (IllegalStateException | SecurityException | SystemException ex1) {
                LOG.logWarn(l, ex1.getMessage(), ex1);
            }
            String msg = String.format(MSG_ERR_STATUS, mail.getStatus(), mail.
                    getClass().getName(), mail.getId(), iVal);
            LOG.logError(l, msg, null);
            throw new StorageException(msg, null);
        }

        // persist mail event
        MSHOutEvent me = new MSHOutEvent();
        me.setMailId(mail.getId());
        me.setSenderMessageId(mail.getSenderMessageId());
        me.setDescription(statusdesc + " " + strMsg);
        me.setStatus(mail.getStatus());
        me.setDate(mail.getStatusDate());
        me.setUserId(userId);
        me.setApplicationId(applicationId);
        try {
            memEManager.persist(me);
            mutUTransaction.commit();

            suc = true;
        } catch (SystemException | RollbackException | HeuristicMixedException
                | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            String msg = String.format(MSG_ERR_COMMIT_PARTS, strMsg, mail.getClass().
                    getName(), mail.getId(), ex.getMessage());
            LOG.logError(l, msg, null);
            try {
                mutUTransaction.rollback();
            } catch (IllegalStateException | SecurityException | SystemException ex1) {
                LOG.logWarn(l, MSG_ERR_ROLLBACK, ex1);
            }
            throw new StorageException(msg, ex);
        }

        return suc;

    }

    /**
     *
     * @param ad
     * @return
     */
    @Override
    public boolean addExecutionTask(SEDTaskExecution task) {
        if (!Utils.isEmptyString(task.getResult()) && task.getResult().length() > 1024) {
            String res = task.getResult();
            StringWriter sw = new StringWriter();
            sw.append("Too long string: '");
            sw.append(res);
            sw.append("'.  Message substr to 1024");
            LOG.logWarn(sw.toString(), null);
            task.setResult(res.substring(0, 1023));

        }
        boolean suc = false;
        try {
            add(task);
            suc = true;
        } catch (StorageException se) {
            StringWriter sw = new StringWriter();
            sw.append("Error occured while adding task. Error");
            sw.append(se.getMessage());
            sw.append(". Task: ");

            try {
                sw.append(XMLUtils.serializeToString(task));
            } catch (JAXBException ex) {
                sw.append("Task: " + task.getName());
                LOG.formatedWarning(
                        "Jaxb exception: %s for SEDTaskExecution for cron id: %d", ex.
                                getMessage(), task.getCronId());
            }
            LOG.logError(sw.toString(), se);
        }

        return suc;
    }

    /**
     *
     * @param <T>
     * @param type
     * @param searchParams
     * @param forCount
     * @param sortField
     * @param sortOrder
     * @return
     */
    protected <T, D> CriteriaQuery createSearchCriteria(Class<T> type,
            Object searchParams, Class<D> filterType,
            boolean forCount, String sortField, String sortOrder) {
        long l = LOG.logStart();
        CriteriaBuilder cb = memEManager.getCriteriaBuilder();
        CriteriaQuery cq = forCount ? cb.createQuery(Long.class
        ) : cb.createQuery(
                type);
        Root<T> om = cq.from(filterType == null ? type : filterType);
        if (forCount) {
            cq.select(cb.count(om));
        } else if (sortField != null) {
            if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
                cq.orderBy(cb.asc(om.get(sortField)));
            } else {
                cq.orderBy(cb.desc(om.get(sortField)));
            }
        } else {
            cq.orderBy(cb.desc(om.get("Id")));
        }
        List<Predicate> lstPredicate = new ArrayList<>();
        // set order by
        if (searchParams != null) {
            Class cls = searchParams.getClass();
            Method[] methodList = cls.getMethods();
            for (Method m : methodList) {
                // only getters (public, starts with get, no arguments)
                String mName = m.getName();
                if (Modifier.isPublic(m.getModifiers()) && m.getParameterCount() == 0
                        && !m.getReturnType().equals(Void.TYPE)
                        && (mName.startsWith("get") || mName.startsWith("is"))) {
                    String fieldName = mName.substring(mName.startsWith("get") ? 3 : 2);
                    // get returm parameter
                    Object searchValue;
                    try {
                        searchValue = m.invoke(searchParams, new Object[]{});
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        LOG.logError(l, ex);
                        continue;
                    }

                    if (searchValue == null) {
                        continue;
                    }

                    if (fieldName.endsWith("List") && searchValue instanceof List) {
                        LOG.formatedDebug("Set parameter list %s  for method %s",
                                searchValue, fieldName);
                        String property = fieldName.substring(0, fieldName.lastIndexOf(
                                "List"));
                        if (!((List) searchValue).isEmpty()) {
                            lstPredicate.add(om.get(property).in(
                                    ((List) searchValue).toArray()));
                        } else {
                            lstPredicate.add(om.get(property).isNull());
                        }
                    } else {
                        try {
                            cls.getMethod("set" + fieldName, new Class[]{m.getReturnType()});
                        } catch (NoSuchMethodException | SecurityException ex) {
                            // method does not have setter // ignore other methods
                            continue;
                        }

                        if (fieldName.endsWith("From") && searchValue instanceof Comparable) {
                            lstPredicate.add(cb.greaterThanOrEqualTo(
                                    om.get(fieldName.substring(0, fieldName.
                                            lastIndexOf("From"))),
                                    (Comparable) searchValue));
                            LOG.formatedDebug("Set interval parameter from %s  for method %s",
                                    searchValue, fieldName);
                        } else if (fieldName.endsWith("To") && searchValue instanceof Comparable) {
                            lstPredicate.add(cb.lessThan(
                                    om.
                                            get(fieldName.substring(0, fieldName.lastIndexOf(
                                                    "To"))),
                                    (Comparable) searchValue));
                            LOG.formatedDebug("Set interval parameter to %s  for method %s",
                                    searchValue, fieldName);
                        } else if (searchValue instanceof String) {
                            if (!((String) searchValue).isEmpty()) {
                                LOG.formatedDebug("Set parameter as String %s  for method %s",
                                        searchValue, fieldName);
                                lstPredicate.add(cb.equal(om.get(fieldName), searchValue));
                            }
                        } else if (searchValue instanceof BigInteger) {
                            LOG.formatedDebug("Set parameter as integer %s  for method %s",
                                    searchValue, fieldName);
                            lstPredicate.add(cb.equal(om.get(fieldName), searchValue));
                        } else {
                            LOG.formatedWarning("Unknown search value type %s for method %s! "
                                    + "Parameter is ignored!",
                                    searchValue, fieldName);
                        }
                    }
                }
            }
            if (!lstPredicate.isEmpty()) {
                Predicate[] tblPredicate = lstPredicate.stream().toArray(
                        Predicate[]::new);
                cq.where(cb.and(tblPredicate));
            }
        }

        return cq;
    }

    @Override
    public <T> List<T> getDataList(Class<T> type, String hql,
            Map<String, Object> params) {
        TypedQuery<T> q = memEManager.createQuery(hql, type);
        params.forEach((param, value) -> {
            q.setParameter(param, value);
        });
        return q.getResultList();
    }

    /**
     *
     * @param <T>
     * @param type
     * @param startingAt
     * @param maxResultCnt
     * @param sortField
     * @param sortOrder
     * @param filters
     * @return
     */
    @Override
    public <T> List<T> getDataList(Class<T> type, int startingAt, int maxResultCnt,
            String sortField,
            String sortOrder, Object filters) {

        return getDataList(type, startingAt, maxResultCnt, sortField, sortOrder,
                filters, null);
    }

    /**
     *
     * @param <T>
     * @param resultType
     * @param startingAt
     * @param maxResultCnt
     * @param sortField
     * @param sortOrder
     * @param filters
     * @param filterType
     * @return
     */
    @Override
    public <T, D> List<T> getDataList(Class<T> resultType, int startingAt,
            int maxResultCnt,
            String sortField,
            String sortOrder, Object filters, Class<D> filterType) {
        long l = LOG.logStart(resultType, startingAt, maxResultCnt, sortField,
                sortOrder,
                filters);
        List<T> lstResult;
        try {
            CriteriaQuery<T> cq = createSearchCriteria(resultType, filters, filterType,
                    false, sortField,
                    sortOrder);
            TypedQuery<T> q = memEManager.createQuery(cq);

            if (maxResultCnt > 0) {
                q.setMaxResults(maxResultCnt);
            }
            if (startingAt > 0) {
                q.setFirstResult(startingAt);
            }
            lstResult = q.getResultList();
        } catch (NoResultException ex) {
            lstResult = new ArrayList<>();
        }
        LOG.logEnd(l, resultType, startingAt, maxResultCnt, sortField, sortOrder,
                filters);
        return lstResult;
    }

    /**
     *
     * @param <T>
     * @param type
     * @param filters
     * @return
     */
    @Override
    public <T> long getDataListCount(Class<T> type, Object filters) {
        long l = LOG.logStart(type, filters);
        CriteriaQuery<Long> cqCount = createSearchCriteria(type, filters, null, true,
                null,
                null);
        Long res = memEManager.createQuery(cqCount).getSingleResult();
        LOG.logEnd(l, type, filters);
        return res;
    }

    /**
     *
     * @param action
     * @param convId
     * @return
     */
    @Override
    public List<MSHInMail> getInMailConvIdAndAction(String action, String convId) {
        long l = LOG.logStart(action, convId);
        Query q
                = memEManager.createNamedQuery(
                        "si.laurentius.msh.inbox.mail.MSHInMail.getByConvIdAndAction",
                        MSHInMail.class
                );
        q.setParameter("convId", convId);
        q.setParameter("action", action);
        List<MSHInMail> lst = q.getResultList();

        LOG.logEnd(l);
        return lst;
    }

    private String getJNDIPrefix() {

        return System.getProperty(SYS_PROP_JNDI_PREFIX, "java:/jboss/");
    }

    private String getJNDI_JMSPrefix() {
        return System.getProperty(SYS_PROP_JNDI_JMS_PREFIX, "java:/jms/");
    }

    /**
     *
     * @param cronId
     * @param type
     * @return
     */
    @Override
    public SEDTaskExecution getLastSuccesfullTaskExecution(BigInteger cronId,
            String type) {
        long l = LOG.logStart();
        SEDTaskExecution dt = null;

        TypedQuery<SEDTaskExecution> tq
                = memEManager.createNamedQuery(
                        "si.laurentius.cron.SEDTaskExecution.getByStatusAndTypeAndCronJobId",
                        SEDTaskExecution.class
                );

        tq.setParameter(SEDNamedQueries.QUERY_PARAM_STATUS, SEDTaskStatus.SUCCESS.
                getValue());
        tq.setParameter(SEDNamedQueries.QUERY_PARAM_TYPE, type);
        tq.setParameter(SEDNamedQueries.QUERY_PARAM_CRON_ID, cronId);

        tq.setMaxResults(1);
        try {
            dt = tq.getSingleResult();
        } catch (NoResultException ign) {
            LOG.logWarn(l, "No succesfull task execution for type: " + type, null);
        }

        return dt;

    }

    /**
     *
     * @param <T>
     * @param type
     * @param mailId
     * @return
     */
    @Override
    public <T> T getMailById(Class<T> type, BigInteger mailId) {
        long l = LOG.logStart(type, mailId);
        TypedQuery<T> tq = memEManager.createNamedQuery(type.getName() + ".getById",
                type);
        tq.setParameter("id", mailId);
        T result = tq.getSingleResult();

        LOG.logEnd(l);
        return result;
    }

    /**
     *
     * @param <T>
     * @param type
     * @param mailMessageId - ebms nessage id
     * @return
     */
    @Override
    public <T> List<T> getMailByMessageId(Class<T> type, String mailMessageId) {
        long l = LOG.logStart(type, mailMessageId);
        TypedQuery<T> tq = memEManager.createNamedQuery(
                type.getName() + ".getByMessageId", type);
        tq.setParameter("messageId", mailMessageId);
        List<T> result = tq.getResultList();
        LOG.logEnd(l);
        return result;
    }

    /**
     *
     * @param <T>
     * @param type
     * @param mailMessageId - ebms nessage id
     * @return
     */
    @Override
    public <T> List<T> getMailBySenderMessageId(Class<T> type,
            String mailMessageId) {
        long l = LOG.logStart(type, mailMessageId);
        TypedQuery<T> tq = memEManager.createNamedQuery(
                type.getName() + ".getBySenderMessageId", type);
        tq.setParameter("senderMessageId", mailMessageId);
        List<T> result = tq.getResultList();
        LOG.logEnd(l);
        return result;
    }

    /**
     *
     * @param <T>
     * @param type
     * @param mailId
     * @return
     */
    @Override
    public <T> List<T> getMailEventList(Class<T> type, BigInteger mailId) {
        long l = LOG.logStart(type, mailId);
        TypedQuery tq = memEManager.createNamedQuery(
                type.getName() + ".getMailEventList", type);
        tq.setParameter("mailId", mailId);
        List<T> mailEvents = tq.getResultList();
        LOG.logEnd(l, type);
        return mailEvents;
    }

    /**
     *
     * @param <T>
     * @param type
     * @param mailId
     * @return
     */
    @Override
    public <T> List<T> getMailPartList(Class<T> type, BigInteger mailId) {
        long l = LOG.logStart(type, mailId);
        TypedQuery tq = memEManager.createNamedQuery(
                type.getName() + ".getMailPartList", type);
        tq.setParameter("mailId", mailId);
        List<T> mailEvents = tq.getResultList();
        LOG.logEnd(l, type);
        return mailEvents;
    }

    /**
     *
     * @param bi
     * @throws StorageException
     */
    @Override
    public void removeInMail(BigInteger bi)
            throws StorageException {
        removeMail(MSHInMail.class,
                MSHInEvent.class,
                bi);
    }

    /**
     *
     * @param <T>
     * @param <E>
     * @param type
     * @param typeEvent
     * @param bi
     * @throws StorageException
     */
    public <T, E> void removeMail(Class<T> type, Class<E> typeEvent, BigInteger bi)
            throws StorageException {
        long l = LOG.logStart(type);
        T mail = getMailById(type, bi);
        try {
            mutUTransaction.begin();

            // remove events
            List<E> lst = getMailEventList(typeEvent, bi);
            lst.stream().forEach((e) -> {
                memEManager.remove(memEManager.contains(e) ? e : memEManager.merge(e));
            });
            // remove mail
            memEManager.remove(memEManager.contains(mail) ? mail : memEManager.merge(
                    mail));
            mutUTransaction.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException
                | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            {
                try {
                    mutUTransaction.rollback();
                } catch (IllegalStateException | SecurityException | SystemException ex1) {
                    LOG.logWarn(l, MSG_ERR_ROLLBACK, ex);
                }
                String msg = String.format(MSG_ERR_REMOVE, type.getName(), bi, ex.
                        getMessage());
                LOG.logError(l, msg, null);
                throw new StorageException(msg, ex);
            }
        }

        LOG.logEnd(l);
    }

    /**
     *
     * @param bi
     * @throws StorageException
     */
    @Override
    public void removeOutMail(BigInteger bi)
            throws StorageException {
        removeMail(MSHOutMail.class,
                MSHOutEvent.class,
                bi);
    }

    @Override
    public void sendOutMessage(MSHOutMail mail, int retry, long delay,
            int iPriority,
            String userId,
            String applicationId)
            throws StorageException {
        long l = LOG.logStart();

        Date dt = sendOutMessage(mail.getId(), SEDOutboxMailStatus.SCHEDULE, retry,
                delay, iPriority, userId, applicationId);
        // update values
        mail.setStatusDate(dt);
        mail.setStatus(SEDOutboxMailStatus.SCHEDULE.getValue());

    }

    @Override
    public Date sendOutMessage(BigInteger id, SEDOutboxMailStatus status,
            int retry, long delay, int iPriority,
            String userId,
            String applicationId)
            throws StorageException {
        long l = LOG.logStart();

        // prepare mail to persist
        Date dtStatus = Calendar.getInstance().getTime();

        // --------------------
        // serialize data and submit message
        String msgFactoryJndiName = getJNDIPrefix() + SEDValues.EBMS_JMS_CONNECTION_FACTORY_JNDI;
        String msgQueueJndiName = getJNDI_JMSPrefix() + SEDValues.JNDI_QUEUE_EBMS;
        iPriority = iPriority > 9 ? 9 : (iPriority < 0 ? 0 : iPriority);
        Connection connection = null;

        String msgDesc = String.format(
                "Add mail to submit queue. Retry %d, delay %d ms", retry, delay);

        try {
            // create JMS session

            ConnectionFactory cf = InitialContext.doLookup(
                    msgFactoryJndiName);
            if (mqMSHQueue == null) {
                mqMSHQueue = InitialContext.doLookup(msgQueueJndiName);
            }
            connection = cf.createConnection();
        } catch (NamingException | JMSException ex) {
            String msg = String.format(MSG_ERR_JMS_SUBMIT, id, msgQueueJndiName, ex.
                    getMessage());
            LOG.logError(l, msg, ex);
            throw new StorageException(msg, ex);
        }

        Message message = null;
        try (Session session = connection.createSession(true,
                Session.SESSION_TRANSACTED);
                MessageProducer sender = session.createProducer(mqMSHQueue);) {

            message = session.createMessage();
            message.setLongProperty(SEDValues.EBMS_QUEUE_PARAM_MAIL_ID,
                    id.longValue());
            // problem for duplicate detection on resending 
            //message.setStringProperty(SEDValues.EBMS_QUEUE_DUPLICATE_DETECTION_ID_Artemis,  mail.getId().toString());
            message.setIntProperty(SEDValues.EBMS_QUEUE_PARAM_RETRY, retry);
            message.setLongProperty(SEDValues.EBMS_QUEUE_PARAM_DELAY, delay);
            message.setLongProperty(SEDValues.EBMS_QUEUE_DELAY_AMQ, delay);
            message.setLongProperty(SEDValues.EBMS_QUEUE_DELAY_Artemis,
                    delay + System.currentTimeMillis());

            Query updq = memEManager.createNamedQuery(SEDNamedQueries.UPDATE_OUTMAIL);
            updq.setParameter("id", id);
            updq.setParameter("statusDate", dtStatus);
            updq.setParameter("status", status.getValue());

            MSHOutEvent me = new MSHOutEvent();
            me.setMailId(id);
            me.setDescription(msgDesc);
            me.setStatus(status.getValue());
            me.setDate(dtStatus);

            me.setUserId(userId);
            me.setApplicationId(applicationId);
            // create message

            mutUTransaction.begin();
            int iVal = updq.executeUpdate();
            if (iVal != 1) {
                try {
                    mutUTransaction.rollback();
                } catch (IllegalStateException | SecurityException | SystemException ex1) {
                    LOG.logWarn(l, ex1.getMessage(), ex1);
                }
                String msg
                        = "Status not setted to MSHOutMail:" + id + " result: '" + iVal
                        + "'. Mail not exists or id duplicates?";
                LOG.logError(l, msg, null);
                throw new StorageException(msg, null);
            }

            memEManager.persist(me);
            // deadlocks  TODO
            //sender.send(message);
            mutUTransaction.commit();
            // transaction is not working TODO!!!
            sender.setPriority(iPriority);
            sender.send(message);
            session.commit();

            LOG.formatedlog(
                    "Message %d added to send queue with params: retry %d, delay %d",
                    id.longValue(), retry, delay);

        } catch (JMSException | NotSupportedException | SystemException | RollbackException | HeuristicMixedException
                | HeuristicRollbackException | SecurityException | IllegalStateException ex) {

            if (mutUTransaction != null) {
                try {

                    mutUTransaction.rollback();

                } catch (IllegalStateException | SecurityException | SystemException ex1) {
                    LOG.logWarn(l, "Error rollback transaction", ex1);
                }
            }

            String msg
                    = "Error sending mail : '" + id + "'! Err:" + ex.
                            getMessage();
            LOG.logError(l, msg, ex);
            throw new StorageException(msg, ex);

        } finally {
            try {
                connection.close();
            } catch (JMSException jmse) {
                LOG.logWarn(l, "Error closing connection JSM session", jmse);

            }
        }
        return dtStatus;

    }

    /**
     *
     * @param mail
     * @param applicationId
     * @throws StorageException
     */
    @Override
    public void serializeInMail(MSHInMail mail, String applicationId)
            throws StorageException {
        long l = LOG.logStart();
        try {

            validateAndPrepareInMailToStore(mail);

            Date dt = Calendar.getInstance().getTime();

            if (Utils.isEmptyString(mail.getStatus())) {
                mail.setStatus(SEDInboxMailStatus.RECEIVED.getValue());
                mail.setReceivedDate(dt);
                mail.setStatusDate(dt);
            }

            if (mail.getStatusDate() == null) {
                mail.setStatusDate(dt);
            }

            if (mail.getReceivedDate() == null) {
                mail.setReceivedDate(dt);
            }

            mutUTransaction.begin();
            // persist mail
            memEManager.persist(mail);

            if (mail.getMSHInPayload() != null) {
                for (MSHInPart mp : mail.getMSHInPayload().getMSHInParts()) {
                    mp.setMailId(mail.getId());
                    memEManager.persist(mp);
                }
            }
            // persist mail event
            MSHInEvent me = new MSHInEvent();
            me.setMailId(mail.getId());
            me.setStatus(mail.getStatus());
            me.setDescription("Mail stored to inbox");
            me.setApplicationId(applicationId);
            me.setDate(mail.getStatusDate());

            memEManager.persist(me);
            mutUTransaction.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException
                | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            {
                try {
                    mutUTransaction.rollback();
                } catch (IllegalStateException | SecurityException | SystemException ex1) {
                    LOG.logWarn(l, "", ex);
                }
                String msg = "Error occurred on serializing mail! Err:" + ex.
                        getMessage();
                LOG.logError(l, msg, ex);
                throw new StorageException(msg, ex);
            }
        }
        LOG.logEnd(l);
    }

    /**
     * Method stores metadata for in and out mail in transaction. Message parts
     * must already exists in Laurentius store. Path in element "part/filepath"
     * must be relative path in laurentius store.Purpose of method is to store
     * incomming mail and its coresponding out message
     *
     * <ul>
     * <li>set ebms messageId if null</li>
     * <li>set conversation id as mail id if null</li>
     * <li>set ebms sender name from senderbox if null </li>
     * <li>set ebms receiver name from receiverbox if null </li>
     * <li>serialize payload to laurentius store:
     * <ul>
     * <li>sets ebms part messageId if null</li>
     * <li>sets part's sha256 and size</li>
     * <li>sets mimetype from filname if null</li>
     * </ul></li>
     * <li>sets status to submitted</li>
     * <li><b>Add mail to submit queue</b></li>
     * </ul>
     *
     * @param inMail
     * @param outMail
     * @param applicationId
     * @param pmode
     * @throws StorageException
     */
    @Override
    public void serializeInOutMail(MSHInMail inMail, MSHOutMail outMail,
            String applicationId,
            PMode pmode)
            throws StorageException {

        long l = LOG.logStart();

        String locadomain = SEDSystemProperties.getLocalDomain();

        if (pmode == null) {
            try {
                pmode = mpModeManager.getPModeMSHOutMail(outMail);
            } catch (PModeException ex) {
                throw new StorageException(ex.getMessage(), ex);
            }
        }
        int iPriority = pmode.getPriority() == null ? 4 : pmode.getPriority();
        iPriority = iPriority > 9 ? 9 : (iPriority < 0 ? 0 : iPriority);
        Date storeDate = Calendar.getInstance().getTime();

        //------------------------------------------------------------
        // prepare in mail:
        validateAndPrepareInMailToStore(inMail);

        if (Utils.isEmptyString(inMail.getStatus())) {
            inMail.setStatus(SEDInboxMailStatus.RECEIVED.getValue());
            inMail.setReceivedDate(storeDate);
            inMail.setStatusDate(storeDate);
        }

        if (inMail.getStatusDate() == null) {
            inMail.setStatusDate(storeDate);
        }

        if (inMail.getReceivedDate() == null) {
            inMail.setReceivedDate(storeDate);
        }

        // persist mail event
        MSHInEvent eventInMail = new MSHInEvent();

        eventInMail.setStatus(inMail.getStatus());
        eventInMail.setDescription("Mail stored to inbox");
        eventInMail.setApplicationId(applicationId);
        eventInMail.setDate(inMail.getStatusDate());

        //------------------------------------------------------------
        // prepare out mail:
        validateAndPrepareOutMailToStore(outMail);
        outMail.setStatus(SEDOutboxMailStatus.SUBMITTED.getValue());
        outMail.setStatusDate(storeDate);
        outMail.setSubmittedDate(storeDate);

        // create out event
        MSHOutEvent eventOutMail = new MSHOutEvent();
        eventOutMail.setDescription("Mail stored to outbox.");
        eventOutMail.setStatus(outMail.getStatus());
        eventOutMail.setDate(storeDate);
        eventOutMail.setSenderMessageId(outMail.getSenderMessageId());
        eventOutMail.setUserId("");
        eventOutMail.setApplicationId(applicationId);

        try {

            mutUTransaction.begin();

            // ----------------------------------------------
            // persist in mail
            memEManager.persist(inMail);

            if (inMail.getMSHInPayload() != null) {
                for (MSHInPart mp : inMail.getMSHInPayload().getMSHInParts()) {
                    mp.setMailId(inMail.getId());
                    memEManager.persist(mp);
                }
            }
            // save event
            eventInMail.setMailId(inMail.getId());
            memEManager.persist(eventInMail);

            // ----------------------------------------------
            // persist out mail
            memEManager.persist(outMail);
            // update payload
            if (outMail.getMSHOutPayload() != null) {
                for (MSHOutPart mp : outMail.getMSHOutPayload().getMSHOutParts()) {
                    mp.setMailId(outMail.getId());
                    memEManager.persist(mp);
                }
            }
            // set conversation id
            if (Utils.isEmptyString(outMail.getConversationId())) {
                outMail.setConversationId(outMail.getId().toString() + "@" + locadomain);
                memEManager.merge(outMail);
            }
            // set event mail id
            eventOutMail.setMailId(outMail.getId());
            memEManager.persist(eventOutMail);

            mutUTransaction.commit();

            // --------------------------------------
            // sumbmit out mail to queue
            sendOutMessage(outMail, 0, 0, iPriority, "", applicationId);

        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException
                | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            {
                try {
                    mutUTransaction.rollback();
                } catch (IllegalStateException | SecurityException | SystemException ex1) {
                    LOG.logWarn(l, "", ex);
                }
                String msg = "Error occurred on serializing mail! Err:" + ex.
                        getMessage();
                LOG.logError(l, msg, ex);
                throw new StorageException(msg, ex);
            }
        }

        LOG.logEnd(l);
    }

    private void validateAndPrepareOutMailToStore(MSHOutMail mail) throws StorageException {

        // set message id
        if (Utils.isEmptyString(mail.getMessageId())) {
            mail.setMessageId(Utils.getUUIDWithLocalDomain());
        }

        // set sender name
        if (Utils.isEmptyString(mail.getSenderName())) {
            mail.setSenderName(mail.getSenderEBox());
        }

        // set receiver id
        if (Utils.isEmptyString(mail.getReceiverName())) {
            mail.setReceiverName(mail.getReceiverEBox());
        }

        // check mail parts
        if (mail.getMSHOutPayload() != null) {
            for (MSHOutPart mp : mail.getMSHOutPayload().getMSHOutParts()) {
                if (Utils.isEmptyString(mp.getEbmsId())) {
                    mp.setEbmsId(Utils.getUUIDWithLocalDomain());
                }
                File f = StorageUtils.getFile(mp.getFilepath());
                if (!f.exists()) {
                    String msg = String.format(
                            "Mail part: %s (path %s) does not exist in storage!", mp.
                                    getName(), mp.getFilepath());
                    throw new StorageException(msg);
                }
                mp.setSize(BigInteger.valueOf(f.length()));
                String digest = DigestUtils.getBase64Sha256Digest(f);
                if (Utils.isEmptyString(mp.getSha256Value())) {
                    mp.setSha256Value(digest);
                } else if (!mp.getSha256Value().equals(digest)) {
                    mp.setSha256Value(digest);
                    LOG.formatedWarning(
                            "Mail has wrong payload part digest (sender: %s, service %s, senderMessageId: %s, filepart %s) "
                            + "digest: %s, expected digest %s!",
                            mail.getSenderEBox(), mail.getService(), mail.
                            getSenderMessageId(), mp.getFilepath(), mp.getSha256Value(),
                            digest);
                }

                if (Utils.isEmptyString(mp.getMimeType())) {
                    mp.setMimeType(MimeValue.getMimeTypeByFileName(f.getName()));
                }
                if (Utils.isEmptyString(mp.getSource())) {
                    mp.setSource(SEDMailPartSource.MAIL.getValue());
                }
            }
        } else {
            LOG.formatedWarning(
                    "Serialize mail (sender: %s, service %s, senderMessageId: %s ) with no payload!",
                    mail.getSenderEBox(), mail.getService(), mail.
                    getSenderMessageId());
        }

    }

    private void validateAndPrepareInMailToStore(MSHInMail mail) throws StorageException {
        long l = LOG.logStart();

        if (Utils.isEmptyString(mail.getMessageId())) {
            String msg = "Missing mail ebms Id";
            throw new StorageException(msg);
        }
        if (Utils.isEmptyString(mail.getSenderEBox())) {
            String msg = String.format(
                    "Missing SenderEBox for mail (ebmsId)  %s", mail.getMessageId());
            throw new StorageException(msg);
        }
        if (Utils.isEmptyString(mail.getReceiverEBox())) {
            String msg = String.format(
                    "Missing ReceiverEBox for mail (ebmsId)  %s", mail.
                            getMessageId());
            throw new StorageException(msg);
        }

        if (Utils.isEmptyString(mail.getConversationId())) {
            String msg = String.format(
                    "Missing ConversationId for mail (ebmsId)  %s", mail.
                            getMessageId());
            throw new StorageException(msg);
        }
        if (Utils.isEmptyString(mail.getService())) {
            String msg = String.format(
                    "Missing Service for mail (ebmsId)  %s", mail.getMessageId());
            throw new StorageException(msg);
        }

        if (Utils.isEmptyString(mail.getAction())) {
            String msg = String.format(
                    "Missing Action for mail (ebmsId)  %s", mail.getMessageId());
            throw new StorageException(msg);
        }

        // check mail parts
        if (mail.getMSHInPayload() != null) {
            for (MSHInPart mp : mail.getMSHInPayload().getMSHInParts()) {
                if (Utils.isEmptyString(mp.getEbmsId())) {

                    String msg = String.format(
                            "Missing Payload part's ebmsId (name %s, path %s)!", mp.
                                    getName(), mp.getFilepath());
                    throw new StorageException(msg);
                }

                File f = StorageUtils.getFile(mp.getFilepath());
                if (!f.exists()) {
                    String msg = String.format(
                            "Mail part: %s (path %s) does not exist in storage!", mp.
                                    getName(), mp.getFilepath());
                    throw new StorageException(msg);
                }
                if (mp.getSize() == null || mp.getSize().longValue() != f.length()) {

                    LOG.formatedWarning(
                            "Mail has wrong payload part size (sender: %s, service %s, senderMessageId: %s, filepart %s). New value setted!"
                            + "expected size from payload: %d, file size %d!",
                            mail.getSenderEBox(), mail.getService(), mail.
                            getSenderMessageId(), mp.getEbmsId(),
                            mp.getSize() == null ? 0 : mp.
                            getSize().longValue(), f.length());
                    mp.setSize(BigInteger.valueOf(f.length()));
                }

                String digest = DigestUtils.getBase64Sha256Digest(f);
                if (Utils.isEmptyString(mp.getSha256Value())) {
                    mp.setSha256Value(digest);
                } else if (!mp.getSha256Value().equals(digest)) {
                    mp.setSha256Value(digest);
                    LOG.formatedWarning(
                            "Mail has wrong payload part digest (sender: %s, service %s, senderMessageId: %s, filepart %s) "
                            + "digest: %s, expected digest %s!",
                            mail.getSenderEBox(), mail.getService(), mail.
                            getSenderMessageId(), mp.getFilepath(), mp.getSha256Value(),
                            digest);
                }

                if (Utils.isEmptyString(mp.getMimeType())) {
                    mp.setMimeType(MimeValue.getMimeTypeByFileName(f.getName()));
                }
                if (Utils.isEmptyString(mp.getSource())) {
                    mp.setSource(SEDMailPartSource.MAIL.getValue());
                }
            }
        } else {
            LOG.formatedWarning(
                    "Serialize mail (sender: %s, service %s, senderMessageId: %s ) with no payload!",
                    mail.getSenderEBox(), mail.getService(), mail.
                    getSenderMessageId());
        }
        LOG.logEnd(l);

    }

    /**
     * Method stores metadata for out mail. Message parts must already exists in
     * Laurentius store. Path in element "part/filepath" must be relative path
     * in laurentius store.
     * <ul>
     * <li>set ebms messageId if null</li>
     * <li>set conversation id as mail id if null</li>
     * <li>set ebms sender name from senderbox if null </li>
     * <li>set ebms receiver name from receiverbox if null </li>
     * <li>serialize payload to laurentius store:
     * <ul>
     * <li>sets ebms part messageId if null</li>
     * <li>sets part's sha256 and size</li>
     * <li>sets mimetype from filname if null</li>
     * </ul></li>
     * <li>sets status to submitted</li>
     * <li><b>Add mail to submit queue</b></li>
     * </ul>
     *
     * @param mail
     * @param userID
     * @param applicationId
     * @param pmode
     * @throws StorageException
     */
    @Override
    public void serializeOutMail(MSHOutMail mail, String userID,
            String applicationId, PMode pmode)
            throws StorageException {
        long l = LOG.logStart();
        String locadomain = SEDSystemProperties.getLocalDomain();

        if (pmode == null) {
            try {
                pmode = mpModeManager.getPModeMSHOutMail(mail);
            } catch (PModeException ex) {
                throw new StorageException(ex.getMessage(), ex);
            }
        }

        try {
            validateAndPrepareOutMailToStore(mail);

            mail.setStatus(SEDOutboxMailStatus.SUBMITTED.getValue());
            mail.setStatusDate(Calendar.getInstance().getTime());
            mail.setSubmittedDate(mail.getStatusDate());

            mutUTransaction.begin();
            memEManager.persist(mail);

            if (mail.getMSHOutPayload() != null) {
                for (MSHOutPart mp : mail.getMSHOutPayload().getMSHOutParts()) {

                    mp.setMailId(mail.getId());
                    memEManager.persist(mp);
                }
            }

            if (Utils.isEmptyString(mail.getConversationId())) {
                mail.setConversationId(mail.getId().toString() + "@" + locadomain);
                memEManager.merge(mail);
            }

            // persist mail event
            MSHOutEvent me = new MSHOutEvent();
            me.setDescription("Mail composed in Laurentius-gui.");
            me.setMailId(mail.getId());
            me.setStatus(mail.getStatus());
            me.setDate(mail.getStatusDate());
            me.setSenderMessageId(mail.getSenderMessageId());
            me.setUserId(userID);
            me.setApplicationId(applicationId);
            memEManager.persist(me);

            mutUTransaction.commit();

            int iPriority = pmode.getPriority() == null ? 4 : pmode.getPriority();
            iPriority = iPriority > 9 ? 9 : (iPriority < 0 ? 0 : iPriority);
            // add message to queue
            sendOutMessage(mail, 0, 0, iPriority, userID, applicationId);
            //mJMS.sendMessage(mail.getId().longValue(), 0, 0, false);

        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException
                | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            {
                try {
                    mutUTransaction.rollback();
                } catch (IllegalStateException | SecurityException | SystemException ex1) {
                    LOG.logWarn(l, "", ex);
                }
                String msg = "Error occurred on serializing mail! Err:" + ex.
                        getMessage();
                LOG.logError(l, msg, ex);
                throw new StorageException(msg, ex);
            }
        }
        LOG.logEnd(l);

    }

    /**
     *
     * @param mail
     * @param status
     * @param desc
     * @throws StorageException
     */
    @Override
    public void setStatusToInMail(MSHInMail mail, SEDInboxMailStatus status,
            String desc)
            throws StorageException {
        setStatusToInMail(mail, status, desc, null, null, null, null);
    }

    /**
     *
     * @param mail
     * @param status
     * @param desc
     * @param userID
     * @param applicationId
     * @throws StorageException
     */
    @Override
    public void setStatusToInMail(MSHInMail mail, SEDInboxMailStatus status,
            String desc,
            String userID,
            String applicationId)
            throws StorageException {
        setStatusToInMail(mail, status, desc, userID, applicationId, null, null);

    }

    /**
     *
     * @param mail
     * @param status
     * @param desc
     * @param userID
     * @param applicationId
     * @param filePath
     * @param mime
     * @throws StorageException
     */
    @Override
    public void setStatusToInMail(MSHInMail mail, SEDInboxMailStatus status,
            String desc,
            String userID,
            String applicationId, String filePath, String mime)
            throws StorageException {
        long l = LOG.logStart();
        try {
            mutUTransaction.begin();

            mail.setStatusDate(Calendar.getInstance().getTime());
            mail.setStatus(status.getValue());

            Query updq = memEManager.createNamedQuery(SEDNamedQueries.UPDATE_INMAIL);
            updq.setParameter("id", mail.getId());
            updq.setParameter("statusDate", mail.getStatusDate());
            updq.setParameter("status", mail.getStatus());

            // limit desc.
            String strDsc = desc == null ? status.getDesc() : desc;
            strDsc = strDsc.length() >= 512 ? strDsc.substring(0, 512) : strDsc;
            // persist mail event
            MSHInEvent me = new MSHInEvent();
            me.setMailId(mail.getId());
            me.setDescription(strDsc);
            me.setStatus(mail.getStatus());
            me.setDate(mail.getStatusDate());
            me.setUserId(userID);
            me.setApplicationId(applicationId);
            me.setEvidenceFilepath(filePath);
            me.setEvidenceMimeType(mime);

            int iVal = updq.executeUpdate();
            if (iVal != 1) {
                try {
                    mutUTransaction.rollback();
                } catch (IllegalStateException | SecurityException | SystemException ex1) {
                    LOG.logWarn(l, ex1.getMessage(), ex1);
                }
                String msg
                        = "Status not setted to MSHInMail:" + mail.getId() + " result: '" + iVal
                        + "'. Mail not exists or id duplicates?";
                LOG.logError(l, msg, null);
                throw new StorageException(msg, null);
            }
            memEManager.persist(me);
            mutUTransaction.commit();

        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException
                | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            {
                try {
                    mutUTransaction.rollback();
                } catch (IllegalStateException | SecurityException | SystemException ex1) {
                    LOG.logWarn(l, ex1.getMessage(), ex1);
                }
                String msg
                        = "Error commiting status to incommingxmail: '" + mail.getId() + "'! Err:"
                        + ex.getMessage();
                LOG.logError(l, msg, ex);
                throw new StorageException(msg, ex);
            }
        }
        LOG.logEnd(l);

    }

    public Date setStatusToInMail(BigInteger id, SEDInboxMailStatus status,
            String desc,
            String userID,
            String applicationId, String filePath, String mime)
            throws StorageException {
        long l = LOG.logStart();
        Date dtStatus = Calendar.getInstance().getTime();
        String statusVal = status.getValue();

        try {
            mutUTransaction.begin();

            Query updq = memEManager.createNamedQuery(SEDNamedQueries.UPDATE_INMAIL);
            updq.setParameter("id", id);
            updq.setParameter("statusDate", dtStatus);
            updq.setParameter("status", statusVal);

            // limit desc.
            String strDsc = desc == null ? status.getDesc() : desc;
            strDsc = strDsc.length() >= 512 ? strDsc.substring(0, 512) : strDsc;
            // persist mail event
            MSHInEvent me = new MSHInEvent();
            me.setMailId(id);
            me.setDescription(strDsc);
            me.setStatus(statusVal);
            me.setDate(dtStatus);
            me.setUserId(userID);
            me.setApplicationId(applicationId);
            me.setEvidenceFilepath(filePath);
            me.setEvidenceMimeType(mime);

            int iVal = updq.executeUpdate();
            if (iVal != 1) {
                try {
                    mutUTransaction.rollback();
                } catch (IllegalStateException | SecurityException | SystemException ex1) {
                    LOG.logWarn(l, ex1.getMessage(), ex1);
                }
                String msg
                        = "Status not setted to MSHInMail:" + id + " result: '" + iVal
                        + "'. Mail not exists or id duplicates?";
                LOG.logError(l, msg, null);
                throw new StorageException(msg, null);
            }
            memEManager.persist(me);
            mutUTransaction.commit();

        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException
                | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            {
                try {
                    mutUTransaction.rollback();
                } catch (IllegalStateException | SecurityException | SystemException ex1) {
                    LOG.logWarn(l, ex1.getMessage(), ex1);
                }
                String msg
                        = "Error commiting status to incommingxmail: '" + id + "'! Err:"
                        + ex.getMessage();
                LOG.logError(l, msg, ex);
                throw new StorageException(msg, ex);
            }
        }
        LOG.logEnd(l);
        return dtStatus;

    }

    /**
     *
     * @param mail
     * @param status
     * @param desc
     * @throws StorageException
     */
    @Override
    public void setStatusToOutMail(MSHOutMail mail, SEDOutboxMailStatus status,
            String desc)
            throws StorageException {
        setStatusToOutMail(mail, status, desc, null, null);
    }

    /**
     *
     * @param mail
     * @param status
     * @param desc
     * @param userID
     * @param applicationId
     * @throws StorageException
     */
    @Override
    public void setStatusToOutMail(MSHOutMail mail, SEDOutboxMailStatus status,
            String desc,
            String userID, String applicationId)
            throws StorageException {
        setStatusToOutMail(mail, status, desc, userID, applicationId, null, null);

    }

    /**
     *
     * @param mail
     * @param status
     * @param desc
     * @param userID
     * @param applicationId
     * @param filePath
     * @param mime
     * @throws StorageException
     */
    @Override
    public void setStatusToOutMail(MSHOutMail mail, SEDOutboxMailStatus status,
            String desc,
            String userID,
            String applicationId,
            String filePath,
            String mime)
            throws StorageException {
        long l = LOG.logStart();

        Date dt = setStatusToOutMail(mail.getId(), mail.getSenderMessageId(), mail.
                getSentDate(), mail.getReceivedDate(),
                mail.getDeliveredDate(), status, desc, userID, applicationId,
                filePath, mime);

        mail.setStatusDate(dt);
        mail.setStatus(status.getValue());

        LOG.logEnd(l);

    }

    @Override
    public Date setStatusToOutMail(BigInteger id, String senderMessageID,
            Date sentDate,
            Date receivedDate,
            Date deliveredDate,
            SEDOutboxMailStatus status,
            String desc,
            String userID,
            String applicationId, String filePath, String mime)
            throws StorageException {
        long l = LOG.logStart();

        Date dtStatus = null;
        try {
            dtStatus = Calendar.getInstance().getTime();
            String newStatusValue = status.getValue();

            mutUTransaction.begin();
            
            Query updq = memEManager.createNamedQuery(SEDNamedQueries.UPDATE_OUTMAIL);
            updq.setParameter("id", id);
            updq.setParameter("statusDate", dtStatus);
            updq.setParameter("status", newStatusValue);

            Query updqSD = null;
            if (SEDOutboxMailStatus.SENT.getValue().equals(status.getValue())
                    && sentDate != null) {
                updqSD = memEManager.createNamedQuery(
                        SEDNamedQueries.UPDATE_OUTMAIL_SENT_DATE);
                updqSD.setParameter("id", id);
                updqSD.setParameter("sentDate", sentDate);
                updqSD.setParameter("receivedDate",
                        receivedDate == null ? ""
                                : receivedDate);
            } else if (SEDOutboxMailStatus.DELIVERED.getValue().equals(status.
                    getValue())) {
                updqSD = memEManager.createNamedQuery(
                        SEDNamedQueries.UPDATE_OUTMAIL_DELIVERED_DATE);
                updqSD.setParameter("id", id);
                updqSD.setParameter("deliveredDate", deliveredDate);
            }

            // persist mail event
            MSHOutEvent me = new MSHOutEvent();
            me.setMailId(id);
            me.setDescription(desc == null ? status.getDesc()
                    : (desc.length() > 512 ? desc.substring(0, 508) + "..." : desc));
            me.setStatus(newStatusValue);
            me.setDate(dtStatus);
            me.setSenderMessageId(senderMessageID);
            me.setUserId(userID);
            me.setApplicationId(applicationId);
            me.setEvidenceFilepath(filePath);
            me.setEvidenceMimeType(mime);
            
            

            int iVal = updq.executeUpdate();

            if (iVal != 1) {
                try {
                    mutUTransaction.rollback();
                } catch (IllegalStateException | SecurityException | SystemException ex1) {
                    LOG.logWarn(l, ex1.getMessage(), ex1);
                }
                String msg
                        = "Status not setted to MSHOutMail:" + id + " result: '" + iVal
                        + "'. Mail not exists or id duplicates?";
                LOG.logError(l, msg, null);
                throw new StorageException(msg, null);
            }
            if (updqSD != null) {
                updqSD.executeUpdate();
            }
            memEManager.persist(me);
            mutUTransaction.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException
                | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            {
                try {
                    mutUTransaction.rollback();
                } catch (IllegalStateException | SecurityException | SystemException ex1) {
                    LOG.logWarn(l, ex1.getMessage(), ex1);
                }
                String msg
                        = "Error commiting status to outboxmail: '" + id + "'! Err:" + ex.
                                getMessage();
                LOG.logError(l, msg, ex);
                throw new StorageException(msg, ex);
            }
        }
        LOG.logEnd(l);
        return dtStatus;

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
            suc = true;
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException
                | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            try {
                LOG.logError(l, ex.getMessage(), ex);
                mutUTransaction.rollback();
            } catch (IllegalStateException | SecurityException | SystemException ex1) {
                LOG.logWarn(l, MSG_ERR_ROLLBACK, ex1);
            }
        }
        return suc;
    }

    ;

  /**
   *
   * @param ad
   * @return
   */
  @Override
    public boolean updateExecutionTask(SEDTaskExecution ad) {
        if (!Utils.isEmptyString(ad.getResult()) && ad.getResult().length() > 1024) {
            String res = ad.getResult();
            StringWriter sw = new StringWriter();
            sw.append("Too long string: '");
            sw.append(res);
            sw.append("'.  Message substr to 1024");
            LOG.logWarn(sw.toString(), null);
            ad.setResult(res.substring(0, 1023));

        }
        return update(ad);
    }

    /**
     *
     * @param mail
     * @param statusDesc
     * @param user
     * @throws StorageException
     */
    @Override
    public void updateInMail(MSHInMail mail, String statusDesc, String user)
            throws StorageException {
        long l = LOG.logStart();
        // --------------------
        // serialize data to db
        try {

            mutUTransaction.begin();
            // persist mail event
            MSHInEvent me = new MSHInEvent();
            me.setMailId(mail.getId());
            me.setStatus(mail.getStatus());
            me.setDescription(statusDesc);
            me.setUserId(user);
            me.setDate(mail.getStatusDate());
            // persist mail
            memEManager.merge(mail);
            memEManager.persist(me);
            mutUTransaction.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException
                | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            {
                try {
                    mutUTransaction.rollback();
                } catch (IllegalStateException | SecurityException | SystemException ex1) {
                    LOG.logWarn(l, "", ex);
                }
                String msg
                        = "Error occurred on update in mail: '" + mail.getId() + "'! Err:" + ex.
                        getMessage();
                LOG.logError(l, msg, ex);
                throw new StorageException(msg, ex);
            }
        }
        LOG.logEnd(l);
    }

    @Override
    public void updateOutMail(MSHOutMail mail, String statusDesc, String user)
            throws StorageException {
        long l = LOG.logStart();
        // --------------------
        // serialize data to db
        try {

            mutUTransaction.begin();
            // persist mail event
            MSHOutEvent me = new MSHOutEvent();
            me.setMailId(mail.getId());
            me.setStatus(mail.getStatus());
            me.setDescription(statusDesc);
            me.setUserId(user);
            me.setDate(mail.getStatusDate());
            // persist mail
            memEManager.merge(mail);
            memEManager.persist(me);
            mutUTransaction.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException
                | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            {
                try {
                    mutUTransaction.rollback();
                } catch (IllegalStateException | SecurityException | SystemException ex1) {
                    LOG.logWarn(l, "", ex);
                }
                String msg
                        = "Error occurred on update in mail: '" + mail.getId() + "'! Err:" + ex.
                        getMessage();
                LOG.logError(l, msg, ex);
                throw new StorageException(msg, ex);
            }
        }
        LOG.logEnd(l);
    }

    /**
     * Method returs root cause message.
     *
     * @param ex
     * @return message of last caused message;
     */
    protected String getExceptioRootCase(Throwable ex) {
        if (ex == null) {
            return null;
        }
        if (ex.getCause() != null) {
            return getExceptioRootCase(ex.getCause());
        }

        return ex.getClass().getName() + ":" + ex.getMessage();

    }

}
