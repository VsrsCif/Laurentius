/*
 * Copyright 2015, Supreme Court Republic of Slovenia
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
package si.mju.plugin;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.mail.MessagingException;
import javax.naming.NamingException;
import si.laurentius.commons.email.EmailAttachmentData;
import si.laurentius.commons.email.EmailData;
import si.laurentius.commons.email.EmailUtils;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.StringFormater;
import si.laurentius.commons.utils.Utils;
import static si.laurentius.commons.utils.Utils.isEmptyString;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.inbox.payload.MSHInPart;
import si.laurentius.plugin.interfaces.InMailProcessorInterface;
import si.laurentius.plugin.interfaces.PropertyType;
import si.laurentius.plugin.interfaces.exception.InMailProcessException;
import si.laurentius.plugin.processor.InMailProcessorDef;
import si.laurentius.plugin.processor.MailProcessorPropertyDef;

/**
 *
 * @author Jože Rihtaršič
 */
@Stateless
@Local(InMailProcessorInterface.class)
public class MJUMailSubmitter implements InMailProcessorInterface {

  /**
   * Thanks to:
   * https://www.mkyong.com/regular-expressions/how-to-validate-email-address-with-regular-expression/
   */
  private static final Pattern EMAIL_PATTEREN = Pattern.compile(
          "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
          + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

  public static final String KEY_MJU_EMAIL_FROM = "si.mju.email.from";
  public static final String KEY_MJU_EMAIL_TO = "si.mju.email.to";
  public static final String KEY_MJU_EMAIL_SUBJECT = "si.mju.subject";
  public static final String KEY_MJU_EMAIL_CONFIG_JNDI = "si.mju.mail.config.jndi";

  private static final SEDLogger LOG = new SEDLogger(MJUMailSubmitter.class);

  StorageUtils msStorageUtils = new StorageUtils();

  @Override
  public InMailProcessorDef getDefinition() {
    InMailProcessorDef impd = new InMailProcessorDef();
    impd.setType("mju-mail");
    impd.setName("MJU email submitter");
    impd.setDescription("Example of email submitter");
    
    impd.getMailProcessorPropertyDeves().add(createProperty(
            KEY_MJU_EMAIL_FROM, "sender@change-me.com",
            "Test email from address", true,
            PropertyType.String.getType(), null, null));

    impd.getMailProcessorPropertyDeves().add(createProperty(
            KEY_MJU_EMAIL_TO, "receiver@change-me.com", "Test email to address",
            true,
            PropertyType.String.getType(), null, null));

    impd.getMailProcessorPropertyDeves().add(createProperty(
            KEY_MJU_EMAIL_SUBJECT,
            "[LAURENTIUS-TEST] ${Id} ${SenderEBox} ${Service}", "Mail subject",
            true,
            PropertyType.String.getType(), null, null));

    impd.getMailProcessorPropertyDeves().add(createProperty(
            KEY_MJU_EMAIL_CONFIG_JNDI,
            "java:jboss/mail/Default",
            "Mail config jndi(def: java:jboss/mail/Default)",
            true,
            PropertyType.String.getType(), null, null));

    return impd;

  }

  @Override
  public List<String> getInstanceIds() {
    return Collections.emptyList();
  }

  @Override
  public boolean proccess(MSHInMail mi, Map<String, Object> map) throws InMailProcessException {
    long l = LOG.logStart(mi.getId());
    boolean suc = false;
    // ------------------------------
    // read and validate init parameters
    String emailJNDI = (String) map.get(KEY_MJU_EMAIL_CONFIG_JNDI);
    if (Utils.isEmptyString(emailJNDI)) {
      // set default value
      emailJNDI = "java:jboss/mail/Default";
    }
    String emailSubject = (String) map.get(KEY_MJU_EMAIL_SUBJECT);
    if (Utils.isEmptyString(emailSubject)) {
      // set default value
      emailSubject = "[LAURENTIUS-TEST] ${Id} ${SenderEBox} ${Service}";
    }
    String emailTo = (String) map.get(KEY_MJU_EMAIL_TO);
    validateEmailAddress(emailTo);    
    String emailFrom = (String) map.get(KEY_MJU_EMAIL_FROM);
    validateEmailAddress(emailFrom);

    // ------------------------------
    // create and submit mail
    EmailData emd = new EmailData(emailTo, null, StringFormater.
            format(emailSubject, mi), createMailBody(mi));
    emd.setEmailSenderAddress(emailFrom);

    if (mi.getMSHInPayload() != null) {
      for (MSHInPart mip : mi.getMSHInPayload().getMSHInParts()) {
        EmailAttachmentData ead = new EmailAttachmentData(mip.getFilename(),
                mip.getMimeType(), StorageUtils.getFile(mip.getFilepath()));
        emd.getAttachments().add(ead);
      }
    }
    EmailUtils memailUtil = new EmailUtils();
    try {
      memailUtil.sendMailMessage(emd, emailJNDI);
      suc = true;
    } catch (MessagingException | NamingException | IOException ex) {
      LOG.logError(ex.getMessage(), ex);
      throw new InMailProcessException(
              InMailProcessException.ProcessExceptionCode.ProcessException, ex.
                      getMessage(),
              ex);
    }

    return suc;
  }

  public String createMailBody(MSHInMail mi) {
    StringWriter sw = new StringWriter();
    sw.append("Spoštovani\n\n");
    sw.append("prejeli ste sporočilo v vaš laurentius predal: \n");
    sw.append(mi.getReceiverName());
    sw.append(" (");
    sw.append(mi.getReceiverName());
    sw.append("),\n");
    sw.append("\npošiljatelj: ");
    sw.append(mi.getSenderName());
    sw.append("(");
    sw.append(mi.getSenderEBox());
    sw.append(").\n");

    sw.append("\nOpis vsebine: ");
    sw.append(mi.getSubject());
    sw.append("\nStoritev: ");
    sw.append(mi.getService());
    sw.append("\nAkcija: ");
    sw.append(mi.getAction());
    sw.append("\nPošiljateljeva oznaka sporočila: ");
    sw.append(mi.getSenderMessageId());

    sw.append(mi.getSenderEBox());
    sw.append(")\n\n\nPriponke:");
    for (MSHInPart mp : mi.getMSHInPayload().getMSHInParts()) {
      sw.append("\n - ");
      sw.append(mp.getName());
      sw.append(" ");
      sw.append(mp.getFilename());
      sw.append(" ");
      sw.append(mp.getDescription());
    }
    return sw.toString();
  }

  protected MailProcessorPropertyDef createProperty(String key, String defValue,
          String desc, boolean mandatory, String type, String valFormat,
          String valList) {
    MailProcessorPropertyDef ttp = new MailProcessorPropertyDef();
    ttp.setKey(key);
    ttp.setDefValue(defValue);
    ttp.setDescription(desc);
    ttp.setMandatory(mandatory);
    ttp.setType(type);
    ttp.setValueFormat(valFormat);
    ttp.setValueList(valList);
    return ttp;
  }

  public void validateEmailAddress(String strVal) throws InMailProcessException {
    if (isEmptyString(strVal)) {
      throw new InMailProcessException(
              InMailProcessException.ProcessExceptionCode.InitException,
              "Mail address is mandatory! Check configuration for this process instance!");
    } else {
      Matcher m = EMAIL_PATTEREN.matcher(strVal);
      if (!m.matches()) {
        throw new InMailProcessException(
                InMailProcessException.ProcessExceptionCode.InitException,
                String.format(
                        "Mail address '%s' is invalid! Check configuration for this process instance!",
                        strVal));
      }
    }
  }

}
