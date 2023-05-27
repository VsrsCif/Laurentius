/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.digics.laurentius.plugin.zkp;

import si.digics.laurentius.plugin.zkp.exception.ZKPException;
import si.digics.laurentius.plugin.zkp.utils.ZKPUtils;
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
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.laurentius.msh.outbox.payload.MSHOutPayload;
import si.laurentius.msh.pmode.PMode;
import si.laurentius.plugin.crontask.CronTaskDef;
import si.laurentius.plugin.crontask.CronTaskPropertyDef;
import si.laurentius.plugin.interfaces.PropertyListType;
import si.laurentius.plugin.interfaces.PropertyType;
import si.laurentius.plugin.interfaces.TaskExecutionInterface;
import si.laurentius.plugin.interfaces.exception.TaskException;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.io.StringWriter;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Jože Rihtaršič
 */
@Stateless
@Local(TaskExecutionInterface.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class ZKPTask implements TaskExecutionInterface {

  private static final SEDLogger LOG = new SEDLogger(ZKPTask.class);
  private static final String SIGN_ALIAS = "zpp.sign.key.alias";
  private static final String REC_SEDBOX = "zpp.sedbox";
  private static final String PROCESS_MAIL_COUNT = "zpp.max.mail.count";

  @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
  SEDDaoInterface mDB;

  @EJB(mappedName = SEDJNDI.JNDI_JMSMANAGER)
  JMSManagerInterface mJMS;

  @EJB(mappedName = SEDJNDI.JNDI_DBCERTSTORE)
  SEDCertStoreInterface mCertBean;

  @EJB(mappedName = SEDJNDI.JNDI_PMODE)
  PModeInterface mpModeManager;

  KeystoreUtils mkeyUtils = new KeystoreUtils();
  ZKPUtils   mzppZKPUtils = new ZKPUtils();

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
    mi.setService(ZKPConstants.ZKP_A_SERVICE);
    mi.setAction(ZKPConstants.S_ZKP_ACTION_DELIVERY_NOTIFICATION);
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
        processInZKPDelivery(m, signKeyAlias);
      } catch (FOPException | HashException | ZKPException ex) {
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
    tt.setName("ZKP Sign AdviceOfDelivery");
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
   * @param inMail
   * @param signAlias
   * @throws FOPException
   * @throws HashException
   * @throws ZKPException
   */
  public void processInZKPDelivery(MSHInMail inMail, String signAlias)
          throws FOPException,
          HashException,
          ZKPException {
    long l = LOG.logStart();
    // create delivery advice

    MSHOutMail mout = new MSHOutMail();
    mout.setMessageId(Utils.getUUIDWithLocalDomain());
    mout.setService(ZKPConstants.ZKP_A_SERVICE);
    mout.setAction(ZKPConstants.S_ZKP_ACTION_ADVICE_OF_DELIVERY);
    mout.setConversationId(inMail.getConversationId());
    mout.setSenderEBox(inMail.getReceiverEBox());
    mout.setSenderName(inMail.getReceiverName());
    mout.setRefToMessageId(inMail.getMessageId());
    mout.setReceiverEBox(inMail.getSenderEBox());
    mout.setReceiverName(inMail.getSenderName());
    mout.setSubject(ZKPConstants.S_ZKP_ACTION_ADVICE_OF_DELIVERY);
    // prepare mail to persist
    Date dt = new Date();
    // set current status
    mout.setStatus(SEDOutboxMailStatus.SUBMITTED.getValue());
    mout.setSubmittedDate(dt);
    mout.setStatusDate(dt);

    try {

      // sign with receiver certificate 
      PrivateKey pk = mCertBean.getPrivateKeyForAlias(signAlias);
      X509Certificate xcert = mCertBean.getX509CertForAlias(signAlias);

      MSHOutPart mp = mzppZKPUtils.createSignedAdviceOfDelivery(inMail, pk,
              xcert);
      mout.setMSHOutPayload(new MSHOutPayload());
      mout.getMSHOutPayload().getMSHOutParts().add(mp);

      PMode pmd = mpModeManager.getPModeMSHOutMail(mout);

      mDB.serializeOutMail(mout, "", ZKPConstants.ZKP_PLUGIN_TYPE, pmd);
      mDB.setStatusToInMail(inMail, SEDInboxMailStatus.PREADY,
              "AdviceOfDelivery created and submitted to out queue");
    } catch (SEDSecurityException | StorageException | PModeException ex) {
      String msg = ex.getMessage();
      LOG.logError(l, msg, ex);
      throw new ZKPException(msg);
    }

    LOG.logEnd(l);
  }

}
