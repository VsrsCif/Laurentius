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
package si.laurentius.test.zkp;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import si.laurentius.*;
import si.laurentius.control.Control;
import si.laurentius.inbox.mail.InMail;
import si.laurentius.inbox.payload.InPart;
import si.laurentius.outbox.event.OutEvent;
import si.laurentius.outbox.mail.OutMail;
import si.laurentius.outbox.payload.OutPart;
import si.laurentius.outbox.payload.OutPayload;
import si.laurentius.outbox.property.OutProperties;
import si.laurentius.outbox.property.OutProperty;
import si.laurentius.test.zkp.fop.FOPException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.ws.BindingProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.nio.file.Files;
import java.util.*;

/**
 * @author Jože Rihtaršič
 */
public class WSZKPClientExample {

    // TODO check if we can use test-zkp-sign. We'll need to add such aliased cert to jks keystore.
    public static final String S_KEY_ALIAS = "test-zpp-sign";
    // TODO same as for test-zpp-sign ^^
    public static final String S_KEY_NOT_REGISTRED_ALIAS = "test-zpp-sign-not-registred";

    public static final String S_KEYSTORE = "/laurentius.jks";
    public static final String S_KEYSTORE_PASSWD = "passwd1234";
    public static final String S_KEY_PASSWD = "key1234";

    public static final String APPL_ID = "appl_1";
    public static final String APPL_PASSWORD = "appl1234";

    public static final String MAILBOX_ADDRESS
            = "http://localhost:8080/laurentius-ws/mailbox?wsdl";

    public static final String DOMAIN = "test-laurentius.si"; // CHANGE BOX DOMAIN!!! (test-laurentius.si)
    public static final String SENDER_BOX = "a.department@" + DOMAIN;
    public static final String RECEIVER_BOX = "b.department@" + DOMAIN;

    public static final Logger LOG = Logger.getLogger(WSZKPClientExample.class);

    public static final String LOG_SECTION_SEPARATOR = "*****************************";

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

                // TODO: research why commented out approach was used
                // Mailbox msb = new Mailbox(new URL(MAILBOX_ADDRESS));
                Mailbox msb = new Mailbox(Mailbox.class.
                        getResource("/wsdl/mailbox.wsdl")); // wsdl is in laurentius-wsdl.jar
                mTestInstance = msb.getSEDMailBoxWSPort();
                Map<String, Object> req_ctx = ((BindingProvider) mTestInstance).
                        getRequestContext();
                req_ctx.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, MAILBOX_ADDRESS);
                // } catch (MalformedURLException ex) {
                // LOG.error("Bad url", ex);
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
        // ZKP_A
//        try {
//            // 1. test: AdviceOfDelivery is signed with key registered in laurentius
//            testAdviceOfDelivery(ZKPDeliveryConstants.S_ZKP_A_SERVICE, S_KEY_ALIAS, "Test message ZKP_A");
//        } catch (SEDException_Exception | JAXBException | FOPException | ZKPException ex) {
//            LOG.error("Signature failed: " + ex.getMessage(), ex);
//        }

//        try {
//            // 2. test: AdviceOfDelivery is signed with a key NOT registered in laurentius
//            testAdviceOfDelivery(ZKPDeliveryConstants.S_ZKP_A_SERVICE, S_KEY_NOT_REGISTRED_ALIAS, "Test message ZKP_A");
//        } catch (SEDException_Exception | JAXBException | FOPException | ZKPException ex) {
//            LOG.error("Signature failed: " + ex.getMessage(), ex);
//        }

        // ZKP_B
//        try {
//            // 1. test: AdviceOfDelivery is signed with key registered in laurentius
//            testAdviceOfDelivery(ZKPDeliveryConstants.S_ZKP_B_SERVICE, S_KEY_ALIAS, "Test message ZKP_B");
//        } catch (SEDException_Exception | JAXBException | FOPException | ZKPException ex) {
//            LOG.error("Signature failed: " + ex.getMessage(), ex);
//        }

//        try {
//            // 1. test: AdviceOfDelivery is signed with key registered in laurentius
//            testAdviceOfDelivery(ZKPDeliveryConstants.S_ZKP_B_SERVICE, S_KEY_NOT_REGISTRED_ALIAS, "Test message ZKP_B");
//        } catch (SEDException_Exception | JAXBException | FOPException | ZKPException ex) {
//            LOG.error("Signature failed: " + ex.getMessage(), ex);
//        }
//
//        // non-delivery for ZKP A and B
//
//        try {
//            testNonDeliveryZKP_A("Test non-delivery Message ZKP-A");
//        } catch (SEDException_Exception | JAXBException e) {
//            LOG.error("Non delivery test failed with exception", e);
//        }

        try {
            testNonDeliveryZKP_B("Test non-delivery Message ZKP-B");
        } catch (SEDException_Exception | JAXBException | FOPException | ZKPException e) {
            LOG.error("Non delivery test failed with exception", e);
        }
    }

    private static void testAdviceOfDelivery(String zkpService, String singatureKey, String testMessage)
            throws SEDException_Exception, JAXBException, InterruptedException, FOPException,
            ZKPException {
        BasicConfigurator.configure();

        WSZKPClientExample client = new WSZKPClientExample();
        BigInteger submitMailId = submitMail(zkpService, testMessage, client);
        OutMail outMail = getOutMail(client, submitMailId);

        if (outMail == null) {
            LOG.error("Message sent but no out message found for senderbox " + SENDER_BOX);
            return;
        }

        boolean isMessageSent = checkMessageSent(client, submitMailId);
        if (!isMessageSent) {
            // TODO return with non 0 exit status, or assert.fail ?
            return;
        }

        //-----------------------------------------------------------------------
        // RECEIVED MAIL
        //-----------------------------------------------------------------------
        // example get in mail list
        // because "in process" in demo laurentius sets inmail status to DELIVERED - search is done by this status
        // else default is RECEIVED
        Thread.sleep(2000);
        List<InMail> lstIM = client.getInMailList(RECEIVER_BOX, (zkpService.equals(ZKPDeliveryConstants.S_ZKP_A_SERVICE) ? "PLOCKED" : "RECEIVED"));
        // search for corresponding in mail
        InMail im = null;
        for (InMail m : lstIM) {
            LOG.info(m.getSenderMessageId() + " - " + outMail.
                    getSenderMessageId());
            if (Objects.equals(m.getSenderMessageId(), outMail.getSenderMessageId())) {
                im = m;
                break;
            }
        }

        if (im == null) {
            LOG.error("Message submitted but no mail in status RECEIVED found for " + RECEIVER_BOX);
            return;
        }

        ZKPUtils zkp = new ZKPUtils();
        OutMail omDA = zkp.createZkpAdviceOfDelivery(im, S_KEYSTORE, S_KEYSTORE_PASSWD, singatureKey, S_KEY_PASSWD, zkpService);

        client.serialize(omDA);
        client.submitMail(omDA);

        Thread.sleep(1000);

        InMail imWithDecPayload = client.getInMail(im.getId(), RECEIVER_BOX);

        for (InPart ip : imWithDecPayload.getInPayload().getInParts()) {
            LOG.info(ip.getDescription());
        }

    }

    private static void testNonDeliveryZKP_A(String testMessage) throws SEDException_Exception, JAXBException, InterruptedException {
        BasicConfigurator.configure();
        final String zkpService = ZKPDeliveryConstants.S_ZKP_A_SERVICE;

        final WSZKPClientExample client = new WSZKPClientExample();
        final BigInteger submitMailId = submitMail(zkpService, testMessage, client);
        final OutMail om = getOutMail(client, submitMailId);

        if (om == null) {
            throw new AssertionError("No OUT message found for senderbox " + SENDER_BOX);
        }

        boolean isMessageSent = checkMessageSent(client, submitMailId);
        if (!isMessageSent) {
            throw new AssertionError("Message did not reach SENT status");
        }

        String origOutMailId = om.getMessageId();
        LOG.info("Original out mail ID = " + origOutMailId);

        int attempts = 0;
        OutMail nonDeliveryOutMail = null;
        while (nonDeliveryOutMail == null) {
            OutMail outMail = getOutMail(client, submitMailId);
            if (outMail != null) {
                if (outMail.getStatus().equals("NOTDELIVERED")) {
                    LOG.info("Out Mail entered status: " + outMail.getStatus());
                    nonDeliveryOutMail = outMail;
                }
            }
            Thread.sleep(10000);
            attempts++;
            if (attempts > 20) {
                break;
            }
        }
        if (nonDeliveryOutMail == null) {
            throw new AssertionError("Message did not reach NOTDELIVERED status after " + attempts * 10 + " seconds");
        }

        final String nonDeliveredMailId = nonDeliveryOutMail.getMessageId();

        Thread.sleep(2000);
        int inMailCheckN = 0;
        Optional<InMail> inMailNonDeliveryNotification = Optional.empty();
        while (inMailCheckN < 10) {
            LOG.info(LOG_SECTION_SEPARATOR);
            LOG.info("Checking SENDER_BOX for InMailList. Origin messageId: " + nonDeliveredMailId);

            List<InMail> inMailReceived = client.getInMailList(SENDER_BOX, "RECEIVED");

            inMailNonDeliveryNotification = inMailReceived.stream().filter(inMail -> nonDeliveredMailId.equals(inMail.getRefToMessageId())).findFirst();
            if (inMailNonDeliveryNotification.isPresent()) {
                InMail nonDeliveryNotification = inMailNonDeliveryNotification.get();

                // Assert action
                if (!nonDeliveryNotification.getAction().equals("NotDelivered")) {
                    throw new AssertionError("Reference message to NOTDELIVERED is not of action \"NotDelivered\"");
                }
                break;
            }

            inMailCheckN++;
            Thread.sleep(1000);
        }

        if (!inMailNonDeliveryNotification.isPresent()) {
            throw new AssertionError("No Non delivery notification in InMail of SENDER_BOX");
        }
    }

    private static void testNonDeliveryZKP_B(String testMessage) throws SEDException_Exception, JAXBException, InterruptedException, FOPException, ZKPException {
        BasicConfigurator.configure();
        final String zkpService = ZKPDeliveryConstants.S_ZKP_B_SERVICE;

        final WSZKPClientExample client = new WSZKPClientExample();
        final BigInteger submitMailId = submitMail(zkpService, testMessage, client);
        final OutMail om = getOutMail(client, submitMailId);

        if (om == null) {
            throw new AssertionError("No OUT message found for senderbox " + SENDER_BOX);
        }

        boolean isMessageSent = checkMessageSent(client, submitMailId);
        if (!isMessageSent) {
            throw new AssertionError("Message did not reach SENT status");
        }

        String origOutMailId = om.getMessageId();
        LOG.info("Original out mail ID = " + origOutMailId);

        // NOW the receiving party should notify the sender that there was no delivery

        // TODO make it work with multiple tests (search inmail by reference, select correct one)
        Thread.sleep(5000);
        List<InMail> inMailReceived = client.getInMailList(RECEIVER_BOX, "RECEIVED");
        Optional<InMail> firstInMailOptional = inMailReceived.stream().findFirst();
        while (!firstInMailOptional.isPresent()) {
            inMailReceived = client.getInMailList(RECEIVER_BOX, "RECEIVED");
            firstInMailOptional = inMailReceived.stream().findFirst();
            Thread.sleep(5000);
        }

        ZKPUtils zkp = new ZKPUtils();
        OutMail nonDeliveryNotificationOM = zkp.createZkpBNonDeliveryNotification(firstInMailOptional.get(), S_KEYSTORE, S_KEYSTORE_PASSWD, S_KEY_ALIAS, S_KEY_PASSWD);

        client.serialize(nonDeliveryNotificationOM);
        client.submitMail(nonDeliveryNotificationOM);

        Thread.sleep(1000);
    }


    private static OutMail getOutMail(WSZKPClientExample client, BigInteger submitMailId) throws SEDException_Exception, JAXBException {
        List<OutMail> outMailList = client.getOutMailList(SENDER_BOX);
        OutMail outMail = null;
        // get out mail data
        for (OutMail m : outMailList) {
            if (Objects.equals(m.getId(), submitMailId)) {
                outMail = m;
                break;
            }
        }
        return outMail;
    }

    private static BigInteger submitMail(String zkpService, String testMessage, WSZKPClientExample wc) throws SEDException_Exception, JAXBException {
        BigInteger bi = wc.submitMail(createOutMail(RECEIVER_BOX, "Mr. Receiver",
                SENDER_BOX, "Mr. Sender",
                zkpService,
                ZKPDeliveryConstants.S_ZKP_ACTION_DELIVERY_NOTIFICATION,
                testMessage,
                "VL 1/2016"));
        return bi;
    }

    private static boolean checkMessageSent(WSZKPClientExample client, BigInteger submitMailId) throws InterruptedException, SEDException_Exception, JAXBException {
        boolean msgSent = false;
        int iCnt = 0;
        while (!msgSent) {
            Thread.sleep(1000);
            // example list events for  out mail
            List<OutEvent> lstOE = client.getOutMailEventList(submitMailId, SENDER_BOX);
            for (OutEvent e : lstOE) {
                if (Objects.equals(e.getStatus(), "SENT")) {
                    msgSent = true;
                    break;
                }

            }
            if (++iCnt > 8) {
                LOG.error("Message is not sent in 8 seconds! - check url connection");
            }
        }
        return msgSent;
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
        LOG.info("got 'submitMail' response:\n" + serialize(mr));
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
        final List<OutProperty> outProperties = om.getOutProperties().getOutProperties();
        outProperties.add(opr);

        OutProperty sodisif = new OutProperty();
        sodisif.setName("SodiSif");
        sodisif.setValue("1234");
        outProperties.add(sodisif);

        om.setOutPayload(new OutPayload());

        OutPart op = new OutPart();
        op.setFilename("hello.txt");
        op.setDescription("This is test attachment");
        op.setBin("hello".getBytes());
        //    op.setBin(generateBytes(15748));
        //    op.setBin(generateBytes(5000));
        op.setMimeType("plain/text");
        om.getOutPayload().getOutParts().add(op);

        OutPart op1 = new OutPart();
        op1.setFilename("helloAgain.txt");
        op1.setDescription("This is second test attachment");
        op1.setBin("hello again".getBytes());
        //    op1.setBin(generateBytes(5000));
        op1.setMimeType("plain/text");
        om.getOutPayload().getOutParts().add(op1);

        return om;

    }

    private static byte[] generateBytes(int ips) {
        try {
            byte[] buff = "Brown fox jumps over smart dog.".getBytes();
            File f = File.createTempFile("largeFile", ".txt");
            try (FileOutputStream fos = new FileOutputStream(f)) {
                int iSize = ips * 1000;
                int iCnt = 0;
                while (iCnt < iSize) {
                    fos.write(buff);
                    iCnt += buff.length;
                }
            }
            long bytes = f.length();
            long kilobytes = (bytes / 1024);
            long megabytes = (kilobytes / 1024);
            LOG.info("Generated file of size (MB): " + megabytes);
            return Files.readAllBytes(f.toPath());
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    private Control createControl() {

        Control c = new Control();
        c.setApplicationId(APPL_ID);
        c.setUserId("UserId"); /// user id is for log purposes
        return c;

    }
}
