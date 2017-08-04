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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.ejb.Local;
import javax.ejb.Stateless;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.enums.SEDInboxMailStatus;
import si.laurentius.commons.utils.StringFormater;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.plugin.crontask.CronTaskDef;
import si.laurentius.plugin.interfaces.PropertyListType;
import si.laurentius.plugin.interfaces.PropertyType;
import si.laurentius.plugin.interfaces.TaskExecutionInterface;
import si.laurentius.plugin.interfaces.exception.TaskException;
import si.laurentius.report.SEDReportBoxStatus;
import si.laurentius.report.Status;
import si.laurentius.task.filter.StatusReportInMailFilter;
import si.laurentius.task.filter.StatusReportOutMailFilter;

/**
 *
 * @author Jože Rihtaršič
 */
@Stateless
@Local(TaskExecutionInterface.class)
public class TaskEmailStatusReport extends TaskEmailReport {

  public static final String KEY_REPORT_IN_STATUS_LIST = "report.status.in-list";
  public static final String KEY_REPORT_OUT_STATUS_LIST = "report.status.out-list";
  public static final String KEY_REPORT_STATUS_CHANGE_START_INTEVRAL = "report.status.change.start.interval";
  public static final String KEY_REPORT_STATUS_CHANGE_END_INTEVRAL = "report.status.change.end.interval";
  public static final String KEY_REPORT_STATUS_SERVICE_LIST = "report.status.service-list";
  public static final String KEY_REPORT_WRITE_TO_FOLDER = "report.status.write.folder";
  public static final String KEY_REPORT_SHOW_MAIL_LIST = "report.status.show.mail.list";
  public static final String KEY_REPORT_NO_MAIL = "skip.on.NoMail";

  final SimpleDateFormat SDF_YYYYMMDD_HHMISS = new SimpleDateFormat(
          "yyyyMMdd_HHmmss");

  /**
   *
   * @param p
   * @param sw
   * @return
   */
  @Override
  public String generateMailReport(Properties p, StringWriter sw) throws TaskException {

    String sedbox = null;
    boolean bSkipNoMail = true;
    int iTimeIntervalStart = 60;
    int iTimeIntervalEnd = 5;
    List<String> inStatuses = new ArrayList<>();
    List<String> outStatuses = new ArrayList<>();
    List<String> services = new ArrayList<>();
    String showMailDataList = null;
    String writeToFolder = null;

    if (!p.containsKey(KEY_SEDBOX)) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException,
              "Missing parameter:  '" + KEY_SEDBOX + "'!");
    } else {
      sedbox = p.getProperty(KEY_SEDBOX) + '@' + SEDSystemProperties.
              getLocalDomain();
    }

    if (p.containsKey(KEY_REPORT_WRITE_TO_FOLDER)) {

      writeToFolder = p.getProperty(KEY_REPORT_WRITE_TO_FOLDER);
      if (!Utils.isEmptyString(writeToFolder)) {
        writeToFolder = StringFormater.replaceProperties(writeToFolder);
      }
    }

    if (!p.containsKey(KEY_REPORT_NO_MAIL)) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException,
              "Missing parameter:  '" + KEY_REPORT_NO_MAIL + "'!");
    } else {
      bSkipNoMail = p.getProperty(KEY_REPORT_NO_MAIL).trim().equalsIgnoreCase(
              "true");
    }

    if (!p.containsKey(KEY_REPORT_STATUS_CHANGE_START_INTEVRAL)) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException,
              "Missing parameter:  '" + KEY_REPORT_STATUS_CHANGE_START_INTEVRAL + "'!");
    } else {
      iTimeIntervalStart = Integer.parseInt(p.getProperty(
              KEY_REPORT_STATUS_CHANGE_START_INTEVRAL).trim());
    }

    if (!p.containsKey(KEY_REPORT_STATUS_CHANGE_END_INTEVRAL)) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException,
              "Missing parameter:  '" + KEY_REPORT_STATUS_CHANGE_END_INTEVRAL + "'!");
    } else {
      iTimeIntervalEnd = Integer.parseInt(p.getProperty(
              KEY_REPORT_STATUS_CHANGE_END_INTEVRAL).trim());
    }

    if (iTimeIntervalEnd >= iTimeIntervalStart) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException,
              "Invalid parameter TimeInterval: Start interval must be greater than end interval!");
    }

    if (p.containsKey(KEY_REPORT_IN_STATUS_LIST)) {
      String val = p.getProperty(
              KEY_REPORT_IN_STATUS_LIST);
      inStatuses.addAll(Arrays.asList(val.split(",")));
    }

    if (p.containsKey(KEY_REPORT_OUT_STATUS_LIST)){
      String val = p.getProperty(
              KEY_REPORT_OUT_STATUS_LIST);
      outStatuses.addAll(Arrays.asList(val.split(",")));
    }
    
    if (outStatuses.isEmpty() && inStatuses.isEmpty()){

      throw new TaskException(TaskException.TaskExceptionCode.InitException,
              "At least on of statuses should be selected for  :  '" + KEY_REPORT_IN_STATUS_LIST + " or '" +KEY_REPORT_OUT_STATUS_LIST +"'!");
    } 

    if (!p.containsKey(KEY_REPORT_STATUS_SERVICE_LIST)) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException,
              "Missing parameter:  '" + KEY_REPORT_STATUS_SERVICE_LIST + "'!");
    } else {
      String val = p.getProperty(
              KEY_REPORT_STATUS_SERVICE_LIST);
      services.addAll(Arrays.asList(val.split(",")));;

    }

    if (p.containsKey(KEY_REPORT_SHOW_MAIL_LIST)) {
      showMailDataList = p.getProperty(
              KEY_REPORT_SHOW_MAIL_LIST);
    }

    Calendar cStart = Calendar.getInstance();
    cStart.add(Calendar.MINUTE, -1 * iTimeIntervalStart);
    Date startDate = cStart.getTime();
    Calendar cEnd = Calendar.getInstance();
    cEnd.add(Calendar.MINUTE, -1 * iTimeIntervalEnd);
    Date endDate = cEnd.getTime();

    SEDReportBoxStatus sr = mdaoReports.
            getStatusReport(sedbox, startDate, endDate,
                    inStatuses, outStatuses, services);

    sw.append("Got status report ");
    MSHInMail mi = new MSHInMail();
    mi.setStatus(SEDInboxMailStatus.RECEIVED.getValue());
    mi.setReceiverEBox(sedbox);

    if (bSkipNoMail && (sr.getInMail() == null || sr.getInMail().getStatuses().
            isEmpty())
            && (sr.getOutMail() == null || sr.getOutMail().getStatuses().
            isEmpty())) {
      sw.append("Nothing to report! Suppress mail");
      return null;
    }

    StringWriter swBody = new StringWriter();
    swBody.append("SED-Predal: ");
    swBody.append(sr.getSedbox());
    swBody.append(System.lineSeparator());
    swBody.append("Datum porocila: ");
    swBody.append(SDF_DD_MM_YYY_HH_MI.format(sr.getReportDate()));
    swBody.append(System.lineSeparator());
    swBody.append("Sprememba statusa od: ");
    swBody.append(iTimeIntervalStart + " do: " + iTimeIntervalEnd + "minut od datuma poročila");
    swBody.append(System.lineSeparator());
    swBody.append("Storitve:");
    swBody.append(String.join(",", services));
    swBody.append(System.lineSeparator());
    swBody.append("Dohodni statusi:");
    swBody.append(String.join(",", inStatuses));
    swBody.append(System.lineSeparator());
    swBody.append("Izhodni statusi:");
    swBody.append(String.join(",", outStatuses));

    if (sr.getInMail() != null && !sr.getInMail().getStatuses().isEmpty()) {
      sw.append("in mail: " + sr.getInMail().getStatuses().size());
      swBody.append(System.lineSeparator());
      swBody.append("Statusi dohodne pošte:");
      swBody.append(System.lineSeparator());
      for (Status s : sr.getInMail().getStatuses()) {
        swBody.append(String.format("\t%s\t%s: %d", s.getService(), s.
                getStatus(), s.getCount()));
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
        swBody.append(String.format("\t%s\t%s: %d", s.getService(), s.
                getStatus(), s.getCount()));
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

    if (!Utils.isEmptyString(showMailDataList)) {
      StatusReportInMailFilter srimf = new StatusReportInMailFilter();
      srimf.setReceiverEBox(sedbox);
      srimf.setServiceList(services);
      srimf.setStatusList(inStatuses);
      srimf.setStatusDateFrom(startDate);
      srimf.setStatusDateTo(endDate);

      StatusReportOutMailFilter sromf = new StatusReportOutMailFilter();
      sromf.setSenderEBox(sedbox);
      sromf.setServiceList(services);
      sromf.setStatusList(inStatuses);
      sromf.setStatusDateFrom(startDate);
      sromf.setStatusDateTo(endDate);

      List<MSHInMail> lstInMail = mdao.
              getDataList(MSHInMail.class, -1, 500, "Id", "ASC", srimf);

      List<MSHOutMail> lstOutMail = mdao.
              getDataList(MSHOutMail.class, -1, 500, "Id", "ASC", sromf);

      swBody.append(System.lineSeparator());
      swBody.append("Seznam dohodne pošte (do 500 pošiljk): ");
      swBody.append("St pošiljk: '" + lstInMail.size() + "'");
      swBody.append(System.lineSeparator());

      swBody.append(showMailDataList);
      swBody.append(System.lineSeparator());
      int iVal = 1;
      for (MSHInMail im : lstInMail) {
        swBody.append((iVal++) + ". ");
        swBody.append(StringFormater.format(showMailDataList, im));
        swBody.append(System.lineSeparator());
      }

      swBody.append(System.lineSeparator());
      swBody.append(System.lineSeparator());
      swBody.append("Seznam izhodne pošte (do 500 pošiljk): ");
      swBody.append("St pošiljk: '" + lstOutMail.size() + "'");
      swBody.append(System.lineSeparator());

      swBody.append(showMailDataList);
      swBody.append(System.lineSeparator());
      iVal = 1;
      for (MSHOutMail om : lstOutMail) {
        swBody.append((iVal++) + ". ");
        swBody.append(StringFormater.format(showMailDataList, om));
        swBody.append(System.lineSeparator());
      }
    }

    swBody.append(System.lineSeparator());
    String res = swBody.toString();

    if (!Utils.isEmptyString(writeToFolder)) {
      File f = new File(writeToFolder);
      if (!f.exists()) {
        f.mkdir();
      }

      File fRep = new File(f, "report_" + SDF_YYYYMMDD_HHMISS.format(Calendar.
              getInstance().getTime()));
      try (FileOutputStream fos = new FileOutputStream(fRep)) {
        fos.write(res.getBytes());
        fos.flush();
      } catch (IOException ex) {
        LOG.logError("Error occured while writting report to file", ex);
      }

    }

    return res;
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
    tt.setDescription("Incoming/outgoing mail report for sed box");

    tt.getCronTaskPropertyDeves().add(
            createTTProperty(KEY_REPORT_IN_STATUS_LIST, "In mail status list",
                    false, PropertyType.MultiList.
                            getType(), null, PropertyListType.InMailStatus.
                            getType(), null));

    tt.getCronTaskPropertyDeves().add(
            createTTProperty(KEY_REPORT_OUT_STATUS_LIST, "Out mail status list",
                    false, PropertyType.MultiList.
                            getType(), null, PropertyListType.OutMailStatus.
                            getType(), null));

    tt.getCronTaskPropertyDeves().add(
            createTTProperty(KEY_REPORT_STATUS_SERVICE_LIST,
                    "Report for services",
                    true, PropertyType.MultiList.
                            getType(), null, PropertyListType.Services.getType(),
                    null));

    tt.getCronTaskPropertyDeves().add(
            createTTProperty(KEY_REPORT_STATUS_CHANGE_START_INTEVRAL,
                    "Start time interval changed in minutes from now", true,
                    PropertyType.Integer.
                            getType(), null, null, "60"));

    tt.getCronTaskPropertyDeves().add(
            createTTProperty(KEY_REPORT_STATUS_CHANGE_END_INTEVRAL,
                    "End time interval changed in minutes from now", true,
                    PropertyType.Integer.
                            getType(), null, null, "5"));

    tt.getCronTaskPropertyDeves().add(
            createTTProperty(KEY_REPORT_SHOW_MAIL_LIST,
                    "Show mail list in report", true,
                    PropertyType.String.
                            getType(), null, null,
                    "${Id} ${Service}  ${SenderEBox} ${ReceiverEBox}"));

    tt.getCronTaskPropertyDeves().add(createTTProperty(KEY_REPORT_NO_MAIL,
            "Suppress if not Mail ", true,
            PropertyType.Boolean.
                    getType(), null, null, "false"));

    tt.getCronTaskPropertyDeves().add(
            createTTProperty(KEY_REPORT_WRITE_TO_FOLDER,
                    "(ex: ${laurentius.home}/test-backup/) If parameter is given than report is written to folder",
                    false,
                    PropertyType.String.
                            getType(), null, null, null));

    return tt;
  }

}
