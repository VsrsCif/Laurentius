/*
 * Copyright 2015, Supreme Court Republic of Slovenia
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
package si.laurentius.msh.ws;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.interceptor.Interceptors;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jws.WebService;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import si.laurentius.msh.pmode.PMode;
import si.laurentius.msh.pmode.PartyIdentitySet;
import si.laurentius.msh.pmode.Service;
import si.laurentius.GetInMailRequest;
import si.laurentius.GetInMailResponse;
import si.laurentius.InMailEventListRequest;
import si.laurentius.InMailEventListResponse;
import si.laurentius.InMailListRequest;
import si.laurentius.InMailListResponse;
import si.laurentius.ModifyInMailRequest;
import si.laurentius.ModifyInMailResponse;
import si.laurentius.ModifyOutMailRequest;
import si.laurentius.ModifyOutMailResponse;
import si.laurentius.OutMailEventListRequest;
import si.laurentius.OutMailEventListResponse;
import si.laurentius.OutMailListRequest;
import si.laurentius.OutMailListResponse;
import si.laurentius.SEDException;
import si.laurentius.SEDExceptionCode;
import si.laurentius.SEDException_Exception;
import si.laurentius.SEDMailBoxWS;
import si.laurentius.SubmitMailRequest;
import si.laurentius.SubmitMailResponse;
import si.laurentius.application.SEDApplication;
import si.laurentius.control.Control;
import si.laurentius.ebox.SEDBox;
import si.laurentius.inbox.event.InEvent;
import si.laurentius.inbox.mail.InMail;
import si.laurentius.inbox.payload.InPart;
import si.laurentius.outbox.event.OutEvent;
import si.laurentius.outbox.mail.OutMail;
import si.laurentius.outbox.payload.OutPart;
import si.laurentius.rcontrol.RControl;
import si.laurentius.commons.enums.SEDInboxMailStatus;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.enums.SEDOutboxMailStatus;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.SEDValues;
import si.laurentius.commons.exception.PModeException;
import si.laurentius.commons.exception.SVEVReturnValue;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.pmode.PModeUtils;
import si.laurentius.commons.pmode.enums.ActionRole;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.Utils;
import si.laurentius.lce.DigestUtils;
import si.laurentius.msh.pmode.Action;
import si.laurentius.msh.ws.utils.SEDRequestUtils;

/**
 *
 * @author Jože Rihtaršič
 */
@Interceptors(JEELogInterceptor.class)
@WebService(serviceName = "mailbox", portName = "SEDMailBoxWSPort",
        endpointInterface = "si.laurentius.SEDMailBoxWS", targetNamespace = "http://laurentius.si",
        wsdlLocation = "WEB-INF/wsdl/mailbox.wsdl")
public class SEDMailBox implements SEDMailBoxWS {

  private static final String MSG_ERR_MISSING_ATTR = "%s is required attribute";
  private static final String MSG_ERR_MISSING_MAIL = "Mail with id '%s' and  ebox: %s not exists!";
  private static final String MSG_ERR_SENDER_BOX = "Sender box [SubmitMailRequest/Data/OutMail/@senderEBox]: ";
  private static final String MSG_ERR_QUEUE_CONF = "Error occured while submiting mail to ebms queue. Check queue configuration: factory: '%s' queue: '%s'.";

  private static final String PARAMETER_RECEIVER_EBOX = "ReceiverEBox";

  private static final SEDLogger LOG = new SEDLogger(SEDMailBox.class);

  /**
   *
   */
  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  protected SEDLookupsInterface mdbLookups;

  /**
   *
   */
  @PersistenceContext(unitName = "ebMS_PU", name = "ebMS_PU")
  protected EntityManager memEManager;

  @EJB(mappedName = SEDJNDI.JNDI_PMODE)
  protected PModeInterface mpModeManager = null;

  /**
   *
   */
  protected Queue mqMSHQueue = null;
  SimpleDateFormat msdfDDMMYYYYHHMMSS = new SimpleDateFormat(
          "dd.MM.yyyy HH:mm:ss");
  StorageUtils msuStorageUtils = new StorageUtils();

  /**
   *
   */
  @Resource
  protected UserTransaction mutUTransaction;
  @Resource
  protected WebServiceContext mwsCtxt;

  /**
   *
   * @param con
   */
  public String getAuthenticatedPrincipal() {
    return mwsCtxt != null && mwsCtxt.getUserPrincipal() != null ? mwsCtxt.
            getUserPrincipal().
            getName() : null;
  }

  protected void closeConnection(Connection con) {
    try {
      if (con != null) {
        con.close();
      }
    } catch (JMSException jmse) {
      LOG.formatedWarning("Error occured while closing connection %s", jmse.
              getMessage());
    }
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
  protected <T> CriteriaQuery createSearchCriteria(Class<T> type,
          Object searchParams,
          boolean forCount, String sortField, String sortOrder) {
    long l = LOG.logStart();
    CriteriaBuilder cb = memEManager.getCriteriaBuilder();
    CriteriaQuery cq = forCount ? cb.createQuery(Long.class) : cb.createQuery(
            type);
    Root<T> om = cq.from(type);
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

  /**
   *
   * @return
   */
  protected String getCurrrentRemoteIP() {
    String clientIP = null;
    if (mwsCtxt != null && mwsCtxt.getMessageContext() != null) {
      MessageContext msgCtxt = mwsCtxt.getMessageContext();
      HttpServletRequest req = (HttpServletRequest) msgCtxt.get(
              MessageContext.SERVLET_REQUEST);
      clientIP = req.getRemoteAddr();
    } else {
      LOG.log("WebServiceContext is null! Can't get client's IP. ");
    }
    return clientIP;
  }

  /**
   *
   * @param param
   * @return
   * @throws SEDException_Exception
   */
  @Override
  public GetInMailResponse getInMail(GetInMailRequest param)
          throws SEDException_Exception {

    if (param == null) {
      throw SEDRequestUtils.
              createSEDException("Empty request: GetInMailRequest",
                      SEDExceptionCode.MISSING_DATA);
    }
    // validate control
    Control c = param.getControl();
    validateControl(c);

    // validate data
    GetInMailRequest.Data dt = param.getData();
    if (dt == null) {
      throw SEDRequestUtils.createSEDException(
              "Empty data in request: GetInMailRequest/Data",
              SEDExceptionCode.MISSING_DATA);
    }

    if (dt.getReceiverEBox() == null) {
      throw SEDRequestUtils.createSEDException(
              String.format(MSG_ERR_MISSING_ATTR, PARAMETER_RECEIVER_EBOX),
              SEDExceptionCode.MISSING_DATA);
    }

    validatePrincipalBox(dt.getReceiverEBox());

    if (dt.getMailId() == null) {
      throw SEDRequestUtils.createSEDException(
              String.format(MSG_ERR_MISSING_ATTR, "MailId"),
              SEDExceptionCode.MISSING_DATA);
    }
    // init response
    GetInMailResponse rsp = new GetInMailResponse();
    RControl rc = new RControl();
    rc.setReturnValue(SVEVReturnValue.OK.getValue());

    rsp.setRControl(rc);
    rsp.setRData(new GetInMailResponse.RData());

    InMail im;
    try {
      CriteriaBuilder cb = memEManager.getCriteriaBuilder();
      CriteriaQuery<InMail> cq = cb.createQuery(InMail.class);
      Root<InMail> om = cq.from(InMail.class);
      cq.where(cb.and(cb.equal(om.get(PARAMETER_RECEIVER_EBOX), dt.
              getReceiverEBox()),
              cb.equal(om.get("Id"), dt.getMailId())));

      TypedQuery<InMail> q = memEManager.createQuery(cq);
      im = q.getSingleResult();

      // set payload
      if (im.getInPayload() != null && !im.getInPayload().getInParts().isEmpty()) {
        for (InPart ip : im.getInPayload().getInParts()) {          
            ip.setBin(getContentStorageForFilePath(ip.getFilepath()));          
        }
      }

      rsp.getRData().setInMail(im);
    } catch (NoResultException ignore) {
      String message
              = String.format(MSG_ERR_MISSING_MAIL,
                      param.getData().getMailId()
                              .toString(), param.getData().getReceiverEBox());

      throw SEDRequestUtils.createSEDException(message,
              SEDExceptionCode.REQUIRED_DATA_NOT_EXISTS);
    }
    return rsp;

  }

  private byte[] getContentStorageForFilePath(String filePath) {
    try {
      return msuStorageUtils.getByteArray(filePath);
    } catch (StorageException ex) {
      LOG.formatedWarning("Error occured while retrieving filepath %s content. Error:", filePath, ex.getMessage());
    }
    return null;

  }

  @Override
  public InMailEventListResponse getInMailEventList(InMailEventListRequest param)
          throws SEDException_Exception {

    // validate data
    if (param == null) {
      throw SEDRequestUtils.createSEDException(
              "Empty request: OutMailEventListRequest",
              SEDExceptionCode.MISSING_DATA);
    }
    // validate control
    Control c = param.getControl();
    validateControl(c);
    int iStarIndex = c.getStartIndex() == null ? -1 : c.getStartIndex().
            intValue();

    // validate data
    InMailEventListRequest.Data dt = param.getData();
    if (dt == null) {
      throw SEDRequestUtils.createSEDException(
              "Empty data in request: OutMailEventListRequest/Data",
              SEDExceptionCode.MISSING_DATA);
    }

    if (Utils.isEmptyString(dt.getReceiverEBox())) {
      throw SEDRequestUtils.createSEDException(
              String.format(MSG_ERR_MISSING_ATTR, PARAMETER_RECEIVER_EBOX),
              SEDExceptionCode.MISSING_DATA);
    }

    validatePrincipalBox(dt.getReceiverEBox());

    if (dt.getMailId() == null) {
      throw SEDRequestUtils.createSEDException("Mail id  is required!",
              SEDExceptionCode.MISSING_DATA);
    }
    // init response
    InMailEventListResponse rsp = new InMailEventListResponse();
    RControl rc = new RControl();
    rc.setReturnValue(SVEVReturnValue.OK.getValue());
    rc.setStartIndex(BigInteger.valueOf(iStarIndex));
    rsp.setRControl(rc);
    rsp.setRData(new InMailEventListResponse.RData());

    TypedQuery<InEvent> q = memEManager.createNamedQuery(
            NamedQueries.LAU_NQ_INMAIL_GET_EVENTS, InEvent.class);
    q.setParameter(NamedQueries.NQ_PARAM_RECEIVER_EBOX, dt.getReceiverEBox());
    q.setParameter(NamedQueries.NQ_PARAM_MAIL_ID, dt.getMailId());

    List<InEvent> lst = q.getResultList();
    if (!lst.isEmpty()) {
      rsp.getRData().getInEvents().addAll(lst);
    }
    rc.setResponseSize(BigInteger.valueOf(lst.size()));
    rc.setResultSize(BigInteger.valueOf(lst.size()));

    return rsp;
  }

  @Override
  public InMailListResponse getInMailList(InMailListRequest intMailListRequest)
          throws SEDException_Exception {
    int iStarIndex;
    int iResCountIndex;
    // validate data
    if (intMailListRequest == null) {
      throw SEDRequestUtils.createSEDException(
              "Empty request: InMailListRequest",
              SEDExceptionCode.MISSING_DATA);
    }
    // validate control
    Control c = intMailListRequest.getControl();
    validateControl(c);
    iStarIndex = c.getStartIndex() == null ? -1 : c.getStartIndex().intValue();
    iResCountIndex = c.getResponseSize() == null ? -1 : c.getResponseSize().
            intValue();
    // validate data
    InMailListRequest.Data data = intMailListRequest.getData();
    if (data == null) {
      throw SEDRequestUtils.createSEDException(
              "Empty data in request: InMailList/Data",
              SEDExceptionCode.MISSING_DATA);
    }

    InMailListResponse rsp = new InMailListResponse();
    RControl rc = new RControl();
    rc.setReturnValue(SVEVReturnValue.OK.getValue());
    rc.setStartIndex(BigInteger.valueOf(iStarIndex));
    rsp.setRControl(rc);
    rsp.setRData(new InMailListResponse.RData());

    validatePrincipalBox(data.getReceiverEBox());

    try {

      CriteriaQuery<Long> cqCount = createSearchCriteria(InMail.class, data,
              true, null, null);
      CriteriaQuery<InMail> cq = createSearchCriteria(InMail.class, data, false,
              c.getSortBy(),
              c.getSortOrder());

      Long l = memEManager.createQuery(cqCount).getSingleResult();
      rc.setResultSize(BigInteger.valueOf(l));

      TypedQuery<InMail> q = memEManager.createQuery(cq);
      if (iResCountIndex > 0) {
        q.setMaxResults(iResCountIndex);
      }
      if (iStarIndex > 0) {
        q.setFirstResult(iStarIndex);
      }

      List<InMail> lst = q.getResultList();
      if (!lst.isEmpty()) {
        rsp.getRData().getInMails().addAll(lst);
      }
      rc.setResponseSize(BigInteger.valueOf(lst.size()));
    } catch (NoResultException ex) {
      rsp.getRControl().setReturnValue(SVEVReturnValue.WARNING.getValue());
    }
    return rsp;
  }

  private String getJNDIPrefix() {

    return System.getProperty(SEDSystemProperties.SYS_PROP_JNDI_PREFIX,
            "java:/jboss/");
  }

  private String getJNDI_JMSPrefix() {
    return System.getProperty(SEDSystemProperties.SYS_PROP_JNDI_JMS_PREFIX,
            "java:/jms/");
  }

  @Override
  public OutMailEventListResponse getOutMailEventList(
          OutMailEventListRequest param)
          throws SEDException_Exception {

    // validate data
    if (param == null) {
      throw SEDRequestUtils.createSEDException(
              "Empty request: OutMailEventListRequest",
              SEDExceptionCode.MISSING_DATA);
    }
    // validate control
    Control c = param.getControl();
    validateControl(c);
    int iStarIndex = c.getStartIndex() == null ? -1 : c.getStartIndex().
            intValue();

    // validate data
    OutMailEventListRequest.Data dt = param.getData();
    if (dt == null) {
      throw SEDRequestUtils.createSEDException(
              "Empty data in request: OutMailEventListRequest/Data",
              SEDExceptionCode.MISSING_DATA);
    }

    if (dt.getSenderEBox() == null) {
      throw SEDRequestUtils.createSEDException(
              "SenderEBox is required attribute",
              SEDExceptionCode.MISSING_DATA);
    }

    validatePrincipalBox(dt.getSenderEBox());

    if (dt.getMailId() == null && dt.getSenderMessageId() == null) {
      throw SEDRequestUtils.createSEDException(
              "Mail id or senderMessageId is required!",
              SEDExceptionCode.MISSING_DATA);
    }
    // init response
    OutMailEventListResponse rsp = new OutMailEventListResponse();
    RControl rc = new RControl();
    rc.setReturnValue(SVEVReturnValue.OK.getValue());
    rc.setStartIndex(BigInteger.valueOf(iStarIndex));
    rsp.setRControl(rc);
    rsp.setRData(new OutMailEventListResponse.RData());

    TypedQuery<OutEvent> q = memEManager.createNamedQuery(
            NamedQueries.LAU_NQ_OUTMAIL_GET_EVENTS, OutEvent.class);
    q.setParameter(NamedQueries.NQ_PARAM_SENDER_EBOX, dt.getSenderEBox());
    q.setParameter(NamedQueries.NQ_PARAM_MAIL_ID,
            dt.getMailId() == null ? -1 : dt.getMailId());
    q.setParameter(NamedQueries.NQ_PARAM_SENDER_MAIL_ID,
            Utils.isEmptyString(dt.getSenderMessageId()) ? "" : dt.
            getSenderMessageId());

    List<OutEvent> lst = q.getResultList();
    if (!lst.isEmpty()) {
      rsp.getRData().getOutEvents().addAll(lst);
    }
    rc.setResponseSize(BigInteger.valueOf(lst.size()));
    rc.setResultSize(rc.getResponseSize());

    return rsp;
  }

  /**
   *
   * @param outMailListRequest
   * @return
   * @throws SEDException_Exception
   */
  @Override
  public OutMailListResponse getOutMailList(
          OutMailListRequest outMailListRequest)
          throws SEDException_Exception {

    // validate data
    if (outMailListRequest == null) {
      throw SEDRequestUtils.createSEDException(
              "Empty request: OutMailListRequest",
              SEDExceptionCode.MISSING_DATA);
    }
    // validate control
    Control c = outMailListRequest.getControl();
    validateControl(c);
    int iStarIndex = c.getStartIndex() == null ? -1 : c.getStartIndex().
            intValue();
    int iResCountIndex = c.getResponseSize() == null ? -1 : c.getResponseSize().
            intValue();

    // validate data
    OutMailListRequest.Data data = outMailListRequest.getData();
    if (data == null) {
      throw SEDRequestUtils.createSEDException(
              "Empty data in request: OutMailList/Data",
              SEDExceptionCode.MISSING_DATA);
    }

    validatePrincipalBox(data.getSenderEBox());

    OutMailListResponse rsp = new OutMailListResponse();
    RControl rc = new RControl();
    rc.setReturnValue(SVEVReturnValue.OK.getValue());
    rc.setStartIndex(BigInteger.valueOf(iStarIndex));
    rsp.setRControl(rc);
    rsp.setRData(new OutMailListResponse.RData());

    try {

      CriteriaQuery<Long> cqCount = createSearchCriteria(OutMail.class, data,
              true, null, null);
      CriteriaQuery<OutMail> cq = createSearchCriteria(OutMail.class, data,
              false,
              c.getSortBy(), c.getSortOrder());

      Long l = memEManager.createQuery(cqCount).getSingleResult();
      rc.setResultSize(BigInteger.valueOf(l));

      TypedQuery<OutMail> q = memEManager.createQuery(cq);
      if (iResCountIndex > 0) {
        q.setMaxResults(iResCountIndex);
      }
      if (iStarIndex > 0) {
        q.setFirstResult(iStarIndex);
      }

      List<OutMail> lst = q.getResultList();
      if (!lst.isEmpty()) {
        rsp.getRData().getOutMails().addAll(lst);
      }
      rc.setResponseSize(BigInteger.valueOf(lst.size()));
    } catch (NoResultException ex) {
      rsp.getRControl().setReturnValue(SVEVReturnValue.WARNING.getValue());
    }
    return rsp;
  }

  private UserTransaction getUserTransaction() {
    // for jetty
    if (mutUTransaction == null) {
      try {
        InitialContext ic = new InitialContext();
        mutUTransaction = (UserTransaction) ic.lookup(
                getJNDIPrefix() + "UserTransaction");
      } catch (NamingException ex) {
        LOG.logError(0, ex);
      }
    }
    return mutUTransaction;
  }

  private OutMail mailExists(OutMail mail) {

    TypedQuery<OutMail> q
            = memEManager.createNamedQuery(
                    NamedQueries.LAU_NQ_OUTMAIL_getByMessageIdAndSenderBox,
                    OutMail.class);
    q.setParameter("sndMsgId", mail.getSenderMessageId());
    q.setParameter("senderBox", mail.getSenderEBox());
    List<OutMail> lst = q.getResultList();
    return lst.size() > 0 ? lst.get(0) : null;
  }

  /**
   *
   * @param param
   * @return
   * @throws SEDException_Exception
   */
  @Override
  public ModifyInMailResponse modifyInMail(ModifyInMailRequest param)
          throws SEDException_Exception {

    if (param == null) {
      throw SEDRequestUtils.createSEDException(
              "Empty request: ModifyInMailRequest",
              SEDExceptionCode.MISSING_DATA);
    }
    // validate control
    Control c = param.getControl();
    validateControl(c);

    // validate data
    ModifyInMailRequest.Data dt = param.getData();
    if (dt == null) {
      throw SEDRequestUtils.createSEDException(
              "Empty data in request: ModifyInMailRequest/Data",
              SEDExceptionCode.MISSING_DATA);
    }

    if (Utils.isEmptyString(dt.getReceiverEBox())) {
      throw SEDRequestUtils.createSEDException(
              String.format(MSG_ERR_MISSING_ATTR, PARAMETER_RECEIVER_EBOX),
              SEDExceptionCode.MISSING_DATA);
    }

    validatePrincipalBox(dt.getReceiverEBox());

    if (dt.getMailId() == null) {
      throw SEDRequestUtils.createSEDException("Mail id is required!",
              SEDExceptionCode.MISSING_DATA);
    }

    if (dt.getAction() == null) {
      throw SEDRequestUtils
              .createSEDException("Invalid or missing action!",
                      SEDExceptionCode.INVALID_DATA);
    }

    // init response
    ModifyInMailResponse rsp = new ModifyInMailResponse();
    RControl rc = new RControl();
    rc.setReturnValue(SVEVReturnValue.OK.getValue());

    rsp.setRControl(rc);
    rsp.setRData(new ModifyInMailResponse.RData());

    InMail im;
    try {
      CriteriaBuilder cb = memEManager.getCriteriaBuilder();
      CriteriaQuery<InMail> cq = cb.createQuery(InMail.class);
      Root<InMail> om = cq.from(InMail.class);
      cq.where(cb.and(cb.equal(om.get(PARAMETER_RECEIVER_EBOX), dt.
              getReceiverEBox()),
              cb.equal(om.get("Id"), dt.getMailId())));

      TypedQuery<InMail> q = memEManager.createQuery(cq);
      im = q.getSingleResult();

      if (SEDInboxMailStatus.RECEIVED.getValue().equals(im.getStatus())
              || SEDInboxMailStatus.LOCKED.getValue().equals(im.getStatus())
              || SEDInboxMailStatus.ERROR.getValue().equals(im.getStatus())) {

        SEDInboxMailStatus st = null;
        switch (dt.getAction()) {
          case DELIVERED:
            st = SEDInboxMailStatus.DELIVERED;
            break;
          case LOCK:
            st = SEDInboxMailStatus.LOCKED;
            break;
          case DELETE:
            st = SEDInboxMailStatus.DELETED;
            break;
        }
        if (st == null) {
          String message
                  = String.format("Action  %s is not supported !",
                          dt.getAction().value());
          throw SEDRequestUtils.createSEDException(message,
                  SEDExceptionCode.INVALID_DATA);
        }

        Date date = Calendar.getInstance().getTime();
        im.setDeliveredDate(date);
        im.setStatusDate(date);
        im.setStatus(st.getValue());
        InEvent ie = setInMailStatus(im, st.getDesc(), c.getUserId(), c.
                getApplicationId());
        rsp.getRData().setInEvent(ie);

      } else {
        String message
                = String.format(
                        "Mail with id '%s' and  ebox: %s is in invalid status %s !",
                        param.getData().getMailId()
                                .toString(), param.getData().getReceiverEBox(),
                        im.getStatus());
        throw SEDRequestUtils.createSEDException(message,
                SEDExceptionCode.REQUIRED_DATA_NOT_EXISTS);
      }

    } catch (NoResultException ignore) {
      String message
              = String.format(MSG_ERR_MISSING_MAIL,
                      param.getData().getMailId()
                              .toString(), param.getData().getReceiverEBox());
      throw SEDRequestUtils.createSEDException(message,
              SEDExceptionCode.REQUIRED_DATA_NOT_EXISTS);
    }
    return rsp;
  }

  /**
   *
   * @param param
   * @return
   * @throws SEDException_Exception
   */
  @Override
  public ModifyOutMailResponse modifyOutMail(ModifyOutMailRequest param)
          throws SEDException_Exception {
    if (param == null) {
      throw SEDRequestUtils.createSEDException(
              "Empty request: ModifyOutMailRequest",
              SEDExceptionCode.MISSING_DATA);
    }
    // validate control
    Control c = param.getControl();
    validateControl(c);

    // validate data
    ModifyOutMailRequest.Data dt = param.getData();
    if (dt == null) {
      throw SEDRequestUtils.createSEDException(
              "Empty data in request: ModifyInMailRequest/Data",
              SEDExceptionCode.MISSING_DATA);
    }

    if (Utils.isEmptyString(dt.getSenderEBox())) {
      throw SEDRequestUtils.createSEDException(
              String.format(MSG_ERR_MISSING_ATTR, PARAMETER_RECEIVER_EBOX),
              SEDExceptionCode.MISSING_DATA);
    }

    validatePrincipalBox(dt.getSenderEBox());

    if (dt.getMailId() == null) {
      throw SEDRequestUtils.createSEDException("Mail id is required!",
              SEDExceptionCode.MISSING_DATA);
    }

    if (dt.getAction() == null) {
      throw SEDRequestUtils
              .createSEDException("Action is required!",
                      SEDExceptionCode.MISSING_DATA);
    }
    // init response
    ModifyOutMailResponse rsp = new ModifyOutMailResponse();
    RControl rc = new RControl();
    rc.setReturnValue(SVEVReturnValue.OK.getValue());

    rsp.setRControl(rc);
    rsp.setRData(new ModifyOutMailResponse.RData());

    OutMail omail;
    try {
      CriteriaBuilder cb = memEManager.getCriteriaBuilder();
      CriteriaQuery<OutMail> cq = cb.createQuery(OutMail.class);
      Root<OutMail> om = cq.from(OutMail.class);
      cq.where(cb.and(cb.equal(om.get("SenderEBox"), dt.getSenderEBox()),
              cb.equal(om.get("Id"), dt.getMailId())));

      TypedQuery<OutMail> q = memEManager.createQuery(cq);
      omail = q.getSingleResult();
      switch (dt.getAction()) {

        case DELETE:
          if (omail.getStatus().equalsIgnoreCase(SEDOutboxMailStatus.SUBMITTED.
                  getValue())
                  || omail.getStatus().equalsIgnoreCase(
                          SEDOutboxMailStatus.ERROR.getValue())
                  || omail.getStatus().equalsIgnoreCase(
                          SEDOutboxMailStatus.FAILED.getValue())
                  || omail.getStatus().equalsIgnoreCase(
                          SEDOutboxMailStatus.SCHEDULE.getValue())) {
            omail.setStatus(SEDOutboxMailStatus.DELETED.getValue());
            omail.setStatusDate(Calendar.getInstance().getTime());

            OutEvent ou
                    = setOutMailStatus(omail,
                            "Deleted by user/application: " + c.getUserId() + "/" + c.
                            getApplicationId(),
                            c.getUserId(), c.getApplicationId());
            rsp.getRData().setOutEvent(ou);

          } else if (omail.getStatus().equalsIgnoreCase(
                  SEDOutboxMailStatus.DELETED.getValue())) {

            OutEvent ou = new OutEvent();
            ou.setMailId(omail.getId());
            ou.setDate(omail.getStatusDate());
            ou.setStatus(omail.getStatus());
            rsp.getRData().setOutEvent(ou);
          } else if (omail.getStatus().equalsIgnoreCase(
                  SEDOutboxMailStatus.SENT.getValue())
                  || omail.getStatus().equalsIgnoreCase(
                          SEDOutboxMailStatus.PUSHING.getValue())) {
            throw SEDRequestUtils.createSEDException(
                    "Sent mail or mail in progress can not be DELETED",
                    SEDExceptionCode.INVALID_DATA);
          }
          break;
        case RESEND:
          if (omail.getStatus().equalsIgnoreCase(SEDOutboxMailStatus.ERROR.
                  getValue())
                  || omail.getStatus().equalsIgnoreCase(
                          SEDOutboxMailStatus.FAILED.getValue())
                  || omail.getStatus().equalsIgnoreCase(
                          SEDOutboxMailStatus.DELETED.getValue())) {

            omail.setStatus(SEDOutboxMailStatus.SCHEDULE.getValue());
            omail.setStatusDate(Calendar.getInstance().getTime());

            OutEvent ou
                    = setOutMailStatus(omail,
                            "Resending by user/application: " + c.getUserId() + "/" + c.
                            getApplicationId(),
                            c.getUserId(), c.getApplicationId());

            rsp.getRData().setOutEvent(ou);

            try {
              addMailToSubmitQueue(omail);
            } catch (SEDException_Exception se) {
              setOutMailStatus(omail,
                      "Error occured while resending mail: " + c.getUserId() + "/"
                      + c.getApplicationId() + " Error: " + se.getMessage(),
                      c.getUserId(), c.getApplicationId());
              throw se;
            }
          } else {
            throw SEDRequestUtils.createSEDException("Mail in status " + omail.
                    getStatus()
                    + " can not be resend!", SEDExceptionCode.INVALID_DATA);
          }

          break;
      }
    } catch (NoResultException ignore) {
      String message
              = String.format(MSG_ERR_MISSING_MAIL,
                      param.getData().getMailId()
                              .toString(), param.getData().getSenderEBox());
      throw SEDRequestUtils.createSEDException(message,
              SEDExceptionCode.REQUIRED_DATA_NOT_EXISTS);
    }
    return rsp;
  }

  private void serializeMail(OutMail mail, String userID, String applicationId,
          PMode pmode)
          throws SEDException_Exception {
    long l = LOG.logStart(userID, applicationId, pmode.getId());
    String locadomain = SEDSystemProperties.getLocalDomain();
    // prepare mail to persist
    Date dt = Calendar.getInstance().getTime();
    
    int iPriority = pmode.getPriority()== null ? 4 : pmode.getPriority();
    iPriority = iPriority >9 ? 9 : (iPriority <0 ? 0 : iPriority);
    
    
                    
    
    // set current status
    mail.setStatus(SEDOutboxMailStatus.SUBMITTED.getValue());
    mail.setSubmittedDate(dt);
    mail.setStatusDate(dt);
    // set message id
    if (Utils.isEmptyString(mail.getMessageId())) {
      mail.setMessageId(Utils.getUUIDWithDomain(locadomain));
    }
    // --------------------
    // serialize payload
    List<File> serializedFiles = new ArrayList<>();
    try {

      if (mail.getOutPayload() != null && !mail.getOutPayload().getOutParts().
              isEmpty()) {
        for (OutPart p : mail.getOutPayload().getOutParts()) {
          File fout = null;

          if (p.getBin() != null) {
            fout = msuStorageUtils.storeOutFile(p.getMimeType(), p.getBin());
          } else if (!Utils.isEmptyString(p.getFilepath())) {
            File fIn = new File(p.getFilepath());
            if (fIn.exists()) {
              fout = msuStorageUtils.storeOutFile(p.getMimeType(), fIn);
            }
          }
          if (fout != null) {
            serializedFiles.add(fout);
            String strHashValue = DigestUtils.getHexSha1Digest(fout);
            String relPath = StorageUtils.getRelativePath(fout);
            p.setFilepath(relPath);
            p.setSha256Value(strHashValue);
            p.setSize(BigInteger.valueOf(fout.length()));

            if (Utils.isEmptyString(p.getFilename())) {
              p.setFilename(fout.getName());
            }
            if (Utils.isEmptyString(p.getName())) {
              p.setName(p.getFilename().substring(p.getFilename().lastIndexOf(
                      '.')));
            }
          }
        }
      }
    } catch (StorageException ex) {
      SEDException msherr = new SEDException();
      msherr.setErrorCode(SEDExceptionCode.SERVER_ERROR);
      msherr.setMessage(ex.getMessage());
      rollbackSerializedFiles(serializedFiles);
      throw new SEDException_Exception("Error occured while storing payload",
              msherr, ex);
    }

    // --------------------
    // serialize data and submit message
    String msgFactoryJndiName = getJNDIPrefix() + SEDValues.EBMS_JMS_CONNECTION_FACTORY_JNDI;
    String msgQueueJndiName = getJNDI_JMSPrefix() + SEDValues.JNDI_QUEUE_EBMS;
    InitialContext ic = null;

    UserTransaction ut = getUserTransaction();
    assert ut != null;

    ConnectionFactory cf = null;
    try {
      // create JMS session
      ic = new InitialContext();
      cf = (ConnectionFactory) ic.lookup(msgFactoryJndiName);
      if (mqMSHQueue == null) {
        mqMSHQueue = (Queue) ic.lookup(msgQueueJndiName);
      }
    } catch (NamingException ex) {
      SEDException msherr = new SEDException();
      msherr.setErrorCode(SEDExceptionCode.SERVER_ERROR);
      msherr.setMessage(ex.getMessage());
      throw new SEDException_Exception(String.format(MSG_ERR_QUEUE_CONF,
              msgFactoryJndiName, msgQueueJndiName),
              msherr, ex);
    }

    try (Connection connection = cf.createConnection();
            Session session = connection.
                    createSession(true, Session.SESSION_TRANSACTED);
            MessageProducer sender = session.createProducer(mqMSHQueue)) {

      // start transaction
      ut.begin();
      // persist mail
      memEManager.persist(mail);
      // persist mail event
      OutEvent me = new OutEvent();
      me.setMailId(mail.getId());
      me.setSenderMessageId(mail.getSenderMessageId());
      me.setStatus(mail.getStatus());
      me.setDescription(SEDOutboxMailStatus.SUBMITTED.getDesc());
      me.setDate(mail.getStatusDate());
      me.setUserId(userID);
      me.setApplicationId(applicationId);
      memEManager.persist(me);

      // submit to ebms que
      Message message = session.createMessage();
      message.setLongProperty(SEDValues.EBMS_QUEUE_PARAM_MAIL_ID, mail.getId().
              longValue());
      message.setIntProperty(SEDValues.EBMS_QUEUE_PARAM_RETRY, 0);
      message.setLongProperty(SEDValues.EBMS_QUEUE_PARAM_DELAY, 0);
      LOG.log(mail.getId(),  pmode.getId());
      sender.setPriority(iPriority);
      sender.send(message);

      ut.commit();
      session.commit();
      LOG.formatedlog("Transaction commited: user %s, appl %s, pmodeId %s",
              userID, applicationId,
               pmode.getId());

    } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException
            | HeuristicRollbackException | SecurityException | IllegalStateException ex) {

      try {
        ut.rollback();

      } catch (IllegalStateException | SecurityException | SystemException ex1) {
        LOG.logWarn(l, "Error rollback transaction", ex1);
      }

      rollbackSerializedFiles(serializedFiles);

      SEDException msherr = new SEDException();
      msherr.setErrorCode(SEDExceptionCode.SERVER_ERROR);
      msherr.setMessage(ex.getMessage());
      throw new SEDException_Exception("Error occured while storing to DB",
              msherr, ex);

    } catch (JMSException ex) {
      try {
        ut.rollback();
      } catch (IllegalStateException | SecurityException | SystemException ex1) {
        LOG.logWarn(l, "Error rollback transaction", ex);
      }

      rollbackSerializedFiles(serializedFiles);
      SEDException msherr = new SEDException();
      msherr.setErrorCode(SEDExceptionCode.SERVER_ERROR);
      msherr.setMessage(ex.getMessage());
      throw new SEDException_Exception(
              String.format(MSG_ERR_QUEUE_CONF, msgFactoryJndiName,
                      msgQueueJndiName),
              msherr, ex);
    } finally {

      try {
        ic.close();
      } catch (NamingException ignore) {
        LOG.logWarn(l, "Error closing InitialContext for JSM session", ignore);
      }

    }

    LOG.logEnd(l, userID, applicationId,  pmode.getId());
  }

  private void addMailToSubmitQueue(OutMail mail)
          throws SEDException_Exception {
    long l = LOG.logStart();
    // --------------------
    // serialize data and submit message
    String msgFactoryJndiName = getJNDIPrefix() + SEDValues.EBMS_JMS_CONNECTION_FACTORY_JNDI;
    String msgQueueJndiName = getJNDI_JMSPrefix() + SEDValues.JNDI_QUEUE_EBMS;
    InitialContext ic = null;

    ConnectionFactory cf = null;
    try {
      // create JMS session
      ic = new InitialContext();
      cf = (ConnectionFactory) ic.lookup(msgFactoryJndiName);
      if (mqMSHQueue == null) {
        mqMSHQueue = (Queue) ic.lookup(msgQueueJndiName);
      }
    } catch (NamingException ex) {
      SEDException msherr = new SEDException();
      msherr.setErrorCode(SEDExceptionCode.SERVER_ERROR);
      msherr.setMessage(ex.getMessage());
      throw new SEDException_Exception(
              String.format(MSG_ERR_QUEUE_CONF, msgFactoryJndiName,
                      msgQueueJndiName),
              msherr, ex);
    }

    try (Connection connection = cf.createConnection();
            Session session = connection.createSession(false,
                    Session.AUTO_ACKNOWLEDGE);
            MessageProducer sender = session.createProducer(mqMSHQueue);) {

      Message message = session.createMessage();
      message.setLongProperty(SEDValues.EBMS_QUEUE_PARAM_MAIL_ID, mail.getId().
              longValue());
      message.setIntProperty(SEDValues.EBMS_QUEUE_PARAM_RETRY, 0);
      message.setLongProperty(SEDValues.EBMS_QUEUE_PARAM_DELAY, 0);
      sender.send(message);
    } catch (JMSException ex) {

      SEDException msherr = new SEDException();
      msherr.setErrorCode(SEDExceptionCode.SERVER_ERROR);
      msherr.setMessage(ex.getMessage());
      throw new SEDException_Exception(
              String.format(MSG_ERR_QUEUE_CONF, msgFactoryJndiName,
                      msgQueueJndiName),
              msherr, ex);
    } finally {

      try {
        ic.close();
      } catch (NamingException ignore) {
        LOG.logWarn(l, "Error closing InitialContext for JSM session", ignore);
      }

    }
  }

  private InEvent setInMailStatus(InMail im, String desc, String userID,
          String applicationId)
          throws SEDException_Exception {
    InEvent me = new InEvent();
    me.setMailId(im.getId());

    me.setStatus(im.getStatus());
    me.setDate(im.getStatusDate());
    me.setDescription(desc);
    me.setUserId(userID);
    me.setApplicationId(applicationId);

    UserTransaction ut = getUserTransaction();
    assert ut != null;

    try {
      ut.begin();
      memEManager.merge(im);
      memEManager.persist(me);
      ut.commit();
    } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException
            | HeuristicRollbackException | SecurityException | IllegalStateException ex) {

      try {
        ut.rollback();
      } catch (IllegalStateException | SecurityException | SystemException ex1) {
        // ignore
      }
      throw SEDRequestUtils.createSEDException(ex.getMessage(),
              SEDExceptionCode.SERVER_ERROR);

    }
    return me;

  }

  private OutEvent setOutMailStatus(OutMail om, String desc, String userID,
          String applicationId)
          throws SEDException_Exception {

    OutEvent me = new OutEvent();
    me.setMailId(om.getId());
    me.setStatus(om.getStatus());
    me.setDate(om.getStatusDate());
    me.setDescription(desc);
    me.setUserId(userID);
    me.setApplicationId(applicationId);

    UserTransaction ut = getUserTransaction();
    assert ut != null;
    try {
      ut.begin();
      memEManager.merge(om);
      memEManager.persist(me);
      ut.commit();
    } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException
            | HeuristicRollbackException | SecurityException | IllegalStateException ex) {

      try {
        ut.rollback();
      } catch (IllegalStateException | SecurityException | SystemException ex1) {
        // ignore
      }
      throw SEDRequestUtils.createSEDException(ex.getMessage(),
              SEDExceptionCode.SERVER_ERROR);

    }
    return me;

  }

  /**
   * - validate message -> serialize message -> send message to ebms queue
   *
   * @param submitMailRequest
   * @return
   * @throws SEDException_Exception
   */
  @Override
  public SubmitMailResponse submitMail(SubmitMailRequest submitMailRequest)
          throws SEDException_Exception {
    String ip = getCurrrentRemoteIP();

    // validate data
    if (submitMailRequest == null) {
      String msg = "Empty request: SubmitMailRequest send from:" + ip;
      throw SEDRequestUtils.createSEDException(msg,
              SEDExceptionCode.MISSING_DATA);
    }
    if (submitMailRequest.getData() == null) {
      String msg = "Empty data in request: SubmitMailRequest/Data" + ip;
      throw SEDRequestUtils.createSEDException(msg,
              SEDExceptionCode.MISSING_DATA);
    }
    if (submitMailRequest.getData().getOutMail() == null) {
      String msg = "Empty OutMail in request: SubmitMailRequest/Data/OutMail" + ip;
      throw SEDRequestUtils.createSEDException(msg,
              SEDExceptionCode.MISSING_DATA);
    }
    // validate control data
    validateControl(submitMailRequest.getControl());
    // get out mail
    OutMail mail = submitMailRequest.getData().getOutMail();
    // check for missing data
    List<String> lstWarn = SEDRequestUtils.validateMailForMissingData(mail);

    validatePrincipalBox(mail.getSenderEBox());

    SEDBox sb = mdbLookups.getSEDBoxByAddressName(mail.getSenderEBox());
    if (sb == null) {
      String msg
              = MSG_ERR_SENDER_BOX + mail.
                      getSenderEBox()
              + " not exists";
      throw SEDRequestUtils.createSEDException(msg,
              SEDExceptionCode.INVALID_DATA);
    } else {
      if (sb.getActiveFromDate() != null
              && sb.getActiveFromDate().after(Calendar.getInstance().getTime())) {
        String msg
                = MSG_ERR_SENDER_BOX + mail.
                        getSenderEBox()
                + " is  active! (Activation from : '" + sb.getActiveFromDate().
                        toString() + "')";
        throw SEDRequestUtils.createSEDException(msg,
                SEDExceptionCode.INVALID_DATA);
      }
      if (sb.getActiveToDate() != null
              && sb.getActiveToDate().before(Calendar.getInstance().getTime())) {
        String msg
                = MSG_ERR_SENDER_BOX + mail.
                        getSenderEBox()
                + " is  active! (Activation To : '" + sb.getActiveToDate().
                        toString() + "')";
        throw SEDRequestUtils.createSEDException(msg,
                SEDExceptionCode.INVALID_DATA);
      }
    }

    // check if mail already exists
    OutMail om = mailExists(mail);
    if (om == null) {
      // validate mail data
      PMode pmd = validateOutMailData(mail);

      // serialize payload to cache FS and data to db
      serializeMail(mail, submitMailRequest.getControl().getUserId(),
              submitMailRequest
                      .getControl().getApplicationId(), pmd);

    }
    // generate response
    SubmitMailResponse rsp = new SubmitMailResponse();
    rsp.setRControl(new RControl());
    rsp.getRControl().setReturnValue(
            om != null || !lstWarn.isEmpty() ? SVEVReturnValue.WARNING.
            getValue() : SVEVReturnValue.OK.
                    getValue());
    rsp.getRControl().setReturnText((om != null ? String.format(
            "Mail with sender message id '%s' already sent before: %s",
            om.getSenderMessageId(), msdfDDMMYYYYHHMMSS.format(
            om.getSubmittedDate())) : "") + String.join(", ", lstWarn));
    // set data
    rsp.setRData(new SubmitMailResponse.RData());
    rsp.getRData().setSubmittedDate(om != null ? om.getSubmittedDate() : mail.
            getSubmittedDate());
    rsp.getRData().setSenderMessageId(
            om != null ? om.getSenderMessageId() : mail.getSenderMessageId());
    rsp.getRData().setMailId(om != null ? om.getId() : mail.getId());
    return rsp;
  }

  private PMode validateOutMailData(OutMail mail)
          throws SEDException_Exception {

    if (SEDRequestUtils.isNotValidMailAddress(mail.getReceiverEBox())) {
      throw SEDRequestUtils.createSEDException("Invalid format: ReceiverEBox",
              SEDExceptionCode.INVALID_DATA);
    }

    if (SEDRequestUtils.isNotValidMailAddress(mail.getSenderEBox())) {
      throw SEDRequestUtils.createSEDException("Invalid format: SenderEBox",
              SEDExceptionCode.INVALID_DATA);
    }

    PartyIdentitySet sPID;
    PartyIdentitySet rPID;
    Service srv;

    PMode pm;
    try {
      sPID = mpModeManager.
              getPartyIdentitySetForSEDAddress(mail.getSenderEBox());
      rPID = mpModeManager.getPartyIdentitySetForSEDAddress(mail.
              getReceiverEBox());
      srv = mpModeManager.getServiceById(mail.getService());
      Action act = PModeUtils.getActionFromService(mail.getAction(), srv);

      String sendingRole = Objects.equals(act.getInvokeRole(),
              ActionRole.Executor.getValue())
              ? srv.getExecutor().getRole() : srv.getInitiator().getRole();

      pm = mpModeManager.
              getPModeForLocalPartyAsSender(sPID.getId(), sendingRole,
                      rPID.getId(),
                      mail.getService());
    } catch (PModeException ex) {
      throw SEDRequestUtils.createSEDException(
              ex.getMessage(), SEDExceptionCode.INVALID_DATA);
    }
    return pm;
  }

  private void rollbackSerializedFiles(List<File> lstFiles) {
    for (File frb : lstFiles) {
      if (frb.delete()) {
        LOG.
                formatedlog("Deleted file in a rollback: %s ", frb.
                        getAbsolutePath());
      } else {
        LOG.formatedWarning("Failed to deleted file in a rollback: %s ", frb.
                getAbsolutePath());
      }
    }

  }

  public void validateControl(Control c) throws SEDException_Exception {

    if (c == null) {
      throw SEDRequestUtils.createSEDException("SubmitMailRequest/Control",
              SEDExceptionCode.MISSING_DATA);
    }
    if (c.getApplicationId() == null) {
      throw SEDRequestUtils.createSEDException(
              "SubmitMailRequest/Control/@applicationId",
              SEDExceptionCode.MISSING_DATA);
    }
    if (c.getUserId() == null) {
      throw SEDRequestUtils.createSEDException(
              "SubmitMailRequest/Control/@userId",
              SEDExceptionCode.MISSING_DATA);
    }

    if (!Objects.equals(c.getApplicationId(), getAuthenticatedPrincipal())) {
      throw SEDRequestUtils.createSEDException(
              String.format(
                      "applicationId %s does not match authenticated principal %s ",
                      c.getApplicationId(), getAuthenticatedPrincipal()),
              SEDExceptionCode.MISSING_DATA);
    }
  }

  public void validatePrincipalBox(String box) throws SEDException_Exception {

    if (Utils.isEmptyString(box)) {
      throw SEDRequestUtils.createSEDException("EBox is required attribute",
              SEDExceptionCode.MISSING_DATA);
    }
    String authenticatedId = getAuthenticatedPrincipal();
    if (Utils.isEmptyString(authenticatedId)) {
      throw SEDRequestUtils.createSEDException(
              "Application auhtentication is required!",
              SEDExceptionCode.MISSING_DATA);
    }

    SEDApplication cms = mdbLookups.getSEDApplicationById(authenticatedId);
    if (cms == null) {
      throw SEDRequestUtils.createSEDException(String.format(
              "Application %s is not registred!", authenticatedId),
              SEDExceptionCode.MISSING_DATA);

    }
    if (SEDRequestUtils.isNotValidMailAddress(box)) {
      throw SEDRequestUtils.createSEDException(String.format(
              "box %s is not valid address!", box),
              SEDExceptionCode.MISSING_DATA);
    }

    String localPart = box.substring(0, box.indexOf('@'));
    for (SEDBox sb : cms.getSEDBoxes()) {
      if (Objects.equals(sb.getLocalBoxName(), localPart)) {
        return;
      }

    }

    throw SEDRequestUtils.createSEDException(String.format(
            "Box %s is not assigned to application %s", box, authenticatedId),
            SEDExceptionCode.MISSING_DATA);

  }

}
