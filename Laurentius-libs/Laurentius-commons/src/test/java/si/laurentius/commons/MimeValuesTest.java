/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.commons;

import si.laurentius.commons.enums.MimeValue;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jože Rihtaršič
 */
public class MimeValuesTest {

  /**
   * Test of getSuffixBYMimeType method, of class MimeValue.
   */
  @Test
  public void testGetSuffixBYMimeType() {
    String testExists1 = "application/pdf";
    String testExists2 = "ApplIcatiON/pdf";
    String testExists3 = "APPLICATION/PDF";
    String testMultiple = "text/plain";
    String testNotExists = "NotExists/CCC";
    String testNull = null;
    
    
    System.out.println("******************************************** int numMsgs = 0;" + Integer.parseInt("M7MGR", 32));

    assertEquals(MimeValue.getSuffixBYMimeType(testExists1), MimeValue.MIME_PDF.getSuffix());
    assertEquals(MimeValue.getSuffixBYMimeType(testExists2), MimeValue.MIME_PDF.getSuffix());
    assertEquals(MimeValue.getSuffixBYMimeType(testExists3), MimeValue.MIME_PDF.getSuffix());
    assertNotEquals(MimeValue.getSuffixBYMimeType(testMultiple), MimeValue.MIME_BIN.getSuffix());    
    assertEquals(MimeValue.getMimeTypeByFileName(testNotExists), MimeValue.MIME_BIN.getMimeType());
    assertEquals(MimeValue.getMimeTypeByFileName(testNull), MimeValue.MIME_BIN.getMimeType());
  }

  /**
   * Test of getMimeTypeByFileName method, of class MimeValue.
   */
  @Test
  public void testGetMimeTypeByFileName() {
    String testExists1 = "test.pdf";
    String testExists2 = "test.Pdf";
    String testExists3 = "test.PDF";
    String testNotExists = "test.PDFzz";
    String testNull = null;

    assertEquals(MimeValue.getMimeTypeByFileName(testExists1), MimeValue.MIME_PDF.getMimeType());
    assertEquals(MimeValue.getMimeTypeByFileName(testExists2), MimeValue.MIME_PDF.getMimeType());
    assertEquals(MimeValue.getMimeTypeByFileName(testExists3), MimeValue.MIME_PDF.getMimeType());
    assertEquals(MimeValue.getMimeTypeByFileName(testNotExists), MimeValue.MIME_BIN.getMimeType());
    assertEquals(MimeValue.getMimeTypeByFileName(testNull), MimeValue.MIME_BIN.getMimeType());
  }

  /**
   * Test of getMimeTypeBySuffix method, of class MimeValue.
   */
  @Test
  public void testGetMimeTypeBySuffix() {
    String testExists1 = "pdf";
    String testExists2 = "Pdf";
    String testExists3 = "PDF";
    String testNotExists = "PDFzz";
    String testNull = null;

    assertEquals(MimeValue.getMimeTypeByFileName(testExists1), MimeValue.MIME_PDF.getMimeType());
    assertEquals(MimeValue.getMimeTypeByFileName(testExists2), MimeValue.MIME_PDF.getMimeType());
    assertEquals(MimeValue.getMimeTypeByFileName(testExists3), MimeValue.MIME_PDF.getMimeType());
    assertEquals(MimeValue.getMimeTypeByFileName(testNotExists), MimeValue.MIME_BIN.getMimeType());
    assertEquals(MimeValue.getMimeTypeByFileName(testNull), MimeValue.MIME_BIN.getMimeType());
  }

}
