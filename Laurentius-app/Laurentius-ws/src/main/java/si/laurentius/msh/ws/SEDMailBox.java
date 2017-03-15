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
import javax.persistence.criteria.Path;
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
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.Utils;
import si.laurentius.lce.DigestUtils;
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
  SimpleDateFormat msdfDDMMYYYY_HHMMSS = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
  StorageUtils msuStorageUtils = new StorageUtils();

  /**
   *
   */
  @Resource
  protected UserTransaction mutUTransaction;
  @Resource
  WebServiceContext mwsCtxt;

  /**
   *
   * @param con
   */
  protected void closeConnection(Connection con) {
    try {
      if (con != null) {
        con.close();
      }
    } catch (JMSException jmse) {
      // ignore
    }
  }

  /**
   * Generate search criteria from search parameter. Result class should have same "method name" as
   * is in search parameter. If we would like to search by "getAction" parameter . result entity
   * must have getAction and setAction methods. if searhc method ends on To Or From result entity
   * must have method without to of From. Example: for search parameter getDateFrom end entity must
   * have getDate/setDate method and parameter must inherit comparable!
   *
   * @param searchParams
   * @param resultClass
   * @param forCount
   * @return
   */
  private CriteriaQuery createSearchCriteria(Object searchParams, Class resultClass,
      boolean forCount, String sortOrder, String sortColumn) {
    long l = LOG.logStart();
    Class cls = searchParams.getClass();
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery cq = forCount ? cb.createQuery(Long.class) : cb.createQuery(resultClass);
    Root<OutMail> om = cq.from(resultClass);
    if (forCount) {
      cq.select(cb.count(om));
    } else if (sortColumn != null) {
      Path p;
      try {
        p = om.get(sortColumn);
        cq.orderBy(sortOrder != null && sortOrder.equalsIgnoreCase("desc") ? cb.desc(p) : cb.asc(p));
      } catch (IllegalArgumentException ex) {
        LOG.logWarn(l, "Column for sortName: '" + sortColumn + "' not exists!", ex);
      }
    }

    List<Predicate> lstPredicate = new ArrayList<>();

    Method[] methodList = cls.getDeclaredMethods();
    for (Method m : methodList) {

      // only getters (public, starts with get, no arguments)
      String mName = m.getName();
      if (Modifier.isPublic(m.getModifiers()) && m.getParameterCount() == 0 &&
          !m.getReturnType().equals(Void.TYPE) &&
          (mName.startsWith("get") || mName.startsWith("is"))) {
        String fieldName = mName.substring(mName.startsWith("get") ? 3 : 2);
        try {
          cls.getMethod("set" + fieldName, new Class[]{m.getReturnType()});
        } catch (NoSuchMethodException | SecurityException ex) {
          // method does not have setter
          continue;
        }

        try {
          // get returm parameter
          Object searchValue = m.invoke(searchParams, new Object[]{});

          if (searchValue != null) {
            if (fieldName.endsWith("From") && searchValue instanceof Comparable) {
              lstPredicate.add(cb.greaterThanOrEqualTo(
                  om.get(fieldName.substring(0, fieldName.lastIndexOf("From"))),
                  (Comparable) searchValue));
            } else if (fieldName.endsWith("To") && searchValue instanceof Comparable) {
              lstPredicate.add(cb.lessThan(
                  om.get(fieldName.substring(0, fieldName.lastIndexOf("To"))),
                  (Comparable) searchValue));
            } else {
              lstPredicate.add(cb.equal(om.get(fieldName), searchValue));
            }
          }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
          LOG.logError(l, "Error occured while creating sql Criteria.", ex);
        }

      }

    }

    if (!lstPredicate.isEmpty()) {
      Predicate[] tblPredicate = lstPredicate.stream().toArray(Predicate[]::new);
      cq.where(cb.and(tblPredicate));
    }
    LOG.logEnd(l);
    return cq;
  }

  /**
   *
   * @return
   */
  protected String getCurrrentRemoteIP() {
    String clientIP = null;
    if (mwsCtxt != null) {
      MessageContext msgCtxt = mwsCtxt.getMessageContext();
      HttpServletRequest req = (HttpServletRequest) msgCtxt.get(MessageContext.SERVLET_REQUEST);
      clientIP = req.getRemoteAddr();
    } else {
      LOG.log("WebServiceContext is null! Can't get client's IP. ");
    }
    return clientIP;
  }

  private EntityManager getEntityManager() {
    // for jetty
    long l = LOG.logStart();
    if (memEManager == null) {
      try {
        InitialContext ic = new InitialContext();
        memEManager = (EntityManager) ic.lookup(getJNDIPrefix() + "ebMS_PU");

      } catch (NamingException ex) {
        LOG.logError(l, ex);
      }

    }
    return memEManager;
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
      throw SEDRequestUtils.createSEDException("Empty request: GetInMailRequest",
          SEDExceptionCode.MISSING_DATA);
    }
    // validate control
    Control c = param.getControl();
    SEDRequestUtils.validateControl(c);

    // validate data
    GetInMailRequest.Data dt = param.getData();
    if (dt == null) {
      throw SEDRequestUtils.createSEDException("Empty data in request: GetInMailRequest/Data",
          SEDExceptionCode.MISSING_DATA);
    }

    if (dt.getReceiverEBox() == null) {
      throw SEDRequestUtils.createSEDException("ReceiverEBox is required attribute",
          SEDExceptionCode.MISSING_DATA);
    }

    if (dt.getMailId() == null) {
      throw SEDRequestUtils.createSEDException("Mail id  is required!",
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
      CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
      CriteriaQuery<InMail> cq = cb.createQuery(InMail.class);
      Root<InMail> om = cq.from(InMail.class);
      cq.where(cb.and(cb.equal(om.get("ReceiverEBox"), dt.getReceiverEBox()),
          cb.equal(om.get("Id"), dt.getMailId())));

      TypedQuery<InMail> q = getEntityManager().createQuery(cq);
      im = q.getSingleResult();

      if (im.getInPayload() != null && !im.getInPayload().getInParts().isEmpty()) {
        for (InPart ip : im.getInPayload().getInParts()) {
          try {
            ip.setBin(msuStorageUtils.getByteArray(ip.getFilepath()));
          } catch (StorageException ingore) {

          }
        }
      }

      rsp.getRData().setInMail(im);
    } catch (NoResultException ignore) {
      String message =
          String.format("Mail with id '%s' and  ebox: %s not exists!", param.getData().getMailId()
              .toString(), param.getData().getReceiverEBox());
      throw SEDRequestUtils.createSEDException(message, SEDExceptionCode.REQUIRED_DATA_NOT_EXISTS);
    }
    return rsp;

  }

  @Override
  public InMailEventListResponse getInMailEventList(InMailEventListRequest param)
      throws SEDException_Exception {
    int iStarIndex = -1;
    int iResCountIndex = -1;

    // validate data
    if (param == null) {
      throw SEDRequestUtils.createSEDException("Empty request: OutMailEventListRequest",
          SEDExceptionCode.MISSING_DATA);
    }
    // validate control
    Control c = param.getControl();
    SEDRequestUtils.validateControl(c);
    iStarIndex = c.getStartIndex() == null ? -1 : c.getStartIndex().intValue();
    iResCountIndex = c.getResponseSize() == null ? -1 : c.getResponseSize().intValue();

    // validate data
    InMailEventListRequest.Data dt = param.getData();
    if (dt == null) {
      throw SEDRequestUtils.createSEDException(
          "Empty data in request: OutMailEventListRequest/Data", SEDExceptionCode.MISSING_DATA);
    }

    if (Utils.isEmptyString(dt.getReceiverEBox())) {
      throw SEDRequestUtils.createSEDException("ReceiverEBox is required attribute",
          SEDExceptionCode.MISSING_DATA);
    }

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

    TypedQuery<InEvent> q = getEntityManager().createNamedQuery(
        NamedQueries.LAU_NQ_INMAIL_GET_EVENTS, InEvent.class);
    q.setParameter(NamedQueries.NQ_PARAM_RECEIVER_EBOX, dt.getReceiverEBox());
    q.setParameter(NamedQueries.NQ_PARAM_MAIL_ID, dt.getMailId());

    /*
    CriteriaQuery<Long> cqCount = createSearchCriteria(dt, InEvent.class, true, null, null);
    CriteriaQuery<InEvent> cq = createSearchCriteria(dt, InEvent.class, false, c.getSortOrder(),
        c.getSortBy());

    Long l = getEntityManager().createQuery(cqCount).getSingleResult();
    rc.setResultSize(BigInteger.valueOf(l));

    TypedQuery<InEvent> q = getEntityManager().createQuery(cq);

    if (iResCountIndex > 0) {
      q.setMaxResults(iResCountIndex);
    }
    if (iStarIndex > 0) {
      q.setFirstResult(iStarIndex);
    }
     */
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
      throw SEDRequestUtils.createSEDException("Empty request: InMailListRequest",
          SEDExceptionCode.MISSING_DATA);
    }
    // validate control
    Control c = intMailListRequest.getControl();
    SEDRequestUtils.validateControl(c);
    iStarIndex = c.getStartIndex() == null ? -1 : c.getStartIndex().intValue();
    iResCountIndex = c.getResponseSize() == null ? -1 : c.getResponseSize().intValue();
    // validate data
    InMailListRequest.Data data = intMailListRequest.getData();
    if (data == null) {
      throw SEDRequestUtils.createSEDException("Empty data in request: OutMailList/Data",
          SEDExceptionCode.MISSING_DATA);
    }

    InMailListResponse rsp = new InMailListResponse();
    RControl rc = new RControl();
    rc.setReturnValue(SVEVReturnValue.OK.getValue());
    rc.setStartIndex(BigInteger.valueOf(iStarIndex));
    rsp.setRControl(rc);
    rsp.setRData(new InMailListResponse.RData());

    try {

      CriteriaQuery<Long> cqCount = createSearchCriteria(data, InMail.class, true, null, null);
      CriteriaQuery<InMail> cq = createSearchCriteria(data, InMail.class, false, c.getSortOrder(),
          c.getSortBy());

      Long l = getEntityManager().createQuery(cqCount).getSingleResult();
      rc.setResultSize(BigInteger.valueOf(l));

      TypedQuery<InMail> q = getEntityManager().createQuery(cq);
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

    return System.getProperty(SEDSystemProperties.SYS_PROP_JNDI_PREFIX, "java:/jboss/");
  }

  private String getJNDI_JMSPrefix() {
    return System.getProperty(SEDSystemProperties.SYS_PROP_JNDI_JMS_PREFIX, "java:/jms/");
  }

  @Override
  public OutMailEventListResponse getOutMailEventList(OutMailEventListRequest param)
      throws SEDException_Exception {

    // validate data
    if (param == null) {
      throw SEDRequestUtils.createSEDException("Empty request: OutMailEventListRequest",
          SEDExceptionCode.MISSING_DATA);
    }
    // validate control
    Control c = param.getControl();
    SEDRequestUtils.validateControl(c);
    int iStarIndex = c.getStartIndex() == null ? -1 : c.getStartIndex().intValue();
    int iResCountIndex = c.getResponseSize() == null ? -1 : c.getResponseSize().intValue();

    // validate data
    OutMailEventListRequest.Data dt = param.getData();
    if (dt == null) {
      throw SEDRequestUtils.createSEDException(
          "Empty data in request: OutMailEventListRequest/Data", SEDExceptionCode.MISSING_DATA);
    }

    if (dt.getSenderEBox() == null) {
      throw SEDRequestUtils.createSEDException("SenderEBox is required attribute",
          SEDExceptionCode.MISSING_DATA);
    }

    if (dt.getMailId() == null && dt.getSenderMessageId() == null) {
      throw SEDRequestUtils.createSEDException("Mail id or senderMessageId is required!",
          SEDExceptionCode.MISSING_DATA);
    }
    // init response
    OutMailEventListResponse rsp = new OutMailEventListResponse();
    RControl rc = new RControl();
    rc.setReturnValue(SVEVReturnValue.OK.getValue());
    rc.setStartIndex(BigInteger.valueOf(iStarIndex));
    rsp.setRControl(rc);
    rsp.setRData(new OutMailEventListResponse.RData());

    TypedQuery<OutEvent> q = getEntityManager().createNamedQuery(
        NamedQueries.LAU_NQ_OUTMAIL_GET_EVENTS, OutEvent.class);
    q.setParameter(NamedQueries.NQ_PARAM_SENDER_EBOX, dt.getSenderEBox());
    q.setParameter(NamedQueries.NQ_PARAM_MAIL_ID, dt.getMailId() == null ? -1 : dt.getMailId());
    q.setParameter(NamedQueries.NQ_PARAM_SENDER_MAIL_ID,
        Utils.isEmptyString(dt.getSenderMessageId()) ? "" : dt.getSenderMessageId());

    /* CriteriaQuery<Long> cqCount = createSearchCriteria(dt, OutEvent.class, true, null, null);
    CriteriaQuery<OutEvent> cq = createSearchCriteria(dt, OutEvent.class, false, c.getSortOrder(),
        c.getSortBy());

    Long l = getEntityManager().createQuery(cqCount).getSingleResult();
    rc.setResultSize(BigInteger.valueOf(l));
    TypedQuery<OutEvent> q = getEntityManager().createQuery(cq); *
   
    if (iResCountIndex > 0) {
      q.setMaxResults(iResCountIndex);
    }
    if (iStarIndex > 0) {
      q.setFirstResult(iStarIndex);
    }
     */
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
  public OutMailListResponse getOutMailList(OutMailListRequest outMailListRequest)
      throws SEDException_Exception {

    // validate data
    if (outMailListRequest == null) {
      throw SEDRequestUtils.createSEDException("Empty request: OutMailListRequest",
          SEDExceptionCode.MISSING_DATA);
    }
    // validate control
    Control c = outMailListRequest.getControl();
    SEDRequestUtils.validateControl(c);
    int iStarIndex = c.getStartIndex() == null ? -1 : c.getStartIndex().intValue();
    int iResCountIndex = c.getResponseSize() == null ? -1 : c.getResponseSize().intValue();

    // validate data
    OutMailListRequest.Data data = outMailListRequest.getData();
    if (data == null) {
      throw SEDRequestUtils.createSEDException("Empty data in request: OutMailList/Data",
          SEDExceptionCode.MISSING_DATA);
    }

    OutMailListResponse rsp = new OutMailListResponse();
    RControl rc = new RControl();
    rc.setReturnValue(SVEVReturnValue.OK.getValue());
    rc.setStartIndex(BigInteger.valueOf(iStarIndex));
    rsp.setRControl(rc);
    rsp.setRData(new OutMailListResponse.RData());

    try {

      CriteriaQuery<Long> cqCount = createSearchCriteria(data, OutMail.class, true, null, null);
      CriteriaQuery<OutMail> cq = createSearchCriteria(data, OutMail.class, false, c.getSortOrder(),
          c.getSortBy());

      Long l = getEntityManager().createQuery(cqCount).getSingleResult();
      rc.setResultSize(BigInteger.valueOf(l));

      TypedQuery<OutMail> q = getEntityManager().createQuery(cq);
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
        mutUTransaction = (UserTransaction) ic.lookup(getJNDIPrefix() + "UserTransaction");
      } catch (NamingException ex) {
        LOG.logError(0, ex);
      }
    }
    return mutUTransaction;
  }

  private OutMail mailExists(OutMail mail) {

    TypedQuery<OutMail> q =
        getEntityManager().createNamedQuery(NamedQueries.LAU_NQ_OUTMAIL_getByMessageIdAndSenderBox,
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
      throw SEDRequestUtils.createSEDException("Empty request: ModifyInMailRequest",
          SEDExceptionCode.MISSING_DATA);
    }
    // validate control
    Control c = param.getControl();
    SEDRequestUtils.validateControl(c);

    // validate data
    ModifyInMailRequest.Data dt = param.getData();
    if (dt == null) {
      throw SEDRequestUtils.createSEDException("Empty data in request: ModifyInMailRequest/Data",
          SEDExceptionCode.MISSING_DATA);
    }

    if (Utils.isEmptyString(dt.getReceiverEBox())) {
      throw SEDRequestUtils.createSEDException("ReceiverEBox is required attribute",
          SEDExceptionCode.MISSING_DATA);
    }

    if (dt.getMailId() == null) {
      throw SEDRequestUtils.createSEDException("Mail id is required!",
          SEDExceptionCode.MISSING_DATA);
    }

    if (dt.getAction() == null) {
      throw SEDRequestUtils
          .createSEDException("Invalid or missing action!", SEDExceptionCode.INVALID_DATA);
    }

    // init response
    ModifyInMailResponse rsp = new ModifyInMailResponse();
    RControl rc = new RControl();
    rc.setReturnValue(SVEVReturnValue.OK.getValue());

    rsp.setRControl(rc);
    rsp.setRData(new ModifyInMailResponse.RData());

    InMail im;
    try {
      CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
      CriteriaQuery<InMail> cq = cb.createQuery(InMail.class);
      Root<InMail> om = cq.from(InMail.class);
      cq.where(cb.and(cb.equal(om.get("ReceiverEBox"), dt.getReceiverEBox()),
          cb.equal(om.get("Id"), dt.getMailId())));

      TypedQuery<InMail> q = getEntityManager().createQuery(cq);
      im = q.getSingleResult();

      if (SEDInboxMailStatus.RECEIVED.getValue().equals(im.getStatus()) ||
          SEDInboxMailStatus.LOCKED.getValue().equals(im.getStatus()) ||
          SEDInboxMailStatus.ERROR.getValue().equals(im.getStatus())) {

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
        if (st == null){
              String message =
                String.format("Action  %s is not supported !",
                    dt.getAction().value());
            throw SEDRequestUtils.createSEDException(message, SEDExceptionCode.INVALID_DATA);
        }

        Date date = Calendar.getInstance().getTime();
        im.setDeliveredDate(date);
        im.setStatusDate(date);
        im.setStatus(st.getValue());
        InEvent ie = setInMailStatus(im, st.getDesc(), c.getUserId(), c.getApplicationId());
        rsp.getRData().setInEvent(ie);

      } else {
        String message =
            String.format("Mail with id '%s' and  ebox: %s is in invalid status %s !",
                param.getData().getMailId()
                    .toString(), param.getData().getReceiverEBox(), im.getStatus());
        throw SEDRequestUtils.createSEDException(message, SEDExceptionCode.REQUIRED_DATA_NOT_EXISTS);
      }

    } catch (NoResultException ignore) {
      String message =
          String.format("Mail with id '%s' and  ebox: %s not exists!", param.getData().getMailId()
              .toString(), param.getData().getReceiverEBox());
      throw SEDRequestUtils.createSEDException(message, SEDExceptionCode.REQUIRED_DATA_NOT_EXISTS);
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
      throw SEDRequestUtils.createSEDException("Empty request: ModifyOutMailRequest",
          SEDExceptionCode.MISSING_DATA);
    }
    // validate control
    Control c = param.getControl();
    SEDRequestUtils.validateControl(c);

    // validate data
    ModifyOutMailRequest.Data dt = param.getData();
    if (dt == null) {
      throw SEDRequestUtils.createSEDException("Empty data in request: ModifyInMailRequest/Data",
          SEDExceptionCode.MISSING_DATA);
    }

    if (Utils.isEmptyString(dt.getSenderEBox())) {
      throw SEDRequestUtils.createSEDException("ReceiverEBox is required attribute",
          SEDExceptionCode.MISSING_DATA);
    }

    if (dt.getMailId() == null) {
      throw SEDRequestUtils.createSEDException("Mail id is required!",
          SEDExceptionCode.MISSING_DATA);
    }

    if (dt.getAction() == null) {
      throw SEDRequestUtils
          .createSEDException("Action is required!", SEDExceptionCode.MISSING_DATA);
    }
    // init response
    ModifyOutMailResponse rsp = new ModifyOutMailResponse();
    RControl rc = new RControl();
    rc.setReturnValue(SVEVReturnValue.OK.getValue());

    rsp.setRControl(rc);
    rsp.setRData(new ModifyOutMailResponse.RData());

    OutMail omail;
    try {
      CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
      CriteriaQuery<OutMail> cq = cb.createQuery(OutMail.class);
      Root<OutMail> om = cq.from(OutMail.class);
      cq.where(cb.and(cb.equal(om.get("SenderEBox"), dt.getSenderEBox()),
          cb.equal(om.get("Id"), dt.getMailId())));

      TypedQuery<OutMail> q = getEntityManager().createQuery(cq);
      omail = q.getSingleResult();
      switch (dt.getAction()) {
        /*Not need this 
        case ABORT:
          if (omail.getStatus().equalsIgnoreCase(SEDOutboxMailStatus.SUBMITTED.getValue()) ||
              omail.getStatus().equalsIgnoreCase(SEDOutboxMailStatus.FAILED.getValue()) ||
              omail.getStatus().equalsIgnoreCase(SEDOutboxMailStatus.ERROR.getValue()) ||
              omail.getStatus().equalsIgnoreCase(SEDOutboxMailStatus.SCHEDULE.getValue())) {

            omail.setStatus(SEDOutboxMailStatus.CANCELED.getValue());
            omail.setStatusDate(Calendar.getInstance().getTime());

            OutEvent ou =
                setOutMailStatus(omail,
                    "Canceled by user/application: " + c.getUserId() + "/" + c.getApplicationId(),
                    c.getUserId(), c.getApplicationId());
            rsp.getRData().setOutEvent(ou);

          } else if (omail.getStatus().equalsIgnoreCase(SEDOutboxMailStatus.CANCELED.getValue()) ||
              omail.getStatus().equalsIgnoreCase(SEDOutboxMailStatus.CANCELING.getValue()) ||
              omail.getStatus().equalsIgnoreCase(SEDOutboxMailStatus.DELETED.getValue())) {
            // ignore
            OutEvent ou = new OutEvent();
            ou.setMailId(omail.getId());
            ou.setDate(omail.getStatusDate());
            ou.setStatus(omail.getStatus());
            ou.setSenderEBox(omail.getSenderEBox());
            rsp.getRData().setOutEvent(ou);

          } else if (omail.getStatus().equalsIgnoreCase(SEDOutboxMailStatus.SENT.getValue()) ||
              omail.getStatus().equalsIgnoreCase(SEDOutboxMailStatus.PUSHING.getValue())) {
            throw SEDRequestUtils.createSEDException("Sent mail can not be canceled",
                SEDExceptionCode.INVALID_DATA);
          }

          break; */
        case DELETE:
          if (omail.getStatus().equalsIgnoreCase(SEDOutboxMailStatus.SUBMITTED.getValue()) ||
              omail.getStatus().equalsIgnoreCase(SEDOutboxMailStatus.ERROR.getValue()) ||
              omail.getStatus().equalsIgnoreCase(SEDOutboxMailStatus.FAILED.getValue()) ||
              omail.getStatus().equalsIgnoreCase(SEDOutboxMailStatus.SCHEDULE.getValue())) {
            omail.setStatus(SEDOutboxMailStatus.DELETED.getValue());
            omail.setStatusDate(Calendar.getInstance().getTime());

            OutEvent ou =
                setOutMailStatus(omail,
                    "Deleted by user/application: " + c.getUserId() + "/" + c.getApplicationId(),
                    c.getUserId(), c.getApplicationId());
            rsp.getRData().setOutEvent(ou);

          } else if (omail.getStatus().equalsIgnoreCase(SEDOutboxMailStatus.DELETED.getValue())) {

            OutEvent ou = new OutEvent();
            ou.setMailId(omail.getId());
            ou.setDate(omail.getStatusDate());
            ou.setStatus(omail.getStatus());
//            ou.setSenderEBox(omail.getSenderEBox());
            rsp.getRData().setOutEvent(ou);
          } else if (omail.getStatus().equalsIgnoreCase(SEDOutboxMailStatus.SENT.getValue()) ||
              omail.getStatus().equalsIgnoreCase(SEDOutboxMailStatus.PUSHING.getValue())) {
            throw SEDRequestUtils.createSEDException(
                "Sent mail or mail in progress can not be DELETED", SEDExceptionCode.INVALID_DATA);
          }
          break;
        case RESEND:
          if (omail.getStatus().equalsIgnoreCase(SEDOutboxMailStatus.ERROR.getValue()) ||
              omail.getStatus().equalsIgnoreCase(SEDOutboxMailStatus.FAILED.getValue()) ||
              omail.getStatus().equalsIgnoreCase(SEDOutboxMailStatus.DELETED.getValue())) {

            omail.setStatus(SEDOutboxMailStatus.SCHEDULE.getValue());
            omail.setStatusDate(Calendar.getInstance().getTime());

            OutEvent ou =
                setOutMailStatus(omail,
                    "Resending by user/application: " + c.getUserId() + "/" + c.getApplicationId(),
                    c.getUserId(), c.getApplicationId());

            rsp.getRData().setOutEvent(ou);

            try {
              addMailToSubmitQueue(omail);
            } catch (SEDException_Exception se) {
              setOutMailStatus(omail,
                  "Error occured while resending mail: " + c.getUserId() + "/" +
                  c.getApplicationId() + " Error: " + se.getMessage(),
                  c.getUserId(), c.getApplicationId());
              throw se;
            }
          } else {
            throw SEDRequestUtils.createSEDException("Mail in status " + omail.getStatus() +
                " can not be resend!", SEDExceptionCode.INVALID_DATA);
          }

          break;
      }
    } catch (NoResultException ignore) {
      String message =
          String.format("Mail with id '%s' and  ebox: %s not exists!", param.getData().getMailId()
              .toString(), param.getData().getSenderEBox());
      throw SEDRequestUtils.createSEDException(message, SEDExceptionCode.REQUIRED_DATA_NOT_EXISTS);
    }
    return rsp;
  }

  private void serializeMail(OutMail mail, String userID, String applicationId, String pmodeId)
      throws SEDException_Exception {
    long l = LOG.logStart(userID, applicationId, pmodeId);
    // prepare mail to persist
    Date dt = Calendar.getInstance().getTime();
    // set current status
    mail.setStatus(SEDOutboxMailStatus.SUBMITTED.getValue());
    mail.setSubmittedDate(dt);
    mail.setStatusDate(dt);
    // --------------------
    // serialize payload
    List<File> serializedFiles = new ArrayList<>();
    try {

      if (mail.getOutPayload() != null && !mail.getOutPayload().getOutParts().isEmpty()) {
        for (OutPart p : mail.getOutPayload().getOutParts()) {
          File fout = null;

          if (p.getBin() != null) {
            fout = msuStorageUtils.storeOutFile(p.getMimeType(), p.getBin());
            // purge binary data
            // p.setValue(null);
          } else if (!Utils.isEmptyString(p.getFilepath())) {
            File fIn = new File(p.getFilepath());
            if (fIn.exists()) {
              fout = msuStorageUtils.storeOutFile(p.getMimeType(), fIn);
            }
          }
          // set MD5 and relative path;
          if (fout != null) {
            serializedFiles.add(fout);
            String strHashValue = DigestUtils.getHexSha1Digest(fout);
            String relPath = StorageUtils.getRelativePath(fout);
            p.setFilepath(relPath);
            p.setSha1Value(strHashValue);
            p.setSize(BigInteger.valueOf(fout.length()) );

            if (Utils.isEmptyString(p.getFilename())) {
              p.setFilename(fout.getName());
            }
            if (Utils.isEmptyString(p.getName())) {
              p.setName(p.getFilename().substring(p.getFilename().lastIndexOf('.')));
            }
          }
        }
      }
    } catch (StorageException ex) {
      SEDException msherr = new SEDException();
      msherr.setErrorCode(SEDExceptionCode.SERVER_ERROR);
      msherr.setMessage(ex.getMessage());
      rollbackSerializedFiles(serializedFiles);
      throw new SEDException_Exception("Error occured while storing payload", msherr, ex);
    } 

    // --------------------
    // serialize data and submit message
    String msgFactoryJndiName = getJNDIPrefix() + SEDValues.EBMS_JMS_CONNECTION_FACTORY_JNDI;
    String msgQueueJndiName = getJNDI_JMSPrefix() + SEDValues.JNDI_QUEUE_EBMS;
    InitialContext ic = null;
    Connection connection = null;
    Session session = null;
    try {
      // create JMS session
      ic = new InitialContext();
      ConnectionFactory cf = (ConnectionFactory) ic.lookup(msgFactoryJndiName);
      if (mqMSHQueue == null) {
        mqMSHQueue = (Queue) ic.lookup(msgQueueJndiName);
      }
      connection = cf.createConnection();
      session = connection.createSession(true, Session.SESSION_TRANSACTED);

      // start transaction
      getUserTransaction().begin();
      // persist mail
      getEntityManager().persist(mail);
      // persist mail event
      OutEvent me = new OutEvent();
      me.setMailId(mail.getId());
//      me.setSenderEBox(mail.getSenderEBox());
      me.setSenderMessageId(mail.getSenderMessageId());
      me.setStatus(mail.getStatus());
      me.setDescription(SEDOutboxMailStatus.SUBMITTED.getDesc());
      me.setDate(mail.getStatusDate());
      me.setUserId(userID);
      me.setApplicationId(applicationId);
      getEntityManager().persist(me);

      // submit to ebms que
      MessageProducer sender = session.createProducer(mqMSHQueue);
      Message message = session.createMessage();
      message.setLongProperty(SEDValues.EBMS_QUEUE_PARAM_MAIL_ID, mail.getId().longValue());
      message.setIntProperty(SEDValues.EBMS_QUEUE_PARAM_RETRY, 0);
      message.setLongProperty(SEDValues.EBMS_QUEUE_PARAM_DELAY, 0);
      LOG.log(mail.getId(), pmodeId);
      sender.send(message);

      getUserTransaction().commit();
      session.commit();
      LOG.formatedlog("Transaction commited: user %s, appl %s, pmodeId %s", userID, applicationId,
          pmodeId);

    } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException
        | HeuristicRollbackException | SecurityException | IllegalStateException ex) {

      try {
        getUserTransaction().rollback();

      } catch (IllegalStateException | SecurityException | SystemException ex1) {
        LOG.logWarn(l, "Error rollback transaction", ex1);
      }

      try {
        if (session != null) {
          session.rollback();
        }
      } catch (JMSException ex1) {
        LOG.logWarn(l, "Error rollback JSM session", ex1);
      }
      rollbackSerializedFiles(serializedFiles);

      SEDException msherr = new SEDException();
      msherr.setErrorCode(SEDExceptionCode.SERVER_ERROR);
      msherr.setMessage(ex.getMessage());
      throw new SEDException_Exception("Error occured while storing to DB", msherr, ex);

    } catch (NamingException | JMSException ex) {
      try {
        getUserTransaction().rollback();
      } catch (IllegalStateException | SecurityException | SystemException ex1) {
        LOG.logWarn(l, "Error rollback transaction", ex);
      }

      try {
        if (session != null) {
          session.rollback();
        }
      } catch (JMSException ex1) {
        LOG.logWarn(l, "Error rollback JSM session", ex1);
      }
      rollbackSerializedFiles(serializedFiles);
      SEDException msherr = new SEDException();
      msherr.setErrorCode(SEDExceptionCode.SERVER_ERROR);
      msherr.setMessage(ex.getMessage());
      throw new SEDException_Exception(
          "Error occured while submiting mail to ebms queue. Check queue configuration: factory: '" +
          msgFactoryJndiName + "' queue: '" + msgQueueJndiName + "'", msherr, ex);
    } finally {
      if (ic != null) {
        try {
          ic.close();
        } catch (Exception ignore) {
          LOG.logWarn(l, "Error closing InitialContext for JSM session", ignore);
        }
      }
      closeConnection(connection);
    }

    LOG.logEnd(l, userID, applicationId, pmodeId);
  }

  private void addMailToSubmitQueue(OutMail mail)
      throws SEDException_Exception {
    long l = LOG.logStart();
    // --------------------
    // serialize data and submit message
    String msgFactoryJndiName = getJNDIPrefix() + SEDValues.EBMS_JMS_CONNECTION_FACTORY_JNDI;
    String msgQueueJndiName = getJNDI_JMSPrefix() + SEDValues.JNDI_QUEUE_EBMS;
    InitialContext ic = null;
    Connection connection = null;
    Session session = null;
    try {
      // create JMS session
      ic = new InitialContext();
      ConnectionFactory cf = (ConnectionFactory) ic.lookup(msgFactoryJndiName);
      if (mqMSHQueue == null) {
        mqMSHQueue = (Queue) ic.lookup(msgQueueJndiName);
      }
      connection = cf.createConnection();
      session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer sender = session.createProducer(mqMSHQueue);
      Message message = session.createMessage();
      message.setLongProperty(SEDValues.EBMS_QUEUE_PARAM_MAIL_ID, mail.getId().longValue());
      message.setIntProperty(SEDValues.EBMS_QUEUE_PARAM_RETRY, 0);
      message.setLongProperty(SEDValues.EBMS_QUEUE_PARAM_DELAY, 0);
      sender.send(message);
    } catch (NamingException | JMSException ex) {

      SEDException msherr = new SEDException();
      msherr.setErrorCode(SEDExceptionCode.SERVER_ERROR);
      msherr.setMessage(ex.getMessage());
      throw new SEDException_Exception(
          "Error occured while submiting mail to ebms queue. Check queue configuration: factory: '" +
          msgFactoryJndiName + "' queue: '" + msgQueueJndiName + "'", msherr, ex);
    } finally {
      if (ic != null) {
        try {
          ic.close();
        } catch (Exception ignore) {
          LOG.logWarn(l, "Error closing InitialContext for JSM session", ignore);
        }
      }
      closeConnection(connection);
    }
  }

  private InEvent setInMailStatus(InMail im, String desc, String userID, String applicationId)
      throws SEDException_Exception {
    InEvent me = new InEvent();
    me.setMailId(im.getId());

    me.setStatus(im.getStatus());
    me.setDate(im.getStatusDate());
    me.setDescription(desc);
    me.setUserId(userID);
    me.setApplicationId(applicationId);
    try {
      getUserTransaction().begin();
      getEntityManager().merge(im);
      getEntityManager().persist(me);
      getUserTransaction().commit();
    } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException
        | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
      {
        try {
          getUserTransaction().rollback();
        } catch (IllegalStateException | SecurityException | SystemException ex1) {
          // ignore
        }
        throw SEDRequestUtils.createSEDException(ex.getMessage(), SEDExceptionCode.SERVER_ERROR);
      }
    }
    return me;

  }

  private OutEvent setOutMailStatus(OutMail om, String desc, String userID, String applicationId)
      throws SEDException_Exception {

    OutEvent me = new OutEvent();
    me.setMailId(om.getId());
//    me.setSenderEBox(om.getSenderEBox());
    me.setStatus(om.getStatus());
    me.setDate(om.getStatusDate());
    me.setDescription(desc);
    me.setUserId(userID);
    me.setApplicationId(applicationId);
    try {
      getUserTransaction().begin();
      getEntityManager().merge(om);
      getEntityManager().persist(me);
      getUserTransaction().commit();
    } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException
        | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
      {
        try {
          getUserTransaction().rollback();
        } catch (IllegalStateException | SecurityException | SystemException ex1) {
          // ignore
        }
        throw SEDRequestUtils.createSEDException(ex.getMessage(), SEDExceptionCode.SERVER_ERROR);
      }
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
      throw SEDRequestUtils.createSEDException(msg, SEDExceptionCode.MISSING_DATA);
    }
    if (submitMailRequest.getData() == null) {
      String msg = "Empty data in request: SubmitMailRequest/Data" + ip;
      throw SEDRequestUtils.createSEDException(msg, SEDExceptionCode.MISSING_DATA);
    }
    if (submitMailRequest.getData().getOutMail() == null) {
      String msg = "Empty OutMail in request: SubmitMailRequest/Data/OutMail" + ip;
      throw SEDRequestUtils.createSEDException(msg, SEDExceptionCode.MISSING_DATA);
    }
    // validate control data
    SEDRequestUtils.validateControl(submitMailRequest.getControl());
    // get out mail
    OutMail mail = submitMailRequest.getData().getOutMail();
    // check for missing data
    SEDRequestUtils.checkOutMailForMissingData(mail);

    SEDBox sb = mdbLookups.getSEDBoxByAddressName(mail.getSenderEBox());
    if (sb == null) {
      String msg =
          "Sender box [SubmitMailRequest/Data/OutMail/@senderEBox]:  " + mail.getSenderEBox() +
          " not exists";
      throw SEDRequestUtils.createSEDException(msg, SEDExceptionCode.INVALID_DATA);
    } else {
      if (sb.getActiveFromDate() != null &&
          sb.getActiveFromDate().after(Calendar.getInstance().getTime())) {
        String msg =
            "Sender box [SubmitMailRequest/Data/OutMail/@senderEBox]:  " + mail.getSenderEBox() +
            " is  active! (Activation from : '" + sb.getActiveFromDate().toString() + "')";
        throw SEDRequestUtils.createSEDException(msg, SEDExceptionCode.INVALID_DATA);
      }
      if (sb.getActiveToDate() != null &&
          sb.getActiveToDate().before(Calendar.getInstance().getTime())) {
        String msg =
            "Sender box [SubmitMailRequest/Data/OutMail/@senderEBox]:  " + mail.getSenderEBox() +
            " is  active! (Activation To : '" + sb.getActiveToDate().toString() + "')";
        throw SEDRequestUtils.createSEDException(msg, SEDExceptionCode.INVALID_DATA);
      }
    }

    // check if mail already exists
    OutMail om = mailExists(mail);
    if (om == null) {
      // validate mail data
      PMode pmd = validateOutMailData(mail);

      // serialize payload to cache FS and data to db
      serializeMail(mail, submitMailRequest.getControl().getUserId(), submitMailRequest
          .getControl().getApplicationId(), pmd.getId());

    }
    // generate response
    SubmitMailResponse rsp = new SubmitMailResponse();
    rsp.setRControl(new RControl());
    rsp.getRControl().setReturnValue(
        om != null ? SVEVReturnValue.WARNING.getValue() : SVEVReturnValue.OK.getValue());
    rsp.getRControl().setReturnText(
        om != null ? String.format("Mail with sender message id '%s' already sent before: %s",
                om.getSenderMessageId(), msdfDDMMYYYY_HHMMSS.format(om.getSubmittedDate())) : "");
    // set data
    rsp.setRData(new SubmitMailResponse.RData());
    rsp.getRData().setSubmittedDate(om != null ? om.getSubmittedDate() : mail.getSubmittedDate());
    rsp.getRData().setSenderMessageId(
        om != null ? om.getSenderMessageId() : mail.getSenderMessageId());
    rsp.getRData().setMailId(om != null ? om.getId() : mail.getId());
    return rsp;
  }

  private PMode validateOutMailData(OutMail mail)
      throws SEDException_Exception {

    if (SEDRequestUtils.isValidMailAddress(mail.getReceiverEBox())) {
      throw SEDRequestUtils.createSEDException("Invalid format: ReceiverEBox",
          SEDExceptionCode.INVALID_DATA);
    }

    if (SEDRequestUtils.isValidMailAddress(mail.getSenderEBox())) {
      throw SEDRequestUtils.createSEDException("Invalid format: SenderEBox",
          SEDExceptionCode.INVALID_DATA);
    }

    PartyIdentitySet sPID;
    PartyIdentitySet rPID;
    Service srv;

    PMode pm;
    try {
      sPID = mpModeManager.getPartyIdentitySetForSEDAddress(mail.getSenderEBox());
      rPID = mpModeManager.getPartyIdentitySetForSEDAddress(mail.getReceiverEBox());
      srv = mpModeManager.getServiceById(mail.getService());
      Service.Action act = PModeUtils.getActionFromService(mail.getAction(), srv);
      pm = mpModeManager.getPModeForLocalPartyAsSender(sPID.getId(), act.getSendingRole(),
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
        LOG.formatedlog("Deleted file in a rollback: %s ", frb.getAbsolutePath());
      } else {
        LOG.formatedWarning("Failed to deleted file in a rollback: %s ", frb.getAbsolutePath());
      }
    }

  }

}
