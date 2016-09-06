/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.laurentius.task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.xml.bind.JAXBException;
import si.laurentius.msh.inbox.event.MSHInEvent;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.inbox.mail.MSHInMailList;
import si.laurentius.msh.inbox.payload.MSHInPart;
import si.laurentius.msh.outbox.event.MSHOutEvent;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.outbox.mail.MSHOutMailList;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.laurentius.cron.SEDTaskType;
import si.laurentius.cron.SEDTaskTypeProperty;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.SEDDaoInterface;
import si.laurentius.commons.interfaces.SEDLookupsInterface;
import si.laurentius.commons.interfaces.TaskExecutionInterface;
import si.laurentius.commons.interfaces.exception.TaskException;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.StringFormater;
import si.laurentius.commons.utils.Utils;
import si.laurentius.commons.utils.xml.XMLUtils;

/**
 *
 * @author sluzba
 */
@Stateless
@Local(TaskExecutionInterface.class)
public class TaskBackup implements TaskExecutionInterface {

  /**
     *
     */
  public static String KEY_EXPORT_FOLDER = "backup.folder";

  /**
     *
     */
  public static String KEY_CHUNK_SIZE = "backup.chunk.size";

  /**
     *
     */
  public static String KEY_DELETE_OLD = "backup.clear.first";

  /**
     *
     */
  public static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");

  /**
     *
     */
  public static String STORAGE_FOLDER = "storage";

  StorageUtils mSU = new StorageUtils();

  String outFileFormat = "%s_%03d.xml";

  private static final SEDLogger LOG = new SEDLogger(TaskBackup.class);

  @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
  SEDDaoInterface mdao;

  @EJB(mappedName = SEDJNDI.JNDI_SEDLOOKUPS)
  SEDLookupsInterface mLookups;

  /**
   *
   * @param p
   * @return
   */
  @Override
  public String executeTask(Properties p) throws TaskException {
    long l = LOG.logStart();
    String backupFolder = sdf.format(Calendar.getInstance().getTime());
    StringWriter sw = new StringWriter();
    sw.append("Start backup: ");
    sw.append(backupFolder);
    sw.append("\n");

    String sfolder;
    boolean bDelOldFolder;
    int iChunkSize;

    if (!p.containsKey(KEY_EXPORT_FOLDER)) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException,
          "Missing parameter:  '" + KEY_EXPORT_FOLDER + "'!");
    }
    sfolder = p.getProperty(KEY_EXPORT_FOLDER);

    if (!p.containsKey(KEY_DELETE_OLD)) {
      bDelOldFolder = false;
    } else {
      bDelOldFolder = p.getProperty(KEY_DELETE_OLD).trim().equalsIgnoreCase("true");
    }
    if (!p.containsKey(KEY_CHUNK_SIZE)) {
      iChunkSize = 1000;
    } else {
      try {
        iChunkSize = Integer.parseInt(p.getProperty(KEY_CHUNK_SIZE).trim());
      } catch (NumberFormatException nfe) {
        iChunkSize = 1000;
        String msg = " Bad chunk size parameter: '" + p.getProperty(KEY_CHUNK_SIZE) + "' ";
        sw.append(msg);
        LOG.logWarn(l, msg, nfe);
      }
    }

    sw.append("- Init folders:");
    long lst = LOG.getTime();
    File bckFolder = initFolders(sfolder, backupFolder, bDelOldFolder);
    sw.append(" end: " + (lst - LOG.getTime()) + " ms\n");

    sw.append("- Backup settings and lookups");
    lst = LOG.getTime();
    mLookups.exportLookups(bckFolder, true);
    sw.append(" end: " + (lst - LOG.getTime()) + " ms\n");

    sw.append("---------------------\nBackup out mail\n");
    lst = LOG.getTime();
    String rs = archiveOutMails(null, bckFolder, iChunkSize);
    sw.append(rs);
    sw.append(" end: " + (lst - LOG.getTime()) + " ms\n---------------------\n\n");

    sw.append("---------------------\nBackup in mail");
    lst = LOG.getTime();
    rs = archiveInMails(null, bckFolder, iChunkSize);
    sw.append(rs);
    sw.append(" end: " + (lst - LOG.getTime()) + " ms\n---------------------\n\n");

    sw.append("backup ends in : " + (l - LOG.getTime()) + " ms\n");
    LOG.logEnd(l);
    return sw.toString();
  }

  private File initFolders(String rootFolder, String bckFolder, boolean clearFirst)
      throws TaskException {
    File f = new File(StringFormater.replaceProperties(rootFolder));
    if (f.exists() && clearFirst) {
      try {
        removeRecursive(f.toPath());
      } catch (IOException ex) {
        throw new TaskException(TaskException.TaskExceptionCode.InitException,
            "Could not remove folder: '" + f.getAbsolutePath() + "'!", ex);
      }
    }

    if (!f.exists() && !f.mkdirs()) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException,
          "Could not create folder: '" + f.getAbsolutePath() + "'!");
    }
    File bck = new File(f, bckFolder);
    if (bck.exists()) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException, "Backup folder: '"
          + bck.getAbsolutePath() + "' already exists. " + "Backup would overwrite folder content!");
    } else if (!bck.mkdirs()) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException,
          "Could not create folder: '" + bck.getAbsolutePath() + "'!");

    }

    File bckStrg = new File(bck, STORAGE_FOLDER);
    if (!bckStrg.mkdirs()) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException,
          "Could not create folder: '" + bckStrg.getAbsolutePath() + "'!");

    }
    return bck;
  }

  /**
   *
   * @param to
   * @param f
   * @param iChunkSize
   * @return
   */
  public String archiveOutMails(Date to, File f, int iChunkSize) throws TaskException {
    StringWriter sw = new StringWriter();
    MSHOutMailList noList = new MSHOutMailList();
    SearchParameters sp = new SearchParameters();
    sp.setSubmittedDateTo(to);
    long l = mdao.getDataListCount(MSHOutMail.class, sp);
    sw.append("\tbackup " + l + " outmail\n");
    long pages = l / iChunkSize + 1;

    int iPage = 0;
    while (iPage < pages) {

      List<MSHOutMail> lst =
          mdao.getDataList(MSHOutMail.class, (iPage++) * iChunkSize, iChunkSize, "Id", "ASC", sp);
      if (!lst.isEmpty()) {
        noList.setCount(lst.size());
        for (MSHOutMail m : lst) {
          // add events
          List<MSHOutEvent> le = mdao.getMailEventList(MSHOutEvent.class, m.getId());
          m.getMSHOutEvents().addAll(le);
          // backup binaries
          if (m.getMSHOutPayload() != null && !m.getMSHOutPayload().getMSHOutParts().isEmpty()) {
            for (MSHOutPart p : m.getMSHOutPayload().getMSHOutParts()) {
              if (p.getFilepath() != null) {
                try {
                  mSU.copyFileToFolder(p.getFilepath(), f);
                } catch ( StorageException ex) {
                  throw new TaskException(TaskException.TaskExceptionCode.ProcessException,
                      "Error occured while copying  file : '" + p.getFilepath() + "'!", ex);
                }
              }
            }
          }
        }

        noList.getMSHOutMails().addAll(lst);
        File fout = new File(f, String.format(outFileFormat, "MSHOutMail", iPage));

        try {
          XMLUtils.serialize(noList, fout);
        } catch (JAXBException | FileNotFoundException ex) {
          throw new TaskException(TaskException.TaskExceptionCode.ProcessException,
              "Error occured while exporting out data : '" + f.getFreeSpace() + "'!", ex);
        }
        String strVal =
            "Exported page " + iPage + " size: " + lst.size() + " to " + fout.getAbsolutePath();
        sw.append("\t" + strVal + "\n");

        LOG.log(strVal);
      }
    }
    return sw.toString();

  }

  /**
   *
   * @param to
   * @param f
   * @param iChunkSize
   * @return
   * @throws TaskException
   */
  public String archiveInMails(Date to, File f, int iChunkSize) throws TaskException {
    StringWriter sw = new StringWriter();
    MSHInMailList noList = new MSHInMailList();
    SearchParameters sp = new SearchParameters();
    sp.setSubmittedDateTo(to);
    long l = mdao.getDataListCount(MSHOutMail.class, sp);
    sw.append("\tbackup " + l + " inmail\n");
    long pages = l / iChunkSize + 1;

    int iPage = 0;
    while (iPage < pages) {

      List<MSHInMail> lst =
          mdao.getDataList(MSHInMail.class, (iPage++) * iChunkSize, iChunkSize, "Id", "ASC", sp);
      if (!lst.isEmpty()) {
        noList.setCount(lst.size());
        for (MSHInMail m : lst) {
          // add events
          List<MSHInEvent> le = mdao.getMailEventList(MSHInEvent.class, m.getId());
          m.getMSHInEvents().addAll(le);
          // backup binaries
          if (m.getMSHInPayload() != null && !m.getMSHInPayload().getMSHInParts().isEmpty()) {
            for (MSHInPart p : m.getMSHInPayload().getMSHInParts()) {
              if (p.getFilepath() != null) {
                try {
                  mSU.copyFileToFolder(p.getFilepath(), f);
                } catch ( StorageException ex) {
                  throw new TaskException(TaskException.TaskExceptionCode.ProcessException,
                      "Error occured while copying  file : '" + p.getFilepath() + "'!", ex);
                }
              }
            }
          }
        }

        noList.getMSHInMails().addAll(lst);
        File fout = new File(f, String.format(outFileFormat, "MSHInMail", iPage));

        try {
          XMLUtils.serialize(noList, fout);
        } catch (JAXBException | FileNotFoundException ex) {
          throw new TaskException(TaskException.TaskExceptionCode.ProcessException,
              "Error occured while exporting out data : '" + f.getFreeSpace() + "'!", ex);
        }
        String strVal =
            "Exported page " + iPage + " size: " + lst.size() + " to " + fout.getAbsolutePath();
        sw.append("\t" + strVal + "\n");
        LOG.log("Exported page " + iPage + " size: " + lst.size() + " to " + fout.getAbsolutePath());

      }

    }

    return sw.toString();
  }

  /**
   *
   * @param path
   * @throws IOException
   */
  public static void removeRecursive(Path path) throws IOException {
    Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        // try to delete the file anyway, even if its attributes
        // could not be read, since delete-only access is
        // theoretically possible
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        if (exc == null) {
          Files.delete(dir);
          return FileVisitResult.CONTINUE;
        } else {
          // directory iteration failed; propagate exception
          throw exc;
        }
      }
    });
  }

  /*
   * @Override public String getType() { return "backup"; }
   * 
   * @Override public String getName() { return "Backup data"; }
   * 
   * @Override public String getDesc() { return "Backup data to 'xml' and files to backup-storage";
   * }
   * 
   * @Override public Properties getProperties() { Properties p = new Properties();
   * p.setProperty(KEY_EXPORT_FOLDER, "Backup folder"); p.setProperty(KEY_CHUNK_SIZE,
   * "Max mail count in chunk");
   * 
   * p.setProperty(KEY_DELETE_OLD, "Clear backup folder (true/false)"); return p; }
   */
  /**
   *
   * @return
   */
  @Override
  public SEDTaskType getTaskDefinition() {
    SEDTaskType tt = new SEDTaskType();
    tt.setType("backup");
    tt.setName("Backup data");
    tt.setDescription("Backup data to 'xml' and files to backup-storage");
    tt.getSEDTaskTypeProperties().add(createTTProperty(KEY_EXPORT_FOLDER, "Archive folder"));
    tt.getSEDTaskTypeProperties().add(
        createTTProperty(KEY_CHUNK_SIZE, "Max mail count in chunk", true, "int", null, null));
    tt.getSEDTaskTypeProperties().add(
        createTTProperty(KEY_DELETE_OLD, "Clear backup folder (true/false)", true, "boolean", null,
            null));

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

}
