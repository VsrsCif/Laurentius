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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.laurentius.msh.outbox.payload.MSHOutPayload;
import si.laurentius.msh.pmode.PMode;
import si.laurentius.msh.pmode.PartyIdentitySet;
//import org.msh.svev.pmode.PMode;
import si.laurentius.cron.SEDTaskType;
import si.laurentius.cron.SEDTaskTypeProperty;
import si.laurentius.commons.MimeValues;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.SEDOutboxMailStatus;
import si.laurentius.commons.exception.PModeException;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.PModeInterface;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.interfaces.TaskExecutionInterface;
import si.laurentius.commons.interfaces.exception.TaskException;
import si.laurentius.commons.pmode.EBMSMessageContext;
//import si.laurentius.commons.utils.PModeManager;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.StringFormater;
import si.laurentius.commons.utils.Utils;
import si.laurentius.task.exception.FSException;

/**
 *
 * @author Jože Rihtaršič
 */
@Stateless
@Local(TaskExecutionInterface.class)
public class TaskFileSubmitter implements TaskExecutionInterface {


  
  
  private static final DateFormat SDF = SimpleDateFormat.getDateTimeInstance(
      SimpleDateFormat.DEFAULT, SimpleDateFormat.DEFAULT);

  private static final Pattern EMAIL_PATTEREN = Pattern.compile("^.+@.+(\\\\.[^\\\\.]+)+$");
  private static final String OUTMAIL_FILENAME = "outmail";
  private static final String OUTMAIL_SUFFIX = ".txt";
  private static final String OUTMAIL_SUFFIX_PROCESS = ".process";
  private static final String OUTMAIL_SUFFIX_ERROR = ".error";
  private static final String OUTMAIL_SUFFIX_SUBMITTED = ".submitted";

  private static final String PROP_SENDER_MSG_ID = "senderMessageId";
  private static final String PROP_SERVICE = "service";
  private static final String PROP_ACTION = "action";
  private static final String PROP_SENDER_EBOX = "senderEBox";
  private static final String PROP_RECEIVER_EBOX = "receiverEBox";
  private static final String PROP_PAYLOAD = "payload";

  /**
   *
   */
  public static String KEY_EXPORT_FOLDER = "file.submit.folder";

  StorageUtils mSU = new StorageUtils();

  String outFileFormat = "%s_%03d.xml";

  private static final SEDLogger LOG = new SEDLogger(TaskFileSubmitter.class);

  @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
  SEDDaoInterface mdao;

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  SEDLookupsInterface mLookups;
  
  @EJB (mappedName = SEDJNDI.JNDI_PMODE)
  protected PModeInterface mpModeManager;

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
      sw.append("Submit folder: " + sfolder + " not exists!");
    } else if (!fRoot.isDirectory()) {
      sw.append("Submit folder: " + sfolder + " is not a folder!");
    } else {
      File[] flst =
          fRoot.listFiles((File dir, String name) -> name.startsWith(OUTMAIL_FILENAME) &&
               name.endsWith(OUTMAIL_SUFFIX));
      for (File file : flst) {
        LOG.log("check file data: " + file.getName());

        try {
          if (!isFileLocked(file)) {
            Properties lock = new Properties();
            lock.setProperty("start.submitting", SDF.format(Calendar.getInstance().getTime()));

            File fMetaData = new File(file.getAbsolutePath() + OUTMAIL_SUFFIX_PROCESS);
            try (FileOutputStream fosMD = new FileOutputStream(fMetaData)) {
              lock.store(fosMD, "OutMail proccessed");
            }

            Properties pmail = new Properties();

            try (FileInputStream fp = new FileInputStream(file)) {

              pmail.load(fp);

              BigInteger bi = processOutMail(pmail, fMetaData);
              File fewFMetaData = new File(file.getAbsolutePath() + OUTMAIL_SUFFIX_SUBMITTED);
              if (fMetaData.renameTo(fewFMetaData)) {
                fMetaData = fewFMetaData;
                iVal++;
                try (FileOutputStream fos = new FileOutputStream(fMetaData, true)) {
                  PrintStream ps = new PrintStream(fos);
                  if (bi != null) {
                    ps.append("Laurentius.id=");
                    ps.append(bi.toString());
                    ps.append("\n");
                  }
                }
              } else {
                LOG.logError(l, "Error rename status file fpr: " + file.getAbsolutePath(), null);
              }

            } catch (IOException ex) {
              LOG.logError(l, "Error reading outmail data: " + file.getAbsolutePath(), ex);
              File fewFMetaData = new File(file.getAbsolutePath() + OUTMAIL_SUFFIX_ERROR);
              if (fMetaData.renameTo(fewFMetaData)) {
                try (FileOutputStream fos = new FileOutputStream(fMetaData, true)) {
                  PrintStream ps = new PrintStream(fos);
                  ex.printStackTrace(ps);
                }
              } else {
                LOG.logError(l, "Error rename status file fpr: " + file.getAbsolutePath(), null);
              }
            } catch (FSException ex) {
              LOG.logError(l, "Error subbmitting mail: " + file.getAbsolutePath(), ex);
              File fewFMetaData = new File(file.getAbsolutePath() + OUTMAIL_SUFFIX_ERROR);
              if (fMetaData.renameTo(fewFMetaData)) {
                fMetaData = fewFMetaData;
                try (FileOutputStream fos = new FileOutputStream(fMetaData, true)) {
                  PrintStream ps = new PrintStream(fos);
                  ex.printStackTrace(ps);
                }
              } else {
                LOG.logError(l, "Error rename status file fpr: " + file.getAbsolutePath(), null);
              }
            }
          }

        } catch (IOException ex) {
          LOG.logError(l, "Errror reading outmail data: " + file.getAbsolutePath(), ex);
        }

      }

    }
    sw.append("Submited '" + iVal + "' from folder: " + sfolder);
    sw.append("backup ends in : " + (l - LOG.getTime()) + " ms\n");
    LOG.logEnd(l);
    return sw.toString();
  }

  private boolean isFileLocked(File f)
      throws IOException {

    boolean bVal = false;
    for (File smbl : f.getParentFile().listFiles()) {
      if (smbl.isFile() &&
           !smbl.equals(f) &&
           (smbl.getName().equals(f.getName() + OUTMAIL_SUFFIX_ERROR) ||
           smbl.getName().equals(f.getName() + OUTMAIL_SUFFIX_PROCESS) || smbl.getName()
          .equals(f.getName() + OUTMAIL_SUFFIX_SUBMITTED))) {
        bVal = true;
        break;
      }

    }
    return bVal;
  }

  private BigInteger processOutMail(Properties p, File fMetaData)
      throws FSException {
    long l = LOG.logStart();
    // validate data
    BigInteger res = null;
    String msgId = readProperty(p, PROP_SENDER_MSG_ID, true);
    String service = readProperty(p, PROP_SERVICE, true);
    String action = readProperty(p, PROP_ACTION, true);
    String sender = readProperty(p, PROP_SENDER_EBOX, true);
    String receiver = readProperty(p, PROP_RECEIVER_EBOX, true);
    String payload = readProperty(p, PROP_PAYLOAD, true);
    String[] lst = payload.split(";");
    // validate data
    /*
     * if (isValidMailAddress(sender)) { throw new FSException("Sender SEDBox: '" + sender +
     * "' is not valid!"); } if (isValidMailAddress(receiver)) { throw new
     * FSException("Receiver SEDBox: '" + receiver + "' is not valid!"); }
     */

    if (lst == null || lst.length == 0) {
      throw new FSException("No payload to submit");
    }

    MSHOutMail mout = new MSHOutMail();
    mout.setMessageId(Utils.getInstance().getGuidString());
    mout.setService(service);
    mout.setAction(action);
    mout.setConversationId(msgId);
    mout.setSenderEBox(sender);
    mout.setSenderName(sender);
    mout.setRefToMessageId(msgId);
    mout.setReceiverEBox(receiver);
    mout.setReceiverName(receiver);
    mout.setSubject(service + " " + msgId);
    // prepare mail to persist
    Date dt = new Date();
    // set current status
    mout.setStatus(SEDOutboxMailStatus.SUBMITTED.getValue());
    mout.setSubmittedDate(dt);
    mout.setStatusDate(dt);

    mout.setMSHOutPayload(new MSHOutPayload());

    for (String fn : lst) {
      File f = new File(fn);
      if (!f.exists()) {
        f = new File(fMetaData.getParentFile(), fn);
        if (!f.exists()) {
          throw new FSException("File: '" + fn + "' not exists ");
        }
      }

      File fNew;
      String mimeType = MimeValues.getMimeTypeByFileName(fn);
      try {
        fNew = mSU.storeOutFile(mimeType, f);
      } catch (StorageException ex) {
        throw new FSException("Error reading file: '" + f.getAbsolutePath() + "' not exists ", ex);
      }

      MSHOutPart mp = new MSHOutPart();
      mp.setDescription(f.getName());
      try {
        mp.setFilepath(StorageUtils.getRelativePath(fNew));
      } catch (StorageException ex) {
        String errDesc =
            "Error getting relative path for file : '" + fNew.getAbsolutePath() + "'. Err:" +
            ex.getMessage() +
             ".  Message with id '" + mout.getMessageId() + "' is not procesed!";

        throw new FSException(errDesc, ex);
      }
      mp.setMimeType(mimeType);
      mout.getMSHOutPayload().getMSHOutParts().add(mp);
    }

    
    EBMSMessageContext ectx = null;
    try {
      // validate message 
      ectx = mpModeManager.createMessageContextForOutMail(mout);      
    } catch (PModeException ex) {
      String errDesc =
          "Error configurating send context for mail. Err:" + ex.getMessage() +
           ".  Message with id '" + mout.getMessageId() + "' is not procesed!";

      throw new FSException(errDesc, ex);
    }
    

    try {
      mdao.serializeOutMail(mout, "", "file-submitter", ectx.getPMode().getId());
      res = mout.getId();
    } catch (StorageException ex) {
      throw new FSException("Error serializing mail", ex);
    }
    LOG.logEnd(l);
    return res;
  }

  private String readProperty(Properties p, String prpKEy, boolean required)
      throws FSException {
    if (!p.containsKey(prpKEy)) {
      if (required) {
        throw new FSException("Missing property: " + prpKEy);
      }
    } else {
      return p.getProperty(prpKEy);
    }
    return null;

  }

  /**
   *
   * @return
   */
  @Override
  public SEDTaskType getTaskDefinition() {
    SEDTaskType tt = new SEDTaskType();
    tt.setType("filesubmitter");
    tt.setName("File subbmiter");
    tt.setDescription(
        "Tasks submits mail in given folder. Mail must be in form: 'receiver-box_service_action'");
    tt.getSEDTaskTypeProperties().add(createTTProperty(KEY_EXPORT_FOLDER, "Submit folder"));

    return tt;
  }

  private SEDTaskTypeProperty createTTProperty(String key, String desc, boolean mandatory,
      String type, String valFormat, String valList) {
    SEDTaskTypeProperty ttp = new SEDTaskTypeProperty();
    ttp.setKey(key);
    ttp.setDescription(desc);
    ttp.setMandatory(mandatory);
    ttp.setType(type);
    ttp.setValueFormat(valFormat);
    ttp.setValueList(valList);
    return ttp;
  }

  private SEDTaskTypeProperty createTTProperty(String key, String desc) {
    return createTTProperty(key, desc, true, "string", null, null);
  }

  /**
   *
   * @param address
   * @return
   */
  public static boolean isValidMailAddress(String address) {
    return address != null && EMAIL_PATTEREN.matcher(address).matches();

  }
}
