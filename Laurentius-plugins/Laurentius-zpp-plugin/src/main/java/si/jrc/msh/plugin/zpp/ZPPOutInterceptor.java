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
package si.jrc.msh.plugin.zpp;

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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.message.MessageUtils;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.SignalMessage;
import org.w3c.dom.Element;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.jrc.msh.plugin.zpp.doc.DocumentSodBuilder;
import si.jrc.msh.plugin.zpp.enums.FopTransformation;
import si.jrc.msh.plugin.zpp.enums.ZPPPartPropertyType;
import si.jrc.msh.plugin.zpp.enums.ZPPPartType;
import si.jrc.msh.plugin.zpp.exception.ZPPException;
import si.jrc.msh.plugin.zpp.utils.FOPUtils;
import si.jrc.msh.plugin.zpp.utils.ZPPUtils;
import si.laurentius.lce.enc.SEDCrypto;
import si.laurentius.lce.sign.pdf.SignUtils;
import si.laurentius.lce.sign.pdf.ValidateSignatureUtils;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.enums.SEDOutboxMailStatus;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.cxf.SoapUtils;
import si.laurentius.commons.enums.SEDMailPartSource;
import si.laurentius.commons.exception.FOPException;
import si.laurentius.commons.exception.HashException;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.pmode.EBMSMessageContext;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;

import si.laurentius.lce.KeystoreUtils;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.commons.interfaces.SEDCertStoreInterface;
import si.laurentius.commons.utils.Utils;
import si.laurentius.lce.sign.pdf.SignatureInfo;
import si.laurentius.msh.outbox.property.MSHOutProperties;
import si.laurentius.msh.outbox.property.MSHOutProperty;
import si.laurentius.plugin.interceptor.MailInterceptorDef;
import si.laurentius.plugin.interfaces.SoapInterceptorInterface;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
@Stateless
@Local(SoapInterceptorInterface.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class ZPPOutInterceptor implements SoapInterceptorInterface {

    @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
    SEDDaoInterface mDB;

    @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
    SEDLookupsInterface mdbLookup;

    @EJB(mappedName = SEDJNDI.JNDI_DBCERTSTORE)
    SEDCertStoreInterface mCertBean;

    /**
     *
     */
    protected final SEDLogger LOG = new SEDLogger(ZPPOutInterceptor.class);
    DocumentSodBuilder dsbSodBuilder = new DocumentSodBuilder();

    /**
     *
     */
    protected final SEDCrypto.SymEncAlgorithms mAlgorithem = SEDCrypto.SymEncAlgorithms.AES256_CBC;

    FOPUtils mfpFop = null;
    ;
  SEDCrypto mscCrypto = new SEDCrypto();
    KeystoreUtils mksu = new KeystoreUtils();
    ZPPUtils mzppZPPUtils = new ZPPUtils();
    KeystoreUtils mKeystoreUtils = new KeystoreUtils();

    /**
     *
     * @return
     */
    public FOPUtils getFOP() {
        if (mfpFop == null) {
            File fconf
                    = new File(SEDSystemProperties.getPluginsFolder(),
                            ZPPConstants.SVEV_FOLDER + File.separator + ZPPConstants.FOP_CONFIG_FILENAME);

            mfpFop
                    = new FOPUtils(fconf, SEDSystemProperties.getPluginsFolder().
                            getAbsolutePath()
                            + File.separator + ZPPConstants.SVEV_FOLDER + File.separator
                            + ZPPConstants.XSLT_FOLDER);
        }
        return mfpFop;
    }

    @Override
    public MailInterceptorDef getDefinition() {
        MailInterceptorDef mid = new MailInterceptorDef();
        mid.setDescription("Sets ZPP out mail with delivery notification");
        mid.setName("ZPP out intercepror");
        mid.setType("ZPPOutInterceptor");
        return mid;
    }

    /**
     *
     * @param t
     */
    @Override
    public void handleFault(SoapMessage t, Properties contextProperties) {
        // ignore
    }

    /**
     *
     * @param msg
     */
    @Override
    public boolean handleMessage(SoapMessage msg, Properties contextProperties) {
        long l = LOG.logStart(msg);

        boolean isRequest = MessageUtils.isRequestor(msg);
        QName sv = (isRequest ? SoapFault.FAULT_CODE_CLIENT : SoapFault.FAULT_CODE_SERVER);

        EBMSMessageContext ectx = SoapUtils.getEBMSMessageOutContext(msg);
        EBMSMessageContext eInctx = SoapUtils.getEBMSMessageInContext(msg);
        MSHOutMail outMail = SoapUtils.getMSHOutMail(msg);
        MSHInMail mInMail = SoapUtils.getMSHInMail(msg);

        if (outMail != null && (ZPPConstants.S_ZPP_SERVICE.equals(ectx.getService().
                getServiceName())
                || ZPPConstants.S_ZPPB_SERVICE.equals(ectx.getService().
                        getServiceName()))) {

            if (ZPPConstants.S_ZPP_ACTION_DELIVERY_NOTIFICATION.equals(ectx.
                    getAction().getName())) {
                try {
                    prepareToZPPDelivery(outMail, ectx, sv);
                } catch (JAXBException | FileNotFoundException | HashException | SEDSecurityException | StorageException | FOPException
                        | ZPPException ex) {
                    LOG.logError(l, ex.getMessage(), ex);
                    throw new SoapFault(ex.getMessage(), sv);
                }
            } else if (Objects.
                    equals(ZPPConstants.S_ZPP_SERVICE, outMail.getService())
                    && Objects.equals(ZPPConstants.S_ZPP_ACTION_ADVICE_OF_DELIVERY,
                            outMail.getAction())) {
                processOutZPPAdviceOfDelivery(outMail, ectx, msg);
            }

        }

        if (mInMail != null) {
            if (Objects.equals(ZPPConstants.S_ZPP_SERVICE, mInMail.getService())
                    && Objects.equals(ZPPConstants.S_ZPP_ACTION_ADVICE_OF_DELIVERY,
                            mInMail.getAction())) {
                try {
                    processInZPPAdviceOfDelivery(mInMail, eInctx, msg);
                } catch (ZPPException ex) {
                    throw new SoapFault(ex.getMessage(), sv);
                }
            }

            if (Objects.equals(ZPPConstants.S_ZPPB_SERVICE, mInMail.getService())
                    && Objects.equals(ZPPConstants.S_ZPP_ACTION_ADVICE_OF_DELIVERY,
                            mInMail.getAction())) {
                try {
                    processInZPPBDeliveryReciept(mInMail, eInctx, msg);
                } catch (ZPPException ex) {
                    throw new SoapFault(ex.getMessage(), sv);
                }
            }
        }
        LOG.logEnd(l);
        return true;
    }

    private void updateMailProperty(MSHOutMail mom, String key, String value) {
        MSHOutProperties mop = mom.getMSHOutProperties();
        if (mop == null) {
            mop = new MSHOutProperties();
            mom.setMSHOutProperties(mop);
        }

        Optional<MSHOutProperty> oPrp = mop.getMSHOutProperties().stream()
                .filter(p -> p.getName().equalsIgnoreCase(key)).findFirst();
        if (!oPrp.isPresent()) {
            MSHOutProperty p = new MSHOutProperty();
            p.setName(key);
            p.setValue(value);
            mop.getMSHOutProperties().add(p);
        }
    }

    /**
     * Method prepare mail for ZPP delivery. Fist check if mail already has key,
     * encrypted payload and deliveryNotification. If not AES key and delivery
     * notification is generated, than payload are encrypted and addded to mail.
     * Non encrypted payload are removed from mail for further submission!
     *
     * @param outMail - out mail sending by ZPPDelivery
     * @param eoutCtx -context
     * @param sv
     * @throws SEDSecurityException
     * @throws StorageException
     * @throws FOPException
     * @throws HashException
     * @throws ZPPException
     */
    private void prepareToZPPDelivery(MSHOutMail outMail,
            EBMSMessageContext eoutCtx, QName sv)
            throws SEDSecurityException,
            StorageException, FOPException, HashException, ZPPException, JAXBException, FileNotFoundException {

        long l = LOG.logStart(outMail);

        // test mail for attachmetns 
        if (outMail.getMSHOutPayload() == null || outMail.getMSHOutPayload().
                getMSHOutParts().isEmpty()) {
            String mg = "Empty message: " + outMail.getId() + ". No payloads to delivery.";
            throw new SoapFault(mg, sv);
        }
        // test if outmail is already prepared for sending by ZPP
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

            if (Objects.equals(op.getType(), ZPPPartType.DeliveryNotification.
                    getPartType())) {
                mDeliveryNotification = op;
            } else if (Objects.equals(op.getType(), ZPPPartType.LocalEncryptionKey.
                    getPartType())) {
                mEncKey = op;
            } else if (Objects.equals(op.getType(), ZPPPartType.EncryptedPart.
                    getPartType())) {
                String ebmIdRef = mzppZPPUtils.getPartProperty(op,
                        ZPPPartPropertyType.RefPartEbmsId.getType());
                mMEncParts.put(ebmIdRef, op);
                // add only emai parts to delivery
            } else if (SEDMailPartSource.MAIL.getValue().equals(op.getSource())) {
                lstMailParts.add(op);
            }
        }

        // set encryption key
        Key skey = null;
        if (mEncKey != null) {
            skey = mzppZPPUtils.getEncKeyFromLocalPart(mEncKey);
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
            mEncKey = mzppZPPUtils.createLocalEncKeyPart(outMail, skey, mAlgorithem);

            // add key to mail
            lstAddMailParts.add(mEncKey);

        }

        // test delivery notification if resending is "next day" - new 
        // notification must be generated
        LocalDateTime dnCreateDate = null;
        String strCreateDate = mDeliveryNotification != null
                ? mzppZPPUtils.getPartProperty(mDeliveryNotification,
                        ZPPPartPropertyType.PartCreated.getType()) : null;

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
                mzppZPPUtils.updateMSHOutPartVisualization(outMail,
                        mDeliveryNotification,
                        ZPPPartType.DeliveryNotification,
                        ZPPConstants.S_ZPPB_SERVICE.equals(outMail.getService())
                        ? FopTransformation.DeliveryNotificationB
                        : FopTransformation.DeliveryNotification,
                        pk,
                        xcert);
                lstUpdateMailParts.add(mDeliveryNotification);
            } else {
                mDeliveryNotification = mzppZPPUtils.createMSHOutPart(
                        outMail,
                        ZPPPartType.DeliveryNotification,
                        ZPPConstants.S_ZPPB_SERVICE.equals(outMail.getService())
                        ? FopTransformation.DeliveryNotificationB
                        : FopTransformation.DeliveryNotification,
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
                String partHash = mzppZPPUtils.getPartProperty(mopEnc,
                        ZPPPartPropertyType.RefPartDigestSHA256.getType());
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
                encPart = mzppZPPUtils.createEncryptedPart(skey, op);
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
                    "Mail ready to deliver by ZPP delivery protocol", null,
                    ZPPConstants.S_ZPP_PLUGIN_TYPE);
        } else {
            mDB.setStatusToOutMail(outMail, SEDOutboxMailStatus.PROCESS,
                    "Deliver mail by ZPP delivery protocol", null,
                    ZPPConstants.S_ZPP_PLUGIN_TYPE);
        }

        // submit only delivery notification and encrypted parts
        outMail.getMSHOutPayload().getMSHOutParts().clear();

        outMail.getMSHOutPayload().getMSHOutParts().add(mDeliveryNotification);
        outMail.getMSHOutPayload().getMSHOutParts().addAll(lstEncParts);

        // mail
        LOG.logEnd(l,
                "Out mail: '" + outMail.getId() + "' ready to send by LegalZPP!");

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
    public void processOutZPPAdviceOfDelivery(MSHOutMail om,
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
            Logger.getLogger(ZPPOutInterceptor.class.getName()).
                    log(Level.SEVERE, null, ex);
        }

    }

    public void processInZPPAdviceOfDelivery(MSHInMail mInMail,
            EBMSMessageContext eInCtx, SoapMessage msg) throws ZPPException {
        long l = LOG.logStart();
        try {

            List<MSHOutMail> momLst = mDB.getMailByMessageId(MSHOutMail.class,
                    mInMail.getRefToMessageId());
            if (momLst.isEmpty()) {
                String strMsg = String.format(
                        "No out mail (refId %s) for deliveryAdvice %s ", mInMail.
                                getMessageId(), mInMail.getRefToMessageId());
                throw new ZPPException(strMsg);
            }

            MSHOutMail mom = null;
            for (MSHOutMail mdn : momLst) {
                if (Objects.equals(ZPPConstants.S_ZPP_SERVICE, mdn.getService())
                        && Objects.equals(
                                ZPPConstants.S_ZPP_ACTION_DELIVERY_NOTIFICATION, mdn.
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
                throw new ZPPException(strMsg);
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
                    Key key = mzppZPPUtils.getEncKeyFromOut(mom);
                    LOG.log("processInZPPAdviceoFDelivery - get key" + key);
                    Element elKey
                            = mscCrypto.encryptedKeyWithReceiverPublicKey(key, xc,
                                    mInMail.
                                            getSenderEBox(),
                                    mInMail.getConversationId());
                    LOG.log("processInZPPAdviceoFDelivery - get encrypted key" + elKey);
                    // got signal message:
                    SignalMessage signal = msg.getExchange().get(SignalMessage.class);
                    signal.getAnies().add(elKey);
                }

                //mom.setDeliveredDate(mInMail.getSentDate());
                mom.setDeliveredDate(minDate == null ? mInMail.getSentDate() : minDate.getTime());
                mDB.setStatusToOutMail(mom, SEDOutboxMailStatus.DELIVERED,
                        "Received ZPP advice of delivery",
                        null, null, StorageUtils.getRelativePath(advOfDelivery),
                        MimeValue.MIME_XML.getMimeType());

            }

        } catch (SEDSecurityException | StorageException | IOException | CertificateException | SignatureException ex) {
            LOG.logError(l, ex);
            throw new ZPPException("Error processing AdviceOfDelivery", ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ZPPOutInterceptor.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
        LOG.logEnd(l);
    }

    /**
     *
     * @param mInMail
     * @param eInCtx
     * @param msg
     * @throws ZPPException
     */
    public void processInZPPBDeliveryReciept(MSHInMail mInMail,
            EBMSMessageContext eInCtx, SoapMessage msg) throws ZPPException {
        long l = LOG.logStart();
        try {

            List<MSHOutMail> momLst = mDB.getMailByMessageId(MSHOutMail.class,
                    mInMail.getRefToMessageId());
            if (momLst.isEmpty()) {
                String strMsg = String.format(
                        "No out mail (refId %s) for deliveryAdvice %s ", mInMail.
                                getMessageId(), mInMail.getRefToMessageId());
                throw new ZPPException(strMsg);
            }

            MSHOutMail mom = null;
            for (MSHOutMail mdn : momLst) {

                if (Objects.equals(ZPPConstants.S_ZPPB_SERVICE, mdn.getService())
                        && Objects.equals(
                                ZPPConstants.S_ZPP_ACTION_DELIVERY_NOTIFICATION, mdn.
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
                throw new ZPPException(strMsg);
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
                    Key key = mzppZPPUtils.getEncKeyFromOut(mom);
                    LOG.log("processInZPPAdviceoFDelivery - get key" + key);
                    Element elKey
                            = mscCrypto.encryptedKeyWithReceiverPublicKey(key, xc,
                                    mInMail.
                                            getSenderEBox(),
                                    mInMail.getConversationId());
                    LOG.log("processInZPPAdviceoFDelivery - get encrypted key" + elKey);
                    // got signal message:
                    SignalMessage signal = msg.getExchange().get(SignalMessage.class);
                    signal.getAnies().add(elKey);
                }

                mDB.setStatusToOutMail(mom, SEDOutboxMailStatus.DELIVERED,
                        "Received ZPP advice of delivery",
                        null, null, StorageUtils.getRelativePath(advOfDelivery),
                        MimeValue.MIME_XML.getMimeType());

            }

        } catch (NoSuchProviderException | SEDSecurityException | StorageException | IOException | CertificateException | NoSuchAlgorithmException | InvalidKeyException | SignatureException ex) {
            LOG.logError(l, ex);
            throw new ZPPException("Error processing AdviceOfDelivery", ex);
        }
        LOG.logEnd(l);
    }

    private File signPDFDocument(PrivateKey pk, X509Certificate xcert, File f,
            boolean replace) {
        long l = LOG.logStart();
        File ftmp = null;
        try {
            ftmp = StorageUtils.getNewStorageFile("pdf", "zpp-signed");

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
