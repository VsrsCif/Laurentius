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
package si.vsrs.cif.laurentius.plugin.zkp;

import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.message.MessageUtils;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.SignalMessage;
import org.w3c.dom.Element;
import si.vsrs.cif.laurentius.plugin.zkp.doc.DocumentSodBuilder;
import si.vsrs.cif.laurentius.plugin.zkp.enums.FopTransformation;
import si.vsrs.cif.laurentius.plugin.zkp.enums.ZKPPartPropertyType;
import si.vsrs.cif.laurentius.plugin.zkp.enums.ZKPPartType;
import si.vsrs.cif.laurentius.plugin.zkp.exception.ZKPException;
import si.vsrs.cif.laurentius.plugin.zkp.utils.FOPUtils;
import si.vsrs.cif.laurentius.plugin.zkp.utils.ZKPUtils;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.cxf.SoapUtils;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.enums.SEDMailPartSource;
import si.laurentius.commons.enums.SEDOutboxMailStatus;
import si.laurentius.commons.exception.FOPException;
import si.laurentius.commons.exception.HashException;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.SEDCertStoreInterface;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.pmode.EBMSMessageContext;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.Utils;
import si.laurentius.lce.KeystoreUtils;
import si.laurentius.lce.enc.SEDCrypto;
import si.laurentius.lce.sign.pdf.SignUtils;
import si.laurentius.lce.sign.pdf.SignatureInfo;
import si.laurentius.lce.sign.pdf.ValidateSignatureUtils;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.laurentius.msh.outbox.property.MSHOutProperties;
import si.laurentius.msh.outbox.property.MSHOutProperty;
import si.laurentius.plugin.interceptor.MailInterceptorDef;
import si.laurentius.plugin.interfaces.SoapInterceptorInterface;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
@Stateless
@Local(SoapInterceptorInterface.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class ZKPOutInterceptor implements SoapInterceptorInterface {

    @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
    SEDDaoInterface mDB;

    @EJB(mappedName = SEDJNDI.JNDI_DBCERTSTORE)
    SEDCertStoreInterface mCertBean;

    /**
     *
     */
    protected final SEDLogger LOG = new SEDLogger(ZKPOutInterceptor.class);

    /**
     *
     */
    protected final SEDCrypto.SymEncAlgorithms mAlgorithem = SEDCrypto.SymEncAlgorithms.AES256_CBC;

    FOPUtils mfpFop = null;
    
    SEDCrypto mscCrypto = new SEDCrypto();
    ZKPUtils mzkpZKPUtils = new ZKPUtils();

    @Override
    public MailInterceptorDef getDefinition() {
        MailInterceptorDef mid = new MailInterceptorDef();
        mid.setDescription("Sets ZKP out mail with delivery notification");
        mid.setName("ZKP out intercepror");
        mid.setType("ZKPOutInterceptor");
        return mid;
    }

    /**
     * No fault handling?
     * @param t
     */
    @Override
    public void handleFault(SoapMessage t, Properties contextProperties) {
        // ignore
    }

    public void handleZKPOutMessage(EBMSMessageContext ctx, MSHOutMail mail, SoapMessage msg, QName sv) {
        long l = LOG.logStart(msg, ctx, msg);

        if (Arrays.asList(
                ZKPConstants.ZKP_A_SERVICE,
                ZKPConstants.ZKP_B_SERVICE
        ).contains(ctx.getService().getServiceName())) {
            if (ZKPConstants.ZKP_ACTION_DELIVERY_NOTIFICATION.equals(ctx.
                    getAction().getName())) {
                try {
                    prepareToZKPDelivery(mail, ctx, sv);
                } catch (JAXBException | FileNotFoundException | HashException | SEDSecurityException | StorageException | FOPException
                         | ZKPException ex) {
                    LOG.logError(l, ex.getMessage(), ex);
                    throw new SoapFault(ex.getMessage(), sv);
                }
            } else if (Objects.
                    equals(ZKPConstants.ZKP_A_SERVICE, mail.getService())
                    && Objects.equals(ZKPConstants.ZKP_ACTION_ADVICE_OF_DELIVERY,
                    mail.getAction())) {
                processOutZKPAdviceOfDelivery(mail, ctx, msg);
            }
        }
        LOG.logEnd(l);
    }


    /**
     * Interceptor for ZKP out mail
     * @param msg
     */
    @Override
    public boolean handleMessage(SoapMessage msg, Properties contextProperties) {
        long l = LOG.logStart(msg);

        boolean isRequest = MessageUtils.isRequestor(msg);
        QName sv = (isRequest ? SoapFault.FAULT_CODE_CLIENT : SoapFault.FAULT_CODE_SERVER);

        // Handling outbound request messages
        MSHOutMail outMail = SoapUtils.getMSHOutMail(msg);
        if (outMail != null) {
            EBMSMessageContext ctx = SoapUtils.getEBMSMessageOutContext(msg);
            handleZKPOutMessage(ctx, outMail, msg, sv);
        }

        // Handling responses to messages
        MSHInMail mInMail = SoapUtils.getMSHInMail(msg);
        if(mInMail != null) {
            EBMSMessageContext ctx = SoapUtils.getEBMSMessageInContext(msg);
            handleZKPOutResponse(msg, sv, ctx, mInMail);
        }

        LOG.logEnd(l);
        return true;
    }

    private void handleZKPOutResponse(SoapMessage msg, QName sv, EBMSMessageContext eInctx, MSHInMail mInMail) {
        if (Objects.equals(ZKPConstants.ZKP_A_SERVICE, mInMail.getService())
                && Objects.equals(ZKPConstants.ZKP_ACTION_ADVICE_OF_DELIVERY,
                mInMail.getAction())) {
            try {
                processInZKPAdviceOfDelivery(mInMail, eInctx, msg);
            } catch (ZKPException ex) {
                throw new SoapFault(ex.getMessage(), sv);
            }
        }

        if (Objects.equals(ZKPConstants.ZKP_A_SERVICE, mInMail.getService())
                && Objects.equals(ZKPConstants.ZKP_ACTION_ADVICE_OF_DELIVERY,
                mInMail.getAction())) {
            try {
                processInZKPBDeliveryReciept(mInMail, eInctx, msg);
            } catch (ZKPException ex) {
                throw new SoapFault(ex.getMessage(), sv);
            }
        }
    }

    /**
     * Method prepare mail for ZKP delivery. Fist check if mail already has key,
     * encrypted payload and deliveryNotification. If not AES key and delivery
     * notification is generated, than payload are encrypted and addded to mail.
     * Non encrypted payload are removed from mail for further submission!
     *
     * @param outMail - out mail sending by ZKPDelivery
     * @param eoutCtx -context
     * @param sv
     * @throws SEDSecurityException
     * @throws StorageException
     * @throws FOPException
     * @throws HashException
     * @throws ZKPException
     */
    private void prepareToZKPDelivery(MSHOutMail outMail,
            EBMSMessageContext eoutCtx, QName sv)
            throws SEDSecurityException,
            StorageException, FOPException, HashException, ZKPException, JAXBException, FileNotFoundException {

        long l = LOG.logStart(outMail);

        // test mail for attachmetns 
        if (outMail.getMSHOutPayload() == null || outMail.getMSHOutPayload().
                getMSHOutParts().isEmpty()) {
            String mg = "Empty message: " + outMail.getId() + ". No payloads to delivery.";
            throw new SoapFault(mg, sv);
        }
        // test if outmail is already prepared for sending by ZKP
        MSHOutPart mEncKey = null;
        MSHOutPart mDeliveryNotification = null;
        // Encrypted payloads
        List<MSHOutPart> lstEncParts = new ArrayList<>();
        // current encrypted payloads
        Map<String, MSHOutPart> mMEncParts = new HashMap<>();
        // mail part
        List<MSHOutPart> lstMailParts = new ArrayList<>();
        // add/update parts list
        List<MSHOutPart> lstAddMailParts = new ArrayList<>();
        List<MSHOutPart> lstUpdateMailParts = new ArrayList<>();
        List<MSHOutPart> lstRemoveParts = new ArrayList<>();

        // scan mailparts for DeliveryNotification, encrypted parts, encrypted key and user mail content
        for (MSHOutPart op : outMail.getMSHOutPayload().getMSHOutParts()) {

            if (Objects.equals(op.getType(), ZKPPartType.DeliveryNotification.
                    getPartType())) {
                mDeliveryNotification = op;
            } else if (Objects.equals(op.getType(), ZKPPartType.LocalEncryptionKey.
                    getPartType())) {
                mEncKey = op;
            } else if (Objects.equals(op.getType(), ZKPPartType.EncryptedPart.
                    getPartType())) {
                String ebmIdRef = mzkpZKPUtils.getPartProperty(op,
                        ZKPPartPropertyType.RefPartEbmsId.getType());
                mMEncParts.put(ebmIdRef, op);
                // add only emai parts to delivery
            } else if (SEDMailPartSource.MAIL.getValue().equals(op.getSource())) {
                lstMailParts.add(op);
            }
        }

        // set encryption key
        Key skey = null;
        if (mEncKey != null) {
            skey = mzkpZKPUtils.getEncKeyFromLocalPart(mEncKey);
        }

        if (skey == null) {
            // clear encrypted payloads if any
            // replace key
            mMEncParts.entrySet().
                    forEach((entry) -> {
                        lstRemoveParts.add(entry.getValue());
                    });
            mMEncParts.clear();
            // generate new key     
            skey = mscCrypto.getKey(mAlgorithem);
            mEncKey = mzkpZKPUtils.createLocalEncKeyPart(outMail, skey, mAlgorithem);

            // add key to mail
            lstAddMailParts.add(mEncKey);

        }

        // test delivery notification if resending is "next day" - new 
        // notification must be generated
        LocalDateTime dnCreateDate = null;
        String strCreateDate = mDeliveryNotification != null
                ? mzkpZKPUtils.getPartProperty(mDeliveryNotification,
                        ZKPPartPropertyType.PartCreated.getType()) : null;

        if (!Utils.isEmptyString(strCreateDate)) {
            dnCreateDate = LocalDateTime.parse(strCreateDate,
                    DateTimeFormatter.ISO_DATE_TIME);
        }

        if (dnCreateDate == null || dnCreateDate.toLocalDate().isBefore(LocalDate.
                now())) {
            String alias
                    = eoutCtx.getSenderPartyIdentitySet().getLocalPartySecurity().
                            getSignatureKeyAlias();
            PrivateKey pk = mCertBean.getPrivateKeyForAlias(alias);
            X509Certificate xcert = mCertBean.getX509CertForAlias(alias);

            if (mDeliveryNotification != null) {
                mzkpZKPUtils.updateMSHOutPartVisualization(outMail,
                        mDeliveryNotification,
                        ZKPPartType.DeliveryNotification,
                        FopTransformation.DeliveryNotification,
                        pk,
                        xcert);
                lstUpdateMailParts.add(mDeliveryNotification);
            } else {
                mDeliveryNotification = mzkpZKPUtils.createMSHOutPart(
                        outMail,
                        ZKPPartType.DeliveryNotification,
                        FopTransformation.DeliveryNotification,
                        pk, xcert);
                lstAddMailParts.add(mDeliveryNotification);

            }
        }

        // encrypt payloads
        for (MSHOutPart op : lstMailParts) {
            if (op.getIsSent()) {
                // non enc. part is not sent to receiver 
                // update message
                op.setIsSent(Boolean.FALSE);
                lstUpdateMailParts.add(op);
            }
            MSHOutPart encPart = null;
            if (mMEncParts.containsKey(op.getEbmsId())) {
                MSHOutPart mopEnc = mMEncParts.get(op.getEbmsId());
                String partHash = mzkpZKPUtils.getPartProperty(mopEnc,
                        ZKPPartPropertyType.RefPartDigestSHA256.getType());
                // test hash
                if (Objects.equals(partHash, op.getSha256Value())) {
                    encPart = mopEnc;
                    mMEncParts.remove(op.getEbmsId()); // remove from cache
                } else {
                    LOG.formatedWarning(
                            "OutPart (ebmsId %s) for mail %s is encrypted Part for %s, "
                            + "but refHash  do not match. MSHOutPart is encrypted again, old part is discharged!",
                            mopEnc.getEbmsId(), outMail.getMessageId(), op.getEbmsId());
                }
            }
            if (encPart == null) {
                encPart = mzkpZKPUtils.createEncryptedPart(skey, op);
                lstAddMailParts.add(encPart);
            }
            lstEncParts.add(encPart);

        }
        if (!lstAddMailParts.isEmpty()
                || !lstRemoveParts.isEmpty()
                || !lstUpdateMailParts.isEmpty()) {
            // add new payload's / update changed payloads
            mDB.updateOutMailPayload(outMail, lstAddMailParts, lstUpdateMailParts,
                    lstRemoveParts, SEDOutboxMailStatus.PROCESS,
                    "Mail ready to deliver by ZKP delivery protocol", null,
                    ZKPConstants.ZKP_A_SERVICE);
        } else {
            mDB.setStatusToOutMail(outMail, SEDOutboxMailStatus.PROCESS,
                    "Deliver mail by ZKP delivery protocol", null,
                    ZKPConstants.ZKP_A_SERVICE);
        }

        // submit only delivery notification and encrypted parts
        outMail.getMSHOutPayload().getMSHOutParts().clear();

        outMail.getMSHOutPayload().getMSHOutParts().add(mDeliveryNotification);
        outMail.getMSHOutPayload().getMSHOutParts().addAll(lstEncParts);

        // mail
        LOG.logEnd(l,
                "Out mail: '" + outMail.getId() + "' ready to send by LegalZKP!");

    }

    /**
     * Method add sign of AdviceOfDelivey with system certificat.
     * AdviceOfDelivey must be signed by Recipient and secure ebox provider
     * system.
     *
     * @param om
     * @param eoutCtx
     * @param msg
     */
    public void processOutZKPAdviceOfDelivery(MSHOutMail om,
            EBMSMessageContext eoutCtx,
            SoapMessage msg) {

        try {
            String alias
                    = eoutCtx.getSenderPartyIdentitySet().getLocalPartySecurity().
                            getSignatureKeyAlias();
            X509Certificate xcertSed = mCertBean.getX509CertForAlias(alias);

            MSHOutPart mp = om.getMSHOutPayload().getMSHOutParts().get(0);
            File fda = StorageUtils.getFile(mp.getFilepath());
            ValidateSignatureUtils vsu = new ValidateSignatureUtils();
            List<X509Certificate> cslst = vsu.getSignatureCerts(fda);

            // check if delivery advice is already signed
            for (X509Certificate xc : cslst) {
                if (xcertSed.equals(xc)) {
                    // delivey advice is alread signed
                    return;
                }
            }
            // sign delivey advice
            PrivateKey pk = mCertBean.getPrivateKeyForAlias(alias);
            signPDFDocument(pk, xcertSed, fda, true);

        } catch (SEDSecurityException | IOException | CertificateException | NoSuchAlgorithmException | InvalidKeyException
                | NoSuchProviderException | SignatureException ex) {
            Logger.getLogger(ZKPOutInterceptor.class.getName()).
                    log(Level.SEVERE, null, ex);
        }

    }

    public void processInZKPAdviceOfDelivery(MSHInMail mInMail,
            EBMSMessageContext eInCtx, SoapMessage msg) throws ZKPException {
        long l = LOG.logStart();
        try {

            List<MSHOutMail> momLst = mDB.getMailByMessageId(MSHOutMail.class,
                    mInMail.getRefToMessageId());
            if (momLst.isEmpty()) {
                String strMsg = String.format(
                        "No out mail (refId %s) for deliveryAdvice %s ", mInMail.
                                getMessageId(), mInMail.getRefToMessageId());
                throw new ZKPException(strMsg);
            }

            MSHOutMail mom = null;
            for (MSHOutMail mdn : momLst) {
                if (Objects.equals(ZKPConstants.ZKP_A_SERVICE, mdn.getService())
                        && Objects.equals(
                                ZKPConstants.ZKP_ACTION_DELIVERY_NOTIFICATION, mdn.
                                        getAction())
                        && Objects.equals(mInMail.getConversationId(), mdn.
                                getConversationId())) {
                    mom = mdn;
                    break;
                }

            }

            if (mom == null) {
                String strMsg = String.format(
                        "Found out mail (refId %s) but with wrong conversation id, service or action!",
                        mInMail.getMessageId(), mInMail.getRefToMessageId());
                throw new ZKPException(strMsg);
            }

            String alias = eInCtx.getSenderPartyIdentitySet().
                    getExchangePartySecurity().
                    getSignatureCertAlias();

            X509Certificate xcertSed = mCertBean.getX509CertForAlias(alias);

            // AdviceOfDelivery
            File advOfDelivery
                    = StorageUtils.getFile(mInMail.getMSHInPayload().getMSHInParts().
                            get(0).getFilepath());

            ValidateSignatureUtils vsu = new ValidateSignatureUtils();

            List<SignatureInfo> lvc = vsu.validateSignatures(advOfDelivery);

            // AdviceOfDelivery must have two signatures: recipient and 
            // delivery system
            Calendar minDate = null;
            if (lvc.size() == 2 && (lvc.get(1).isSignerCertEquals(xcertSed)
                    || lvc.get(0).isSignerCertEquals(xcertSed))) {

                // add secred key for all signed certificates
                for (SignatureInfo sigInfo : lvc) {
                    minDate = minDate == null || minDate.after(sigInfo.getDate())
                            ? sigInfo.getDate()
                            : minDate;

                    X509Certificate xc = sigInfo.getSignerCert();

                    // get key
                    Key key = mzkpZKPUtils.getEncKeyFromOut(mom);
                    LOG.log("processInZKPAdviceoFDelivery - get key" + key);
                    Element elKey
                            = mscCrypto.encryptedKeyWithReceiverPublicKey(key, xc,
                                    mInMail.
                                            getSenderEBox(),
                                    mInMail.getConversationId());
                    LOG.log("processInZKPAdviceoFDelivery - get encrypted key" + elKey);
                    // got signal message:
                    SignalMessage signal = msg.getExchange().get(SignalMessage.class);
                    signal.getAnies().add(elKey);
                }

                //mom.setDeliveredDate(mInMail.getSentDate());
                mom.setDeliveredDate(minDate == null ? mInMail.getSentDate() : minDate.getTime());
                mDB.setStatusToOutMail(mom, SEDOutboxMailStatus.DELIVERED,
                        "Received ZKP advice of delivery",
                        null, null, StorageUtils.getRelativePath(advOfDelivery),
                        MimeValue.MIME_XML.getMimeType());

            }

        } catch (SEDSecurityException | StorageException | IOException | CertificateException | SignatureException ex) {
            LOG.logError(l, ex);
            throw new ZKPException("Error processing AdviceOfDelivery", ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ZKPOutInterceptor.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
        LOG.logEnd(l);
    }

    /**
     *
     * @param mInMail
     * @param eInCtx
     * @param msg
     * @throws ZKPException
     */
    public void processInZKPBDeliveryReciept(MSHInMail mInMail,
            EBMSMessageContext eInCtx, SoapMessage msg) throws ZKPException {
        long l = LOG.logStart();
        try {

            List<MSHOutMail> momLst = mDB.getMailByMessageId(MSHOutMail.class,
                    mInMail.getRefToMessageId());
            if (momLst.isEmpty()) {
                String strMsg = String.format(
                        "No out mail (refId %s) for deliveryAdvice %s ", mInMail.
                                getMessageId(), mInMail.getRefToMessageId());
                throw new ZKPException(strMsg);
            }

            MSHOutMail mom = null;
            for (MSHOutMail mdn : momLst) {

                if (Objects.equals(ZKPConstants.ZKP_A_SERVICE, mdn.getService())
                        && Objects.equals(
                                ZKPConstants.ZKP_ACTION_DELIVERY_NOTIFICATION, mdn.
                                        getAction())
                        && Objects.equals(mInMail.getConversationId(), mdn.
                                getConversationId())) {
                    mom = mdn;
                    break;
                }

            }

            if (mom == null) {
                String strMsg = String.format(
                        "Found out mail (refId %s) but with wrong conversation id, service or action!",
                        mInMail.getMessageId(), mInMail.getRefToMessageId());
                throw new ZKPException(strMsg);
            }

            mom.setDeliveredDate(mInMail.getSentDate());

            String alias
                    = eInCtx.getSenderPartyIdentitySet().getExchangePartySecurity().
                            getSignatureCertAlias();

            X509Certificate xcertSed = mCertBean.getX509CertForAlias(alias);

            // AdviceOfDelivery
            File advOfDelivery
                    = StorageUtils.getFile(mInMail.getMSHInPayload().getMSHInParts().
                            get(0).getFilepath());

            ValidateSignatureUtils vsu = new ValidateSignatureUtils();
            List<X509Certificate> lvc = vsu.getSignatureCerts(advOfDelivery.
                    getAbsolutePath());

            // AdviceOfDelivery must have two signatures: recipient and 
            // delivery system
            if (lvc.size() == 1 && lvc.get(0).equals(
                    xcertSed)) {

                // add secred key for all signed certificates
                for (X509Certificate xc : lvc) {

                    // get key
                    Key key = mzkpZKPUtils.getEncKeyFromOut(mom);
                    LOG.log("processInZKPAdviceoFDelivery - get key" + key);
                    Element elKey
                            = mscCrypto.encryptedKeyWithReceiverPublicKey(key, xc,
                                    mInMail.
                                            getSenderEBox(),
                                    mInMail.getConversationId());
                    LOG.log("processInZKPAdviceoFDelivery - get encrypted key" + elKey);
                    // got signal message:
                    SignalMessage signal = msg.getExchange().get(SignalMessage.class);
                    signal.getAnies().add(elKey);
                }

                mDB.setStatusToOutMail(mom, SEDOutboxMailStatus.DELIVERED,
                        "Received ZKP advice of delivery",
                        null, null, StorageUtils.getRelativePath(advOfDelivery),
                        MimeValue.MIME_XML.getMimeType());

            }

        } catch (NoSuchProviderException | SEDSecurityException | StorageException | IOException | CertificateException | NoSuchAlgorithmException | InvalidKeyException | SignatureException ex) {
            LOG.logError(l, ex);
            throw new ZKPException("Error processing AdviceOfDelivery", ex);
        }
        LOG.logEnd(l);
    }

    private File signPDFDocument(PrivateKey pk, X509Certificate xcert, File f,
            boolean replace) {
        long l = LOG.logStart();
        File ftmp = null;
        try {
            ftmp = StorageUtils.getNewStorageFile("pdf", "zkp-signed");

            SignUtils su = new SignUtils(pk, xcert);
            su.signPDF(f, ftmp, true);
            if (replace) {
                Files.move(ftmp.toPath(), f.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
                ftmp = f;
            }
        } catch (StorageException | IOException ex) {
            LOG.logError(l, ex);
        }
        return ftmp;
    }
}
