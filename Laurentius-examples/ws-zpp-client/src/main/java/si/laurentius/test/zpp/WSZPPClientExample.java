/*
* Copyright 2016, Supreme Court Republic of Slovenia 
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved by 
* the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
*
* https://joinup.ec.europa.eu/software/page/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis, WITHOUT 
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and  
* limitations under the Licence.
 */
package si.laurentius.test.zpp;

import java.io.File;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import si.laurentius.GetInMailRequest;
import si.laurentius.GetInMailResponse;
import si.laurentius.InMailEventListRequest;
import si.laurentius.InMailEventListResponse;
import si.laurentius.InMailListRequest;
import si.laurentius.InMailListResponse;
import si.laurentius.Mailbox;
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
import si.laurentius.SEDException_Exception;
import si.laurentius.SEDMailBoxWS;

import si.laurentius.SubmitMailRequest;
import si.laurentius.SubmitMailResponse;
import si.laurentius.control.Control;
import si.laurentius.inbox.mail.InMail;
import si.laurentius.inbox.payload.InPart;
import si.laurentius.outbox.event.OutEvent;
import si.laurentius.outbox.mail.OutMail;
import si.laurentius.outbox.payload.OutPart;
import si.laurentius.outbox.payload.OutPayload;
import si.laurentius.outbox.property.OutProperties;
import si.laurentius.outbox.property.OutProperty;
import si.laurentius.test.zpp.fop.FOPException;

/**
 *
 * @author Jože Rihtaršič
 */
public class WSZPPClientExample {

  public static final String S_KEY_ALIAS = "test-zpp-sign";
  public static final String S_KEY_NOT_REGISTRED_ALIAS = "test-zpp-sign-not-registred";

  public static final String S_KEYSTORE = "/laurentius.jks";
  public static final String S_KEYSTORE_PASSWD = "passwd1234";
  public static final String S_KEY_PASSWD = "key1234";

  public static final String APPL_ID = "appl_1";
  public static final String APPL_PASSWORD = "appl1234";

  public static String MAILBOX_ADDRESS
          = "http://localhost:8080/laurentius-ws/mailbox?wsdl";

  public static final String DOMAIN = "mb-laurentius.si"; // CHANGE BOX DOMAIN!!!
  public static final String SENDER_BOX = "a.department@" + DOMAIN;
  public static final String RECEIVER_BOX = "b.department@" + DOMAIN;

  public static final Logger LOG = Logger.getLogger(WSZPPClientExample.class);
  
  public static final String LOG_SECTION_SEPARATOR = "*****************************";

  private File[] testFiles;
  SEDMailBoxWS mTestInstance = null;



  public SEDMailBoxWS getService() {
    if (mTestInstance == null) {
      //  System.setProperty("http.maxRedirects", "2"); // at least two redirection login and webservice url.(def: 20)

      try {
        Authenticator.setDefault(new Authenticator() {
          @Override
          protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(APPL_ID, APPL_PASSWORD.
                    toCharArray());
          }
        });
        Mailbox msb = new Mailbox(new URL(MAILBOX_ADDRESS));
        mTestInstance = msb.getSEDMailBoxWSPort();
      } catch (MalformedURLException ex) {
        LOG.error("Bad url", ex);
      } catch (Exception pe) {
        LOG.error("Bad password or application account: " + pe.getMessage());
      }
    }
    return mTestInstance;

  }

  public static void main(String... args) throws InterruptedException {

    
      /**
       * To start test first disable task: ZPPSign-B-Department from web-gui Test
       * procedure: 1. submit mail from a.department to b.department 2. retrieve
       * inmail for b.deparmtnt in PLOCKED status 3. create, sign and submit
       * AdviceOfDelivery 4. retrieve payloads for submitted mail
       *
       */
    try {
      // 1. test: AdviceOfDelivery is signed with key registred in laurentius
      testAdviceOfDelivery(S_KEY_ALIAS);
    } catch (SEDException_Exception | JAXBException | FOPException | ZPPException ex) {
       LOG.error("Signature failed: " + ex.getMessage(), ex);
    }
    
     try {
      // 2. test: AdviceOfDelivery is signed with a key NOT registred in laurentius
      testAdviceOfDelivery(S_KEY_NOT_REGISTRED_ALIAS);
    } catch (SEDException_Exception | JAXBException | FOPException | ZPPException ex) {
      LOG.error("Signature failed: " + ex.getMessage(), ex);
    }
  }

  private static void testAdviceOfDelivery(String singatureKey) throws SEDException_Exception, JAXBException, InterruptedException, FOPException, ZPPException {
    BasicConfigurator.configure();

    WSZPPClientExample wc = new WSZPPClientExample();

    // example submit mail
    BigInteger bi = wc.submitMail(createOutMail(RECEIVER_BOX, "Mr. Receiver",
            SENDER_BOX, "Mr. Sender",
            ZPPDeliveryConstants.S_ZPP_SERVICE,
            ZPPDeliveryConstants.S_ZPP_ACTION_DELIVERY_NOTIFICATION,
            "Test message",
            "VL 1/2016"));

    // example list all out mail
    List<OutMail> lstOm = wc.getOutMailList(SENDER_BOX);
    OutMail om = null;
    // get out mail data
    for (OutMail m : lstOm) {
      if (Objects.equals(m.getId(), bi)) {
        om = m;
        break;
      }
    }
   
    if (om== null){
      LOG.error("Message sent but no out message found for senderbox " + SENDER_BOX);
        return;
    }

    boolean msgSent = false;
    int iCnt = 0;
    while (!msgSent) {
      Thread.sleep(1000);
      // example list events for  out mail
      List<OutEvent> lstOE = wc.getOutMailEventList(bi, SENDER_BOX);
      for (OutEvent e : lstOE) {
        if (Objects.equals(e.getStatus(), "SENT")) {
          msgSent = true;
          break;
        }

      }
      if (++iCnt > 5) {
        LOG.error("Message is not sent in 5 seconds! - check url connection");
        return;
      }
    }

    //-----------------------------------------------------------------------
    // RECEIVED MAIL
    //-----------------------------------------------------------------------
    // example get in mail list
    // because "in process" in demo laurentius sets inmail status to DELIVERED - search is done by this status
    // else default is RECEIVED
    Thread.sleep(2000);
    List<InMail> lstIM = wc.getInMailList(RECEIVER_BOX, "PLOCKED");
    // search for corresponding in mail
    InMail im = null;
    for (InMail m : lstIM) {
      LOG.info(m.getSenderMessageId() + " - " + om.
              getSenderMessageId());
      if (Objects.equals(m.getSenderMessageId(), om.getSenderMessageId())) {
        im = m;
        break;
      }
    }

    ZPPUtils zpp = new ZPPUtils();
    OutMail omDA = zpp.createZppAdviceOfDelivery(im, S_KEYSTORE,
            S_KEYSTORE_PASSWD, singatureKey, S_KEY_PASSWD);

    wc.serialize(omDA);
    wc.submitMail(omDA);

    Thread.sleep(1000);

    InMail imWithDecPayload = wc.getInMail(im.getId(), RECEIVER_BOX);

    for (InPart ip : imWithDecPayload.getInPayload().getInParts()) {
      LOG.info(ip.getDescription());

    }

  }

  public long getDeltaTime(long l) {
    return Calendar.getInstance().getTimeInMillis() - l;
  }

  /**
   * Sumit mail example
   *
   * @return
   * @throws SEDException_Exception
   */
  public BigInteger submitMail(OutMail om)
          throws SEDException_Exception, JAXBException {

    // create message
    SubmitMailRequest smr = new SubmitMailRequest();
    smr.setControl(createControl());
    smr.setData(new SubmitMailRequest.Data());

    smr.getData().setOutMail(om);

    // submit request
    LOG.info("submit message");
    SubmitMailResponse mr = getService().submitMail(smr);
    LOG.info(LOG_SECTION_SEPARATOR);
    LOG.info("got 'sumitMail' response:\n" + serialize(mr));
    LOG.info(LOG_SECTION_SEPARATOR);

    return mr.getRData() != null ? mr.getRData().getMailId() : null;
  }

  public List<OutMail> getOutMailList(String senderBox)
          throws SEDException_Exception, JAXBException {
    OutMailListRequest omlr = new OutMailListRequest();
    Control c = createControl();
    c.setResponseSize(BigInteger.valueOf(100));
    omlr.setControl(c);
    omlr.setData(new OutMailListRequest.Data());
    omlr.getData().setSenderEBox(senderBox);

    OutMailListResponse mlr = getService().getOutMailList(omlr);
    LOG.info(LOG_SECTION_SEPARATOR);
    LOG.info("Got 'getOutMailList' response:\n" + serialize(mlr));
    LOG.info(LOG_SECTION_SEPARATOR);
    return mlr.getRData().getOutMails();

  }

  public List<OutEvent> getOutMailEventList(BigInteger bi, String senderBox)
          throws SEDException_Exception, JAXBException {
    OutMailEventListRequest omelr = new OutMailEventListRequest();
    Control c = createControl();
    c.setResponseSize(BigInteger.valueOf(100));
    omelr.setControl(c);
    omelr.setData(new OutMailEventListRequest.Data());
    omelr.getData().setSenderEBox(senderBox);
    omelr.getData().setMailId(bi);
    LOG.info(LOG_SECTION_SEPARATOR);
    LOG.info("'getOutMailEventList' request:\n" + serialize(omelr));
    LOG.info(LOG_SECTION_SEPARATOR);
    OutMailEventListResponse mler = getService().getOutMailEventList(omelr);
    LOG.info(LOG_SECTION_SEPARATOR);
    LOG.info("Got 'getOutMailEventList' response:\n" + serialize(mler));
    LOG.info(LOG_SECTION_SEPARATOR);
    return mler.getRData().getOutEvents();

  }

  public void modifyOutMail(BigInteger bi, String senderBox)
          throws JAXBException {
    ModifyOutMailRequest req = new ModifyOutMailRequest();
    Control c = createControl();
    c.setResponseSize(BigInteger.valueOf(100));
    req.setControl(c);
    req.setData(new ModifyOutMailRequest.Data());
    req.getData().setSenderEBox(senderBox);
    req.getData().setMailId(bi);
    req.getData().setAction(ModifOutActionCode.DELETE);

    ModifyOutMailResponse res;
    try {
      LOG.info(LOG_SECTION_SEPARATOR);
      LOG.info("'getInMailList' request:\n" + serialize(req));
      LOG.info(LOG_SECTION_SEPARATOR);
      res = getService().modifyOutMail(req);
      LOG.info(LOG_SECTION_SEPARATOR);
      LOG.info("Got 'modifyOutMail' response:\n" + serialize(res));
      LOG.info(LOG_SECTION_SEPARATOR);
    } catch (SEDException_Exception ex) {
      LOG.info(LOG_SECTION_SEPARATOR);
      LOG.info("Got 'modifyOutMail' SEDException_Exception:\n" + serialize(ex.
              getFaultInfo()));
      LOG.info(LOG_SECTION_SEPARATOR);
    }
  }

  public List<InMail> getInMailList(String receiverBox, String status)
          throws SEDException_Exception, JAXBException {
    InMailListRequest req = new InMailListRequest();
    Control c = createControl();
    c.setResponseSize(BigInteger.valueOf(100));
    req.setControl(c);
    req.setData(new InMailListRequest.Data());
    req.getData().setReceiverEBox(receiverBox);
    req.getData().setStatus(status);

    LOG.info(LOG_SECTION_SEPARATOR);
    LOG.info("'getInMailList' request:\n" + serialize(req));
    LOG.info(LOG_SECTION_SEPARATOR);

    InMailListResponse mlr = getService().getInMailList(req);
    LOG.info(LOG_SECTION_SEPARATOR);
    LOG.info("Got 'getInMailList' response:\n" + serialize(mlr));
    LOG.info(LOG_SECTION_SEPARATOR);
    // return first mail id
    return mlr.getRData().getInMails();

  }

  public void getInMailEventList(BigInteger bi, String receiverBox)
          throws SEDException_Exception, JAXBException {
    InMailEventListRequest omelr = new InMailEventListRequest();
    Control c = createControl();
    c.setResponseSize(BigInteger.valueOf(100));
    omelr.setControl(c);
    omelr.setData(new InMailEventListRequest.Data());
    omelr.getData().setReceiverEBox(receiverBox);
    omelr.getData().setMailId(bi);

    InMailEventListResponse mler = getService().getInMailEventList(omelr);
    LOG.info(LOG_SECTION_SEPARATOR);
    LOG.info("Got 'getInMailEventList' response:\n" + serialize(mler));
    LOG.info(LOG_SECTION_SEPARATOR);

  }

  public InMail getInMail(BigInteger bi, String receiverBox)
          throws SEDException_Exception, JAXBException {
    GetInMailRequest reg = new GetInMailRequest();
    Control c = createControl();
    c.setResponseSize(BigInteger.valueOf(100));
    reg.setControl(c);
    reg.setData(new GetInMailRequest.Data());
    reg.getData().setReceiverEBox(receiverBox);
    reg.getData().setMailId(bi);

    GetInMailResponse mler = getService().getInMail(reg);
    LOG.info(LOG_SECTION_SEPARATOR);
    LOG.info("Got 'getInMail' response:\n" + serialize(mler));
    LOG.info(LOG_SECTION_SEPARATOR);
    return mler.getRData().getInMail();

  }

  public void modifyInMail(BigInteger bi, String receiverBox)
          throws JAXBException {
    ModifyInMailRequest req = new ModifyInMailRequest();
    Control c = createControl();
    c.setResponseSize(BigInteger.valueOf(100));
    req.setControl(c);
    req.setData(new ModifyInMailRequest.Data());
    req.getData().setReceiverEBox(receiverBox);
    req.getData().setMailId(bi);
    req.getData().setAction(ModifyActionCode.DELETE);

    ModifyInMailResponse res;
    try {
      res = getService().modifyInMail(req);
      LOG.info(LOG_SECTION_SEPARATOR);
      LOG.info("Got response:\n" + serialize(res));
      LOG.info(LOG_SECTION_SEPARATOR);
    } catch (SEDException_Exception ex) {
      LOG.info("Got SEDException_Exception: " + serialize(ex.getFaultInfo()));
    }
  }

  public String serialize(Object o)
          throws JAXBException {

    StringWriter sw = new StringWriter();

    JAXBContext context = JAXBContext.newInstance(o.getClass());
    Marshaller m = context.createMarshaller();
    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    m.marshal(o, sw);

    return sw.toString();
  }

  private static OutMail createOutMail(String rcBox, String rcName,
          String sndBox,
          String sndName,
          String service, String action, String contentDesc,
          String oprst) {

    OutMail om = new OutMail();

    om.setSenderMessageId("SM_ID-" + UUID.randomUUID().toString());
    om.setConversationId(UUID.randomUUID().toString());
    om.setAction(action);
    om.setService(service);
    om.setReceiverName(rcName);
    om.setReceiverEBox(rcBox);
    om.setSenderName(sndName);
    om.setSenderEBox(sndBox);
    om.setSubject(oprst + " " + contentDesc);
    om.setOutProperties(new OutProperties());
    OutProperty opr = new OutProperty();
    opr.setName("oprst");
    opr.setValue(oprst);
    om.getOutProperties().getOutProperties().add(opr);

    om.setOutPayload(new OutPayload());

    OutPart op = new OutPart();
    op.setFilename("hello.txt");
    op.setDescription("This is test attachment");
    op.setBin("hello".getBytes());
    op.setMimeType("plain/text");
    om.getOutPayload().getOutParts().add(op);

    OutPart op1 = new OutPart();
    op1.setFilename("helloAgain.txt");
    op1.setDescription("This is second test attachment");
    op1.setBin("hello again".getBytes());
    op1.setMimeType("plain/text");
    om.getOutPayload().getOutParts().add(op1);

    return om;

  }

  private Control createControl() {

    Control c = new Control();
    c.setApplicationId(APPL_ID);
    c.setUserId("UserId"); /// user id is for log purposes
    return c;

  }
}
