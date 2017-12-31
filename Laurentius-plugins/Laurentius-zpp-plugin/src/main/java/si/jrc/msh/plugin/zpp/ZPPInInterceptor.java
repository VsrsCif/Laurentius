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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.UserTransaction;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.message.MessageUtils;
import org.apache.xml.security.encryption.EncryptedKey;
import org.apache.xml.security.keys.keyresolver.KeyResolverException;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.inbox.payload.MSHInPart;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.ebox.SEDBox;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import si.jrc.msh.plugin.zpp.doc.DocumentSodBuilder;
import si.jrc.msh.plugin.zpp.exception.ZPPErrorCode;
import si.jrc.msh.plugin.zpp.exception.ZPPException;
import si.jrc.msh.plugin.zpp.utils.FOPUtils;
import si.jrc.msh.plugin.zpp.utils.ZPPUtils;
import si.laurentius.lce.enc.SEDCrypto;
import si.laurentius.lce.enc.SEDKey;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.enums.SEDInboxMailStatus;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.enums.SEDOutboxMailStatus;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.cxf.SoapUtils;
import si.laurentius.commons.ebms.EBMSError;
import si.laurentius.commons.exception.FOPException;
import si.laurentius.commons.exception.HashException;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.JMSManagerInterface;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.StringFormater;

import si.laurentius.commons.utils.xml.XMLUtils;
import si.laurentius.lce.KeystoreUtils;
import si.laurentius.commons.interfaces.SEDCertStoreInterface;
import si.laurentius.commons.pmode.EBMSMessageContext;
import si.laurentius.commons.utils.Utils;
import si.laurentius.lce.DigestUtils;
import si.laurentius.lce.sign.pdf.ValidateSignatureUtils;
import si.laurentius.msh.inbox.payload.IMPartProperty;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.laurentius.msh.outbox.payload.MSHOutPayload;
import si.laurentius.plugin.interceptor.MailInterceptorDef;
import si.laurentius.plugin.interfaces.SoapInterceptorInterface;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
@Stateless
@Local(SoapInterceptorInterface.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class ZPPInInterceptor implements SoapInterceptorInterface {

  /**
   *
   */
  protected final SEDLogger LOG = new SEDLogger(ZPPInInterceptor.class);
  SEDCrypto mSedCrypto = new SEDCrypto();
  DocumentSodBuilder dsbSodBuilder = new DocumentSodBuilder();
  KeystoreUtils mkeyUtils = new KeystoreUtils();
  StorageUtils msuStorage = new StorageUtils();
  ZPPUtils mzppZPPUtils = new ZPPUtils();

  FOPUtils mfpFop = null;
  StringFormater msfFormat = new StringFormater();

  @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
  SEDDaoInterface mDB;

  @EJB(mappedName = SEDJNDI.JNDI_JMSMANAGER)
  JMSManagerInterface mJMS;

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  SEDLookupsInterface msedLookup;

  @EJB(mappedName = SEDJNDI.JNDI_DBCERTSTORE)
  SEDCertStoreInterface mCertBean;

  /**
   *
   */
  @Resource
  public UserTransaction mutUTransaction;

  /**
   *
   */
  @PersistenceContext(unitName = "ebMS_ZPP_PU", name = "ebMS_ZPP_PU")
  public EntityManager memEManager;

  @Override
  public MailInterceptorDef getDefinition() {
    MailInterceptorDef mid = new MailInterceptorDef();
    mid.setDescription("Sets ZPP in to locked status");
    mid.setName("ZPP in intercepror");
    mid.setType("ZPPInInterceptor");
    return mid;
  }

  /**
   *
   * @param msg
   */
  @Override
  public boolean handleMessage(SoapMessage msg, Properties contextProperties) {
    long l = LOG.logStart();

   // boolean isRequestor = MessageUtils.isRequestor(msg);
    
    SEDBox sb = SoapUtils.getMSHInMailReceiverBox(msg);

    EBMSMessageContext eInctx = SoapUtils.getEBMSMessageInContext(msg);
    MSHOutMail moutMail = SoapUtils.getMSHOutMail(msg);
    MSHInMail mInMail = SoapUtils.getMSHInMail(msg);

       
    if (mInMail != null) {
      switch (mInMail.getService()) {
        case ZPPConstants.S_ZPP_SERVICE:
          processInZPPMessage(mInMail, eInctx, msg);
          break;
        case ZPPConstants.S_ZPPB_SERVICE:
          processInZPPBMessage(mInMail, eInctx, msg);
          break;
      }
    }
    
    Object sigAnies = SoapUtils.getInSignals(msg); 
    if (sigAnies != null) {
      LOG.log("Proccess in signal elments");
      try {
        
        
        processSignalMessages((List<Element>) sigAnies, moutMail, sb);
      } catch (ZPPException ex) {
        String strMsg = String.format(
              "Error occured while processing signal  %s ", ex.getMessage());
      LOG.logError(l, strMsg, ex);
      throw new EBMSError(ZPPErrorCode.InvalidDeliveryAdvice,
              mInMail!=null? mInMail.getMessageId(): (moutMail!=null?moutMail.getMessageId():""),
              strMsg, SoapFault.FAULT_CODE_SERVER);
      }
    }

    LOG.logEnd(l);
    return true;
  }
  
   public void processInZPPBMessage(MSHInMail inMail,
          EBMSMessageContext eInctx, SoapMessage msg) {
    switch (inMail.getAction()) {
      case ZPPConstants.S_ZPP_ACTION_DELIVERY_NOTIFICATION:
        processInZPPBDelivery(inMail, eInctx);
        break;
      case ZPPConstants.S_ZPP_ACTION_ADVICE_OF_DELIVERY:
        validateInZPPBDeliveryReciept(inMail, eInctx, msg);
        break;
    }
     
   }

  /**
   *  Process in delivery notification (lock message), Fiction notification width decryption key 
   * and S_ZPP_ACTION_ADVICE_OF_DELIVERY
   * @param inMail
   * @param eInctx
   * @param msg 
   */
  public void processInZPPMessage(MSHInMail inMail,
          EBMSMessageContext eInctx, SoapMessage msg) {

    switch (inMail.getAction()) {
      case ZPPConstants.S_ZPP_ACTION_DELIVERY_NOTIFICATION:
        processInZPPDelivery(inMail);
        break;
      case ZPPConstants.S_ZPP_ACTION_FICTION_NOTIFICATION:
        processInZPPFictionNotification(inMail);
        break;
      case ZPPConstants.S_ZPP_ACTION_ADVICE_OF_DELIVERY:
        validateInZPPAdviceOfDelivery(inMail, eInctx, msg);
        break;
    }

   


  }

  public void processSignalMessages(List<Element> signalElements,
          MSHOutMail moutMail, SEDBox sb) throws ZPPException {
    long l = LOG.logStart();
    if (signalElements != null) {

      Key k = null;
      for (Element e : signalElements) {
        if (e.getLocalName().equals(ZPPConstants.ELM_SIGNAL_ENCRYPTED_KEY)) {

          // get encrypte key
          EncryptedKey ek;
          try {
            ek = mSedCrypto.element2SimetricEncryptedKey(e);
          } catch (SEDSecurityException ex) {
            String msg = "Error occured while parsing ecrypted key for mail: " + moutMail.
                    getMessageId();
            LOG.logError(l, msg, ex);
            throw new ZPPException(msg, ex);
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
                      ZPPConstants.S_ZPP_PLUGIN_TYPE);
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
            LOG.logWarn(l, msg, null);
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
            throw new ZPPException(msg, ex);
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
   * @throws si.jrc.msh.plugin.zpp.exception.ZPPException
   */
  public void validateInZPPAdviceOfDelivery(MSHInMail mInMail,
          EBMSMessageContext eInCtx, SoapMessage msg) {

    long l = LOG.logStart();

    List<MSHOutMail> momLst = mDB.getMailByMessageId(MSHOutMail.class,
            mInMail.getRefToMessageId());

    if (momLst.isEmpty()) {
      String strMsg = String.format(
              "No out mail (refId %s) for deliveryAdvice %s ", mInMail.
                      getRefToMessageId(), mInMail.getMessageId());
      LOG.logError(l, strMsg, null);
      throw new EBMSError(ZPPErrorCode.InvalidDeliveryAdvice, mInMail.
              getMessageId(),
              strMsg, SoapFault.FAULT_CODE_CLIENT);
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
      LOG.logError(l, strMsg, null);
      throw new EBMSError(ZPPErrorCode.InvalidDeliveryAdvice, mInMail.
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

      // AdviceOfDelivery must have two signatures: recipient and 
      // delivery system
      if (lvc.size() != 2 || !(lvc.get(1).equals(xcertSed) || lvc.get(0).equals(
              xcertSed))) {
        String strMsg = 
                "AdviceOfDelivery must have two signatures: recipient's and "
                + " signature of recipient delivery system";
        
        LOG.logError(l, strMsg, null);
      throw new EBMSError(ZPPErrorCode.InvalidDeliveryAdvice, mInMail.
              getMessageId(),
              strMsg, SoapFault.FAULT_CODE_CLIENT);

      }
    } catch (IOException | CertificateException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException | SignatureException | SEDSecurityException ex) {
      String strMsg = String.format(
              "Error occured while validating AdviceOfDelivery: %s  for ref message: %s! Ex: %s!",
              mInMail.getMessageId(), mInMail.getRefToMessageId(), ex.getMessage());
      
      LOG.logError(l, strMsg, ex);
      throw new EBMSError(ZPPErrorCode.InvalidDeliveryAdvice, mInMail.
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
  public void validateInZPPBDeliveryReciept(MSHInMail mInMail,
          EBMSMessageContext eInCtx, SoapMessage msg) {

    long l = LOG.logStart();

    List<MSHOutMail> momLst = mDB.getMailByMessageId(MSHOutMail.class,
            mInMail.getRefToMessageId());

    if (momLst.isEmpty()) {
      String strMsg = String.format(
              "No out mail (refId %s) for deliveryAdvice %s ", mInMail.
                      getRefToMessageId(), mInMail.getMessageId());
      LOG.logError(l, strMsg, null);
      throw new EBMSError(ZPPErrorCode.InvalidDeliveryAdvice, mInMail.
              getMessageId(),
              strMsg, SoapFault.FAULT_CODE_CLIENT);
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
      LOG.logError(l, strMsg, null);
      throw new EBMSError(ZPPErrorCode.InvalidDeliveryAdvice, mInMail.
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
                "AdviceOfDelivery must have two signatures: recipient's and "
                + " signature of recipient delivery system";
        
        LOG.logError(l, strMsg, null);
      throw new EBMSError(ZPPErrorCode.InvalidDeliveryAdvice, mInMail.
              getMessageId(),
              strMsg, SoapFault.FAULT_CODE_CLIENT);

      }
    } catch (IOException | CertificateException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException | SignatureException | SEDSecurityException ex) {
      String strMsg = String.format(
              "Error occured while validating AdviceOfDelivery: %s  for ref message: %s! Ex: %s!",
              mInMail.getMessageId(), mInMail.getRefToMessageId(), ex.getMessage());
      
      LOG.logError(l, strMsg, ex);
      throw new EBMSError(ZPPErrorCode.InvalidDeliveryAdvice, mInMail.
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
  public void processInZPPDelivery(MSHInMail mInMail) {
    long l = LOG.logStart();
    mInMail.setStatus(SEDInboxMailStatus.PLOCKED.getValue());
    mInMail.setStatusDate(Calendar.getInstance().getTime());
    try {
      mDB.serializeInMail(mInMail, ZPPConstants.S_ZPP_PLUGIN_TYPE);
    } catch (StorageException ex) {
      String msg = String.format(
              "Server error occured while receiving mail: %s, Error: %s." + mInMail.
                      getId(), ex.getMessage());
      LOG.logError(l, msg, ex);
      throw new EBMSError(ZPPErrorCode.ServerError, null,
              msg, SoapFault.FAULT_CODE_CLIENT);
    }

    LOG.logEnd(l);
  }

  public void processInZPPBDelivery(MSHInMail mInMail, EBMSMessageContext eInCtx) {
    long l = LOG.logStart();

    mInMail.setStatus(SEDInboxMailStatus.PLOCKED.getValue());
    mInMail.setStatusDate(Calendar.getInstance().getTime());

    try {
      String signAlias = eInCtx.getReceiverPartyIdentitySet().
              getLocalPartySecurity().
              getSignatureKeyAlias();

      MSHOutMail mout = createZPPBDeliveryReciept(mInMail, signAlias);

      mDB.serializeInMail(mInMail, "ZPP plugin");

      // create out mail
      mDB.serializeOutMail(mout, "", ZPPConstants.S_ZPP_PLUGIN_TYPE, "");
      //mDB.setStatusToInMail(inMail, SEDInboxMailStatus.PREADY,
      //        "AdviceOfDelivery created and submitted to out queue");

    } catch (StorageException ex) {
      LOG.logError(l, "Error setting status ERROR to MSHInMail :'" + mInMail.
              getId() + "'!", ex);
    }

    LOG.logEnd(l);
  }

  public MSHOutMail createZPPBDeliveryReciept(MSHInMail inMail, String signAlias) {
    long l = LOG.logStart();
    // create delivery advice

    MSHOutMail mout = new MSHOutMail();
    mout.setMessageId(Utils.getUUIDWithLocalDomain());
    mout.setService(ZPPConstants.S_ZPPB_SERVICE);
    mout.setAction(ZPPConstants.S_ZPP_ACTION_ADVICE_OF_DELIVERY);
    mout.setConversationId(inMail.getConversationId());
    mout.setSenderEBox(inMail.getReceiverEBox());
    mout.setSenderName(inMail.getReceiverName());
    mout.setRefToMessageId(inMail.getMessageId());
    mout.setReceiverEBox(inMail.getSenderEBox());
    mout.setReceiverName(inMail.getSenderName());
    mout.setSubject(ZPPConstants.S_ZPP_ACTION_ADVICE_OF_DELIVERY);
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
              "Server error occured while preparing sign cert %s for ZPPBDeliveryReciept for mail: %d, Error: %s.",
              signAlias, inMail.getId(), ex.getMessage());
      LOG.logError(l, msg, ex);
      throw new EBMSError(ZPPErrorCode.ServerError, null,
              msg, SoapFault.FAULT_CODE_CLIENT);
    }

    MSHOutPart mp = mzppZPPUtils.createSignedZPPBDeliveryReciept(inMail, pk,
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
      throw new EBMSError(ZPPErrorCode.ServerError, null,
              msg, SoapFault.FAULT_CODE_CLIENT);
    }

  }

  public void processInZPPFictionNotification(MSHInMail inMail) {
    long l = LOG.logStart();
    EncryptedKey ek = null;
    for (MSHInPart mip : inMail.getMSHInPayload().getMSHInParts()) {
      if (Objects.equals(mip.getType(), ZPPConstants.ELM_SIGNAL_ENCRYPTED_KEY)) {
        ek = getEncryptedKeyFromInPart(mip, inMail);
        break;
      }
    }
    assert ek != null : "Missing EncryptedKey in ZPPFictionNotification!";

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
      throw new EBMSError(ZPPErrorCode.ServerError, inMail.getMessageId(),
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
      throw new EBMSError(ZPPErrorCode.ServerError, inMail.getMessageId(),
              errmsg, SoapFault.FAULT_CODE_CLIENT);
    }

    if (pk == null) {
      String errmsg
              = "Could not get private key for cert: " + xc.getSubjectDN()
              + ". Private key is needed to decrypt Encrypted key for "
              + inMail.getConversationId();
      LOG.logError(l, errmsg, null);
      throw new EBMSError(ZPPErrorCode.ServerError, inMail.getMessageId(),
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
      throw new EBMSError(ZPPErrorCode.ServerError, inMail.getMessageId(),
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
                      + ZPPConstants.SVEV_FOLDER + File.separator + ZPPConstants.FOP_CONFIG_FILENAME);

      mfpFop
              = new FOPUtils(fconf, System.getProperty(
                      SEDSystemProperties.SYS_PROP_HOME_DIR)
                      + File.separator + ZPPConstants.SVEV_FOLDER + File.separator
                      + ZPPConstants.XSLT_FOLDER);
    }
    return mfpFop;
  }

  /**
   *
   * @param mailId
   * @return
   * @throws IOException
   */
  public Key getEncryptionKeyForDeliveryAdvice(BigInteger mailId)
          throws IOException {
    TypedQuery<SEDKey> q
            = memEManager.createNamedQuery("si.jrc.msh.sec.SEDKey.getById",
                    SEDKey.class);
    q.setParameter("id", mailId);
    return q.getSingleResult();

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
                      ZPPConstants.S_ZPP_ACTION_DELIVERY_NOTIFICATION, convID);
      if (lst.isEmpty()) {
        String errMsg
                = "Mail with convid: " + convID + " and action: "
                + ZPPConstants.S_ZPP_ACTION_DELIVERY_NOTIFICATION + " not found!"
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
                            ZPPConstants.S_ZPP_ENC_SUFFIX));
            String mime = null;
            for (IMPartProperty mp : mip.getIMPartProperties()) {
              if (Objects.equals(mp.getName(),
                      ZPPConstants.S_PART_PROPERTY_ORIGIN_MIMETYPE)) {
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
                                    getSuffixBYMimeType(mime), "zpp-dec"))) {

              LOG.log("Decrypt file: " + newFileName);

              mSedCrypto.decryptStream(fis, bos, key);

              MSHInPart miDec = new MSHInPart();
              String desc = mip.getDescription();
              if (desc != null && desc.startsWith(
                      ZPPConstants.MSG_DOC_PREFIX_DESC)) {
                desc = desc.substring(ZPPConstants.MSG_DOC_PREFIX_DESC.length());
              }
              miDec.setIsSent(Boolean.FALSE);
              miDec.setIsReceived(Boolean.FALSE);
              miDec.setGeneratedFromPartId(mip.getId());

              miDec.setSource(ZPPConstants.S_ZPP_PLUGIN_TYPE);
              miDec.setDescription(desc);
              miDec.setEbmsId(mip.getEbmsId() + "-dec");
              miDec.setEncoding(mip.getEncoding());
              miDec.setFilename(newFileName);
              miDec.setMimeType(mime);
              miDec.setName(mip.getName());
              miDec.setType(mip.getType());
              miDec.setIsEncrypted(Boolean.FALSE);

              miDec.setSha256Value(DigestUtils.getHexSha256Digest(fNew));
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
                  null, ZPPConstants.S_ZPP_PLUGIN_TYPE);

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
