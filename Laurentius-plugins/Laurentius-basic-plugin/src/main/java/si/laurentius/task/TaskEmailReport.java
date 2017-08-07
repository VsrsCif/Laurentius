/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.laurentius.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import javax.ejb.EJB;
import javax.mail.MessagingException;
import javax.naming.NamingException;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.email.EmailData;
import si.laurentius.commons.email.EmailUtils;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.interfaces.SEDReportInterface;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StringFormater;
import si.laurentius.commons.utils.Utils;
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
public abstract class TaskEmailReport implements TaskExecutionInterface 
    
{

  /**
     *
     */
  public static final String KEY_EMAIL_FROM = "email.from";

  /**
     *
     */
  public static final  String KEY_EMAIL_SUBJECT = "email.subject";

  /**
     *
     */
  public static final  String KEY_EMAIL_TO = "email.to";

  /**
     *
     */
  public static final  String KEY_MAIL_CONFIG_JNDI = "mail.config.jndi";

  /**
     *
     */
  public static final  String KEY_SEDBOX = "sedbox";
  
  public static final String KEY_REPORT_WRITE_TO_FOLDER = "report.status.write.folder";
  
  final SimpleDateFormat SDF_YYYYMMDD_HHMISS = new SimpleDateFormat(
          "yyyyMMdd_HHmmss");
  

  /**
     *
     */
  protected static final SEDLogger LOG = new SEDLogger(TaskEmailStatusReport.class);
  final SimpleDateFormat SDF_DD_MM_YYY_HH_MI = new SimpleDateFormat("dd.MM.yyyy HH:mm");
  @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
  SEDDaoInterface mdao;
  @EJB(mappedName = SEDJNDI.JNDI_SEDREPORTS)
  SEDReportInterface mdaoReports;

  /**
   *
   * @param key
   * @param desc
   * @param mandatory
   * @param type
   * @param valFormat
   * @param valList
   * @return
   */
  protected CronTaskPropertyDef createTTProperty(String key, String desc, boolean mandatory,
      String type, String valFormat, String valList, String defValue) {
    CronTaskPropertyDef ttp = new CronTaskPropertyDef();
    ttp.setKey(key);
    ttp.setDescription(desc);
    ttp.setMandatory(mandatory);
    ttp.setType(type);
    ttp.setValueFormat(valFormat);
    ttp.setValueList(valList);
     ttp.setDefValue(defValue);
    return ttp;
  }

  /**
   *
   * @param p
   * @return
   */
  @Override
  public String executeTask(Properties p) throws TaskException {
    long l = LOG.logStart();
    EmailUtils memailUtil = new EmailUtils();
    StringWriter sw = new StringWriter();
    sw.append("Start report task: ");
    
     String writeToFolder = null;

    String smtpConf = null;
    if (p.containsKey(KEY_MAIL_CONFIG_JNDI)) {
      smtpConf = p.getProperty(KEY_MAIL_CONFIG_JNDI);
    }
    if (smtpConf == null || smtpConf.trim().isEmpty()) {
      smtpConf = "java:jboss/mail/Default";
    }
    
    if (p.containsKey(KEY_REPORT_WRITE_TO_FOLDER)) {

      writeToFolder = p.getProperty(KEY_REPORT_WRITE_TO_FOLDER);
      if (!Utils.isEmptyString(writeToFolder)) {
        writeToFolder = StringFormater.replaceProperties(writeToFolder);
      }
    }

    EmailData ed = validateMailParameters(p);
    String strBody = generateMailReport(p, sw);
    if (Utils.isEmptyString(strBody)) {
      ed.setBody(strBody);
      
      if (!Utils.isEmptyString(writeToFolder)) {
      File f = new File(writeToFolder);
      if (!f.exists()) {
        f.mkdir();
      }

      
      File fRep = new File(f, "report_"+getDefinition().getType() + "_" + SDF_YYYYMMDD_HHMISS.format(Calendar.
              getInstance().getTime()));
      try (FileOutputStream fos = new FileOutputStream(fRep)) {
        fos.write(strBody.getBytes());
        fos.flush();
      } catch (IOException ex) {
        LOG.logError("Error occured while writting report to file", ex);
      }

    }


      try {
        sw.append("Submit mail\n");
        memailUtil.sendMailMessage(ed, smtpConf);
      } catch (MessagingException | NamingException | IOException ex) {
        LOG.logError(l, "Error submitting report", ex);
        throw new TaskException(TaskException.TaskExceptionCode.ProcessException,
            "Error submitting report: " + ex.getMessage(), ex);
      }
    } else {
      sw.append("Mail not submitted - nothing to submit\n");
    }
    return sw.toString();
  }

  abstract String generateMailReport(Properties p, StringWriter sw) throws TaskException;

 
  /**
   *
   * @return
   */
  @Override
  public CronTaskDef getDefinition() {
    CronTaskDef tt = new CronTaskDef();
    tt.setType("");
    tt.setName("");
    tt.setDescription("");
    tt.getCronTaskPropertyDeves().add(createTTProperty(KEY_SEDBOX, 
            "Receiver sedbox (without domain).", true, PropertyType.List.
                    getType(), null, PropertyListType.LocalBoxes.getType(), null));
    tt.getCronTaskPropertyDeves().add(
        createTTProperty(KEY_EMAIL_TO, "Receiver email addresses, separated by comma.", true,
                    PropertyType.String.
                    getType(), null, null, "receiver.one@not.exists.com,receiver.two@not.exists.com"));
    tt.getCronTaskPropertyDeves().add(createTTProperty(KEY_EMAIL_FROM, "Sender email address", true,
                    PropertyType.String.
                    getType(), null, null, "change.me@not.exists.com"));
    tt.getCronTaskPropertyDeves().add(createTTProperty(KEY_EMAIL_SUBJECT, "EMail subject.", true,
                    PropertyType.String.
                    getType(), null, null, "[Laurentius] test mail"));
    tt.getCronTaskPropertyDeves().add(
        createTTProperty(KEY_MAIL_CONFIG_JNDI, 
                "Mail config jndi (def: java:jboss/mail/Default)", true,
                    "string", null, null, "java:jboss/mail/Default"));
    
     
    return tt;
  }

  /**
   *
   * @param p
   * @return
   * @throws TaskException
   */
  public EmailData validateMailParameters(Properties p) throws TaskException {

    String emailTo = null;
    String emailFrom = null;
    String emailSubject = null;
      String writeToFolder = null;

    if (!p.containsKey(KEY_EMAIL_TO)) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException,
          "Missing parameter:  '" + KEY_EMAIL_TO + "'!");
    } else {
      emailTo = p.getProperty(KEY_EMAIL_TO);
    }
    if (!p.containsKey(KEY_EMAIL_FROM)) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException,
          "Missing parameter:  '" + KEY_EMAIL_FROM + "'!");
    } else {
      emailFrom = p.getProperty(KEY_EMAIL_FROM);
    }
    if (!p.containsKey(KEY_EMAIL_SUBJECT)) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException,
          "Missing parameter:  '" + KEY_EMAIL_SUBJECT + "'!");
    } else {
      emailSubject = p.getProperty(KEY_EMAIL_SUBJECT);
    }
    

    EmailData emd = new EmailData(emailTo, null, emailSubject, null);
    emd.setEmailSenderAddress(emailFrom);
    return emd;

  }

}
