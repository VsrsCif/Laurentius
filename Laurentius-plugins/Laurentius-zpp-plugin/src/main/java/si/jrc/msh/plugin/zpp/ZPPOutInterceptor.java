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
import java.io.IOException;
import java.math.BigInteger;
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
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.SignalMessage;
import org.w3c.dom.Element;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.laurentius.msh.outbox.payload.MSHOutPayload;
import si.jrc.msh.plugin.zpp.doc.DocumentSodBuilder;
import si.jrc.msh.plugin.zpp.exception.ZPPException;
import si.jrc.msh.plugin.zpp.utils.FOPUtils;
import si.laurentius.lce.enc.SEDCrypto;
import si.laurentius.lce.enc.SEDKey;
import si.laurentius.lce.sign.pdf.SignUtils;
import si.laurentius.lce.sign.pdf.ValidateSignatureUtils;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.enums.SEDOutboxMailStatus;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.SEDValues;
import si.laurentius.commons.cxf.SoapUtils;
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
import si.laurentius.lce.DigestUtils;
import si.laurentius.msh.outbox.payload.OMPartProperty;
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

  KeystoreUtils mKeystoreUtils = new KeystoreUtils();

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
  ;
  SEDCrypto mscCrypto = new SEDCrypto();
  KeystoreUtils mksu = new KeystoreUtils();

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
    } catch (RollbackException | HeuristicMixedException | HeuristicRollbackException
            | SecurityException | IllegalStateException | SystemException ex) {
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
     
      OMPartProperty omp = new OMPartProperty();
      omp.setName(ZPPConstants.S_PART_PROPERTY_ORIGIN_MIMETYPE);
      omp.setValue(pt.getMimeType());
      
       ptNew.setMimeType(MimeValue.MIME_BIN.getMimeType());
      
      ptNew.getOMPartProperties().add(omp);
      //ptNew.setMimeType(pt.getMimeType());
      ptNew.setFilepath(StorageUtils.getRelativePath(fOut));
      ptNew.setDescription(ZPPConstants.MSG_DOC_PREFIX_DESC + pt.
              getDescription());
      ptNew.setSha256Value(DigestUtils.getHexSha256Digest(fOut));
      ptNew.setSize(BigInteger.valueOf(fOut.length()));
      ptNew.setName(pt.getName());
      ptNew.setFilename(fOut.getName());
      ptNew.setIsEncrypted(Boolean.TRUE);
      op.getMSHOutParts().add(ptNew);
    }

    LOG.logEnd(l, "Encrypted parts: '" + mail.getMSHOutPayload().
            getMSHOutParts().size()
            + "' for out mail" + skey.getId());
    return op;
  }

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

  private SEDKey getSecretKeyForId(BigInteger bi) {
    long l = LOG.logStart(bi);
    SEDKey sk = null;
    try {
      TypedQuery<SEDKey> q
              = memEManager.createNamedQuery("si.jrc.msh.sec.SEDKey.getById",
                      SEDKey.class);
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

    MSHInMail mInMail = SoapUtils.getMSHInMail(msg);

    LOG.formatedlog("got mail %s, service %s ", mInMail,
            mInMail != null
                    ? mInMail.getService() : "sss");
    // if service ZPP delivery, action delivery
    if (outMail != null && ZPPConstants.S_ZPP_SERVICE.equals(ectx.getService().
            getServiceName())) {

      if (ZPPConstants.S_ZPP_ACTION_DELIVERY_NOTIFICATION.equals(ectx.
              getAction().getName())) {
        try {
          prepareToZPPDelivery(outMail, ectx, sv);
        } catch (HashException | SEDSecurityException | StorageException | FOPException
                | ZPPException ex) {
          LOG.logError(l, ex.getMessage(), ex);
          throw new SoapFault(ex.getMessage(), sv);
        }
      } else if (ZPPConstants.S_ZPP_ACTION_FICTION_NOTIFICATION.equals(ectx.
              getAction().getName())) {
        try {
          prepareFictionNotification(outMail, ectx, msg);

        } catch (HashException | SEDSecurityException | StorageException | FOPException
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
        processInZPPAdviceOfDelivery(mInMail, msg);
      }
    }
    LOG.logEnd(l);
    return true;
  }

  private void prepareToZPPDelivery(MSHOutMail outMail,
          EBMSMessageContext eoutCtx, QName sv)
          throws SEDSecurityException,
          StorageException, FOPException, HashException, ZPPException {
    long l = LOG.logStart(outMail);

    if (outMail.getMSHOutPayload() == null || outMail.getMSHOutPayload().
            getMSHOutParts().isEmpty()) {
      String mg = "Empty message: " + outMail.getId() + ". No payloads to delivery.";
      throw new SoapFault(mg, sv);
    }

    SEDKey skey = getSecretKeyForId(outMail.getId());
    // check if mail is setted for ZPP delivery (case of resending
    if (skey != null
            && outMail.getMSHOutPayload().getMSHOutParts().get(0).getType() != null
            && outMail.getMSHOutPayload().getMSHOutParts().get(0).getType()
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
      String msg
              = "Resending out mail: " + outMail.getId()
              + ". Key and delivery nofitication already generated.";
      mDB.setStatusToOutMail(outMail, SEDOutboxMailStatus.PROCESS, msg, null,
              ZPPConstants.S_ZPP_PLUGIN_TYPE);
      LOG.log(msg);
    } else {

      if (skey == null) {
        skey = createAndStoreNewKey(outMail.getId());
      }

      // create nofitication
      File fDNViz
              = StorageUtils.getNewStorageFile(MimeValue.MIME_PDF.getSuffix(),
                      ZPPConstants.MSG_DELIVERY_NOTIFICATION_FILENAME + "-");
      getFOP().generateVisualization(outMail, fDNViz,
              FOPUtils.FopTransformations.DeliveryNotification,
              MimeValue.MIME_PDF.getMimeType());

      String alias
              = eoutCtx.getSenderPartyIdentitySet().getLocalPartySecurity().
                      getSignatureKeyAlias();

      PrivateKey pk = mCertBean.getPrivateKeyForAlias(alias);
      X509Certificate xcert = mCertBean.getX509CertForAlias(alias);

      signPDFDocument(pk, xcert, fDNViz, true);

      String fPDFVizualization = StorageUtils.getRelativePath(fDNViz);

      MSHOutPart ptNew = new MSHOutPart();
      ptNew.setEncoding(SEDValues.ENCODING_UTF8);
      ptNew.setMimeType(MimeValue.MIME_PDF.getMimeType());
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
      String str
              = "Added DeliveryNotification and encrypted parts: "
              + outMail.getMSHOutPayload().getMSHOutParts().size();
      mDB.setStatusToOutMail(outMail, SEDOutboxMailStatus.PROCESS, str, null,
              ZPPConstants.S_ZPP_PLUGIN_TYPE);
    }

    // mail
    LOG.logEnd(l, "Out mail: '" + skey.getId() + "' ready to send by LegalZPP!");

  }

  private void prepareFictionNotification(MSHOutMail outMail,
          EBMSMessageContext ectx,
          SoapMessage msg)
          throws SEDSecurityException,
          StorageException, FOPException, HashException, ZPPException {

    long l = LOG.logStart();

  }

  public void processOutZPPAdviceOfDelivery(MSHOutMail om,
          EBMSMessageContext eoutCtx,
          SoapMessage msg) {

    try {
      MSHOutPart mp = om.getMSHOutPayload().getMSHOutParts().get(0);
      File fda = StorageUtils.getFile(mp.getFilepath());
      ValidateSignatureUtils vsu = new ValidateSignatureUtils();
      List<X509Certificate> cslst = vsu.getSignatureCerts(fda);
      // is 
      if (cslst.size() < 2) {

        String alias
                = eoutCtx.getSenderPartyIdentitySet().getLocalPartySecurity().
                        getSignatureKeyAlias();

        PrivateKey pk = mCertBean.getPrivateKeyForAlias(alias);
        X509Certificate xcert = mCertBean.getX509CertForAlias(alias);

        signPDFDocument(pk, xcert, fda, true);
      }
    } catch (SEDSecurityException | IOException | CertificateException | NoSuchAlgorithmException | InvalidKeyException
            | NoSuchProviderException | SignatureException ex) {
      Logger.getLogger(ZPPOutInterceptor.class.getName()).
              log(Level.SEVERE, null, ex);
    }

  }

  public void processInZPPAdviceOfDelivery(MSHInMail mInMail, SoapMessage msg) {
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

      ValidateSignatureUtils vsu = new ValidateSignatureUtils();
      List<X509Certificate> lvc = vsu.getSignatureCerts(docFile.
              getAbsolutePath());

      X509Certificate xc = lvc.get(0);

      // get key
      Key key = getEncryptionKeyForDeliveryAdvice(moID);
      LOG.log("processInZPPAdviceoFDelivery - get key" + key);
      Element elKey
              = mscCrypto.encryptedKeyWithReceiverPublicKey(key, xc, mInMail.
                      getSenderEBox(),
                      mInMail.getConversationId());
      LOG.log("processInZPPAdviceoFDelivery - get encrypted key" + elKey);
      // got signal message:
      SignalMessage signal = msg.getExchange().get(SignalMessage.class);
      signal.getAnies().add(elKey);
      //mDB.set

      mDB.setStatusToOutMail(mom, SEDOutboxMailStatus.DELIVERED,
              "Received ZPP advice of delivery",
              null, null, StorageUtils.getRelativePath(docFile),
              MimeValue.MIME_XML.getMimeType());

    } catch (StorageException | IOException
            | SEDSecurityException | CertificateException | NoSuchAlgorithmException
            | InvalidKeyException | NoSuchProviderException | SignatureException ex) {
      LOG.logError(l, ex);
    }
    LOG.logEnd(l);
  }

  public Key getEncryptionKeyForDeliveryAdvice(BigInteger mailId)
          throws IOException {
    TypedQuery<SEDKey> q
            = memEManager.createNamedQuery("si.jrc.msh.sec.SEDKey.getById",
                    SEDKey.class);
    q.setParameter("id", mailId);
    return q.getSingleResult();
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
    } catch (IOException ex) {
      LOG.logError(l, ex);
    } catch (StorageException ex) {
      Logger.getLogger(ZPPOutInterceptor.class.getName()).
              log(Level.SEVERE, null, ex);
    }
    return ftmp;
  }

}
