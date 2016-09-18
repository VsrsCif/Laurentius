/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.commons;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jože Rihtaršič
 */
public class MimeValuesTest {

  /**
   * Test of getSuffixBYMimeType method, of class MimeValues.
   */
  @Test
  public void testGetSuffixBYMimeType() {
    String testExists1 = "application/pdf";
    String testExists2 = "ApplIcatiON/pdf";
    String testExists3 = "APPLICATION/PDF";
    String testMultiple = "text/plain";
    String testNotExists = "NotExists/CCC";
    String testNull = null;

    assertEquals(MimeValues.getSuffixBYMimeType(testExists1), MimeValues.MIME_PDF.getSuffix());
    assertEquals(MimeValues.getSuffixBYMimeType(testExists2), MimeValues.MIME_PDF.getSuffix());
    assertEquals(MimeValues.getSuffixBYMimeType(testExists3), MimeValues.MIME_PDF.getSuffix());
    assertNotEquals(MimeValues.getSuffixBYMimeType(testMultiple), MimeValues.MIME_BIN.getSuffix());    
    assertEquals(MimeValues.getMimeTypeByFileName(testNotExists), MimeValues.MIME_BIN.getMimeType());
    assertEquals(MimeValues.getMimeTypeByFileName(testNull), MimeValues.MIME_BIN.getMimeType());
  }

  /**
   * Test of getMimeTypeByFileName method, of class MimeValues.
   */
  @Test
  public void testGetMimeTypeByFileName() {
    String testExists1 = "test.pdf";
    String testExists2 = "test.Pdf";
    String testExists3 = "test.PDF";
    String testNotExists = "test.PDFzz";
    String testNull = null;

    assertEquals(MimeValues.getMimeTypeByFileName(testExists1), MimeValues.MIME_PDF.getMimeType());
    assertEquals(MimeValues.getMimeTypeByFileName(testExists2), MimeValues.MIME_PDF.getMimeType());
    assertEquals(MimeValues.getMimeTypeByFileName(testExists3), MimeValues.MIME_PDF.getMimeType());
    assertEquals(MimeValues.getMimeTypeByFileName(testNotExists), MimeValues.MIME_BIN.getMimeType());
    assertEquals(MimeValues.getMimeTypeByFileName(testNull), MimeValues.MIME_BIN.getMimeType());
  }

  /**
   * Test of getMimeTypeBySuffix method, of class MimeValues.
   */
  @Test
  public void testGetMimeTypeBySuffix() {
    String testExists1 = "pdf";
    String testExists2 = "Pdf";
    String testExists3 = "PDF";
    String testNotExists = "PDFzz";
    String testNull = null;

    assertEquals(MimeValues.getMimeTypeByFileName(testExists1), MimeValues.MIME_PDF.getMimeType());
    assertEquals(MimeValues.getMimeTypeByFileName(testExists2), MimeValues.MIME_PDF.getMimeType());
    assertEquals(MimeValues.getMimeTypeByFileName(testExists3), MimeValues.MIME_PDF.getMimeType());
    assertEquals(MimeValues.getMimeTypeByFileName(testNotExists), MimeValues.MIME_BIN.getMimeType());
    assertEquals(MimeValues.getMimeTypeByFileName(testNull), MimeValues.MIME_BIN.getMimeType());
  }

}
