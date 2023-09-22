/*
 * Copyright 2016, Supreme Court Republic of Slovenia
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
package si.jrc.msh.utils;

import si.laurentius.commons.cxf.EBMSConstants;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.cxf.binding.soap.SoapVersion;
import org.apache.cxf.interceptor.Fault;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.laurentius.msh.outbox.property.MSHOutProperty;
import si.laurentius.msh.pmode.PartyIdentitySet;
import si.laurentius.msh.pmode.PartyIdentitySetType;

import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.AgreementRef;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.CollaborationInfo;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Description;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.From;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.MessageInfo;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.MessageProperties;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.PartInfo;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.PartProperties;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.PartyId;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.PartyInfo;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.PayloadInfo;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Property;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Receipt;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.SignalMessage;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.To;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.UserMessage;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Error;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import si.jrc.msh.exception.EBMSErrorCode;
import si.laurentius.commons.ebms.EBMSError;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.PModeConstants;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.pmode.EBMSMessageContext;

import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.commons.utils.xml.XMLUtils;
import si.laurentius.msh.outbox.payload.OMPartProperty;

/**
 *
 * @author Jože Rihtaršič
 */
public class EBMSBuilder {

    private static final String ID_PREFIX = "SED-";

    /**
     *
     */
    protected final static SEDLogger LOG = new SEDLogger(EBMSBuilder.class);

    /**
     *
     * @param ebError
     * @return
     */
    public static Error createError(EBMSError ebError) {

        Error er = new Error();
        er.setCategory(ebError.getEbmsErrorCode().getCategory());
        er.setDescription(new Description());
        er.getDescription().setLang("en");
        er.getDescription().setValue(ebError.getEbmsErrorCode().getDescription());
        er.setOrigin(ebError.getEbmsErrorCode().getOrigin());
        er.setErrorCode(ebError.getEbmsErrorCode().getCode());
        er.setErrorDetail(ebError.getSubMessage());
        er.setRefToMessageInError(ebError.getRefToMessage());
        er.setSeverity(ebError.getEbmsErrorCode().getSeverity());
        er.setShortDescription(ebError.getEbmsErrorCode().getName());
        return er;
    }

    public static SignalMessage createErrorSignal(EBMSError ebError,
            Date timestamp) {
        SignalMessage sigMsg = new SignalMessage();
        // generate MessageInfo
        sigMsg.setMessageInfo(
                createMessageInfo(SEDSystemProperties.getLocalDomain(), ebError.
                        getRefToMessage(), timestamp));
        sigMsg.getErrors().add(createError(ebError));
        return sigMsg;
    }

    public static SignalMessage createErrorSignal(Fault err, String refMsgId,
            String desc, String senderDomain,
            Date timestamp) {
        SignalMessage sigMsg = new SignalMessage();
        // generate MessageInfo
        sigMsg.setMessageInfo(createMessageInfo(senderDomain, refMsgId, timestamp));

        Error er = new Error();
        er.setDescription(new Description());
        er.getDescription().setLang("en");
        er.getDescription().setValue(EBMSErrorCode.Other.getDescription());
        er.setErrorDetail(desc);
        er.setCategory(EBMSErrorCode.Other.getCategory());
        er.setRefToMessageInError(refMsgId);
        er.setErrorCode(EBMSErrorCode.Other.getCode());
        er.setOrigin(EBMSErrorCode.Other.getOrigin());
        er.setSeverity(EBMSErrorCode.Other.getSeverity());
        er.setShortDescription(EBMSErrorCode.Other.getName());
        sigMsg.getErrors().add(er);

        return sigMsg;
    }

    private static MessageInfo createMessageInfo(String senderDomain,
            String refToMessage,
            Date timestamp) {
        return createMessageInfo(UUID.randomUUID().toString(), senderDomain,
                refToMessage, timestamp);
    }

    private static MessageInfo createMessageInfo(String msgId, String senderDomain,
            String refToMessage,
            Date timestamp) {
        if (Utils.isEmptyString(msgId)) {
            msgId = Utils.getUUIDWithDomain(senderDomain);
        }
        MessageInfo mi = new MessageInfo();
        mi.setMessageId(msgId);
        mi.setTimestamp(timestamp);
        mi.setRefToMessageId(refToMessage);
        return mi;
    }

    /**
     *
     * @param version
     * @return
     */
    public static Messaging createMessaging(SoapVersion version) {
        Messaging msg = new Messaging();
        // ID must be an NCName. This means that it must start with a letter or underscore,
        // and can only contain letters, digits, underscores, hyphens, and periods.
        msg.setId(ID_PREFIX + UUID.randomUUID().toString()); // generate unique id
        if (version.getVersion() != 1.1) {
            msg.setMustUnderstand(Boolean.TRUE);
        } else {
            msg.setS11MustUnderstand(Boolean.TRUE);
        }
        return msg;

    }

    /**
     * Method creates ebms 3.0 list of PartyIDs
     *
     * @param pis
     * @param address
     * @param name
     * @param use4cm
     * @return
     */
    public static List<PartyId> createPartyIdList(PartyIdentitySet pis,
            String address, String name, boolean use4cm) {
        List<PartyId> pilst = new ArrayList<>();
        if (use4cm) {
            String domain = pis.isIsLocalIdentity()?SEDSystemProperties.getLocalDomain(): pis.getDomain();
            PartyId pi = new PartyId();
            pi.setType(EBMSConstants.EBMS_ECORE_PARTY_TYPE_UNREGISTERED);
            pi.setValue(domain);
            pilst.add(pi);
        } else {
        
            for (PartyIdentitySetType.PartyId pisPi : pis.getPartyIds()) {
                PartyId pi = new PartyId();
                pi.setType(pisPi.getType());

                if (!Utils.isEmptyString(pisPi.getFixValue())) {
                    pi.setValue(pisPi.getFixValue());

                } else if (!pisPi.getValueSource().equals(
                        PModeConstants.PARTY_ID_SOURCE_TYPE_IGNORE)) {
                    switch (pisPi.getValueSource()) {
                        case PModeConstants.PARTY_ID_SOURCE_TYPE_ADDRESS:
                            pi.setValue(address);
                            break;
                        case PModeConstants.PARTY_ID_SOURCE_TYPE_NAME:
                            pi.setValue(Utils.isEmptyString(name) ? address : name);
                            break;
                        case PModeConstants.PARTY_ID_SOURCE_TYPE_IDENTIFIER:
                            String identifier = address.substring(0, address.indexOf('@'));
                            pi.setValue(identifier);
                            break;
                        default:
                            pi.setValue(address);

                    }
                }
                pilst.add(pi);
            }
        }
        return pilst;
    }

    public static UserMessage createUserMessage(
            EBMSMessageContext ctx,
            MSHOutMail mo,
            Date timestamp,
            QName sv)
            throws EBMSError {

        PartyIdentitySet pisSender = ctx.getSenderPartyIdentitySet();
        PartyIdentitySet pisReceiver = ctx.getReceiverPartyIdentitySet();

        UserMessage usgMsg = new UserMessage();

        // UserMessage usgMsg = new UserMessage();
        // --------------------------------------
        // generate MessageInfo
        MessageInfo mi
                = createMessageInfo(mo.getMessageId(),
                        SEDSystemProperties.getLocalDomain(),
                        mo.getRefToMessageId(),
                        timestamp);
        usgMsg.setMessageInfo(mi);

        // generate from
        usgMsg.setPartyInfo(new PartyInfo());
        usgMsg.getPartyInfo().setFrom(new From());
        // sender ids
        usgMsg.getPartyInfo().getFrom().setRole(ctx.getSendingRole()); // get from p-mode
        List<PartyId> plstSender = createPartyIdList(pisSender, mo.getSenderEBox(),
                mo.getSenderName(), pisSender.isUseFourCornerModel()!=null && pisSender.isUseFourCornerModel());
        usgMsg.getPartyInfo().getFrom().getPartyIds().addAll(plstSender);

        // generate to
        usgMsg.getPartyInfo().setTo(new To());
        usgMsg.getPartyInfo().getTo().setRole(ctx.getReceivingRole());
        List<PartyId> plstReceiver = createPartyIdList(pisReceiver, mo.
                getReceiverEBox(),
                mo.getReceiverName(), pisReceiver.isUseFourCornerModel() != null && pisReceiver.isUseFourCornerModel());
        usgMsg.getPartyInfo().getTo().getPartyIds().addAll(plstReceiver);

        // set colloboration info
        // BusinessInfo bi = pm.getLegs().get(0).getBusinessInfo();
        usgMsg.setCollaborationInfo(new CollaborationInfo());
        usgMsg.getCollaborationInfo().setService(
                new org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Service());
        usgMsg.getCollaborationInfo().getService().setValue(ctx.getService().
                getServiceName());
        if (!Utils.isEmptyString(ctx.getService().getServiceType())) {
            usgMsg.getCollaborationInfo().getService().setType(ctx.getService().
                    getServiceType());
        }
        usgMsg.getCollaborationInfo().setAction(mo.getAction());
        usgMsg.getCollaborationInfo().setConversationId(mo.getConversationId());

        si.laurentius.msh.pmode.AgreementRef ar = ctx.getExchangeAgreementRef();
        if (ar != null) {
            usgMsg.getCollaborationInfo().setAgreementRef(new AgreementRef());
            usgMsg.getCollaborationInfo().getAgreementRef().setPmode(ar.getPmode());
            usgMsg.getCollaborationInfo().getAgreementRef().setValue(ar.getValue());
            usgMsg.getCollaborationInfo().getAgreementRef().setType(ar.getType());
        }

        List<Property> lstProperties = new ArrayList<>();
        // add four corner properties
        if (pisReceiver.isUseFourCornerModel() != null && pisReceiver.isUseFourCornerModel() ||
                pisSender.isUseFourCornerModel() != null && pisSender.isUseFourCornerModel()){
            
            Property pRec = new Property();
            pRec.setName(EBMSConstants.EBMS_PROP_4CM_FINAL_RECIPIENT);
            pRec.setValue(mo.getReceiverEBox());
            lstProperties.add(pRec);
            
            Property pSnd = new Property();
            pSnd.setName(EBMSConstants.EBMS_PROP_4CM_ORIGINAL_SENDER);
            pSnd.setValue(mo.getSenderEBox());
            lstProperties.add(pSnd);

            List<Property> finalRecipientNames = lstProperties.stream().filter(p -> EBMSConstants.EBMS_PROP_4CM_FINAL_RECIPIENT_NAME.equals(p.getName())).collect(Collectors.toList());
            if(finalRecipientNames.isEmpty()) {
                Property pRecName = new Property();
                pRecName.setName(EBMSConstants.EBMS_PROP_4CM_FINAL_RECIPIENT_NAME);
                pRecName.setValue(mo.getReceiverName());
                lstProperties.add(pRecName);
            }
        }
        
        if (ctx.getService().isUseSEDProperties() == null || ctx.getService().
                isUseSEDProperties()) {

            if (!Utils.isEmptyString(mo.getSubject())) {
                Property p = new Property();
                p.setName(EBMSConstants.EBMS_PROPERTY_DESC);
                p.setValue(mo.getSubject());
                lstProperties.add(p);

            }
            // add submit date
            if (mo.getSubmittedDate() != null) {
                Calendar cal = new GregorianCalendar();
                cal.setTime(mo.getSubmittedDate());
                Property p = new Property();
                p.setName(EBMSConstants.EBMS_PROPERTY_SUBMIT_DATE);
                p.setValue(DatatypeConverter.printDateTime(cal));
                lstProperties.add(p);
            }

            if (!Utils.isEmptyString(mo.getSenderMessageId())) {
                Property p = new Property();
                p.setName(EBMSConstants.EBMS_PROPERTY_SENDER_MSG_ID);

                p.setValue(mo.getSenderMessageId());
                lstProperties.add(p);

            }
        }
        // add aditional properties
        if (mo.getMSHOutProperties() != null
                && !mo.getMSHOutProperties().getMSHOutProperties().isEmpty()) {
            for (MSHOutProperty moutProp : mo.getMSHOutProperties().
                    getMSHOutProperties()) {
                Property p = new Property();
                p.setName(moutProp.getName());
                if (!Utils.isEmptyString(moutProp.getType())) {
                    p.setType(moutProp.getType());
                }
                p.setValue(moutProp.getValue());
                lstProperties.add(p);
            }
        }
        if (!lstProperties.isEmpty()) {
            MessageProperties mp = new MessageProperties();
            mp.getProperties().addAll(lstProperties);
            usgMsg.setMessageProperties(mp);
        }

        // add payload info
        usgMsg.setPayloadInfo(new PayloadInfo());
        if (mo.getMSHOutPayload() != null && mo.getMSHOutPayload().getMSHOutParts() != null) {
            for (MSHOutPart mp : mo.getMSHOutPayload().getMSHOutParts()) {
                String attachentId = mp.getEbmsId();

                if (Utils.isEmptyString(attachentId)) {
                    LOG.formatedWarning("NULL ID for attachment for out message %s", mo.
                            getMessageId());
                }
                PartInfo pl = new PartInfo();

                pl.setHref(EBMSConstants.ATT_CID_PREFIX + attachentId); // all parts are attachments!
                if (mp.getDescription() != null && !mp.getDescription().isEmpty()) {
                    pl.setDescription(new Description());
                    pl.getDescription().setLang(Locale.getDefault().getLanguage());
                    pl.getDescription().setValue(mp.getDescription());
                }
                List<Property> fileProp = new ArrayList<>();
                for (OMPartProperty op : mp.getOMPartProperties()) {
                    Property fp = new Property();
                    fp.setName(op.getName());
                    if (!Utils.isEmptyString(op.getType())) {
                        fp.setType(op.getType());
                    }
                    fp.setValue(op.getValue());
                    fileProp.add(fp);
                }

                if (ctx.getTransportProtocol().isGzipCompress() != null && ctx.
                        getTransportProtocol().isGzipCompress()) {
                    Property fp = new Property();
                    fp.setName(EBMSConstants.EBMS_PAYLOAD_COMPRESSION_TYPE);
                    fp.setValue(MimeValue.MIME_GZIP.getMimeType());
                    fileProp.add(fp);

                }

                if (!Utils.isEmptyString(mp.getEncoding())) {
                    Property fp = new Property();
                    fp.setName(EBMSConstants.EBMS_PAYLOAD_PROPERTY_ENCODING);
                    fp.setValue(mp.getEncoding());
                    fileProp.add(fp);
                }

                if (!Utils.isEmptyString(mp.getMimeType())) {
                    Property fp = new Property();
                    fp.setName(EBMSConstants.EBMS_PAYLOAD_PROPERTY_MIME);
                    fp.setValue(mp.getMimeType());
                    fileProp.add(fp);
                }
                if (ctx.getService().isUseSEDProperties() == null || ctx.getService().
                        isUseSEDProperties()) {
                    if (!Utils.isEmptyString(mp.getName())) {
                        Property fp = new Property();
                        fp.setName(EBMSConstants.EBMS_PAYLOAD_PROPERTY_NAME);
                        fp.setValue(mp.getName());
                        fileProp.add(fp);
                    }

                    if (!Utils.isEmptyString(mp.getFilename())) {
                        Property fp = new Property();
                        fp.setName(EBMSConstants.EBMS_PAYLOAD_PROPERTY_FILENAME);
                        fp.setValue(mp.getFilename());
                        fileProp.add(fp);
                    }
                    if (!Utils.isEmptyString(mp.getType())) {
                        Property fp = new Property();
                        fp.setName(EBMSConstants.EBMS_PAYLOAD_PROPERTY_TYPE);
                        fp.setValue(mp.getType());
                        fileProp.add(fp);
                    }
                    Property fp = new Property();
                    fp.setName(EBMSConstants.EBMS_PAYLOAD_PROPERTY_IS_ENCRYPTED);
                    fp.setValue(mp.isIsEncrypted() ? "true" : "false");
                    fileProp.add(fp);
                }

                if (!fileProp.isEmpty()) {
                    pl.setPartProperties(new PartProperties());
                    pl.getPartProperties().getProperties().addAll(fileProp);
                }
                usgMsg.getPayloadInfo().getPartInfos().add(pl);
            }
        }

        return usgMsg;
    }

    /**
     *
     * @param refMessageId
     * @param senderDomain
     * @param inboundMail
     * @param timestamp
     * @return
     */
    public static SignalMessage generateAS4ReceiptSignal(String refMessageId,
            String senderDomain,
            Element inboundMail, Date timestamp) {
        SignalMessage sigMsg = null;
        try (InputStream isXSLT = EBMSBuilder.class.getResourceAsStream(
                "/xslt/as4receipt-jmsh.xsl")) {

            // add message infof
            //sigMsg.setMessageInfo(createMessageInfo(senderDomain, refMessageId, timestamp));
            // generate receipt
            //Receipt rcp = new Receipt();
            // generate as4 receipt from xslt
            Messaging m = (Messaging) XMLUtils.deserialize(inboundMail, isXSLT,
                    Messaging.class);
            if (m != null && m.getSignalMessages().size() == 1) {
                sigMsg = m.getSignalMessages().get(0);
                sigMsg.getMessageInfo().setMessageId(
                        UUID.randomUUID().toString() + "@" + senderDomain);
                sigMsg.getMessageInfo().setTimestamp(timestamp);
            }

        } catch (JAXBException | TransformerException
                | IOException ex) {
            LOG.logError(0, ex);
        }
        return sigMsg;
    }

    /**
     * Method returns message ID, if exists User message: usermessage id is
     * returned else if signal message than first message id is returned
     *
     * @param mi
     * @return Message id
     */
    public static String getFirstMessageId(Messaging mi) {
        if (mi == null) {
            return null;
        } else if (!mi.getUserMessages().isEmpty()) {
            return getUserMessageId(mi.getUserMessages().get(0));
        } else if (!mi.getSignalMessages().isEmpty()) {
            return getSignalMessageId(mi.getSignalMessages().get(0));
        }
        return null;
    }

    /**
     *
     * @param sm Signal message
     * @return
     */
    public static String getSignalMessageId(SignalMessage sm) {
        return sm != null && sm.getMessageInfo() != null ? sm.getMessageInfo().
                getMessageId() : null;
    }

    /**
     *
     * @param um user message
     * @return
     */
    public static String getUserMessageId(UserMessage um) {
        return um != null && um.getMessageInfo() != null ? um.getMessageInfo().
                getMessageId() : null;
    }

    /**
     *
     * @param userMessage
     * @param senderDomain
     * @param inboundMail
     * @param timestamp
     * @return
     */
    public SignalMessage generateAS4ReceiptSignal(UserMessage userMessage,
            String senderDomain,
            File inboundMail, Date timestamp) {
        SignalMessage sigMsg = new SignalMessage();
        try (FileInputStream fos = new FileInputStream(inboundMail);
                InputStream isXSLT = getClass().getResourceAsStream(
                        "/xslt/soap2AS4Receipt.xsl")) {

            // add message infof
            sigMsg.setMessageInfo(createMessageInfo(senderDomain, userMessage.
                    getMessageInfo()
                    .getMessageId(), timestamp));
            // generate receipt
            Receipt rcp = new Receipt();
            // generate as4 receipt from xslt
            Document doc = XMLUtils.deserializeToDom(fos, isXSLT);
            rcp.getAnies().add(doc.getDocumentElement());
            sigMsg.setReceipt(rcp);

        } catch (JAXBException | TransformerException | ParserConfigurationException | SAXException
                | IOException ex) {
            LOG.logError(0, ex);
        }
        return sigMsg;
    }
}
