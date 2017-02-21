/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.laurentius.task;

import java.io.StringWriter;
import java.util.List;
import java.util.Properties;
import javax.ejb.Local;
import javax.ejb.Stateless;
import si.laurentius.commons.SEDInboxMailStatus;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.plugin.crontask.CronTaskDef;
import si.laurentius.plugin.interfaces.TaskExecutionInterface;
import si.laurentius.plugin.interfaces.exception.TaskException;
import si.laurentius.report.SEDReportBoxStatus;
import si.laurentius.report.Status;

/**
 *
 * @author Jože Rihtaršič
 */
@Stateless
@Local(TaskExecutionInterface.class)
public class TaskEmailStatusReport extends TaskEmailReport {

  /**
   *
   * @param p
   * @param sw
   * @return
   */
  @Override
  public String generateMailReport(Properties p, StringWriter sw) throws TaskException {

    String sedbox = null;
    if (!p.containsKey(KEY_SEDBOX)) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException,
          "Missing parameter:  '" + KEY_SEDBOX + "'!");
    } else {
      sedbox = p.getProperty(KEY_SEDBOX);
    }

    SEDReportBoxStatus sr = mdaoReports.getStatusReport(sedbox);
    sw.append("Got status report ");
    MSHInMail mi = new MSHInMail();
    mi.setStatus(SEDInboxMailStatus.RECEIVED.getValue());
    List<MSHInMail> lstInMail = mdao.getDataList(MSHInMail.class, 0, 500, "Id", "ASC", mi);
    StringWriter swBody = new StringWriter();
    swBody.append("SED-Predal: ");
    swBody.append(sr.getSedbox());
    swBody.append(System.lineSeparator());
    swBody.append("Datum: ");
    swBody.append(SDF_DD_MM_YYY_HH_MI.format(sr.getReportDate()));
    if (sr.getInMail() != null && !sr.getInMail().getStatuses().isEmpty()) {
      sw.append("in mail: " + sr.getInMail().getStatuses().size());
      swBody.append(System.lineSeparator());
      swBody.append(System.lineSeparator());
      swBody.append("Statusi dohodne pošte:");
      swBody.append(System.lineSeparator());
      for (Status s : sr.getInMail().getStatuses()) {
        swBody.append(String.format("\t%s: %d", s.getStatus(), s.getCount()));
        swBody.append(System.lineSeparator());
      }
    } else {
      swBody.append(System.lineSeparator());
      swBody.append(System.lineSeparator());
      swBody.append("Za predal '" + sedbox + "' ni dohodne pošte");
      swBody.append(System.lineSeparator());
    }
    if (sr.getOutMail() != null && !sr.getOutMail().getStatuses().isEmpty()) {
      sw.append(", out mail: " + sr.getInMail().getStatuses().size());
      swBody.append(System.lineSeparator());
      swBody.append(System.lineSeparator());
      swBody.append("Statusi izhodne pošte:");
      swBody.append(System.lineSeparator());
      for (Status s : sr.getOutMail().getStatuses()) {
        swBody.append(String.format("\t%s: %d", s.getStatus(), s.getCount()));
        swBody.append(System.lineSeparator());
      }
    } else {
      swBody.append(System.lineSeparator());
      swBody.append(System.lineSeparator());
      swBody.append("Za predal '" + sedbox + "' ni izhodne pošte");
      swBody.append(System.lineSeparator());
    }
    swBody.append(System.lineSeparator());
    swBody.append(System.lineSeparator());
    swBody.append("Seznam dohodne pošte za prevzem (do 500 pošiljk): ");
    swBody.append("St pošiljk: '" + lstInMail.size() + "'");
    swBody.append(System.lineSeparator());
    sw.append("In mail size: " + lstInMail.size());
    swBody.append("st., id, dat  prejema, transakcija ID, Storitev, Akcija, Pošiljatelj, Opis");
    swBody.append(System.lineSeparator());
    int iVal = 1;
    for (MSHInMail im : lstInMail) {
      swBody.append((iVal++) + "., ");
      swBody.append(im.getId().toString() + ", ");
      swBody.append(SDF_DD_MM_YYY_HH_MI.format(im.getReceivedDate()) + ", ");
      swBody.append(im.getConversationId() + ", ");
      swBody.append(im.getService() + ", ");
      swBody.append(im.getAction() + ", ");
      swBody.append(im.getSenderEBox() + ", ");
      swBody.append(im.getSenderName() + ", ");
      swBody.append(im.getSubject());
      swBody.append(System.lineSeparator());
    }
    swBody.append(System.lineSeparator());
    return swBody.toString();
  }

  /**
   *
   * @return
   */
  @Override
  public CronTaskDef getDefinition() {
    CronTaskDef tt = super.getDefinition();
    tt.setType("statusreport");
    tt.setName("Status report");
    tt.setDescription("Incoming outcomming mail report from sed box");
    return tt;
  }

}
