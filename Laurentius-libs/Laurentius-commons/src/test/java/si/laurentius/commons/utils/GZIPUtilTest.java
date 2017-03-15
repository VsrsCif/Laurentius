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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipException;
import javax.xml.bind.DatatypeConverter;
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
   *
   * @throws java.lang.Exception
   */
  @Test
  public void testGZIPNotCompressed()
          throws Exception {
    System.out.println("JDUP5: " + Integer.parseInt("JDUP5", 32));
    GZIPUtil gzipInstalce = new GZIPUtil();

    File source = tuUtils.createFile("abcd");
    File target = tuUtils.createEmptyFile();
    File decomTarget = tuUtils.createEmptyFile();

    gzipInstalce.compressGZIP(source, target);
    gzipInstalce.decompressGZIP(target, decomTarget);

    assertTrue("Compressed file length should not be 0!", target.length() != 0);
    assertTrue("Decompressed file length should not be 0!",
            decomTarget.length() != 0);
    assertTrue("Compressed string 'abcd' should be bigger than source", source.
            length()
            < target.length());
    assertEquals("Source file and decompressed file should have equal size",
            source.length(),
            decomTarget.length());
    assertEquals(
            "Source file and decompressed file should have equal hash value",
            getHexDigest(
                    source, "MD5"), getHexDigest(decomTarget, "MD5"));

    source.delete();
    target.delete();
    decomTarget.delete();
  }

  /**
   * Test of compressGZIP method compress of class GZIPUtil.
   *
   * @throws java.lang.Exception
   */
  @Test
  public void testGZIPCompressed()
          throws Exception {

    GZIPUtil gzipInstalce = new GZIPUtil();

    File source = tuUtils.createFile("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
    File target = tuUtils.createEmptyFile();
    File decomTarget = tuUtils.createEmptyFile();

    gzipInstalce.compressGZIP(source, target);
    gzipInstalce.decompressGZIP(target, decomTarget);

    assertTrue("Compressed file length should not be 0!", target.length() != 0);
    assertTrue("Decompressed file length should not be 0!",
            decomTarget.length() != 0);
    assertTrue(
            "Compressed string 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' should not be bigger or equal than source",
            source.length() > target.length());
    assertEquals("Source file and decompressed file should have equal size",
            source.length(),
            decomTarget.length());
    assertEquals(
            "Source file and decompressed file should have equal hash value",
            getHexDigest(
                    source, "MD5"), getHexDigest(decomTarget, "MD5"));

    source.delete();
    target.delete();
    decomTarget.delete();
  }

  /**
   * Test of compressGZIP method compress of class GZIPUtil.
   *
   * @throws java.lang.Exception
   */
  @Test
  public void testDetectCorruptedData()
          throws IOException {
    String data = "<min><der>sample</der></min>";
    // Compressed HEX payload:
    String strVal
            = "1F8B0800000000000000B3C9CDCCB3B349492DB22B4ECC2DC849B5D107B16DF441C2007E6CD3781C000000";
    //DAMAGED data (HEX):1
    String dmgdaga
            = "1F8BC600000000000000B3C9CDCC3FA149492D6D7E4ECC2DC849B56607B16D1B41C2007E6CD3781C8E0000";

    File source = tuUtils.createFile(DatatypeConverter.parseHexBinary(strVal));
    File badSource = tuUtils.createFile(DatatypeConverter.
            parseHexBinary(dmgdaga));
    File decomTarget = tuUtils.createEmptyFile();
    GZIPUtil gzipInstalce = new GZIPUtil();
    gzipInstalce.decompressGZIP(source, decomTarget);
    assertEquals(data, TestUtils.readFileToString(decomTarget, "UTF-8"));
    ZipException ze = null;
    try {
      gzipInstalce.decompressGZIP(badSource, decomTarget);
      fail("decompression should fail because of currupted data!");
    } catch (ZipException e) {
      ze = e;
    }
    assertNotNull("decompression should fail because of currupted data!", ze);

  }

 

  public static String toHexString(byte[] buff) {
    // converting byte array to Hexadecimal String
    StringBuilder sb = new StringBuilder(2 * buff.length);
    for (byte b : buff) {
      sb.append(String.format("%02x", b & 0xff));
    }
    return sb.toString();
  }
  public static String getHexDigest(File f, String jcaName) throws NoSuchAlgorithmException, IOException {
    String val = null;
    try (InputStream is = new FileInputStream(f)) {
      val = getHexDigest(is, jcaName);
    }
    return val;
  }

  public static String getHexDigest(InputStream is, String jcaName) throws NoSuchAlgorithmException, IOException {

    MessageDigest md = MessageDigest.getInstance(jcaName);
    md.reset();
    byte[] buffer = new byte[1024];
    int len = is.read(buffer);
    while (len != -1) {
      md.update(buffer, 0, len); // calculate Digest
      len = is.read(buffer);
    }

    byte[] hash = md.digest();
    return toHexString(hash);
  }

}
