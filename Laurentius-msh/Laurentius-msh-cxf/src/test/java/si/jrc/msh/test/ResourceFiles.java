/*
 * Code was inspired by blog: Rafał Borowiec 
 * http://blog.codeleak.pl/2014/07/junit-testing-exception-with-java-8-and-lambda-expressions.html
 */
package si.jrc.msh.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import javax.activation.DataHandler;
import javax.activation.URLDataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import org.apache.cxf.attachment.AttachmentImpl;
import org.apache.cxf.binding.soap.Soap11;
import org.apache.cxf.binding.soap.Soap12;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.log4j.Logger;
import org.junit.Assert;
import si.laurentius.commons.MimeValues;

/**
 *
 * @author Jože Rihtaršič
 */
public class ResourceFiles {

  public static final Logger LOG = Logger.getLogger(ResourceFiles.class);
  private static final String ROOT_FOLDER = "/soap-test-requests/";

  public static final String S_REQUEST_SOAP11_HEADER = "messaging_soap11.xml";
  public static final String S_REQUEST_MISSING_HEADER = "messaging_missing.xml";
  public static final String S_REQUEST_MULTIPLE_MESSAGING = "messaging_multiple.xml";
  public static final String S_REQUEST_MESSAGING_XSD_INVALID = "messaging_xsd_invalid.xml";
  public static final String S_REQUEST_MESSAGING_XSD_INVALID_TO = "messaging_xsd_invalid_To.xml";
  public static final String S_REQUEST_MESSAGING_2UM = "messaging_two_usermessages.xml";
  public static final String S_REQUEST_MESSAGING_EMPTY = "messaging_empty.xml";
  public static final String S_REQUEST_VALID_SIMPLE = "valid_simple_message.xml";
  public static final String S_REQUEST_VALID_PAYLOAD = "valid_payload_message.xml";
  public static final String S_REQUEST_INVALID_PAYLOAD_MISSING_MIME = "invalid_payload_missing_mime.xml";
  
  public static final String S_REQUEST_INVALID_RECEIVER = "invalid_receiver.xml";
  public static final String S_REQUEST_INVALID_AGR_REF_URI = "invalid_AgreementRef_URI.xml";

  public static final String S_REQUEST_SIGN_INVALID = "signed_invalid.xml";

  public static final String S_REQUEST_SMIME_SIGN_ENC_VALID = "ebms_request_valid.soap";
  public static final String S_REQUEST_SMIME_ENC_INVALID = "ebms_request_invalid.soap";

  public static SoapMessage getSoap11Message(String msg) {
    return getSoapMessage(msg, SOAPConstants.SOAP_1_1_PROTOCOL);
  }

  public static SoapMessage getSoap12Message(String msg) {
    return getSoapMessage(msg, SOAPConstants.SOAP_1_2_PROTOCOL);
  }

  private static SoapMessage getSoapMessage(String resourceName, String protocol) {

    MessageFactory factory;
    try {
      factory = MessageFactory.newInstance(protocol);
    } catch (SOAPException ex) {
      LOG.error("Error creating MessageFactory for protocol: " + protocol, ex);
      return null;
    }
    SoapMessage msg = new SoapMessage(protocol.equals(SOAPConstants.SOAP_1_1_PROTOCOL) ?
        Soap11.getInstance() : Soap12.getInstance());

    SOAPMessage message;
    try {
      message =
          factory.createMessage(new MimeHeaders(),
              ResourceFiles.class.getResourceAsStream(ROOT_FOLDER + resourceName));
      msg.setContent(SOAPMessage.class, message);

      // set exchange with no pmode
      Exchange e = new ExchangeImpl();
      msg.setExchange(e);
    } catch (IOException | SOAPException ex) {
      LOG.error("Error creating Message for test file: " + ROOT_FOLDER + resourceName, ex);
      return null;
    }

    return msg;
  }

  public static SoapMessage getSoap12MessageFromSMime(String resourceName) {
    MessageFactory factory;
    try {
      factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
    } catch (SOAPException ex) {
      LOG.error("Error creating MessageFactory for protocol: " + SOAPConstants.SOAP_1_2_PROTOCOL, ex);
      return null;
    }
    SoapMessage msg = new SoapMessage(Soap12.getInstance());

    SOAPMessage message;
    try {

      URLDataSource fds = new URLDataSource(ResourceFiles.class.getResource(ROOT_FOLDER +
          resourceName));

      MimeMultipart multiPart = new MimeMultipart(fds);
      // first multipart is  soap message
      BodyPart pb = multiPart.getBodyPart(0);

      /*       //  System.out.println("Got payload: " + bp.getPreamble());
       System.out.println("*************************************************");
        for (Enumeration e = pb.getAllHeaders(); e.hasMoreElements();) {
          javax.mail.Header h = (javax.mail.Header) e.nextElement();
          System.out.println(String.format("Key %s -> value: %s", h.getName(), h.getValue()));
        }*/
      message =
          factory.createMessage(new MimeHeaders(), pb.getInputStream());

      msg.setContent(SOAPMessage.class, message);

      //  add attachments
      msg.setAttachments(new ArrayList<>());

      for (int i = 1; i < multiPart.getCount(); i++) {

        BodyPart bp = multiPart.getBodyPart(i);

        String id = bp.getHeader("Content-ID")[0];
        System.out.println("Got Content:" + id);
        id = id.startsWith("<") ? id.substring(1) : id;
        id = id.endsWith(">") ? id.substring(0, id.length() - 1) : id;
        System.out.println("Got Content:" + id);
        AttachmentImpl att = new AttachmentImpl(id);
        att.setHeader("id", id);

        ByteArrayDataSource bads = new ByteArrayDataSource(bp.getInputStream(),
            MimeValues.MIME_BIN.getMimeType());
        att.setDataHandler(new DataHandler(bads));
        //att.
        msg.getAttachments().add(att);

        /*//  System.out.println("Got payload: " + bp.getPreamble());
        for (Enumeration e = bp.getAllHeaders(); e.hasMoreElements();) {
          javax.mail.Header h = (javax.mail.Header) e.nextElement();
          System.out.println(String.format("Key %s -> value: %s", h.getName(), h.getValue()));
        }*/
      }

      msg.setContent(SOAPMessage.class, message);
      // set exchange with no pmode
      Exchange e = new ExchangeImpl();
      msg.setExchange(e);

    } catch (MessagingException | SOAPException | IOException ex) {
      LOG.error("Error creating Message for test file: " + ROOT_FOLDER + resourceName, ex);
      Assert.fail("Fail to create soap message from source: " + ROOT_FOLDER + resourceName);
      return null;
    }
    return msg;
  }

  private static String getStringFromInputStream(InputStream is) {

    BufferedReader br = null;
    StringBuilder sb = new StringBuilder();

    String line;
    try {

      br = new BufferedReader(new InputStreamReader(is));
      while ((line = br.readLine()) != null) {
        sb.append(line);
      }

    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    return sb.toString();

  }
}
