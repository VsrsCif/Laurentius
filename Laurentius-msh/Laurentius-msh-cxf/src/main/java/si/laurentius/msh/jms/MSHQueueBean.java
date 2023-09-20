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
package si.laurentius.msh.jms;

import java.io.File;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.persistence.NoResultException;
import si.jrc.msh.client.MSHPluginOutEventHandler;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.pmode.ReceptionAwareness;
import si.jrc.msh.client.MshClient;
import si.jrc.msh.client.Result;
import si.jrc.msh.exception.EBMSErrorCode;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.enums.SEDOutboxMailStatus;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.SEDValues;
import si.laurentius.commons.enums.SEDMailPartSource;
import si.laurentius.commons.exception.PModeException;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.interfaces.SEDCertUtilsInterface;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.pmode.EBMSMessageContext;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.Utils;
import si.laurentius.lce.DigestUtils;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.laurentius.msh.pmode.PMode;
import si.laurentius.plugin.interfaces.OutMailEventInterface;

/**
 *
 * @author Jože Rihtaršič
 */
@MessageDriven(
        activationConfig = {
            @ActivationConfigProperty(propertyName = "acknowledgeMode",
                    propertyValue = "Auto-acknowledge")
            ,
      @ActivationConfigProperty(propertyName = "destinationType",
                    propertyValue = "javax.jms.Queue")
            ,
      @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/MSHQueue")
            ,
      @ActivationConfigProperty(propertyName = "maxSession",
                    propertyValue = "8")})
@TransactionManagement(TransactionManagementType.BEAN)
public class MSHQueueBean implements MessageListener {

    /**
     *
     */
    public static final SEDLogger LOG = new SEDLogger(MSHQueueBean.class);

    @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
    SEDDaoInterface mDB;

    @EJB(mappedName = SEDJNDI.JNDI_DBCERTUTILS)
    SEDCertUtilsInterface mCertBean;

    MshClient mmshClient = new MshClient();

    @EJB(mappedName = SEDJNDI.JNDI_PMODE)
    PModeInterface mpModeManager;

    StorageUtils msStorageUtils = new StorageUtils();
    MSHPluginOutEventHandler mPluginOutEventHandler = new MSHPluginOutEventHandler();

    /**
     * implementation of onMessage methods for submiting MSH out user message.
     *
     * @param msg jms wiht parameters Mail id, jmsRetryCount and jmsRetryDelay
     */
    @Override
    public void onMessage(Message msg) {
        long t = LOG.logStart();
        // parse JMS Message data 
        long jmsMessageId; // 
        int jmsRetryCount = 0;
        long jmsRetryDelay = -1;

        // Read property Mail ID
        try {
            jmsMessageId = msg.getLongProperty(SEDValues.EBMS_QUEUE_PARAM_MAIL_ID);
        } catch (JMSException ex) {
            LOG.logError(t, String.format(
                    "Bad JMS message for queue: 'MSHQueue' with no property: '%s'",
                    SEDValues.EBMS_QUEUE_PARAM_MAIL_ID), ex);
            return;
        }

        try {
            jmsRetryCount = msg.getIntProperty(SEDValues.EBMS_QUEUE_PARAM_RETRY);
        } catch (JMSException ex) {
            LOG.logError(t, String.format(
                    "JMS message for queue: 'MSHQueue' with no property: '%s'",
                    SEDValues.EBMS_QUEUE_PARAM_RETRY), ex);
        }
        try {
            jmsRetryDelay = msg.getLongProperty(SEDValues.EBMS_QUEUE_PARAM_DELAY);
        } catch (JMSException ex) {
            LOG.logError(t, String.format(
                    "JMS message for queue: 'MSHQueue' with no property: '%s'",
                    SEDValues.EBMS_QUEUE_PARAM_DELAY), ex);
        }
        LOG.formatedlog(
                "Start pushing out mail with ID: %s ",
                jmsMessageId);

        MSHOutMail mail;
        try {
            mail = mDB.getMailById(MSHOutMail.class, BigInteger.valueOf(jmsMessageId));
            LOG.formatedlog("Got out mail with ID: %s, ebmsId: %s ",
                    jmsMessageId, mail.getMessageId());
        } catch (NoResultException ex) {
            LOG.logError(t,
                    "Message with id: '" + jmsMessageId + "' not exists in DB!", ex);
            return;
        }
        LOG.formatedlog("Get EBMSMessageContext for message: %s", jmsMessageId);

        if (SEDOutboxMailStatus.DELIVERED.getValue().equals(mail.getStatus())
                || SEDOutboxMailStatus.DELETED.getValue().equals(mail.getStatus())
                || SEDOutboxMailStatus.SENT.getValue().equals(mail.getStatus())) {
            String strMsg = String.format(
                    "Out mail '%d' in wrong status: '%s'. Mail sending suppresed! ",
                    jmsMessageId, mail.getStatus());
            LOG.logError(t, strMsg, null);
            return;

        }
        // get pmode EBMSMessageContext

        EBMSMessageContext sd;
        try {
            sd = mpModeManager.createMessageContextForOutMail(mail);
        } catch (PModeException ex) {
            String errDesc = String.format(
                    "Error retrieving EBMSMessageContext for message id: '%d'. Error: %s",
                    jmsMessageId, ex.getMessage());
            LOG.logError(t, errDesc, ex);
            setStatusToOutMail(mail, SEDOutboxMailStatus.FAILED, errDesc, ex);
            mPluginOutEventHandler.outEvent(mail, null,
                    OutMailEventInterface.PluginOutEvent.FAILED);
            return;
        } catch (EJBException ex) {
            Exception exc = ex.getCausedByException();
            String errDesc = String.format(
                    "Error retrieving EBMSMessageContext for message id: '%d'. Error: %s",
                    jmsMessageId, Utils.getInitCauseMessage(exc));
            LOG.logError(t, errDesc, ex);
            setStatusToOutMail(mail, SEDOutboxMailStatus.FAILED, errDesc, ex);
            mPluginOutEventHandler.outEvent(mail, null,
                    OutMailEventInterface.PluginOutEvent.FAILED);
            return;
        }

        if (sd.getReceiverPartyIdentitySet().isActive() != null
                && !sd.getReceiverPartyIdentitySet().isActive()) {
            String errDesc = String.format(
                    "Receiver %s is inactive. (Check pmode settings.) Message is set to pending status.",
                    sd.getReceiverPartyIdentitySet().getId());
            LOG.logWarn(t, errDesc, null);
            setStatusToOutMail(mail, SEDOutboxMailStatus.PENDING, errDesc, null);

            return;
        }

        // create ebms-message id
        if (Utils.isEmptyString(mail.getMessageId())) {
            mail.setMessageId(Utils.getUUIDWithDomain(SEDSystemProperties.
                    getLocalDomain()));
        }

        if (!sd.isPushTransfrer()) {
            setStatusToOutMail(mail, SEDOutboxMailStatus.PULLREADY,
                    "Message ready for pull signal!");
            LOG.formatedlog("Start pushing  message: %s", jmsMessageId);
        } else {
            LOG.formatedlog("Start pushing  message: %s", jmsMessageId);
            setStatusToOutMail(mail, SEDOutboxMailStatus.PUSHING,
                    "Start pushing to receiver MSH");

            mail.setSentDate(Calendar.getInstance().getTime());

            Result sm = mmshClient.pushMessage(mail, sd, mCertBean);

            if (sm.getError() != null) {

                mPluginOutEventHandler.outEvent(mail, sd,
                        OutMailEventInterface.PluginOutEvent.ERROR);

               
                setStatusToOutMail(mail, SEDOutboxMailStatus.ERROR, sm.getError().
                        getSubMessage(),
                        sm.getResultFile(), sm.getMimeType());

                if ((sm.getError().getEbmsErrorCode().equals(
                        EBMSErrorCode.ConnectionFailure)
                        || sm.getError().getEbmsErrorCode().equals(
                                EBMSErrorCode.DeliveryFailure)
                        || sm.getError().getEbmsErrorCode().equals(EBMSErrorCode.Other))) {

                    if (resendMail(mail, sd, jmsRetryCount, jmsRetryDelay)) {
                        mPluginOutEventHandler.outEvent(mail, sd,
                                OutMailEventInterface.PluginOutEvent.RESEND);
                    } else {
                        
                        
                        setStatusToOutMail(mail, SEDOutboxMailStatus.FAILED,
                                "Max resend mail reached",
                                null, sm.getMimeType());

                    }

                } else if (sm.getError().getEbmsErrorCode().equals(
                        EBMSErrorCode.ReceiverNotExists)) {
                    
                    try {
                        addPartToMessage(sm, SEDOutboxMailStatus.NOTDELIVERED, "Add error to message", mail);
                    } catch (StorageException ex) {
                        LOG.logError(sm.getResultFile(), ex);
                    }
                    
                    setStatusToOutMail(mail, SEDOutboxMailStatus.NOTDELIVERED,
                            sm.getError().getSubMessage(),
                            null, sm.getMimeType());

                } else {
                    try {
                        addPartToMessage(sm, SEDOutboxMailStatus.ERROR, "Add error to message", mail);
                    } catch (StorageException ex) {
                        LOG.logError(sm.getResultFile(), ex);
                    }
                    setStatusToOutMail(mail, SEDOutboxMailStatus.FAILED,
                            "Configuration error: " + sm.getError().getSubMessage(),
                            null, sm.getMimeType());

                    mPluginOutEventHandler.outEvent(mail, sd,
                            OutMailEventInterface.PluginOutEvent.FAILED);
                }

            } else {
                String resPath = sm.getResultFile();
               
                try {
                    
                    addPartToMessage(sm, SEDOutboxMailStatus.SENT, "Add AS4Reciept to mail", mail);

                    mDB.setStatusToOutMail(mail.getId(),
                            mail.getSenderMessageId(), mail.getSentDate(),
                            mail.getReceivedDate(), null, SEDOutboxMailStatus.SENT,
                            "Get delivery receipt", null, "ebms", null, null);
                    mPluginOutEventHandler.outEvent(mail, sd,
                            OutMailEventInterface.PluginOutEvent.SEND);
                } catch (StorageException ex) {
                    LOG.logError(resPath, ex);
                }
            }
        }
        LOG.logEnd(t, jmsMessageId);
    }

    private void addPartToMessage(Result sm, SEDOutboxMailStatus status, String message,  MSHOutMail mail) throws StorageException {
        String resPath = sm.getResultFile();
        File fRes = StorageUtils.getFile(resPath);

        MSHOutPart mipt = new MSHOutPart();

        mipt.setIsSent(Boolean.FALSE);
        mipt.setIsReceived(Boolean.TRUE);

        mipt.setMailId(mail.getId());
        mipt.setDescription("Soap response");
        mipt.setEbmsId(Utils.getUUIDWithLocalDomain()); // set response id?
        mipt.setName(fRes.getName());
        mipt.setSource(SEDMailPartSource.EBMS.getValue());
        mipt.setMimeType(MimeValue.MIME_XML.getMimeType());
        mipt.setFilename(fRes.getName());

        mipt.setSize(BigInteger.valueOf(fRes.length()));
        mipt.setSha256Value(DigestUtils.getBase64Sha256Digest(fRes));
        mipt.setFilepath(resPath);
        mail.getMSHOutPayload().getMSHOutParts().add(mipt);

        mDB.addOutMailPayload(mail, Collections.singletonList(mipt),
                status, message, null,
                "ebms"
        );

    }

    /**
     * Method sets message back in queue for sending
     *
     * @param mail - mail to be resend
     * @param sd
     * @param jmsRetryCount
     * @param jmsRetryDelay
     * @return
     */
    public boolean resendMail(MSHOutMail mail, EBMSMessageContext sd,
            int jmsRetryCount,
            long jmsRetryDelay) {

        PMode pmd = sd.getPMode();
        int iPriority = pmd.getPriority() == null ? 4 : pmd.getPriority();
        iPriority = iPriority > 9 ? 9 : (iPriority < 0 ? 0 : iPriority);

        long t = LOG.logStart();
        boolean bResend = false;
        if (sd.getReceptionAwareness() != null && sd.getReceptionAwareness().
                getRetry() != null) {
            ReceptionAwareness.Retry rty = sd.getReceptionAwareness().getRetry();

            if (jmsRetryCount < rty.getMaxRetries()) {
                jmsRetryCount++;
                if (jmsRetryDelay <= 0) {
                    jmsRetryDelay = rty.getPeriod();
                } else {
                    jmsRetryDelay *= rty.getMultiplyPeriod();
                }
                try {
                    LOG.formatedWarning("Resend mail: %d retry %d delay %d", mail.getId(),
                            jmsRetryCount,
                            jmsRetryDelay);
                    mDB.sendOutMessage(mail, jmsRetryCount, jmsRetryDelay, iPriority,
                            null, null);

                    bResend = true;
                } catch (StorageException ex1) {
                    String errDesc = String.format(
                            "Mail resend %d  error occured. Err: %s", mail.getId(),
                            ex1.getMessage());
                    LOG.logError(t, errDesc, ex1);
                    setStatusToOutMail(mail, SEDOutboxMailStatus.ERROR,
                            errDesc + " " + ex1.getMessage());
                }
            }
        }
        return bResend;
    }

    /**
     * Set status to out mail - method chages mail status log event to "event
     * table"
     *
     * @param mail -
     * @param status - status
     * @param desc - description of event
     * @param ex - if throwable not nul "getMessage()" is appended to desc
     * parameter
     */
    public void setStatusToOutMail(MSHOutMail mail, SEDOutboxMailStatus status,
            String desc,
            Throwable ex) {
        String strpath = null;
        if (ex != null) {

            try {
                strpath = msStorageUtils.storeThrowableAndGetRelativePath(ex);
            } catch (StorageException ex1) {
                LOG.logError(LOG.getTime(), "Error storing evidence error", ex1);
            }
        }
        if (Utils.isEmptyString(strpath)) {
            setStatusToOutMail(mail, status, desc);
        } else {
            setStatusToOutMail(mail, status, desc, strpath, MimeValue.MIME_TXT.
                    getMimeType());
        }

    }

    /**
     * Set status to out mail - method chages mail status log event to "event
     * table"
     *
     * @param mail -
     * @param status - status
     * @param desc - description of event
     */
    public void setStatusToOutMail(MSHOutMail mail, SEDOutboxMailStatus status,
            String desc) {
        long l = LOG.logStart();
        try {
            mDB.setStatusToOutMail(mail, status, desc);
        } catch (StorageException ex2) {
            LOG.logError(l,
                    "Error occurred while setting status " + status.getValue() + " to MSHOutMail :'"
                    + mail.getId()
                    + "'!", ex2);
        }
    }

    public void setStatusToOutMail(MSHOutMail mail, SEDOutboxMailStatus status,
            String desc,
            String fileName, String mime) {
        long l = LOG.logStart();
        try {
            mDB.setStatusToOutMail(mail, status, desc, null, null, fileName, mime);
        } catch (StorageException ex2) {
            LOG.logError(l,
                    "Error occurred while setting status " + status.getValue() + " to MSHOutMail :'"
                    + mail.getId()
                    + "'!", ex2);
        }
    }

}
