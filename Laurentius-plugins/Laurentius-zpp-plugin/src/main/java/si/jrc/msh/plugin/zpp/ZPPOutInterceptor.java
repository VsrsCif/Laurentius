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
import java.math.BigInteger;
import java.security.Key;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.xml.namespace.QName;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.message.MessageUtils;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.laurentius.msh.outbox.payload.MSHOutPayload;
import si.jrc.msh.plugin.zpp.doc.DocumentSodBuilder;
import si.jrc.msh.plugin.zpp.exception.ZPPException;
import si.jrc.msh.plugin.zpp.utils.FOPUtils;
import si.jrc.msh.sec.SEDCrypto;
import si.jrc.msh.sec.SEDKey;
import si.laurentius.commons.MimeValues;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDOutboxMailStatus;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.SEDValues;
import si.laurentius.commons.cxf.SoapUtils;
import si.laurentius.commons.exception.FOPException;
import si.laurentius.commons.exception.HashException;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.interfaces.SoapInterceptorInterface;
import si.laurentius.commons.pmode.EBMSMessageContext;
import si.laurentius.commons.utils.HashUtils;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;

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

  /**
   *
   */
  protected final SEDLogger LOG = new SEDLogger(ZPPOutInterceptor.class);
  DocumentSodBuilder dsbSodBuilder = new DocumentSodBuilder();

  /**
   *
   */
  protected final SEDCrypto.SymEncAlgorithms mAlgorithem = SEDCrypto.SymEncAlgorithms.AES128_CBC;

  /**
   *
   */
  @PersistenceContext(unitName = "ebMS_ZPP_PU", name = "ebMS_ZPP_PU")
  public EntityManager memEManager;
  FOPUtils mfpFop = null;
  HashUtils mpHU = new HashUtils();
  SEDCrypto mscCrypto = new SEDCrypto();

  /**
   *
   */
  @Resource
  public UserTransaction mutUTransaction;

  private SEDKey createAndStoreNewKey(BigInteger bi)
      throws SEDSecurityException, ZPPException {
    long l = LOG.logStart(bi);
    SEDKey sk;

    Key skey = mscCrypto.getKey(mAlgorithem);
    sk = new SEDKey(bi, skey.getEncoded(), skey.getAlgorithm(), skey.getFormat());
    try {
      mutUTransaction.begin();
    } catch (NotSupportedException | SystemException ex) {
      LOG.logError(l, ex);
      throw new ZPPException("Error starting DB transaction! ", ex);
    }
    memEManager.persist(sk);
    try {
      mutUTransaction.commit();
    } catch (RollbackException | HeuristicMixedException | HeuristicRollbackException |
        SecurityException | IllegalStateException | SystemException ex) {
      throw new ZPPException("Error committing  secret key to DB! ", ex);
    }

    LOG.logEnd(l, bi);
    return sk;
  }

  private MSHOutPayload getEncryptedPayloads(SEDKey skey, MSHOutMail mail)
      throws SEDSecurityException, StorageException, HashException {
    long l = LOG.logStart();
    MSHOutPayload op = new MSHOutPayload();
    for (MSHOutPart pt : mail.getMSHOutPayload().getMSHOutParts()) {
      File fIn = StorageUtils.getFile(pt.getFilepath());
      File fOut = new File(fIn.getAbsoluteFile() + ZPPConstants.S_ZPP_ENC_SUFFIX);
      mscCrypto.encryptFile(fIn, fOut, skey);
      MSHOutPart ptNew = new MSHOutPart();
      ptNew.setMimeType(pt.getMimeType());
      ptNew.setFilepath(StorageUtils.getRelativePath(fOut));
      ptNew.setDescription(ZPPConstants.MSG_DOC_PREFIX_DESC + pt.getDescription());
      ptNew.setMd5(mpHU.getMD5Hash(fOut));
      ptNew.setName(pt.getName());
      ptNew.setFilename(fOut.getName());
      ptNew.setIsEncrypted(Boolean.TRUE);
      op.getMSHOutParts().add(ptNew);
    }

    LOG.logEnd(l, "Encrypted parts: '" + mail.getMSHOutPayload().getMSHOutParts().size() +
         "' for out mail" + skey.getId());
    return op;
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

  private SEDKey getSecretKeyForId(BigInteger bi) {
    long l = LOG.logStart(bi);
    SEDKey sk = null;
    try {
      TypedQuery<SEDKey> q =
          memEManager.createNamedQuery("si.jrc.msh.sec.SEDKey.getById", SEDKey.class);
      q.setParameter("id", bi);
      sk = q.getSingleResult();
    } catch (NoResultException ignore) {

    }
    LOG.logEnd(l, bi);
    return sk;
  }

  /**
   *
   * @param t
   */
  @Override
  public void handleFault(SoapMessage t) {
    // ignore
  }

  /**
   *
   * @param msg
   */
  @Override
  public boolean handleMessage(SoapMessage msg) {
    long l = LOG.logStart(msg);

    boolean isRequest = MessageUtils.isRequestor(msg);
    QName sv = (isRequest ? SoapFault.FAULT_CODE_CLIENT : SoapFault.FAULT_CODE_SERVER);

    EBMSMessageContext ectx = SoapUtils.getEBMSMessageOutContext(msg);
    MSHOutMail outMail = SoapUtils.getMSHOutMail(msg);
    // if service ZPP delivery, action delivery
    if (outMail != null && ZPPConstants.S_ZPP_SERVICE.equals(ectx.getService().getServiceName())) {

      if (ZPPConstants.S_ZPP_ACTION_DELIVERY_NOTIFICATION.equals(ectx.getAction().getName())) {
        try {
          prepareToZPPDelivery(outMail, sv);
        } catch (HashException | SEDSecurityException | StorageException | FOPException |
            ZPPException ex) {
          LOG.logError(l, ex.getMessage(), ex);
          throw new SoapFault(ex.getMessage(), sv);
        }
      } else if (ZPPConstants.S_ZPP_ACTION_FICTION_NOTIFICATION.equals(ectx.getAction().getName())) {
        try {
          prepareFictionNotification(outMail, sv);
        } catch (HashException | SEDSecurityException | StorageException | FOPException |
            ZPPException ex) {
          LOG.logError(l, ex.getMessage(), ex);
          throw new SoapFault(ex.getMessage(), sv);
        }
      }
    }
    LOG.logEnd(l);
    return true;
  }

  private void prepareToZPPDelivery(MSHOutMail outMail, QName sv)
      throws SEDSecurityException,
      StorageException, FOPException, HashException, ZPPException {
    long l = LOG.logStart(outMail);

    if (outMail.getMSHOutPayload() == null || outMail.getMSHOutPayload().getMSHOutParts().isEmpty()) {
      String mg = "Empty message: " + outMail.getId() + ". No payloads to delivery.";
      throw new SoapFault(mg, sv);
    }

    SEDKey skey = getSecretKeyForId(outMail.getId());
    // check if mail is setted for ZPP delivery (case of resending
    if (skey != null &&
         outMail.getMSHOutPayload().getMSHOutParts().get(0).getType() != null &&
         outMail.getMSHOutPayload().getMSHOutParts().get(0).getType()
        .equals(ZPPConstants.S_ZPP_ACTION_DELIVERY_NOTIFICATION)) {
      // because "delivery notification" is added if mail is succesfully encrypted -assuming all
      // payloads is encrypted
      // remove non encrypted payloads
      int i = outMail.getMSHOutPayload().getMSHOutParts().size();
      while (i-- > 1) {
        if (!outMail.getMSHOutPayload().getMSHOutParts().get(i).getIsEncrypted()) {
          outMail.getMSHOutPayload().getMSHOutParts().remove(i);
        }
      }
      String msg =
          "Resending out mail: " + outMail.getId() +
           ". Key and delivery nofitication already generated.";
      mDB.setStatusToOutMail(outMail, SEDOutboxMailStatus.PROCESS, msg, null,
          ZPPConstants.S_ZPP_PLUGIN_TYPE);
      LOG.log(msg);
    } else {

      if (skey == null) {
        skey = createAndStoreNewKey(outMail.getId());
      }

      // create nofitication
      File fDNViz =
          StorageUtils.getNewStorageFile(MimeValues.MIME_PDF.getSuffix(),
              ZPPConstants.MSG_DELIVERY_NOTIFICATION_FILENAME + "-");
      getFOP().generateVisualization(outMail, fDNViz,
          FOPUtils.FopTransformations.DeliveryNotification, MimeValues.MIME_PDF.getMimeType());
      String fPDFVizualization = StorageUtils.getRelativePath(fDNViz);

      MSHOutPart ptNew = new MSHOutPart();
      ptNew.setEncoding(SEDValues.ENCODING_UTF8);
      ptNew.setMimeType(MimeValues.MIME_PDF.getMimeType());
      ptNew.setName(ZPPConstants.S_ZPP_ACTION_DELIVERY_NOTIFICATION);
      ptNew.setDescription(ZPPConstants.MSG_DELIVERY_NOTIFICATION_DESC);
      ptNew.setType(ZPPConstants.S_ZPP_ACTION_DELIVERY_NOTIFICATION);
      ptNew.setFilepath(fPDFVizualization);
      ptNew.setFilename(fDNViz.getName());

      ptNew.setIsEncrypted(Boolean.FALSE);
      // encrypt payloads
      MSHOutPayload pl = getEncryptedPayloads(skey, outMail);
      // add vizualization as pdf
      pl.getMSHOutParts().add(0, ptNew);
      // set encrypted payloads
      outMail.setMSHOutPayload(pl);
      String str =
          "Added DeliveryNotification and encrypted parts: " +
           outMail.getMSHOutPayload().getMSHOutParts().size();
      mDB.setStatusToOutMail(outMail, SEDOutboxMailStatus.PROCESS, str, null,
          ZPPConstants.S_ZPP_PLUGIN_TYPE);
    }

    // mail
    LOG.logEnd(l, "Out mail: '" + skey.getId() + "' ready to send by LegalZPP!");

  }
  
  
  private void prepareFictionNotification(MSHOutMail outMail, QName sv)
      throws SEDSecurityException,
      StorageException, FOPException, HashException, ZPPException {
    
    // generate fiction notification for receiver
    // generate encrypted key with receiver key
    // generate out mail
    
    // generate fiction notification for sender
    
    // update ficition notification
  }

}
