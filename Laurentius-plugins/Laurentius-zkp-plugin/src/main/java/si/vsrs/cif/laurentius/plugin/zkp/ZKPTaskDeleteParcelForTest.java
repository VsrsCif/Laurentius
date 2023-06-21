/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.vsrs.cif.laurentius.plugin.zkp;

import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.enums.SEDInboxMailStatus;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.msh.inbox.mail.MSHInMail;
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
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

/**
 * @author Jože Rihtaršič
 */
@Stateless
@Local(TaskExecutionInterface.class)
public class ZKPTaskDeleteParcelForTest implements TaskExecutionInterface {

    private static final SEDLogger LOG = new SEDLogger(ZKPTaskDeleteParcelForTest.class);
    private static final String SIGN_ALIAS = "zkp.sign.key.alias";
    private static final String PROCESS_MAIL_COUNT = "zkp.max.mail.count";
    private static final String DAYS_TO_WAIT = "zkp.wait.days";
    private static final String MINUTES_TO_WAIT = "zkp.wait.minutes";
    private static final String REC_SEDBOX = "zkp.sedbox";

    @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
    SEDDaoInterface mDB;

    /**
     * @param p
     * @return
     * @throws TaskException
     */
    @Override
    public String executeTask(Properties p) throws TaskException {

        long l = LOG.logStart();
        StringWriter sw = new StringWriter();
        sw.append("Start zkp plugin task: \n");

        int maxMailProc = Integer.parseInt(p.getProperty(PROCESS_MAIL_COUNT, "100"));
        int days = Integer.parseInt(p.getProperty(DAYS_TO_WAIT, "15"));
        int minutes = Integer.parseInt(p.getProperty(MINUTES_TO_WAIT, "0"));
        boolean dev = Boolean.parseBoolean(p.getProperty("dev.mode", "false"));

        if (!p.containsKey(REC_SEDBOX)) {
            throw new TaskException(TaskException.TaskExceptionCode.InitException,
                    "Missing parameter:  '" + REC_SEDBOX + "'!");
        }
        String sedBox = p.getProperty(REC_SEDBOX);

        // Gather inbound waiting messages waiting for 15 days and delete them

        // DELETE A NON-DELIVERED PACKAGE THAT TIMED OUT
        processDeleteWaitingMessage(sedBox, sw, maxMailProc, days, minutes, dev);

        sw.append("End zkp plugin delete task");
        LOG.logEnd(l);

        return sw.toString();
    }

    private void processDeleteWaitingMessage(String sedBox, StringWriter sw, int maxMailProc, int days, int minutes, boolean dev) {
        long l = LOG.logStart();
        LOG.logWarn(l, "PROCESSING DELETE WAITING MESSAGE RECEIVER END", null);
        Calendar cDatFict = Calendar.getInstance();
        cDatFict.add(Calendar.DAY_OF_MONTH, -days);
        cDatFict.add(Calendar.MINUTE, -minutes);
        if(!dev) {
            cDatFict.set(Calendar.HOUR_OF_DAY, 0);
            cDatFict.set(Calendar.SECOND, 0);
            cDatFict.set(Calendar.MILLISECOND, 0);
        }

        // get all not delivered mail
        ZKPMailFilter mi = new ZKPMailFilter();
        mi.setStatus(SEDInboxMailStatus.PLOCKED.getValue());
        mi.setAction(ZKPConstants.ZKP_ACTION_DELIVERY_NOTIFICATION);
        mi.setService(ZKPConstants.ZKP_A_SERVICE);
        mi.setReceivedDateTo(cDatFict.getTime());
        mi.setReceiverEBox(sedBox + "@" + SEDSystemProperties.getLocalDomain());
        List<MSHInMail> lst = mDB.getDataList(MSHInMail.class, -1, maxMailProc,
                "Id", "ASC", mi);
        sw.append("got " + lst.size() + " mail!");
        LOG.formatedWarning("got %d mail!", lst.size());
        for (MSHInMail m : lst) {
            try {
                mDB.setStatusToInMail(m, SEDInboxMailStatus.DELETED, "Deleted message - delivery timeout");
            } catch (StorageException ex) {
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
        tt.setType("zkp-delete-parcel-test");
        tt.setName("ZKP delivery - test delete");
        tt.setDescription("Task deleting the parcel from the receiving end");
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
        tt.getCronTaskPropertyDeves().add(createTTProperty(REC_SEDBOX,
                "Receiver sedbox (without domain).", true, PropertyType.List.
                        getType(), null, PropertyListType.LocalBoxes.getType()));

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
