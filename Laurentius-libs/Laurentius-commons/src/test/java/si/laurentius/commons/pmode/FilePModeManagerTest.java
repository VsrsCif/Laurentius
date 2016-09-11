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
package si.laurentius.commons.pmode;

import static java.lang.System.setProperty;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import static org.apache.log4j.Level.DEBUG;
import static org.apache.log4j.Level.FATAL;
import static org.apache.log4j.Logger.getRootLogger;
import org.apache.log4j.PatternLayout;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.pmode.PMode;
import si.laurentius.msh.pmode.PartyIdentitySet;
import si.laurentius.commons.SEDSystemProperties;

import static si.laurentius.commons.SEDSystemProperties.SYS_PROP_HOME_DIR;
import static si.laurentius.commons.SEDSystemProperties.SYS_PROP_PMODE;
import si.laurentius.commons.exception.PModeException;
import si.laurentius.commons.utils.SEDLogger;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public class FilePModeManagerTest {

  public static final SEDLogger LOG = new SEDLogger(FilePModeManager.class);

  public static final String TEST_PMODE_FILE = "pmode-conf.xml";

  /**
   *
   */
  public FilePModeManagerTest() {

    setProperty(SYS_PROP_HOME_DIR, ".");
    setProperty(SYS_PROP_PMODE, TEST_PMODE_FILE);

    ConsoleAppender console = new ConsoleAppender(); // create appender
    // configure the appender
    String PATTERN = "%d [%p|%c|%C{1}] %m%n";
    console.setLayout(new PatternLayout(PATTERN));
    console.setThreshold(FATAL);
    console.activateOptions();
    // add appender to any Logger (here is root)
    getRootLogger().addAppender(console);

    FileAppender fa = new FileAppender();
    fa.setName("FileLogger");
    fa.setFile("test.log");
    fa.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
    fa.setThreshold(DEBUG);
    fa.setAppend(true);
    fa.activateOptions();
    // add appender to any Logger (here is root)
    getRootLogger().addAppender(fa);
  }



  @Before
  public void setUp()
      throws Exception {

    System.getProperties().put(SEDSystemProperties.S_PROP_LAU_DOMAIN, "test-sed.si");
    
    System.getProperties().put(SEDSystemProperties.SYS_PROP_HOME_DIR, "src/test/resources/pmode/");
    System.getProperties().put(SEDSystemProperties.SYS_PROP_PMODE, TEST_PMODE_FILE);
  }

  /**
   *
   */
  @After
  public void tearDown() {
  }

  @Test
  public void testReloadPModesFromStream() {
    FilePModeManager pmd = new FilePModeManager();
    try {
      // resource from jar
      pmd.reloadPModes(FilePModeManagerTest.class.getResourceAsStream("/pmode/" + TEST_PMODE_FILE));
    } catch (PModeException ex) {
      Assert.fail("fail to reload test pmode from resource file:" + TEST_PMODE_FILE);
    }
  }

  @Test
  public void testReloadPModesFromSystemParameters() {
    FilePModeManager pmd = new FilePModeManager();
    try {
      pmd.reloadPModes();
    } catch (PModeException ex) {
      Assert.fail("fail to reload test pmode from resource file:" + TEST_PMODE_FILE +
          ".Message: " + ex.getMessage());
    }
  }

  /**
   * Test of getPModeById method, of class PModeManager.
   */
  @Test
  public void testGetPModeById() {

    String pModeId = "ZPP-legal";
    FilePModeManager instance = new FilePModeManager();
    try {
      instance.reloadPModes();
    } catch (PModeException ex) {
      Assert.fail("fail to reload test pmode from resource file:" + TEST_PMODE_FILE +
          ".Message: " + ex.getMessage());
      return;
    }

    PMode result;
    try {
      result = instance.getPModeById(pModeId);
    } catch (PModeException ex) {
      Assert.fail("Message: " + ex.getMessage());
      return;
    }
    Assert.assertNotNull(result);
    Assert.assertEquals(pModeId, result.getId());

  }

  /**
   * Test of getPartyIdentitySetForSEDAddress method, of class PModeManager.
   */
  @Test
  public void testGetPartyIdentitySetForSEDAddress() {

    FilePModeManager instance = new FilePModeManager();
    try {
      instance.reloadPModes();
    } catch (PModeException ex) {
      Assert.fail("fail to reload test pmode from resource file:" + TEST_PMODE_FILE +
          ".Message: " + ex.getMessage());
      return;
    }
    // check find for identifier
    String address = "ceftestparty2gw@test-sed.si";
    String expectedID = "test-sed-cef";
    PartyIdentitySet result;
    try {
      result = instance.getPartyIdentitySetForSEDAddress(address);
      Assert.assertNotNull(result);
      Assert.assertEquals(expectedID, result.getId());
    } catch (PModeException ex) {
      Assert.fail(String.format("Fail test for address %s. Error: %s", address, ex.getMessage()));
    }

    // check find for domain with no idetifiers
    address = "svev.test@test-sed.si";
    expectedID = "test-sed-svev";
    try {
      result = instance.getPartyIdentitySetForSEDAddress(address);
      Assert.assertNotNull(result);
      Assert.assertEquals(expectedID, result.getId());

    } catch (PModeException ex) {
      Assert.fail(String.format("Fail test for address %s. Error: %s", address, ex.getMessage()));

    }

    // check for not existed
    address = "svev.test@domain-not-exists.si";
    try {
      result = instance.getPartyIdentitySetForSEDAddress(address);
      Assert.fail(String.format("Got %s exptected error for address %s.", result.getId(), address));
    } catch (PModeException ex) {
      Assert.assertNotNull(ex.getMessage());
    }
  }

  /**
   * Test of getPartyIdentitySetForPartyId method, of class PModeManager.
   */
  @Test
  public void testGetPartyIdentitySetForPartyId() {

    FilePModeManager instance = new FilePModeManager();
    try {
      instance.reloadPModes();
    } catch (PModeException ex) {
      Assert.fail("fail to reload test pmode from resource file:" + TEST_PMODE_FILE +
          ".Message: " + ex.getMessage());
      return;
    }
    // check find for SVEV  address
    String idValue = "ceftestparty2gw@test-sed.si";
    String idType = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:si-svev:address";
    String expectedID = "test-sed-svev";
    PartyIdentitySet result;
    try {
      result = instance.getPartyIdentitySetForPartyId(idType, idValue);
      Assert.assertNotNull(result);
      Assert.assertEquals(expectedID, result.getId());
    } catch (PModeException ex) {
      Assert.fail(String.format("Fail test for id %s, type %s. Error: %s", idValue, idType,
          ex.getMessage()));
    }

    // check find for idetifiers
    idValue = "ceftestparty2gw";
    idType = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";
    expectedID = "test-sed-cef";
    try {
      result = instance.getPartyIdentitySetForPartyId(idType, idValue);
      Assert.assertNotNull(result);
      Assert.assertEquals(expectedID, result.getId());
    } catch (PModeException ex) {
      Assert.fail(String.format("Fail test for id %s, type %s. Error: %s", idValue, idType,
          ex.getMessage()));
    }

    // not exists for idetifiers
    idValue = "ceftestparty2gw-notExist";
    idType = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";
    expectedID = null;
    try {
      result = instance.getPartyIdentitySetForPartyId(idType, idValue);
      Assert.assertNull(result);
    } catch (PModeException ex) {
      Assert.fail(String.format("Fail test for id %s, type %s. Error: %s", idValue, idType,
          ex.getMessage()));
    }

    // not exists for idetifiers
    idValue = "ceftestparty2gw";
    idType = "urn:oasis:names:tc:ebcore:partyid-type:unregistered-notExist";
    expectedID = null;
    try {
      result = instance.getPartyIdentitySetForPartyId(idType, idValue);
      Assert.assertNull(result);
    } catch (PModeException ex) {
      Assert.fail(String.format("Fail test for id %s, type %s. Error: %s", idValue, idType,
          ex.getMessage()));
    }

  }

  /**
   * Test of getPModeForLocalPartyAsSender method, of class PModeManager.
   */
  @Test
  public void testGetPModeForLocalPartyAsSender() {

    FilePModeManager instance = new FilePModeManager();
    try {
      instance.reloadPModes();
    } catch (PModeException ex) {
      Assert.fail("fail to reload test pmode from resource file:" + TEST_PMODE_FILE +
          ".Message: " + ex.getMessage());
      return;
    }
    // valid request
    String localSenderPISId = "test-sed-svev";
    String localSenderRole = "Sender";
    String exchangeSenderPISId = "court-sed";
    String serviceId = "LegalDelivery_ZPP";

    String exptectedPModeId = "ZPP-legal";

    PMode result;
    try {
      result = instance.getPModeForLocalPartyAsSender(localSenderPISId, localSenderRole,
          exchangeSenderPISId, serviceId);
      Assert.assertNotNull(result);
      Assert.assertEquals(exptectedPModeId, result.getId());
    } catch (PModeException ex) {
      Assert.fail(String.format("Fail test valid request. Error: %s", ex.getMessage()));
    }

    // Invalid role
    localSenderPISId = "test-sed-svev";
    localSenderRole = "Sender-notExist";
    exchangeSenderPISId = "court-sed";
    serviceId = "LegalDelivery_ZPP";
    exptectedPModeId = null;

    try {
      result = instance.getPModeForLocalPartyAsSender(localSenderPISId, localSenderRole,
          exchangeSenderPISId, serviceId);
      Assert.fail(String.format("Got %s exptected error for invalid role %s.", result.getId(),
          localSenderRole));

    } catch (PModeException ex) {
      Assert.assertNotNull(ex.getMessage());
    }

    // Invalid service
    localSenderPISId = "test-sed-svev";
    localSenderRole = "Sender";
    exchangeSenderPISId = "court-sed";
    serviceId = "LegalDelivery_ZPP-notExist";
    exptectedPModeId = null;

    try {
      result = instance.getPModeForLocalPartyAsSender(localSenderPISId, localSenderRole,
          exchangeSenderPISId, serviceId);
      Assert.fail(String.format("Got %s exptected error for invalid service %s.", result.getId(),
          localSenderRole));

    } catch (PModeException ex) {
      Assert.assertNotNull(ex.getMessage());
    }

    // Invalid exchangeReceiverPISId
    localSenderPISId = "test-sed-svev";
    localSenderRole = "Sender";
    exchangeSenderPISId = "court-sed-notExist";
    serviceId = "LegalDelivery_ZPP";
    exptectedPModeId = null;

    try {
      result = instance.getPModeForLocalPartyAsSender(localSenderPISId, localSenderRole,
          exchangeSenderPISId, serviceId);
      Assert.fail(String.format("Got %s exptected error for invalid exchangeSenderPISId %s.",
          result.getId(),
          localSenderRole));

    } catch (PModeException ex) {
      Assert.assertNotNull(ex.getMessage());
    }

    // Invalid localSenderPISId
    localSenderPISId = "test-sed-svev-notExist";
    localSenderRole = "Sender";
    exchangeSenderPISId = "court-sed";
    serviceId = "LegalDelivery_ZPP";
    exptectedPModeId = null;

    try {
      result = instance.getPModeForLocalPartyAsSender(localSenderPISId, localSenderRole,
          exchangeSenderPISId, serviceId);
      Assert.fail(String.format("Got %s exptected error for invalid localSenderPISId %s.",
          result.getId(),
          localSenderRole));

    } catch (PModeException ex) {
      Assert.assertNotNull(ex.getMessage());
    }

  }

  /**
   * Test of getPModeForExchangePartyAsSender method, of class PModeManager.
   */
  @Test
  public void testGetPModeForExchangePartyAsSender() {

    FilePModeManager instance = new FilePModeManager();
    try {
      instance.reloadPModes();
    } catch (PModeException ex) {
      Assert.fail("fail to reload test pmode from resource file:" + TEST_PMODE_FILE +
          ".Message: " + ex.getMessage());
      return;
    }
    // valid request
    String exchangeSenderPISId = "cef";

    String exchangeSenderRole =
        "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator";
    String localReceiverPISId = "test-sed-cef";
    String serviceId = "connectivity-service";

    String exptectedPModeId = "test-oneway-push";

    PMode result;
    try {
      result = instance.getPModeForExchangePartyAsSender(exchangeSenderPISId, exchangeSenderRole,
          localReceiverPISId, serviceId);
      Assert.assertNotNull(result);
      Assert.assertEquals(exptectedPModeId, result.getId());
    } catch (PModeException ex) {
      Assert.fail(String.format("Fail test valid request. Error: %s", ex.getMessage()));
    }

    // Invalid role
    exchangeSenderPISId = "test-sed-svev";
    exchangeSenderRole = "Receiver-notExist";
    localReceiverPISId = "court-sed";
    serviceId = "LegalDelivery_ZPP";
    exptectedPModeId = null;

    try {
      result = instance.getPModeForExchangePartyAsSender(exchangeSenderPISId, exchangeSenderRole,
          localReceiverPISId, serviceId);
      Assert.fail(String.format("Got %s exptected error for invalid role %s.", result.getId(),
          exchangeSenderRole));

    } catch (PModeException ex) {
      Assert.assertNotNull(ex.getMessage());
    }

    // Invalid service
    exchangeSenderPISId = "test-sed-svev";
    exchangeSenderRole = "Receiver";
    localReceiverPISId = "court-sed";
    serviceId = "LegalDelivery_ZPP-notExist";
    exptectedPModeId = null;
    try {
      result = instance.getPModeForExchangePartyAsSender(exchangeSenderPISId, exchangeSenderRole,
          localReceiverPISId, serviceId);
      Assert.fail(String.format("Got %s exptected error for invalid serviceId %s.", result.getId(),
          serviceId));
    } catch (PModeException ex) {
      Assert.assertNotNull(ex.getMessage());
    }

    // Invalid exchangeSenderPISId
    exchangeSenderPISId = "test-sed-svev-notExist";
    exchangeSenderRole = "Receiver";
    localReceiverPISId = "court-sed";
    serviceId = "LegalDelivery_ZPP";
    exptectedPModeId = null;
    try {
      result = instance.getPModeForExchangePartyAsSender(exchangeSenderPISId, exchangeSenderRole,
          localReceiverPISId, serviceId);
      Assert.fail(String.format("Got %s exptected error for invalid exchangeSenderPISId %s.",
          result.getId(),
          exchangeSenderPISId));
    } catch (PModeException ex) {
      Assert.assertNotNull(ex.getMessage());
    }

    // Invalid localReceiverPISId
    exchangeSenderPISId = "test-sed-svev";
    exchangeSenderRole = "Receiver";
    localReceiverPISId = "court-sed-notExist";
    serviceId = "LegalDelivery_ZPP";
    exptectedPModeId = null;
    try {
      result = instance.getPModeForExchangePartyAsSender(exchangeSenderPISId, exchangeSenderRole,
          localReceiverPISId, serviceId);
      Assert.fail(String.format("Got %s exptected error for invalid localReceiverPISId %s.",
          result.getId(),
          localReceiverPISId));
    } catch (PModeException ex) {
      Assert.assertNotNull(ex.getMessage());
    }
  }

  /**
   * Test of getPModeForExchangePartyAsSender method, of class PModeManager.
   */
  @Test
  public void testCreateMessageContextForOutMail() {
    FilePModeManager instance = new FilePModeManager();
    try {
      instance.reloadPModes();
    } catch (PModeException ex) {
      Assert.fail("fail to reload test pmode from resource file:" + TEST_PMODE_FILE +
          ".Message: " + ex.getMessage());
      return;
    }
    String sender = "svev.test@test-sed.si";
    String receiver = "svev.test@court-sed.si";
    String serviceId = "LegalDelivery_ZPP";
    String actString = "DeliveryNotification";
    
    String exPmodeId = "ZPP-legal";
    String exSec ="sign_sha256";
    String exSedRole ="Sender";
    String exRecRole ="Receiver";
    String exSenderId ="test-sed-svev";
    String exReceiverId ="court-sed";
    String exRecAv = "AS4ReceiptResponse";
    String exTransport = "court-http";
    
    MSHOutMail mSHOutMail = new MSHOutMail();
    mSHOutMail.setReceiverEBox(receiver);
    mSHOutMail.setSenderEBox(sender);
    mSHOutMail.setService(serviceId);
    mSHOutMail.setAction(actString);
    
    EBMSMessageContext emc;
     try {
       emc = instance.createMessageContextForOutMail(mSHOutMail);
       Assert.assertNotNull("EBMSMessageContext", emc);           
       Assert.assertNotNull("PMode", emc.getPMode());
       Assert.assertNotNull("Service",emc.getService());
       Assert.assertNotNull("Action",emc.getAction());
       Assert.assertNotNull("ReceiverPartyIdentitySet",emc.getReceiverPartyIdentitySet());
       Assert.assertNotNull("SenderPartyIdentitySet",emc.getSenderPartyIdentitySet());
       Assert.assertNotNull("ReceptionAwareness",emc.getReceptionAwareness());
       Assert.assertNotNull("MEPType",emc.getMEPType());
       Assert.assertNotNull("TransportChannelType",emc.getTransportChannelType());
       Assert.assertNotNull("TransportProtocol",emc.getTransportProtocol());
       
       Assert.assertEquals(exPmodeId, emc.getPMode().getId());
       Assert.assertEquals(serviceId, emc.getService().getId());
       Assert.assertEquals(exSec, emc.getSecurity().getId());
       Assert.assertEquals(exSenderId,emc.getSenderPartyIdentitySet().getId());
       Assert.assertEquals(exReceiverId,emc.getReceiverPartyIdentitySet().getId());
       Assert.assertEquals(exSedRole,emc.getSendingRole());
       Assert.assertEquals(exRecRole,emc.getReceivingRole());
       Assert.assertEquals(exRecAv,emc.getReceptionAwareness().getId());
       Assert.assertEquals(exTransport,emc.getTransportProtocol().getId());
       
    } catch (PModeException ex) {
       Assert.fail(ex.getMessage());
    }
    
    
  }

}
