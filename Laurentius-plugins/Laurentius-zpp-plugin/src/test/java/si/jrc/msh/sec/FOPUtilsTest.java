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
import java.util.UUID;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.stream.StreamSource;
import org.apache.xmlgraphics.util.MimeConstants;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import si.jrc.msh.plugin.zpp.ZPPOutInterceptor;
import si.jrc.msh.plugin.zpp.utils.FOPUtils;
import si.laurentius.commons.MimeValues;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.exception.FOPException;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.inbox.payload.MSHInPart;
import si.laurentius.msh.inbox.payload.MSHInPayload;
import si.laurentius.msh.inbox.property.MSHInProperties;
import si.laurentius.msh.inbox.property.MSHInProperty;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.laurentius.msh.outbox.payload.MSHOutPayload;
import si.laurentius.msh.outbox.property.MSHOutProperties;
import si.laurentius.msh.outbox.property.MSHOutProperty;

/**
 *
 * @author Jože Rihtaršič
 */
public class FOPUtilsTest {

  protected static final String FOP_CONFIG_FILE = "src/test/resources/SVEV/fop.xconf";
  protected static final String XSLT_FO_FOLDER = "src/test/resources/SVEV/xslt/";
  protected static final String LAU_HOME = "target/TEST-LAU_HOME/";
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
    if (! (new File(LAU_HOME)).exists()) {
      Files.createDirectory(Paths.get(LAU_HOME));
      System.getProperties().put(SEDSystemProperties.SYS_PROP_HOME_DIR, LAU_HOME);
    }

  }

  /**
   *
   */
  @After
  public void tearDown() {
  }

  @Test
  public void testGenerateDeliveryNotificationVisualization()
      throws JAXBException, IOException, FOPException {

    /*
OBVESTILO O PRISPELI POŠILJKI
Pošiljatelj
<podatki o sodišču>
Naslovnik
<podatki o naslovniku>
Zadeva : Obvestilo o prispeli pošiljki in pravni pouk o posledicah neprevzema
Obveščamo vas, da je v vaš varen elektronski predal dne <datum posredovanja obvestila> prispela
pošiljka z oznako <oznaka e-pošiljke>.
Pošiljko lahko prevzamete v roku 15 dni v vašem varnem elektronskem predalu na naslovu
<naslov s povezavo za dostop>. Rok za prevzem začne teči od dne <datum posredovanja
obvestila>. Če v tem roku pošiljke ne boste prevzeli, se bo po sedmem odstavku 141.a člena ZPP s
potekom tega roka vročitev štela za opravljeno.
Naša oznaka
<Oznaka SVEV sporočila>
<Kraj nastanka obvestila>, <Datum nastanka obvestila>
     */
    MSHOutMail om = createOutMail();

    FOPUtils instance = new FOPUtils(new File(FOP_CONFIG_FILE), XSLT_FO_FOLDER);
    File ftxt = new File(LAU_HOME + "DeliveryNotification.txt");
    File fpdf = new File(LAU_HOME + "DeliveryNotification.pdf");
    instance.generateVisualization(om, ftxt, FOPUtils.FopTransformations.DeliveryNotification,
        MimeConstants.MIME_PLAIN_TEXT);
    String strRes = null;

//    OBVESTILO O PRISPELI POŠILJKI
    instance.generateVisualization(om, fpdf, FOPUtils.FopTransformations.DeliveryNotification,
        MimeConstants.MIME_PDF);

  }

  /**
   *
   * VROČILNICA Pošiljatelj
   * < podatki o sodišču>
   * Naslovnik
   * < podatki o naslovniku>
   * Zadeva : Potrjena vročilnica po ZPP Naslovnik potrjujem, da sem dne
   * <datum elektronskega podpisa vročilnice> sprejel pošiljko z oznako <oznaka e-pošiljke>. To
   * sporočilo je potrdilo o vročitvi pošiljke in opravljeni storitvi. Naša oznaka
   * <Oznaka SVEV sporočila>
   * Storitev : Elektronska vročitev pošiljke po ZPP Datum opravljene storitve :
   * <Datum opravljene storitve>
   * <Kraj nastanka obvestila>, <Datum nastanka obvestila>
   *
   *
   * @throws JAXBException
   * @throws IOException
   * @throws FOPException
   */
  @Test
  public void testGenerateAdviceOfDeliveryVisualization()
      throws JAXBException, IOException, FOPException {

    MSHInMail im = createInMail();

    FOPUtils instance = new FOPUtils(new File(FOP_CONFIG_FILE), XSLT_FO_FOLDER);
    File ftxt = new File(LAU_HOME + "AdviceOfDelivery.txt");
    File fpdf = new File(LAU_HOME + "AdviceOfDelivery.pdf");
    instance.generateVisualization(im, ftxt, FOPUtils.FopTransformations.AdviceOfDelivery,
        MimeConstants.MIME_PLAIN_TEXT);

    instance.generateVisualization(im, fpdf, FOPUtils.FopTransformations.AdviceOfDelivery,
        MimeConstants.MIME_PDF);

  }
  /**
   * 
   * 
   * POROČILO O POTRDITVI SPREJEMA
Pošiljatelj
< podatki o sodišču>
Zadeva : Potrditev sprejema dokumenta v postopek elektronskega vročanja
Potrjujemo sprejem dokumenta z oznako
<oznaka e-pošiljke>
Naša oznaka
<Oznaka SVEV sporočila>
Za naslovnika
< podatki o naslovniku>
Potrjujemo, da smo v postopek elektronskega vročanja po Zakonu o pravdnem postopku v sistem
<ponudnik e-predala> sprejeli navedeno pošiljko, ki jo bomo dostavili v naslovnikov varni
elektronski predal. Po opravljeni vročitvi vam bomo posredovali potrdilo o opravljeni elektronski
vročitvi.
Storitev : Sporočilo o sprejemu pošiljke v postopek elektronske vročitve po ZPP
Datum opravljene storitve : <Datum opravljene storitve>
<Kraj nastanka obvestila>, <Datum nastanka obvestila>
* 
   * @throws JAXBException
   * @throws IOException
   * @throws FOPException 
   */

  @Test
  public void testGenerateDeliveryRecieptVisualization()
      throws JAXBException, IOException, FOPException {

    MSHInMail im = createInMail();

    FOPUtils instance = new FOPUtils(new File(FOP_CONFIG_FILE), XSLT_FO_FOLDER);
    File ftxt = new File(LAU_HOME + "DeliveryReciept.txt");
    File fpdf = new File(LAU_HOME + "DeliveryReciept.pdf");
    instance.generateVisualization(im, ftxt, FOPUtils.FopTransformations.DeliveryReciept,
        MimeConstants.MIME_PLAIN_TEXT);

    instance.generateVisualization(im, fpdf, FOPUtils.FopTransformations.DeliveryReciept,
        MimeConstants.MIME_PDF);

  }
  
  @Test
  public void testGenerateFictionNotification() throws FOPException {
    /**
     *
     * OBVESTILO O VROČENI POŠILJKI Pošiljatelj
     * < podatki o sodišču>
     * Naslovnik
     * < podatki o naslovniku>
     * Zadeva : Obvestilo o vročeni pošiljki kot posledica neprevzema pošiljke Ker pošiljke z oznako
     * <oznaka e-pošiljke> niste prevzeli v roku 15 dni, se je po sedmem odstavka 141.a člena ZPP s
     * potekom tega roka vročitev štela za opravljeno dne <datum fikcije>. Pošiljka je bila tega dne
     * puščena v vašem varnem elektronskem predalu, lahko pa jo prevzamete tudi
     * pri:<podatki o sodišču>. Naša oznaka
     * <Oznaka SVEV sporočila>
     * <Kraj nastanka obvestila>, <Datum nastanka obvestila>
     */
     MSHOutMail om = createOutMail();

    FOPUtils instance = new FOPUtils(new File(FOP_CONFIG_FILE), XSLT_FO_FOLDER);
    File ftxt = new File(LAU_HOME + "AdviceOfDeliveryFictionNotification.txt");
    File fpdf = new File(LAU_HOME + "AdviceOfDeliveryFictionNotification.pdf");
    instance.generateVisualization(om, ftxt, FOPUtils.FopTransformations.AdviceOfDeliveryFictionNotification,
        MimeConstants.MIME_PLAIN_TEXT);
    String strRes = null;

//    OBVESTILO O PRISPELI POŠILJKI
    instance.generateVisualization(om, fpdf, FOPUtils.FopTransformations.AdviceOfDeliveryFictionNotification,
        MimeConstants.MIME_PDF);
  }

  @Test
  public void testGenerateFictionAdviceOfDelivery()
      throws JAXBException, IOException, FOPException {

    /**
     *
     *
     * VROČILNICA NA PODLAGI FIKCIJE Pošiljatelj
     * < podatki o sodišču>
     * Naslovnik
     * < podatki o naslovniku>
     * Zadeva : Potrdilo o opravljeni vročitvi na podlagi fikcije po ZPP Potrjujemo, • da je
     * naslovnik pošiljke z oznako <oznaka e-pošiljke> dne <datum posredovanja obvestila>
     * prejel obvestilo o tej pošiljki s pravnim poukom o posledicah neprevzema v 15 dneh, • da
     * naslovnik pošiljke v 15 dneh od dneva obvestila o prispeli pošiljki ni prevzel, zato se po
     * sedmem odstavku 141.a člena ZPP šteje, da je bila vročitev opravljena dne <datum
     * fikcije>, • da je bila po poteku 15 dnevnega roka iz sistema <ponudnik e-predala> naslovniku
     * pošiljka puščena v njegovem varnem elektronskem predalu in poslano obvestilo, da lahko
     * pisanje prevzame tudi pri < podatki o sodišču>. To sporočilo je potrdilo o vročitvi pošiljke
     * in opravljeni storitvi. Naša oznaka
     * <določi ponudnik e-predala>
     * Storitev : Elektronska vročitev pošiljke po ZPP Datum opravljene storitve
     * :<Datum: ponudnik e-predala>
     * <Kraj opravljene storitve>, <Datum nastanka obvestila>
     */
    MSHOutMail om = createOutMail();

    FOPUtils instance = new FOPUtils(new File(FOP_CONFIG_FILE), XSLT_FO_FOLDER);
    File ftxt = new File(LAU_HOME + "AdviceOfDeliveryFiction.txt");
    File fpdf = new File(LAU_HOME + "AdviceOfDeliveryFiction.pdf");
    instance.generateVisualization(om, ftxt, FOPUtils.FopTransformations.AdviceOfDeliveryFiction,
        MimeConstants.MIME_PLAIN_TEXT);

    instance.generateVisualization(om, fpdf, FOPUtils.FopTransformations.AdviceOfDeliveryFiction,
        MimeConstants.MIME_PDF);

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

    MSHOutMail mout = createOutMail();
    
    JAXBSource source = new JAXBSource(JAXBContext.newInstance(mout.getClass()), mout);

    FOPUtils instance = new FOPUtils(new File(FOP_CONFIG_FILE), XSLT_FO_FOLDER);

    try (FileOutputStream fos = new FileOutputStream(LAU_HOME + "test.txt")) {
      instance.generateVisualization(source, fos, new StreamSource(XSLT_FO_FOLDER +
          FOPUtils.FopTransformations.DeliveryNotification.getFileName()),
          MimeConstants.MIME_PLAIN_TEXT);
    }
    try (FileOutputStream fos = new FileOutputStream(LAU_HOME + "test.pdf")) {
      instance.generateVisualization(source, fos, new StreamSource(XSLT_FO_FOLDER +
          FOPUtils.FopTransformations.DeliveryNotification.getFileName()),
          MimeConstants.MIME_PDF);
    }

  }

  private MSHInMail createInMail() {

    MSHInMail im = new MSHInMail();

    im.setSenderMessageId("SM_ID-" + UUID.randomUUID().toString());
    im.setAction("action");
    im.setService("LegalDelivery_ZPP");
    im.setMessageId(UUID.randomUUID().toString()+ "@domain.com");
    im.setConversationId("2322@domain.com");
    im.setReceiverName("Mr. Receiver Name");
    im.setReceiverEBox("receiver.name@test-sed.si");
    im.setSenderName("Mr. Sender Name");
    im.setSenderEBox("izvrsba@test-sed.si");
    im.setSubject("Test content");

    im.setMSHInPayload(new MSHInPayload());
    MSHInPart ip = new MSHInPart();
    ip.setFilename("Test.txt");
    ip.setDescription("test attachment");
    ip.setMimeType(MimeValues.MIME_TEXI.getMimeType());
    
    MSHInPart ip2 = new MSHInPart();
    ip2.setFilename("Test.txt");
    ip2.setDescription("test attachment");
    ip2.setMimeType(MimeValues.MIME_TEXI.getMimeType());


    im.getMSHInPayload().getMSHInParts().add(ip);
    im.getMSHInPayload().getMSHInParts().add(ip2);

    MSHInProperty iprop1 = new MSHInProperty();
    iprop1.setName("Property 1");
    iprop1.setValue("value");
    MSHInProperty iprop2 = new MSHInProperty();
    iprop2.setName("Property 2");
    iprop2.setValue("value");
    im.setMSHInProperties(new MSHInProperties());
    im.getMSHInProperties().getMSHInProperties().add(iprop1);
    im.getMSHInProperties().getMSHInProperties().add(iprop2);
    
    
    return im;

  }

  private MSHOutMail createOutMail() {

    MSHOutMail om = new MSHOutMail();

    om.setSenderMessageId("SM_ID-" + UUID.randomUUID().toString());
    om.setAction("DeliveryNotification");
    om.setService("LegalDelivery_ZPP");
    om.setConversationId("1234@domain.com");
    om.setMessageId(UUID.randomUUID().toString() + "@domain.com");
    om.setReceiverName("Mr. Receiver Name");
    om.setReceiverEBox("receiver.name@test-sed.si");
    om.setSentDate(Calendar.getInstance().getTime());
    
    om.setSenderName("Mr. Sender Name");
    om.setSenderEBox("izvrsba@test-sed.si");
    om.setSubject("Test content");
    om.setMSHOutPayload(new MSHOutPayload());
    MSHOutPart op = new MSHOutPart();
    op.setFilename("Test.txt");
    op.setDescription("test file");
    op.setMimeType(MimeValues.MIME_TEXI.getMimeType());
    MSHOutPart op1 = new MSHOutPart();
    op1.setFilename("Test.txt");
    op1.setDescription("test file");
    op1.setMimeType(MimeValues.MIME_TEXI.getMimeType());
    
    om.getMSHOutPayload().getMSHOutParts().add(op);
    om.getMSHOutPayload().getMSHOutParts().add(op1);
    
    om.setMSHOutProperties(new MSHOutProperties());
    MSHOutProperty iprop1 = new MSHOutProperty();
        
    iprop1.setName("Vpisnik kratica");
    iprop1.setValue("VL");
    MSHOutProperty iprop2 = new MSHOutProperty();
    iprop2.setName("Opravilna st");
    iprop2.setValue("1235/2016");

    om.getMSHOutProperties().getMSHOutProperties().add(iprop1);
    om.getMSHOutProperties().getMSHOutProperties().add(iprop2);

    

    return om;

  }

}
