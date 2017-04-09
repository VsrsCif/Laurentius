/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.jrc.msh.plugin.zpp;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import org.apache.xmlgraphics.util.MimeConstants;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.laurentius.msh.outbox.payload.MSHOutPayload;
import si.jrc.msh.plugin.zpp.doc.DocumentSodBuilder;
import si.jrc.msh.plugin.zpp.exception.ZPPException;
import si.jrc.msh.plugin.zpp.utils.FOPUtils;
import si.laurentius.lce.enc.SEDCrypto;
import si.laurentius.lce.sign.pdf.SignUtils;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.enums.SEDInboxMailStatus;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.enums.SEDOutboxMailStatus;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.exception.FOPException;
import si.laurentius.commons.exception.HashException;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.JMSManagerInterface;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.StringFormater;
import si.laurentius.commons.utils.Utils;
import si.laurentius.lce.KeystoreUtils;
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
@TransactionManagement(TransactionManagementType.BEAN)
public class ZPPTask implements TaskExecutionInterface {

  private static final SEDLogger LOG = new SEDLogger(ZPPTask.class);
  private static final String SIGN_ALIAS = "zpp.sign.key.alias";
  private static final String REC_SEDBOX = "zpp.sedbox";
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

    String sedBox = "";
    if (!p.containsKey(REC_SEDBOX)) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException,
              "Missing parameter:  '" + REC_SEDBOX + "'!");
    } else {
      sedBox = p.getProperty(REC_SEDBOX);
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

    MSHInMail mi = new MSHInMail();
    mi.setStatus(SEDInboxMailStatus.PLOCKED.getValue());
    mi.setService(ZPPConstants.S_ZPP_SERVICE);
    mi.setAction(ZPPConstants.S_ZPP_ACTION_DELIVERY_NOTIFICATION);
    mi.setReceiverEBox(sedBox + "@" + SEDSystemProperties.getLocalDomain());

    List<MSHInMail> lst = mDB.
            getDataList(MSHInMail.class, -1, maxMailProc, "Id", "ASC", mi);
    sw.append("got " + lst.size() + " mails for sedbox: '" + sedBox + "'!");

    // set status to proccess
    lst.stream().forEach((m) -> {
      try {
        mDB.setStatusToInMail(m, SEDInboxMailStatus.PROCESS,
                "Add message to zpp deliver proccess");
      } catch (StorageException ex) {
        String msg = String.format(
                "Error occurred processing mail: '%s'. Err: %s.", m.getId(),
                ex.getMessage());
        LOG.logError(l, msg, ex);
        sw.append(msg);
      }
    });

    for (MSHInMail m : lst) {
      try {
        processInZPPDelivery(m, signKeyAlias);
      } catch (FOPException | HashException | ZPPException ex) {
        String msg = String.format(
                "Error occurred processing mail: '%s'. Err: %s.", m.getId(),
                ex.getMessage());
        LOG.logError(l, msg, ex);

        sw.append(msg);
      }
    }

    sw.append("End zpp plugin task");
    return sw.toString();
  }

  /**
   *
   * @return
   */
  @Override
  public CronTaskDef getDefinition() {

    CronTaskDef tt = new CronTaskDef();
    tt.setType("zpp-plugin");
    tt.setName("ZPP Sign AdviceOfDelivery");
    tt.setDescription("Create and Sign adviceOfDelivery for incomming mail");
    tt.getCronTaskPropertyDeves().add(createTTProperty(REC_SEDBOX,
            "Receiver sedbox (without domain).", true, PropertyType.List.
                    getType(), null, PropertyListType.LocalBoxes.getType()));
    tt.getCronTaskPropertyDeves().add(createTTProperty(SIGN_ALIAS,
            "Signature key alias defined in keystore.", true, PropertyType.List.
                    getType(), null, PropertyListType.KeystoreCertKeys.getType()));
    tt.getCronTaskPropertyDeves().add(createTTProperty(PROCESS_MAIL_COUNT,
            "Max mail count proccesed.", true, PropertyType.Integer.getType(),
            null, null));
    return tt;
  }

  private CronTaskPropertyDef createTTProperty(String key, String desc,
          boolean mandatory,
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

  /**
   *
   * @param mInMail
   * @param signAlias
   * @param keystore
   * @throws FOPException
   * @throws HashException
   * @throws si.jrc.msh.plugin.zpp.exception.ZPPException
   */
  public void processInZPPDelivery(MSHInMail mInMail, String signAlias)
          throws FOPException,
          HashException,
          ZPPException {
    long l = LOG.logStart();
    // create delivery advice

    MSHOutMail mout = new MSHOutMail();
    mout.setMessageId(Utils.getInstance().getGuidString());
    mout.setService(ZPPConstants.S_ZPP_SERVICE);
    mout.setAction(ZPPConstants.S_ZPP_ACTION_ADVICE_OF_DELIVERY);
    mout.setConversationId(mInMail.getConversationId());
    mout.setSenderEBox(mInMail.getReceiverEBox());
    mout.setSenderName(mInMail.getReceiverName());
    mout.setRefToMessageId(mInMail.getMessageId());
    mout.setReceiverEBox(mInMail.getSenderEBox());
    mout.setReceiverName(mInMail.getSenderName());
    mout.setSubject(ZPPConstants.S_ZPP_ACTION_ADVICE_OF_DELIVERY);
    // prepare mail to persist
    Date dt = new Date();
    // set current status
    mout.setStatus(SEDOutboxMailStatus.SUBMITTED.getValue());
    mout.setSubmittedDate(dt);
    mout.setStatusDate(dt);

    File fDNViz = null;
    try {
      fDNViz = StorageUtils.getNewStorageFile("pdf", "AdviceOfDelivery");

      getFOP().generateVisualization(mInMail, fDNViz,
              FOPUtils.FopTransformations.AdviceOfDelivery,
              MimeConstants.MIME_PDF);

      // sign with receiver certificate 
      PrivateKey pk = mCertBean.getPrivateKeyForAlias(signAlias);
      X509Certificate xcert = mCertBean.getX509CertForAlias(signAlias);

      signPDFDocument(pk, xcert, fDNViz);
      // sign with systemCertificate

      mout.setMSHOutPayload(new MSHOutPayload());
      MSHOutPart mp = new MSHOutPart();
      mp.setDescription("DeliveryAdvice");
      mp.setMimeType(MimeValue.MIME_PDF.getMimeType());
      mout.getMSHOutPayload().getMSHOutParts().add(mp);
      mp.setSha256Value(DigestUtils.getHexSha256Digest(fDNViz));
      mp.setSize(BigInteger.valueOf(fDNViz.length()));
      mp.setFilename(fDNViz.getName());
      mp.setFilepath(StorageUtils.getRelativePath(fDNViz));
      mp.setName(mp.getFilename().
              substring(0, mp.getFilename().lastIndexOf(".")));

      mDB.serializeOutMail(mout, "", "ZPPDeliveryPlugin", "");
      mDB.setStatusToInMail(mInMail, SEDInboxMailStatus.PREADY,
              "AdviceOfDelivery created and submitted to out queue");
    } catch (SEDSecurityException | StorageException ex) {
      String msg = ex.getMessage();
      LOG.logError(l, msg, ex);
      throw new ZPPException(msg);
    }

    LOG.logEnd(l);
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

  private void signPDFDocument(PrivateKey pk, X509Certificate xcert, File f) {
    try {
      File ftmp = File.createTempFile("tmp_sign", ".pdf");

      SignUtils su = new SignUtils(pk, xcert);
      su.signPDF(f, ftmp, true);
      Files.move(ftmp.toPath(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);

    } catch (IOException ex) {
      Logger.getLogger(ZPPOutInterceptor.class
              .getName()).
              log(Level.SEVERE, null, ex);
    }
  }
}
