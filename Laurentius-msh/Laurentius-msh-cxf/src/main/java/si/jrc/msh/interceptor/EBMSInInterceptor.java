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
package si.jrc.msh.interceptor;


import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.activation.DataHandler;
import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.SoapVersion;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.interceptor.OutgoingChainInterceptor;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.security.wss4j.CryptoCoverageChecker;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.inbox.payload.MSHInPart;
import si.laurentius.msh.pmode.PartyIdentitySet;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.SignalMessage;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.UserMessage;
import si.laurentius.ebox.SEDBox;
import si.jrc.msh.client.sec.SecurityUtils;
import si.laurentius.commons.ebms.EBMSError;
import si.jrc.msh.exception.EBMSErrorCode;
import si.jrc.msh.exception.EBMSErrorMessage;
import si.jrc.msh.utils.EBMSBuilder;
import si.jrc.msh.utils.EBMSValidation;
import si.laurentius.commons.cxf.EBMSConstants;
import si.jrc.msh.utils.EBMSParser;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.cxf.SoapUtils;
import si.laurentius.commons.enums.SEDMailPartSource;
import si.laurentius.commons.exception.HashException;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.pmode.EBMSMessageContext;
import si.laurentius.commons.utils.GZIPUtil;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.Utils;
import si.laurentius.lce.DigestUtils;
import si.laurentius.msh.inbox.payload.IMPartProperty;
import si.laurentius.msh.inbox.payload.MSHInPayload;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.pmode.ReceptionAwareness;
import si.laurentius.msh.pmode.Security;

/**
 *
 * @author Jože Rihtaršič
 */
public class EBMSInInterceptor extends AbstractEBMSInterceptor {

  static final Set<QName> HEADERS = new HashSet<>();
  static final SEDLogger LOG = new SEDLogger(EBMSInInterceptor.class);

  static {
    HEADERS.add(new QName(EBMSConstants.EBMS_NS,
            EBMSConstants.EBMS_ROOT_ELEMENT_NAME));
    HEADERS.addAll(new WSS4JInInterceptor().getUnderstoodHeaders());
  }

  final StorageUtils msuStorageUtils = new StorageUtils();
  final EBMSValidation mebmsValidation = new EBMSValidation();
  final GZIPUtil mGZIPUtils = new GZIPUtil();
  final EBMSParser mebmsParser = new EBMSParser();
  final CryptoCoverageChecker checker = new CryptoCoverageChecker();

  /**
   *
   */
  public EBMSInInterceptor() {
    super(Phase.PRE_PROTOCOL); // user preprotocol for generating receipt
    getAfter().add(WSS4JInInterceptor.class.getName());
  }

  /**
   *
   * @return
   */
  @Override
  public Set<QName> getUnderstoodHeaders() {
    return HEADERS;
  }

  /**
   *
   * @param msg
   */
  @Override
  public void handleMessage(SoapMessage msg) {
    long l = LOG.logStart();

    SoapVersion version = msg.getVersion();
    boolean isBackChannel = SoapUtils.isRequestMessage(msg);

    // check soap version
    if (version.getVersion() != 1.2) {
      LOG.logError(l, EBMSErrorMessage.INVALID_SOAP_VERSION, null);
      throw new EBMSError(EBMSErrorCode.ValueInconsistent, null,
              EBMSErrorMessage.INVALID_SOAP_VERSION, SoapFault.FAULT_CODE_CLIENT);
    }

    // get Soap content
    SOAPMessage request = msg.getContent(SOAPMessage.class);
    if (request == null) {
      LOG.logError(l, "Message is not a SOAP message! Check log file: '"
              + SoapUtils.getInLogFilePath(msg) + "'", null);
      throw new EBMSError(EBMSErrorCode.InvalidSoapRequest, null,
              "Not a soap message", SoapFault.FAULT_CODE_CLIENT);
    }

    // validate soap request and retrieve messaging
    Messaging msgHeader = mebmsValidation.vaildateHeader_Messaging(msg,
            SoapFault.FAULT_CODE_CLIENT);

    // if user message get context from user message
    EBMSMessageContext inmctx = null;
    UserMessage um = null;
    String messageId = null;
    if (!msgHeader.getUserMessages().isEmpty()) {
      // vaildateHeader_Messaging already checked if count is 1
      um = msgHeader.getUserMessages().get(0);
      mebmsValidation.vaildateUserMessage(msg, um, SoapFault.FAULT_CODE_CLIENT);

      inmctx = EBMSParser.createEBMSContextFromUserMessage(um,
              getPModeManager());
      messageId = um.getMessageInfo().getMessageId();
      
/*
      // check if message already exists
      List<MSHInMail> dupllst = getDAO().getMailByMessageId(MSHInMail.class,
              messageId);
      List<MSHInMail> dupInclusionllst = new ArrayList<>();

      LOG.formatedlog("Got %d in messages with message id: %s", dupllst.size(),
              messageId);

      if (!dupllst.isEmpty()
              && inmctx.getReceptionAwareness() != null
              && inmctx.getReceptionAwareness().getDuplicateDetection() != null
              && inmctx.getReceptionAwareness().getDuplicateDetection().
                      getWindowPeriode() != null) {

        ReceptionAwareness.DuplicateDetection dd
                = inmctx.getReceptionAwareness().getDuplicateDetection();
        Duration dTime = dd.getWindowPeriode();
        Date dt = Calendar.getInstance().getTime();
        dTime.negate().addTo(dt);

        StringWriter sw = new StringWriter();
        for (MSHInMail dmi : dupllst) {
          Date recDate = dmi.getReceivedDate();
          if (recDate.after(dt)) {
            sw.append(String.format(
                    "Message with id %s (receiver id: %d) already received in conversation %s date:  %s",
                    messageId, dmi.getId(), dmi.getConversationId(),
                    SimpleDateFormat.getDateTimeInstance().format(recDate)));
            dupInclusionllst.add(dmi);
          }

        }
        dupllst.clear();

        String warn = sw.toString();

        if (!dupInclusionllst.isEmpty()) {
          LOG.logWarn(warn, null);
          if (dd.getEliminate()) {
            SignalMessage as4Receipt
                    = EBMSBuilder.generateAS4ReceiptSignal(messageId,
                            SEDSystemProperties.getLocalDomain(), request.
                            getSOAPPart()
                            .getDocumentElement(), Calendar.getInstance().
                                    getTime());
            EBMSError warning = new EBMSError(EBMSErrorCode.DuplicateDeteced,
                    messageId,
                    warn + " Duplicate is eliminated.",
                    SoapFault.FAULT_CODE_CLIENT);

            EBMSError warningSVEV = new EBMSError(
                    EBMSErrorCode.IgnoredAlreadyReceivedMessage,
                    messageId,
                    warn + " Duplicate is eliminated.",
                    SoapFault.FAULT_CODE_CLIENT);

            as4Receipt.getErrors().add(EBMSBuilder.createError(warning));
            as4Receipt.getErrors().add(EBMSBuilder.createError(warningSVEV));

            msg.getExchange().put(SignalMessage.class, as4Receipt);
            Endpoint e = msg.getExchange().get(Endpoint.class);
            if (!msg.getExchange().isOneWay() && !isBackChannel) {
              Message responseMsg = new MessageImpl();
              responseMsg.setExchange(msg.getExchange());
              responseMsg = e.getBinding().createMessage(responseMsg);
              msg.getExchange().setOutMessage(responseMsg);

              MessageFactory mf;
              try {
                mf = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
                SOAPMessage soapMessage = mf.createMessage();

                responseMsg.setContent(SOAPMessage.class, soapMessage);

                InterceptorChain chainOut
                        = OutgoingChainInterceptor.getOutInterceptorChain(msg
                                .getExchange());

                LOG.logWarn("got out interceptor:" + chainOut, null);
                responseMsg.setInterceptorChain(chainOut);
                SoapUtils.setEBMSMessageOutContext(
                        createOutContextFromInContext(inmctx, messageId),
                        responseMsg);
                chainOut.doIntercept(responseMsg);

              } catch (SOAPException ex) {
                LOG.logError(l, ex);
              }
            }

            LOG.formatedWarning("Duplicate %s eliminated", messageId);
            msg.getInterceptorChain().abort();
            return;
          }
        }
      }*/

      if (mebmsValidation.isTestUserMessage(um)) {
        LOG.formatedWarning(
                "Received test user message %s. Abort processing message!",
                um.getMessageInfo().getMessageId());

        SignalMessage as4Receipt
                = EBMSBuilder.generateAS4ReceiptSignal(messageId,
                        SEDSystemProperties.getLocalDomain(), request.
                        getSOAPPart()
                        .getDocumentElement(), Calendar.getInstance().getTime());
        msg.getExchange().put(SignalMessage.class, as4Receipt);
        InterceptorChain chain = msg.getInterceptorChain();
        chain.abort();

        return;

      }

    }
    // validate signals
    for (SignalMessage sm : msgHeader.getSignalMessages()) {

      mebmsValidation.processSignalMessage(msg, sm, SoapFault.FAULT_CODE_CLIENT);

    }

    // if backchannel out EBMSMessageContext must be  registred
    if (isBackChannel) {
      EBMSMessageContext outmctx = SoapUtils.getEBMSMessageOutContext(msg);
      if (outmctx == null) {
        String msgERr = "Out message context is not setted!";
        LOG.logError(l, msgERr, null);
        throw new EBMSError(EBMSErrorCode.ApplicationError, messageId,
                msgERr, SoapFault.FAULT_CODE_CLIENT);
      }

      if (inmctx != null && Objects.equals(inmctx.getPMode().getId(), outmctx.
              getPMode().getId())) {
        String msgERr = String.format("In pmode id: '%s' out pmode id: '%s'!",
                inmctx.getPMode().getId(), outmctx.getPMode().getId());
        LOG.logError(l, msgERr, null);
        throw new EBMSError(EBMSErrorCode.ProcessingModeMismatch, messageId,
                msgERr, SoapFault.FAULT_CODE_CLIENT);

      }

      if (inmctx == null) {
        inmctx = new EBMSMessageContext();
        inmctx.setPMode(outmctx.getPMode());
        inmctx.setService(outmctx.getService());
        inmctx.setMEPType(outmctx.getMEPType());
        inmctx.setMEPLegType(outmctx.getMEPLegType());
        inmctx.setSendingRole(outmctx.getReceivingRole());
        inmctx.setReceivingRole(outmctx.getSendingRole());
        inmctx.setReceiverPartyIdentitySet(outmctx.getSenderPartyIdentitySet());
        inmctx.setSenderPartyIdentitySet(outmctx.getReceiverPartyIdentitySet());
        inmctx.setPushTransfrer(inmctx.isPushTransfrer());
        if (outmctx.getMEPLegType().getTransport() != null) { // transport binding is BackChannel
          inmctx.setTransportChannelType(
                  outmctx.getMEPLegType().getTransport().getBackChannel());
        } else {
          inmctx.setSecurity(outmctx.getSecurity());
        }
        if (inmctx.getTransportChannelType() != null) {
          if (!Utils.isEmptyString(inmctx.getTransportChannelType().
                  getSecurityIdRef())) {
            String secId = inmctx.getTransportChannelType().getSecurityIdRef();
            Security sec = getPModeManager().getSecurityById(secId);
            if (sec != null) {
              inmctx.setSecurity(sec);
            } else {
              String msgERr = String.format(
                      "Error occured while retrieving securitypatteren for '%s'!",
                      secId);
              LOG.logError(l, msgERr, null);
              throw new EBMSError(EBMSErrorCode.ProcessingModeMismatch,
                      messageId,
                      msgERr, SoapFault.FAULT_CODE_CLIENT);
            }
          } else {
            inmctx.setSecurity(outmctx.getSecurity());
          }
        }

      }
    }
    if (inmctx == null) {
      LOG.log("IS isBackChannel: " + isBackChannel);
      String msgERr = String.format(
              "Could not find PMode parameters for message %s", messageId);
      LOG.logError(l, msgERr, null);
      throw new EBMSError(EBMSErrorCode.ProcessingModeMismatch, messageId,
              msgERr, SoapFault.FAULT_CODE_CLIENT);
    }

    SoapUtils.setEBMSMessageInContext(inmctx, msg);

    if (!isBackChannel && SoapUtils.getEBMSMessageOutContext(msg) == null) {
      SoapUtils.setEBMSMessageOutContext(createOutContextFromInContext(inmctx,
              messageId), msg);
    }

    if (SoapUtils.isSoapFault(request) && !SoapUtils.hasSecurity(request)) {
      LOG.formatedWarning(
              "Message is soap fault with no Security. Message: '%s', pmode '%s'!'",
              messageId,
              inmctx.getPMode().getId());
    } else if (inmctx.getSecurity() != null) {
      handleMessageSecurity(msg, inmctx, messageId);
    } else {
      LOG.formatedWarning("No Security policy for message: '%s', pmode '%s'!'",
              messageId,
              inmctx.getPMode().getId());
    }

    if (um != null) {
      processUserMessageUnit(msg, um, inmctx, messageId,
              SoapFault.FAULT_CODE_CLIENT);
    }

    // validate signals
    for (SignalMessage sm : msgHeader.getSignalMessages()) {
      if (isBackChannel && sm.getReceipt() != null) {
        MSHOutMail mo = SoapUtils.getMSHOutMail(msg);
        if (mo != null && Objects.equals(sm.getMessageInfo().getRefToMessageId(),
                mo.getMessageId())) {
          SoapUtils.setMSHOutMailReciept(sm, msg);
          LOG.formatedWarning("Signal Reciept for message %s, ref %s, date %s",
                  sm.getMessageInfo().getMessageId(),
                  sm.getMessageInfo().getRefToMessageId(),
                  sm.getMessageInfo().getTimestamp().toString());
        }

      }
    }
    
    if (inmctx!= null && um != null) {
      testDuplicateMail(messageId, inmctx, msg, request);
    }

    LOG.logEnd(l);
  }
  
  
  
  public  void testDuplicateMail(String messageId, EBMSMessageContext inmctx, SoapMessage msg, SOAPMessage request){
      long l = LOG.logStart();
      boolean isBackChannel = SoapUtils.isRequestMessage(msg);
       
      List<MSHInMail> dupllst = getDAO().getMailByMessageId(MSHInMail.class,
              messageId);
      List<MSHInMail> dupInclusionllst = new ArrayList<>();

      LOG.formatedlog("Got %d in messages with message id: %s", dupllst.size(),
              messageId);

      if (!dupllst.isEmpty()
              && inmctx.getReceptionAwareness() != null
              && inmctx.getReceptionAwareness().getDuplicateDetection() != null
              && inmctx.getReceptionAwareness().getDuplicateDetection().
                      getWindowPeriode() != null) {

        ReceptionAwareness.DuplicateDetection dd
                = inmctx.getReceptionAwareness().getDuplicateDetection();
        Duration dTime = dd.getWindowPeriode();
        Date dt = Calendar.getInstance().getTime();
        dTime.negate().addTo(dt);

        StringWriter sw = new StringWriter();
        for (MSHInMail dmi : dupllst) {
          Date recDate = dmi.getReceivedDate();
          if (recDate.after(dt)) {
            sw.append(String.format(
                    "Message with id %s (receiver id: %d) already received in conversation %s date:  %s",
                    messageId, dmi.getId(), dmi.getConversationId(),
                    SimpleDateFormat.getDateTimeInstance().format(recDate)));
            dupInclusionllst.add(dmi);
          }

        }
        dupllst.clear();

        String warn = sw.toString();

        if (!dupInclusionllst.isEmpty()) {
          LOG.logWarn(warn, null);
          if (dd.getEliminate()) {
            SignalMessage as4Receipt
                    = EBMSBuilder.generateAS4ReceiptSignal(messageId,
                            SEDSystemProperties.getLocalDomain(), request.
                            getSOAPPart()
                            .getDocumentElement(), Calendar.getInstance().
                                    getTime());
            EBMSError warning = new EBMSError(EBMSErrorCode.DuplicateDeteced,
                    messageId,
                    warn + " Duplicate is eliminated.",
                    SoapFault.FAULT_CODE_CLIENT);

            EBMSError warningSVEV = new EBMSError(
                    EBMSErrorCode.IgnoredAlreadyReceivedMessage,
                    messageId,
                    warn + " Duplicate is eliminated.",
                    SoapFault.FAULT_CODE_CLIENT);

            as4Receipt.getErrors().add(EBMSBuilder.createError(warning));
            as4Receipt.getErrors().add(EBMSBuilder.createError(warningSVEV));

            msg.getExchange().put(SignalMessage.class, as4Receipt);
            Endpoint e = msg.getExchange().get(Endpoint.class);
            if (!msg.getExchange().isOneWay() && !isBackChannel) {
              Message responseMsg = new MessageImpl();
              responseMsg.setExchange(msg.getExchange());
              responseMsg = e.getBinding().createMessage(responseMsg);
              msg.getExchange().setOutMessage(responseMsg);

              MessageFactory mf;
              try {
                mf = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
                SOAPMessage soapMessage = mf.createMessage();

                responseMsg.setContent(SOAPMessage.class, soapMessage);

                InterceptorChain chainOut
                        = OutgoingChainInterceptor.getOutInterceptorChain(msg
                                .getExchange());

                LOG.logWarn("got out interceptor:" + chainOut, null);
                responseMsg.setInterceptorChain(chainOut);
                SoapUtils.setEBMSMessageOutContext(
                        createOutContextFromInContext(inmctx, messageId),
                        responseMsg);
                chainOut.doIntercept(responseMsg);

              } catch (SOAPException ex) {
                LOG.logError(l, ex);
              }
            }

            LOG.formatedWarning("Duplicate %s eliminated", messageId);
            msg.getInterceptorChain().abort();
            return;
          }
        }
      }
  
  
  }

  public EBMSMessageContext createOutContextFromInContext(
          EBMSMessageContext inmctx,
          String messageId) {
    long l = LOG.logStart(messageId);
    EBMSMessageContext outmctx = new EBMSMessageContext();
    outmctx.setPMode(inmctx.getPMode());
    outmctx.setService(inmctx.getService());
    outmctx.setMEPType(inmctx.getMEPType());
    outmctx.setMEPLegType(inmctx.getMEPLegType());
    outmctx.setSendingRole(inmctx.getReceivingRole());
    outmctx.setReceivingRole(inmctx.getSendingRole());
    outmctx.setReceiverPartyIdentitySet(inmctx.getSenderPartyIdentitySet());
    outmctx.setSenderPartyIdentitySet(inmctx.getReceiverPartyIdentitySet());
    outmctx.setPushTransfrer(inmctx.isPushTransfrer());
    outmctx.setSecurity(inmctx.getSecurity());
    if (inmctx.getMEPLegType().getTransport() != null
            && inmctx.getMEPLegType().getTransport().getBackChannel() != null) { // transport binding is BackChannel
      outmctx.setTransportChannelType(
              inmctx.getMEPLegType().getTransport().getBackChannel());
    } else {
      outmctx.setSecurity(outmctx.getSecurity());
    }

    if (outmctx.getTransportChannelType() != null) {
      if (!Utils.isEmptyString(outmctx.getTransportChannelType().
              getSecurityIdRef())) {
        String secId = outmctx.getTransportChannelType().getSecurityIdRef();
        Security sec = getPModeManager().getSecurityById(secId);

        if (sec != null) {
          outmctx.setSecurity(getPModeManager().getSecurityById(secId));
        } else {
          String msgERr = String.format(
                  "Error occured while retrieving securitypatteren for '%s'!",
                  secId);
          LOG.logError(l, msgERr, null);
          throw new EBMSError(EBMSErrorCode.ProcessingModeMismatch, messageId,
                  msgERr, SoapFault.FAULT_CODE_CLIENT);
        }
      } else {
        outmctx.setSecurity(inmctx.getSecurity());
      }
    }
    LOG.logEnd(l);
    return outmctx;
  }

  public MSHInMail processUserMessageUnit(SoapMessage msg, UserMessage um,
          EBMSMessageContext ectx,
          String msgId, QName sv) {
    long l = LOG.logStart();

    SOAPMessage request = msg.getContent(SOAPMessage.class);

    MSHInMail mMail = mebmsParser.parseUserMessage(um, ectx,
            SoapFault.FAULT_CODE_CLIENT);
    SoapUtils.setMSHInMail(mMail, msg);
    String receiverBox = mMail.getReceiverEBox();
    if (receiverBox == null || receiverBox.trim().isEmpty()) {
      String errmsg = "Missing receiver box!";
      LOG.logError(l, errmsg, null);
      throw new EBMSError(EBMSErrorCode.ValueInconsistent, mMail.getMessageId(),
              errmsg,
              SoapFault.FAULT_CODE_CLIENT);
    }

    SEDBox inSb = getSedBoxByName(receiverBox);
    if (inSb == null) {
      String errmsg = String.format("Receiver '%s' is not defined in this MSH!",
              receiverBox);
      LOG.logError(l, errmsg, null);
      throw new EBMSError(EBMSErrorCode.ReceiverNotExists, mMail.getMessageId(),
              errmsg,
              SoapFault.FAULT_CODE_CLIENT);

    }
    mMail.setReceiverEBox(inSb.getLocalBoxName() + "@" + SEDSystemProperties.
            getLocalDomain());
    if (inSb.getActiveToDate() != null && inSb.getActiveToDate().before(
            Calendar.getInstance().getTime())) {
      String errmsg
              = "Receiver box: '" + mMail.getReceiverEBox() + "' not exists or is not active.";
      LOG.logError(l, errmsg, null);
      throw new EBMSError(EBMSErrorCode.ReceiverNotExists, mMail.getMessageId(),
              errmsg,
              SoapFault.FAULT_CODE_CLIENT);
    }
    // set inbox to message context
    SoapUtils.setMSHInMailReceiverBox(inSb, msg);

    try {
      MimeValue soapPartMime = MimeValue.MIME_XML;
      File f = storeSoapPart(request.getSOAPPart(), soapPartMime);
      if (mMail.getMSHInPayload() == null) {
        mMail.setMSHInPayload(new MSHInPayload());
      }
      MSHInPart p = new MSHInPart();
      p.setIsSent(Boolean.FALSE);
      p.setIsReceived(Boolean.TRUE);
      p.setEbmsId(msgId);
      p.setMimeType(soapPartMime.getMimeType());
      p.setDescription("SOAP Part");
      p.setName("SOAPPart");
      p.setSource(SEDMailPartSource.EBMS.getValue());
      p.setFilename(f.getName());
      p.setFilepath(StorageUtils.getRelativePath(f));
      mMail.getMSHInPayload().getMSHInParts().add(p);

      // store 
    } catch (StorageException ex) {

      LOG.logError(l, ex.getMessage(), ex);
      throw new EBMSError(EBMSErrorCode.Other, mMail.getMessageId(),
              ex.getMessage(),
              SoapFault.FAULT_CODE_CLIENT);
    }

    // serialize attachments
    if (mMail.getMSHInPayload() != null && !mMail.getMSHInPayload().
            getMSHInParts().isEmpty()) {
      for (MSHInPart p : mMail.getMSHInPayload().getMSHInParts()) {
        // check if payload is compressed
        boolean isCmpr = false;
        for (IMPartProperty prp : p.getIMPartProperties()) {
          if (!Utils.isEmptyString(prp.getName())
                  && !Utils.isEmptyString(prp.getValue())
                  && prp.getName().equalsIgnoreCase(
                          EBMSConstants.EBMS_PAYLOAD_COMPRESSION_TYPE)
                  && prp.getValue().equalsIgnoreCase(MimeValue.MIME_GZIP.
                          getMimeType())) {
            // found property EBMS_PAYLOAD_COMPRESSION_TYPE 
            isCmpr = true;
            // remove property because is no longer needed
            p.getIMPartProperties().remove(prp);
            break;
          }
        }

        try {
          serializeAttachments(p, msg.getAttachments(),
                  isCmpr, msgId, sv);
        } catch (StorageException | HashException ex) {
          String errmsg = "Error reading attachments .";
          LOG.logError(l, errmsg, ex);
          throw new EBMSError(EBMSErrorCode.ExternalPayloadError, mMail.
                  getMessageId(), errmsg,
                  SoapFault.FAULT_CODE_CLIENT);
        }
      }

    }

    // serializa data DB
    // prepare mail to persist
    Date dt = Calendar.getInstance().getTime();
    // set current status
    mMail.setStatus(null);
    //mMail.setStatusDate(dt);
    mMail.setReceivedDate(dt);

    msg.getExchange().put(MSHInMail.class, mMail);

    LOG.log("Generate AS4Receipt");
    SignalMessage as4Receipt
            = EBMSBuilder.generateAS4ReceiptSignal(mMail.getMessageId(),
                    SEDSystemProperties.getLocalDomain(), request.getSOAPPart()
                    .getDocumentElement(), dt);
    msg.getExchange().put(SignalMessage.class, as4Receipt);

    return mMail;
  }

  private File storeSoapPart(SOAPPart sp, MimeValue mv) throws StorageException {

    File f = StorageUtils.getNewStorageFile(mv.getSuffix(), "embs-header");

    try {
      TransformerFactory.newInstance().newTransformer().transform(
              new DOMSource(sp.getEnvelope().getHeader()),
              new StreamResult(f));
    } catch (TransformerException | SOAPException e) {
      throw new StorageException("Error occured while storing ebms header", e);
    }

    return f;

  }

  private SEDBox getSedBoxByName(String sbox) {
    String localName = sbox.contains("@") ? sbox.substring(0, sbox.indexOf("@")) : sbox;
    return getLookups().getSEDBoxByLocalName(localName);

  }

  private void handleMessageSecurity(SoapMessage msg, EBMSMessageContext ectx,
          String messageId) {
    
    PartyIdentitySet rPID = ectx.getReceiverPartyIdentitySet();
    PartyIdentitySet sPID = ectx.getSenderPartyIdentitySet();
    long l = LOG.logStart();
    try {
      WSS4JInInterceptor sc
              = configureInSecurityInterceptors(ectx.getSecurity(), rPID.
                      getLocalPartySecurity(),
                      sPID.getExchangePartySecurity(), messageId,
                      SoapFault.FAULT_CODE_CLIENT);
      sc.handleMessage(msg);

    } catch (Throwable tg) {
      LOG.logError(l, "Error validating security: '"
              + SoapUtils.getInLogFilePath(msg) + "'", tg);
      throw new EBMSError(EBMSErrorCode.ProcessingModeMismatch,
              messageId,
              "Error occured validating security: " + tg.getMessage(), tg,
              SoapFault.FAULT_CODE_CLIENT);
    }

    try {
      CryptoCoverageChecker cc = SecurityUtils.
              configureCryptoCoverageCheckerInterceptors(
                      ectx.getSecurity());
      cc.handleMessage(msg);

    } catch (Throwable tg) {
      LOG.logError(l, "Error validating security: '"
              + SoapUtils.getInLogFilePath(msg) + "'", tg);
      throw new EBMSError(EBMSErrorCode.ProcessingModeMismatch,
              messageId,
              "Security coverage mishatch! Error: " + tg.getMessage(), tg,
              SoapFault.FAULT_CODE_CLIENT);
    }
    LOG.logEnd(l);
  }

  /**
   *
   * @param message
   */
  @Override
  public void handleFault(SoapMessage message) {
    super.handleFault(message);

  }

  private void serializeAttachments(MSHInPart p, Collection<Attachment> lstAttch,
          boolean compressed,
          String msgId, QName sv)
          throws StorageException, HashException {
    DataHandler dh = null;
    for (Attachment a : lstAttch) {
      if (a.getId().equals(p.getEbmsId())) {
        dh = a.getDataHandler();
        break;
      }
    }

    File fout = null;
    if (dh != null) {
      if (compressed) {
        fout = msuStorageUtils.getCreateEmptyInFile(p.getMimeType());
        try {
          mGZIPUtils.decompressGZIP(dh.getInputStream(), fout);
        } catch (IOException ex) {
          String msg = String.format(
                  "Error decompressing attachment: %s. Error: %s", p.getEbmsId(),
                  ex.getMessage());
          LOG.logError(msg, ex);
          throw new EBMSError(EBMSErrorCode.DecompressionFailure, msgId,
                  msg, sv);

        }
      } else {
        try {
          // if not compressed

          fout = msuStorageUtils.storeInFile(
                  p.getIsEncrypted() ? MimeValue.MIME_ENC.getMimeType() : p.
                  getMimeType(), dh.getInputStream());
        } catch (IOException ex) {
          throw new StorageException(String.format(
                  "Error storing attachment %s for message: %s.",
                  p.getEbmsId(), msgId), ex);
        }
      }
    }
    // set MD5 and relative path;
    if (fout != null) {
      String relPath = StorageUtils.getRelativePath(fout);
      p.setFilepath(relPath);
      p.setSha256Value(DigestUtils.getHexSha256Digest(fout));
      p.setSize(BigInteger.valueOf(fout.length()));

      if (Utils.isEmptyString(p.getFilename())) {
        p.setFilename(fout.getName());
      }
      if (Utils.isEmptyString(p.getName())) {
        p.setName(p.getFilename().substring(p.getFilename().lastIndexOf(".")));
      }
    }

  }

}
