/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.lce;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringWriter;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;
import static org.junit.Assert.*;
import si.laurentius.lce.utils.TestUtils;

/**
 *
 * @author sluzba
 */
public class DigestUtilsTest {

  TestUtils tuUtils = new TestUtils();

  @Test
  public void testGetHexMD5Digest_File()
          throws Exception {
    File f = tuUtils.createFile("first file");
    File f2 = tuUtils.createFile("second file");

    String val1 = DigestUtils.getHexMD5Digest(f);
    String val2 = DigestUtils.getHexMD5Digest(f2);
    assertNotNull("Hashs for temp file is null", val1);
    assertNotNull("Hashs for temp file 2 is null", val2);
    assertNotEquals("equal hashes for different files", val1, val2);

    f.delete();
    f2.delete();
  }

  /**
   * Test of getMD5Hash method, of class HashUtils.
   *
   * @throws java.lang.Exception
   */
  @Test
  public void testGetHexMD5Digest_InputStream()
          throws Exception {

    String val1 = DigestUtils.getHexMD5Digest("first test file".
            getBytes("UTF-8"));
    String val2 = DigestUtils.getHexMD5Digest("secodn test file".getBytes(
            "UTF-8"));
    assertNotNull("Hashs for first input stream is null", val1);
    assertNotNull("Hashs for second input stream is null", val2);
    assertNotEquals("equal hashes for different files", val1, val2);
  }

  @Test
  public void testGetHexMD5Digest_Compare()
          throws Exception {
    String valFile = "first test file";
    File f = tuUtils.createFile(valFile.getBytes("UTF-8"));

    String val1 = DigestUtils.getHexMD5Digest(valFile.getBytes("UTF-8"));
    String val2 = DigestUtils.getHexMD5Digest(f);
    assertNotNull("Hashs for first input stream is null", val1);
    assertNotNull("Hashs for second input stream is null", val2);
    assertEquals("equal hashes for different files", val1, val2);
  }

  @Test
  public void testGetHexMD5Digest_compareBigValue()
          throws Exception {
    String value = "";
    while (value.length() < 100000) {
      value += "The quick brown fox jumps over the lazy dog.";
    }

    File f = tuUtils.createFile(value.getBytes("UTF-8"));

    String val1 = DigestUtils.getHexMD5Digest(value.getBytes("UTF-8"));
    String val2 = DigestUtils.getHexMD5Digest(f);
    assertNotNull("Hashs for first input stream is null", val1);
    assertNotNull("Hashs for second input stream is null", val2);
    assertEquals("equal hashes for different files", val1, val2);
    f.delete();
  }

  @Test
  public void testGetHexSH1Digest_File()
          throws Exception {
    File f = tuUtils.createFile("first file");
    File f2 = tuUtils.createFile("second file");

    String val1 = DigestUtils.getHexSha1Digest(f);
    String val2 = DigestUtils.getHexSha1Digest(f2);
    assertNotNull("Hashs for temp file is null", val1);
    assertNotNull("Hashs for temp file 2 is null", val2);
    assertNotEquals("equal hashes for different files", val1, val2);

    f.delete();
    f2.delete();
  }

  /**
   * Test of getMD5Hash method, of class HashUtils.
   *
   * @throws java.lang.Exception
   */
  @Test
  public void testGetHexSH1Digest_InputStream()
          throws Exception {

    String val1 = DigestUtils.getHexSha1Digest("first test file".getBytes(
            "UTF-8"));
    String val2 = DigestUtils.getHexSha1Digest("secodn test file".getBytes(
            "UTF-8"));
    assertNotNull("Hashs for first input stream is null", val1);
    assertNotNull("Hashs for second input stream is null", val2);
    assertNotEquals("equal hashes for different files", val1, val2);
  }

  @Test
  public void testGetHexSH1Digest_Compare()
          throws Exception {
    String valFile = "first test file";
    File f = tuUtils.createFile(valFile.getBytes("UTF-8"));

    String val1 = DigestUtils.getHexSha1Digest(valFile.getBytes("UTF-8"));
    String val2 = DigestUtils.getHexSha1Digest(f);
    assertNotNull("Hashs for first input stream is null", val1);
    assertNotNull("Hashs for second input stream is null", val2);
    assertEquals("equal hashes for different files", val1, val2);
  }

  @Test
  public void testGetHexSH1Digest_compareBigValue()
          throws Exception {
    String value = "";
    while (value.length() < 100000) {
      value += "The quick brown fox jumps over the lazy dog.";
    }

    File f = tuUtils.createFile(value.getBytes("UTF-8"));

    String val1 = DigestUtils.getHexSha1Digest(value.getBytes("UTF-8"));
    String val2 = DigestUtils.getHexSha1Digest(f);
    assertNotNull("Hashs for first input stream is null", val1);
    assertNotNull("Hashs for second input stream is null", val2);
    assertEquals("equal hashes for different files", val1, val2);
  }

  @Test
  public void testToHexString() {
    String testByte = "The quick brown fox jumps over the lazy dog.";
    String val1 = DigestUtils.toHexString(testByte.getBytes());
    String val2 = Hex.toHexString(testByte.getBytes());
    assertNotNull("Hashs for first input stream is null", val1);
    assertNotNull("Hashs for second input stream is null", val2);
    assertEquals("equal hashes for different files", val1, val2);

  }

}
