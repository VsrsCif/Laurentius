/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.laurentius.process.xslt;

import java.io.File;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Test;
import static org.junit.Assert.*;
import org.w3c.dom.Document;

/**
 *
 * @author sluzba
 */
public class XSLTUtilsTest {
  
  public XSLTUtilsTest() {
  }

 

  @Test
  public void testTransform_3args_2() throws Exception {
    
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();

    Document source =  builder.parse(XSLTUtilsTest.class.getResourceAsStream("/xml/IVPN_001.xml"));
    InputStream xslt = XSLTUtilsTest.class.getResourceAsStream("/xslt/zbs_001.xslt");
    File fileResult = new File("target/test.xml");
    XSLTUtils.transform(source, xslt, fileResult);

  }
  
}
