/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.laurentius.task;

import java.io.StringWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import javax.ejb.Local;
import javax.ejb.Stateless;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.SEDValues;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.utils.StringFormater;
import si.laurentius.commons.utils.Utils;
import si.laurentius.cron.SEDTaskExecution;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.plugin.crontask.CronTaskDef;
import si.laurentius.plugin.interfaces.PropertyListType;
import si.laurentius.plugin.interfaces.PropertyType;
import si.laurentius.plugin.interfaces.TaskExecutionInterface;
import si.laurentius.plugin.interfaces.exception.TaskException;
import si.laurentius.report.SEDReportBoxStatus;
import si.laurentius.report.Status;
import static si.laurentius.task.TaskEmailReport.LOG;
import si.laurentius.task.filter.ReportTimeIntervalType;
import si.laurentius.task.filter.StatusReportInMailFilter;
import si.laurentius.task.filter.StatusReportOutMailFilter;

/**
 *
 * @author Jože Rihtaršič
 */
@Stateless
@Local(TaskExecutionInterface.class)
public class TaskEmailStatusReport extends TaskEmailReport {

  public static final String KEY_REPORT_SEDBOXES = "report.status.sedboxes";
  public static final String KEY_REPORT_IN_STATUS_LIST = "report.status.in-list";
  public static final String KEY_REPORT_OUT_STATUS_LIST = "report.status.out-list";
  public static final String KEY_REPORT_TIME_INTERVAL_TYPE = "report.status.time.interval.type";
  public static final String KEY_REPORT_TIME_START_INTERVAL = "report.status.time.start.interval";
  public static final String KEY_REPORT_STARTFROMLAST = "report.status.time.startFromLastEI";
  public static final String KEY_REPORT_TIME_END_INTERVAL = "report.status.time.end.interval";

  public static final String KEY_REPORT_STATUS_SERVICE_LIST = "report.status.service-list";

  public static final String KEY_REPORT_SHOW_MAIL_LIST = "report.status.show.mail.datamask";

  /**
   *
   * @param p
   * @param sw
   * @return
   */
  @Override
  public String generateMailReport(Properties p, StringWriter sw) throws TaskException {

    List<String> sedboxes = new ArrayList<>();
    ;
    boolean bSkipNoMail = false;
    boolean bReportOnlyNew = false;
    int iTimeIntervalStart = 60;
    int iTimeIntervalEnd = 5;
    List<String> inStatuses = new ArrayList<>();
    List<String> outStatuses = new ArrayList<>();
    List<String> services = new ArrayList<>();
    String showMailDataList = null;
    ReportTimeIntervalType rti = null;

    if (!p.containsKey(KEY_REPORT_SEDBOXES)) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException,
              "Missing parameter:  '" + KEY_REPORT_SEDBOXES + "'!");
    } else {
      String[] lstSB = p.getProperty(KEY_REPORT_SEDBOXES).split(",");
      for (String sb : lstSB) {
        sedboxes.add(sb + '@' + SEDSystemProperties.
                getLocalDomain());
      }
    }

    if (!p.containsKey(KEY_REPORT_NO_MAIL)) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException,
              "Missing parameter:  '" + KEY_REPORT_NO_MAIL + "'!");
    } else {
      bSkipNoMail = p.getProperty(KEY_REPORT_NO_MAIL).trim().equalsIgnoreCase(
              "true");
    }

    if (!p.containsKey(KEY_REPORT_STARTFROMLAST)) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException,
              "Missing parameter:  '" + KEY_REPORT_STARTFROMLAST + "'!");
    } else {
      bReportOnlyNew = p.getProperty(KEY_REPORT_STARTFROMLAST).trim().
              equalsIgnoreCase("true");
    }

    if (!p.containsKey(KEY_REPORT_TIME_INTERVAL_TYPE)) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException,
              "Missing parameter:  '" + KEY_REPORT_TIME_INTERVAL_TYPE + "'!");
    } else {
      String intervalType = p.getProperty(KEY_REPORT_TIME_INTERVAL_TYPE).trim();
      try {
        rti = ReportTimeIntervalType.valueOf(intervalType);
      } catch (IllegalArgumentException iae) {
        throw new TaskException(TaskException.TaskExceptionCode.InitException,
                "Invalid parameter:  '" + KEY_REPORT_TIME_INTERVAL_TYPE + "' : '" + intervalType + "'!");
      }

    }

    if (!p.containsKey(KEY_REPORT_TIME_START_INTERVAL)) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException,
              "Missing parameter:  '" + KEY_REPORT_TIME_START_INTERVAL + "'!");
    } else {
      iTimeIntervalStart = Integer.parseInt(p.getProperty(
              KEY_REPORT_TIME_START_INTERVAL).trim());
    }

    if (!p.containsKey(KEY_REPORT_TIME_END_INTERVAL)) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException,
              "Missing parameter:  '" + KEY_REPORT_TIME_END_INTERVAL + "'!");
    } else {
      iTimeIntervalEnd = Integer.parseInt(p.getProperty(
              KEY_REPORT_TIME_END_INTERVAL).trim());
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

    if (p.containsKey(KEY_REPORT_OUT_STATUS_LIST)) {
      String val = p.getProperty(
              KEY_REPORT_OUT_STATUS_LIST);
      outStatuses.addAll(Arrays.asList(val.split(",")));
    }

    if (outStatuses.isEmpty() && inStatuses.isEmpty()) {

      throw new TaskException(TaskException.TaskExceptionCode.InitException,
              "At least one of statuses should be selected for  :  '" + KEY_REPORT_IN_STATUS_LIST + " or '" + KEY_REPORT_OUT_STATUS_LIST + "'!");
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

    // get last report callDate
    Date lastCallDate = null;
    if (bReportOnlyNew) {
      BigInteger cronId = new BigInteger(p.getProperty(
              SEDValues.S_CRON_ID_PROPERTY));
      SEDTaskExecution te = null;
      try {
        te = mdao.getLastSuccesfullTaskExecution(cronId, getDefinition().
                getType());
        if (te != null) {
          lastCallDate = te.getStartTimestamp();
        }
      } catch (StorageException ex) {
        LOG.logWarn(0, "ERROR reading task execution", ex);
      }

    }

    // calculate start date
    Calendar cStart = Calendar.getInstance();
    cStart.add(Calendar.MINUTE, -1 * iTimeIntervalStart);

    if (lastCallDate != null && lastCallDate.after(cStart.getTime())) {
      cStart.setTime(lastCallDate);
      cStart.add(Calendar.MINUTE, -1 * iTimeIntervalEnd);
    }

    Date startDate = cStart.getTime();

    Calendar cEnd = Calendar.getInstance();
    cEnd.add(Calendar.MINUTE, -1 * iTimeIntervalEnd);
    Date endDate = cEnd.getTime();

    String strTimeIntervalMessage = "";
    SEDReportBoxStatus sr = null;
    switch (rti) {
      case AddedTime:
        strTimeIntervalMessage = "V poročilu so zajete samo nove pošiljke od zadnjega poročila:";
        sr = mdaoReports.
                getReportForAddMailnterval(sedboxes, startDate, endDate,
                        inStatuses, outStatuses, services);

        break;
      case StatusChangedTime:
        strTimeIntervalMessage = "V poročilu so zajete samo nove spremembe statusov pošiljk od zadnjega poročila:";
        sr = mdaoReports.
                getReportForStatusChangeInterval(sedboxes, startDate, endDate,
                        inStatuses, outStatuses, services);
        break;
    }

    if (bSkipNoMail
            && (sr.getInMail() == null || sr.getInMail().getStatuses().isEmpty())
            && (sr.getOutMail() == null || sr.getOutMail().getStatuses().
            isEmpty())) {
      sw.append("Nothing to report! Suppress mail");
      return null;
    }

    StringWriter swBody = new StringWriter();
    swBody.append("Predali: ");
    swBody.append(String.join(", ", sedboxes));
    swBody.append(System.lineSeparator());
    swBody.append("Datum poročila: ");
    swBody.append(SDF_DD_MM_YYY_HH_MI.format(sr.getReportDate()));
    swBody.append(System.lineSeparator());

    swBody.append(System.lineSeparator());

    swBody.append("Tip časovni interval:");
    swBody.append(rti.name());

    swBody.append(System.lineSeparator());
    swBody.append(strTimeIntervalMessage);
    swBody.append(bReportOnlyNew ? "true" : "false");
    swBody.append(System.lineSeparator());
    if (bReportOnlyNew) {
      swBody.append("Zadnje uspešno generirano poročilo:");
      swBody.append(SDF_DD_MM_YYY_HH_MI.format(sr.getReportDate()));
      swBody.append(System.lineSeparator());
    }

    swBody.append(
            "V poročilu so zajete pošiljke  od: ");
    swBody.append(SDF_DD_MM_YYY_HH_MI.format(startDate));
    if (!bReportOnlyNew || lastCallDate == null) {
      swBody.append(" (-" + iTimeIntervalStart + " min)");
    }
    swBody.append(" do: ");
    swBody.append(SDF_DD_MM_YYY_HH_MI.format(endDate));
    swBody.append(" (-" + iTimeIntervalEnd + " min)");
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
      swBody.append(
              "Za predale '" + String.join(", ", sedboxes) + "' ni dohodne pošte");
      swBody.append(System.lineSeparator());
    }
    if (sr.getOutMail() != null && !sr.getOutMail().getStatuses().isEmpty()) {
      sw.append(", out mail: " + sr.getInMail().getStatuses().size());
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
      swBody.append(
              "Za predale '" + String.join(", ", sedboxes) + "' ni izhodne pošte");
      swBody.append(System.lineSeparator());
    }
    swBody.append(System.lineSeparator());
    swBody.append(System.lineSeparator());

    if (!Utils.isEmptyString(showMailDataList)) {
      int iVal = 1;
      if (!inStatuses.isEmpty()) {
        StatusReportInMailFilter srimf = new StatusReportInMailFilter();
        srimf.setReceiverEBoxList(sedboxes);
        srimf.setServiceList(services);
        srimf.setStatusList(inStatuses);

        switch (rti) {
          case AddedTime:
            strTimeIntervalMessage = "V poročilu so zajete samo nove pošiljke od zadnjega poročila:";
            srimf.setReceivedDateFrom(startDate);
            srimf.setReceivedDateTo(endDate);

            break;
          case StatusChangedTime:
            srimf.setStatusDateFrom(startDate);
            srimf.setStatusDateTo(endDate);
            break;
        }

        List<MSHInMail> lstInMail = mdao.
                getDataList(MSHInMail.class, -1, 500, "Id", "ASC", srimf);

        swBody.append(System.lineSeparator());
        swBody.append("Seznam dohodne pošte (do 500 pošiljk): ");
        swBody.append("St pošiljk: '" + lstInMail.size() + "'");
        swBody.append(System.lineSeparator());

        swBody.append(showMailDataList);
        swBody.append(System.lineSeparator());
        iVal = 1;
        for (MSHInMail im : lstInMail) {
          swBody.append((iVal++) + ". ");
          swBody.append(StringFormater.format(showMailDataList, im));
          swBody.append(System.lineSeparator());
        }
        swBody.append(System.lineSeparator());
      }
      if (!outStatuses.isEmpty()) {
        StatusReportOutMailFilter sromf = new StatusReportOutMailFilter();
        sromf.setSenderEBoxList(sedboxes);
        sromf.setServiceList(services);
        sromf.setStatusList(outStatuses);
        
        switch (rti) {
          case AddedTime:
            strTimeIntervalMessage = "V poročilu so zajete samo nove pošiljke od zadnjega poročila:";
            sromf.setSubmittedDateFrom(startDate);
            sromf.setSubmittedDateTo(endDate);

            break;
          case StatusChangedTime:
            sromf.setStatusDateFrom(startDate);
            sromf.setStatusDateTo(endDate);
            break;
        }
        
        
        List<MSHOutMail> lstOutMail = mdao.
                getDataList(MSHOutMail.class, -1, 500, "Id", "ASC", sromf);
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
    }

    swBody.append(System.lineSeparator());
    String res = swBody.toString();

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
    tt.setDescription("Incoming/outgoing mail report");

    tt.getCronTaskPropertyDeves().add(createTTProperty(KEY_REPORT_SEDBOXES,
            "Local sedbox (without domain).", true, PropertyType.MultiList.
                    getType(), null, PropertyListType.LocalBoxes.getType(), null));

    tt.getCronTaskPropertyDeves().add(
            createTTProperty(KEY_REPORT_STATUS_SERVICE_LIST,
                    "Report for services",
                    true, PropertyType.MultiList.
                            getType(), null, PropertyListType.Services.getType(),
                    null));

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

    tt.getCronTaskPropertyDeves().add(createTTProperty(
            KEY_REPORT_TIME_INTERVAL_TYPE,
            "Time interval type: status changed or added to laurentius (submitted/received time)",
            true,
            PropertyType.List.
                    getType(), null, ReportTimeIntervalType.listOfNames(),
            ReportTimeIntervalType.StatusChangedTime.name()));

    tt.getCronTaskPropertyDeves().add(createTTProperty(
            KEY_REPORT_TIME_START_INTERVAL,
            "Start time interval in minutes from now", true,
            PropertyType.Integer.
                    getType(), null, null, "60"));
    tt.getCronTaskPropertyDeves().add(createTTProperty(KEY_REPORT_STARTFROMLAST,
            "Start time is end interval from last succesfully generated report: (Report date - end interval)",
            true, PropertyType.Boolean.
                    getType(), null, null, "false"));

    tt.getCronTaskPropertyDeves().add(createTTProperty(
            KEY_REPORT_TIME_END_INTERVAL,
            "End time interval in minutes from now", true,
            PropertyType.Integer.
                    getType(), null, null, "5"));

    tt.getCronTaskPropertyDeves().add(
            createTTProperty(KEY_REPORT_SHOW_MAIL_LIST,
                    "Mail data mask. If mask is not given, no mail list is added to report.",
                    false,
                    PropertyType.String.
                            getType(), null, null,
                    "${Id} ${Service} ${Action} ${Status} ${SenderEBox} ${ReceiverEBox}"));

    return tt;
  }

}
