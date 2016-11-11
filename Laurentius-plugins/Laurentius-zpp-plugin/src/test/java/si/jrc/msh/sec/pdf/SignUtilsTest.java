/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.jrc.msh.sec.pdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.List;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import si.jrc.msh.sec.TestProperties;

/**
 *
 * @author sluzba
 */
public class SignUtilsTest {

  public static final String S_TEMPLATE_IMAGE = "/laurentius_pdf_template.png";
  public static final String S_PDF_GRID_TEST = "gridtest.pdf";
  protected static final String PDF_SIGN_HOME = "target/";
  protected static final String PDF_SIGN_VISUALIZATION = "signed_visualization.pdf";
  protected static final String PDF_SIGN_VISUALIZATION_DEF = "signed_def_visualization.pdf";
  protected static final String PDF_SIGN_SERVER = "double_signed.pdf";

  @BeforeClass
  public static void setUpClass()
      throws IOException {

    try (InputStream pdfStream = SignUtilsTest.class.getResourceAsStream("/"+S_PDF_GRID_TEST);
        FileOutputStream fos = new FileOutputStream(PDF_SIGN_HOME + S_PDF_GRID_TEST)) {
      int read = 0;
      byte[] bytes = new byte[1024];
      while ((read = pdfStream.read(bytes)) != -1) {
        fos.write(bytes, 0, read);
      }

    }
    
  }

  @Before
  public void setUp() {
  }

  /**
   *
   *
   * /**
   * Test of signPDF method, of class SignUtils.
   */
  @Test
  public void test_A_SignPDF()
      throws Exception {

    PrivateKey privateKeyB = TestProperties.S_KEY_B;
    assertNotNull("Missing private key", privateKeyB);
    X509Certificate certB = TestProperties.S_CERT_B;
    assertNotNull("Missing private key Certificate", certB);

    SignUtils signPdf = new SignUtils(privateKeyB, certB);

    File signedDocumentFile;
    File documentFile =  new File(PDF_SIGN_HOME + S_PDF_GRID_TEST);
    int page;
    try (InputStream imageStream = SignUtilsTest.class.getResourceAsStream(S_TEMPLATE_IMAGE)) {

      assertNotNull("Resource '" + S_TEMPLATE_IMAGE + "' not found!", imageStream);
      signedDocumentFile = new File(PDF_SIGN_HOME + PDF_SIGN_VISUALIZATION);
      // page is 1-based here
      page = 1;
      signPdf.setVisibleSignDesigner(documentFile, 10,
          10,
          -50, imageStream, page);
      signPdf.setVisibleSignatureProperties("name", "location", "Security", 0, page, true);

      signPdf.signPDF(documentFile, signedDocumentFile, false);

    }
  }

  /**
   * Test of test_B_DoubleSignPDF method, of class SignUtils.
   */
  @Test
  public void test_B_DoubleSignPDF()
      throws Exception {

    // "server" signature
    PrivateKey privateKeyA = TestProperties.S_KEY_A;
    assertNotNull("Missing private key", privateKeyA);
    X509Certificate certA = TestProperties.S_CERT_A;
    assertNotNull("Missing private key Certificate", certA);

    File signedDocumentFile = new File(PDF_SIGN_HOME + PDF_SIGN_VISUALIZATION);
    assertTrue("Missing signed document: '" + PDF_SIGN_VISUALIZATION + "'!",
        signedDocumentFile.exists());

    // "server" signature
    SignUtils signPdfServer = new SignUtils(privateKeyA, certA);
    File dblSignedDocumentFile = new File(PDF_SIGN_HOME + PDF_SIGN_SERVER);
    signPdfServer.signPDF(signedDocumentFile, dblSignedDocumentFile, false);

    
  }

  /**
   * Test of test_B_DoubleSignPDF method, of class SignUtils.
   */
  @Test
  public void test_C_getSignerCertificate()
      throws Exception {
    File signedDocumentFile = new File(PDF_SIGN_HOME + PDF_SIGN_VISUALIZATION);
    ValidateSignatureUtils vsu = new ValidateSignatureUtils();
    List<X509Certificate> lst = vsu.getSignatureCerts(signedDocumentFile.getAbsolutePath());

    assertEquals(1, lst.size());
    assertEquals(TestProperties.S_CERT_B, lst.get(0));

  }

  @Test
  public void test_D_getTwoSignerCertificates()
      throws Exception {
    File signedDocumentFile = new File(PDF_SIGN_HOME + PDF_SIGN_SERVER);

    ValidateSignatureUtils vsu = new ValidateSignatureUtils();
    List<X509Certificate> lst = vsu.getSignatureCerts(signedDocumentFile.getAbsolutePath());

    assertEquals(2, lst.size());
    assertEquals(TestProperties.S_CERT_B, lst.get(0));
    assertEquals(TestProperties.S_CERT_A, lst.get(1));
  }

  @Test
  public void test_E_SignPDFDefVizualization()
      throws Exception {

    PrivateKey privateKeyB = TestProperties.S_KEY_B;
    assertNotNull("Missing private key", privateKeyB);
    X509Certificate certB = TestProperties.S_CERT_B;
    assertNotNull("Missing private key Certificate", certB);

    SignUtils signPdf = new SignUtils(privateKeyB, certB);

    File documentFile =  new File(PDF_SIGN_HOME + S_PDF_GRID_TEST);
    File signedDocumentFile;    
    try (InputStream imageStream = SignUtilsTest.class.getResourceAsStream(S_TEMPLATE_IMAGE)) {
      assertNotNull("Resource '" + S_TEMPLATE_IMAGE + "' not found!", imageStream);

      signedDocumentFile = new File(PDF_SIGN_HOME + PDF_SIGN_VISUALIZATION_DEF);
      signPdf.signPDF(documentFile, signedDocumentFile, true);

    }
  }

}
