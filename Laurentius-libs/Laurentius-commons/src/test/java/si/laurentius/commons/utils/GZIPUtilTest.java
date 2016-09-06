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

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public class GZIPUtilTest {
  
  TestUtils tuUtils = new TestUtils();
  

 
  /**
   * Test of compressGZIP method compress of class GZIPUtil.
   * @throws java.lang.Exception
   */
  @Test
  public void testGZIPNotCompressed()
      throws Exception {
    System.out.println("JDUP5: " +Integer.parseInt("JDUP5", 32) );
    GZIPUtil gzipInstalce = new GZIPUtil();
    HashUtils mhu = new HashUtils();
    File source =  tuUtils.createFile("abcd");
    File target = tuUtils.createEmptyFile();
    File decomTarget = tuUtils.createEmptyFile();
    
    gzipInstalce.compressGZIP(source, target);    
    gzipInstalce.decompressGZIP(target, decomTarget);
    
    assertTrue("Compressed file length should not be 0!", target.length() !=0);
    assertTrue("Decompressed file length should not be 0!", decomTarget.length() !=0);
    assertTrue("Compressed string 'abcd' should be bigger than source", source.length() < target.length());
    assertEquals("Source file and decompressed file should have equal size", source.length(),   decomTarget.length());
    assertEquals("Source file and decompressed file should have equal hash value", mhu.getMD5Hash(source), mhu.getMD5Hash(decomTarget));
    
    source.delete();
    target.delete();
    decomTarget.delete();
  }
   /**
   * Test of compressGZIP method compress of class GZIPUtil.
   * @throws java.lang.Exception
   */
  @Test
  public void testGZIPCompressed()
      throws Exception {
    
    GZIPUtil gzipInstalce = new GZIPUtil();
    HashUtils mhu = new HashUtils();
    File source =  tuUtils.createFile("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
    File target = tuUtils.createEmptyFile();
    File decomTarget = tuUtils.createEmptyFile();
    
    gzipInstalce.compressGZIP(source, target);    
    gzipInstalce.decompressGZIP(target, decomTarget);
    
    assertTrue("Compressed file length should not be 0!", target.length() !=0);
    assertTrue("Decompressed file length should not be 0!", decomTarget.length() !=0);
    assertTrue("Compressed string 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' should not be bigger or equal than source", source.length() > target.length());
    assertEquals("Source file and decompressed file should have equal size", source.length(),   decomTarget.length());
    assertEquals("Source file and decompressed file should have equal hash value", mhu.getMD5Hash(source), mhu.getMD5Hash(decomTarget));
    
    source.delete();
    target.delete();
    decomTarget.delete();
  }

  
  
}
