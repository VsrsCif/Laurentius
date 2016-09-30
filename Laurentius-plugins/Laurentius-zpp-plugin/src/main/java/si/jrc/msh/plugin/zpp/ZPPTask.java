/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.jrc.msh.plugin.zpp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.faces.application.FacesMessage;
import javax.jms.JMSException;
import javax.naming.NamingException;
import org.apache.xmlgraphics.util.MimeConstants;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.laurentius.msh.outbox.payload.MSHOutPayload;
import si.laurentius.cert.SEDCertStore;
import si.laurentius.cron.SEDTaskType;
import si.laurentius.cron.SEDTaskTypeProperty;
import si.jrc.msh.plugin.zpp.doc.DocumentSodBuilder;
import si.jrc.msh.plugin.zpp.exception.ZPPException;
import si.jrc.msh.plugin.zpp.utils.FOPUtils;
import si.jrc.msh.sec.SEDCrypto;
import si.laurentius.cert.SEDCertificate;
import si.laurentius.commons.MimeValues;
import si.laurentius.commons.SEDInboxMailStatus;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDOutboxMailStatus;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.exception.FOPException;
import si.laurentius.commons.exception.HashException;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.JMSManagerInterface;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.interfaces.TaskExecutionInterface;
import si.laurentius.commons.interfaces.exception.TaskException;
import si.laurentius.commons.utils.HashUtils;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.StringFormater;
import si.laurentius.commons.utils.Utils;
import si.laurentius.commons.utils.sec.KeystoreUtils;

/**
 *
 * @author Jože Rihtaršič
 */
@Stateless
@Local(TaskExecutionInterface.class)
public class ZPPTask implements TaskExecutionInterface {

  private static final SEDLogger LOG = new SEDLogger(ZPPTask.class);
  private static final String SIGN_ALIAS = "zpp.sign.key.alias";
  private static final String SIGN_KEYSTORE = "zpp.sign.keystore";
  private static final String REC_SEDBOX = "zpp.sedbox";

  SEDCrypto mSedCrypto = new SEDCrypto();
  HashUtils mpHU = new HashUtils();
  DocumentSodBuilder dsbSodBuilder = new DocumentSodBuilder();
  KeystoreUtils mkeyUtils = new KeystoreUtils();

  FOPUtils mfpFop = null;
  StringFormater msfFormat = new StringFormater();

  @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
  SEDDaoInterface mDB;

  @EJB(mappedName = SEDJNDI.JNDI_JMSMANAGER)
  JMSManagerInterface mJMS;

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  SEDLookupsInterface msedLookup;

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
    String keystore = "";
    if (!p.containsKey(SIGN_ALIAS)) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException,
          "Missing parameter:  '" + SIGN_ALIAS + "'!");
    } else {
      signKeyAlias = p.getProperty(SIGN_ALIAS);
    }
    if (!p.containsKey(SIGN_KEYSTORE)) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException,
          "Missing parameter:  '" + SIGN_KEYSTORE + "'!");
    } else {
      keystore = p.getProperty(SIGN_KEYSTORE);
    }

    String sedBox = "";
    if (!p.containsKey(REC_SEDBOX)) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException,
          "Missing parameter:  '" + REC_SEDBOX + "'!");
    } else {
      sedBox = p.getProperty(REC_SEDBOX);
    }

    MSHInMail mi = new MSHInMail();
    mi.setStatus(SEDInboxMailStatus.PLOCKED.getValue());
    mi.setReceiverEBox(sedBox + "@" + SEDSystemProperties.getLocalDomain());

    List<MSHInMail> lst = mDB.getDataList(MSHInMail.class, -1, 100, "Id", "ASC", mi);
    sw.append("got " + lst.size() + " mails for sedbox: '" + sedBox + "'!");

    // set status to proccess
    lst.stream().forEach((m) -> {
      try {
        mDB.setStatusToInMail(m, SEDInboxMailStatus.PROCESS, "Add message to zpp deliver proccess");
      } catch (StorageException ex) {
        String msg = String.format("Error occurred processing mail: '%s'. Err: %s.", m.getId(),
            ex.getMessage());
        LOG.logError(l, msg, ex);
        sw.append(msg);
      }
    });

    for (MSHInMail m : lst) {
      try {
        processInZPPDelivery(m, signKeyAlias, keystore);
      } catch (FOPException | HashException | ZPPException ex) {
        String msg = String.format("Error occurred processing mail: '%s'. Err: %s.", m.getId(),
            ex.getMessage());
        LOG.logError(l, msg, ex);

        sw.append(msg);
      }
    }

    sw.append("Endzpp plugin task");
    return sw.toString();
  }

  /**
   *
   * @return
   */
  @Override
  public SEDTaskType getTaskDefinition() {
    SEDTaskType tt = new SEDTaskType();
    tt.setType("zpp-plugin");
    tt.setName("ZPP plugin");
    tt.setDescription("Sign deliveryadvice for incomming mail");
    tt.getSEDTaskTypeProperties().add(createTTProperty(REC_SEDBOX, "Receiver sedbox."));
    tt.getSEDTaskTypeProperties().add(createTTProperty(SIGN_ALIAS, "Signature key alias."));
    tt.getSEDTaskTypeProperties().add(createTTProperty(SIGN_KEYSTORE, "Keystore name."));
    return tt;
  }

  private SEDTaskTypeProperty createTTProperty(String key, String desc, boolean mandatory,
      String type, String valFormat, String valList) {
    SEDTaskTypeProperty ttp = new SEDTaskTypeProperty();
    ttp.setKey(key);
    ttp.setDescription(desc);
    ttp.setMandatory(mandatory);
    ttp.setType(type);
    ttp.setValueFormat(valFormat);
    ttp.setValueList(valList);
    return ttp;
  }

  private SEDTaskTypeProperty createTTProperty(String key, String desc) {
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
  public void processInZPPDelivery(MSHInMail mInMail, String signAlias, String keystore)
      throws FOPException,
      HashException,
      ZPPException {
    long l = LOG.logStart();
    // create delivery advice
    File fDNViz = null;
    File fDA = null;
    try {
      // vizualization
      fDNViz = StorageUtils.getNewStorageFile("pdf", "AdviceOfDelivery");
      String path = fDNViz.getAbsolutePath();
      path = path.substring(0, path.lastIndexOf(".pdf")) + ".xml";
      // xml enveloped delivery advice
      fDA = new File(path); // create deliveryadvice
    } catch (StorageException ex) {
      String msg = "Error occured while creating delivery advice file!";
      throw new ZPPException(msg, ex);

    }

    getFOP().generateVisualization(mInMail, fDNViz, FOPUtils.FopTransformations.AdviceOfDelivery,
        MimeConstants.MIME_PDF);
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

    mout.setMSHOutPayload(new MSHOutPayload());

    try (FileOutputStream fos = new FileOutputStream(fDA)) {
      MSHOutPart mp = new MSHOutPart();
      mp.setDescription("DeliveryAdvice");
      mp.setFilepath(StorageUtils.getRelativePath(fDNViz));
      mp.setMimeType(MimeValues.MIME_XML.getMimeType());
      mout.getMSHOutPayload().getMSHOutParts().add(mp);

      SEDCertStore cs = msedLookup.getSEDCertStoreByName(keystore);

      SEDCertificate aliasCrt =
          msedLookup.getSEDCertificatForAlias(signAlias, cs, true);
      if (aliasCrt == null) {
        String msg = String.format("Key for alias '%s' do not exists store '%s'!", signAlias,
            keystore);
        LOG.logError(l, msg, null);
        throw new ZPPException(msg);
      }

      if (!KeystoreUtils.isCertValid(aliasCrt)) {
        String msg = "Key for alias '" + signAlias + " is not valid!";
        LOG.logError(l, msg, null);
        throw new ZPPException(msg);
      }

      // create signed delivery advice
      dsbSodBuilder.createMail(mout, fos, mkeyUtils.getPrivateKeyEntryForAlias(signAlias, cs));
      mp.setDescription("DeliveryAdvice");

      mp.setFilepath(StorageUtils.getRelativePath(fDA));
      mp.setMd5(mpHU.getMD5Hash(fDA));
      mp.setFilename(fDA.getName());
      mp.setName(mp.getFilename().substring(0, mp.getFilename().lastIndexOf(".")));

      mDB.serializeOutMail(mout, "", "ZPPDeliveryPlugin", "");

    } catch (IOException | SEDSecurityException | StorageException ex) {
      String msg = "Error occured while creating delivery advice file!";
      throw new ZPPException(msg, ex);
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

}
