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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.Key;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;
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
import javax.xml.transform.TransformerException;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.xml.security.encryption.EncryptedKey;
import org.apache.xml.security.keys.keyresolver.KeyResolverException;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.inbox.payload.MSHInPart;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.pmode.PMode;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.SignalMessage;
import si.laurentius.ebox.Execute;
import si.laurentius.ebox.Export;
import si.laurentius.ebox.SEDBox;
import org.w3c.dom.Element;
import si.jrc.msh.plugin.zpp.doc.DocumentSodBuilder;
import si.jrc.msh.plugin.zpp.utils.FOPUtils;
import si.jrc.msh.sec.SEDCrypto;
import si.jrc.msh.sec.SEDKey;
import si.laurentius.commons.MimeValues;
import si.laurentius.commons.SEDInboxMailStatus;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDOutboxMailStatus;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.cxf.SoapUtils;
import si.laurentius.commons.exception.FOPException;
import si.laurentius.commons.exception.HashException;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.JMSManagerInterface;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.interfaces.SoapInterceptorInterface;
import si.laurentius.commons.pmode.EBMSMessageContext;
import si.laurentius.commons.utils.HashUtils;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.StringFormater;
import si.laurentius.commons.utils.sec.KeystoreUtils;
import si.laurentius.commons.utils.xml.XMLUtils;

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
  HashUtils mpHU = new HashUtils();
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

  /**
   *
   * @param msg
   */
  @Override
  public void handleMessage(SoapMessage msg) {
    long l = LOG.logStart();

    SEDBox sb = SoapUtils.getMSHInMailReceiverBox(msg);

    //EBMSMessageContext ectx = SoapUtils.getEBMSMessageOutContext(msg);
    MSHOutMail moutMail = SoapUtils.getMSHOutMail(msg);
    MSHInMail mInMail = SoapUtils.getMSHInMail(msg);

    Object sigAnies = msg.getExchange().get("SIGNAL_ELEMENTS");

    try {
      if (mInMail != null && ZPPConstants.S_ZPP_SERVICE.equals(mInMail.getService()) &&
           ZPPConstants.S_ZPP_ACTION_DELIVERY_NOTIFICATION.equals(mInMail.getAction())) {
        processInZPPDelivery(mInMail);
      }

      if (mInMail != null && ZPPConstants.S_ZPP_SERVICE.equals(mInMail.getService()) &&
           ZPPConstants.S_ZPP_ACTION_ADVICE_OF_DELIVERY.equals(mInMail.getAction())) {
        processInZPPAdviceOfDelivery(mInMail, msg);
      }

      if (sigAnies != null) {
        LOG.log("Proccess in signal elments");
        processSignalMessages((List<Element>) sigAnies, moutMail, sb);
        /* List<Element> lst = (List<Element>) sigAnies;
        Key k = null;
        for (Element e : lst) {
          if (e.getLocalName().equals(ZPPConstants.ELM_SIGNAL_ENCRYPTED_KEY)) {

            // get encrypte key
            EncryptedKey ek = mSedCrypto.element2SimetricEncryptedKey(e);
            // resolve certificate
            X509Certificate xc;
            try {
              xc = ek.getKeyInfo().getX509Certificate();
            } catch (KeyResolverException ex) {
              LOG.logError(l, ex);
              String errmsg =
                  "Could not resolve Cert info from Encrypted key key DeliveryAdvice "
                      + moutMail.getId() + "Err:" + ex.getMessage();
              LOG.logError(l, errmsg, ex);
              try {
                mDB.setStatusToOutMail(moutMail, SEDOutboxMailStatus.ERROR, errmsg, null,
                    ZPPConstants.S_ZPP_PLUGIN_TYPE);
              } catch (StorageException ex1) {
                LOG.logError(l, "Error setting status ERROR to MSHInMail :'" + moutMail.getId()
                    + "'!", ex1);
              }
              return;
            }
            Key pk = mkeyUtils.getPrivateKeyForX509Cert(msedLookup.getSEDCertStore(), xc);
            if (pk == null) {
              String errmsg =
                  "Could not get private key for cert: " + xc.getSubjectDN()
                      + ". Private key is needed to decrypt Encrypted key for "
                      + moutMail.getConversationId();
              LOG.logError(l, errmsg, null);
              try {
                mDB.setStatusToOutMail(moutMail, SEDOutboxMailStatus.ERROR, errmsg, null,
                    ZPPConstants.S_ZPP_PLUGIN_TYPE);
              } catch (StorageException ex) {
                LOG.logError(l, "Error setting status ERROR to MSHInMail :'" + moutMail.getId()
                    + "'!", ex);
              }

              return;
            }
            // String singDAAlias = "";
            // SEDCertStore sc = msedLookup.getSEDCertStoreByCertAlias(singDAAlias, true);

            k = mSedCrypto.decryptEncryptedKey(e, pk, SEDCrypto.SymEncAlgorithms.AES128_CBC);
            break;

          }
        
        LOG.log("ZPPInInterceptor 3, key: " + k);
        if (moutMail != null && k != null) {
          LOG.log("ZPPInInterceptor 4, key: " + k);
          decryptMail(k, moutMail.getConversationId(), sb);

        }
      }*/
      }
    } catch (FOPException | HashException | SEDSecurityException ex) {
      LOG.logError(l, ex);
      if (mInMail != null) {
        try {
          mDB.setStatusToInMail(mInMail, SEDInboxMailStatus.ERROR, ex.getMessage(), null,
              ZPPConstants.S_ZPP_PLUGIN_TYPE);
        } catch (StorageException ex1) {
          LOG.logError(l, "Error setting status ERROR to MSHInMail :'" + moutMail.getId() + "'!",
              ex1);
        }
      }
      if (moutMail != null) {
        try {
          mDB.setStatusToOutMail(moutMail, SEDOutboxMailStatus.ERROR, ex.getMessage(), null,
              ZPPConstants.S_ZPP_PLUGIN_TYPE);
        } catch (StorageException ex1) {
          LOG.logError(l, "Error setting status ERROR to MSHInMail :'" + moutMail.getId() + "'!",
              ex1);
        }
      }

    }
    LOG.logEnd(l);
  }

  public void processSignalMessages(List<Element> signalElements, MSHOutMail moutMail, SEDBox sb)
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
            String errmsg =
                "Could not resolve Cert info from Encrypted key key DeliveryAdvice " +
                 moutMail.getId() + "Err:" + ex.getMessage();
            LOG.logError(l, errmsg, ex);
            try {
              mDB.setStatusToOutMail(moutMail, SEDOutboxMailStatus.ERROR, errmsg, null,
                  ZPPConstants.S_ZPP_PLUGIN_TYPE);
            } catch (StorageException ex1) {
              LOG.logError(l, "Error setting status ERROR to MSHInMail :'" + moutMail.getId() +
                   "'!", ex1);
            }
            return;
          }
          Key pk = mkeyUtils.getPrivateKeyForX509Cert(msedLookup.getSEDCertStore(), xc);
          if (pk == null) {
            String errmsg =
                "Could not get private key for cert: " + xc.getSubjectDN() +
                 ". Private key is needed to decrypt Encrypted key for " +
                 moutMail.getConversationId();
            LOG.logError(l, errmsg, null);
            try {
              mDB.setStatusToOutMail(moutMail, SEDOutboxMailStatus.ERROR, errmsg, null,
                  ZPPConstants.S_ZPP_PLUGIN_TYPE);
            } catch (StorageException ex) {
              LOG.logError(l, "Error setting status ERROR to MSHInMail :'" + moutMail.getId() +
                   "'!", ex);
            }

            return;
          }
          // String singDAAlias = "";
          // SEDCertStore sc = msedLookup.getSEDCertStoreByCertAlias(singDAAlias, true);

          k = mSedCrypto.decryptEncryptedKey(e, pk, SEDCrypto.SymEncAlgorithms.AES128_CBC);
          break;

        }
      }
      LOG.log("ZPPInInterceptor 3, key: " + k);
      if (moutMail != null && k != null) {
        LOG.log("ZPPInInterceptor 4, key: " + k);
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

      File docFile =
          StorageUtils.getFile(mInMail.getMSHInPayload().getMSHInParts().get(0).getFilepath());

      String x509 =
          XMLUtils.getElementValue(new FileInputStream(docFile),
              ZPPInInterceptor.class.getResourceAsStream("/xslt/getX509CertFromDocument.xsl"));
      if (x509 != null) {
        X509Certificate xc =
            mSedCrypto.getCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(x509)));
        // get key
        Key key = getEncryptionKeyForDeliveryAdvice(mInMail);
        LOG.log("processInZPPAdviceoFDelivery - get key" + key);
        Element elKey =
            mSedCrypto.encryptedKeyWithReceiverPublicKey(key, xc, mInMail.getSenderEBox(),
                mInMail.getConversationId());
        LOG.log("processInZPPAdviceoFDelivery - get encrypted key" + elKey);
        // got signal message:
        SignalMessage signal = msg.getExchange().get(SignalMessage.class);
        signal.getAnies().add(elKey);

      }

    } catch (JAXBException | TransformerException | IOException | SEDSecurityException ex) {
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
    mInMail.setStatus(SEDInboxMailStatus.PLUGINLOCKED.getValue());
    mInMail.setStatusDate(Calendar.getInstance().getTime());
    try {
      mDB.updateInMail(mInMail, "ZPP mail received. ", null);
      // notify in delivery
    } catch (StorageException ex) {
      LOG.logError(l, "Error setting status ERROR to MSHInMail :'" + mInMail.getId() + "'!", ex);
    }

    LOG.logEnd(l);
  }

  /**
   *
   * @return
   */
  public FOPUtils getFOP() {
    if (mfpFop == null) {
      File fconf =
          new File(System.getProperty(SEDSystemProperties.SYS_PROP_HOME_DIR) + File.separator +
               ZPPConstants.SVEV_FOLDER + File.separator + ZPPConstants.FOP_CONFIG_FILENAME);

      mfpFop =
          new FOPUtils(fconf, System.getProperty(SEDSystemProperties.SYS_PROP_HOME_DIR) +
               File.separator + ZPPConstants.SVEV_FOLDER + File.separator +
               ZPPConstants.XSLT_FOLDER);
    }
    return mfpFop;
  }

  /**
   *
   * @param mi
   * @return
   * @throws IOException
   */
  public Key getEncryptionKeyForDeliveryAdvice(MSHInMail mi)
      throws IOException {
    // conv id is [send mailID]@domain
    String convId = mi.getConversationId();

    TypedQuery<SEDKey> q =
        memEManager.createNamedQuery("si.jrc.msh.sec.SEDKey.getById", SEDKey.class);
    q.setParameter("id",
        new BigInteger(mi.getConversationId().substring(0, mi.getConversationId().indexOf("@"))));
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

      List<MSHInMail> lst =
          mDB.getInMailConvIdAndAction(ZPPConstants.S_ZPP_ACTION_DELIVERY_NOTIFICATION, convID);
      if (lst.isEmpty()) {
        String errMsg =
            "Mail with convid: " + convID + " and action: " +
             ZPPConstants.S_ZPP_ACTION_DELIVERY_NOTIFICATION + " not found!" +
             "Nothing to decrypt!";
        LOG.logError(l, errMsg, null);
      }
      for (MSHInMail mi : lst) {

        if (sb == null) {
          sb = msedLookup.getSEDBoxByAddressName(mi.getReceiverEBox());
        }

        String exporFileName = null;
        File exportFolder = null;
        if (sb != null && sb.getExport() != null && sb.getExport().getActive() &&
             sb.getExport().getFileMask() != null) {
          Export e = sb.getExport();
          exporFileName = msfFormat.format(e.getFileMask(), mi);
          String folder = StringFormater.replaceProperties(e.getFolder());
          exportFolder = new File(folder);
          if (!exportFolder.exists()) {
            exportFolder.mkdirs();
          }
        }

        List<MSHInPart> lstDec = new ArrayList<>();
        int i = 0;
        List<String> listFiles = new ArrayList<>();
        for (MSHInPart mip : mi.getMSHInPayload().getMSHInParts()) {
          String oldFileName = mip.getFilename();
          i++;
          if (mip.getIsEncrypted()) {

            String newFileName =
                oldFileName.substring(0, oldFileName.lastIndexOf(ZPPConstants.S_ZPP_ENC_SUFFIX));
            File fNew;
            try (FileInputStream fis = new FileInputStream(StorageUtils.getFile(mip.getFilepath()));
                FileOutputStream bos =
                new FileOutputStream(fNew = StorageUtils.getFile(newFileName))) {
              LOG.log("Decrypt file: " + newFileName);
              mSedCrypto.decryptStream(fis, bos, key);

              MSHInPart miDec = new MSHInPart();
              String desc = mip.getDescription();
              if (desc != null && desc.startsWith(ZPPConstants.MSG_DOC_PREFIX_DESC)) {
                desc = desc.substring(ZPPConstants.MSG_DOC_PREFIX_DESC.length());
              }
              miDec.setDescription(desc);
              miDec.setEbmsId(mip.getEbmsId() + "-dec");
              miDec.setEncoding(mip.getEncoding());
              miDec.setFilename(newFileName);
              miDec.setMimeType(mip.getMimeType());
              miDec.setName(mip.getName());
              miDec.setType(mip.getType());
              miDec.setIsEncrypted(Boolean.FALSE);

              miDec.setMd5(mpHU.getMD5Hash(fNew));
              miDec.setFilepath(StorageUtils.getRelativePath(fNew));
              lstDec.add(miDec);
              if (sb != null && sb.getExport() != null && exportFolder != null &&
                   exporFileName != null) {
                String filPrefix = exportFolder.getAbsolutePath() + File.separator + exporFileName;
                if (sb.getExport().getExportMetaData()) {
                  String fn = filPrefix + "." + MimeValues.MIME_XML.getSuffix();
                  try {

                    listFiles.add(fn);
                    XMLUtils.serialize(mi, fn);
                  } catch (JAXBException | FileNotFoundException ex) {
                    LOG.logError(l, "Export metadata ERROR. Export file:" + fn + ".", ex);
                  }
                }
                String fn =
                    filPrefix + "_" + i + "." + MimeValues.getSuffixBYMimeType(miDec.getMimeType());
                listFiles.add(fn);
                msuStorage.copyFile(fNew, new File(fn), true);
              }

            } catch (IOException | StorageException | SEDSecurityException | HashException ex) {
              LOG.logError(l, "Error occured while decrypting  file: '" + oldFileName +
                   "' for inmail:" + mi.getId(), ex);
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

        if (sb != null && sb.getExecute() != null && sb.getExecute().getActive() &&
             sb.getExecute().getCommand() != null) {
          Execute e = sb.getExecute();
          String params = msfFormat.format(e.getParameters(), mi);
          try {
            mJMS.executeProcessOnInMail(mi.getId().longValue(), sb.getExecute().getCommand(),
                String.join(File.pathSeparator, listFiles) + " " + params);
          } catch (NamingException | JMSException ex) {
            LOG.logError(l, "Error sumittig mail on execute queue :'" + mi.getId() + "'!", ex);
          }

        }

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
