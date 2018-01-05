/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.jrc.msh.plugin.zpp;

import java.io.IOException;
import java.io.StringWriter;
import java.security.Key;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.xml.bind.JAXBException;
import si.jrc.msh.plugin.zpp.entities.LastDeliveredMail;
import si.jrc.msh.plugin.zpp.enums.FopTransformation;
import si.jrc.msh.plugin.zpp.enums.ZPPPartType;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.laurentius.msh.outbox.payload.MSHOutPayload;

import si.jrc.msh.plugin.zpp.exception.ZPPException;
import si.jrc.msh.plugin.zpp.utils.ZPPUtils;
import si.laurentius.lce.enc.SEDCrypto;
import si.laurentius.commons.enums.SEDInboxMailStatus;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.enums.SEDOutboxMailStatus;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.exception.FOPException;
import si.laurentius.commons.exception.HashException;
import si.laurentius.commons.exception.PModeException;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.JMSManagerInterface;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.lce.KeystoreUtils;
import si.laurentius.msh.inbox.payload.MSHInPart;
import si.laurentius.msh.inbox.payload.MSHInPayload;
import si.laurentius.msh.pmode.PartyIdentitySet;
import si.laurentius.commons.interfaces.SEDCertStoreInterface;
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
public class ZPPTaskFictionByLastDelivery implements TaskExecutionInterface {

  private static final SEDLogger LOG = new SEDLogger(
          ZPPTaskFictionByLastDelivery.class);
  private static final String SIGN_ALIAS = "zpp.sign.key.alias";
  private static final String MINUTES_DELAY = "zpp.fiction.p6.delayInMinutes";

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
  ZPPUtils mzppZPPUtils = new ZPPUtils();

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
    int iDelayMin = -10;
    if (!p.containsKey(SIGN_ALIAS)) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException,
              "Missing parameter:  '" + SIGN_ALIAS + "'!");
    } else {
      signKeyAlias = p.getProperty(SIGN_ALIAS);
    }

    if (p.containsKey(MINUTES_DELAY)) {
      String val = p.getProperty(MINUTES_DELAY);
      if (!Utils.isEmptyString(val)) {
        try {
          iDelayMin = Integer.parseInt(val) * -1;
        } catch (NumberFormatException nfe) {
          LOG.logError(String.format(
                  "Error parameter '%s'. Value '%s' is not a number Mail count 100 is setted!",
                  MINUTES_DELAY, val), nfe);
        }
      }
    }



    List<LastDeliveredMail> lstLastDM = getLastDeliveredMail();
    LOG.formatedWarning(
            "Execute ZPPTaskFictionByLastDelivery delivered mail %d ",
            lstLastDM.size());
    for (LastDeliveredMail ldm : lstLastDM) {
      fictionalDeliveryMail(ldm, signKeyAlias, iDelayMin);
    }

    sw.append("End zpp fiction plugin task");
    return sw.toString();
  }

  public List<LastDeliveredMail> getLastDeliveredMail() {

    Calendar c = Calendar.getInstance();
    c.add(Calendar.DAY_OF_MONTH, -15);
    String hql
            = "select new si.jrc.msh.plugin.zpp.entities.LastDeliveredMail(om.ReceiverEBox, max(om.DeliveredDate) AS DeliveredDate) "
            + "	from MSHOutMail om,  MSHInMail im "
            + "	where om.ConversationId = im.ConversationId "
            + "	 and om.Service =im.Service "
            + "	 and om.Service = :service "
            + "	  and om.Action=:outAction "
            + "	  and im.Action=:inAction "
            + "	  and om.ReceivedDate >=:receivedDate "
            + "  and om.DeliveredDate is not null "
            + "	  group by om.ReceiverEBox";

    Map<String, Object> prms = new HashMap<>();
    prms.put("service", ZPPConstants.S_ZPP_SERVICE);
    prms.put("outAction", ZPPConstants.S_ZPP_ACTION_DELIVERY_NOTIFICATION);
    prms.put("inAction", ZPPConstants.S_ZPP_ACTION_ADVICE_OF_DELIVERY);
    prms.put("receivedDate", c.getTime());

    return mDB.getDataList(LastDeliveredMail.class, hql, prms);

  }

  public void fictionalDeliveryMail(LastDeliveredMail da, String signKeyAlias, int delayMinutes) {
    long l = LOG.logStart();
    Calendar cStart = Calendar.getInstance();
    cStart.add(Calendar.DAY_OF_MONTH, -15);

    Date lastDate = da.getDeliveredDate();
    Calendar cld = Calendar.getInstance();
    cld.setTime(lastDate);
    cld.add(Calendar.MINUTE, delayMinutes);
    

    String hql2
            = "select om"
            + " from MSHOutMail  om "
            + "  where om.Service = :service "
            + "  and om.Action=:outAction "
            + "  and om.ReceivedDate > :receivedDate "
            + "  and om.ReceivedDate < :lastDeliveredDate "
            + "  and om.ReceiverEBox = :receiverEBox "
            + "  and om.Status = :status ";

    LOG.formatedWarning("Get outmail from %s to %s for receiverBox %s",
            cStart.getTime().toString(), cld.getTime().toString(), da.
            getReceiverEBox());

    Map<String, Object> prms = new HashMap<>();
    prms.put("service", ZPPConstants.S_ZPP_SERVICE);
    prms.put("outAction", ZPPConstants.S_ZPP_ACTION_DELIVERY_NOTIFICATION);
    prms.put("status", SEDOutboxMailStatus.SENT.getValue());
    prms.put("receivedDate", cStart.getTime());
    prms.put("lastDeliveredDate", cld.getTime());
    prms.put("receiverEBox", da.getReceiverEBox());

    List<MSHOutMail> lst = mDB.getDataList(MSHOutMail.class, hql2, prms);

    LOG.formatedWarning("Got %d mail to deliver", lst.size());

    for (MSHOutMail m : lst) {
      try {
        m.setDeliveredDate(lastDate);
        processZPPFictionDelivery(m, signKeyAlias);
      } catch (SEDSecurityException | StorageException | FOPException | HashException | ZPPException ex) {
        String msg = String.format(
                "Error occurred processing mail: '%s'. Err: %s.", m.getId(),
                ex.getMessage());
        LOG.logError(l, msg, ex);

      }
    }

  }

  /**
   *
   * @return
   */
  @Override
  public CronTaskDef getDefinition() {
    CronTaskDef tt = new CronTaskDef();
    tt.setType("zpp-fiction-by-last-delivery");
    tt.setName("ZPP fiction by Last delivery");
    tt.setDescription(
            "Create FictionAdviceOfDelivery for outgoing mail and send ficiton notification to "
            + "receiver");
    tt.getCronTaskPropertyDeves().add(createTTProperty(SIGN_ALIAS,
            "Signature key alias defined in keystore.", true, PropertyType.List.
                    getType(), null, PropertyListType.KeystoreCertKeys.getType()));
    tt.getCronTaskPropertyDeves().add(createTTProperty(MINUTES_DELAY,
            "Delay in minutes from last delivery.", true, PropertyType.Integer.getType(),
            null, "-10"));

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
    mDB.setStatusToOutMail(mOutMail, SEDOutboxMailStatus.DELIVERED, "Fiction ",
            "ZPP plugin", "");
    LOG.logEnd(l);
  }

  /**
   * Method created fiction notification with ecryption key.
   *
   * @param mOutMail
   * @param sigAlias
   * @return
   * @throws ZPPException
   * @throws SEDSecurityException
   */
  private MSHOutMail createZPPFictionNotification(MSHOutMail mOutMail,
          String sigAlias)
          throws ZPPException, SEDSecurityException {

    long l = LOG.logStart();

    String sedBox = mOutMail.getReceiverEBox();

    PartyIdentitySet pis;
    try {
      pis = mpModeManager.getPartyIdentitySetForSEDAddress(sedBox);
    } catch (PModeException ex) {
      throw new ZPPException(ex.getMessage(), ex);
    }
    if (pis == null) {
      throw new ZPPException(
              "Receiver sedbox: '" + sedBox + "' is not defined in PMode settings!");
    }
    if (pis.getExchangePartySecurity() == null) {
      throw new ZPPException("Receiver sedbox: '" + sedBox
              + "' does not have defined party serurity!");
    }

    MSHOutMail moFNotification = null;
    try {

      moFNotification = new MSHOutMail();
      moFNotification.setMessageId(Utils.getInstance().getGuidString());
      moFNotification.setService(ZPPConstants.S_ZPP_SERVICE);
      moFNotification.setAction(ZPPConstants.S_ZPP_ACTION_FICTION_NOTIFICATION);
      moFNotification.setConversationId(mOutMail.getConversationId());
      moFNotification.setSenderEBox(mOutMail.getSenderEBox());
      moFNotification.setSenderName(mOutMail.getSenderName());
      moFNotification.setRefToMessageId(mOutMail.getMessageId());
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

      // create AdviceOfDeliveryFictionNotification for receiver
      PrivateKey pk = mCertBean.getPrivateKeyForAlias(sigAlias);
      X509Certificate xcert = mCertBean.getX509CertForAlias(sigAlias);
      MSHOutPart mp = mzppZPPUtils.createMSHOutPart(
              mOutMail, ZPPPartType.AdviceOfDeliveryFiction,
              FopTransformation.AdviceOfDeliveryFictionNotification_6Odst,
              pk, xcert);

      // create ecrypted key:
      String recSignKeyAlias = pis.getExchangePartySecurity().
              getSignatureCertAlias();
      X509Certificate recXc = mCertBean.getX509CertForAlias(recSignKeyAlias);

      // get key
      Key key;
      try {
        key = mzppZPPUtils.getEncKeyFromOut(mOutMail);
      } catch (StorageException ex) {
        LOG.logError(ex.getMessage(), ex);
        throw new ZPPException(ex);
      }
      MSHOutPart mpEncKey = mzppZPPUtils.createEncryptedKey(key, recXc, sedBox,
              mOutMail.getConversationId());

      moFNotification.getMSHOutPayload().getMSHOutParts().add(mp);
      moFNotification.getMSHOutPayload().getMSHOutParts().add(mpEncKey);

    } catch (IOException | StorageException ex) {
      String msg = "Error creating visiualization";
      LOG.logError(l, msg, ex);
      throw new ZPPException(msg);
    }

    LOG.logEnd(l);
    return moFNotification;

  }

  /**
   * Method generates in mail (AdviceOfDelivery) for sender of original mail.
   * Mail contains document AdviceOfDeliveryFiction, and proof of submitting
   * mail to receivers secure mail box system.
   *
   * @param mOutMail
   * @param signAlias
   * @return
   * @throws ZPPException
   * @throws FOPException
   * @throws SEDSecurityException
   */
  private MSHInMail createZPPAdviceOfDeliveryFiction(MSHOutMail mOutMail,
          String signAlias)
          throws ZPPException, FOPException, SEDSecurityException {
    long l = LOG.logStart();
    MSHInMail moADF = null;

    String domain = SEDSystemProperties.getLocalDomain();

    moADF = new MSHInMail();
    moADF.setMessageId(Utils.getInstance().getGuidString() + "@" + domain);
    moADF.setService(ZPPConstants.S_ZPP_SERVICE);
    moADF.setAction(ZPPConstants.S_ZPP_ACTION_ADVICE_OF_DELIVERY_FICTION);
    moADF.setConversationId(mOutMail.getConversationId());
    moADF.setSenderEBox("fikcija.zpp@" + domain);
    moADF.setSenderName("Laurentius ZPP fikcija");
    moADF.setRefToMessageId(mOutMail.getMessageId());
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
      // create AdviceOfDelivery - fiction
      PrivateKey pk = mCertBean.getPrivateKeyForAlias(signAlias);
      X509Certificate xcert = mCertBean.getX509CertForAlias(signAlias);

      MSHInPart mp = mzppZPPUtils
              .createMSHInPart(mOutMail, ZPPPartType.AdviceOfDeliveryFiction,
                      FopTransformation.AdviceOfDeliveryFiction_6Odst,
                       pk, xcert);

      moADF.getMSHInPayload().getMSHInParts().add(mp);

    } catch (StorageException | HashException ex) {
      String msg = "Error occured while creating delivery advice file!";
      throw new ZPPException(msg, ex);
    }

    LOG.logEnd(l);
    return moADF;
  }
}
