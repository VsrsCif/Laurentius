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
package si.mju.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.Key;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import org.apache.cxf.binding.soap.SoapMessage;

import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.inbox.payload.MSHInPart;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.ebox.SEDBox;
import org.w3c.dom.Element;

import si.laurentius.lce.enc.SEDCrypto;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.enums.SEDInboxMailStatus;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.enums.SEDOutboxMailStatus;
import si.laurentius.commons.cxf.SoapUtils;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.StringFormater;

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
public class MJUZPPInInterceptor implements SoapInterceptorInterface {

  /**
   *
   */
  protected final SEDLogger LOG = new SEDLogger(MJUZPPInInterceptor.class);
  SEDCrypto mSedCrypto = new SEDCrypto();

  KeystoreUtils mkeyUtils = new KeystoreUtils();
  StorageUtils msuStorage = new StorageUtils();

  StringFormater msfFormat = new StringFormater();

  @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
  SEDDaoInterface mDB;






  @Override
  public MailInterceptorDef getDefinition() {
    MailInterceptorDef mid = new MailInterceptorDef();
    mid.setDescription("MJU ZPP in to locked status");
    mid.setName("MJU in intercepror");
    mid.setType("MJUInInterceptor");
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

      if (sigAnies != null) {
        LOG.log("Proccess in signal elments");
        processSignalMessages((List<Element>) sigAnies, moutMail, sb);
      }
    } catch ( SEDSecurityException ex) {
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
          org.apache.xml.security.encryption.EncryptedKey ek = mSedCrypto.element2SimetricEncryptedKey(e);
          // resolve certificate
          X509Certificate xc;
          try {
            xc = ek.getKeyInfo().getX509Certificate();
          } catch (org.apache.xml.security.keys.keyresolver.KeyResolverException ex) {
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
          
          

          Key pk = null; // todo
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
        decryptMail(k, moutMail.getConversationId());

      }
    }
    LOG.logEnd(l);
  }

  


  /**
   *
   * @param key
   * @param convID
   * @param sb
   */
  public void decryptMail(Key key, String convID) {
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
              /*
              if (desc != null && desc.startsWith(
                      ZPPConstants.MSG_DOC_PREFIX_DESC)) {
                desc = desc.substring(ZPPConstants.MSG_DOC_PREFIX_DESC.length());
              }*/
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
