package si.laurentius.commons.email;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sluzba
 */
public class EmailData {

  List<EmailAttachmentData> mlstAttachments = new ArrayList<>();
  String mstrBody;
  String mstrEmailAddresses;
  String mstrEmailCCAddresses;
  String mstrEmailSenderAddress;
  String mstrSubject;

  /**
   *
   * @param mstrEmailAddresses
   * @param mstrEmailCCAddresses
   * @param mstrSubject
   * @param mstrBody
   */
  public EmailData(String mstrEmailAddresses, String mstrEmailCCAddresses, String mstrSubject,
      String mstrBody) {
    this.mstrEmailAddresses = mstrEmailAddresses;
    this.mstrEmailCCAddresses = mstrEmailCCAddresses;
    this.mstrSubject = mstrSubject;
    this.mstrBody = mstrBody;
  }

  /**
   *
   * @return
   */
  public List<EmailAttachmentData> getAttachments() {
    return mlstAttachments;
  }

  /**
   *
   * @return
   */
  public String getBody() {
    return mstrBody;
  }

  /**
   *
   * @return
   */
  public String getEmailAddresses() {
    return mstrEmailAddresses;
  }

  /**
   *
   * @return
   */
  public String getEmailCCAddresses() {
    return mstrEmailCCAddresses;
  }

  /**
   *
   * @return
   */
  public String getEmailSenderAddress() {
    return mstrEmailSenderAddress;
  }

  /**
   *
   * @return
   */
  public String getSubject() {
    return mstrSubject;
  }

  /**
   *
   * @param mstrBody
   */
  public void setBody(String mstrBody) {
    this.mstrBody = mstrBody;
  }

  /**
   *
   * @param mstrEmailAddresses
   */
  public void setEmailAddresses(String mstrEmailAddresses) {
    this.mstrEmailAddresses = mstrEmailAddresses;
  }

  /**
   *
   * @param mstrEmailCCAddresses
   */
  public void setEmailCCAddresses(String mstrEmailCCAddresses) {
    this.mstrEmailCCAddresses = mstrEmailCCAddresses;
  }

  /**
   *
   * @param mstrEmailSenderAddress
   */
  public void setEmailSenderAddress(String mstrEmailSenderAddress) {
    this.mstrEmailSenderAddress = mstrEmailSenderAddress;
  }

  /**
   *
   * @param mstrSubject
   */
  public void setSubject(String mstrSubject) {
    this.mstrSubject = mstrSubject;
  }

  @Override
  public String toString() {
    return "EmailData{" + "to=" + mstrEmailAddresses + ", cc=" + mstrEmailCCAddresses
        + ", subject=" + mstrSubject + ", Attachments size=" + mlstAttachments.size() + '}';
  }

}
