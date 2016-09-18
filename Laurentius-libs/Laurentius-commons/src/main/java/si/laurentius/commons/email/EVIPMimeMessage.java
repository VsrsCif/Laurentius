/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.laurentius.commons.email;

import java.io.StringWriter;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import static javax.mail.internet.InternetAddress.getLocalAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author Jože Rihtaršič
 */
public class EVIPMimeMessage extends MimeMessage {

  private String mStrMessageId = null;

  /**
   *
   * @param session
   * @param messageId
   */
  public EVIPMimeMessage(Session session, String messageId) {
    super(session);
    this.session = session;
    mStrMessageId = messageId;
  }

  /**
   *
   * @param ssn
   * @return
   */
  public String getUniqueMessageIDValue(Session ssn) {
    String suffix = null;

    InternetAddress addr = getLocalAddress(ssn);
    if (addr != null) {
      suffix = addr.getAddress();
    } else {
      suffix = "no-suffix"; // worst-case default
    }

    StringWriter s = new StringWriter();

    // Unique string is <hashcode>.<id>.<currentTime>.JavaMail.<suffix>
    // s.append(s.hashCode()+"");
    // s.append('.');
    s.append(mStrMessageId).append('.');
    // s.append(""+System.currentTimeMillis()).append('.');
    s.append("EVIPMail.");
    s.append(suffix);
    return s.toString();
  }

  /**
     *
     */
  @Override
  public void updateMessageID() throws MessagingException {
    setHeader("Message-ID", "<" + getUniqueMessageIDValue(session) + ">");
  }

}
