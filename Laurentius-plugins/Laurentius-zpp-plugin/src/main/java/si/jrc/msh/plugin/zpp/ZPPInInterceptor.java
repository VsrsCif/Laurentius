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
import java.security.Key;
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
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.UserTransaction;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.cxf.binding.soap.SoapMessage;
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
import si.laurentius.commons.utils.Utils;
import si.laurentius.lce.DigestUtils;
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

    SEDBox sb = SoapUtils.getMSHInMailReceiverBox(msg);

    //EBMSMessageContext ectx = SoapUtils.getEBMSMessageOutContext(msg);
    MSHOutMail moutMail = SoapUtils.getMSHOutMail(msg);
    MSHInMail mInMail = SoapUtils.getMSHInMail(msg);

    Object sigAnies = SoapUtils.getInSignals(msg);

    try {
      if (mInMail != null && ZPPConstants.S_ZPP_SERVICE.equals(mInMail.
              getService())
              && ZPPConstants.S_ZPP_ACTION_DELIVERY_NOTIFICATION.equals(mInMail.
                      getAction())) {
        processInZPPDelivery(mInMail);
      }

      if (mInMail != null && ZPPConstants.S_ZPP_SERVICE.equals(mInMail.
              getService())
              && ZPPConstants.S_ZPP_ACTION_FICTION_NOTIFICATION.equals(mInMail.
                      getAction())) {
        processInZPPFictionNotification(mInMail);
      }

      /*
      if (mInMail != null && ZPPConstants.S_ZPP_SERVICE.equals(mInMail.getService()) &&
          ZPPConstants.S_ZPP_ACTION_ADVICE_OF_DELIVERY.equals(mInMail.getAction())) {
        processInZPPAdviceOfDelivery(mInMail, msg);
      }*/
      if (sigAnies != null) {
        LOG.log("Proccess in signal elments");
        processSignalMessages((List<Element>) sigAnies, moutMail, sb);
      }
    } catch (FOPException | HashException | SEDSecurityException ex) {
      LOG.logError(l, ex);
      if (mInMail != null) {
        try {
          mDB.setStatusToInMail(mInMail, SEDInboxMailStatus.ERROR, ex.
                  getMessage(), null,
                  ZPPConstants.S_ZPP_PLUGIN_TYPE);
        } catch (StorageException ex1) {
          LOG.logError(l,
                  "Error setting status ERROR to MSHInMail :'" + moutMail.
                          getId() + "'!",
                  ex1);
        }
      }
      if (moutMail != null) {
        try {
          mDB.setStatusToOutMail(moutMail, SEDOutboxMailStatus.ERROR, ex.
                  getMessage(), null,
                  ZPPConstants.S_ZPP_PLUGIN_TYPE);
        } catch (StorageException ex1) {
          LOG.logError(l,
                  "Error setting status ERROR to MSHInMail :'" + moutMail.
                          getId() + "'!",
                  ex1);
        }
      }

    } catch (IOException ex) {
      Logger.getLogger(ZPPInInterceptor.class.getName()).log(Level.SEVERE, null,
              ex);
    } catch (ParserConfigurationException ex) {
      Logger.getLogger(ZPPInInterceptor.class.getName()).log(Level.SEVERE, null,
              ex);
    } catch (SAXException ex) {
      Logger.getLogger(ZPPInInterceptor.class.getName()).log(Level.SEVERE, null,
              ex);
    } catch (JAXBException ex) {
      Logger.getLogger(ZPPInInterceptor.class.getName()).log(Level.SEVERE, null,
              ex);
    }
    LOG.logEnd(l);
    return true;
  }

  public void processSignalMessages(List<Element> signalElements,
          MSHOutMail moutMail, SEDBox sb)
          throws SEDSecurityException {
    long l = LOG.logStart();
    if (signalElements != null) {

      Key k = null;
      for (Element e : signalElements) {
        if (e.getLocalName().equals(ZPPConstants.ELM_SIGNAL_ENCRYPTED_KEY)) {

          // get encrypte key
          EncryptedKey ek = mSedCrypto.element2SimetricEncryptedKey(e);
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
              mDB.
                      setStatusToOutMail(moutMail, SEDOutboxMailStatus.ERROR,
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

          Key pk = mCertBean.getPrivateKeyForX509Cert(xc);
          if (pk == null) {
            String errmsg
                    = "Could not get private key for cert: " + xc.getSubjectDN()
                    + ". Private key is needed to decrypt Encrypted key for "
                    + moutMail.getConversationId();
            LOG.logError(l, errmsg, null);
            try {
              mDB.
                      setStatusToOutMail(moutMail, SEDOutboxMailStatus.ERROR,
                              errmsg, null,
                              ZPPConstants.S_ZPP_PLUGIN_TYPE);
            } catch (StorageException ex) {
              LOG.logError(l,
                      "Error setting status ERROR to MSHInMail :'" + moutMail.
                              getId()
                      + "'!", ex);
            }

            return;
          }
          k = mSedCrypto.decryptEncryptedKey(e, pk,
                  SEDCrypto.SymEncAlgorithms.AES128_CBC);
          break;

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
   * @param msg
   * @throws FOPException
   * @throws HashException
   */
  public void processInZPPAdviceOfDelivery(MSHInMail mInMail, SoapMessage msg)
          throws FOPException, HashException {
    long l = LOG.logStart();
    try {
      // get x509 keys
      String convId = mInMail.getConversationId();
      LOG.formatedlog("Get key for conversation : '%s'", convId);
      BigInteger moID = new BigInteger(convId.substring(0, convId.indexOf("@")));
      MSHOutMail mom = mDB.getMailById(MSHOutMail.class, moID);
      mom.setDeliveredDate(mInMail.getSentDate());

      File docFile
              = StorageUtils.getFile(mInMail.getMSHInPayload().getMSHInParts().
                      get(0).getFilepath());

      String x509
              = XMLUtils.getElementValue(new FileInputStream(docFile),
                      ZPPInInterceptor.class.getResourceAsStream(
                              "/xslt/getX509CertFromDocument.xsl"));
      if (x509 != null) {
        X509Certificate xc
                = mSedCrypto.getCertificate(new ByteArrayInputStream(Base64.
                        getDecoder().decode(x509)));
        // get key
        Key key = getEncryptionKeyForDeliveryAdvice(moID);
        LOG.log("processInZPPAdviceoFDelivery - get key" + key);
        Element elKey
                = mSedCrypto.encryptedKeyWithReceiverPublicKey(key, xc, mInMail.
                        getSenderEBox(),
                        mInMail.getConversationId());
        LOG.log("processInZPPAdviceoFDelivery - get encrypted key" + elKey);
        // got signal message:
        SignalMessage signal = msg.getExchange().get(SignalMessage.class);
        signal.getAnies().add(elKey);

        mDB.setStatusToOutMail(mom, SEDOutboxMailStatus.DELIVERED,
                "Received ZPP advice of delivery",
                null, null, StorageUtils.getRelativePath(docFile),
                MimeValue.MIME_XML.getMimeType());
        //mDB.set
      }

    } catch (StorageException | JAXBException | TransformerException | IOException
            | SEDSecurityException ex) {
      LOG.logError(l, ex);
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
            for (IMPartProperty mp:  mip.getIMPartProperties()){
              if (Objects.equals(mp.getValue(), ZPPConstants.S_PART_PROPERTY_ORIGIN_MIMETYPE)){
                mime = mp.getValue();
                break;
              }
            }
            if (Utils.isEmptyString(mime)){
              mime = mip.getMimeType();
            }
            
            File fNew;
            try (FileInputStream fis = new FileInputStream(StorageUtils.getFile(
                    mip.getFilepath()));
                    FileOutputStream bos
                    = new FileOutputStream(fNew = StorageUtils.
                            getNewStorageFile(MimeValue.getSuffixBYMimeType(mime), "zpp-dec"))) {

              LOG.log("Decrypt file: " + newFileName);

              mSedCrypto.decryptStream(fis, bos, key);

              MSHInPart miDec = new MSHInPart();
              String desc = mip.getDescription();
              if (desc != null && desc.startsWith(
                      ZPPConstants.MSG_DOC_PREFIX_DESC)) {
                desc = desc.substring(ZPPConstants.MSG_DOC_PREFIX_DESC.length());
              }
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
            } catch (IOException | StorageException | SEDSecurityException  ex) {
              LOG.logError(l,
                      "Error occured while decrypting  file: '" + oldFileName
                      + "' for inmail:" + mi.getId(), ex);
            }
          }
        }

        mi.getMSHInPayload().getMSHInParts().addAll(lstDec);
        mi.setStatus(SEDInboxMailStatus.RECEIVED.getValue());
        mi.setStatusDate(Calendar.getInstance().getTime());
        try {
          mDB.updateInMail(mi, "Received secred key and decrypt payloads", null);
        } catch (StorageException ex) {
          LOG.logError(l, "Error updating mail :'" + mi.getId() + "'!", ex);
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
