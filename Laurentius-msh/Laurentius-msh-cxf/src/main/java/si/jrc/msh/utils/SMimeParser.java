/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.jrc.msh.utils;

import static com.google.common.base.CharMatcher.is;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Enumeration;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import jdk.nashorn.internal.ir.Symbol;

/**
 *
 * @author Jože Rihtaršič
 */
public class SMimeParser {
  
  public static void main(String... args)
      throws MessagingException, FileNotFoundException, IOException, SOAPException {

    //String file = "/sluzba/data/laurentius.home/ebms_log/2016/08/27/test-mime.soap";
    String file = "/sluzba/data/laurentius.home/ebms_log/2016/08/28/ebms_0000000050-r001_out-request.soap";
    
    String file2 = "/sluzba/code/SVEV2.0/Laurentius/Laurentius-msh/Laurentius-msh-cxf/src/test/resources/soap-test-requests/valid_simple_message.xml";
    
    MessageFactory factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
    SOAPMessage msg2 =
        factory.createMessage(null, new FileInputStream(file2));
  
    msg2.setContentDescription("test");
    
    msg2.writeTo(System.out);
    
    System.out.println("GET HEADER PART - simple: " + msg2.getSOAPHeader());
    System.out.println("GET getSOAPPart PART - simple: " + msg2.getSOAPPart().getEnvelope().getHeader());
    System.out.println("GET getSOAPBody PART - simple: " + msg2.getSOAPBody());
    
    FileDataSource fds = new FileDataSource(file);
    
    MimeMultipart multiPart = new MimeMultipart(fds);
    
    
    BodyPart pb = multiPart.getBodyPart(0);
    
    String msg ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+ getStringFromInputStream(
            pb.getInputStream());
    System.out.println("GOT MESSAGE " + msg);
    SOAPMessage message =
        factory.createMessage(new MimeHeaders(), new ByteArrayInputStream(msg.getBytes(Charset.forName("UTF-8"))));
    
    System.out.println("GET HEADER PART: " + message.getSOAPHeader());
    message.writeTo(System.out);
    
    for (int i = 0; i < multiPart.getCount(); i++) {
      
      BodyPart bp = multiPart.getBodyPart(i);
      System.out.println("Got payload: " + getStringFromInputStream(bp.getInputStream()));
      //  System.out.println("Got payload: " + bp.getPreamble());
      
      for (Enumeration e = bp.getAllHeaders(); e.hasMoreElements();) {
        javax.mail.Header h = (javax.mail.Header) e.nextElement();
        System.out.println(String.format("Key %s -> value: %s", h.getName(), h.getValue()));
      }
      
    }
    
    System.out.println("GOT COUNT: " + multiPart.getCount());
    
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
