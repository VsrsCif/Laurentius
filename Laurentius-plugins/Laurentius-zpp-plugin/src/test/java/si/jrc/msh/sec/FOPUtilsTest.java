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
package si.jrc.msh.sec;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.stream.StreamSource;
import org.apache.xmlgraphics.util.MimeConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import si.jrc.msh.plugin.zpp.ZPPConstants;
import si.jrc.msh.plugin.zpp.ZPPOutInterceptor;
import si.jrc.msh.plugin.zpp.utils.FOPUtils;
import si.laurentius.commons.MimeValues;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.exception.FOPException;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.msh.outbox.mail.MSHOutMail;

/**
 *
 * @author Jože Rihtaršič
 */
public class FOPUtilsTest {

  protected static final String LAU_HOME = "target/TEST-LAU_HOME";
  protected final SEDLogger LOG = new SEDLogger(ZPPOutInterceptor.class);
  FOPUtils mfpFop = null;

  /**
   *
   */
  public FOPUtilsTest() {
  }

  /**
   *
   * @throws java.io.IOException
   */
  @BeforeClass
  public static void startClass()
      throws IOException {

    Files.createDirectory(Paths.get(LAU_HOME));
    System.getProperties().put(SEDSystemProperties.SYS_PROP_HOME_DIR, LAU_HOME);

  }

  /**
   *
   */
  @After
  public void tearDown() {
  }

  /**
   * Test of generateVisualization method, of class FOPUtils.
   *
   * @throws javax.xml.bind.JAXBException
   * @throws java.io.FileNotFoundException
   * @throws java.io.IOException
   * @throws si.laurentius.commons.exception.FOPException
   */
  @Test
  public void testGenerateVisualizationFromMSHOutMail()
      throws JAXBException,
      FileNotFoundException, FOPException, IOException, StorageException {

    String fopConfigFile = "src/test/resources/SVEV/fop.xconf";
    String xsltFolder =
        "src/test/resources/SVEV/xslt/LegalDelivery_ZPP-DeliveryNotification.fo";

    MSHOutMail mout = new MSHOutMail();
    mout.setAction("DeliveryNotification");
    mout.setId(BigInteger.valueOf(1234));
    mout.setConversationId("ConversationId");
    mout.setMessageId("MessageId");
    mout.setReceiverEBox("receiver.box@ebox.si");
    mout.setReceiverName("receiver Box");
    mout.setSenderEBox("sender.box@ebox.si");
    mout.setSenderName("Sender Box");
    mout.setSenderMessageId("SenderMessageID");
    mout.setSentDate(Calendar.getInstance().getTime());

     

    JAXBSource source = new JAXBSource(JAXBContext.newInstance(mout.getClass()), mout);

    FOPUtils instance = new FOPUtils(new File(fopConfigFile), xsltFolder);

    try (FileOutputStream fos = new FileOutputStream("test.txt")) {
      instance.generateVisualization(source, fos, new StreamSource(xsltFolder),
          MimeConstants.MIME_PLAIN_TEXT);
    }
    try (FileOutputStream fos = new FileOutputStream("test.pdf")) {
      instance.generateVisualization(source, fos, new StreamSource(xsltFolder),
          MimeConstants.MIME_PDF);
    }

  }

  public FOPUtils getFOP() {
    if (mfpFop == null) {
      File fconf =
          new File(System.getProperty(SEDSystemProperties.SYS_PROP_HOME_DIR) + File.separator +
               ZPPConstants.SVEV_FOLDER + File.separator + ZPPConstants.FOP_CONFIG_FILENAME);

      mfpFop =
          new FOPUtils(fconf, System.getProperty(SEDSystemProperties.SYS_PROP_HOME_DIR) +
               File.separator + ZPPConstants.SVEV_FOLDER + File.separator +
               ZPPConstants.XSLT_FOLDER);
    }
    return mfpFop;
  }

}