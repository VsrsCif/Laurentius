/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.ejb;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.BeforeClass;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.enums.SEDInboxMailStatus;
import si.laurentius.commons.enums.SEDMailPartSource;
import si.laurentius.commons.enums.SEDOutboxMailStatus;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.ejb.db.MockUserTransaction;
import si.laurentius.ejb.utils.InitialContextFactoryForTest;
import si.laurentius.ejb.utils.TestLookupUtils;
import si.laurentius.ejb.utils.TestUtils;
import static si.laurentius.ejb.utils.TestUtils.LAU_TEST_DOMAIN;
import static si.laurentius.ejb.utils.TestUtils.setLogger;
import si.laurentius.lce.DigestUtils;
import si.laurentius.msh.inbox.event.MSHInEvent;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.outbox.event.MSHOutEvent;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.outbox.payload.MSHOutPart;

/**
 *
 * @author sluzba
 */
public class SEDDaoBeanTest extends TestUtils {

  static EntityManagerFactory memfMSHFactory = null;
  static SEDDaoBean mTestInstance = new SEDDaoBean();

  @BeforeClass
  public static void setUpClass() throws IOException, NamingException, JMSException {

    // ---------------------------------
    // set logger
    setLogger(SEDDaoBeanTest.class.getSimpleName());

    // create initial context factory 
    System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
            InitialContextFactoryForTest.class.getName());

    // create persistence unit
    memfMSHFactory = Persistence.createEntityManagerFactory(
            PERSISTENCE_UNIT_NAME);
    mTestInstance.memEManager = memfMSHFactory.createEntityManager();
    mTestInstance.mutUTransaction
            = new MockUserTransaction(mTestInstance.memEManager.getTransaction());
    System.setProperty(SEDSystemProperties.SYS_PROP_LAU_DOMAIN, LAU_TEST_DOMAIN);

    setUpStorage("target/storage/SEDDaoBeanTest");

    setupJMS(S_JMS_JNDI_CF, "java:/jms/", Collections.
            singletonList(S_JMS_QUEUE));

  }

  @Test
  public void test_A_01_SerializeOutMail_fillData() throws Exception {
    System.out.println("test_A_01_SerializeOutMail_fillData");

    int iCount = getMessagesInQueue();
    MSHOutMail init = TestLookupUtils.createOutMail();

    init.setMessageId(null);
    init.setConversationId(null);
    init.setSenderName(null);
    init.setReceiverName(null);
    init.setSubmittedDate(null);

    mTestInstance.serializeOutMail(init, "testUser", "testApplication",
            "pmodeid");

    assertNotNull(init.getId());

    clearEntityManagerCahche();  // make sure to read from db
    MSHOutMail fromDB = mTestInstance.
            getMailById(MSHOutMail.class, init.getId());
    assertTrue(init != fromDB); // make sure to read from db

    assertNotNull("Id must not be null", fromDB.getId());
    assertNotNull("MessageId must not be null", fromDB.getMessageId());
    assertNotNull("ConversationId must not be null", fromDB.getConversationId());
    assertNotNull("SenderName must not be null", fromDB.getSenderName());
    assertNotNull("ReceiverName must not be null", fromDB.getReceiverName());
    assertNotNull("SubmittedDate must not be null", fromDB.getSubmittedDate());
    assertNotNull("Status must not be null", fromDB.getStatus());
    assertNotNull("StatusDate must not be null", fromDB.getStatusDate());

    assertEquals(fromDB.getSenderEBox(), fromDB.getSenderName());
    assertEquals(fromDB.getReceiverEBox(), fromDB.getReceiverName());
    assertEquals(++iCount, getMessagesInQueue());

  }

  @Test
  public void test_A_01_SerializeOutMail_events() throws Exception {
    System.out.println("test_A_01_SerializeOutMail_fillData");

    int iCount = getMessagesInQueue();
    MSHOutMail init = TestLookupUtils.createOutMail();

    mTestInstance.serializeOutMail(init, "testUser", "testApplication",
            "pmodeid");

    assertNotNull(init.getId());
    clearEntityManagerCahche();  // make sure to read from db
    MSHOutMail fromDB = mTestInstance.
            getMailById(MSHOutMail.class, init.getId());
    assertTrue(init != fromDB); // make sure to read from db

    assertEquals(++iCount, getMessagesInQueue());

    // test event lists
    List<MSHOutEvent> leEvnt = mTestInstance.getMailEventList(MSHOutEvent.class,
            init.getId());
    assertEquals(2, leEvnt.size());

    assertEquals(SEDOutboxMailStatus.SUBMITTED.getValue(), leEvnt.get(0).
            getStatus());
    assertEquals(SEDOutboxMailStatus.SCHEDULE.getValue(), leEvnt.get(1).
            getStatus());

    assertEquals(fromDB.getSubmittedDate(), leEvnt.get(0).getDate());
    assertTrue(leEvnt.get(1).getDate().compareTo(
            leEvnt.get(0).getDate()) >= 0);
  }

  @Test
  public void test_A_01_SerializeOutMail_payload() throws Exception {
    System.out.println("test_A_01_SerializeOutMail_Payload");

    int iCount = getMessagesInQueue();
    MSHOutMail init = TestLookupUtils.createOutMail();
    for (MSHOutPart op : init.getMSHOutPayload().getMSHOutParts()) {
      op.setMimeType(null);
      op.setSha256Value(null);
      op.setSize(null);
     
    }
    
    assertNotNull(init.getMSHOutPayload());
    assertTrue(init.getMSHOutPayload().getMSHOutParts().size() > 0);

    mTestInstance.serializeOutMail(init, "testUser", "testApplication",
            "pmodeid");

    assertNotNull(init.getId());
    clearEntityManagerCahche();  // make sure to read from db
    MSHOutMail fromDB = mTestInstance.
            getMailById(MSHOutMail.class, init.getId());
    assertTrue(init != fromDB); // make sure to read from db

    assertEquals(++iCount, getMessagesInQueue());

    assertNotNull(fromDB.getMSHOutPayload());

    assertEquals(init.getMSHOutPayload().getMSHOutParts().size(), fromDB.
            getMSHOutPayload().getMSHOutParts().size());

    for (MSHOutPart op : fromDB.getMSHOutPayload().getMSHOutParts()) {
      File f = StorageUtils.getFile(op.getFilepath());
      assertNotNull(op.getSha256Value());
      assertNotNull(op.getSize());
      assertNotNull(op.getMimeType());
      
      assertEquals(SEDMailPartSource.MAIL.getValue(), op.getSource());
      assertEquals(DigestUtils.getHexSha256Digest(f), op.getSha256Value());
      assertEquals(f.length(), op.getSize().longValue());
    }

  }
  
  
   @Test
  public void test_B_01_SerializeInMail() throws Exception {
    System.out.println("test_B_01_SerializeInMail_fillData");

    int iCount = getMessagesInQueue();
     MSHInMail init = TestLookupUtils.createInMail();

    

    mTestInstance.serializeInMail(init,  "testApplication");

    assertNotNull(init.getId());

    clearEntityManagerCahche();  // make sure to read from db
    MSHInMail fromDB = mTestInstance.
            getMailById(MSHInMail.class, init.getId());
    assertTrue(init != fromDB); // make sure to read from db

    assertNotNull("Id must not be null", fromDB.getId());
    
     List<MSHInEvent> leEvnt = mTestInstance.getMailEventList(MSHInEvent.class,
            init.getId());
    assertEquals(1, leEvnt.size());

    assertEquals(SEDInboxMailStatus.RECEIVED.getValue(), leEvnt.get(0).
            getStatus());
    
    assertEquals(fromDB.getReceivedDate(), leEvnt.get(0).getDate());

  }
   

  

  @Test
  public void testAddInMailPayload() throws Exception {
  }

  @Test
  public void testAddOutMailPayload() throws Exception {
  }

  @Test
  public void testAddExecutionTask() throws Exception {
  }

  @Test
  public void testGetDataList() throws Exception {
  }

  @Test
  public void testGetDataListCount() throws Exception {
  }

  @Test
  public void testGetInMailConvIdAndAction() throws Exception {
  }

  @Test
  public void testGetLastSuccesfullTaskExecution() throws Exception {
  }

  @Test
  public void testGetMailById() throws Exception {
  }

  @Test
  public void testGetMailByMessageId() throws Exception {
  }

  @Test
  public void testGetMailBySenderMessageId() throws Exception {
  }

  @Test
  public void testGetMailEventList() throws Exception {
  }

  @Test
  public void testGetMailPartList() throws Exception {
  }

  @Test
  public void testRemoveInMail() throws Exception {
  }

  @Test
  public void testRemoveMail() throws Exception {
  }

  @Test
  public void testRemoveOutMail() throws Exception {
  }

  @Test
  public void testSendOutMessage() throws Exception {
  }



  @Test
  public void testSetStatusToInMail_3args() throws Exception {
  }

  @Test
  public void testSetStatusToInMail_5args() throws Exception {
  }

  @Test
  public void testSetStatusToInMail_7args() throws Exception {
  }

  @Test
  public void testSetStatusToOutMail_3args() throws Exception {
  }

  @Test
  public void testSetStatusToOutMail_5args() throws Exception {
  }

  @Test
  public void testSetStatusToOutMail_7args() throws Exception {
  }

  @Test
  public void testUpdate() throws Exception {
  }

  @Test
  public void testUpdateExecutionTask() throws Exception {
  }

  @Test
  public void testUpdateInMail() throws Exception {
  }

  @Test
  public void testUpdateOutMail() throws Exception {
  }

  
  
  public int getMessagesInQueue() throws NamingException, JMSException {
    ConnectionFactory cf = (ConnectionFactory) InitialContext.doLookup(S_JMS_JNDI_CF);

    Connection connection = cf.createConnection();
    connection.start();

    Session session = connection.createSession(false,
            Session.AUTO_ACKNOWLEDGE);
    Queue queue = session
            .createQueue(S_JMS_QUEUE);

    QueueBrowser qb = session
            .createBrowser(queue);
    Enumeration en = qb.getEnumeration();
    int iSize = 0;
    while (en.hasMoreElements()) {
      en.nextElement();
      iSize++;
    }
    return iSize;

  }

  public void clearEntityManagerCahche() {
    mTestInstance.memEManager.clear();
  }

}
