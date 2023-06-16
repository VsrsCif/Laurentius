/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.vsrs.cif.laurentius.plugin.zkp;

import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.enums.SEDInboxMailStatus;
import si.laurentius.commons.enums.SEDOutboxMailStatus;
import si.laurentius.commons.exception.FOPException;
import si.laurentius.commons.exception.HashException;
import si.laurentius.commons.exception.PModeException;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.JMSManagerInterface;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.interfaces.SEDCertStoreInterface;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.lce.KeystoreUtils;
import si.laurentius.lce.enc.SEDCrypto;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.inbox.payload.MSHInPart;
import si.laurentius.msh.inbox.payload.MSHInPayload;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.laurentius.msh.outbox.payload.MSHOutPayload;
import si.laurentius.msh.pmode.PartyIdentitySet;
import si.laurentius.plugin.crontask.CronTaskDef;
import si.laurentius.plugin.crontask.CronTaskPropertyDef;
import si.laurentius.plugin.interfaces.PropertyListType;
import si.laurentius.plugin.interfaces.PropertyType;
import si.laurentius.plugin.interfaces.TaskExecutionInterface;
import si.laurentius.plugin.interfaces.exception.TaskException;
import si.vsrs.cif.laurentius.plugin.zkp.exception.ZKPException;
import si.vsrs.cif.laurentius.plugin.zkp.utils.ZKPUtils;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import java.io.IOException;
import java.io.StringWriter;
import java.security.Key;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Jože Rihtaršič
 */
@Stateless
@Local(TaskExecutionInterface.class)
public class ZKPTaskFiction implements TaskExecutionInterface {

  private static final SEDLogger LOG = new SEDLogger(ZKPTaskFiction.class);
  private static final String SIGN_ALIAS = "zkp.sign.key.alias";
  private static final String PROCESS_MAIL_COUNT = "zkp.max.mail.count";

  @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
  SEDDaoInterface mDB;

  @EJB(mappedName = SEDJNDI.JNDI_JMSMANAGER)
  JMSManagerInterface mJMS;

  @EJB(mappedName = SEDJNDI.JNDI_DBCERTSTORE)
  SEDCertStoreInterface mCertBean;

  @EJB(mappedName = SEDJNDI.JNDI_PMODE)
  PModeInterface mpModeManager;

  SEDCrypto mSedCrypto = new SEDCrypto();
  KeystoreUtils mkeyUtils = new KeystoreUtils();
  ZKPUtils mzkpZKPUtils = new ZKPUtils();

  

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
    sw.append("Start zkp plugin task: \n");

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
    ZKPMailFilter mi = new ZKPMailFilter();
    mi.setStatus(SEDOutboxMailStatus.SENT.getValue());
    mi.setAction(ZKPConstants.ZKP_ACTION_DELIVERY_NOTIFICATION);
    mi.setService(ZKPConstants.ZKP_A_SERVICE);
    mi.setSentDateTo(cDatFict.getTime());
    List<MSHOutMail> lst = mDB.getDataList(MSHOutMail.class, -1, maxMailProc,
            "Id", "ASC", mi);
    sw.append("got " + lst.size() + " mail!");

    for (MSHOutMail m : lst) {
      try {
        processZKPFictionDelivery(m, signKeyAlias);
      } catch (SEDSecurityException | StorageException | FOPException | HashException | ZKPException ex) {
        String msg = String.format(
                "Error occurred processing mail: '%s'. Err: %s.", m.getId(),
                ex.getMessage());
        LOG.logError(l, msg, ex);

        sw.append(msg);
      }
    }

    sw.append("End zkp fiction plugin task");
    return sw.toString();
  }

  /**
   *
   * @return
   */
  @Override
  public CronTaskDef getDefinition() {
    CronTaskDef tt = new CronTaskDef();
    tt.setType("zkp-fiction-plugin");
    tt.setName("ZKP fiction delivery");
    tt.setDescription(
            "Create FictionAdviceOfDelivery for outgoing mail and send ficiton notification to "
            + "receiver");
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
   * @param mOutMail
   * @param sigAlias
   * @throws FOPException
   * @throws HashException
   * @throws ZKPException
   * @throws StorageException
   * @throws SEDSecurityException
   */
  private void processZKPFictionDelivery(MSHOutMail mOutMail, String sigAlias)
          throws FOPException,
          HashException,
          ZKPException,
          StorageException,
          SEDSecurityException {
    long l = LOG.logStart();

    MSHOutMail fn = createZKPFictionNotification(mOutMail, sigAlias);

    // do it in transaction!
    mDB.serializeOutMail(fn, , ZKPConstants.ZKP_PLUGIN_TYPE,null);
    mOutMail.setDeliveredDate(Calendar.getInstance().getTime());
    mDB.setStatusToOutMail(mOutMail, SEDOutboxMailStatus.DELIVERED, "Fiction ",
            "ZKP plugin", "");
    
    LOG.logEnd(l);
  }

  /**
   * Method created fiction notification with ecryption key.
   *
   * @param mOutMail
   * @param sigAlias
   * @return
   * @throws ZKPException
   * @throws SEDSecurityException
   */
  private MSHOutMail createZKPFictionNotification(MSHOutMail mOutMail,
          String sigAlias)
          throws ZKPException, SEDSecurityException {

    long l = LOG.logStart();

    String sedBox = mOutMail.getReceiverEBox();

    PartyIdentitySet pis;
    try {
      pis = mpModeManager.getPartyIdentitySetForSEDAddress(sedBox);
    } catch (PModeException ex) {
      throw new ZKPException(ex.getMessage(), ex);
    }
    if (pis == null) {
      throw new ZKPException(
              "Receiver sedbox: '" + sedBox + "' is not defined in PMode settings!");
    }
    if (pis.getExchangePartySecurity() == null) {
      throw new ZKPException("Receiver sedbox: '" + sedBox
              + "' does not have defined party serurity!");
    }

    MSHOutMail moFNotification = null;
    try {

      moFNotification = new MSHOutMail();
      moFNotification.setMessageId(Utils.getInstance().getGuidString());
      moFNotification.setService(ZKPConstants.ZKP_B_SERVICE);
      moFNotification.setAction(ZKPConstants.ZKP_ACTION_NOT_DELIVERED);
      moFNotification.setConversationId(mOutMail.getConversationId());
      moFNotification.setSenderEBox(mOutMail.getSenderEBox());
      moFNotification.setSenderName(mOutMail.getSenderName());
      moFNotification.setRefToMessageId(mOutMail.getMessageId());
      moFNotification.setReceiverEBox(mOutMail.getReceiverEBox());
      moFNotification.setReceiverName(mOutMail.getReceiverName());
      moFNotification.setSubject(ZKPConstants.SUBJECT_NOTDELIVERED_MESSAGE);
      // prepare mail to persist
      Date dt = Calendar.getInstance().getTime();
      // set current status
      moFNotification.setStatus(SEDOutboxMailStatus.SUBMITTED.getValue());
      moFNotification.setSubmittedDate(dt);
      moFNotification.setStatusDate(dt);

      moFNotification.setMSHOutPayload(new MSHOutPayload());

      // create AdviceOfDeliveryFictionNotification for receiver
      PrivateKey pk = mCertBean.getPrivateKeyForAlias(sigAlias);
      X509Certificate xcert = mCertBean.getX509CertForAlias(sigAlias);
      MSHOutPart mp = mzkpZKPUtils.createSignedAdviceOfFictionNotification(
              mOutMail, pk, xcert);

      // create ecrypted key:
      String recSignKeyAlias = pis.getExchangePartySecurity().
              getSignatureCertAlias();
      X509Certificate recXc = mCertBean.getX509CertForAlias(recSignKeyAlias);

      // get key
      Key key;
      try {
        key = mzkpZKPUtils.getEncKeyFromOut(mOutMail);
      } catch (StorageException ex) {
        LOG.logError(ex.getMessage(), ex);
        throw new ZKPException(ex);
      }
      MSHOutPart mpEncKey = mzkpZKPUtils.createEncryptedKey(key, recXc, sedBox,
              mOutMail.getConversationId());

      moFNotification.getMSHOutPayload().getMSHOutParts().add(mp);
      moFNotification.getMSHOutPayload().getMSHOutParts().add(mpEncKey);

    } catch (IOException | StorageException ex) {
      String msg = "Error creating visiualization";
      LOG.logError(l, msg, null);
      throw new ZKPException(msg);
    }

    LOG.logEnd(l);
    return moFNotification;

  }


  /**
   * Method generates in mail (AdviceOfDelivery) for sender of original mail.
   * Mail contains document AdviceOfDeliveryFiction, and proof of submitting
   * mail to receivers secure mail box system.
   */
  private MSHInMail createZPPAdviceOfDeliveryFiction(MSHOutMail mOutMail,
                                                     String signAlias)
          throws ZKPException, FOPException, SEDSecurityException {
    long l = LOG.logStart();
    MSHInMail moADF = null;

    String domain = SEDSystemProperties.getLocalDomain();

    moADF = new MSHInMail();
    moADF.setMessageId(Utils.getInstance().getGuidString() + "@" + domain);
    moADF.setService(ZKPConstants.ZKP_B_SERVICE);
    moADF.setAction(ZKPConstants.);
    moADF.setConversationId(mOutMail.getConversationId());
    moADF.setSenderEBox("fikcija.zpp@" + domain);
    moADF.setSenderName("Laurentius ZPP fikcija");
    moADF.setRefToMessageId(mOutMail.getMessageId());
    moADF.setReceiverEBox(mOutMail.getSenderEBox());
    moADF.setReceiverName(mOutMail.getSenderName());
    moADF.setSubject(ZKPConstants.SUBJECT_NOTDELIVERED_MESSAGE);
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
      // create AdviceOfDelivery - fiction
      PrivateKey pk = mCertBean.getPrivateKeyForAlias(signAlias);
      X509Certificate xcert = mCertBean.getX509CertForAlias(signAlias);

      MSHInPart mp = mzkpZKPUtils.createSignedAdviceOfDeliveryFiction(mOutMail,
              pk,
              xcert);

      moADF.getMSHInPayload().getMSHInParts().add(mp);

    } catch (StorageException | HashException ex) {
      String msg = "Error occured while creating delivery advice file!";
      throw new ZKPException(msg, ex);
    }

    LOG.logEnd(l);
    return moADF;
  }
}
