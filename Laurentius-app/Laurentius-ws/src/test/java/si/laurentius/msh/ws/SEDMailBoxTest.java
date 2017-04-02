/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.laurentius.msh.ws;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import si.laurentius.GetInMailRequest;
import si.laurentius.GetInMailResponse;
import si.laurentius.InMailEventListRequest;
import si.laurentius.InMailEventListResponse;
import si.laurentius.InMailListRequest;
import si.laurentius.InMailListResponse;
import si.laurentius.ModifOutActionCode;
import si.laurentius.ModifyActionCode;
import si.laurentius.ModifyInMailRequest;
import si.laurentius.ModifyInMailResponse;
import si.laurentius.ModifyOutMailRequest;
import si.laurentius.ModifyOutMailResponse;
import si.laurentius.OutMailEventListRequest;
import si.laurentius.OutMailEventListResponse;
import si.laurentius.OutMailListRequest;
import si.laurentius.OutMailListResponse;
import si.laurentius.SEDExceptionCode;
import si.laurentius.SEDException_Exception;
import si.laurentius.SubmitMailRequest;
import si.laurentius.SubmitMailResponse;
import si.laurentius.control.Control;
import si.laurentius.inbox.event.InEvent;
import si.laurentius.inbox.mail.InMail;
import si.laurentius.inbox.payload.InPart;
import si.laurentius.inbox.payload.InPayload;
import si.laurentius.outbox.event.OutEvent;
import si.laurentius.outbox.mail.OutMail;
import si.laurentius.outbox.payload.OutPart;
import si.laurentius.outbox.payload.OutPayload;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.enums.SEDInboxMailStatus;
import si.laurentius.commons.enums.SEDOutboxMailStatus;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.exception.HashException;
import si.laurentius.commons.exception.SVEVReturnValue;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.pmode.FilePModeManager;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.Utils;
import si.laurentius.lce.DigestUtils;
import si.laurentius.msh.test.db.MockUserTransaction;
import si.laurentius.msh.test.db.SEDTestLookup;
import si.laurentius.msh.test.db.TestWebServiceContext;

/**
 *
 * @author Jože Rihtaršič
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SEDMailBoxTest extends TestUtils {

  public static final String INIT_LOOKUPS_RESOURCE_PATH = "/sed-lookups.xml";
  public static final String INIT_PMODE_RESOURCE_PATH = "/pmode-conf.xml";

  /**
   *
   */
  public static final Logger LOG = Logger.getLogger(SEDMailBoxTest.class);

  static SEDMailBox mTestInstance = new SEDMailBox();
  static EntityManagerFactory memfFactory = null;
  static EntityManagerFactory memfMSHFactory = null;

  /**
   *
   * @throws Exception
   */
  @BeforeClass
  public static void startClass() throws Exception {

    try {
      // ---------------------------------
      // set logger
      setLogger(SEDMailBoxTest.class.getSimpleName());

      // ---------------------------------
      // set system variables
      // create home dir in target
      Files.createDirectory(Paths.get(LAU_HOME));
      System.getProperties().
              put(SEDSystemProperties.SYS_PROP_HOME_DIR, LAU_HOME);
      System.setProperty(SEDSystemProperties.SYS_PROP_JNDI_PREFIX, "");
      System.setProperty(SEDSystemProperties.SYS_PROP_JNDI_JMS_PREFIX, "");

      // ---------------------------------
      // set jms environment
      Queue mshueue = setJMSEnvironment();
      // mTestInstance.JNDI_CONNECTION_FACTORY = JNDI_CONNECTION_FACTORY;
      // mTestInstance.JNDI_QUEUE_NAME = SEDValues.EBMS_QUEUE_JNDI;
      mTestInstance.mqMSHQueue = mshueue;
      
      mTestInstance.mwsCtxt = new TestWebServiceContext("ApplicationId");

      // ---------------------------------
      // set lookups     
      mTestInstance.mdbLookups = new SEDTestLookup(SEDMailBoxTest.class.
              getResourceAsStream(
                      INIT_LOOKUPS_RESOURCE_PATH));

      mTestInstance.mpModeManager = new FilePModeManager(SEDMailBoxTest.class.
              getResourceAsStream(
                      INIT_PMODE_RESOURCE_PATH));

      memfMSHFactory = Persistence.createEntityManagerFactory(
              PERSISTENCE_UNIT_NAME);
      memfFactory = Persistence.
              createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
      // msLookup.memEManager = memfMSHFactory.createEntityManager();
      mTestInstance.memEManager = memfFactory.createEntityManager();
      // msLookup.mutUTransaction = new MockUserTransaction(msLookup.memEManager.getTransaction());
      mTestInstance.mutUTransaction
              = new MockUserTransaction(mTestInstance.memEManager.
                      getTransaction());

      // set lookup
    } catch (NamingException | JMSException ex) {
      LOG.error("ERROR startClass", ex);
    }
  }

  /**
   *
   * @throws Exception
   */
  @AfterClass
  public static void tearDownClass() throws Exception {
  }


  StorageUtils msuStorageUtils = new StorageUtils();

  private void assertModifyOutMail(OutMail om, SEDOutboxMailStatus startOMStatus,
          ModifOutActionCode oac, SEDOutboxMailStatus endOMStatus,
          SEDExceptionCode ecExpected)
          throws StorageException, HashException {

    storeUpdateOutMail(om, startOMStatus);

    ModifyOutMailRequest momr = new ModifyOutMailRequest();
    momr.setControl(createControl());
    momr.setData(new ModifyOutMailRequest.Data());
    momr.getData().setMailId(om.getId());
    momr.getData().setSenderEBox(om.getSenderEBox());
    momr.getData().setAction(oac);

    try {
      ModifyOutMailResponse mer = mTestInstance.modifyOutMail(momr);
      assertNotNull("Response", mer);
      assertNotNull("Response/RControl", mer.getRControl());
      assertNotNull("Response/RControl/@returnValue", mer.getRControl().
              getReturnValue());
      assertEquals("Response/RControl/@returnValue", mer.getRControl().
              getReturnValue().intValue(),
              SVEVReturnValue.OK.getValue());
      assertNotNull("Response/RData", mer.getRData());
      assertNotNull("Response/RData/OutEvent", mer.getRData().getOutEvent());
      assertEquals("Mail id", om.getId(), mer.getRData().getOutEvent().
              getMailId());
//      assertEquals("Sender box", om.getSenderEBox(), mer.getRData().getOutEvent().getSenderEBox());

      OutMail omtest = getOutMail(om.getId());
      assertNotNull("OutMail not exists in db", omtest);
      assertEquals("Wrong modified end status", endOMStatus.getValue(), omtest.
              getStatus());

    } catch (SEDException_Exception ex) {
      assertNotNull(
              "Error " + ex.getMessage() + " is not exptected to be thrown",
              ecExpected);
      assertNotNull("SEDException_Exception/FaultInfo", ex.getFaultInfo());
      assertNotNull("SEDException_Exception/FaultInfo/ErrorCode", ex.
              getFaultInfo().getErrorCode());
      assertEquals("Erro code", ecExpected, ex.getFaultInfo().getErrorCode());
    }

  }

  private void assertThrowErrorOnSubmit(SubmitMailRequest smr,
          String assertMessage,
          SEDExceptionCode ecExpected) {

    SEDException_Exception ex = null;
    try {
      mTestInstance.submitMail(smr);
    } catch (SEDException_Exception exRes) {
      ex = exRes;
    }
    assertNotNull(assertMessage, ex);
    assertEquals(ecExpected, ex.getFaultInfo().getErrorCode());

  }

  private Control createControl() {

    Control c = new Control();
    c.setApplicationId("ApplicationId");
    c.setUserId("UserId");
    return c;

  }

  private InMail createInMail() {

    InMail im = new InMail();

    im.setSenderMessageId("SM_ID-" + UUID.randomUUID().toString());
    im.setAction("action");
    im.setService("LegalDelivery_ZPP");
    im.setConversationId(UUID.randomUUID().toString());
    im.setReceiverName("Mr. Receiver Name");
    im.setSenderEBox("receiver.name@test-sed.si");
    im.setSenderName("Mr. Sender Name");
    im.setReceiverEBox("izvrsba@test-sed.si");

    String testContent = "Test content";
    im.setInPayload(new InPayload());
    InPart ip = new InPart();
    ip.setFilename("Test.txt");
    ip.setDescription("test attachment");
    ip.setBin(testContent.getBytes());
    ip.setMimeType(MimeValue.MIME_TEXI.getMimeType());
    InPart.Property iprop1 = new InPart.Property();
    iprop1.setName("Property 1");
    iprop1.setValue("value");
    InPart.Property iprop2 = new InPart.Property();
    iprop2.setName("Property 2");
    iprop2.setValue("value");

    ip.getProperties().add(iprop1);
    ip.getProperties().add(iprop2);

    im.getInPayload().getInParts().add(ip);

    return im;

  }

  private OutMail createOutMail() {

    OutMail om = new OutMail();

    om.setSenderMessageId("SM_ID-" + UUID.randomUUID().toString());
    om.setAction("DeliveryNotification");
    om.setService("LegalDelivery_ZPP");
    om.setConversationId(UUID.randomUUID().toString());
    om.setReceiverName("Mr. Receiver Name");
    om.setReceiverEBox("receiver.name@test-sed.si");
    om.setSenderName("Mr. Sender Name");
    om.setSenderEBox("izvrsba@test-sed.si");

    String testContent = "Test content";
    om.setOutPayload(new OutPayload());
    OutPart op = new OutPart();
    op.setFilename("Test.txt");
    op.setDescription("test attachment");
    op.setBin(testContent.getBytes());
    op.setMimeType(MimeValue.MIME_TEXI.getMimeType());

    OutPart.Property iprop1 = new OutPart.Property();
    iprop1.setName("Property 1");
    iprop1.setValue("value");
    OutPart.Property iprop2 = new OutPart.Property();
    iprop2.setName("Property 2");
    iprop2.setValue("value");

    op.getProperties().add(iprop1);
    op.getProperties().add(iprop2);

    om.getOutPayload().getOutParts().add(op);

    return om;

  }

  private OutMail getOutMail(BigInteger iId) {
    EntityManager me = null;
    OutMail om = null;
    try {
      me = memfFactory.createEntityManager();
      om = me.find(OutMail.class, iId);
    } finally {
      if (me != null) {
        me.close();
      }
    }
    return om;
  }

  /**
   *
   */
  @Before
  public void setUp() {

  }

  private void storeInMail(InMail mail) throws StorageException, HashException {

    EntityManager me = null;
    try {
      // --------------------
      // serialize payload

      if (mail.getInPayload() != null && !mail.getInPayload().getInParts().
              isEmpty()) {
        for (InPart p : mail.getInPayload().getInParts()) {
          File fout = null;

          if (p.getBin() != null) {
            fout = msuStorageUtils.storeInFile(p.getMimeType(), p.getBin());
            // purge binary data
            // p.setValue(null);
          } else if (!Utils.isEmptyString(p.getFilepath())) {
            File fIn = new File(p.getFilepath());
            if (fIn.exists()) {
              fout = msuStorageUtils.storeOutFile(p.getMimeType(), fIn);
            }
          }
          // set hash and relative path;
          if (fout != null) {

            String relPath = StorageUtils.getRelativePath(fout);
            p.setFilepath(relPath);
            String hashValue = DigestUtils.getHexSha1Digest(fout);
            p.setSha1Value(hashValue);
            p.setSize(BigInteger.valueOf(fout.length()));

            if (Utils.isEmptyString(p.getFilename())) {
              p.setFilename(fout.getName());
            }
            if (Utils.isEmptyString(p.getName())) {
              p.setName(p.getFilename().substring(p.getFilename().lastIndexOf(
                      ".")));
            }
          }
        }
      }

      me = memfFactory.createEntityManager();
      mail.setReceivedDate(Calendar.getInstance().getTime());
      mail.setStatusDate(mail.getReceivedDate());
      mail.setStatus(SEDInboxMailStatus.RECEIVED.getValue());
      me.getTransaction().begin();
      me.persist(mail);

      InEvent mevent = new InEvent();

      mevent.setMailId(mail.getId());
      mevent.setStatus(mail.getStatus());
      mevent.setDate(mail.getStatusDate());
      mevent.setUserId("userID");
      mevent.setApplicationId("applicationId");
      me.persist(mevent);
      me.getTransaction().commit();
    } finally {
      if (me != null) {
        me.close();
      }
    }

  }

  private void storeUpdateOutMail(OutMail mail, SEDOutboxMailStatus st) throws StorageException,
          HashException {
    if (mail.getId() != null) {
      EntityManager me = null;
      try {

        me = memfFactory.createEntityManager();
        mail.setReceivedDate(Calendar.getInstance().getTime());
        mail.setStatusDate(mail.getReceivedDate());
        mail.setStatus(st.getValue());
        me.getTransaction().begin();
        me.merge(mail);

        OutEvent mevent = new OutEvent();

        mevent.setMailId(mail.getId());
//        mevent.setSenderEBox(mail.getSenderEBox());
        mevent.setStatus(mail.getStatus());
        mevent.setDate(mail.getStatusDate());
        mevent.setUserId("userID");
        mevent.setApplicationId("applicationId");
        me.persist(mevent);
        me.getTransaction().commit();
      } finally {
        if (me != null) {
          me.close();
        }
      }

    } else {

      EntityManager me = null;
      try {
        // --------------------
        // serialize payload

        if (mail.getOutPayload() != null && !mail.getOutPayload().getOutParts().
                isEmpty()) {
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

              String relPath = StorageUtils.getRelativePath(fout);
              p.setFilepath(relPath);
              String hashValue =DigestUtils.getHexSha1Digest(fout);
              p.setSha1Value(hashValue);
              p.setSize(BigInteger.valueOf(fout.length()));

              if (Utils.isEmptyString(p.getFilename())) {
                p.setFilename(fout.getName());
              }
              if (Utils.isEmptyString(p.getName())) {
                p.setName(p.getFilename().substring(p.getFilename().lastIndexOf(
                        ".")));
              }
            }
          }
        }

        me = memfFactory.createEntityManager();
        mail.setReceivedDate(Calendar.getInstance().getTime());
        mail.setStatusDate(mail.getReceivedDate());
        mail.setStatus(st.getValue());
        me.getTransaction().begin();
        me.persist(mail);

        OutEvent mevent = new OutEvent();

        mevent.setMailId(mail.getId());
//        mevent.setSenderEBox(mail.getSenderEBox());
        mevent.setStatus(mail.getStatus());
        mevent.setDate(mail.getStatusDate());
        mevent.setUserId("userID");
        mevent.setApplicationId("applicationId");
        me.persist(mevent);
        me.getTransaction().commit();
      } finally {
        if (me != null) {
          me.close();
        }
      }
    }

  }

  /**
   *
   */
  @After
  public void tearDown() {
  }

  /**
   * Method test SubmitMail
   *
   * @throws Exception
   */
  @Test
  public void test_A_SubmitMail() throws Exception {
    LOG.info("test_A_SubmitMail");
    // create request
    SubmitMailRequest smr = new SubmitMailRequest();
    smr.setControl(createControl());
    smr.setData(new SubmitMailRequest.Data());
    smr.getData().setOutMail(createOutMail());
    // submit request
    SubmitMailResponse mr = mTestInstance.submitMail(smr);
    assertNotNull("Response", mr);
    assertNotNull("Response/RControl", mr.getRControl());
    assertNotNull("Response/RControl/@returnValue", mr.getRControl().
            getReturnValue());
    assertEquals("Response/RControl/@returnValue", mr.getRControl().
            getReturnValue().intValue(),
            SVEVReturnValue.OK.getValue());
    assertNotNull("Response/RData", mr.getRData());
    assertNotNull("Response/RData/@mailId", mr.getRData().getMailId());
    assertNotNull("Response/RData/@submitDate", mr.getRData().getSubmittedDate());
    assertNotNull("SenderMessageId", mr.getRData().getSenderMessageId());
    assertEquals("SenderMessageId", mr.getRData().getSenderMessageId(), smr.
            getData().getOutMail()
            .getSenderMessageId());

  }

  /**
   * Method test ValidationOfRequest of SubmitMail
   *
   * @throws Exception
   */
  @Test
  public void test_B_SubmitMail_ValidationOfRequest() throws Exception {
    LOG.info("test_B_SubmitMail_ValidationOfRequest");
    // create sumbmit OK mail
    SubmitMailRequest smr = new SubmitMailRequest();
    smr.setControl(createControl());
    smr.setData(new SubmitMailRequest.Data());
    smr.getData().setOutMail(createOutMail());

    // check Control
    Control c = smr.getControl();
    smr.setControl(null);
    assertThrowErrorOnSubmit(smr, "Missing Control",
            SEDExceptionCode.MISSING_DATA);
    smr.setControl(c);
    // check Control/@applicationId
    String aid = c.getApplicationId();
    c.setApplicationId(null);
    assertThrowErrorOnSubmit(smr, "Missing Control/@applicationId",
            SEDExceptionCode.MISSING_DATA);
    c.setApplicationId(aid);
    // check Control/@userId
    String userid = c.getUserId();
    c.setUserId(null);
    assertThrowErrorOnSubmit(smr, "Missing Control/@userId",
            SEDExceptionCode.MISSING_DATA);
    c.setUserId(userid);
    // check Data
    SubmitMailRequest.Data dt = smr.getData();
    smr.setData(null);
    assertThrowErrorOnSubmit(smr, "Missing Data", SEDExceptionCode.MISSING_DATA);
    smr.setData(dt);
    // check Data/OutMail
    OutMail om = smr.getData().getOutMail();
    smr.getData().setOutMail(null);
    assertThrowErrorOnSubmit(smr, "Missing Data/OutMail",
            SEDExceptionCode.MISSING_DATA);
    smr.getData().setOutMail(om);

    // check Data/OutMail/@action
    String value = om.getAction();
    om.setAction(null);
    assertThrowErrorOnSubmit(smr, "Missing Data/OutMail/@action",
            SEDExceptionCode.MISSING_DATA);
    om.setAction(value);
    // check Data/OutMail/@service
    value = om.getService();
    om.setService(null);
    assertThrowErrorOnSubmit(smr, "Missing Data/OutMail/@service",
            SEDExceptionCode.MISSING_DATA);
    om.setService(value);
    // check Data/OutMail/@ConversationId
    value = om.getConversationId();
    om.setConversationId(null);
    assertThrowErrorOnSubmit(smr, "Missing Data/OutMail/@ConversationId",
            SEDExceptionCode.MISSING_DATA);
    om.setConversationId(value);

    // check Data/OutMail/@senderMessageId
    value = om.getSenderMessageId();
    om.setSenderMessageId(null);
    assertThrowErrorOnSubmit(smr, "Missing Data/OutMail/@senderMessageId",
            SEDExceptionCode.MISSING_DATA);
    om.setSenderMessageId(value);
    // check Data/OutMail/@SenderName
    value = om.getSenderName();
    om.setSenderName(null);
    assertThrowErrorOnSubmit(smr, "Missing Data/OutMail/@SenderName",
            SEDExceptionCode.MISSING_DATA);
    om.setSenderName(value);
    // check Data/OutMail/@SenderEBox
    value = om.getSenderEBox();
    om.setSenderEBox(null);
    assertThrowErrorOnSubmit(smr, "Missing Data/OutMail/@SenderEBox",
            SEDExceptionCode.MISSING_DATA);
    om.setSenderEBox(value);

    // check Data/OutMail/@ReceiverName
    value = om.getReceiverName();
    om.setReceiverName(null);
    assertThrowErrorOnSubmit(smr, "Missing Data/OutMail/@ReceiverName",
            SEDExceptionCode.MISSING_DATA);
    om.setReceiverName(value);
    // check Data/OutMail/@SenderEBox
    value = om.getReceiverEBox();
    om.setReceiverEBox(null);
    assertThrowErrorOnSubmit(smr, "Missing Data/OutMail/@SenderEBox",
            SEDExceptionCode.MISSING_DATA);
    om.setReceiverEBox(value);

    SubmitMailResponse mr = mTestInstance.submitMail(smr);
    assertEquals("Response/RControl/@returnValue", mr.getRControl().
            getReturnValue().intValue(),
            SVEVReturnValue.OK.getValue());
  }

  /**
   * Method test duplicate detection of second mail SubmitMail
   *
   * @throws Exception
   */
  @Test
  public void test_C_SubmitMail_ExistsMail() throws Exception {
    LOG.info("test_C_SubmitMail_ExistsMail");
    // create request
    SubmitMailRequest smr = new SubmitMailRequest();
    smr.setControl(createControl());
    smr.setData(new SubmitMailRequest.Data());
    smr.getData().setOutMail(createOutMail());

    // submit for first time,
    SubmitMailResponse mr = mTestInstance.submitMail(smr);
    assertEquals("Response/RControl/@returnValue", mr.getRControl().
            getReturnValue().intValue(),
            SVEVReturnValue.OK.getValue());

    SubmitMailResponse secmr = mTestInstance.submitMail(smr);
    assertEquals("Second submission Response/RControl/@returnValue", secmr.
            getRControl()
            .getReturnValue().intValue(), SVEVReturnValue.WARNING.getValue());
    assertEquals("Second submission Response/RData/@submitDate", mr.getRData().
            getSubmittedDate(),
            secmr.getRData().getSubmittedDate());
    assertEquals("Second submission Response/RData/@mailId", mr.getRData().
            getMailId(), secmr
                    .getRData().getMailId());
    assertEquals("Second submission Response/RData/@senderMessageId", mr.
            getRData()
            .getSenderMessageId(), secmr.getRData().getSenderMessageId());

  }

  /**
   * Test of getOutMailList method, of class SEDMailBox. Method tests search
   * parameters
   *
   * @throws java.lang.Exception
   */
  @Test
  public void test_D_GetOutMailList() throws Exception {
    LOG.info("test_D_GetOutMailList");
    OutMailListRequest omr = new OutMailListRequest();
    omr.setControl(createControl());
    omr.setData(new OutMailListRequest.Data());
    //omr.getData().setSenderEBox("ceftestparty2gw@test-sed.si");
    omr.getData().setSenderEBox("izvrsba@test-sed.si");

    OutMailListResponse mlr = mTestInstance.getOutMailList(omr);
    assertNotNull("Response", mlr);
    assertNotNull("Response/RControl", mlr.getRControl());
    assertNotNull("Response/RControl/@returnValue", mlr.getRControl().
            getReturnValue());
    assertEquals("Response/RControl/@returnValue", mlr.getRControl().
            getReturnValue().intValue(),
            SVEVReturnValue.OK.getValue());
    assertNotNull("Response/RControl/@resultSize", mlr.getRControl().
            getResultSize());
    assertNotNull("Response/RControl/@responseSize", mlr.getRControl().
            getResponseSize());
    assertNotNull("Response/RControl/@StartIndex", mlr.getRControl().
            getStartIndex());
    assertNotNull("Response/RData", mlr.getRData());
    assertEquals("Response/RControl/@resultSize -> getOutMails().size()", mlr.
            getRData()
            .getOutMails().size(), mlr.getRControl().getResultSize().intValue());
    assertEquals("Response/RControl/@resultSize -> getOutMails().size()", mlr.
            getRData()
            .getOutMails().size(), mlr.getRControl().getResponseSize().
                    intValue());
    int iResCNt = mlr.getRData().getOutMails().size();
    // -----------------------------------------
    // / add five new objects
    int TEST_CNT = 5;

    SubmitMailRequest smr = new SubmitMailRequest();
    smr.setControl(createControl());
    smr.setData(new SubmitMailRequest.Data());
    // wait for second for testing time interval search
    long l = Calendar.getInstance().getTimeInMillis();
    while (l + 1000 > Calendar.getInstance().getTimeInMillis()) {
      // wait for second seconds.. // test
    }
    Date dt = Calendar.getInstance().getTime();

    for (int i = 0; i < TEST_CNT; i++) {

      smr.getData().setOutMail(createOutMail()); // create new mail
      SubmitMailResponse mr = mTestInstance.submitMail(smr);
      assertEquals("Response/RControl/@returnValue", mr.getRControl().
              getReturnValue().intValue(),
              SVEVReturnValue.OK.getValue());
    }
    // check new list

    mlr = mTestInstance.getOutMailList(omr);
    assertEquals("Response/RControl/@returnValue", mlr.getRControl().
            getReturnValue().intValue(),
            SVEVReturnValue.OK.getValue());
    assertEquals("Response/RControl/@resultSize -> getOutMails().size()", mlr.
            getRData()
            .getOutMails().size(), mlr.getRControl().getResultSize().intValue());
    assertEquals("Response/RControl/@resultSize -> increased",
            TEST_CNT + iResCNt, mlr.getRData()
                    .getOutMails().size());

    // wait for second for testing time interval search
    Date dto = Calendar.getInstance().getTime();
    l = Calendar.getInstance().getTimeInMillis();
    while (l + 1000 > Calendar.getInstance().getTimeInMillis()) {
      // wait for second seconds.. // test
    }

    // -----------------------------------------
    // add new mail with unique service, action, sender box, receiver box
    String valTest = "connectivity-service"; // must be declared in pmod conf
    String action = "submitMessage"; // must be declared in pmod confž
    String senderBox = "ceftestparty2gw@test-sed.si";
    OutMail om = createOutMail();

    om.setService(valTest);
    om.setAction(action);
    
    om.setSenderEBox(senderBox);
    om.setReceiverEBox(UUID.randomUUID().toString() + "@test-sed.si");
    smr.getData().setOutMail(om); // create new mail
    // add new mail
    SubmitMailResponse mr = mTestInstance.submitMail(smr);
    assertEquals("Response/RControl/@returnValue", mr.getRControl().
            getReturnValue().intValue(),
            SVEVReturnValue.OK.getValue());

    // test search for service
    omr.getData().setService(om.getService());
    omr.getData().setSenderEBox(senderBox);
    mlr = mTestInstance.getOutMailList(omr);
    omr.getData().setService(null);
    assertEquals("Service Response/RControl/@returnValue", mlr.getRControl().
            getReturnValue()
            .intValue(), SVEVReturnValue.OK.getValue());
    assertEquals("Test service parameter", 1, mlr.getRData().getOutMails().
            size());

    assertEquals("Test service response id", om.getId(), mlr.getRData().
            getOutMails().get(0)
            .getId());

    // test search for action
    omr.getData().setAction((om.getAction()));
    mlr = mTestInstance.getOutMailList(omr);
    omr.getData().setAction(null);
    assertEquals("Action Response/RControl/@returnValue", mlr.getRControl().
            getReturnValue()
            .intValue(), SVEVReturnValue.OK.getValue());
    assertEquals("Test Action parameter", 1, mlr.getRData().getOutMails().size());
    assertEquals("Test  Action response id", om.getId(), mlr.getRData().
            getOutMails().get(0)
            .getId());

    // test search for ConversationId
    omr.getData().setConversationId(om.getConversationId());
    mlr = mTestInstance.getOutMailList(omr);
    omr.getData().setConversationId(null);
    assertEquals("ConversationId Response/RControl/@returnValue", mlr.
            getRControl()
            .getReturnValue().intValue(), SVEVReturnValue.OK.getValue());
    assertEquals("Test ConversationId parameter", 1, mlr.getRData().
            getOutMails().size());
    assertEquals("Test ConversationId response id", om.getId(), mlr.getRData().
            getOutMails().get(0)
            .getId());

    // test search for SenderEBox
    omr.getData().setSenderEBox((om.getSenderEBox()));
    mlr = mTestInstance.getOutMailList(omr);
    assertEquals("SenderEBox Response/RControl/@returnValue", mlr.getRControl().
            getReturnValue()
            .intValue(), SVEVReturnValue.OK.getValue());
    assertEquals("Test SenderEBox parameter", 1, mlr.getRData().getOutMails().
            size());
    assertEquals("Test SenderEBox response id", om.getId(), mlr.getRData().
            getOutMails().get(0)
            .getId());

    // test search for ReceiverEBox
    omr.getData().setReceiverEBox((om.getReceiverEBox()));
    mlr = mTestInstance.getOutMailList(omr);
    omr.getData().setReceiverEBox(null);
    assertEquals("ReceiverEBox Response/RControl/@returnValue", mlr.
            getRControl().getReturnValue()
            .intValue(), SVEVReturnValue.OK.getValue());
    assertEquals("Test ReceiverEBox parameter", 1, mlr.getRData().getOutMails().
            size());
    assertEquals("Test ReceiverEBox response id", om.getId(), mlr.getRData().
            getOutMails().get(0)
            .getId());

    // test search for SubmittedDateFrom
    omr.getData().setSubmittedDateFrom(dt);
    omr.getData().setSenderEBox("izvrsba@test-sed.si");
    mlr = mTestInstance.getOutMailList(omr);
    assertEquals("SubmittedDateFrom Response/RControl/@returnValue", mlr.
            getRControl()
            .getReturnValue().intValue(), SVEVReturnValue.OK.getValue());
    assertEquals("Test SubmittedDateFrom parameter", TEST_CNT , mlr.
            getRData().getOutMails()
            .size());

    // test search for SubmittedDateFrom
    omr.getData().setSubmittedDateFrom(dt);
    omr.getData().setSubmittedDateTo(dto);
    mlr = mTestInstance.getOutMailList(omr);
    assertEquals("SubmittedDateFrom Response/RControl/@returnValue", mlr.
            getRControl()
            .getReturnValue().intValue(), SVEVReturnValue.OK.getValue());
    assertEquals("Test SubmittedDateFrom parameter", TEST_CNT, mlr.getRData().
            getOutMails().size());

    // ------------------------
    // TEST pagination
    omr.getData().setSubmittedDateFrom(null);
    omr.getData().setSubmittedDateTo(null);
    omr.getControl().setStartIndex(BigInteger.valueOf(iResCNt));
    omr.getControl().setResponseSize(BigInteger.valueOf(TEST_CNT));
    mlr = mTestInstance.getOutMailList(omr);
    assertEquals("SubmittedDateFrom Response/RControl/@returnValue", mlr.
            getRControl()
            .getReturnValue().intValue(), SVEVReturnValue.OK.getValue());
    assertEquals("service RControl/@ResultSize", TEST_CNT + iResCNt , mlr.
            getRControl()
            .getResultSize().intValue());
    assertEquals("service RControl/@getResponseSize", TEST_CNT, mlr.
            getRControl().getResponseSize()
            .intValue());
  }

  /**
   * Test of getOutMailEventList method, of class SEDMailBox.
   *
   * @throws java.lang.Exception
   */
  @Test
  public void test_E_GetOutMailEventList() throws Exception {
    LOG.info("test_E_GetOutMailEventList");

    // submit mail
    OutMail om = createOutMail();
    SubmitMailRequest smr = new SubmitMailRequest();
    smr.setControl(createControl());
    smr.setData(new SubmitMailRequest.Data());
    smr.getData().setOutMail(om); // create new mail
    SubmitMailResponse mr = mTestInstance.submitMail(smr);
    assertEquals("Response/RControl/@returnValue", mr.getRControl().
            getReturnValue().intValue(),
            SVEVReturnValue.OK.getValue());

    // retrieve mail events by id
    OutMailEventListRequest omer = new OutMailEventListRequest();
    omer.setControl(createControl());
    omer.setData(new OutMailEventListRequest.Data());
    omer.getData().setMailId(om.getId());
    omer.getData().setSenderEBox(om.getSenderEBox());
    OutMailEventListResponse mer = mTestInstance.getOutMailEventList(omer);

    assertNotNull("Response", mer);
    assertNotNull("Response/RControl", mer.getRControl());
    assertNotNull("Response/RControl/@returnValue", mer.getRControl().
            getReturnValue());
    assertEquals("Response/RControl/@returnValue", mer.getRControl().
            getReturnValue().intValue(),
            SVEVReturnValue.OK.getValue());
    assertNotNull("Response/RControl/@resultSize", mer.getRControl().
            getResultSize());
    assertNotNull("Response/RControl/@responseSize", mer.getRControl().
            getResponseSize());
    assertNotNull("Response/RControl/@StartIndex", mer.getRControl().
            getStartIndex());
    assertNotNull("Response/RData", mer.getRData());
    assertEquals("Response/RControl/@resultSize -> OutMailEvent().size()", mer.
            getRData()
            .getOutEvents().size(), mer.getRControl().getResultSize().intValue());
    assertEquals("Response/RControl/@resultSize -> OutMailEvent().size()", mer.
            getRData()
            .getOutEvents().size(), mer.getRControl().getResponseSize().
                    intValue());
    assertTrue("Response/RControl/@resultSize -> OutMailEvent().size()", mer.
            getRData()
            .getOutEvents().size() > 0);
    int iResult = mer.getRData().getOutEvents().size();

    // retrieve mail events by id
    omer.getData().setMailId(null);
    omer.getData().setSenderEBox(om.getSenderEBox());
    omer.getData().setSenderMessageId(om.getSenderMessageId());

    mer = mTestInstance.getOutMailEventList(omer);
    assertEquals("Response/RControl/@returnValue", mer.getRControl().
            getReturnValue().intValue(),
            SVEVReturnValue.OK.getValue());
    assertTrue("Response/RControl/@resultSize -> OutMailEvent().size()", mer.
            getRData()
            .getOutEvents().size() == iResult);
  }

  /**
   * Test of getInMailList method, of class SEDMailBox.
   *
   * @throws java.lang.Exception
   */
  @Test
  public void test_F_GetInMailList() throws Exception {
    LOG.info("test_F_GetInMailList");
    // prepare
    InMail im = createInMail();
    storeInMail(im);
    //

    InMailListRequest imr = new InMailListRequest();
    imr.setControl(createControl());
    imr.setData(new InMailListRequest.Data());
    imr.getData().setReceiverEBox("izvrsba@test-sed.si");

    InMailListResponse mlr = mTestInstance.getInMailList(imr);
    assertNotNull("Response", mlr);
    assertNotNull("Response/RControl", mlr.getRControl());
    assertNotNull("Response/RControl/@returnValue", mlr.getRControl().
            getReturnValue());
    assertEquals("Response/RControl/@returnValue", mlr.getRControl().
            getReturnValue().intValue(),
            SVEVReturnValue.OK.getValue());
    assertNotNull("Response/RControl/@resultSize", mlr.getRControl().
            getResultSize());
    assertNotNull("Response/RControl/@responseSize", mlr.getRControl().
            getResponseSize());
    assertNotNull("Response/RControl/@StartIndex", mlr.getRControl().
            getStartIndex());
    assertNotNull("Response/RData", mlr.getRData());
    assertEquals("Response/RControl/@resultSize -> getOutMails().size()", mlr.
            getRData()
            .getInMails().size(), mlr.getRControl().getResultSize().intValue());
    assertEquals("Response/RControl/@resultSize -> getOutMails().size()", mlr.
            getRData()
            .getInMails().size(), mlr.getRControl().getResponseSize().intValue());
    assertTrue("Response/RControl/@resultSize -> OutMailEvent().size()", mlr.
            getRData()
            .getInMails().size() > 0);
    int iResCNt = mlr.getRData().getInMails().size();

    // -----------------------------------------
    // / add five new objects
    int TEST_CNT = 5;

    // wait for second for testing time interval search
    long l = Calendar.getInstance().getTimeInMillis();
    while (l + 1000 > Calendar.getInstance().getTimeInMillis()) {
      // wait for second seconds.. // test
    }
    Date dt = Calendar.getInstance().getTime();

    for (int i = 0; i < TEST_CNT; i++) {
      storeInMail(createInMail());
    }
    // check new list
    mlr = mTestInstance.getInMailList(imr);
    assertEquals("Response/RControl/@returnValue", mlr.getRControl().
            getReturnValue().intValue(),
            SVEVReturnValue.OK.getValue());
    assertEquals("Response/RControl/@resultSize -> getOutMails().size()", mlr.
            getRData()
            .getInMails().size(), mlr.getRControl().getResultSize().intValue());
    assertEquals("Response/RControl/@resultSize -> increased",
            TEST_CNT + iResCNt, mlr.getRData()
                    .getInMails().size());

    // wait for second for testing time interval search
    Date dto = Calendar.getInstance().getTime();
    l = Calendar.getInstance().getTimeInMillis();
    while (l + 1000 > Calendar.getInstance().getTimeInMillis()) {
      // wait for second seconds.. // test
    }

    // -----------------------------------------
    // add new mail with unique service, action, sender box, receiver box
    String valTest = "TEST_SERVICE"; // must be declared in pmod conf
    InMail im2 = createInMail();

    im2.setService(valTest);
    im2.setAction(UUID.randomUUID().toString());
    String recBox = "izvrsba@test-sed.si";
    im2.setSenderEBox(UUID.randomUUID().toString() + "@test-sed.si");
    im2.setReceiverEBox(recBox);

    storeInMail(im2);

    // test search for service
    imr.getData().setService(im2.getService());
    imr.getData().setReceiverEBox(recBox);
    mlr = mTestInstance.getInMailList(imr);
    imr.getData().setService(null);
    assertEquals("Service Response/RControl/@returnValue", mlr.getRControl().
            getReturnValue()
            .intValue(), SVEVReturnValue.OK.getValue());
    assertEquals("Test service parameter", 1, mlr.getRData().getInMails().size());
    assertEquals("Test service response id", im2.getId(), mlr.getRData().
            getInMails().get(0)
            .getId());

    // test search for action
    imr.getData().setAction((im2.getAction()));
    mlr = mTestInstance.getInMailList(imr);
    imr.getData().setAction(null);
    assertEquals("Action Response/RControl/@returnValue", mlr.getRControl().
            getReturnValue()
            .intValue(), SVEVReturnValue.OK.getValue());
    assertEquals("Test Action parameter", 1, mlr.getRData().getInMails().size());
    assertEquals("Test  Action response id", im2.getId(), mlr.getRData().
            getInMails().get(0)
            .getId());

    // test search for ConversationId
    imr.getData().setConversationId(im2.getConversationId());
    mlr = mTestInstance.getInMailList(imr);
    imr.getData().setConversationId(null);
    assertEquals("ConversationId Response/RControl/@returnValue", mlr.
            getRControl()
            .getReturnValue().intValue(), SVEVReturnValue.OK.getValue());
    assertEquals("Test ConversationId parameter", 1,
            mlr.getRData().getInMails().size());
    assertEquals("Test ConversationId response id", im2.getId(), mlr.getRData().
            getInMails().get(0)
            .getId());

    // test search for SenderEBox
    imr.getData().setSenderEBox(im2.getSenderEBox());
    mlr = mTestInstance.getInMailList(imr);
    imr.getData().setSenderEBox(null);
    assertEquals("SenderEBox Response/RControl/@returnValue", mlr.getRControl().
            getReturnValue()
            .intValue(), SVEVReturnValue.OK.getValue());
    assertEquals("Test SenderEBox parameter", 1, mlr.getRData().getInMails().
            size());
    assertEquals("Test SenderEBox response id", im2.getId(), mlr.getRData().
            getInMails().get(0)
            .getId());

    // test search for ReceiverEBox
    imr.getData().setReceiverEBox((im2.getReceiverEBox()));
    mlr = mTestInstance.getInMailList(imr);

    assertEquals("ReceiverEBox Response/RControl/@returnValue", mlr.
            getRControl().getReturnValue()
            .intValue(), SVEVReturnValue.OK.getValue());
    assertEquals("Test ReceiverEBox parameter", 7, mlr.getRData().getInMails().
            size());
    assertEquals("Test ReceiverEBox response id", im2.getId(), mlr.getRData().
            getInMails().get(6)
            .getId());

    // test search for SubmittedDateFrom
    imr.getData().setReceivedDateFrom(dt);
    mlr = mTestInstance.getInMailList(imr);
    assertEquals("SubmittedDateFrom Response/RControl/@returnValue", mlr.
            getRControl()
            .getReturnValue().intValue(), SVEVReturnValue.OK.getValue());
    assertEquals("Test SubmittedDateFrom parameter", TEST_CNT + 1, mlr.
            getRData().getInMails()
            .size());

    // test search for SubmittedDateFrom
    imr.getData().setReceivedDateFrom(dt);
    imr.getData().setReceivedDateTo(dto);
    mlr = mTestInstance.getInMailList(imr);
    assertEquals("SubmittedDateFrom Response/RControl/@returnValue", mlr.
            getRControl()
            .getReturnValue().intValue(), SVEVReturnValue.OK.getValue());
    assertEquals("Test SubmittedDateFrom parameter", TEST_CNT, mlr.getRData().
            getInMails().size());

    // ------------------------
    // TEST pagination
    imr.getData().setReceivedDateFrom(null);
    imr.getData().setReceivedDateTo(null);
    imr.getControl().setStartIndex(BigInteger.valueOf(iResCNt));
    imr.getControl().setResponseSize(BigInteger.valueOf(TEST_CNT));
    mlr = mTestInstance.getInMailList(imr);
    assertEquals("SubmittedDateFrom Response/RControl/@returnValue", mlr.
            getRControl()
            .getReturnValue().intValue(), SVEVReturnValue.OK.getValue());
    assertEquals("service RControl/@ResultSize", TEST_CNT + iResCNt + 1, mlr.
            getRControl()
            .getResultSize().intValue());
    assertEquals("service RControl/@getResponseSize", TEST_CNT, mlr.
            getRControl().getResponseSize()
            .intValue());
  }

  /**
   * Test of getInMailEventList method, of class SEDMailBox.
   *
   * @throws java.lang.Exception
   */
  @Test
  public void test_G_GetInMailEventList() throws Exception {
    LOG.info("test_G_GetInMailEventList");

    // store mail
    InMail im = createInMail();
    storeInMail(im);

    // retrieve mail events by id
    InMailEventListRequest imer = new InMailEventListRequest();
    imer.setControl(createControl());
    imer.setData(new InMailEventListRequest.Data());
    imer.getData().setMailId(im.getId());
    imer.getData().setReceiverEBox(im.getReceiverEBox());
    InMailEventListResponse mer = mTestInstance.getInMailEventList(imer);

    assertNotNull("Response", mer);
    assertNotNull("Response/RControl", mer.getRControl());
    assertNotNull("Response/RControl/@returnValue", mer.getRControl().
            getReturnValue());
    assertEquals("Response/RControl/@returnValue", mer.getRControl().
            getReturnValue().intValue(),
            SVEVReturnValue.OK.getValue());
    assertNotNull("Response/RControl/@resultSize", mer.getRControl().
            getResultSize());
    assertNotNull("Response/RControl/@responseSize", mer.getRControl().
            getResponseSize());
    assertNotNull("Response/RControl/@StartIndex", mer.getRControl().
            getStartIndex());
    assertNotNull("Response/RData", mer.getRData());
    assertEquals("Response/RControl/@resultSize -> OutMailEvent().size()", mer.
            getRData()
            .getInEvents().size(), mer.getRControl().getResultSize().intValue());
    assertEquals("Response/RControl/@resultSize -> OutMailEvent().size()", mer.
            getRData()
            .getInEvents().size(), mer.getRControl().getResponseSize().
                    intValue());
    assertTrue("Response/RControl/@resultSize -> OutMailEvent().size()", mer.
            getRData()
            .getInEvents().size() > 0);

  }

  /**
   * Test of getInMail method, of class SEDMailBox.
   *
   * @throws java.lang.Exception
   */
  @Test
  public void test_H_GetInMail() throws Exception {
    LOG.info("test_H_GetInMail");

    // store mail
    InMail im = createInMail();
    storeInMail(im);

    // retrieve in mail
    GetInMailRequest gimr = new GetInMailRequest();
    gimr.setControl(createControl());
    gimr.setData(new GetInMailRequest.Data());
    gimr.getData().setMailId(im.getId());
    gimr.getData().setReceiverEBox(im.getReceiverEBox());
    GetInMailResponse mer = mTestInstance.getInMail(gimr);

    assertNotNull("Response", mer);
    assertNotNull("Response/RControl", mer.getRControl());
    assertNotNull("Response/RControl/@returnValue", mer.getRControl().
            getReturnValue());
    assertEquals("Response/RControl/@returnValue", mer.getRControl().
            getReturnValue().intValue(),
            SVEVReturnValue.OK.getValue());
    assertNotNull("Response/RData", mer.getRData());
    assertNotNull("Response/RData/InMail", mer.getRData().getInMail());
    assertEquals("Response/RData/InMail", im.getId(),
            mer.getRData().getInMail().getId());
    assertArrayEquals("Response/RData/getPyloadPart", im.getInPayload().
            getInParts().get(0)
            .getBin(), mer.getRData().getInMail().getInPayload().getInParts().
                    get(0).getBin());

  }

  /**
   * Test of modifyInMail method, of class SEDMailBox.
   *
   * @throws java.lang.Exception
   */
  @Test
  public void test_I_ModifyInMail() throws Exception {
    LOG.info("test_I_ModifyInMail");

    // store mail
    InMail im = createInMail();
    storeInMail(im);

    ModifyInMailRequest mimr = new ModifyInMailRequest();
    mimr.setControl(createControl());
    mimr.setData(new ModifyInMailRequest.Data());
    mimr.getData().setMailId(im.getId());
    mimr.getData().setReceiverEBox(im.getReceiverEBox());
    mimr.getData().setAction(ModifyActionCode.DELIVERED);
    ModifyInMailResponse mer = mTestInstance.modifyInMail(mimr);
    assertNotNull("Response", mer);
    assertNotNull("Response/RControl", mer.getRControl());
    assertNotNull("Response/RControl/@returnValue", mer.getRControl().
            getReturnValue());
    assertEquals("Response/RControl/@returnValue", mer.getRControl().
            getReturnValue().intValue(),
            SVEVReturnValue.OK.getValue());
    assertNotNull("Response/RData", mer.getRData());
    assertNotNull("Response/RData/InEvent", mer.getRData().getInEvent());
    assertEquals("Mail id", im.getId(), mer.getRData().getInEvent().getMailId());

    // get inmail and check status
    GetInMailRequest gimr = new GetInMailRequest();
    gimr.setControl(createControl());
    gimr.setData(new GetInMailRequest.Data());
    gimr.getData().setMailId(im.getId());
    gimr.getData().setReceiverEBox(im.getReceiverEBox());
    GetInMailResponse mrs = mTestInstance.getInMail(gimr);
    assertEquals("Response/RControl/@returnValue", mrs.getRControl().
            getReturnValue().intValue(),
            SVEVReturnValue.OK.getValue());
    assertEquals("Status", SEDInboxMailStatus.DELIVERED.getValue(), mrs.
            getRData().getInMail()
            .getStatus());
    assertEquals("Date", mer.getRData().getInEvent().getDate(), mrs.getRData().
            getInMail()
            .getStatusDate());

  }

  /**
   * Test of modifyInMail method, of class SEDMailBox.
   *
   * @throws java.lang.Exception
   */
  @Test
  public void test_J_ModifyOutMail() throws Exception {
    LOG.info("test_J_ModifyOutMail");

    // assert Delete
    assertModifyOutMail(createOutMail(), SEDOutboxMailStatus.SUBMITTED,
            ModifOutActionCode.DELETE,
            SEDOutboxMailStatus.DELETED, null);
    assertModifyOutMail(createOutMail(), SEDOutboxMailStatus.ERROR,
            ModifOutActionCode.DELETE,
            SEDOutboxMailStatus.DELETED, null);
    assertModifyOutMail(createOutMail(), SEDOutboxMailStatus.FAILED,
            ModifOutActionCode.DELETE,
            SEDOutboxMailStatus.DELETED, null);
    assertModifyOutMail(createOutMail(), SEDOutboxMailStatus.SCHEDULE,
            ModifOutActionCode.DELETE,
            SEDOutboxMailStatus.DELETED, null);
    assertModifyOutMail(createOutMail(), SEDOutboxMailStatus.SENT,
            ModifOutActionCode.DELETE, null,
            SEDExceptionCode.INVALID_DATA);
    assertModifyOutMail(createOutMail(), SEDOutboxMailStatus.PUSHING,
            ModifOutActionCode.DELETE,
            null, SEDExceptionCode.INVALID_DATA);

  }

}
