/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.test.zkp;

import org.apache.log4j.Logger;
import org.apache.xmlgraphics.util.MimeConstants;
import si.laurentius.inbox.mail.InMail;
import si.laurentius.outbox.mail.OutMail;
import si.laurentius.outbox.payload.OutPart;
import si.laurentius.outbox.payload.OutPayload;
import si.laurentius.test.zkp.fop.FOPException;
import si.laurentius.test.zkp.fop.FOPUtils;
import si.laurentius.test.zkp.sec.SignUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.UUID;

/**
 * @author sluzba
 */
public class ZKPUtils {

    public static final String S_SETTINGS_FOLDER = "src/main/resources/";

    FOPUtils mfpFop = null;
    public static final Logger LOG = Logger.getLogger(ZKPUtils.class);

    public OutMail createZkpAdviceOfDelivery(InMail mInMail, String keystore, String keystorepassword, String signAlias, String keypassword, final String sZkpAService) throws FOPException, ZKPException {
        OutMail mout = new OutMail();
        mout.setSenderMessageId(UUID.randomUUID().toString()); // client  message id );
        mout.setService(sZkpAService);
        mout.setAction(ZKPDeliveryConstants.S_ZKP_ACTION_ADVICE_OF_DELIVERY);
        mout.setSubject(ZKPDeliveryConstants.S_ZKP_ACTION_ADVICE_OF_DELIVERY);
        setDataFromReferenceInMail(mout, mInMail);

        File fDNViz = null;
        try {
            fDNViz = File.createTempFile("AdviceOfDelivery", ".pdf");

            getFOP().generateVisualization(mInMail, fDNViz,
                    FOPUtils.FopTransformations.AdviceOfDelivery,
                    MimeConstants.MIME_PDF);

            KeyStore keyStore;
            X509Certificate xcert;
            PrivateKey pk;
            try {
                keyStore = KeyStore.getInstance("JKS");
            } catch (KeyStoreException ex) {
                throw new ZKPException(ex);
            }
            try {
                keyStore.load(SignUtils.class.getResourceAsStream(keystore), keystorepassword.toCharArray());
            } catch (NoSuchAlgorithmException | CertificateException ex) {
                throw new ZKPException(ex);
            }
            try {
                xcert = (X509Certificate) keyStore.getCertificate(signAlias);
            } catch (KeyStoreException ex) {
                throw new ZKPException(ex);
            }

            try {
                pk = (PrivateKey) keyStore.getKey(signAlias,
                        keypassword.toCharArray());
            } catch (NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException ex) {
                throw new ZKPException(ex);
            }

            signPDFDocument(pk, xcert, fDNViz);
            // sign with systemCertificate

            mout.setOutPayload(new OutPayload());
            OutPart mp = new OutPart();
            mp.setDescription(ZKPDeliveryConstants.S_ZKP_ACTION_ADVICE_OF_DELIVERY);
            mp.setMimeType(MimeConstants.MIME_PDF);
            mout.getOutPayload().getOutParts().add(mp);

            mp.setBin(Files.readAllBytes(fDNViz.toPath()));
            mp.setFilename(fDNViz.getName());

            mp.setName(mp.getFilename().
                    substring(0, mp.getFilename().lastIndexOf(".")));

        } catch (IOException ex) {
            throw new ZKPException(ex);
        }

        return mout;
    }

    public OutMail createZkpBNonDeliveryNotification(InMail referenceInMail, String keystore, String keystorepassword, String signAlias, String keypassword) throws FOPException, ZKPException {
        OutMail nonDeliveryNotification = new OutMail();
        nonDeliveryNotification.setSenderMessageId(UUID.randomUUID().toString()); // client  message id );
        nonDeliveryNotification.setService(ZKPDeliveryConstants.S_ZKP_B_SERVICE);
        nonDeliveryNotification.setAction(ZKPDeliveryConstants.S_ZKP_ACTION_NOT_DELIVERED_NOTIFICATION);
        nonDeliveryNotification.setSubject(ZKPDeliveryConstants.S_ZKP_ACTION_NOT_DELIVERED_NOTIFICATION);

        setDataFromReferenceInMail(nonDeliveryNotification, referenceInMail);

        try {
            File attachmentFile = File.createTempFile("NotDeliveredNotification", ".pdf");

            getFOP().generateVisualization(referenceInMail, attachmentFile,
                    FOPUtils.FopTransformations.NotDeliveredNotification,
                    MimeConstants.MIME_PDF);

            try {
                KeyStore keyStore = KeyStore.getInstance("JKS");
                keyStore.load(SignUtils.class.getResourceAsStream(keystore), keystorepassword.toCharArray());
                X509Certificate xcert = (X509Certificate) keyStore.getCertificate(signAlias);
                PrivateKey pk = (PrivateKey) keyStore.getKey(signAlias, keypassword.toCharArray());
                signPDFDocument(pk, xcert, attachmentFile);
            } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException |
                     UnrecoverableKeyException ex) {
                throw new ZKPException(ex);
            }

            // sign with systemCertificate

            nonDeliveryNotification.setOutPayload(new OutPayload());
            OutPart mp = new OutPart();
            mp.setDescription(ZKPDeliveryConstants.S_ZKP_ACTION_NOT_DELIVERED_NOTIFICATION);
            mp.setMimeType(MimeConstants.MIME_PDF);
            nonDeliveryNotification.getOutPayload().getOutParts().add(mp);

            mp.setBin(Files.readAllBytes(attachmentFile.toPath()));
            mp.setFilename(attachmentFile.getName());

            mp.setName(mp.getFilename().
                    substring(0, mp.getFilename().lastIndexOf(".")));

        } catch (IOException ex) {
            throw new ZKPException(ex);
        }

        return nonDeliveryNotification;
    }

    private void setDataFromReferenceInMail(OutMail outMail, InMail inMail) {
        outMail.setConversationId(inMail.getConversationId());
        outMail.setSenderEBox(inMail.getReceiverEBox());
        outMail.setSenderName(inMail.getReceiverName());
        outMail.setRefToMessageId(inMail.getMessageId());
        outMail.setReceiverEBox(inMail.getSenderEBox());
        outMail.setReceiverName(inMail.getSenderName());
    }

    private void signPDFDocument(PrivateKey pk, X509Certificate xcert, File f) {
        try {
            File ftmp = File.createTempFile("tmp_sign", ".pdf");

            SignUtils su = new SignUtils(pk, xcert);
            su.signPDF(f, ftmp, true);
            Files.move(ftmp.toPath(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException ex) {

        }
    }

    public FOPUtils getFOP() {
        if (mfpFop == null) {

            mfpFop = new FOPUtils(new File(S_SETTINGS_FOLDER + ZKPDeliveryConstants.FOP_CONFIG_FILENAME),
                    S_SETTINGS_FOLDER + ZKPDeliveryConstants.XSLT_FOLDER);
        }
        return mfpFop;
    }

}
