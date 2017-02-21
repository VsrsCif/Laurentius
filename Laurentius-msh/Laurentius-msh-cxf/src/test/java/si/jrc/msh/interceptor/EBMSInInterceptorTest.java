/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.jrc.msh.interceptor;

import java.io.File;
import java.io.IOException;
import static java.lang.Thread.currentThread;
import java.util.Set;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.message.Message;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import si.jrc.msh.exception.EBMSErrorCode;
import si.jrc.msh.exception.EBMSErrorMessage;
import si.jrc.msh.test.ResourceFiles;
import static si.jrc.msh.lmbd.EbmsErrorAssertion.assertFault;
import si.jrc.msh.test.SEDTestCertBean;
import si.jrc.msh.test.SEDTestDao;
import si.jrc.msh.test.SEDTestLookup;
import static si.laurentius.commons.SEDSystemProperties.SYS_PROP_HOME_DIR;
import si.laurentius.commons.exception.PModeException;
import si.laurentius.commons.pmode.FilePModeManager;

/**
 *
 * @author Jože Rihtaršič
 */
public class EBMSInInterceptorTest {

  int miMethodStack = 3;

  public static final String INIT_PMODE_RESOURCE_PATH = "/pmode-conf.xml";
  public static final String INIT_LOOKUPS_RESOURCE_PATH = "/sed-lookups.xml";
  static EBMSInInterceptor mTestInstance = new EBMSInInterceptor();
  public static final Logger LOG = Logger.getLogger(EBMSInInterceptorTest.class);

  public EBMSInInterceptorTest() {
  }

  public static void setLogger(String fileName) {
    
    // set logger
    ConsoleAppender console = new ConsoleAppender(); // create appender
    // configure the appender
    String PATTERN = "%d [%p|%c|%C{1}] %m%n";
    console.setLayout(new PatternLayout(PATTERN));
    console.setThreshold(org.apache.log4j.Level.INFO);
    console.activateOptions();
    // add appender to any Logger (here is root)
    Logger.getRootLogger().addAppender(console);
    FileAppender fa = new FileAppender();
    fa.setName("FileLogger-" + fileName);
    fa.setFile("target" + File.separator + fileName + ".log");
    fa.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
    fa.setThreshold(org.apache.log4j.Level.DEBUG);
    fa.setAppend(true);
    fa.activateOptions();
    // add appender to any Logger (here is root)
    Logger.getRootLogger().addAppender(fa);
  }

  @BeforeClass
  public static void setUpClass() {
    // set home dir to target folder
    System.setProperty(SYS_PROP_HOME_DIR, "target");
    try {
      mTestInstance.mPMode = new FilePModeManager(EBMSInInterceptorTest.class.getResourceAsStream(
          INIT_PMODE_RESOURCE_PATH));

      mTestInstance.mSedLookups = new SEDTestLookup(EBMSInInterceptorTest.class.getResourceAsStream(
          INIT_LOOKUPS_RESOURCE_PATH));

        mTestInstance.mSedDao = new SEDTestDao();
        mTestInstance.mCertBean = new SEDTestCertBean();
            
      setLogger(EBMSInInterceptorTest.class.getSimpleName());
    } catch (PModeException | IOException | JAXBException ex) {
      LOG.error("ERROR startClass", ex);
    }
  }

  @Before
  public void setUp() {
    
  }

  /**
   * Test of getUnderstoodHeaders method, of class EBMSInInterceptor.
   */
  @Test
  public void testGetUnderstoodHeaders() {
    logStart(); 
    QName qbEBMS = new QName("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/",
        "Messaging");
    QName qbSecurity =
        new QName(
            "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
            "Security");
    EBMSInInterceptor instance = new EBMSInInterceptor();
    Set<QName> result = instance.getUnderstoodHeaders();
    assertTrue("Interface must understood ebms:Messaging", result.contains(qbEBMS));
    assertTrue("Interface must understood wsse:Security", result.contains(qbSecurity));
  }

  /**
   * Test exception for invalid message: Invalid soap version for AS4.
   */
  @Test
  public void testHandleMessage_SoapVersion() {
    logStart();

    // create soap message
    SoapMessage msg = ResourceFiles.getSoap11Message(ResourceFiles.S_REQUEST_SOAP11_HEADER);
    assertNotNull(msg);
    //test
    assertFault(() -> (mTestInstance).handleMessage(msg))
        .assertEBMSCode(EBMSErrorCode.ValueInconsistent)
        .assertSubMessageContainsString(EBMSErrorMessage.INVALID_SOAP_VERSION);

  }

  /**
   * Test exception for invalid message: Missing header.
   */
  @Test
  public void testHandleMessage_MissingHeader() {
    logStart();
    // create soap message
    SoapMessage msg = ResourceFiles.getSoap12Message(ResourceFiles.S_REQUEST_MISSING_HEADER);
    assertNotNull(msg);
    //test   
    assertFault(() -> (mTestInstance).handleMessage(msg))
        .assertEBMSCode(EBMSErrorCode.InvalidHeader)
        .assertSubMessageContainsString(EBMSErrorMessage.INVALID_HEADER_MISSING_MESSAGING);

  }

  /**
   * Test exception for invalid message: Empty messaging.
   */
  @Test
  public void testHandleMessage_EmptyMessaging() {
    logStart();
    // create soap message
    SoapMessage msg = ResourceFiles.getSoap12Message(ResourceFiles.S_REQUEST_MESSAGING_EMPTY);
    assertNotNull(msg);
    //test   
    assertFault(() -> (mTestInstance).handleMessage(msg))
        .assertEBMSCode(EBMSErrorCode.InvalidHeader)
        .assertSubMessageContainsString(EBMSErrorMessage.INVALID_HEADER_MESSAGING_EMPTY);

  }

  /**
   * Test exception for invalid message: Multiple messaging schema.
   */
  @Test
  public void testHandleMessage_MultipleMessaging() {
    logStart();
    // create soap message
    SoapMessage msg = ResourceFiles.getSoap12Message(ResourceFiles.S_REQUEST_MULTIPLE_MESSAGING);
    assertNotNull(msg);
    //test   
    assertFault(() -> (mTestInstance).handleMessage(msg))
        .assertEBMSCode(EBMSErrorCode.InvalidHeader)
        .assertSubMessageContainsString(EBMSErrorMessage.INVALID_HEADER_MULTIPLE_MESSAGING);
  }

  /**
   * Test exception for invalid message: Invalid schema message.
   */
  @Test
  public void testHandleMessage_InvalidHeaderBySchema() {
logStart();
    // create soap message
    SoapMessage msg = ResourceFiles.getSoap12Message(ResourceFiles.S_REQUEST_MESSAGING_XSD_INVALID);
    assertNotNull(msg);
    //test   
    assertFault(() -> (mTestInstance).handleMessage(msg))
        .assertEBMSCode(EBMSErrorCode.InvalidHeader)
        .assertSubMessageContainsString(EBMSErrorMessage.INVALID_HEADER_MESSAGING);

  }

  /**
   * Test exception for invalid message: Invalid schema message.
   */
  @Test
  public void testHandleMessage_InvalidHeaderBySchemaTo() {
logStart();
    // create soap message
    SoapMessage msg = ResourceFiles.getSoap12Message(
        ResourceFiles.S_REQUEST_MESSAGING_XSD_INVALID_TO);
    assertNotNull(msg);
    //test   
    assertFault(() -> (mTestInstance).handleMessage(msg))
        .assertEBMSCode(EBMSErrorCode.InvalidHeader)
        .assertSubMessageContainsString(EBMSErrorMessage.INVALID_HEADER_MESSAGING);

  }

  /**
   * Test exception for invalid message: Multiple UserMessage.
   */
  @Test
  public void testHandleMessage_InvalidHeaderMultipleUM() {
    logStart();
    // create soap message
    SoapMessage msg = ResourceFiles.getSoap12Message(ResourceFiles.S_REQUEST_MESSAGING_2UM);
    assertNotNull(msg);
    //test   
    assertFault(() -> (mTestInstance).handleMessage(msg))
        .assertEBMSCode(EBMSErrorCode.InvalidHeader)
        .assertSubMessageContainsString(EBMSErrorMessage.INVALID_HEADER_USER_MESSAGE_COUNT);
  }
  
    @Test
  public void testHandleMessage_InvalidPayload_MissingMime() {
    logStart();
    // create soap message
    SoapMessage msg = ResourceFiles.getSoap12Message(ResourceFiles.S_REQUEST_INVALID_PAYLOAD_MISSING_MIME);
    assertNotNull(msg);
    //test   
    assertFault(() -> (mTestInstance).handleMessage(msg))
        .assertEBMSCode(EBMSErrorCode.DecompressionFailure)
        .assertSubMessageContainsString("Missing MimeType for compressed payload");
  }
  

  /**
   * Test exception agreement for null type and bad URI agreement ref
   */
  @Test
  public void testHandleMessage_AgrRefURIError() {
    logStart();

    // create soap message
    SoapMessage msg = ResourceFiles.getSoap12Message(ResourceFiles.S_REQUEST_INVALID_AGR_REF_URI);
    assertNotNull(msg);
    //test for inbound request - Pmode  not in context  
    msg.put(Message.REQUESTOR_ROLE, Boolean.FALSE);

    assertFault(() -> (mTestInstance).handleMessage(msg))
        .assertEBMSCode(EBMSErrorCode.ValueInconsistent)
        .assertSubMessageContainsString(EBMSErrorMessage.INVALID_AGR_REF_URI);
  }

  /**
   * Test exception for null pmode exchange parameters for requestor
   */
  @Test
  public void testHandleMessage_AgrRefNotExists() {
    logStart();

    // create soap message
    SoapMessage msg = ResourceFiles.getSoap12Message(ResourceFiles.S_REQUEST_VALID_SIMPLE);
    assertNotNull(msg);
    //test for inbound request - Pmode  not in context  
    msg.put(Message.REQUESTOR_ROLE, Boolean.FALSE);

    assertFault(() -> (mTestInstance).handleMessage(msg))
        .assertEBMSCode(EBMSErrorCode.ValueNotRecognized)
        .assertSubMessageContainsString("not exist");

  }

  /**
   * Test exception for null pmode exchange parameters for requestor
   */
  @Test
  public void testHandleMessage_Invalid_Receiver() {
    logStart();

    // create soap message
    SoapMessage msg = ResourceFiles.getSoap12Message(ResourceFiles.S_REQUEST_INVALID_RECEIVER);
    assertNotNull(msg);

    //test for inbound request - Pmode  not in context  
    msg.put(Message.REQUESTOR_ROLE, Boolean.FALSE);

    assertFault(() -> (mTestInstance).handleMessage(msg))
        .assertEBMSCode(EBMSErrorCode.ValueNotRecognized)
        .assertSubMessageContainsString(EBMSErrorMessage.INVALID_HEADER_DATA);

  }

  /**
   * Test exception for null pmode exchange parameters for requestor
   */
  @Test
  public void testHandleMessage_InfoPartAttachmentNotExists() {
    logStart();

    // create soap message
    SoapMessage msg = ResourceFiles.getSoap12Message(ResourceFiles.S_REQUEST_VALID_PAYLOAD);
    assertNotNull(msg);

    //test for inbound request - Pmode  not in context  
    msg.put(Message.REQUESTOR_ROLE, Boolean.FALSE);

    assertFault(() -> (mTestInstance).handleMessage(msg))
        .assertEBMSCode(EBMSErrorCode.ValueInconsistent)
        .assertSubMessageContainsString(
            "Attachment: '72e3c588-dffd-4039-8809-b08f7a598ff6' not in soap request");
  }

  // @ Test
  public void testHandleMessage_encryption_invalid() {
    logStart();

    // create soap message
    SoapMessage msg = ResourceFiles.getSoap12MessageFromSMime(
        ResourceFiles.S_REQUEST_SMIME_ENC_INVALID);
    assertNotNull(msg);
    assertNotNull(msg.getAttachments());
    msg.put(Message.REQUESTOR_ROLE, Boolean.FALSE);

    assertFault(() -> (mTestInstance).handleMessage(msg))
        .assertEBMSCode(EBMSErrorCode.ProcessingModeMismatch)
        .assertSubMessageContainsString(
            "Error occured validating security");

  }

  @Test
  public void testHandleMessage_Signature_enc_valid() {
    logStart();
/*
     // create soap message
    SoapMessage msg = ResourceFiles.getSoap12MessageFromSMime(
        ResourceFiles.S_REQUEST_SMIME_SIGN_ENC_VALID);
    assertNotNull(msg);
    assertNotNull(msg.getAttachments());
    msg.put(Message.REQUESTOR_ROLE, Boolean.FALSE);

    try {
      mTestInstance.handleMessage(msg);
    } catch(Throwable th){
      String message = "Invalid signature: " + th.getMessage();
      LOG.error(message, th);
      fail(message);
    }*/
  }

  public void logStart() {
    String msg = getCurrentMethodName() + "  **************************************************";

    LOG.info(msg);

  }

  protected String getCurrentMethodName() {
    return currentThread().getStackTrace().length > miMethodStack ?
        currentThread().getStackTrace()[miMethodStack]
        .getMethodName() : "NULL METHOD";
  }
  
  @Test
  public void testMimeMessage(){

  }

}
