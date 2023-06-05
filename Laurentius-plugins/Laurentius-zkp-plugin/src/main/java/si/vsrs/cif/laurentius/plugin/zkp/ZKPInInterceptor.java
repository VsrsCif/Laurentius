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
import org.apache.xml.security.encryption.EncryptedKey;
import org.apache.xml.security.keys.keyresolver.KeyResolverException;
import org.w3c.dom.Element;
import si.vsrs.cif.laurentius.plugin.zkp.enums.ZKPPartType;
import si.vsrs.cif.laurentius.plugin.zkp.exception.ZKPErrorCode;
import si.vsrs.cif.laurentius.plugin.zkp.exception.ZKPException;
import si.vsrs.cif.laurentius.plugin.zkp.utils.FOPUtils;
import si.vsrs.cif.laurentius.plugin.zkp.utils.ZKPUtils;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.cxf.SoapUtils;
import si.laurentius.commons.ebms.EBMSError;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.enums.SEDInboxMailStatus;
import si.laurentius.commons.enums.SEDOutboxMailStatus;
import si.laurentius.commons.exception.FOPException;
import si.laurentius.commons.exception.HashException;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.JMSManagerInterface;
import si.laurentius.commons.interfaces.SEDCertStoreInterface;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.pmode.EBMSMessageContext;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.Utils;
import si.laurentius.ebox.SEDBox;
import si.laurentius.lce.DigestUtils;
import si.laurentius.lce.enc.SEDCrypto;
import si.laurentius.lce.sign.pdf.SignatureInfo;
import si.laurentius.lce.sign.pdf.ValidateSignatureUtils;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.inbox.payload.IMPartProperty;
import si.laurentius.msh.inbox.payload.MSHInPart;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.laurentius.msh.outbox.payload.MSHOutPayload;
import si.laurentius.plugin.interceptor.MailInterceptorDef;
import si.laurentius.plugin.interfaces.SoapInterceptorInterface;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.JMSException;
import javax.naming.NamingException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
@Stateless
@Local(SoapInterceptorInterface.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class ZKPInInterceptor implements SoapInterceptorInterface {

  /**
   *
   */
  protected final SEDLogger LOG = new SEDLogger(ZKPInInterceptor.class);
  SEDCrypto mSedCrypto = new SEDCrypto();
  ZKPUtils mzkpZKPUtils = new ZKPUtils();

  FOPUtils mfpFop = null;

  @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
  SEDDaoInterface mDB;

  @EJB(mappedName = SEDJNDI.JNDI_JMSMANAGER)
  JMSManagerInterface mJMS;

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  SEDLookupsInterface msedLookup;

  @EJB(mappedName = SEDJNDI.JNDI_DBCERTSTORE)
  SEDCertStoreInterface mCertBean;





  @Override
  public MailInterceptorDef getDefinition() {
    MailInterceptorDef mid = new MailInterceptorDef();
    mid.setDescription("Sets ZKP in to locked status");
    mid.setName("ZKP in intercepror");
    mid.setType("ZKPInInterceptor");
    return mid;
  }

  /**
   *
   * @param msg
   */
  @Override
  public boolean handleMessage(SoapMessage msg, Properties contextProperties) {
    long l = LOG.logStart();

   // boolean isRequestor = MessageUtils.isRequestor(msg);y
    
    SEDBox sb = SoapUtils.getMSHInMailReceiverBox(msg);

    EBMSMessageContext eInctx = SoapUtils.getEBMSMessageInContext(msg);
    MSHOutMail moutMail = SoapUtils.getMSHOutMail(msg);
    MSHInMail mInMail = SoapUtils.getMSHInMail(msg);

    if (mInMail != null) {
      switch (mInMail.getService()) {
        case ZKPConstants.ZKP_A_SERVICE:
          processInZKPMessage(mInMail, eInctx, msg);
          break;
//        case ZKPConstants.ZKP_A_SERVICE:
//          processInZKPBMessage(mInMail, eInctx, msg);
//          break;
      }
    }
    
    Object sigAnies = SoapUtils.getInSignals(msg); 
    if (sigAnies != null) {
      LOG.log("Proccess in signal elments");
      try {
        processSignalMessages((List<Element>) sigAnies, moutMail, sb);
      } catch (ZKPException ex) {
        String strMsg = String.format(
              "Error occured while processing signal  %s ", ex.getMessage());
      LOG.logError(l, strMsg, ex);
      throw new EBMSError(ZKPErrorCode.InvalidDeliveryAdvice,
              mInMail!=null? mInMail.getMessageId(): (moutMail!=null?moutMail.getMessageId():""),
              strMsg, SoapFault.FAULT_CODE_SERVER);
      }
    }

    LOG.logEnd(l);
    return true;
  }
  
   public void processInZKPBMessage(MSHInMail inMail,
          EBMSMessageContext eInctx, SoapMessage msg) {
    switch (inMail.getAction()) {
      case ZKPConstants.ZKP_ACTION_DELIVERY_NOTIFICATION:
        processInZKPBDelivery(inMail, eInctx);
        break;
      case ZKPConstants.ZKP_ACTION_ADVICE_OF_DELIVERY:
        validateInZKPBDeliveryReciept(inMail, eInctx, msg);
        break;
    }
     
   }

  /**
   *  Process in delivery notification (lock message), Fiction notification width decryption key 
   * and S_ZKP_ACTION_ADVICE_OF_DELIVERY
   * @param inMail
   * @param eInctx
   * @param msg 
   */
  public void processInZKPMessage(MSHInMail inMail,
          EBMSMessageContext eInctx, SoapMessage msg) {

    switch (inMail.getAction()) {
      case ZKPConstants.ZKP_ACTION_DELIVERY_NOTIFICATION:
        processInZKPDelivery(inMail);
        break;
//      case ZKPConstants.ZKP_ACTION_ADVICE_OF_DELIVERY:
//        validateInZKPAdviceOfDelivery(inMail, eInctx, msg);
//        break;
    }
  }

  public void processSignalMessages(List<Element> signalElements,
          MSHOutMail moutMail, SEDBox sb) throws ZKPException {
    long l = LOG.logStart();
    if (signalElements != null) {

      Key k = null;
      for (Element e : signalElements) {
        if (e.getLocalName().equals(ZKPConstants.ELM_SIGNAL_ENCRYPTED_KEY)) {

          // get encrypte key
          EncryptedKey ek;
          try {
            ek = mSedCrypto.element2SimetricEncryptedKey(e);
          } catch (SEDSecurityException ex) {
            String msg = "Error occured while parsing ecrypted key for mail: " + moutMail.
                    getMessageId();
            LOG.logError(l, msg, ex);
            throw new ZKPException(msg, ex);
          }
          // resolve certificate
          X509Certificate xc;
          try {
            xc = ek.getKeyInfo().getX509Certificate();
          } catch (KeyResolverException ex) {
            LOG.logError(l, ex);
            String errmsg
                    = "Could not resolve Cert info from Encrypted key key DeliveryAdvice "
                    + moutMail.getId() + "Err:" + ex.getMessage();
            LOG.logError(l, errmsg, ex);
            try {
              mDB.setStatusToOutMail(moutMail, SEDOutboxMailStatus.ERROR,
                      errmsg, null,
                      ZKPConstants.ZKP_PLUGIN_TYPE);
            } catch (StorageException ex1) {
              LOG.logError(l,
                      "Error setting status ERROR to MSHInMail :'" + moutMail.
                              getId()
                      + "'!", ex1);
            }
            return;
          }

          Key pk = null;
          try {
            pk = mCertBean.getPrivateKeyForX509Cert(xc);
          } catch (SEDSecurityException ex) {
            String msg = "Error occured while accessing private key for cert: " + xc.
                    toString();
            LOG.logWarn(l, msg, ex);
            continue;
          }

          if (pk == null) {
            String msg = "No private key for cert: " + xc.toString();
            LOG.logWarn(l, msg, null);
            continue;
          }

          try {
            k = mSedCrypto.decryptEncryptedKey(e, pk,
                    SEDCrypto.SymEncAlgorithms.AES256_CBC);
            break;
          } catch (SEDSecurityException ex) {
            String msg = "Error occired while decrypting sym key with cert: " + xc.
                    toString();
            LOG.logError(l, msg, ex);
            throw new ZKPException(msg, ex);
          }

        }
      }
      if (moutMail != null && k != null) {
        decryptMail(k, moutMail.getConversationId(), sb);

      }
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
  public void validateInZKPAdviceOfDelivery(MSHInMail mInMail,
          EBMSMessageContext eInCtx, SoapMessage msg) {

    long l = LOG.logStart();

    List<MSHOutMail> momLst = mDB.getMailByMessageId(MSHOutMail.class,
            mInMail.getRefToMessageId());

    if (momLst.isEmpty()) {
      String strMsg = String.format(
              "No out mail (refId %s) for deliveryAdvice %s ", mInMail.
                      getRefToMessageId(), mInMail.getMessageId());
      LOG.logError(l, strMsg, null);
      throw new EBMSError(ZKPErrorCode.InvalidDeliveryAdvice, mInMail.
              getMessageId(),
              strMsg, SoapFault.FAULT_CODE_CLIENT);
    }

    MSHOutMail mom = null;
    for (MSHOutMail mdn : momLst) {
      if (Objects.equals(ZKPConstants.ZKP_PLUGIN_TYPE, mdn.getService())
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
      LOG.logError(l, strMsg, null);
      throw new EBMSError(ZKPErrorCode.InvalidDeliveryAdvice, mInMail.
              getMessageId(),
              strMsg, SoapFault.FAULT_CODE_CLIENT);
    }

    try {

      mom.setDeliveredDate(mInMail.getSentDate());

      String alias = eInCtx.getSenderPartyIdentitySet().
              getExchangePartySecurity().
              getSignatureCertAlias();
      LOG.formatedlog("Get sender cert alias '%s' to validate signature", alias);
      X509Certificate xcertSed = mCertBean.getX509CertForAlias(alias);

      // AdviceOfDelivery
      File advOfDelivery
              = StorageUtils.getFile(mInMail.getMSHInPayload().getMSHInParts().
                      get(0).getFilepath());

      ValidateSignatureUtils vsu = new ValidateSignatureUtils();
      
       List<SignatureInfo> lvc;
      try {
        lvc = vsu.validateSignatures(advOfDelivery);
        /*List<X509Certificate> lvc = vsu.getSignatureCerts(advOfDelivery.
        getAbsolutePath());*/
      } catch (SignatureException ex) {
        String strMsg = String.format(
            "Error occured while validating AdviceOfDelivery signatures: %s  for ref message: %s! Ex: %s!",
            mInMail.getMessageId(), mInMail.getRefToMessageId(), ex.getMessage());
      
        LOG.logError(l, strMsg, ex);
        throw new EBMSError(ZKPErrorCode.InvalidDeliveryAdvice, mInMail.getMessageId(),
                strMsg, SoapFault.FAULT_CODE_CLIENT);
      }

      // AdviceOfDelivery must have two signatures: recipient and 
      // delivery system
      if (lvc.size() != 2 && !(lvc.get(1).isSignerCertEquals(xcertSed) 
              || lvc.get(0).isSignerCertEquals(xcertSed))) {
        String strMsg = 
                "AdviceOfDelivery must hae two signatures: recipient's and "
                + " signature of recipient delivery system";
        
        LOG.logError(l, strMsg, null);
      throw new EBMSError(ZKPErrorCode.InvalidDeliveryAdvice, mInMail.
              getMessageId(),
              strMsg, SoapFault.FAULT_CODE_CLIENT);

      }
    } catch (NoSuchAlgorithmException | IOException | CertificateException | SEDSecurityException ex) {
      String strMsg = String.format(
              "Error occured while validating AdviceOfDelivery: %s  for ref message: %s! Ex: %s!",
              mInMail.getMessageId(), mInMail.getRefToMessageId(), ex.getMessage());
      
      LOG.logError(l, strMsg, ex);
      throw new EBMSError(ZKPErrorCode.InvalidDeliveryAdvice, mInMail.
              getMessageId(),
             strMsg, SoapFault.FAULT_CODE_CLIENT);
           
    }
    LOG.logEnd(l);
  }
  
  /**
   * 
   * @param mInMail
   * @param eInCtx
   * @param msg 
   */
  public void validateInZKPBDeliveryReciept(MSHInMail mInMail,
          EBMSMessageContext eInCtx, SoapMessage msg) {

    long l = LOG.logStart();

    List<MSHOutMail> momLst = mDB.getMailByMessageId(MSHOutMail.class,
            mInMail.getRefToMessageId());

    if (momLst.isEmpty()) {
      String strMsg = String.format(
              "No out mail (refId %s) for deliveryAdvice %s ", mInMail.
                      getRefToMessageId(), mInMail.getMessageId());
      LOG.logError(l, strMsg, null);
      throw new EBMSError(ZKPErrorCode.InvalidDeliveryAdvice, mInMail.
              getMessageId(),
              strMsg, SoapFault.FAULT_CODE_CLIENT);
    }

    MSHOutMail mom = null;
    for (MSHOutMail mdn : momLst) {
      if (Objects.equals(ZKPConstants.ZKP_PLUGIN_TYPE, mdn.getService())
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
      LOG.logError(l, strMsg, null);
      throw new EBMSError(ZKPErrorCode.InvalidDeliveryAdvice, mInMail.
              getMessageId(),
              strMsg, SoapFault.FAULT_CODE_CLIENT);
    }

    try {

      mom.setDeliveredDate(mInMail.getSentDate());

      String alias = eInCtx.getSenderPartyIdentitySet().
              getExchangePartySecurity().
              getSignatureCertAlias();
      LOG.formatedlog("Get sender cert alias '%s' to validate signature", alias);
      X509Certificate xcertSed = mCertBean.getX509CertForAlias(alias);

      // AdviceOfDelivery
      File advOfDelivery
              = StorageUtils.getFile(mInMail.getMSHInPayload().getMSHInParts().
                      get(0).getFilepath());

      ValidateSignatureUtils vsu = new ValidateSignatureUtils();
      List<X509Certificate> lvc = vsu.getSignatureCerts(advOfDelivery.
              getAbsolutePath());

      // AdviceOfDelivery must have one signatures: 
      // delivery system
      if (lvc.size() != 1 || !(lvc.get(0).equals(xcertSed))) {
        String strMsg = 
                "AdviceOfDelivery must have on signature which must match sender AP!";
        
        String certStr = String.format(" Expected subject: %s, got %s", xcertSed.getSubjectX500Principal().getName(), lvc.get(0).getSubjectX500Principal().getName());
        
        LOG.logError(l, strMsg + certStr, null);
      throw new EBMSError(ZKPErrorCode.InvalidDeliveryAdvice, mInMail.
              getMessageId(),
              strMsg, SoapFault.FAULT_CODE_CLIENT);

      }
    } catch (IOException | CertificateException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException | SignatureException | SEDSecurityException ex) {
      String strMsg = String.format(
              "Error occured while validating AdviceOfDelivery: %s  for ref message: %s! Ex: %s!",
              mInMail.getMessageId(), mInMail.getRefToMessageId(), ex.getMessage());
      
      LOG.logError(l, strMsg, ex);
      throw new EBMSError(ZKPErrorCode.InvalidDeliveryAdvice, mInMail.
              getMessageId(),
             strMsg, SoapFault.FAULT_CODE_CLIENT);
           
    }
    LOG.logEnd(l);
  }

  /**
   *
   * @param mInMail
   * @throws FOPException
   * @throws HashException
   */
  public void processInZKPDelivery(MSHInMail mInMail) {
    long l = LOG.logStart();
    mInMail.setStatus(SEDInboxMailStatus.PLOCKED.getValue());
    mInMail.setStatusDate(Calendar.getInstance().getTime());
    try { 
      mDB.serializeInMail(mInMail, ZKPConstants.ZKP_PLUGIN_TYPE);
    } catch (StorageException ex) {
      String msg = String.format(
              "Server error occured while receiving mail: %s, Error: %s." + mInMail.
                      getId(), ex.getMessage());
      LOG.logError(l, msg, ex);
      throw new EBMSError(ZKPErrorCode.ServerError, null,
              msg, SoapFault.FAULT_CODE_CLIENT);
    }

    LOG.logEnd(l);
  }

  public void processInZKPBDelivery(MSHInMail inMail, EBMSMessageContext eInCtx) {
    long l = LOG.logStart();

    inMail.setStatus(SEDInboxMailStatus.PLOCKED.getValue());
    inMail.setStatusDate(Calendar.getInstance().getTime());

    try {
      String signAlias = eInCtx.getReceiverPartyIdentitySet().
              getLocalPartySecurity().
              getSignatureKeyAlias();

      MSHOutMail mout = createZKPBDeliveryReciept(inMail, signAlias);
    
      
      mDB.serializeInOutMail(inMail, mout,ZKPConstants.ZKP_PLUGIN_TYPE, eInCtx.getPMode());
      
    } catch (StorageException ex) {
      LOG.logError(l, "Error setting status ERROR to MSHInMail :'" + inMail.
              getId() + "'!", ex);
    }

    LOG.logEnd(l);
  }

  public MSHOutMail createZKPBDeliveryReciept(MSHInMail inMail, String signAlias) {
    long l = LOG.logStart();
    // create delivery advice

    MSHOutMail mout = new MSHOutMail();
    mout.setMessageId(Utils.getUUIDWithLocalDomain());
    mout.setService(ZKPConstants.ZKP_A_SERVICE);
    mout.setAction(ZKPConstants.ZKP_ACTION_ADVICE_OF_DELIVERY);
    mout.setConversationId(inMail.getConversationId());
    mout.setSenderEBox(inMail.getReceiverEBox());
    mout.setSenderName(inMail.getReceiverName());
    mout.setRefToMessageId(inMail.getMessageId());
    mout.setReceiverEBox(inMail.getSenderEBox());
    mout.setReceiverName(inMail.getSenderName());
    mout.setSubject(ZKPConstants.ZKP_ACTION_ADVICE_OF_DELIVERY);
    // prepare mail to persist
    Date dt = new Date();
    // set current status
    mout.setStatus(SEDOutboxMailStatus.SUBMITTED.getValue());
    mout.setSubmittedDate(dt);
    mout.setStatusDate(dt);
    PrivateKey pk = null;
    X509Certificate xcert = null;
    try {
      // sign with receiver certificate 
      pk = mCertBean.getPrivateKeyForAlias(signAlias);
      xcert = mCertBean.getX509CertForAlias(signAlias);
    } catch (SEDSecurityException ex) {
      String msg = String.format(
              "Server error occured while preparing sign cert %s for ZKPBDeliveryReciept for mail: %d, Error: %s.",
              signAlias, inMail.getId(), ex.getMessage());
      LOG.logError(l, msg, ex);
      throw new EBMSError(ZKPErrorCode.ServerError, null,
              msg, SoapFault.FAULT_CODE_CLIENT);
    }

    MSHOutPart mp = mzkpZKPUtils.createSignedZKPBDeliveryReciept(inMail, pk,
            xcert);
    mout.setMSHOutPayload(new MSHOutPayload());
    mout.getMSHOutPayload().getMSHOutParts().add(mp);

    LOG.logEnd(l);
    return mout;
  }

  private EncryptedKey getEncryptedKeyFromInPart(MSHInPart mip, MSHInMail inMail) {

    File encKeyFile = StorageUtils.getFile(mip.getFilepath());
    try {
      return mSedCrypto.file2SimetricEncryptedKey(encKeyFile);
    } catch (SEDSecurityException ex) {
      String msg = String.format(
              "Server error occured while retrieving encKey from inMail : %s, Error: %s." + inMail.
                      getMessageId(), ex.getMessage());
      LOG.logError(msg, ex);
      throw new EBMSError(ZKPErrorCode.ServerError, null,
              msg, SoapFault.FAULT_CODE_CLIENT);
    }

  }

  public void processInZKPFictionNotification(MSHInMail inMail) {
    long l = LOG.logStart();
    EncryptedKey ek = null;
    for (MSHInPart mip : inMail.getMSHInPayload().getMSHInParts()) {
      if (Objects.equals(mip.getType(), ZKPPartType.EncryptedKey.getPartType())) {
        ek = getEncryptedKeyFromInPart(mip, inMail);
        break;
      }
    }
    assert ek != null : "Missing EncryptedKey in ZKPFictionNotification!";

    Key k = null;

    // resolve certificate
    X509Certificate xc;
    try {
      xc = ek.getKeyInfo().getX509Certificate();
    } catch (KeyResolverException ex) {
      String errmsg
              = "Could not resolve Cert info from Encrypted key  DeliveryAdvice "
              + inMail.getId() + "Err:" + ex.getMessage();
      LOG.logError(l, errmsg, ex);
      throw new EBMSError(ZKPErrorCode.ServerError, inMail.getMessageId(),
              errmsg, SoapFault.FAULT_CODE_CLIENT);
    }

    Key pk;
    try {
      pk = mCertBean.getPrivateKeyForX509Cert(xc);
    } catch (SEDSecurityException ex) {
      String errmsg
              = "Could not get private key for cert: " + xc.getSubjectDN()
              + ". Private key is needed to decrypt Encrypted key for "
              + inMail.getConversationId();
      LOG.logError(l, errmsg, ex);
      throw new EBMSError(ZKPErrorCode.ServerError, inMail.getMessageId(),
              errmsg, SoapFault.FAULT_CODE_CLIENT);
    }

    if (pk == null) {
      String errmsg
              = "Could not get private key for cert: " + xc.getSubjectDN()
              + ". Private key is needed to decrypt Encrypted key for "
              + inMail.getConversationId();
      LOG.logError(l, errmsg, null);
      throw new EBMSError(ZKPErrorCode.ServerError, inMail.getMessageId(),
              errmsg, SoapFault.FAULT_CODE_CLIENT);
    }

    try {
      k = mSedCrypto.decryptEncryptedKey(ek,
              pk, SEDCrypto.SymEncAlgorithms.AES128_CBC);
    } catch (SEDSecurityException ex) {
      String errmsg
              = "Could not decrypt sym key with a private key for cert: " + xc.
                      getSubjectDN()
              + ". Private key is needed to decrypt Encrypted key for "
              + inMail.getConversationId();
      LOG.logError(l, errmsg, ex);
      throw new EBMSError(ZKPErrorCode.ServerError, inMail.getMessageId(),
              errmsg, SoapFault.FAULT_CODE_CLIENT);
    }

    if (k != null) {
      decryptMail(k, inMail.getConversationId(), null);
    } else {
      String errmsg
              = "Null decrypt sym key for cert: " + xc.getSubjectDN()
              + ". Private key is needed to decrypt Encrypted key for "
              + inMail.getConversationId();
      LOG.logWarn(l, errmsg, null);
    }
    LOG.logEnd(l);
  }

  /**
   *
   * @return
   */
  public FOPUtils getFOP() {
    if (mfpFop == null) {
      File fconf
              = new File(System.getProperty(
                      SEDSystemProperties.SYS_PROP_HOME_DIR) + File.separator
                      + ZKPConstants.SVEV_FOLDER + File.separator + ZKPConstants.FOP_CONFIG_FILENAME);

      mfpFop
              = new FOPUtils(fconf, System.getProperty(
                      SEDSystemProperties.SYS_PROP_HOME_DIR)
                      + File.separator + ZKPConstants.SVEV_FOLDER + File.separator
                      + ZKPConstants.XSLT_FOLDER);
    }
    return mfpFop;
  }



  /**
   *
   * @param key
   * @param convID
   * @param sb
   */
  public void decryptMail(Key key, String convID, SEDBox sb) {
    long l = LOG.logStart();

    try {

      List<MSHInMail> lst
              = mDB.getInMailConvIdAndAction(
                      ZKPConstants.ZKP_ACTION_DELIVERY_NOTIFICATION, convID);
      if (lst.isEmpty()) {
        String errMsg
                = "Mail with convid: " + convID + " and action: "
                + ZKPConstants.ZKP_ACTION_DELIVERY_NOTIFICATION + " not found!"
                + "Nothing to decrypt!";
        LOG.logError(l, errMsg, null);
      }
      for (MSHInMail mi : lst) {

        if (sb == null) {
          sb = msedLookup.getSEDBoxByAddressName(mi.getReceiverEBox());
        }

        List<MSHInPart> lstDec = new ArrayList<>();

        for (MSHInPart mip : mi.getMSHInPayload().getMSHInParts()) {
          String oldFileName = mip.getFilename();
          if (mip.getIsEncrypted()) {

            String newFileName
                    = oldFileName.substring(0, oldFileName.lastIndexOf(
                            ZKPConstants.ZKP_ENC_SUFFIX));
            String mime = null;
            for (IMPartProperty mp : mip.getIMPartProperties()) {
              if (Objects.equals(mp.getName(),
                      ZKPConstants.PART_PROPERTY_ORIGIN_MIMETYPE)) {
                mime = mp.getValue();
                break;
              }
            }
            if (Utils.isEmptyString(mime)) {
              mime = mip.getMimeType();
            }

            File fNew;
            try (FileInputStream fis = new FileInputStream(StorageUtils.getFile(
                    mip.getFilepath()));
                    FileOutputStream bos
                    = new FileOutputStream(fNew = StorageUtils.
                            getNewStorageFile(MimeValue.
                                    getSuffixBYMimeType(mime), "zkp-dec"))) {

              LOG.log("Decrypt file: " + newFileName);

              mSedCrypto.decryptStream(fis, bos, key);

              MSHInPart miDec = new MSHInPart();
              String desc = mip.getDescription();
              if (desc != null && desc.startsWith(
                      ZKPConstants.MSG_DOC_PREFIX_DESC)) {
                desc = desc.substring(ZKPConstants.MSG_DOC_PREFIX_DESC.length());
              }
              miDec.setIsSent(Boolean.FALSE);
              miDec.setIsReceived(Boolean.FALSE);
              miDec.setGeneratedFromPartId(mip.getId());

              miDec.setSource(ZKPConstants.ZKP_PLUGIN_TYPE);
              miDec.setDescription(desc);
              miDec.setEbmsId(mip.getEbmsId() + "-dec");
              miDec.setEncoding(mip.getEncoding());
              miDec.setFilename(newFileName);
              miDec.setMimeType(mime);
              miDec.setName(mip.getName());
              miDec.setType(mip.getType());
              miDec.setIsEncrypted(Boolean.FALSE);

              miDec.setSha256Value(DigestUtils.getBase64Sha256Digest(fNew));
              miDec.setSize(BigInteger.valueOf(fNew.length()));

              miDec.setFilepath(StorageUtils.getRelativePath(fNew));
              lstDec.add(miDec);
            } catch (IOException | StorageException | SEDSecurityException ex) {
              LOG.logError(l,
                      "Error occured while decrypting  file: '" + oldFileName
                      + "' for inmail:" + mi.getId(), ex);
            }
          }
        }

        try {
          mDB.addInMailPayload(mi, lstDec, SEDInboxMailStatus.RECEIVED,
                  "Received secred key and decrypt payloads",
                  null, ZKPConstants.ZKP_PLUGIN_TYPE);

        } catch (StorageException ex) {
          LOG.logError(l, "Error updating mail :'" + mi.getId() + "'!", ex);
        }
        try {
          LOG.formatedlog("EXPORT MAIL %d", mi.getId().longValue());
          mJMS.exportInMail(mi.getId().longValue());
        } catch (NamingException | JMSException ex) {
          LOG.logError(l,
                  "Error occured while submitting mail to export queue:'" + mi.
                          getId() + "'!",
                  ex);
        }
        /*
        if (sb.getExport() != null && sb.getExport().getActive() != null &&
            sb.getExport().getActive()) {
          try {
            mJMS.exportInMail(mi.getId().longValue());
          } catch (NamingException | JMSException ex) {
            LOG.logError(l, "Error occured while submitting mail to export queue:'" + mi.getId() +
                "'!", ex);
          }
        }*/

      }
    } finally {

    }
    LOG.logEnd(l);
  }

  /**
   *
   * @param t
   */
  @Override
  public void handleFault(SoapMessage t, Properties contextProperties) {
    // ignore
  }

}
