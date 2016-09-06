package si.laurentius.commons.email;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import static java.nio.charset.Charset.forName;
import java.nio.charset.CharsetEncoder;
import static java.util.Calendar.getInstance;
import java.util.Date;
import static java.util.UUID.randomUUID;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import static javax.mail.Message.RecipientType.CC;
import static javax.mail.Message.RecipientType.TO;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import static javax.mail.Transport.send;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import static javax.mail.internet.MimeUtility.encodeText;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

/**
 *
 * @author sluzba
 */
public class EmailUtils {

  private static final String S_MIME_TXT = "text/plain";

  static CharsetEncoder asciiEncoder = forName("US-ASCII").newEncoder();
  // private static final String S_OUTMAIL_ADDRESS = "nobody@sodisce.si";
  // do no use DCLogger -> cyclic dependecy..
  private static final Logger mlgLogger = getLogger(EmailUtils.class.getName());

  private Address[] getAddresses(String strAddrString) throws AddressException,
      UnsupportedEncodingException {
    Address[] to = null;
    if (strAddrString == null || strAddrString.trim().isEmpty()) {
      return to;
    }
    String[] lstAdr = strAddrString.split(",");
    to = new InternetAddress[lstAdr.length];
    for (int i = 0; i < lstAdr.length; i++) {
      String adr = lstAdr[i].trim();
      if (asciiEncoder.canEncode(adr)) {
        to[i] = new InternetAddress(adr);
      } else { // suppose non asci char in name !!
        String[] lst = adr.split("<");
        if (lst.length == 2) {
          to[i] = new InternetAddress(lst[1].replaceAll(">", ""), lst[0].trim(), "UTF-8");
        }

      }
    }

    return to;

  }

  /**
   *
   * @param emailAddress
   * @param subject
   * @param body
   * @param mailConfig
   * @throws NamingException
   * @throws IOException
   */
  public void sendMailMessage(String emailAddress, String subject, String body, String mailConfig)
      throws MessagingException, NamingException, IOException {

    sendMailMessage(new EmailData(emailAddress, null, subject, body), mailConfig);

  }

  /**
   *
   * @param eml
   * @param mailConfig
   * @throws MessagingException
   * @throws IOException
   */
  public void sendMailMessage(EmailData eml, String mailConfig) throws MessagingException,
      NamingException, IOException {
    mlgLogger.info("EmailUtils.sendMailMessage: " + eml.toString());
    long l = getInstance().getTimeInMillis();
    Session session = (Session) new InitialContext().lookup(mailConfig);
    String emailid = "sed-" + randomUUID().toString();
    MimeMessage m = new EVIPMimeMessage(session, emailid);

    m.setContentID(emailid);
    m.setContentID(emailid);
    m.addHeader("sed-id", emailid);

    Address[] to = getAddresses(eml.getEmailAddresses());
    m.setFrom(new InternetAddress(eml.getEmailSenderAddress()));
    m.setSender(new InternetAddress(eml.getEmailSenderAddress()));
    m.setRecipients(TO, to);
    if (eml.getEmailCCAddresses() != null) {
      Address[] toCC = getAddresses(eml.getEmailCCAddresses());
      m.setRecipients(CC, toCC);
    }

    String subject = eml.getSubject();

    m.setSubject(subject.length() > 160 ? subject.substring(0, 160) : subject);
    m.setSentDate(new Date());
    Multipart multipart = new MimeMultipart();
    StringWriter swrBody = new StringWriter();
    swrBody.append(eml.getBody());

    MimeBodyPart messageBodyPart = new MimeBodyPart();
    messageBodyPart.setHeader("Content-Type", S_MIME_TXT + "; charset=\"utf-8\"");
    messageBodyPart.setContent(swrBody.toString(), S_MIME_TXT + "; charset=\"utf-8\"");
    multipart.addBodyPart(messageBodyPart);

    for (EmailAttachmentData d : eml.getAttachments()) {
      mlgLogger.info("EmailUtils.sendMailMessage: - add attachments doc  "
          + d.getFile().getAbsolutePath());
      MimeBodyPart messageattachmentPart = new MimeBodyPart();
      DataSource source = new FileDataSource(d.getFile());
      messageattachmentPart.setDataHandler(new DataHandler(source));
      messageattachmentPart.setFileName(encodeText(d.getFileName()));
      multipart.addBodyPart(messageattachmentPart);
    }
    // Put parts in message
    m.setContent(multipart);

    send(m);
    mlgLogger.info("EmailUtils.sendMailMessage: " + eml.toString() + " - END ( "
        + (getInstance().getTimeInMillis() - l) + " ms)");

  }

}
