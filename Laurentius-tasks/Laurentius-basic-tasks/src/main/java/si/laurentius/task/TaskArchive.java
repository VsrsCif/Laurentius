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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigInteger;
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
import java.util.logging.Level;
import java.util.logging.Logger;
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
 * @author Jože Rihtaršič
 */
@Stateless
@Local(TaskExecutionInterface.class)
public class TaskArchive implements TaskExecutionInterface {

  /**
     *
     */
  public static String KEY_EXPORT_FOLDER = "archive.folder";

  /**
     *
     */
  public static String KEY_CHUNK_SIZE = "archive.chunk.size";

  /**
     *
     */
  public static String KEY_DELETE_RECORDS = "archive.delete.records";

  /**
     *
     */
  public static String KEY_ARCHIVE_OFFSET = "archive.day.offset";

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

  private static final SEDLogger LOG = new SEDLogger(TaskArchive.class);

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
    sw.append("Start archive: ");
    sw.append(backupFolder);
    sw.append("\n");

    String sfolder;
    boolean bDelRecords;
    int iChunkSize;
    int dayOffset;
    Date dtArhiveTo;

    if (!p.containsKey(KEY_EXPORT_FOLDER)) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException,
          "Missing parameter:  '" + KEY_EXPORT_FOLDER + "'!");
    } else {
      sfolder = p.getProperty(KEY_EXPORT_FOLDER);
    }

    if (!p.containsKey(KEY_ARCHIVE_OFFSET)) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException,
          "Missing parameter:  '" + KEY_ARCHIVE_OFFSET + "'!");
    } else {
      try {
        dayOffset = Integer.parseInt(p.getProperty(KEY_ARCHIVE_OFFSET).trim());
        Calendar cld = Calendar.getInstance();
        cld.add(Calendar.DAY_OF_MONTH, -1 * dayOffset);
        dtArhiveTo = cld.getTime();

      } catch (NumberFormatException nfe) {
        throw new TaskException(TaskException.TaskExceptionCode.InitException, "Bad parameter:  '"
            + KEY_EXPORT_FOLDER + "' - not a number!", nfe);
      }

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

    if (!p.containsKey(KEY_DELETE_RECORDS)) {
      bDelRecords = false;
    } else {
      bDelRecords = p.getProperty(KEY_DELETE_RECORDS).trim().equalsIgnoreCase("true");
    }

    sw.append("- Init folders:");
    long lst = LOG.getTime();
    File archFolder = initFolders(sfolder, backupFolder);

    File fbackMails = new File(archFolder, "backup-mails.txt");

    try (FileWriter fw = new FileWriter(fbackMails)) {

      sw.append(" end: " + (lst - LOG.getTime()) + " ms\n");

      sw.append("- Arhive settings and lookups");
      lst = LOG.getTime();
      mLookups.exportLookups(archFolder, true);
      sw.append(" end: " + (lst - LOG.getTime()) + " ms\n");

      sw.append("---------------------\nArhive out mail\n");
      lst = LOG.getTime();
      String rs = archiveOutMails(dtArhiveTo, archFolder, iChunkSize, fw);
      sw.append(rs);
      sw.append(" end: " + (lst - LOG.getTime()) + " ms\n---------------------\n\n");

      sw.append("---------------------\nArhive in mail");
      lst = LOG.getTime();
      rs = archiveInMails(dtArhiveTo, archFolder, iChunkSize, fw);
      sw.append(rs);
      sw.append(" end: " + (lst - LOG.getTime()) + " ms\n---------------------\n\n");
      fw.flush();
    } catch (IOException ex) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException,
          "Error opening archive list file:  '" + fbackMails.getAbsolutePath() + "'!", ex);
    }

    // delete record if archive succedded
    if (bDelRecords) {

      try (BufferedReader br = new BufferedReader(new FileReader(fbackMails))) {

        String line;
        while ((line = br.readLine()) != null) {
          String data[] = line.split(":");
          if (data[0].equals(MSHOutMail.class.getSimpleName())) {
            try {
              mdao.removeOutMail(new BigInteger(data[1]));
            } catch (StorageException ex) {
              LOG.logError(l, "Error removing archived out mail: " + line, ex);
            }
          } else if (data[0].equals(MSHInMail.class.getSimpleName())) {
            try {
              mdao.removeInMail(new BigInteger(data[1]));
            } catch (StorageException ex) {
              LOG.logError(l, "Error removing archived out mail: " + line, ex);
            }
          } else {
            LOG.logError(l, "Unknown object: " + line, null);
            continue;
          }
          // delete record()
          // data[0] class
          // data[1] id
          String[] files = data[2].split(";");
          for (String file : files) {
            try {
              StorageUtils.removeFile(file);
            } catch (StorageException ex) {
              LOG.logError(0, "Error removing file: " + file, ex);

            }
          }

        }
        // delete records
        // delete files
      } catch (IOException ex) {
        Logger.getLogger(TaskArchive.class.getName()).log(Level.SEVERE, null, ex);
      }

    }

    sw.append("backup ends in : " + (l - LOG.getTime()) + " ms\n");
    LOG.logEnd(l);
    return sw.toString();
  }

  private File initFolders(String rootFolder, String archFolder) throws TaskException {
    File f = new File(StringFormater.replaceProperties(rootFolder));

    if (!f.exists() && !f.mkdirs()) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException,
          "Could not create folder: '" + f.getAbsolutePath() + "'!");
    }
    File bck = new File(f, archFolder);
    if (bck.exists()) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException, "Arhive folder: '"
          + bck.getAbsolutePath() + "' already exists. " + "Arhive would overwrite folder content!");
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
   * @param fw
   * @return
   * @throws IOException
   */
  public String archiveOutMails(Date to, File f, int iChunkSize, Writer fw) throws TaskException,
      IOException {
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
          fw.append(m.getClass().getSimpleName());
          fw.append(":");
          fw.append(m.getId().toString());
          fw.append(":");

          // add events
          List<MSHOutEvent> le = mdao.getMailEventList(MSHOutEvent.class, m.getId());
          m.getMSHOutEvents().addAll(le);
          // backup binaries
          if (m.getMSHOutPayload() != null && !m.getMSHOutPayload().getMSHOutParts().isEmpty()) {
            for (MSHOutPart p : m.getMSHOutPayload().getMSHOutParts()) {
              if (p.getFilepath() != null) {
                try {
                  mSU.copyFileToFolder(p.getFilepath(), f);
                  fw.append(p.getFilepath());
                  fw.append(";");
                } catch (IOException | StorageException ex) {
                  throw new TaskException(TaskException.TaskExceptionCode.ProcessException,
                      "Error occured while copying  file : '" + p.getFilepath() + "'!", ex);
                }
              }
            }
          }
          fw.append("\n");
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
   * @param fw
   * @return
   * @throws TaskException
   * @throws IOException
   */
  public String archiveInMails(Date to, File f, int iChunkSize, Writer fw) throws TaskException,
      IOException {
    StringWriter sw = new StringWriter();
    MSHInMailList noList = new MSHInMailList();
    SearchParameters sp = new SearchParameters();
    sp.setSubmittedDateTo(to);
    long l = mdao.getDataListCount(MSHOutMail.class, sp);
    sw.append("\narchive " + l + " inmail\n");
    long pages = l / iChunkSize + 1;

    int iPage = 0;
    while (iPage < pages) {

      List<MSHInMail> lst =
          mdao.getDataList(MSHInMail.class, (iPage++) * iChunkSize, iChunkSize, "Id", "ASC", sp);
      if (!lst.isEmpty()) {
        noList.setCount(lst.size());
        for (MSHInMail m : lst) {
          fw.append(m.getClass().getSimpleName());
          fw.append(":");
          fw.append(m.getId().toString());
          fw.append(":");
          // add events
          List<MSHInEvent> le = mdao.getMailEventList(MSHInEvent.class, m.getId());
          m.getMSHInEvents().addAll(le);
          // backup binaries
          if (m.getMSHInPayload() != null && !m.getMSHInPayload().getMSHInParts().isEmpty()) {
            for (MSHInPart p : m.getMSHInPayload().getMSHInParts()) {
              if (p.getFilepath() != null) {
                try {
                  mSU.copyFileToFolder(p.getFilepath(), f);
                  fw.append(p.getFilepath());
                  fw.append(";");
                } catch (IOException | StorageException ex) {
                  throw new TaskException(TaskException.TaskExceptionCode.ProcessException,
                      "Error occured while copying  file : '" + p.getFilepath() + "'!", ex);
                }
              }
            }
          }
          fw.append("\n");
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
   * @Override public String getType() { return "archive"; }
   * 
   * @Override public String getName() { return "Archive data"; }
   * 
   * @Override public String getDesc() { return
   * "Archive data to 'xml' and files to archive-storage"; }
   * 
   * @Override public Properties getProperties() { Properties p = new Properties();
   * p.setProperty(KEY_EXPORT_FOLDER, "Archive folder"); p.setProperty(KEY_CHUNK_SIZE,
   * "Max mail count in chunk"); p.setProperty(KEY_DELETE_RECORDS,
   * "Delete exported records (true/false)"); p.setProperty(KEY_ARCHIVE_OFFSET,
   * "Archive records older than [n] days");
   * 
   * return p; }
   */
  /**
   *
   * @return
   */
  @Override
  public SEDTaskType getTaskDefinition() {
    SEDTaskType tt = new SEDTaskType();
    tt.setType("archive");
    tt.setName("Archive data");
    tt.setDescription("Archive data to 'xml' and files to archive-storage");
    tt.getSEDTaskTypeProperties().add(createTTProperty(KEY_EXPORT_FOLDER, "Archive folder"));
    tt.getSEDTaskTypeProperties().add(
        createTTProperty(KEY_CHUNK_SIZE, "Max mail count in chunk", true, "int", null, null));
    tt.getSEDTaskTypeProperties().add(
        createTTProperty(KEY_DELETE_RECORDS, "Delete exported records (true/false)", true,
            "boolean", null, null));
    tt.getSEDTaskTypeProperties().add(
        createTTProperty(KEY_ARCHIVE_OFFSET, "Archive records older than [n] days", true, "int",
            null, null));

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
