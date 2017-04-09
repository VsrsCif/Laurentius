/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.laurentius.task;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
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

    String smtpConf = null;
    if (p.containsKey(KEY_MAIL_CONFIG_JNDI)) {
      smtpConf = p.getProperty(KEY_MAIL_CONFIG_JNDI);
    }
    if (smtpConf == null || smtpConf.trim().isEmpty()) {
      smtpConf = "java:jboss/mail/Default";
    }

    EmailData ed = validateMailParameters(p);
    String strBody = generateMailReport(p, sw);
    if (strBody != null) {

      ed.setBody(strBody);

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

  /*
   * public Properties getMailProperties() { Properties p = new Properties();
   * p.setProperty(KEY_SEDBOX, "Sedbox"); p.setProperty(KEY_EMAIL_TO, "Email address to.");
   * p.setProperty(KEY_EMAIL_FROM, "Email address from"); p.setProperty(KEY_EMAIL_SUBJECT,
   * "Email subject"); p.setProperty(KEY_MAIL_CONFIG_JNDI,
   * "Mail config jndi(def: java:jboss/mail/Default)"); return p; }
   */
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
