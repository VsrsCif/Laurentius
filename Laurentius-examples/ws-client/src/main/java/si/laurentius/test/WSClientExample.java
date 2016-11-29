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
package si.laurentius.test;

import java.io.File;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
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
import si.laurentius.outbox.mail.OutMail;
import si.laurentius.outbox.payload.OutPart;
import si.laurentius.outbox.payload.OutPayload;
import si.laurentius.outbox.property.OutProperties;
import si.laurentius.outbox.property.OutProperty;

/**
 *
 * @author Jože Rihtaršič
 */
public class WSClientExample {

  public static String MAILBOX_ADDRESS =
      "http://localhost:8080/laurentius-ws/mailbox?wsdl";

  public static final String DOMAIN = "test-laurentius.si";
  public static final String SENDER_BOX = "a.department@" + DOMAIN;
  public static final String RECEIVER_BOX = "b.department@" + DOMAIN;
  public static final String SERVICE = "DeliveryWithReceipt";
  public static final String ACTION = "Delivery";

  public static final Logger LOG = Logger.getLogger(WSClientExample.class);

  public File[] testFiles;
  SEDMailBoxWS mTestInstance = null;

  public WSClientExample() {

  }

  public SEDMailBoxWS getService() {
    if (mTestInstance == null) {
      try {
        Mailbox msb = new Mailbox(new URL(MAILBOX_ADDRESS));
        mTestInstance = msb.getSEDMailBoxWSPort();

      } catch (MalformedURLException ex) {
        LOG.error("Bad url", ex);

      }
    }
    return mTestInstance;

  }

  public static void main(String... args) {

    BasicConfigurator.configure();

    WSClientExample wc = new WSClientExample();
    try {
      // example submit mail
      BigInteger bi = wc.sumitMail();
      // example list all out mail
      wc.getOutMailList();
      // example list events for  out mail
      wc.getOutMailEventList(bi);
      // example modify status for out mail
      wc.modifyOutMail(bi);
      

      // example get in mail list
      BigInteger inMailId =  wc.getInMailList();
      // example get in mail
      wc.getInMail(inMailId);
      // example get in mail events
      wc.getInMailEventList(inMailId);
      // example modify status for out mail
      wc.modifyInMail(inMailId);

    } catch (JAXBException | SEDException_Exception ex) {
      LOG.error("Error occured while executing sample", ex);
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
  public BigInteger sumitMail()
      throws SEDException_Exception, JAXBException {

    // create message
    SubmitMailRequest smr = new SubmitMailRequest();
    smr.setControl(createControl());
    smr.setData(new SubmitMailRequest.Data());
    OutMail om = createOutMail(RECEIVER_BOX, "Mr. Receiver",
        SENDER_BOX, "Mr. Sender",
        SERVICE,
        ACTION,
        "Test message",
        "VL 1/2016");
    smr.getData().setOutMail(om);

    // submit request
    LOG.info("submit message");
    SubmitMailResponse mr = getService().submitMail(smr);
    LOG.info("*****************************");
    LOG.info("got 'sumitMail' response:\n" + serialize(mr));
    LOG.info("*****************************");

    return mr.getRData() != null ? mr.getRData().getMailId() : null;
  }

  public void getOutMailList()
      throws SEDException_Exception, JAXBException {
    OutMailListRequest omlr = new OutMailListRequest();
    Control c = createControl();
    c.setResponseSize(BigInteger.valueOf(100));
    omlr.setControl(c);
    omlr.setData(new OutMailListRequest.Data());
    omlr.getData().setSenderEBox(SENDER_BOX);

    OutMailListResponse mlr = getService().getOutMailList(omlr);
    LOG.info("*****************************");
    LOG.info("Got 'getOutMailList' response:\n" + serialize(mlr));
    LOG.info("*****************************");

  }

  public void getOutMailEventList(BigInteger bi)
      throws SEDException_Exception, JAXBException {
    OutMailEventListRequest omelr = new OutMailEventListRequest();
    Control c = createControl();
    c.setResponseSize(BigInteger.valueOf(100));
    omelr.setControl(c);
    omelr.setData(new OutMailEventListRequest.Data());
    omelr.getData().setSenderEBox(SENDER_BOX);
    omelr.getData().setMailId(bi);
    
    OutMailEventListResponse mler = getService().getOutMailEventList(omelr);
    LOG.info("*****************************");
    LOG.info("Got 'getOutMailEventList' response:\n" + serialize(mler));
    LOG.info("*****************************");

  }
  
  public void modifyOutMail(BigInteger bi)
      throws  JAXBException {
    ModifyOutMailRequest req = new ModifyOutMailRequest();
    Control c = createControl();
    c.setResponseSize(BigInteger.valueOf(100));
    req.setControl(c);
    req.setData(new ModifyOutMailRequest.Data());
    req.getData().setSenderEBox(SENDER_BOX);
    req.getData().setMailId(bi);
    req.getData().setAction(ModifOutActionCode.DELETE);
    
    ModifyOutMailResponse res;
    try {
      res = getService().modifyOutMail(req);
      LOG.info("*****************************");
       LOG.info("Got 'modifyOutMail' response:\n" + serialize(res));
       LOG.info("*****************************");
    } catch (SEDException_Exception ex) {
      LOG.info("*****************************");
      LOG.info("Got 'modifyOutMail' SEDException_Exception:\n" + serialize(ex.getFaultInfo()));
      LOG.info("*****************************");
    }
  }
  
  
   public BigInteger getInMailList()
      throws SEDException_Exception, JAXBException {
    InMailListRequest req = new InMailListRequest();
    Control c = createControl();
    c.setResponseSize(BigInteger.valueOf(100));
    req.setControl(c);
    req.setData(new InMailListRequest.Data());
    req.getData().setReceiverEBox(RECEIVER_BOX);

    InMailListResponse mlr = getService().getInMailList(req);
    LOG.info("*****************************");
    LOG.info("Got 'getInMailList' response:\n" + serialize(mlr));
    LOG.info("*****************************");
    // return first mail id
    return mlr.getRData().getInMails().get(0).getId();

  }

  public void getInMailEventList(BigInteger bi)
      throws SEDException_Exception, JAXBException {
    InMailEventListRequest omelr = new InMailEventListRequest();
    Control c = createControl();
    c.setResponseSize(BigInteger.valueOf(100));
    omelr.setControl(c);
    omelr.setData(new InMailEventListRequest.Data());
    omelr.getData().setReceiverEBox(RECEIVER_BOX);
    omelr.getData().setMailId(bi);
    
    InMailEventListResponse mler = getService().getInMailEventList(omelr);
    LOG.info("*****************************");
    LOG.info("Got 'getInMailEventList' response:\n" + serialize(mler));
    LOG.info("*****************************");

  }
  
  public void getInMail(BigInteger bi)
      throws SEDException_Exception, JAXBException {
    GetInMailRequest reg = new GetInMailRequest();
    Control c = createControl();
    c.setResponseSize(BigInteger.valueOf(100));
    reg.setControl(c);
    reg.setData(new GetInMailRequest.Data());
    reg.getData().setReceiverEBox(RECEIVER_BOX);
    reg.getData().setMailId(bi);
    
    GetInMailResponse mler = getService().getInMail(reg);
    LOG.info("*****************************");
    LOG.info("Got 'getInMail' response:\n" + serialize(mler));
    LOG.info("*****************************");

  }
  
  public void modifyInMail(BigInteger bi)
      throws  JAXBException {
    ModifyInMailRequest req = new ModifyInMailRequest();
    Control c = createControl();
    c.setResponseSize(BigInteger.valueOf(100));
    req.setControl(c);
    req.setData(new ModifyInMailRequest.Data());
    req.getData().setReceiverEBox(RECEIVER_BOX);
    req.getData().setMailId(bi);
    req.getData().setAction(ModifyActionCode.DELETE);
    
    ModifyInMailResponse res;
    try {
      res = getService().modifyInMail(req);
      LOG.info("*****************************");
     LOG.info("Got response:\n" + serialize(res));
     LOG.info("*****************************");
    } catch (SEDException_Exception ex) {
      LOG.info("Got SEDException_Exception: " + serialize(ex));
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

  private OutMail createOutMail(String rcBox, String rcName, String sndBox,
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
    c.setApplicationId("ApplicationId");
    c.setUserId("UserId");
    return c;

  }
}
