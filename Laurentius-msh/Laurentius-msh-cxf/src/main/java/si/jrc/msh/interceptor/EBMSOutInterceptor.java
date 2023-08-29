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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import org.apache.cxf.attachment.AttachmentImpl;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.SoapVersion;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.MessageUtils;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.laurentius.msh.pmode.PMode;
import si.laurentius.msh.pmode.PartyIdentitySet;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.SignalMessage;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.UserMessage;
import si.laurentius.commons.ebms.EBMSError;
import si.jrc.msh.exception.EBMSErrorCode;
import si.jrc.msh.utils.EBMSBuilder;
import si.jrc.msh.utils.MSHOutPartDataSource;
import si.jrc.msh.utils.MimeHeaderConstants;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.cxf.SoapUtils;
import si.laurentius.commons.enums.SEDMailPartSource;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.utils.GZIPUtil;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.pmode.EBMSMessageContext;
import si.laurentius.commons.utils.Utils;

/**
 * Sets up the outgoing chain to build a ebms 3.0 (AS4) form message. First it
 * will create Messaging object according pmode configuratin added as
 * "PMode.class" param in message context. For user message attachments are
 * added (and compressed according to pmode settings ) In the end encryption and
 * security interceptors are configured.
 *
 * @author Jože Rihtaršič
 */
public class EBMSOutInterceptor extends AbstractEBMSInterceptor {

    /**
     * Logger for EBMSOutInterceptor class
     */
    protected final static SEDLogger LOG = new SEDLogger(EBMSOutInterceptor.class);

    /**
     * GZIP utils
     */
    protected final GZIPUtil mGZIPUtils = new GZIPUtil();

    /**
     * Contstructor EBMSOutInterceptor for setting instance in a phase
     * Phase.PRE_PROTOCOL
     */
    public EBMSOutInterceptor() {
        super(Phase.PRE_PROTOCOL);
    }

    /**
     * Method transforms message to ebMS 3.0 (AS4) message form and sets
     * signature and encryption interceptors.
     *
     * @param msg: SoapMessage handled in CXF bus
     */
    @Override
    public void handleMessage(SoapMessage msg) {
        long l = LOG.logStart(msg);
        SoapVersion version = msg.getVersion();
        // is out mail request or response
        boolean isRequest = MessageUtils.isRequestor(msg);
        QName qnFault = (isRequest ? SoapFault.FAULT_CODE_CLIENT : SoapFault.FAULT_CODE_SERVER);

        if (msg.getContent(SOAPMessage.class) == null) {
            String errmsg = "Internal error missing SOAPMessage!";
            LOG.logError(l, errmsg, null);
            throw new EBMSError(EBMSErrorCode.ApplicationError, null, errmsg, qnFault);
        }

        // get context variables
        EBMSMessageContext ectx = SoapUtils.getEBMSMessageOutContext(msg);
        if (ectx == null) {
            String errmsg = "Internal error missing EBMSMessageContext!";
            LOG.logError(l, errmsg, null);
            throw new EBMSError(EBMSErrorCode.ApplicationError, null, errmsg, qnFault);
        }
        //  persist Soap part to out/in mail. 
        // This must be done  after signature
        msg.getInterceptorChain().add(new EBMSOutPartPersisterInterceptor());

        MSHOutMail outMail = SoapUtils.getMSHOutMail(msg);

        String msgId = outMail != null ? outMail.getMessageId() : null;

        LOG.formatedlog("Prepare to submit message: %s", msgId);
        // get pmode data
        PartyIdentitySet sPID = ectx.getSenderPartyIdentitySet();
        PartyIdentitySet rPID = ectx.getReceiverPartyIdentitySet();
        PMode pMode = ectx.getPMode();

        // create message 
        Messaging msgHeader = EBMSBuilder.createMessaging(version);
        // create usermessageunit for out mail 
        if (outMail != null) {
            // remove all ebms payloads. 
            if (outMail.getMSHOutPayload() != null && !outMail.getMSHOutPayload().getMSHOutParts().isEmpty()) {
                List<MSHOutPart> opList = outMail.getMSHOutPayload().getMSHOutParts().stream().filter( prt -> {
                    return !Objects.equals(SEDMailPartSource.EBMS.getValue(), prt.getSource());
                }).collect(Collectors.toList());
                
                outMail.getMSHOutPayload().getMSHOutParts().clear();
                outMail.getMSHOutPayload().getMSHOutParts().addAll(opList);

            }

            // add user message
            outMail.setSentDate(Calendar.getInstance().getTime()); // reset sent  to new value 

            UserMessage um;
            try {

                // add attachments
                try {

                    // set attachment for wss signature!
                    LOG.formatedlog("Set attachmetns for message: %s, gzip compress: %s, base64Encode %s",
                            msgId,
                            Utils.toStringBooleanValue(ectx.getTransportProtocol().isGzipCompress()),
                            Utils.toStringBooleanValue(ectx.getTransportProtocol().isBase64Encoded()));

                    setAttachments(msg, outMail, ectx.getTransportProtocol().isGzipCompress() != null && ectx.getTransportProtocol().isGzipCompress(),
                            ectx.getTransportProtocol().isBase64Encoded() != null && ectx.getTransportProtocol().isBase64Encoded());
                } catch (StorageException ex) {
                    String msgError = "Error adding attachments to soap" + ex.getMessage();
                    LOG.logError(l, msgError, ex);
                    throw new EBMSError(EBMSErrorCode.PModeConfigurationError, msgId,
                            msgError, ex,
                            SoapFault.FAULT_CODE_CLIENT);
                }
                // create user message
                LOG.log("Create userMessage unit for  message: " + msgId);
                um = EBMSBuilder.createUserMessage(ectx, outMail, outMail.getSentDate(), qnFault);
                msgHeader.getUserMessages().add(um);
            } catch (EBMSError ex) {

                LOG.logError(l, ex.getSubMessage(), ex);
                throw new EBMSError(EBMSErrorCode.PModeConfigurationError, msgId, ex.getMessage(), ex,
                        SoapFault.FAULT_CODE_CLIENT);
            }

        }
        SignalMessage signal = msg.getExchange().get(SignalMessage.class);
        if (signal != null) {
            LOG.log("Add signal message: " + msgId);
            msgHeader.getSignalMessages().add(signal);
        }
        // add error signal
        EBMSError err = msg.getExchange().get(EBMSError.class);

        if (err != null) {
            LOG.log("Add error message: " + msgId);
            SignalMessage sm
                    = EBMSBuilder.createErrorSignal(err, Calendar.getInstance()
                            .getTime());

            msgHeader.getSignalMessages().add(sm);
        }
        SOAPMessage request = msg.getContent(SOAPMessage.class);
        try {

            SOAPHeader sh = request.getSOAPHeader();
            Marshaller marshaller = JAXBContext.newInstance(Messaging.class).createMarshaller();
            marshaller.marshal(msgHeader, sh);
            request.saveChanges();
        } catch (JAXBException | SOAPException ex) {
            String errMsg = "Error adding ebms header to soap: " + ex.getMessage();
            LOG.logError(l, errMsg, ex);
            throw new EBMSError(EBMSErrorCode.ApplicationError, msgId, errMsg, ex,
                    SoapFault.FAULT_CODE_CLIENT);
        }
        // if out mail add security
        if (ectx.getSecurity() != null) {
            LOG.log("Set security: " + msgId);
            WSS4JOutInterceptor sc
                    = configureOutSecurityInterceptors(ectx.getSecurity(), sPID.getLocalPartySecurity(),
                            rPID.getExchangePartySecurity(), msgId,
                            SoapFault.FAULT_CODE_CLIENT);

            sc.handleMessage(msg);

        } else {
            LOG.logWarn("No Security policy for message: '" + msgId
                    + "' pmode: " + pMode.getId() + "!", null);
        }

        LOG.logEnd(l);
    }

    /**
     * Method sets attachments to outgoing ebmsUserMessage.
     *
     * @param msg - SAOP message
     * @param mail - MSH out mail
     * @throws StorageException
     */
    private void setAttachments(SoapMessage msg, MSHOutMail mail, boolean compress, boolean bBase64)
            throws StorageException {
        long l = LOG.logStart();
        if (mail != null && mail.getMSHOutPayload() != null
                && !mail.getMSHOutPayload().getMSHOutParts().isEmpty()) {

            msg.setAttachments(new ArrayList<>(mail.getMSHOutPayload().getMSHOutParts().size()));

            for (MSHOutPart p : mail.getMSHOutPayload().getMSHOutParts()) {

                if (Utils.isEmptyString(p.getEbmsId())) {
                    String id = UUID.randomUUID().toString() + "@" + SEDSystemProperties.getLocalDomain();
                    p.setEbmsId(id);    
                }

                AttachmentImpl att = new AttachmentImpl(p.getEbmsId());
                DataSource fds = new MSHOutPartDataSource(p, compress, bBase64);
                DataHandler dh = new DataHandler(fds);
                att.setDataHandler(dh);

                att.setHeader(MimeHeaderConstants.KEY_ID, p.getEbmsId());
                att.setHeader(MimeHeaderConstants.KEY_CONTENT_DISPOSITION,
                        String.format(MimeHeaderConstants.VAL_CONTENT_DISPOSITION_ATT_NAME, p.getFilename()));
                // apache CXF 3.1.4 bug overwrites this header  problems at SMTP conduit! 
                att.setHeader(MimeHeaderConstants.KEY_CONTENT_ENCODING, bBase64
                        ? MimeHeaderConstants.VAL_CONTENT_ENCODING_BASE64
                        : MimeHeaderConstants.VAL_CONTENT_ENCODING_BINARY);
                msg.getAttachments().add(att);
            }
        }
    }

    /**
     *
     * @param message
     */
    @Override
    public void handleFault(SoapMessage message) {
        super.handleFault(message); // To change body of generated methods, choose Tools | Templates.
        Exchange map = message.getExchange();
        map.entrySet().stream().forEach((entry) -> {
            LOG.formatedlog("Key: %s, val: %s", entry.getKey(), entry.getValue());
        });

    }

}
