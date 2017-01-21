/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.jrc.msh.transport;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.logging.Logger;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.transport.AbstractConduit;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import si.laurentius.commons.cxf.SoapUtils;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.pmode.EBMSMessageContext;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.pmode.PartyIdentitySetType;

/**
 *
 * @author Joze Rihtarsic
 * "Sandbox" class for using smtp conduit 
 *  
 *   
 */
public class SMTPConduit extends AbstractConduit {

  private static final Logger LOGGER = LogUtils.getL7dLogger(SMTPConduit.class);
  private static final SEDLogger LOG = new SEDLogger(SMTPConduit.class);
  
  private String setJNDISession  ="java:jboss/mail/Default";

  Bus bus;

  public SMTPConduit(EndpointReferenceType t, final Bus bus) {
    super(t);
    this.bus = bus;   
  }

  @Override
  public void prepare(final Message msg)
      throws IOException {

    File f;
    try {
      f = StorageUtils.getNewStorageFile("soap", "test");
      msg.setContent(OutputStream.class, new SMTPOutStream(msg, f));
    } catch (StorageException ex) {
      throw new IOException(ex);
    }

  }

  @Override
  public void close() {
    super.close();

  }

  @Override
  public void close(Message msg)
      throws IOException {
    super.close(msg); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  private void dataReceived(Message message, byte[] bytes, boolean async) {
    final Message inMessage = new MessageImpl();
    inMessage.setExchange(message.getExchange());
    message.getExchange().setInMessage(inMessage);
    incomingObserver.onMessage(inMessage);
    if (bytes!=null) {
      inMessage.setContent(InputStream.class, new ByteArrayInputStream(bytes));
      
    }
    if (!message.getExchange().isSynchronous()) {
      message.getExchange().setInMessage(null);
    }
  }

  class SMTPOutStream extends FileOutputStream {

    File f;
    final Message outMessage;

    public SMTPOutStream(Message m, File file)
        throws FileNotFoundException {
      super(file);
      f = file;
      outMessage = m;
    }

    @Override
    public void close()
        throws IOException {
      super.close();
      try {
        
        Session session = (Session) PortableRemoteObject.narrow(new InitialContext().lookup(
            setJNDISession), Session.class);
        
        MSHOutMail mo = SoapUtils.getMSHOutMail(outMessage);
        EBMSMessageContext moc = SoapUtils.getEBMSMessageOutContext(outMessage);
        
        String senderName="";
        for (PartyIdentitySetType.PartyId pid: moc.getSenderPartyIdentitySet().getPartyIds()){
          if (pid.getType().equalsIgnoreCase("mailto")){
          senderName = pid.getFixValue();
          }
      
        };
        String receiverName="";
        for (PartyIdentitySetType.PartyId pid: moc.getReceiverPartyIdentitySet().getPartyIds()){
          if (pid.getType().equalsIgnoreCase("mailto")){
          receiverName = pid.getFixValue();
          }
      
        };
        
        MimeMessage m = new MimeMessage(session);
        String emailid = mo.getMessageId();
        m.addHeader("id", emailid);
        m.setFrom(new InternetAddress(senderName));
        m.setSender(new InternetAddress(senderName));
        m.setRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(receiverName));
        
        
        m.setSubject(mo.getSubject());
        m.setSentDate(new Date());

        /*Multipart multipart = new MimeMultipart();
        
        
        
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setHeader("Content-Type", "text/plain; charset=\"utf-8\"");
        messageBodyPart.setContent("test message to mail", "text/plain; charset=\"utf-8\"");
        messageBodyPart.setDisposition(Part.INLINE);
        multipart.addBodyPart(messageBodyPart);
        

        SOAPMessage sreq = outMessage.getContent(SOAPMessage.class);

        MimeBodyPart soap = new MimeBodyPart();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        sreq.writeTo(bos);
        ByteDataSource bds = new ByteDataSource(bos.toByteArray(), "application/soap+xml");
        soap.setDataHandler(new DataHandler(bds));
        
        soap.setDisposition(Part.ATTACHMENT);
        multipart.addBodyPart(soap);

        for (Attachment at : outMessage.getAttachments()) {

          MimeBodyPart mbp = new MimeBodyPart();
          mbp.setDataHandler(at.getDataHandler());
          mbp.setDisposition(Part.ATTACHMENT);

          Iterator<String> itAt = at.getHeaderNames();
          while (itAt.hasNext()) {
            String str = itAt.next();
            mbp.addHeader(str, at.getHeader(str));

          }
          multipart.addBodyPart(mbp);

        }
        
        m.setContent(multipart);*/
        System.out.println("********************* 3");
        FileDataSource fds = new FileDataSource(f);

        Multipart mp = new MimeMultipart();
        Multipart multipart = new MimeMultipart(fds);
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setHeader("Content-Type", "text/plain; charset=\"utf-8\"");
        messageBodyPart.setContent("Laurentius ebms message over SMTP", "text/plain; charset=\"utf-8\"");
        messageBodyPart.setDisposition(Part.INLINE);
        mp.addBodyPart(messageBodyPart);
        for (int i = 0; i < multipart.getCount(); i++) {
          BodyPart bp = multipart.getBodyPart(i);

          if (i == 0) {
            bp.setHeader("Content-Disposition", "attachment; filename=soap-part.xml");
          }
/*          map.setDataHandler(bp.getDataHandler());
*/          

          
          bp.setHeader("Content-Transfer-Encoding", "base64");
          bp.setDisposition(Part.ATTACHMENT);
          mp.addBodyPart(bp);
          LOG.formatedWarning("BodyPart %d, has mime %s ", i, bp.getContentType());

        }
        System.out.println("********************* 2" + multipart.getParent());
        // Put parts in message
        m.setContent(mp);
        System.out.println("********************* 1");

        m.saveChanges();
        
        Transport.send(m);
        
      } catch (NamingException | MessagingException ex) {
        ex.printStackTrace();
      }

       dataReceived(outMessage, null, false);
    }

  }

  public String getSetJNDISession() {
    return setJNDISession;
  }

  public void setSetJNDISession(String setJNDISession) {
    this.setJNDISession = setJNDISession;
  }

}
