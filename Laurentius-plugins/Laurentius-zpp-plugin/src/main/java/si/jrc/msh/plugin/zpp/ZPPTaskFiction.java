/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.jrc.msh.plugin.zpp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.Key;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.apache.xmlgraphics.util.MimeConstants;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.laurentius.msh.outbox.payload.MSHOutPayload;


import si.jrc.msh.plugin.zpp.doc.DocumentSodBuilder;
import si.jrc.msh.plugin.zpp.exception.ZPPException;
import si.jrc.msh.plugin.zpp.utils.FOPUtils;
import si.laurentius.lce.enc.SEDCrypto;
import si.laurentius.lce.enc.SEDKey;
import si.laurentius.lce.sign.pdf.SignUtils;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.enums.SEDInboxMailStatus;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.enums.SEDOutboxMailStatus;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.SEDValues;
import si.laurentius.commons.exception.FOPException;
import si.laurentius.commons.exception.HashException;
import si.laurentius.commons.exception.PModeException;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.JMSManagerInterface;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.StringFormater;
import si.laurentius.commons.utils.Utils;
import si.laurentius.lce.KeystoreUtils;
import si.laurentius.msh.inbox.payload.MSHInPart;
import si.laurentius.msh.inbox.payload.MSHInPayload;
import si.laurentius.msh.pmode.PartyIdentitySet;
import si.laurentius.commons.interfaces.SEDCertStoreInterface;
import si.laurentius.lce.DigestUtils;
import si.laurentius.plugin.crontask.CronTaskDef;
import si.laurentius.plugin.crontask.CronTaskPropertyDef;
import si.laurentius.plugin.interfaces.PropertyListType;
import si.laurentius.plugin.interfaces.PropertyType;
import si.laurentius.plugin.interfaces.TaskExecutionInterface;
import si.laurentius.plugin.interfaces.exception.TaskException;

/**
 *
 * @author Jože Rihtaršič
 */
@Stateless
@Local(TaskExecutionInterface.class)
public class ZPPTaskFiction implements TaskExecutionInterface {

  private static final SEDLogger LOG = new SEDLogger(ZPPTaskFiction.class);
  private static final String SIGN_ALIAS = "zpp.sign.key.alias";
   private static final String PROCESS_MAIL_COUNT = "zpp.max.mail.count";

  SEDCrypto mSedCrypto = new SEDCrypto();
  DocumentSodBuilder dsbSodBuilder = new DocumentSodBuilder();
  KeystoreUtils mkeyUtils = new KeystoreUtils();

  FOPUtils mfpFop = null;
  StringFormater msfFormat = new StringFormater();

  @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
  SEDDaoInterface mDB;

  @EJB(mappedName = SEDJNDI.JNDI_JMSMANAGER)
  JMSManagerInterface mJMS;

  @EJB(mappedName = SEDJNDI.JNDI_DBCERTSTORE)
  SEDCertStoreInterface mCertBean;

  @EJB(mappedName = SEDJNDI.JNDI_PMODE)
  PModeInterface mpModeManager;


  /**
   *
   */
  @PersistenceContext(unitName = "ebMS_ZPP_PU", name = "ebMS_ZPP_PU")
  public EntityManager memEManager;

  // TODO externalize
  /**
   *
   * @param p
   * @return
   * @throws TaskException
   */
  @Override
  public String executeTask(Properties p)
      throws TaskException {

    long l = LOG.logStart();
    StringWriter sw = new StringWriter();
    sw.append("Start zpp plugin task: \n");
    
     String signKeyAlias = "";
    if (!p.containsKey(SIGN_ALIAS)) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException,
              "Missing parameter:  '" + SIGN_ALIAS + "'!");
    } else {
      signKeyAlias = p.getProperty(SIGN_ALIAS);
    }

  
    int maxMailProc = 100;
    if (p.containsKey(PROCESS_MAIL_COUNT)) {
      String val = p.getProperty(PROCESS_MAIL_COUNT);
      if (!Utils.isEmptyString(val)) {
        try {
          maxMailProc = Integer.parseInt(val);
        } catch (NumberFormatException nfe) {
          LOG.logError(String.format(
              "Error parameter '%s'. Value '%s' is not a number Mail count 100 is setted!",
              PROCESS_MAIL_COUNT, val), nfe);
        }
      }
    }
    Calendar cDatFict = Calendar.getInstance();
    cDatFict.add(Calendar.DAY_OF_MONTH, -15);
    cDatFict.set(Calendar.HOUR_OF_DAY, 0);
    cDatFict.set(Calendar.MINUTE, 0);
    cDatFict.set(Calendar.SECOND, 0);
    cDatFict.set(Calendar.MILLISECOND, 0);
    // get all not delivered mail
    ZPPMailFilter mi = new ZPPMailFilter();
    mi.setStatus(SEDOutboxMailStatus.SENT.getValue());
    mi.setAction(ZPPConstants.S_ZPP_ACTION_DELIVERY_NOTIFICATION);
    mi.setService(ZPPConstants.S_ZPP_SERVICE);
    mi.setSentDateTo(cDatFict.getTime());
    List<MSHOutMail> lst = mDB.getDataList(MSHOutMail.class, -1, maxMailProc, "Id", "ASC", mi);
    sw.append("got " + lst.size() + " mail!");

    for (MSHOutMail m : lst) {
      try {
        processZPPFictionDelivery(m, signKeyAlias);
      } catch (SEDSecurityException | StorageException | FOPException | HashException | ZPPException ex) {
        String msg = String.format("Error occurred processing mail: '%s'. Err: %s.", m.getId(),
            ex.getMessage());
        LOG.logError(l, msg, ex);

        sw.append(msg);
      } 
    }

    sw.append("End zpp fiction plugin task");
    return sw.toString();
  }

  /**
   *
   * @return
   */
  @Override
  public CronTaskDef getDefinition() {
    CronTaskDef tt = new CronTaskDef();
    tt.setType("zpp-fiction-plugin");
    tt.setName("ZPP fiction delivery");
    tt.setDescription(
        "Create FictionAdviceOfDelivery for outgoing mail and send ficiton notification to " +
        "receiver");
    tt.getCronTaskPropertyDeves().add(createTTProperty(SIGN_ALIAS,
            "Signature key alias defined in keystore.", true, PropertyType.List.
                    getType(), null, PropertyListType.KeystoreCertKeys.getType()));
    tt.getCronTaskPropertyDeves().add(createTTProperty(PROCESS_MAIL_COUNT,
            "Max mail count proccesed.", true, PropertyType.Integer.getType(),
            null, null));
    return tt;
  }

  private CronTaskPropertyDef createTTProperty(String key, String desc, boolean mandatory,
      String type, String valFormat, String valList) {
    CronTaskPropertyDef ttp = new CronTaskPropertyDef();
    ttp.setKey(key);
    ttp.setDescription(desc);
    ttp.setMandatory(mandatory);
    ttp.setType(type);
    ttp.setValueFormat(valFormat);
    ttp.setValueList(valList);
    return ttp;
  }

  private CronTaskPropertyDef createTTProperty(String key, String desc) {
    return createTTProperty(key, desc, true, "string", null, null);
  }

  /**
   *
   * @param mInMail
   * @param signAlias
   * @param keystore
   * @throws FOPException
   * @throws HashException
   * @throws si.jrc.msh.plugin.zpp.exception.ZPPException
   */
  private void processZPPFictionDelivery(MSHOutMail mOutMail, String sigAlias)
      throws FOPException,
      HashException,
      ZPPException,
      StorageException,
      SEDSecurityException {
    long l = LOG.logStart();

    MSHOutMail fn = createZPPFictionNotification(mOutMail, sigAlias);
    MSHInMail fi = createZPPAdviceOfDeliveryFiction(mOutMail, sigAlias);

    // do it in transaction!
    mDB.serializeInMail(fi, "ZPP-plugin");
    mDB.serializeOutMail(fn, null, "ZPP-plugin", null);
    mOutMail.setDeliveredDate(Calendar.getInstance().getTime());
    mDB.setStatusToOutMail(mOutMail, SEDOutboxMailStatus.DELIVERED, "Fiction ", "ZPP plugin", "");
    LOG.logEnd(l);
  }

  private MSHOutMail createZPPFictionNotification(MSHOutMail mOutMail,String sigAlias)
      throws ZPPException, FOPException, SEDSecurityException {

    long l = LOG.logStart();

    String sedBox = mOutMail.getReceiverEBox();

    PartyIdentitySet pis;
    try {
      pis = mpModeManager.getPartyIdentitySetForSEDAddress(sedBox);
    } catch (PModeException ex) {
      throw new ZPPException(ex.getMessage(), ex);
    }
    if (pis == null) {
      throw new ZPPException("Receiver sedbox: '" + sedBox + "' is not defined in PMode settings!");
    }
    if (pis.getExchangePartySecurity() == null) {
      throw new ZPPException("Receiver sedbox: '" + sedBox +
          "' does not have defined party serurity!");
    }

    String recSignKeyAlias = pis.getExchangePartySecurity().getSignatureCertAlias();

   
  //  SEDCertStore recSc = mmsedLookup.getSEDCertStoreByName(recKeystore);
    
    MSHOutMail moFNotification = null;
    File fEncryptedKey = null;
    File fDNViz = null;
    // create vizualization
    try {
      // vizualization
      fDNViz = StorageUtils.getNewStorageFile("pdf", "AdviceOfDeliveryNotification");
      fEncryptedKey = StorageUtils.getNewStorageFile("xml", "EncryptedKey");

    } catch (StorageException ex) {
      String msg = "Error occured while creating delivery advice file!";
      throw new ZPPException(msg, ex);
    }

    try {
      getFOP().generateVisualization(mOutMail, fDNViz,
          FOPUtils.FopTransformations.AdviceOfDeliveryFictionNotification,
          MimeConstants.MIME_PDF);

      
      PrivateKey pk = mCertBean.getPrivateKeyForAlias(recSignKeyAlias);
        X509Certificate xcert = mCertBean.getX509CertForAlias(recSignKeyAlias);
      signPDFDocument(pk, xcert, fDNViz, true);

      moFNotification = new MSHOutMail();
      moFNotification.setMessageId(Utils.getInstance().getGuidString());
      moFNotification.setService(ZPPConstants.S_ZPP_SERVICE);
      moFNotification.setAction(ZPPConstants.S_ZPP_ACTION_FICTION_NOTIFICATION);
      moFNotification.setConversationId(mOutMail.getConversationId());
      moFNotification.setSenderEBox(mOutMail.getSenderEBox());
      moFNotification.setSenderName(mOutMail.getSenderName());
      moFNotification.setRefToMessageId(mOutMail.getMessageId()); // ---??? is better no ref to - in twoway delivery ?
      moFNotification.setReceiverEBox(mOutMail.getReceiverEBox());
      moFNotification.setReceiverName(mOutMail.getReceiverName());
      moFNotification.setSubject(ZPPConstants.S_ZPP_ACTION_FICTION_NOTIFICATION);
      // prepare mail to persist
      Date dt = Calendar.getInstance().getTime();
      // set current status
      moFNotification.setStatus(SEDOutboxMailStatus.SUBMITTED.getValue());
      moFNotification.setSubmittedDate(dt);
      moFNotification.setStatusDate(dt);

      moFNotification.setMSHOutPayload(new MSHOutPayload());

      MSHOutPart mp = new MSHOutPart();
      mp.setDescription(ZPPConstants.S_ZPP_ACTION_FICTION_NOTIFICATION);
      mp.setFilepath(StorageUtils.getRelativePath(fDNViz));
      mp.setMimeType(MimeValue.MIME_PDF.getMimeType());
      moFNotification.getMSHOutPayload().getMSHOutParts().add(mp);

      // create signed delivery advice
      mp.setDescription("AdviceOfDeliveryNotification");
      mp.setFilepath(StorageUtils.getRelativePath(fDNViz));
      mp.setSha256Value(DigestUtils.getHexSha256Digest(fDNViz));
      mp.setSize(BigInteger.valueOf(fDNViz.length()));
      mp.setFilename(fDNViz.getName());
      mp.setName(mp.getFilename().substring(0, mp.getFilename().lastIndexOf(".")));
    } catch (StorageException ex) {
      String msg = "Error creating visiualization";
      LOG.logError(l, msg, null);
      throw new ZPPException(msg);
    }
    // --------------------------------------------------------
    // create encrypted key
    // get x509 keys
    String convId = mOutMail.getConversationId();
    LOG.formatedlog("Get key for conversation : '%s'", convId);
    BigInteger moID = new BigInteger(convId.substring(0, convId.indexOf("@")));
    MSHOutMail mom = mDB.getMailById(MSHOutMail.class, moID);
    mom.setDeliveredDate(mOutMail.getSentDate());

    X509Certificate recXc;
     recXc = mCertBean.getX509CertForAlias(recSignKeyAlias);
     /*
    try {
      //recXc = mkeyUtils.getTrustedCertForAlias(mCertBean.getCertificateStore(), recSignKeyAlias);
      recXc = mCertBean.getX509CertForAlias(recSignKeyAlias);
    } catch (SEDSecurityException ex) {
      String msg = String.format("Key for alias '%s' do not exists keystore'!", recSignKeyAlias);
      LOG.logError(l, msg, ex);
      throw new ZPPException(msg);
    }*/

    LOG.formatedlog("Get key for conversation : '%s'", convId);

    // get key
    Key key;
    try {
      key = getEncryptionKeyForDeliveryAdvice(moID);
    } catch (IOException ex) {
      LOG.logError(ex.getMessage(), ex);
      throw new ZPPException(ex);
    }
    LOG.log("processInZPPAdviceoFDelivery - get key" + key);
    //Element elKey;
    try (FileOutputStream fos = new FileOutputStream(fEncryptedKey)) {
      String strKey = mSedCrypto.encryptKeyWithReceiverPublicKey(key, recXc, sedBox, convId);
      fos.write(strKey.getBytes());
      fos.flush();
     
    } catch (IOException | SEDSecurityException ex) {
      LOG.logError(ex.getMessage(), ex);
      throw new ZPPException(ex);
    }

    try {

      MSHOutPart ptencKey = new MSHOutPart();
      ptencKey.setEncoding(SEDValues.ENCODING_UTF8);
      ptencKey.setMimeType(MimeValue.MIME_XML.getMimeType());
      ptencKey.setName(ZPPConstants.ELM_SIGNAL_ENCRYPTED_KEY);
      ptencKey.setDescription(ZPPConstants.ELM_SIGNAL_ENCRYPTED_KEY);
      ptencKey.setType(ZPPConstants.ELM_SIGNAL_ENCRYPTED_KEY);
      ptencKey.setFilepath(StorageUtils.getRelativePath(fEncryptedKey));
      ptencKey.setFilename(fEncryptedKey.getName());
      ptencKey.setIsEncrypted(Boolean.FALSE);
      ptencKey.setSha256Value(DigestUtils.getHexSha256Digest(fEncryptedKey));
      ptencKey.setSize(BigInteger.valueOf(fEncryptedKey.length()));
      
      moFNotification.getMSHOutPayload().getMSHOutParts().add(ptencKey);

    } catch ( StorageException ex) {
      LOG.logError(ex.getMessage(), ex);
      throw new ZPPException(ex);
    }
    LOG.logEnd(l);
    return moFNotification;

  }

  private MSHInMail createZPPAdviceOfDeliveryFiction(MSHOutMail mOutMail, String signAlias)
      throws ZPPException, FOPException, SEDSecurityException {
    long l = LOG.logStart();
    MSHInMail moADF = null;

    File fDNViz = null;

    // create vizualization
    try {
      // vizualization
      fDNViz = StorageUtils.getNewStorageFile("pdf", "AdviceOfDeliveryFiction");

    } catch (StorageException ex) {
      String msg = "Error occured while creating delivery advice file!";
      throw new ZPPException(msg, ex);
    }

    getFOP().generateVisualization(mOutMail, fDNViz,
        FOPUtils.FopTransformations.AdviceOfDeliveryFiction,
        MimeConstants.MIME_PDF);

    String domain = SEDSystemProperties.getLocalDomain();
    moADF = new MSHInMail();
    moADF.setMessageId(Utils.getInstance().getGuidString() + "@" + domain);
    moADF.setService(ZPPConstants.S_ZPP_SERVICE);
    moADF.setAction(ZPPConstants.S_ZPP_ACTION_ADVICE_OF_DELIVERY_FICTION);
    moADF.setConversationId(mOutMail.getConversationId());
    moADF.setSenderEBox("fikcija.zpp@" + domain);
    moADF.setSenderName("Laurentius ZPP fikcija");
    moADF.setRefToMessageId(mOutMail.getMessageId()); // ---??? is better no ref to - in twoway delivery ?
    moADF.setReceiverEBox(mOutMail.getSenderEBox());
    moADF.setReceiverName(mOutMail.getSenderName());
    moADF.setSubject(ZPPConstants.S_ZPP_ACTION_ADVICE_OF_DELIVERY_FICTION);
    // prepare mail to persist
    Date dt = new Date();
    // set current status
    moADF.setStatus(SEDInboxMailStatus.RECEIVED.getValue());
    moADF.setSubmittedDate(dt);
    moADF.setStatusDate(dt);
    moADF.setSentDate(dt);
    moADF.setReceivedDate(dt);

    moADF.setMSHInPayload(new MSHInPayload());

    try {
      
      PrivateKey pk = mCertBean.getPrivateKeyForAlias(signAlias);
        X509Certificate xcert = mCertBean.getX509CertForAlias(signAlias);
      signPDFDocument(pk, xcert, fDNViz, true);
      MSHInPart mp = new MSHInPart();
      mp.setDescription(ZPPConstants.S_ZPP_ACTION_FICTION_NOTIFICATION);
      mp.setFilepath(StorageUtils.getRelativePath(fDNViz));
      mp.setMimeType(MimeValue.MIME_PDF.getMimeType());
      moADF.getMSHInPayload().getMSHInParts().add(mp);

      mp.setDescription("AdviceOfDeliveryFiction");

      mp.setFilepath(StorageUtils.getRelativePath(fDNViz));
      mp.setSha256Value(DigestUtils.getHexSha256Digest(fDNViz));
      mp.setSize(BigInteger.valueOf(fDNViz.length()));
      
      mp.setFilename(fDNViz.getName());
      mp.setName(mp.getFilename().substring(0, mp.getFilename().lastIndexOf(".")));

    } catch (StorageException ex) {
      String msg = "Error occured while creating delivery advice file!";
      throw new ZPPException(msg, ex);
    }

    LOG.logEnd(l);
    return moADF;
  }

  /**
   *
   * @return
   */
  public FOPUtils getFOP() {
   if (mfpFop == null) {
      File fconf= new File(SEDSystemProperties.getPluginsFolder(),
                      ZPPConstants.SVEV_FOLDER + File.separator + ZPPConstants.FOP_CONFIG_FILENAME);

      mfpFop = new FOPUtils(fconf, SEDSystemProperties.getPluginsFolder()
                      + File.separator + ZPPConstants.SVEV_FOLDER + File.separator
                      + ZPPConstants.XSLT_FOLDER);
    }
    return mfpFop;
  }

  private File signPDFDocument(PrivateKey pk, X509Certificate xcert, File f, boolean replace) {
    long l = LOG.logStart();
    File ftmp = null;
    try {
      ftmp = StorageUtils.getNewStorageFile("pdf", "zpp-signed");
      
      SignUtils su = new SignUtils(pk, xcert);
      su.signPDF(f, ftmp, true);
      if (replace) {
        Files.move(ftmp.toPath(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
        ftmp = f;
      }
    } catch (IOException  ex) {
      LOG.logError(l, ex);
    } catch (StorageException ex) {
      Logger.getLogger(ZPPOutInterceptor.class.getName()).log(Level.SEVERE, null, ex);
    }
    return ftmp;
  }

  public Key getEncryptionKeyForDeliveryAdvice(BigInteger mailId)
      throws IOException {
    TypedQuery<SEDKey> q =
        memEManager.createNamedQuery("si.jrc.msh.sec.SEDKey.getById", SEDKey.class);
    q.setParameter("id", mailId);
    return q.getSingleResult();

  }

}
