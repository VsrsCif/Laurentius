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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
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
import javax.xml.transform.TransformerException;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.message.MessageUtils;
import org.apache.xml.security.encryption.EncryptedKey;
import org.apache.xml.security.keys.keyresolver.KeyResolverException;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.inbox.payload.MSHInPart;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.SignalMessage;
import si.laurentius.ebox.SEDBox;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import si.jrc.msh.plugin.zpp.doc.DocumentSodBuilder;
import si.jrc.msh.plugin.zpp.exception.ZPPException;
import si.jrc.msh.plugin.zpp.utils.FOPUtils;
import si.laurentius.lce.enc.SEDCrypto;
import si.laurentius.lce.enc.SEDKey;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.enums.SEDInboxMailStatus;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.enums.SEDOutboxMailStatus;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.cxf.SoapUtils;
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
  public boolean handleMessage(SoapMessage msg) {
    long l = LOG.logStart();

    boolean isRequest = MessageUtils.isRequestor(msg);
    QName sv = (isRequest ? SoapFault.FAULT_CODE_CLIENT : SoapFault.FAULT_CODE_SERVER);

    SEDBox sb = SoapUtils.getMSHInMailReceiverBox(msg);

    EBMSMessageContext eInctx = SoapUtils.getEBMSMessageInContext(msg);
    MSHOutMail moutMail = SoapUtils.getMSHOutMail(msg);
    MSHInMail mInMail = SoapUtils.getMSHInMail(msg);

    Object sigAnies = SoapUtils.getInSignals(msg);

    if (mInMail != null && ZPPConstants.S_ZPP_SERVICE.equals(mInMail.
            getService())
            && ZPPConstants.S_ZPP_ACTION_DELIVERY_NOTIFICATION.equals(mInMail.
                    getAction())) {
      try {
        processInZPPDelivery(mInMail);
      } catch (FOPException | HashException ex) {
         LOG.logError(l, ex.getMessage(), ex);
        throw new SoapFault(ex.getMessage(), sv);
      }
    }

    if (mInMail != null && ZPPConstants.S_ZPP_SERVICE.equals(mInMail.
            getService())
            && ZPPConstants.S_ZPP_ACTION_FICTION_NOTIFICATION.equals(mInMail.
                    getAction())) {
      try {
        processInZPPFictionNotification(mInMail);
      } catch (FOPException | HashException | SEDSecurityException | IOException | ParserConfigurationException | SAXException | JAXBException ex) {
         LOG.logError(l, ex.getMessage(), ex);
        throw new SoapFault(ex.getMessage(), sv);
      }
    }

    // validate in DeliveryOfAdvice
    if (mInMail != null && ZPPConstants.S_ZPP_SERVICE.equals(mInMail.
            getService())
            && ZPPConstants.S_ZPP_ACTION_ADVICE_OF_DELIVERY.equals(mInMail.
                    getAction())) {
      try {
        validateInZPPAdviceOfDelivery(mInMail, eInctx, msg);
      } catch (ZPPException exzpp) {
        LOG.logError(l, exzpp.getMessage(), exzpp);
        throw new SoapFault(exzpp.getMessage(), sv);
      }

    }

    if (sigAnies != null) {
      LOG.log("Proccess in signal elments");
      try {
        processSignalMessages((List<Element>) sigAnies, moutMail, sb);
      } catch (ZPPException ex) {
        LOG.logError(l, ex.getMessage(), ex);
        throw new SoapFault(ex.getMessage(), sv);
      }
    }
 
    LOG.logEnd(l);
    return true;
  }

  public void processSignalMessages(List<Element> signalElements,
          MSHOutMail moutMail, SEDBox sb) throws ZPPException
           {
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
             String msg = "Error occured while parsing ecrypted key for mail: " + moutMail.getMessageId();
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
            String msg = "Error occured while accessing private key for cert: " + xc.toString();
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
            String msg = "Error occired while decrypting sym key with cert: " + xc.toString();
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
          EBMSMessageContext eInCtx, SoapMessage msg)
          throws ZPPException {
    
     long l = LOG.logStart();
    try {
     

      List<MSHOutMail> momLst = mDB.getMailByMessageId(MSHOutMail.class, mInMail.getRefToMessageId());
      if (momLst.isEmpty()){
        String strMsg = String.format("No out mail (refId %s) for deliveryAdvice %s ", mInMail.getMessageId(), mInMail.getRefToMessageId());
        throw new ZPPException(strMsg);
      }
      
      MSHOutMail mom = null;
      for (MSHOutMail mdn: momLst){
        if (Objects.equals(ZPPConstants.S_ZPP_SERVICE, mdn.getService())
               && Objects.equals(ZPPConstants.S_ZPP_ACTION_DELIVERY_NOTIFICATION, mdn.getAction())
                && Objects.equals(mInMail.getConversationId(), mdn.getConversationId())
                ){
          mom = mdn;
          break;
        }
      
      }
      
      if (mom == null){
        String strMsg = String.format("Found out mail (refId %s) but with wrong conversation id, service or action!", mInMail.getMessageId(), mInMail.getRefToMessageId());
        throw new ZPPException(strMsg);
      }
     
      
      // get x509 keys
     // String convId = mInMail.getConversationId();
     // LOG.formatedlog("Get key for conversation : '%s'", convId);
    //  BigInteger moID = new BigInteger(convId.substring(0, convId.indexOf("@")));
    //  MSHOutMail mom = mDB.getMailById(MSHOutMail.class, moID);
      mom.setDeliveredDate(mInMail.getSentDate());

      String alias = eInCtx.getSenderPartyIdentitySet().getLocalPartySecurity().
              getSignatureKeyAlias();
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
        throw new ZPPException(
                "AdviceOfDelivery must have two signatures: recipient's and "
                + " signature of recipient delivery system");

      }
    } catch (IOException | CertificateException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException | SignatureException | SEDSecurityException ex) {
      throw new ZPPException(
              "Error occured while validating AdviceOfDelivery! ex");
    }
    LOG.logEnd(l);
  }

  /**
   *
   * @param mInMail
   * @throws FOPException
   * @throws HashException
   */
  public void processInZPPDelivery(MSHInMail mInMail)
          throws FOPException, HashException {
    long l = LOG.logStart();
    mInMail.setStatus(SEDInboxMailStatus.PLOCKED.getValue());
    mInMail.setStatusDate(Calendar.getInstance().getTime());
    try {
      mDB.serializeInMail(mInMail, "ZPP plugin");
      //mDB.setStatusToInMail(mInMail, SEDInboxMailStatus.PLOCKED, "ZPP mail received.");
      // add ZPPReceipt
      // notify in delivery
    } catch (StorageException ex) {
      LOG.logError(l, "Error setting status ERROR to MSHInMail :'" + mInMail.
              getId() + "'!", ex);
    }

    LOG.logEnd(l);
  }

  public void processInZPPFictionNotification(MSHInMail inMail)
          throws FOPException, HashException, SEDSecurityException, IOException,
          ParserConfigurationException, SAXException, JAXBException {
    long l = LOG.logStart();
    File encKeyFile = null;
    EncryptedKey ek = null;
    for (MSHInPart mip : inMail.getMSHInPayload().getMSHInParts()) {
      if (Objects.equals(mip.getType(), ZPPConstants.ELM_SIGNAL_ENCRYPTED_KEY)) {
        encKeyFile = StorageUtils.getFile(mip.getFilepath());
        ek = mSedCrypto.file2SimetricEncryptedKey(encKeyFile);

        break;
      }
    }

    Key k = null;

    // resolve certificate
    X509Certificate xc;
    try {
      xc = ek.getKeyInfo().getX509Certificate();
    } catch (KeyResolverException ex) {
      String errmsg
              = "Could not resolve Cert info from Encrypted key key DeliveryAdvice "
              + inMail.getId() + "Err:" + ex.getMessage();
      LOG.logError(l, errmsg, ex);

      return;
    }
    //Key pk = mkeyUtils.getPrivateKeyForX509Cert(mCertBean.getCertificateStore(), xc);
    Key pk = mCertBean.getPrivateKeyForX509Cert(xc);
    if (pk == null) {
      String errmsg
              = "Could not get private key for cert: " + xc.getSubjectDN()
              + ". Private key is needed to decrypt Encrypted key for "
              + inMail.getConversationId();
      LOG.logError(l, errmsg, null);

      return;
    }
    k = mSedCrypto.decryptEncryptedKey(XMLUtils.deserializeToDom(encKeyFile).
            getDocumentElement(),
            pk, SEDCrypto.SymEncAlgorithms.AES128_CBC);

    if (inMail != null && k != null) {
      decryptMail(k, inMail.getConversationId(), null);

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
          mDB.addInMailPayload(mi, lstDec, SEDInboxMailStatus.RECEIVED, "Received secred key and decrypt payloads",
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
  public void handleFault(SoapMessage t) {
    // ignore
  }

}
