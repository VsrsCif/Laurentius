/*
 * Copyright 2015, Supreme Court Republic of Slovenia
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package si.jrc.msh.plugin.meps;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.xml.bind.JAXBException;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.enums.SEDInboxMailStatus;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.SEDDaoInterface;

import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.StringFormater;
import si.laurentius.commons.utils.Utils;
import si.laurentius.commons.utils.xml.XMLUtils;
import si.laurentius.meps.envelope.EnvelopeData;
import si.laurentius.meps.envelope.PostalData;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.inbox.payload.MSHInPart;
import si.laurentius.plugin.crontask.CronTaskDef;
import si.laurentius.plugin.crontask.CronTaskPropertyDef;
import si.laurentius.plugin.interfaces.PropertyListType;
import si.laurentius.plugin.interfaces.PropertyType;
import si.laurentius.plugin.interfaces.TaskExecutionInterface;
import si.laurentius.plugin.interfaces.exception.TaskException;

/**
 * This is samople of cron task plugin component.
 *
 * @author Jože Rihtaršič
 */
@Stateless
@Local(TaskExecutionInterface.class)
public class MEPSTask implements TaskExecutionInterface {

  public static final String KEY_FOLDER = "meps.folder";
  public static final String KEY_SENDER_SEDBOX = "meps.sender.sedbox";
  public static final String KEY_SENDER_SERVICE = "meps.service";
  public static final String KEY_MAX_MAIL_COUT = "meps.mail.max.count";

  private static final SEDLogger LOG = new SEDLogger(MEPSTask.class);
  @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
  SEDDaoInterface mDB;

  /**
   * execute metod
   *
   * @param p - parameters defined at configuration of task instance
   * @return result description
   */
  @Override
  public String executeTask(Properties p)
          throws TaskException {

    long l = LOG.logStart();
    StringWriter sw = new StringWriter();
    sw.append("Start MEPS export plugin task: \n");

    // ---------------------------
    // read properties
    String sedBox = "";
    if (!p.containsKey(KEY_SENDER_SEDBOX)) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException,
              "Missing parameter:  '" + KEY_SENDER_SEDBOX + "'!");
    } else {
      sedBox = p.getProperty(KEY_SENDER_SEDBOX);
    }

    String service = "";
    if (!p.containsKey(KEY_SENDER_SERVICE)) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException,
              "Missing parameter:  '" + KEY_SENDER_SERVICE + "'!");
    } else {
      service = p.getProperty(KEY_SENDER_SERVICE);
    }

    String outFolder = "";
    if (!p.containsKey(KEY_FOLDER)) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException,
              "Missing parameter:  '" + KEY_FOLDER + "'!");
    } else {
      outFolder = p.getProperty(KEY_FOLDER);
    }

    int maxMailProc = 1500;
    if (p.containsKey(KEY_MAX_MAIL_COUT)) {
      String val = p.getProperty(KEY_MAX_MAIL_COUT);
      if (!Utils.isEmptyString(val)) {
        try {
          maxMailProc = Integer.parseInt(val);
        } catch (NumberFormatException nfe) {
          LOG.logError(String.format(
                  "Error parameter '%s'. Value '%s' is not a number Mail count 100 is setted!",
                  KEY_MAX_MAIL_COUT, val), nfe);
        }
      }
    }
    // ---------------------------
    // init
    File rootFolder = new File(StringFormater.replaceProperties(outFolder));
    if (!rootFolder.exists()) {
      rootFolder.mkdirs();
    }

    // retrieve files
    MSHInMail miFilter = new MSHInMail();
    String ebox = sedBox + "@" + SEDSystemProperties.getLocalDomain();
    miFilter.setStatus(SEDInboxMailStatus.RECEIVED.getValue());
    miFilter.setService(service);
    miFilter.setAction("AddMail");
    miFilter.setSenderEBox(ebox);

    LOG.
            formatedDebug("Get mail to export for service: %s, receiver %s",
                    service, ebox);

    List<MSHInMail> lst = mDB.
            getDataList(MSHInMail.class, -1, maxMailProc, "Id", "ASC", miFilter);
    sw.append("got " + lst.size() + " mails for sedbox: '" + sedBox + "'!");

    if (lst.isEmpty()) {

    } else {

      // lock mail
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

      File metadata = new File(rootFolder, "metadata.txt");
      FileWriter mailData = null;
      try {
        mailData = new FileWriter(metadata);
        for (MSHInMail m : lst) {

          exportData(m, mailData, rootFolder, service);
        }
        mailData.flush();

      } catch (IOException ex) {
        Logger.getLogger(MEPSTask.class.getName()).log(Level.SEVERE, null, ex);
      } finally {
        if (mailData != null) {
          try {
            mailData.close();
          } catch (IOException ex) {
            Logger.getLogger(MEPSTask.class.getName()).log(Level.SEVERE, null,
                    ex);
          }
        }
      }

      lst.stream().forEach((m) -> {
        try {
          mDB.setStatusToInMail(m, SEDInboxMailStatus.DELIVERED,
                  "Add message to zpp deliver proccess");
        } catch (StorageException ex) {
          String msg = String.format(
                  "Error occurred processing mail: '%s'. Err: %s.", m.getId(),
                  ex.getMessage());
          LOG.logError(l, msg, ex);
          sw.append(msg);
        }
      });

      // export
    }

    sw.append("End zpp plugin task");
    return sw.toString();
  }

  private void exportData(MSHInMail mInMail, FileWriter metadata,
          File outFolder, String strFormatedTime) throws TaskException {
    File envData = null;
    File export = null;
    for (MSHInPart mp : mInMail.getMSHInPayload().getMSHInParts()) {
      if (Objects.equals(MimeValue.MIME_XML.getMimeType(), mp.getMimeType())
              || Objects.equals(MimeValue.MIME_XML1.getMimeType(), mp.
                      getMimeType())) {
        if (envData != null) {
          throw new TaskException(
                  TaskException.TaskExceptionCode.ProcessException,
                  "Mail must have only one XML  attachmetns (Envelope data)!"
          );
        } else {
          envData = StorageUtils.getFile(mp.getFilepath());
        }

      } else if (Objects.equals(MimeValue.MIME_PDF.getMimeType(), mp.
              getMimeType())
              && Objects.equals("MEPS", mp.getSource())) {
        if (export != null) {
          throw new TaskException(
                  TaskException.TaskExceptionCode.ProcessException,
                  "Mail must have only one MEPS attachmetns (concenated.pdf)!"
          );
        } else {
          export = StorageUtils.getFile(mp.getFilepath());
        }
      }
    }

    if (envData == null) {
      throw new TaskException(
              TaskException.TaskExceptionCode.ProcessException,
              "Mail must have one XML  attachmetns (Envelope data)!"
      );
    }

    if (export == null) {
      throw new TaskException(
              TaskException.TaskExceptionCode.ProcessException,
              "Mail must have one MEPS  attachmetns (Conceated pdf)!"
      );
    }

    try {
      String conntentFileName = "doc_" + mInMail.getId() + ".pdf";
      EnvelopeData ed = (EnvelopeData) XMLUtils.deserialize(envData,
              EnvelopeData.class);
      String dataLine = generateDataLine(mInMail.getId().longValue(), ed,
              strFormatedTime, conntentFileName);
      metadata.append(dataLine);
      StorageUtils.copyFile(export, new File(outFolder, conntentFileName), true);

    } catch (JAXBException ex) {
      Logger.getLogger(MEPSTask.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      Logger.getLogger(MEPSTask.class.getName()).log(Level.SEVERE, null, ex);
    } catch (StorageException ex) {
      Logger.getLogger(MEPSTask.class.getName()).log(Level.SEVERE, null, ex);
    }

  }

  private String generateDataLine(long mailId, EnvelopeData doe,
          String strFormatedTime, String filename) {

    // ger r number
    String strRRNumber = null;
    String strRNumb = "";
    PostalData.UPNCode upn = doe.getPostalData() != null ? doe.getPostalData().
            getUPNCode() : null;

    if (upn != null && upn.getCode() != null) {
      strRNumb = upn.getCode().toString();

      if (upn.getControl() == null) {
        // calculate upn
        upn.setControl(5);
      }

      while (strRNumb.length() < 8) {
        strRNumb = "0" + strRNumb;
      }
      strRRNumber = upn.getPrefix() + strRNumb + upn.getControl().toString() + upn.
              getSuffix();
    }

    StringWriter out = new StringWriter();
    out.write(toCSVString(filename));
    out.write(PackageConstants.S_SEPARATOR_DATA);
    out.write(strRNumb);
    out.write(PackageConstants.S_SEPARATOR_DATA);
    out.write(toCSVString(doe.getSenderMailData().getCaseCode()));
    out.write(PackageConstants.S_SEPARATOR_DATA);
    out.write(toCSVString(strFormatedTime));
    out.write(PackageConstants.S_SEPARATOR_DATA);
    out.write(toCSVString(doe.getReceiverAddress().getName()));
    out.write(PackageConstants.S_SEPARATOR_DATA);
    out.write(toCSVString(doe.getReceiverAddress().getName2()));
    out.write(PackageConstants.S_SEPARATOR_DATA);
    out.write(toCSVString(doe.getReceiverAddress().getAddress()));
    out.write(PackageConstants.S_SEPARATOR_DATA);
    out.write(toCSVString(doe.getReceiverAddress().getPostalCode()));
    out.write(PackageConstants.S_SEPARATOR_DATA);
    out.write(toCSVString(doe.getReceiverAddress().getTown()));
    out.write(PackageConstants.S_SEPARATOR_DATA);
    out.write(toCSVString(doe.getSenderAddress().getName()));
    out.write(PackageConstants.S_SEPARATOR_DATA);
    out.write(toCSVString(doe.getSenderAddress().getName2()));
    out.write(PackageConstants.S_SEPARATOR_DATA);
    out.write(toCSVString(doe.getSenderAddress().getAddress()));
    out.write(PackageConstants.S_SEPARATOR_DATA);
    out.write(toCSVString(doe.getSenderAddress().getPostalCode()));
    out.write(PackageConstants.S_SEPARATOR_DATA);
    out.write(toCSVString(doe.getSenderAddress().getTown()));
    out.write(PackageConstants.S_SEPARATOR_DATA);
    out.write(""); // na strojno kuverto se ne izpisuje, kdo je naredil posiljko
//        out.write((isDeliverAdviceRecognition ? "": toCSVString(strUserId)));
    out.write(PackageConstants.S_SEPARATOR_DATA);
    out.write(toCSVString(doe.getSenderMailData().getContentDescription()));
    out.write(PackageConstants.S_SEPARATOR_DATA);
    out.write(""); //na strojno kuverto se ne izpisuje koledarja
    //out.write((isDeliverAdviceRecognition ? "" : toCSVString((doe.getKoledar() != null ? msdfDDMMYYYY.format(doe.getKoledar())               : ""))));
    out.write(PackageConstants.S_SEPARATOR_DATA);
    out.write(toCSVString(doe.getPostalData().getSubmitPostalCode()));
    out.write(PackageConstants.S_SEPARATOR_DATA);
    out.write("");

    out.write(upn != null && upn.getControl() != null ? upn.getControl().
            toString() : "");
    out.write(PackageConstants.S_SEPARATOR_DATA);
    out.write(toCSVString(""));
    out.write(PackageConstants.S_SEPARATOR_DATA);
    out.write(toCSVString(doe.getPostalData().getSubmitPostalName()));
    out.write(PackageConstants.S_SEPARATOR_DATA);
    out.write(doe.getPostalData().getSubmitPostalCode());
    out.write(PackageConstants.S_SEPARATOR_DATA);
    out.write(strRRNumber != null ? strRRNumber : "");
    out.write(PackageConstants.S_SEPARATOR_DATA);
    out.write(upn != null ? upn.getPrefix() : "");
    out.write(PackageConstants.S_SEPARATOR_DATA);
    out.write("");
    out.write(PackageConstants.S_SEPARATOR_DATA);
    out.write("#"); // enc number        
    out.write(doe.getSenderMailData().getSenderMailCode());
    //out.write(doe.getEncid());
    out.write(PackageConstants.S_SEPARATOR_DATA);
    out.write("package");
    out.write(PackageConstants.S_SEPARATOR_DATA);
    out.write(doe.getPostalData().getEnvelopeType());
    out.write(PackageConstants.S_SEPARATOR_LINE);
    return out.toString();
  }

  private String toCSVString(String strVal) {
    String strWriteVal = strVal != null ? strVal : "";
    strWriteVal = strWriteVal.replaceAll("\\|", "¤"); // CRO request
    return strWriteVal;
  }

  /**
   * Retrun cron task definition: name, unique type, description, parameters..
   *
   * @return Cron task definition
   */
  @Override
  public CronTaskDef getDefinition() {
    CronTaskDef tt = new CronTaskDef();
    tt.setType("MEPS-Process");
    tt.setName("MEPS Batch process");
    tt.setDescription("Prepare mail to process");

    tt.getCronTaskPropertyDeves().add(
            createTTProperty(KEY_FOLDER, "Export folder", true,
                    PropertyType.String.getType(), null, null,
                    "${laurentius.home}/meps-archive/"));

    tt.getCronTaskPropertyDeves().add(
            createTTProperty(KEY_SENDER_SEDBOX, "Sender box", true,
                    PropertyType.List.getType(), null,
                    PropertyListType.LocalBoxes.getType(), null));

    tt.getCronTaskPropertyDeves().add(
            createTTProperty(KEY_SENDER_SERVICE, "Service", true,
                    PropertyType.List.getType(), null,
                    "PrintAndEnvelope-LegalZPP,PrintAndEnvelope-LegalZPP-NP,"
                    + "PrintAndEnvelope-LegalZUP,PrintAndEnvelope-Mail-C5,"
                    + "PrintAndEnvelope-RegistredMail-C5,PrintAndEnvelope-RegistredPackage",
                    "PrintAndEnvelope-LegalZPP"));

    tt.getCronTaskPropertyDeves().add(
            createTTProperty(KEY_MAX_MAIL_COUT, "Max mail count", true,
                    PropertyType.Integer.getType(), null, null, "15000"));

    return tt;
  }

  private CronTaskPropertyDef createTTProperty(String key, String desc,
          boolean mandatory,
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

}
