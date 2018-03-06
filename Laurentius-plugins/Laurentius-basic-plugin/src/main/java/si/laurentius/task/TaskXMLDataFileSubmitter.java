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
package si.laurentius.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.xml.bind.JAXBException;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.enums.SEDOutboxMailStatus;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.exception.PModeException;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.pmode.EBMSMessageContext;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.StringFormater;
import si.laurentius.commons.utils.Utils;
import si.laurentius.commons.utils.xml.XMLUtils;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.laurentius.plugin.crontask.CronTaskDef;
import si.laurentius.plugin.crontask.CronTaskPropertyDef;
import si.laurentius.plugin.interfaces.TaskExecutionInterface;
import si.laurentius.plugin.interfaces.exception.TaskException;
import si.laurentius.task.exception.FSException;

/**
 *
 * @author Jože Rihtaršič
 */
@Stateless
@Local(TaskExecutionInterface.class)
public class TaskXMLDataFileSubmitter implements TaskExecutionInterface {

  /**
   *
   */
  public static final String KEY_EXPORT_FOLDER = "file.submit.folder";
  private static final SEDLogger LOG = new SEDLogger(
          TaskXMLDataFileSubmitter.class);

  private static final String OUTMAIL_FILENAME = "outmail";
  private static final String OUTMAIL_SUFFIX = ".xml";
  private static final String OUTMAIL_SUFFIX_ERROR = ".error";
  private static final String OUTMAIL_SUFFIX_PROCESS = ".process";
  private static final String OUTMAIL_SUFFIX_SUBMITTED = ".submitted";

  private final DateFormat SDF = SimpleDateFormat.getDateTimeInstance(
          SimpleDateFormat.DEFAULT, SimpleDateFormat.DEFAULT);
  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  SEDLookupsInterface mLookups;

  StorageUtils mSU = new StorageUtils();

  @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
  SEDDaoInterface mdao;

  @EJB(mappedName = SEDJNDI.JNDI_PMODE)
  protected PModeInterface mpModeManager;

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

  /**
   *
   * @param p
   * @return
   */
  @Override
  public String executeTask(Properties p)
          throws TaskException {
    long l = LOG.logStart();
    StringWriter sw = new StringWriter();
    int iVal = 0;
    String sfolder;
    if (!p.containsKey(KEY_EXPORT_FOLDER)) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException,
              "Missing parameter:  '" + KEY_EXPORT_FOLDER + "'!");
    } else {
      sfolder = p.getProperty(KEY_EXPORT_FOLDER);
    }
    sfolder = StringFormater.replaceProperties(sfolder);
    sw.append("Submit from folder: " + sfolder);

    File fRoot = new File(sfolder);
    if (!fRoot.exists()) {
      sw.append("Submit folder not exist!");
    } else if (!fRoot.isDirectory()) {
      sw.append("Submit folder is not a folder!");
    } else {
      File[] flst
              = fRoot.listFiles((File dir, String name) -> name.startsWith(
              OUTMAIL_FILENAME)
              && name.endsWith(OUTMAIL_SUFFIX));
      for (File file : flst) {
        try {
          if (isFileLocked(file)) {
            LOG.formatedDebug(
                    "File: " + file.getName() + " is locked or submitted: abort submitting file");
          } else {
            Properties lock = new Properties();
            lock.setProperty("start.submitting", SDF.format(Calendar.
                    getInstance().getTime()));

            File fMetaData = new File(
                    file.getAbsolutePath() + OUTMAIL_SUFFIX_PROCESS);
            try (FileOutputStream fosMD = new FileOutputStream(fMetaData)) {
              LOG.
                      log("Lock file: " + file.getName() + " - create new process file");
              lock.store(fosMD, "OutMail proccessed");
            }

            try {
              BigInteger bi = processOutMail(p, file, fRoot);
              LOG.formatedDebug("Submit file %s. MailId %d", file.getName(), bi);
              File fewFMetaData = new File(
                      file.getAbsolutePath() + OUTMAIL_SUFFIX_SUBMITTED);
              if (fMetaData.renameTo(fewFMetaData)) {
                fMetaData = fewFMetaData;
                iVal++;
                try (FileOutputStream fos = new FileOutputStream(fMetaData, true);
                        PrintStream ps = new PrintStream(fos)) {

                  if (bi != null) {
                    ps.append("Laurentius.id=");
                    ps.append(bi.toString());
                    ps.append("\n");
                  }
                }
              } else {
                LOG.logError(l, "Error rename status file fpr: " + file.
                        getAbsolutePath(), null);
              }

            } catch (IOException ex) {
              LOG.logError(l, "Error reading outmail data: " + file.
                      getAbsolutePath(), ex);
              File fewFMetaData = new File(
                      file.getAbsolutePath() + OUTMAIL_SUFFIX_ERROR);
              if (fMetaData.renameTo(fewFMetaData)) {
                try (FileOutputStream fos = new FileOutputStream(fMetaData, true)) {
                  PrintStream ps = new PrintStream(fos);
                  ex.printStackTrace(ps);
                }
              } else {
                LOG.logError(l, "Error rename status file fpr: " + file.
                        getAbsolutePath(), null);
              }
            } catch (FSException ex) {
              LOG.logError(l, "Error subbmitting mail: " + file.
                      getAbsolutePath(), ex);
              File fewFMetaData = new File(
                      file.getAbsolutePath() + OUTMAIL_SUFFIX_ERROR);
              if (fMetaData.renameTo(fewFMetaData)) {
                fMetaData = fewFMetaData;
                try (FileOutputStream fos = new FileOutputStream(fMetaData, true)) {
                  PrintStream ps = new PrintStream(fos);
                  ex.printStackTrace(ps);
                }
              } else {
                LOG.logError(l, "Error rename status file fpr: " + file.
                        getAbsolutePath(), null);
              }
            }
          }

        } catch (IOException ex) {
          LOG.logError(l, "Errror reading outmail data: " + file.
                  getAbsolutePath(), ex);
        }

      }

    }
    sw.append("Submited '" + iVal + "'");
    sw.append(" in : " + (LOG.getTime() - l) + " ms\n");
    LOG.logEnd(l);
    return sw.toString();
  }

  /**
   *
   * @return
   */
  @Override
  public CronTaskDef getDefinition() {
    CronTaskDef tt = new CronTaskDef();
    tt.setType("xmlfilesubmitter");
    tt.setName("XML File subbmiter");
    tt.setDescription(
            "Task submits mail in given folder. '");
    tt.getCronTaskPropertyDeves().add(createTTProperty(KEY_EXPORT_FOLDER,
            "Submit folder", true,
            "string", null, null, "${laurentius.home}/submit/dwr/"));

    return tt;
  }

  private boolean isFileLocked(File f)
          throws IOException {

    boolean bVal = false;
    for (File smbl : f.getParentFile().listFiles()) {
      if (smbl.isFile()
              && !smbl.equals(f)
              && (smbl.getName().equals(f.getName() + OUTMAIL_SUFFIX_ERROR)
              || smbl.getName().equals(f.getName() + OUTMAIL_SUFFIX_PROCESS) || smbl.
              getName()
              .equals(f.getName() + OUTMAIL_SUFFIX_SUBMITTED))) {
        bVal = true;
        break;
      }

    }
    return bVal;
  }

  private BigInteger processOutMail(Properties p, File fMetaData,
          File rootFolder)
          throws FSException {
    long l = LOG.logStart();
    // validate data
    BigInteger res = null;
    MSHOutMail mom;
    try {
      mom = (MSHOutMail) XMLUtils.deserialize(fMetaData, MSHOutMail.class);
    } catch (JAXBException ex) {
      throw new FSException(String.format(
              "Error occured while deserializing file:  '%s', Error; %s!'",
              fMetaData.getAbsoluteFile(), ex.getMessage()), ex);
    }
    if (mom.getMSHOutPayload() != null) {
      for (MSHOutPart mp : mom.getMSHOutPayload().getMSHOutParts()) {

        File f = new File(rootFolder, mp.getFilepath());
        if (f.exists()) {
          mp.setFilepath(f.getAbsolutePath());
        }
      }
    }

    validateMailForMissingData(mom);

    if (mLookups.getSEDBoxByAddressName(mom.getSenderEBox()) == null) {
      throw new FSException(String.format("Sender box '%s' do not exists!'",
              mom.getSenderEBox()));
    }

    if (!Utils.isEmptyString(mom.getSenderMessageId())) {
      List<MSHOutMail> lstSendMail = mdao.getMailBySenderMessageId(
              MSHOutMail.class, mom.getSenderMessageId());
      if (!lstSendMail.isEmpty()) {
        throw new FSException(String.format(
                "Message with senderMessageId '%s' already submitted %s (cnt: %d)",
                mom.getSenderMessageId(), lstSendMail.get(0).getSubmittedDate().
                toString(),
                lstSendMail.size()));
      }
    }

    // prepare mail to persist
    Date dt = new Date();
    // set current status
    mom.setStatus(SEDOutboxMailStatus.SUBMITTED.getValue());
    mom.setSubmittedDate(dt);
    mom.setStatusDate(dt);

    EBMSMessageContext ectx = null;
    try {
      // validate message 
      ectx = mpModeManager.createMessageContextForOutMail(mom);
    } catch (PModeException ex) {
      String errDesc
              = "Error configurating send context for mail. Err:" + ex.
                      getMessage()
              + ".  Message with id '" + mom.getMessageId() + "' is not procesed!";

      throw new FSException(errDesc, ex);
    }

    // store files to DB
    for (MSHOutPart mp : mom.getMSHOutPayload().getMSHOutParts()) {

      File f = new File(mp.getFilepath());
      if (f.exists()) {

        try {
          File fNew = mSU.storeInFile(mp.getMimeType(), f);
          mp.setFilepath(StorageUtils.getRelativePath(fNew));
        } catch (StorageException ex) {
          String errDesc
                  = "Error occured while storing file " + f.getAbsolutePath() + " to storage. Err:" + ex.
                  getMessage()
                  + ".  Message with id '" + mom.getMessageId() + "' is not procesed!";

          throw new FSException(errDesc, ex);
        }

      }
    }

    try {
      mdao.serializeOutMail(mom, "", "xml-file-submitter", ectx.getPMode().
              getId());
      res = mom.getId();
    } catch (StorageException ex) {
      throw new FSException("Error serializing mail", ex);
    }
    LOG.logEnd(l);
    return res;
  }

  public static List<String> validateMailForMissingData(MSHOutMail mail) throws FSException {
    List<String> errLst = new ArrayList<>();
    List<String> warnLst = new ArrayList<>();

    if (Utils.isEmptyString(mail.getSenderMessageId())) {
      errLst.add("SenderMessageId");
    }
    if (mail.getMSHOutPayload() == null || mail.getMSHOutPayload().
            getMSHOutParts().isEmpty()) {
      errLst.add("No content in mail (Attachment is empty)!");
    }
    int iMP = 0;
    for (MSHOutPart mp : mail.getMSHOutPayload().getMSHOutParts()) {
      iMP++;
      if (Utils.isEmptyString(mp.getMimeType())) {
        errLst.add("Mimetype (index:'" + iMP + "')!");
      }
      // check payload
      if (Utils.isEmptyString(mp.getFilepath()) || !(new File(mp.getFilepath()).
              exists())) {
        errLst.add(
                "No payload content. Add value or existing file (index:'" + iMP + "')!");
      }
    }
    if (Utils.isEmptyString(mail.getReceiverName())) {
      errLst.add("ReceiverName");
    }
    if (Utils.isEmptyString(mail.getReceiverEBox())) {
      errLst.add("ReceiverEBox");
    }
    if (Utils.isEmptyString(mail.getSenderName())) {
      errLst.add("SenderName");
    }
    if (Utils.isEmptyString(mail.getSenderEBox())) {
      errLst.add("SenderEBox");
    }
    if (Utils.isEmptyString(mail.getService())) {
      errLst.add("Service");
    }
    if (Utils.isEmptyString(mail.getAction())) {
      errLst.add("Action");
    }

    if (Utils.isEmptyString(mail.getConversationId())) {
      warnLst.add("Missing ConversationId (new is created)");
      mail.setConversationId(Utils.getUUIDWithDomain(SEDSystemProperties.
              getLocalDomain()));
    }
    if (!errLst.isEmpty()) {
      throw new FSException(String.format(
              "Missing data (" + errLst.size() + "):" + String.
              join(", ", errLst)));

    }
    return warnLst;

  }

}
