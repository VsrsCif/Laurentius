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
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
public class MJUProcessorInZPPMail implements InMailProcessorInterface {

  public static final String KEY_MJU_EMAIL_FROM = "si.mju.email.from";
  public static final String KEY_MJU_EMAIL_TO = "si.mju.email.to";
  public static final String KEY_MJU_EMAIL_SUBJECT = "si.mju.subject";
  public static final String KEY_MJU_EMAIL_CONFIG_JNDI = "si.mju.mail.config.jndi";

  private static final SEDLogger LOG = new SEDLogger(MJUProcessorInZPPMail.class);

  StorageUtils msStorageUtils = new StorageUtils();

  @Override
  public InMailProcessorDef getDefinition() {
    InMailProcessorDef impd = new InMailProcessorDef();
    impd.setType("mju-zppmail");
    impd.setName("MJU-ZPP");
    impd.setDescription("Process in zpp mail");
    impd.getMailProcessorPropertyDeves().add(createProperty(
            KEY_MJU_EMAIL_FROM, "true", "Test email from address", true,
            PropertyType.String.getType(), null, null));

    impd.getMailProcessorPropertyDeves().add(createProperty(
            KEY_MJU_EMAIL_TO, "true", "Test email to address", true,
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
    String smtpConf = null;

    String emailJNDI = (String) map.get(KEY_MJU_EMAIL_CONFIG_JNDI);
    String emailSubjecd = (String) map.get(KEY_MJU_EMAIL_SUBJECT);
    String emailTo = (String) map.get(KEY_MJU_EMAIL_TO);
    String emailFrom = (String) map.get(KEY_MJU_EMAIL_FROM);

    EmailData emd = new EmailData(emailTo, null, StringFormater.
            format(emailSubjecd, mi), "New mail");
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
      return true;
    } catch (MessagingException | NamingException | IOException ex) {
      LOG.logError(ex.getMessage(), ex);
    }

    return suc;
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

}
