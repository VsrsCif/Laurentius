/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.process.xslt;

import java.io.File;
import java.io.InputStream;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import si.laurentius.commons.utils.xml.XMLUtils;

/**
 *
 * @author sluzba
 */
public class XSLTUtilsTest {

  public static final String S_XML_TEST_FILE_V01 = "/xml-sample/court-eizvrsba.xml";

  public static final String S_XSLT_FILE_ZBS_V01 = "/xslt/Sodisce2ZbsIzvrsbeXml_v1.00.xslt";
  
  

  public static final String S_TARGET_FOLDER = "target";

  public static File fTarget = new File(S_TARGET_FOLDER);

  public XSLTUtilsTest() {
    if (!fTarget.exists()) {
      fTarget.mkdir();
    }
  }

  @BeforeClass
  public static void setUpClass() {
    
  }

  @Before
  public void setUp() {
  }

  /**
   * Test of transform method, of class XSLTUtils.
   */


  @Test
  public void testZBSTransform()
          throws Exception {
    System.out.println("transform");

    Document source = XMLUtils.deserializeToDom(XSLTUtilsTest.class.
            getResourceAsStream(S_XML_TEST_FILE_V01));

    InputStream xsltSource = XSLTUtilsTest.class.getResourceAsStream(
            S_XSLT_FILE_ZBS_V01);
    File fileResult = File.createTempFile("xslt", ".xml", new File(
            S_TARGET_FOLDER));
    XSLTUtils.transform(source, xsltSource, fileResult);

  }
  
  

}
