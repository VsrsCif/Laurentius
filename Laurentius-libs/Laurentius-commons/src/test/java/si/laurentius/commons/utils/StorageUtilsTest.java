/*
 * Copyright 2016, Supreme Court Republic of Slovenia
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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import static si.laurentius.commons.SEDSystemProperties.SYS_PROP_FOLDER_STORAGE_DEF;
import static si.laurentius.commons.SEDSystemProperties.SYS_PROP_HOME_DIR;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public class StorageUtilsTest {

  TestUtils tuUtils = new TestUtils();

  @Before
  public void setUp()
      throws IOException {
    // reset property
    System.setProperty(SYS_PROP_HOME_DIR, System.getProperty("java.io.tmpdir"));

    Path directory = StorageUtils.getStorageFolder().toPath();
    if (Files.exists(directory)) {
      Path p = Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
            throws IOException {
          Files.deleteIfExists(file);
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc)
            throws IOException {
          Files.delete(dir);
          return FileVisitResult.CONTINUE;
        }

      });
      if (p== null){
        fail();
      }
    }
  }

  /**
   * Test of dateStorageFolder method, of class StorageUtils.
   *
   * @throws java.lang.Exception
   */
  @Test
  public void testDateStorageFolder()
      throws Exception {
    LocalDate cld = LocalDate.of(2010, 5, 6);
    Path p = StorageUtils.dateStorageFolder(cld);
    Path root = StorageUtils.getStorageFolder().toPath();
    assertTrue("date storega starts with StorageFolder", p.startsWith(root));
    assertEquals(p.getName(p.getNameCount() - 1).toString(), "06");
    assertEquals(p.getName(p.getNameCount() - 2).toString(), "05");
    assertEquals(p.getName(p.getNameCount() - 3).toString(), "2010");

    cld = LocalDate.of(2016, 11, 28);
    p = StorageUtils.dateStorageFolder(cld);
    assertEquals(p.getName(p.getNameCount() - 1).toString(), "28");
    assertEquals(p.getName(p.getNameCount() - 2).toString(), "11");
    assertEquals(p.getName(p.getNameCount() - 3).toString(), "2016");
  }

  /**
   * Test of testGetMaxSubFolderNumber method, of class StorageUtils.
   *
   * @throws java.lang.Exception
   */
  @Test
  public void testGetMaxSubFolderNumber()
      throws Exception {
    LocalDate cld = LocalDate.now();
    Path p = StorageUtils.dateStorageFolder(cld);

    int iVal = StorageUtils.getMaxSubFolderNumber(p);
    assertEquals("If null folders expected only 1", 1, iVal);

    File f = p.toAbsolutePath().toFile();
    if (!f.exists() && !f.mkdirs()) {
      fail("Fail creating folder: " + f.getAbsolutePath());
    }
    File fsub = new File(f, "001");
    if (!fsub.exists() && !fsub.mkdirs()) {
      fail("Fail creating folder: " + fsub.getAbsolutePath());
    }
    iVal = StorageUtils.getMaxSubFolderNumber(p);
    assertEquals("Expected only 1", 1, iVal);

    fsub = new File(f, "002");
    if (!fsub.exists() && !fsub.mkdirs()) {
      fail("Fail creating folder: " + fsub.getAbsolutePath());
    }
    iVal = StorageUtils.getMaxSubFolderNumber(p);
    assertEquals(2, iVal);

    fsub = new File(f, "005");
    if (!fsub.exists() && !fsub.mkdirs()) {
      fail("Fail creating folder: " + fsub.getAbsolutePath());
    }
    iVal = StorageUtils.getMaxSubFolderNumber(p);
    assertEquals(5, iVal);

    fsub = new File(f, "12345");
    if (!fsub.exists() && !fsub.mkdirs()) {
      fail("Fail creating folder: " + fsub.getAbsolutePath());
    }
    iVal = StorageUtils.getMaxSubFolderNumber(p);
    assertEquals(12345, iVal);

  }

  /**
   * Test of currentStorageFolder method, of class StorageUtils.
   *
   * @throws java.lang.Exception
   */
  @Test
  public void testCurrentStorageFolder()
      throws Exception {
    LocalDate cld = LocalDate.now();
    Path p = StorageUtils.dateStorageFolder(cld);
    File expResult = new File(p.toFile(), "001");

    File result = StorageUtils.currentStorageFolder();
    assertEquals(expResult, result);

    StorageUtils siInstance = new StorageUtils();
    for (int i = 0; i < StorageUtils.MAX_FILES_IN_FOLDER+10; i++) {
      siInstance.storeOutFile("application/bin", tuUtils.getTestByteArray());
    }

    // if more than MAX_FILES_IN_FOLDER must be created new folder
    expResult = new File(p.toFile(), "002");
    result = StorageUtils.currentStorageFolder();
    assertEquals(expResult, result);

  }

  /**
   * Test of getFile method, of class StorageUtils.
   *
   * @throws java.io.IOException
   */
  @Test
  public void testGetFile()
      throws IOException {

    LocalDate cld = LocalDate.now();
    Path p = StorageUtils.dateStorageFolder(cld);
    File fsub = new File(p.toAbsolutePath().toFile(), "001");
    if (!fsub.exists() && !fsub.mkdirs()) {
      fail("Fail creating folder: " + fsub.getAbsolutePath());
    }
    tuUtils.createFile(fsub, "test");
    String strRoot = StorageUtils.getSEDHomeFolder().getAbsolutePath();
    assertTrue(fsub.getAbsolutePath().startsWith(strRoot));
    String storagePath = fsub.getAbsolutePath().substring(strRoot.length());

    File result = StorageUtils.getFile(storagePath);
    assertEquals(fsub, result);
  }

  /**
   * Test of getNewStorageFile method, of class StorageUtils.
   *
   * @throws java.lang.Exception
   */
  @Test
  public void testGetNewStorageFile()
      throws Exception {
    String suffix = "test";
    String prefix = ".pdf";
    File result = StorageUtils.getNewStorageFile(suffix, prefix);
    assertNotNull(result);

    assertTrue(
        result.getAbsolutePath().startsWith(StorageUtils.getStorageFolder().getAbsolutePath()));

  }

  /**
   * Test of getSEDHomeFolder method, of class StorageUtils.
   */
  @Test
  public void testGetSEDHomeFolder() {
    File expResultTmp = new File(System.getProperty("java.io.tmpdir"));
    File expResult = new File(System.getProperty("user.dir"));
    File result = StorageUtils.getSEDHomeFolder();
    assertEquals(expResultTmp, result);

    System.getProperties().remove(SYS_PROP_HOME_DIR);
    result = StorageUtils.getSEDHomeFolder();
    assertEquals("If propery ${laurentius.home} is not setted home is working dir: ${user.dir}", expResult,
        result);
  }

  /**
   * Test of getStorageFolder method, of class StorageUtils.
   */
  @Test
  public void testGetStorageFolder() {
    File expResultTmp = new File(System.getProperty("java.io.tmpdir") + File.separator +
        SYS_PROP_FOLDER_STORAGE_DEF);
    File expResult = new File(System.getProperty("user.dir") + File.separator +
        SYS_PROP_FOLDER_STORAGE_DEF);
    File result = StorageUtils.getStorageFolder();
    assertEquals(expResultTmp, result);

    System.getProperties().remove(SYS_PROP_HOME_DIR);
    result = StorageUtils.getStorageFolder();
    assertEquals("If propery ${laurentius.home} is not setted home is working dir: ${user.dir}", expResult,
        result);
  }

  /**
   * Test of removeFile method, of class StorageUtils.
   *
   * @throws java.lang.Exception
   */
  @Test
  public void testRemoveFile()
      throws Exception {

    StorageUtils instance = new StorageUtils();
    File f = instance.storeOutFile("application/pdf", tuUtils.getTestByteArray());
    assertTrue(f.exists());
    StorageUtils.removeFile(StorageUtils.getRelativePath(f));
    assertTrue(!f.exists());
  }

  /**
   * Test of copyFile method, of class StorageUtils.
   *
   * @throws java.lang.Exception
   */
  @Test
  public void testCopyFile()
      throws Exception {
    File sourceFile = tuUtils.createFile("test data");
    File destFile = tuUtils.createEmptyFile();

    assertTrue(sourceFile.length() > 0);
    assertTrue(destFile.length() == 0);

    StorageUtils instance = new StorageUtils();
    instance.copyFile(sourceFile, destFile, true);
    assertTrue(sourceFile.length() == destFile.length());

    if (!sourceFile.delete() || !destFile.delete()){
      fail();
    }

  }

  /**
   * Test of copyFileToFolder method, of class StorageUtils.
   *
   * @throws java.lang.Exception
   */
  @Test
  public void testCopyFileToFolder()
      throws Exception {

    File destFolder = StorageUtils.getSEDHomeFolder();
    StorageUtils instance = new StorageUtils();
    File f = instance.storeOutFile("application/bin",tuUtils.getTestByteArray());
    String relPath = StorageUtils.getRelativePath(f);

    
    
    File fNew = instance.copyFileToFolder(relPath, destFolder);
    assertNotNull(fNew);

    assertEquals(f.length(),  fNew.length());
    assertEquals(destFolder.getAbsolutePath(), fNew.getParentFile().getAbsolutePath());

    if (!fNew.delete()){
      fail();
    }
   

  }

  /**
   * Test of getByteArray method, of class StorageUtils.
   *
   * @throws java.lang.Exception
   */
  @Test
  public void testGetByteArray()
      throws Exception {

    byte[] expResult = tuUtils.getTestByteArray();

    StorageUtils instance = new StorageUtils();
    File f = instance.storeOutFile("application/bin", expResult);
    assertTrue(f.exists());

    String storagePath = StorageUtils.getRelativePath(f);
    byte[] result = instance.getByteArray(storagePath);

    assertArrayEquals(expResult, result);

  }

  /**
   * Test of storeFile method, of class StorageUtils.
   *
   * @throws java.lang.Exception
   */
  @Test
  public void testStoreFileIS()
      throws Exception {

    ByteArrayInputStream bis = new ByteArrayInputStream(tuUtils.getTestByteArray());
    long size = bis.available();

    StorageUtils instance = new StorageUtils();
    File result = instance.storeFile("test", ".bin", bis);
    assertEquals(size, (int) result.length());

    assertTrue(
        result.getAbsolutePath().startsWith(StorageUtils.getStorageFolder().getAbsolutePath()));

  }

  /**
   * Test of storeFile method, of class StorageUtils.
   */
  @Test
  public void testStoreFileBuff()
      throws Exception {
    byte[] buff = tuUtils.getTestByteArray();

    StorageUtils instance = new StorageUtils();
    File result = instance.storeFile("test", ".bin", buff);
    assertEquals(buff.length, (int) result.length());

    assertTrue(
        result.getAbsolutePath().startsWith(StorageUtils.getStorageFolder().getAbsolutePath()));

  }

  /**
   * Test of storeInFile method, of class StorageUtils.
   *
   * @throws java.lang.Exception
   */
  @Test
  public void testStoreInFile_File()
      throws Exception {
    System.out.println("storeInFile");
    String mimeType = "application/bin";
    File fIn = tuUtils.createFile("test data");
    StorageUtils instance = new StorageUtils();
    File result = instance.storeInFile(mimeType, fIn);
    assertEquals(fIn.length(), result.length());
    assertTrue(
        result.getAbsolutePath().startsWith(StorageUtils.getStorageFolder().getAbsolutePath()));
  }

  /**
   * @throws java.lang.Exception Test of storeInFile method, of class StorageUtils.
   */
  @Test
  public void testStoreInFile_String_InputStream()
      throws Exception {

    String mimeType = "application/bin";
    InputStream is = new ByteArrayInputStream(tuUtils.getTestByteArray());
    long isL = is.available();
    StorageUtils instance = new StorageUtils();
    File result = instance.storeInFile(mimeType, is);

    assertEquals(isL, result.length());
    assertTrue(
        result.getAbsolutePath().startsWith(StorageUtils.getStorageFolder().getAbsolutePath()));

  }

  /**
   * Test of storeOutFile method, of class StorageUtils.
   *
   * @throws java.lang.Exception
   */
  @Test
  public void testStoreOutFile_String_byteArr()
      throws Exception {
    String mimeType = "application/bin";
    byte[] buffer = tuUtils.getTestByteArray();
    StorageUtils instance = new StorageUtils();
    File result = instance.storeOutFile(mimeType, buffer);
    assertEquals(buffer.length, result.length());
    assertTrue(
        result.getAbsolutePath().startsWith(StorageUtils.getStorageFolder().getAbsolutePath()));
  }

  /**
   * Test of storeOutFile method, of class StorageUtils.
   *
   * @throws java.lang.Exception
   */
  @Test
  public void testStoreOutFile_String_File()
      throws Exception {
    String mimeType = "application/bin";
    File fOut = tuUtils.createFile("test data");
    StorageUtils instance = new StorageUtils();

    File result = instance.storeOutFile(mimeType, fOut);
    assertEquals(fOut.length(), result.length());
    assertTrue(
        result.getAbsolutePath().startsWith(StorageUtils.getStorageFolder().getAbsolutePath()));
  }

}
