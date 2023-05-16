/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.vsrs.cif.laurentius.plugin.zkp;

import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.vsrs.cif.laurentius.plugin.zkp.enums.ZKPPartType;
import si.vsrs.cif.laurentius.plugin.zkp.exception.ZKPException;
import si.vsrs.cif.laurentius.plugin.zkp.utils.ZKPUtils;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.enums.SEDInboxMailStatus;
import si.laurentius.commons.enums.SEDOutboxMailStatus;
import si.laurentius.commons.exception.FOPException;
import si.laurentius.commons.exception.HashException;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.SEDCertStoreInterface;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.inbox.payload.MSHInPart;
import si.laurentius.msh.inbox.payload.MSHInPayload;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.plugin.crontask.CronTaskDef;
import si.laurentius.plugin.crontask.CronTaskPropertyDef;
import si.laurentius.plugin.interfaces.PropertyListType;
import si.laurentius.plugin.interfaces.PropertyType;
import si.laurentius.plugin.interfaces.TaskExecutionInterface;
import si.laurentius.plugin.interfaces.exception.TaskException;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import java.io.StringWriter;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author Jože Rihtaršič
 */
@Stateless
@Local(TaskExecutionInterface.class)
public class ZKPTaskDeleteUndelivered implements TaskExecutionInterface {

    private static final SEDLogger LOG = new SEDLogger(ZKPTaskDeleteUndelivered.class);
    private static final String SIGN_ALIAS = "zkp.sign.key.alias";
    private static final String PROCESS_MAIL_COUNT = "zkp.max.mail.count";
    private static final String DAYS_TO_WAIT = "zkp.wait.days";
    private static final String MINUTES_TO_WAIT = "zkp.wait.minutes";

    @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
    SEDDaoInterface mDB;

    @EJB(mappedName = SEDJNDI.JNDI_DBCERTSTORE)
    SEDCertStoreInterface mCertBean;

    ZKPUtils mzkpZKPUtils = new ZKPUtils();

    /**
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

        if (!p.containsKey(SIGN_ALIAS)) {
            throw new TaskException(TaskException.TaskExceptionCode.InitException, "Missing parameter: '" + SIGN_ALIAS + "'!");
        }
        String signKeyAlias = p.getProperty(SIGN_ALIAS);

        int maxMailProc = Integer.parseInt(p.getProperty(PROCESS_MAIL_COUNT, "100"));
        int days = Integer.parseInt(p.getProperty(DAYS_TO_WAIT, "15"));
        int minutes = Integer.parseInt(p.getProperty(MINUTES_TO_WAIT, "0"));
        boolean dev = Boolean.parseBoolean(p.getProperty("dev.mode", "false"));

        // Gather outbound waiting messages waiting for 15 days, delete them and send undelivered message to sender inbox
        //DELETE AND RESPOND WITH ZKP MESSAGE NOT DELIVERED
        processDeleteUnresponsiveMessage(sw, signKeyAlias, maxMailProc, days, minutes, dev);

        sw.append("End zkp delete not delivered plugin task");

        LOG.logEnd(l);
        return sw.toString();
    }

    private void processDeleteUnresponsiveMessage(StringWriter sw, String signKeyAlias, int maxMailProc, int days, int minutes, boolean dev) {
        long l = LOG.logStart();
        Calendar cDatFict = Calendar.getInstance();
        cDatFict.add(Calendar.DAY_OF_MONTH, -days);
        cDatFict.add(Calendar.MINUTE, -minutes);
        if (!dev) {
            cDatFict.set(Calendar.HOUR_OF_DAY, 0);
            cDatFict.set(Calendar.SECOND, 0);
            cDatFict.set(Calendar.MILLISECOND, 0);
        }

        // get all not delivered mail
        ZKPMailFilter mi = new ZKPMailFilter();
        mi.setStatus(SEDOutboxMailStatus.SENT.getValue());
        mi.setAction(ZKPConstants.ZKP_ACTION_DELIVERY_NOTIFICATION);
        mi.setService(ZKPConstants.ZKP_A_SERVICE);
        mi.setSentDateTo(cDatFict.getTime());
        List<MSHOutMail> lst = mDB.getDataList(MSHOutMail.class, -1, maxMailProc,"Id", "ASC", mi);
        sw.append("got " + lst.size() + " mail!");

        for (MSHOutMail m : lst) {
            try {
                String domain = SEDSystemProperties.getLocalDomain();

                // delete the encryption key of message m so that it cannot be retrieved
                List<MSHOutPart> deleteParts = m.getMSHOutPayload().getMSHOutParts().stream().filter(p -> ZKPPartType.LocalEncryptionKey.equals(p.getType())).collect(Collectors.toList());
                mDB.updateOutMailPayload(m, Collections.emptyList(), Collections.emptyList(), deleteParts, SEDOutboxMailStatus.NOTDELIVERED, "Message not delivered", null, ZKPConstants.ZKP_PLUGIN_TYPE);

                MSHInMail moND = new MSHInMail();
                moND.setMessageId(Utils.getInstance().getGuidString() + "@" + domain);
                moND.setService(m.getService());
                moND.setAction(ZKPConstants.ZKP_ACTION_NOT_DELIVERED);
                moND.setConversationId(m.getConversationId());
                moND.setSenderEBox("neprevzeto.zkp@" + domain);
                moND.setSenderName("Laurentius ZKP neprevzeto");
                moND.setRefToMessageId(m.getMessageId());
                moND.setReceiverEBox(m.getSenderEBox());
                moND.setReceiverName(m.getSenderName());
                moND.setSubject(ZKPConstants.SUBJECT_NOTDELIVERED_MESSAGE);
                // prepare mail to persist
                Date dt = new Date();
                // set current status
                moND.setStatus(SEDInboxMailStatus.RECEIVED.getValue());
                moND.setSubmittedDate(dt);
                moND.setStatusDate(dt);
                moND.setSentDate(dt);
                moND.setReceivedDate(dt);

                moND.setMSHInPayload(new MSHInPayload());

                try {
                    PrivateKey pk = mCertBean.getPrivateKeyForAlias(signKeyAlias);
                    X509Certificate xcert = mCertBean.getX509CertForAlias(signKeyAlias);

                    MSHInPart mp = mzkpZKPUtils.createSignedNotDeliveredNotification(m,
                            pk,
                            xcert);

                    moND.getMSHInPayload().getMSHInParts().add(mp);

                } catch (StorageException | HashException ex) {
                    String msg = "Error occured while creating delivery advice file!";
                    throw new ZKPException(msg, ex);
                }

                // do it in transaction!
                mDB.serializeInMail(moND, ZKPConstants.ZKP_PLUGIN_TYPE);
                m.setDeliveredDate(Calendar.getInstance().getTime());
                mDB.setStatusToOutMail(m, SEDOutboxMailStatus.NOTDELIVERED, "Not delivered ", "ZKP plugin", "");
            } catch (SEDSecurityException | StorageException | FOPException | ZKPException ex) {
                String msg = String.format(
                        "Error occurred processing mail: '%s'. Err: %s.", m.getId(),
                        ex.getMessage());
                LOG.logError(l, msg, ex);

                sw.append(msg);
            }
        }

        LOG.logEnd(l);
    }

    /**
     * @return
     */
    @Override
    public CronTaskDef getDefinition() {
        CronTaskDef tt = new CronTaskDef();
        tt.setType("zkp-not-delivered-deletion");
        tt.setName("ZKP delete not delivered mail encryption key");
        tt.setDescription("Deletes encryption key of not delivered mail and sends notification to sender.");
        tt.getCronTaskPropertyDeves().add(createTTProperty(SIGN_ALIAS,
                "Signature key alias defined in keystore.", true, PropertyType.List.
                        getType(), null, PropertyListType.KeystoreCertKeys.getType()));
        tt.getCronTaskPropertyDeves().add(createTTProperty(PROCESS_MAIL_COUNT,
                "Max mail count proccesed.", true, PropertyType.Integer.getType(),
                null, null));
        tt.getCronTaskPropertyDeves().add(createTTProperty(DAYS_TO_WAIT,
                "Days to wait until deletion.", true, PropertyType.Integer.getType(),
                null, null));
        tt.getCronTaskPropertyDeves().add(createTTProperty(MINUTES_TO_WAIT,
                "Months to wait until deletion.", true, PropertyType.Integer.getType(),
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
}
