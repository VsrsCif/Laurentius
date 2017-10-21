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
package si.laurentius.commons.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import static java.io.File.createTempFile;
import static java.io.File.separator;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import static java.lang.String.format;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import si.laurentius.commons.enums.MimeValue;
import static si.laurentius.commons.enums.MimeValue.getSuffixBYMimeType;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.exception.StorageException;

/**
 * Class handles local storage for binaries attached to mail. Local storage is in
 * ${laurentius.home}/storage folder. Binaries are stored under creation date-named [yyyy/MM/dd_000001]
 * folders. If in folder is more than MAX_FILES_IN_FOLDER files new subfolder is created 001,
 * subfolder has 3 digits with leading '0' for number lower than 1000. 001, 012, 898, 2215, 3656,
 * etc
 *
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public class StorageUtils {
  
  static AtomicInteger CURRENT_SUB_FOLDER_NUMBER  = new AtomicInteger();
  static AtomicInteger CURRENT_FILE_COUNT_IN_FOLDER= new AtomicInteger();

  private static Path CURRENT_PATH;
  public static final int MAX_FILES_IN_FOLDER = 1024;
  private static final SEDLogger LOG = new SEDLogger(StorageUtils.class);
  private static final String S_IN_PREFIX = "IN_";
  private static final String S_OUT_PREFIX = "OUT_";
  private static final String S_ERR_PREFIX = "ERR_";

  /**
   * Current storage folder.
   *
   * @return root folder as defined in system property: ${laurentius.home}/storage/[CURRENT FOLDER - date]/
   * @throws StorageException - if current folder not exists and could not be created
   */
  public static synchronized File currentStorageFolder()
      throws StorageException {
    
    
    LocalDate cld = LocalDate.now();
    // current date
    Path pcDir = dateStorageFolder(cld);
    // check if current path is 'todays' path 
    if (CURRENT_PATH != null && !CURRENT_PATH.startsWith(pcDir)) {
      CURRENT_PATH = null;
    }
    // create new path
    if (CURRENT_PATH == null) {
      int i = getMaxSubFolderNumber(pcDir);
      CURRENT_PATH = pcDir.resolve(format("%03d", i)).toAbsolutePath();
      if (CURRENT_PATH.toFile().exists()) {
      
      try (Stream strPaths = Files.list(CURRENT_PATH)){ 
        CURRENT_FILE_COUNT_IN_FOLDER.set((int)strPaths.count());
      } catch (IOException ex) {
        throw new StorageException(
          format("Error occurred while counting files if storage folder: '%s'",
              CURRENT_PATH.toFile()), ex);
      }
      } else {
      CURRENT_FILE_COUNT_IN_FOLDER.set(0);
      }
    }

    //try { JRC 20.09.2016
      // check max number
      if (Files.exists(CURRENT_PATH, LinkOption.NOFOLLOW_LINKS) &&
          CURRENT_FILE_COUNT_IN_FOLDER.get()>= MAX_FILES_IN_FOLDER
          //Files.list(CURRENT_PATH).count() > MAX_FILES_IN_FOLDER
          
          ) {

        int i = getMaxSubFolderNumber(pcDir) + 1;
        CURRENT_PATH = pcDir.resolve(format("%03d", i)).normalize().toAbsolutePath();
        CURRENT_FILE_COUNT_IN_FOLDER.set(0);

      }
    /*} catch (IOException ex) {
      throw new StorageException(
          format("Error occurred while creating current storage folder: '%s'",
              CURRENT_PATH.toFile()), ex);
    }*/
    File f = CURRENT_PATH.toFile();
    if (!f.exists() && !f.mkdirs()) {
      throw new StorageException(
          format("Error occurred while creating current storage folder: '%s'",
              f.getAbsolutePath()));
    }
    return f;
  }

  /**
   * Method returs path for LocalDate ${laurentius.home}/storage/[year]/[month]/[day]
   *
   * @param ld - Local date
   * @return returs path
   */
  protected static synchronized Path dateStorageFolder(LocalDate ld) {
    return Paths.get(SEDSystemProperties.getStorageFolder().getAbsolutePath(),
        ld.getYear() + "",
        format("%02d", ld.getMonthValue()),
        format("%02d", ld.getDayOfMonth()));
  }

  /**
   * File to relative storage path ${laurentius.home}/[storagePath]
   *
   * @param storagePath
   * @return File to to relative storage path
   */
  public static synchronized File getFile(String storagePath) {
    return new File(SEDSystemProperties.getStorageFolder(),  storagePath);
  }

  /**
   * Method returs max subfolder number. If storage folder has more than MAX_FILES_IN_FOLDER new
   * subfolder is created. Method resturms maxnumber if subfolder number in current date folder
   * ${laurentius.home}/storage/[year]/[month]/[day]/[number]
   *
   * @param dateDir - Current date dir ${laurentius.home}/storage/[year]/[month]/[day]
   * @return returs max subfolder number. If there is no subfolder method returs 1
   */
  protected static synchronized int getMaxSubFolderNumber(Path dateDir) {
    long l = LOG.getTime();
    int iMaxFolder = 1;

    if (dateDir != null && Files.exists(dateDir, LinkOption.NOFOLLOW_LINKS) && Files.isDirectory(
        dateDir, LinkOption.NOFOLLOW_LINKS)) {
      try (DirectoryStream<Path> stream = Files.newDirectoryStream(dateDir, (Path file) ->
          (Files.isDirectory(file)))) {
        for (Path path : stream) {
          String name = path.getFileName().toString();
          try {
            int i = Integer.parseInt(name);
            iMaxFolder = i > iMaxFolder ? i : iMaxFolder;
          } catch (NumberFormatException ne) {
            LOG.logWarn(l, format("Subfolder '%s' is not a number! Path: %s", name,
                dateDir.toFile().getAbsolutePath()), ne);
          }
        }
      } catch (IOException e) {
        LOG.logWarn(l, format("Error reading subfolders for path '%s'",
            dateDir.toFile().getAbsolutePath()), e);
      }
    }
    return iMaxFolder;
  }

  /**
   * create new empty file in current storage folder
   *
   * @param suffix - file suffix (usually mime suffix as .pdf, .xml, etc. )
   * @param prefix - file prefix (in_/out_)
   * @return new storage file
   * @throws StorageException
   */
  public static File getNewStorageFile(String suffix, String prefix)
      throws StorageException {
    File fStore;
    try {
      fStore = createTempFile(prefix, "." + suffix, currentStorageFolder());
      CURRENT_FILE_COUNT_IN_FOLDER.incrementAndGet();
    } catch (IOException ex) {
      throw new StorageException("Error occurred while creating storage file", ex);
    }
    return fStore;
  }

  /**
   * Returns relative string path for file
   *
   * @param path - input file in storage
   * @return String - relative path
   * @throws si.laurentius.commons.exception.StorageException
   */
  public static String getRelativePath(File path)
      throws StorageException {

    File hdir = SEDSystemProperties.getStorageFolder();

    if (path.getAbsolutePath().startsWith(hdir.getAbsolutePath())) {
      String rp = path.getAbsolutePath().substring(hdir.getAbsolutePath().length());
      rp = rp.startsWith(separator) ? rp.substring(1) : rp;
      return rp;
    } else {
      throw new StorageException(format("File: '%s' is not in storage '%s'",
          path.getAbsolutePath(), hdir.getAbsolutePath()));
    }
  }



  /**
   * Mehotd return ${laurentius.home}/storage folder as File object.
   *
   * @return return storage folder
   */
  public static File getStorageFolder() {
    return SEDSystemProperties.getStorageFolder();
  }

  /**
   * delete file in storage.
   *
   * @param strInFileName - relative file path
   * @throws StorageException
   */
  public static synchronized void removeFile(String strInFileName)
      throws StorageException {
    File f = getFile(strInFileName);
    if (!f.isFile()) {
      throw new StorageException(format("Path %s is not a file", f.getAbsoluteFile()));
    }
    if (!f.delete()) {
      throw new StorageException(format("File %s was not deleted", f.getAbsoluteFile()));
    }
  }

  /**
   * Copy file to target file
   *
   * @param sourceFile - source - the file to copy
   * @param destFile - target file (may be associated with a different provider to the source path)
   * @param replaceExisting - replace if target file exists
   * @throws si.laurentius.commons.exception.StorageException, file already exists, error reading file
   */
  public static void copyFile(File sourceFile, File destFile, boolean replaceExisting)
      throws StorageException {

    try {
      if (replaceExisting) {
        Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES,
            StandardCopyOption.REPLACE_EXISTING);
      } else {
        Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
      }
    } catch (FileAlreadyExistsException fe) {
      throw new StorageException(format("Could not copy! Dest file already exists: '%s'",
          destFile.getAbsolutePath()), fe);
    } catch (IOException ex) {
      throw new StorageException(format("Error copying file!: '%s' to file: %s",
          sourceFile.getAbsolutePath(), destFile.getAbsolutePath()), ex);
    }

  }

  /**
   * Copy relative storage file to dest folder.
   *
   * @param storageFilePath - relative storage file path
   * @param folder - deset folder
   * @return dest file
   * @throws StorageException - if dest folder could not be created of file could not be copied to
   * dest folder
   */
  public File copyFileToFolder(String storageFilePath, File folder)
      throws StorageException {
    
    return copyFileToFolder(storageFilePath, folder, false);
  }
  
  /**
   * Copy relative storage file to dest folder.
   *
   * @param storageFilePath - relative storage file path
   * @param folder - deset folder
   * @param bOverWrite . overwrite dest file if exists
   * @return dest file
   * @throws StorageException - if dest folder could not be created of file could not be copied to
   * dest folder
   */
  public File copyFileToFolder(String storageFilePath, File folder, boolean bOverWrite)
      throws StorageException {

    return  copyFileToFolder(storageFilePath, folder, bOverWrite, null);
  }
  
  public File copyFileToFolder(String storageFilePath, File folder, boolean bOverWrite, String targetFilename)
      throws StorageException {

    if (!folder.exists() && !folder.mkdirs()) {
      throw new StorageException(format("Could not create dest folder: '%s' to copy file: '%s'.",
          folder.getAbsolutePath(), storageFilePath));
    }
    File srcFile = getFile(storageFilePath);
    File destFile = new File(folder, Utils.isEmptyString(targetFilename)?srcFile.getName():targetFilename);
    File pf = destFile.getParentFile();
    if (!pf.exists() && !pf.mkdirs()) {
      throw new StorageException(format("Could not create folder '%s'", pf.getAbsolutePath()));
    }

    copyFile(srcFile, destFile, bOverWrite);
    return destFile;
  }

  /**
   * Reads file content to byteArray. Avoid this method if possible especially for large files!
   * Handle files as streams instead
   *
   * @param storagePath - relative storage paths
   * @return file content
   * @throws StorageException error reading file
   */
  public byte[] getByteArray(String storagePath)
      throws StorageException {

    File f = getFile(storagePath);
    byte[] bin;
    try {
      bin = Files.readAllBytes(f.toPath());
    } catch (IOException ex) {
      throw new StorageException("Error occurred while creating storage file", ex);
    }
    return bin;
  }

  /**
   * Store byteArray to storage. Avoid using this method. Handle file contetn as streams!
   *
   * @param suffix - file suffix (usually mime suffix as .pdf, .xml, etc. )
   * @param prefix - file prefix (in_/out_)
   * @param buffer
   * @return Storage File
   * @throws StorageException error storage files
   */
  public File storeFile(String prefix, String suffix, byte[] buffer)
      throws StorageException {
    return storeFile(prefix, suffix, new ByteArrayInputStream(buffer));
  }

  /**
   * Store stream to storage.
   *
   * @param suffix - file suffix (usually mime suffix as .pdf, .xml, etc. )
   * @param prefix - file prefix (in_/out_)
   * @param inStream content stream
   * @return Storage File
   * @throws StorageException error storage files
   */
  public File storeFile(String prefix, String suffix, InputStream inStream)
      throws StorageException {
    File fStore = getNewStorageFile(suffix, prefix);

    try (FileOutputStream fos = new FileOutputStream(fStore)) {

      byte[] buffer = new byte[1024];
      int len = inStream.read(buffer);
      while (len != -1) {
        fos.write(buffer, 0, len);
        len = inStream.read(buffer);
      }

    } catch (IOException ex) {
      throw new StorageException(format("Error occurred while writing to file: '%s'",
          fStore.getAbsolutePath()));
    }
    return fStore;
  }

  /**
   * Copy file as input file to storage!
   *
   * @param mimeType - mimetype of input file
   * @param fIn - input file
   * @return storage file
   * @throws StorageException error occured copying file!
   */
  public File storeInFile(String mimeType, File fIn)
      throws StorageException {
    if (!fIn.exists()) {
      throw new StorageException(format("Source 'IN' file: '%s' not exists ", fIn.getAbsolutePath()));
    }
    File fStore = getNewStorageFile(getSuffixBYMimeType(mimeType), S_IN_PREFIX);
    copyFile(fIn, fStore, true);

    return fStore;
  }

  /**
   * Store input stream as input file to storage.
   *
   * @param mimeType - input mimetype
   * @param is - input stream
   * @return stored file
   * @throws StorageException error storing data from input stream to storage
   */
  public File storeInFile(String mimeType, InputStream is)
      throws StorageException {
    return storeFile(S_IN_PREFIX, getSuffixBYMimeType(mimeType), is);
  }

  /**
   * Store input stream as input file to storage.
   *
   * @param th
   * @return stored file
   * @throws StorageException error storing data from input stream to storage
   */
  public String storeThrowableAndGetRelativePath(Throwable th)
      throws StorageException {
    if (th == null) {
      return null;
    }

    File f = getNewStorageFile(MimeValue.MIME_TXT.getSuffix(), S_ERR_PREFIX);

    try (PrintWriter fw = new PrintWriter(f)) {
      Throwable cs = th;
      String ident = " ";
      do {
        fw.append(ident);
        fw.append("Caused by:");
        fw.append(cs.getMessage());
        fw.append("\n");
        ident += "  ";
        StackTraceElement[] lst = cs.getStackTrace();
        for (int i = 0; i < lst.length && i < 50; i++) {
          fw.append(ident);
          fw.append(lst[i].toString());
          fw.append("\n");
        }
        
      } while ((cs = cs.getCause()) != null);

    } catch (FileNotFoundException ex) {
      throw new StorageException(format("Error opening file:  '%s'!", f.getAbsolutePath()));
    }
    return getRelativePath(f);

  }

  public File getCreateEmptyInFile(String mimeType)
      throws StorageException {
    return getNewStorageFile(getSuffixBYMimeType(mimeType), S_IN_PREFIX);
  }

  /**
   * Store bytearray to storage.
   *
   * @param mimeType - input mimetype
   * @param buff - bytes
   * @return stored file
   * @throws StorageException error storing data from input stream to storage
   */
  public File storeInFile(String mimeType, byte[] buff)
      throws StorageException {
    return storeFile(S_IN_PREFIX, getSuffixBYMimeType(mimeType), new ByteArrayInputStream(buff));
  }

  /**
   * Store output bytearray to storage.
   *
   * @param mimeType - file mimetype
   * @param buffer - file content
   * @return storage file
   * @throws StorageException -error occured storing content to storage
   */
  public File storeOutFile(String mimeType, byte[] buffer)
      throws StorageException {
    return storeFile(S_OUT_PREFIX, getSuffixBYMimeType(mimeType), buffer);
  }

  /**
   * Store/copy output file to storage.
   *
   * @param mimeType - output file mimetype
   * @param fOut - file to copy
   * @return - storage file
   * @throws StorageException - error occured while copying file to storage
   */
  public File storeOutFile(String mimeType, File fOut)
      throws StorageException {
    if (!fOut.exists()) {
      throw new StorageException(format("File in message: '%s' not exists ", fOut.getAbsolutePath()));
    }
    File fStore = getNewStorageFile(S_OUT_PREFIX, getSuffixBYMimeType(mimeType));
    copyFile(fOut, fStore, true);
    return fStore;
  }

}
