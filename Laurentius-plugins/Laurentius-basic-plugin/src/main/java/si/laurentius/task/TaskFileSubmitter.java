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
import java.util.List;
import java.util.Properties;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import si.laurentius.commons.enums.MimeValue;
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
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.laurentius.msh.outbox.payload.MSHOutPayload;
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
public class TaskFileSubmitter implements TaskExecutionInterface {
  /**
   *
   */
  public static  final String KEY_EXPORT_FOLDER = "file.submit.folder";
  private static final SEDLogger LOG = new SEDLogger(TaskFileSubmitter.class);

    
  private static final String OUTMAIL_FILENAME = "outmail";
  private static final String OUTMAIL_SUFFIX = ".txt";
  private static final String OUTMAIL_SUFFIX_ERROR = ".error";
  private static final String OUTMAIL_SUFFIX_PROCESS = ".process";
  private static final String OUTMAIL_SUFFIX_SUBMITTED = ".submitted";

  private static final String PROP_ACTION = "action";
  private static final String PROP_PAYLOAD = "payload";
  private static final String PROP_RECEIVER_EBOX = "receiverEBox";
  private static final String PROP_SENDER_EBOX = "senderEBox";
  private static final String PROP_SENDER_MSG_ID = "senderMessageId";
  private static final String PROP_SERVICE = "service";
  private final DateFormat SDF = SimpleDateFormat.getDateTimeInstance(
          SimpleDateFormat.DEFAULT, SimpleDateFormat.DEFAULT);
  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
          SEDLookupsInterface mLookups;

  StorageUtils mSU = new StorageUtils();



  @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
  SEDDaoInterface mdao;

  
  @EJB (mappedName = SEDJNDI.JNDI_PMODE)
  protected PModeInterface mpModeManager;
  private CronTaskPropertyDef createTTProperty(String key, String desc, boolean mandatory,
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
      File[] flst =
          fRoot.listFiles((File dir, String name) -> name.startsWith(OUTMAIL_FILENAME) &&
               name.endsWith(OUTMAIL_SUFFIX));
      for (File file : flst) {
        try {
          if (isFileLocked(file)) {
            LOG.formatedDebug("File: " + file.getName() + " is locked or submitted: abort submitting file");
          }
          else  {
            Properties lock = new Properties();
            lock.setProperty("start.submitting", SDF.format(Calendar.getInstance().getTime()));

            File fMetaData = new File(file.getAbsolutePath() + OUTMAIL_SUFFIX_PROCESS);
            try (FileOutputStream fosMD = new FileOutputStream(fMetaData)) {
              LOG.log("Lock file: " + file.getName() + " - create new process file");
              lock.store(fosMD, "OutMail proccessed");
            }

            Properties pmail = new Properties();

            try (FileInputStream fp = new FileInputStream(file)) {
              LOG.formatedDebug("Read file: '%s'. " ,file.getName());
              pmail.load(fp);
              if (!pmail.containsKey(PROP_SERVICE) ){
                pmail.setProperty(PROP_SERVICE, p.getProperty(PROP_SERVICE));
              }
              if (!pmail.containsKey(PROP_ACTION)){
                pmail.setProperty(PROP_ACTION, p.getProperty(PROP_ACTION));
              }
              if (!pmail.containsKey(PROP_RECEIVER_EBOX)){
                pmail.setProperty(PROP_RECEIVER_EBOX, p.getProperty(PROP_RECEIVER_EBOX));
              }
              if (!pmail.containsKey(PROP_SENDER_EBOX)){
                pmail.setProperty(PROP_SENDER_EBOX, p.getProperty(PROP_SENDER_EBOX));
          
              }

              BigInteger bi = processOutMail(pmail, fMetaData);
              LOG.formatedDebug("Submit file %s. MailId %d", file.getName(),  bi);
              File fewFMetaData = new File(file.getAbsolutePath() + OUTMAIL_SUFFIX_SUBMITTED);
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
    sw.append("Submited '" + iVal + "'" );
    sw.append(" in : " + (LOG.getTime()-l) + " ms\n");
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
    tt.setType("filesubmitter");
    tt.setName("File subbmiter");
    tt.setDescription(
            "Task submits mail in given folder. '");
    tt.getCronTaskPropertyDeves().add(createTTProperty(KEY_EXPORT_FOLDER, "Submit folder", true,
                    "string", null, null, "${laurentius.home}/submit/dwr/"));
    tt.getCronTaskPropertyDeves().add(createTTProperty(PROP_SERVICE, "Service", false,
                    "string", null, null, "DeliveryWithReceipt"));
    tt.getCronTaskPropertyDeves().add(createTTProperty(PROP_ACTION, "Action", false,
                    "string", null, null, "Delivery"));
    tt.getCronTaskPropertyDeves().add(createTTProperty(PROP_RECEIVER_EBOX, "Receiver box", false,
                    "string", null, null, null));
    tt.getCronTaskPropertyDeves().add(createTTProperty(PROP_SENDER_EBOX, "Sender box", false,
                    "string", null, null, null));
    
    return tt;
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
    String senderName = "";
    String senderBox = readProperty(p, PROP_SENDER_EBOX, true);
    String receiverName = "";
    String receiverBox = readProperty(p, PROP_RECEIVER_EBOX, true);
    String payload = readProperty(p, PROP_PAYLOAD, true);
    String[] lst = payload.split(";");
    
    if (senderBox.contains("@")){
      senderName = senderBox.substring(0,senderBox.indexOf('@') );
      if (!senderBox.endsWith("@"+SEDSystemProperties.getLocalDomain())){
         throw new FSException(String.format("Sender box '%s' do not match local domain '%s'", senderBox, SEDSystemProperties.getLocalDomain()));
      }
      if (mLookups.getSEDBoxByAddressName(senderBox)==null){
        throw new FSException(String.format("Sender box '%s' do not exists!'", senderBox));
      }
    }else {
      senderName = senderBox;
      senderBox +="@"+SEDSystemProperties.getLocalDomain();
    }
    if (receiverBox.contains("@")){
      receiverName = receiverBox.substring(0,receiverBox.indexOf('@') );
    }else {
      receiverName = receiverBox;
      receiverBox +="@"+SEDSystemProperties.getLocalDomain();
      
      if (mLookups.getSEDBoxByAddressName(receiverBox)==null){
        throw new FSException(String.format("Receiver box '%s' do not exists!'", receiverBox));
      }
    }
    
   
    if (lst.length == 0) {
      throw new FSException("No payload to submit");
    }
    
    if (!Utils.isEmptyString(service)) {
      List<MSHOutMail>  lstSendMail = mdao.getMailBySenderMessageId(MSHOutMail.class, msgId);
      if (!lstSendMail.isEmpty()) {
        throw new FSException(String.format("Message with senderMessageId '%s' already submitted %s (cnt: %d)",  msgId,lstSendMail.get(0).getSubmittedDate().toString(), lstSendMail.size()) );
      }
    }
    
    MSHOutMail mout = new MSHOutMail();
    mout.setSenderMessageId(msgId);
    mout.setService(service);
    mout.setAction(action);
    
    mout.setSenderEBox(senderBox);
    mout.setSenderName(senderName);
    mout.setRefToMessageId(msgId);
    mout.setReceiverEBox(receiverBox);
    mout.setReceiverName(receiverName);
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
      String mimeType = MimeValue.getMimeTypeByFileName(fn);
      try {
        fNew = mSU.storeOutFile(mimeType, f);
      } catch (StorageException ex) {
        throw new FSException("Error reading file: '" + f.getAbsolutePath() + "' not exists ", ex);
      }

      MSHOutPart mp = new MSHOutPart();
      mp.setDescription(f.getName());
      mp.setIsSent(Boolean.TRUE);
      mp.setIsReceived(Boolean.FALSE);
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


}
