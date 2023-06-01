package si.vsrs.cif.laurentius.plugin.zkp.doc;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.lce.DigestMethodCode;
import si.laurentius.lce.sign.xml.XMLSignatureUtils;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.outbox.mail.MSHOutMail;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.util.Calendar;
import java.util.List;

/**
 *
 * @author Jože Rihtaršič
 */
public abstract class DocumentBuilder {

  public static final String SIGNATURE_REASON = "Laurentius: Legal delivery advice";
  /**
   *
   */
  public static final String CREA_V1 = "CREA_V1";

  /**
   *
   */
  protected static final String DELIVERY_TYPE = "Legal-ZPP2";

  /**
   *
   */
  protected static final String DOCUMENT_TYPE = "Message";

  /**
   *
   */
  protected static final String ENC_TYPE_B64 = "base64";

  /**
   *
   */
  protected static final String ENC_TYPE_UTF8 = "UTF-8";

  /**
   *
   */
  protected static final String IDPFX_DATA = "dat-test";

  /**
   *
   */
  protected static final String IDPFX_VIS = "vis-test";

  /**
   *
   */
  protected static final String MIME_PDF = "application/pdf";

  /**
   *
   */
  protected static final String MIME_TXT = "text/xml";
  private static final String SIGNATURE_ELEMENT_NAME = "Signatures";
  // schema type

  /**
   *
   */
  public static final String SOD_V1 = "SOD_V1";

  /**
   *
   * @param strVal
   * @param strFile
   */
  public static void writeToFile(String strVal, String strFile) {

    try (FileOutputStream fos = new FileOutputStream(strFile)) {
      fos.write(strVal.getBytes(ENC_TYPE_UTF8));
    } catch (IOException ex) {

    }
  }

  Logger mlgLogger = Logger.getLogger(DocumentBuilder.class.getName());

  // private static ESignDocImpl medSigJDK = null;
  private XMLSignatureUtils mssuSignUtils;
  StorageUtils msuStorageUtils = new StorageUtils();

  /**
   *
   * @param jaxBDoc
   * @param cls
   * @return
   * @throws SEDSecurityException
   */
  protected Document convertEpDoc2W3cDoc(Object jaxBDoc, Class[] cls)
      throws SEDSecurityException {
    Document xDoc = null;
    try {
      javax.xml.parsers.DocumentBuilderFactory dbf =
          javax.xml.parsers.DocumentBuilderFactory.newInstance();
      javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
      xDoc = db.newDocument();

      JAXBContext jc = JAXBContext.newInstance(cls);
      // Marshal the Object to a Document
      Marshaller marshaller = jc.createMarshaller();
      marshaller.marshal(jaxBDoc, xDoc);
    } catch (JAXBException ex) {
      String strMsg =
          "DocumentBuilder.convertEpDoc2W3cDoc: could marshal Document: JAXBException: '" +
          ex.getMessage() + "'.";
      mlgLogger.error(strMsg, ex);
      throw new SEDSecurityException(
          SEDSecurityException.SEDSecurityExceptionCode.CreateSignatureException, ex);
    } catch (ParserConfigurationException ex) {
      String strMsg =
          "DocumentBuilder.convertEpDoc2W3cDoc: could not create w3c document: ParserConfigurationException: '" +
          ex.getMessage() + "'.";
      mlgLogger.error(strMsg, ex);
      throw new SEDSecurityException(
          SEDSecurityException.SEDSecurityExceptionCode.CreateSignatureException, ex);
    }

    return xDoc;
  }

  /**
   *
   * @param dce
   * @param fos
   * @param key
   * @throws SEDSecurityException
   */
  public abstract void createMail(MSHOutMail dce, FileOutputStream fos, KeyStore.PrivateKeyEntry key)
      throws SEDSecurityException;
  
    /**
   *
   * @param dce
   * @param fos
   * @param key
   * @throws SEDSecurityException
   */
  public abstract void createMail(MSHInMail dce, FileOutputStream fos, KeyStore.PrivateKeyEntry key)
      throws SEDSecurityException;

  /**
   *
   * @return
   */
  public XMLSignatureUtils getSignUtils() {
    if (mssuSignUtils == null) {
      mssuSignUtils = new XMLSignatureUtils();
      // mssuSignUtils.setTimeStampServerUrl(Settings.getInstance().getTimestampUrl());
    }
    return mssuSignUtils;
  }

  /**
   *
   * @return
   */
  protected long getTime() {
    return Calendar.getInstance().getTimeInMillis();
  }

  /**
   *
   * @param xDoc
   * @param strIds
   * @param fos
   * @param key
   * @throws SEDSecurityException
   */
  protected synchronized void singDocument(Document xDoc, List<String> strIds,
      FileOutputStream fos, KeyStore.PrivateKeyEntry key)
      throws SEDSecurityException {
    long t = getTime();
    mlgLogger.info("DocumentBuilder.singDocument: begin ");

    NodeList lst = xDoc.getDocumentElement().getElementsByTagName(SIGNATURE_ELEMENT_NAME);
    Element eltSignature = (Element) lst.item(0);

    // method is necessery for SVEV integrations
    getSignUtils().createXAdESEnvelopedSignature(key, eltSignature, strIds, DigestMethodCode.SHA256,
        SignatureMethod.RSA_SHA1, SIGNATURE_REASON);

    // write it to output..            
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer trans;
    try {
      trans = tf.newTransformer();

      trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      trans.transform(new DOMSource(xDoc), new StreamResult(fos));
    } catch (TransformerException ex) {
      throw new SEDSecurityException(SEDSecurityException.SEDSecurityExceptionCode.ReadWriteFileException);
      
    }

    mlgLogger.info("DocumentBuilder.singDocument: - end (" + (getTime() - t) + "ms)");
  }

}
