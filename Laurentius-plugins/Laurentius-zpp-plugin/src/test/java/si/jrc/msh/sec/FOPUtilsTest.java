/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.jrc.msh.sec;

import java.io.FileNotFoundException;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import si.laurentius.commons.exception.FOPException;

/**
 *
 * @author sluzba
 */
public class FOPUtilsTest {

  /**
     *
     */
  public FOPUtilsTest() {}

  /**
     *
     */
  @Before
  public void setUp() {}

  /**
     *
     */
  @After
  public void tearDown() {}

  /**
   * Test of generateVisualization method, of class FOPUtils.
   *
   * @throws javax.xml.bind.JAXBException
   * @throws java.io.FileNotFoundException
   * @throws java.io.IOException
   * @throws si.laurentius.commons.exception.FOPException
   */
  @Test
  public void testGenerateVisualizationFromMSHOutMail() throws JAXBException,
      FileNotFoundException, FOPException, IOException {
    /*
     * String fopConfigFile = "src/test/resources/fop/fop.xconf"; String xsltFolder =
     * "src/test/resources/fop/xslt/LegalDelivery_ZPP-DeliveryNotification.fo";
     * 
     * MSHOutMail mout = new MSHOutMail(); mout.setAction("DeliveryNotification");
     * mout.setId(BigInteger.valueOf(1234)); mout.setConversationId("ConversationId");
     * mout.setMessageId("MessageId"); mout.setReceiverEBox("receiver.box@ebox.si");
     * mout.setReceiverName("receiver Box"); mout.setSenderEBox("sender.box@ebox.si");
     * mout.setSenderName("Sender Box"); mout.setSenderMessageId("SenderMessageID");
     * mout.setSentDate(Calendar.getInstance().getTime());
     * 
     * JAXBSource source = new JAXBSource(JAXBContext.newInstance(mout.getClass()), mout );
     * 
     * FOPUtils instance = new FOPUtils(new File(fopConfigFile),xsltFolder );
     * 
     * try (FileOutputStream fos = new FileOutputStream("test.txt")){
     * instance.generateVisualization(source, fos, new StreamSource(xsltFolder),
     * MimeConstants.MIME_PLAIN_TEXT); } try (FileOutputStream fos = new
     * FileOutputStream("test.pdf")){ instance.generateVisualization(source, fos, new
     * StreamSource(xsltFolder), MimeConstants.MIME_PDF); }
     */

  }

}
