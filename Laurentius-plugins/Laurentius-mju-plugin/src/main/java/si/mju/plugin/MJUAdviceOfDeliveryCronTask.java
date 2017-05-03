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

import com.comtrade.mju.dms.DmsFault;
import com.comtrade.mju.dms.MjuDmsWSPortBinding;
import com.comtrade.mju.dms.MjuDmsWebServiceService;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.xml.bind.JAXBException;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.WebServiceException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import si.gov.nio.cev._2015.AcknowledgeAdviceDeliveryRequest;

import si.gov.nio.cev._2015.GetUndeliveredAdviceResponse;
import si.gov.nio.cev._2015.MailMessage;
import si.gov.nio.cev._2015.document.AttachmentType;
import si.gov.nio.cev._2015.document.Document;
import si.gov.nio.cev._2015.document.VisualisationType;
import si.gov.nio.cev._2015.message.Message;
import si.laurentius.commons.SEDJNDI;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.enums.SEDInboxMailStatus;
import si.laurentius.commons.enums.SEDOutboxMailStatus;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.interfaces.SEDCertStoreInterface;
import si.laurentius.commons.interfaces.SEDDaoInterface;

import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.StringFormater;
import si.laurentius.commons.utils.Utils;
import si.laurentius.commons.utils.xml.XMLUtils;
import si.laurentius.lce.DigestMethodCode;
import si.laurentius.lce.DigestUtils;
import si.laurentius.lce.KeystoreUtils;
import si.laurentius.lce.sign.xml.XMLSignatureUtils;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.laurentius.msh.outbox.payload.MSHOutPayload;
import si.laurentius.plugin.crontask.CronTaskDef;
import si.laurentius.plugin.crontask.CronTaskPropertyDef;
import si.laurentius.plugin.interfaces.PropertyType;
import si.laurentius.plugin.interfaces.TaskExecutionInterface;
import si.laurentius.plugin.interfaces.exception.TaskException;

/**
 * This is samople of cron task plugin component.
 *
 * @author Jože Rihtaršič
 */
@Stateless
@Local(TaskExecutionInterface.class)
public class MJUAdviceOfDeliveryCronTask implements TaskExecutionInterface {

  public static final String KEY_MJU_SVEV1_URL = "mjusvev.url";
  public static final String KEY_MJU_MAX_COUNT = "mjusvev.da.max.count";

  public static final String KEY_MJU_SVEV1_KEYSTORE_FILENAME = "keystore.filename";
  public static final String KEY_MJU_SVEV1_KEYSTORE_TYPE = "keystore.type";
  public static final String KEY_MJU_SVEV1_KEYSTORE_PASSWD = "keystore.passwd";

  public static final String KEY_MJU_SVEV1_SIGN_ALIAS = "sign.key.alias";
  public static final String KEY_MJU_SVEV1_SIGN_KEY_PASSWD = "sign.keystore.passwd";

  private static final String SIGNATURE_ELEMENT_NAME = "Signatures";

  private XMLSignatureUtils mssuSignUtils = new XMLSignatureUtils();
  KeystoreUtils mKeystoreUtils = new KeystoreUtils();

  @EJB(mappedName = SEDJNDI.JNDI_SEDDAO)
  SEDDaoInterface mDB;
  


  private static final SEDLogger LOG = new SEDLogger(
          MJUAdviceOfDeliveryCronTask.class);

  /**
   * execute metod
   *
   * @param map
   * @param p - parameters defined at configuration of task instance
   * @return result description
   */
  @Override
  public String executeTask(Properties map)
          throws TaskException {
    long l = LOG.logStart();
    StringWriter sw = new StringWriter();
    sw.append("Read all DelvieryAdvices from SVEVMJU: ");
    sw.append("\n");

    String mjuUrl = map.getProperty(KEY_MJU_SVEV1_URL);
    if (Utils.isEmptyString(mjuUrl)) {
      throw new TaskException(TaskException.TaskExceptionCode.InitException,
              "Missing parameter:  '" + KEY_MJU_SVEV1_URL + "'!");
    }

    int iCnt = 10;

    String intCnt = map.getProperty(KEY_MJU_MAX_COUNT);
    if (!Utils.isEmptyString(intCnt)) {
      try {
        iCnt = Integer.parseInt(intCnt);
      } catch (NumberFormatException nfe) {
        LOG.logError(
                "Number: 'KEY_MJU_MAX_COUNT' is not a number (10 is setted)!",
                nfe);
      }
    }

    String keystoreFilename = (String) map.get(KEY_MJU_SVEV1_KEYSTORE_FILENAME);
    String keystoreType = (String) map.get(KEY_MJU_SVEV1_KEYSTORE_TYPE);
    String keystorePasswd = (String) map.get(KEY_MJU_SVEV1_KEYSTORE_PASSWD);
    String keyAlias = (String) map.get(KEY_MJU_SVEV1_SIGN_ALIAS);
    String keyPasswd = (String) map.get(KEY_MJU_SVEV1_SIGN_KEY_PASSWD);
    KeyStore.PrivateKeyEntry pkKey = null;
    if (!Utils.isEmptyString(keystoreFilename)
            && !Utils.isEmptyString(keyAlias)) {
      try {
        File fKS = new File(StringFormater.replaceProperties(keystoreFilename));
        KeyStore ks = mKeystoreUtils.getKeystore(fKS, keystoreType,
                keystorePasswd.toCharArray());
        pkKey = mKeystoreUtils.getPrivateKeyEntryForAlias(ks, keyAlias,
                keyPasswd);
      } catch (SEDSecurityException ex) {
        throw new TaskException(TaskException.TaskExceptionCode.InitException,
                ex.getMessage());
      }
    }

    MjuDmsWSPortBinding mjuws = getMJUService(mjuUrl);

    List<MailMessage> mmLst = null;
    try {
      GetUndeliveredAdviceResponse ur = mjuws.getUndeliveredAdvice(iCnt);
      mmLst = ur.getMailMessages();
    } catch (DmsFault ex) {
      String msg = String.format(
              "DmsFault %s, occured while retrieving UndeliveredAdvices ", ex.
                      getFaultInfo().getDescription());
      LOG.logError(msg, ex);
      throw new TaskException(TaskException.TaskExceptionCode.InitException,
              msg);
    }

    for (int i = 0; i < iCnt; i++) {
      MailMessage mm = mmLst.get(i);
      String id = mm.getMessageUuid();
      Document d = (Document) mm.getMailMessageContent().getContent().get(0);
      MSHInMail mi = validateDeliveryAdvice(d, id);
      if (mi != null) {
        try {
          submitDeliveryAdvice(d, id, mi, pkKey);
          AcknowledgeAdviceDeliveryRequest amdr = new AcknowledgeAdviceDeliveryRequest();
          amdr.getMessageUuids().add(id);
          mjuws.acknowledgeAdviceDelivery(amdr);
        } catch (ZPPException ex) {
          String msg = String.format(
                  "ZPPException %s, occured while resending AcknowledgeAdviceDeliveryRequest ",
                  ex.getMessage());
          LOG.logError(msg, ex);
          throw new TaskException(TaskException.TaskExceptionCode.InitException,
                  msg);
        } catch (DmsFault ex) {
           String msg = String.format(
                  "DmsFault %s, occured while acknowledgeAdviceDelivery ",
                  ex.getMessage());
          LOG.logError(msg, ex);
          throw new TaskException(TaskException.TaskExceptionCode.InitException,
                  msg);
        }
      }

    }

    sw.append("Example task ends in : " + (l - LOG.getTime()) + " ms\n");
    LOG.logEnd(l, sw.toString());
    return sw.toString();
  }

  /**
   *
   * @param d
   * @param messageId
   * @return
   */
  public MSHInMail validateDeliveryAdvice(Document d, String messageId) {

    if (d.getData() == null
            || d.getData().getContent() == null
            || d.getData().getContent().getMessages().isEmpty()) {
      LOG.formatedWarning(
              "DeliveryAdvice 'Document' %s with no message data or content??'",
              messageId);
      return null;
    }

    if (d.getData().getContent().getMessages().size() > 1) {
      LOG.formatedWarning(
              "DeliveryAdvice 'Document' %s invalid message data count: %s'",
              messageId, d.getData().getContent().getMessages().size());
      return null;
    }

    Message m = d.getData().getContent().getMessages().get(0);
    String msgConvID = m.getRelatesTo();

    if (Utils.isEmptyString(msgConvID)) {
      LOG.formatedWarning(
              "DeliveryAdvice 'Document' %s with empty 'relatesTo'",
              messageId);
      return null;
    }

    List<MSHInMail> lstIm = mDB.getInMailConvIdAndAction(
            ZPPConstants.S_ZPP_ACTION_DELIVERY_NOTIFICATION,
            msgConvID);

    if (lstIm.isEmpty()) {
      LOG.formatedWarning(
              "DeliveryAdvice 'Document' %s with invalid transaction/relates to %s (Delivery notification not exist!)",
              messageId, msgConvID);
      return null;
    }
    if (lstIm.size() > 1) {
      LOG.formatedWarning(
              "DeliveryAdvice 'Document' %s with invalid transaction/relatesTo %s (More than one delivery notification!)",
              messageId, msgConvID);
      return null;
    }
    MSHInMail mIn = lstIm.get(0);

    return mIn;
  }

  public void submitDeliveryAdvice(Document d, String messageId, MSHInMail mIn,
          KeyStore.PrivateKeyEntry key) throws ZPPException {
    long l = LOG.logStart();
    List<String> singIDS = new ArrayList<>();

    Message m = d.getData().getContent().getMessages().get(0);
    String msgConvID = m.getRelatesTo();

    singIDS.add(d.getData().getId());
    if (d.getVisualisations() != null) {
      for (VisualisationType vt : d.getVisualisations().getVisualisations()) {
        if (!Utils.isEmptyString(vt.getId())) {
          singIDS.add(vt.getId());
        } else {
          String msg = String.
                  format("Document %s has visualization with no id!", messageId);
          throw new ZPPException(msg);
        }
      }
    }
    if (d.getAttachments() != null) {
      for (AttachmentType at : d.getAttachments().getAttachments()) {
        if (!Utils.isEmptyString(at.getId())) {
          singIDS.add(at.getId());
        } else {
          String msg = String.
                  format("Document %s has visualization with no id!", messageId);
          throw new ZPPException(msg);
        }
      }
    }

    MSHOutMail mout = new MSHOutMail();
    mout.setMessageId(Utils.getInstance().getGuidString());
    mout.setService(ZPPConstants.S_ZPP_SERVICE);
    mout.setAction(ZPPConstants.S_ZPP_ACTION_ADVICE_OF_DELIVERY);
    mout.setConversationId(msgConvID);
    mout.setSenderEBox(m.getFrom().getPoBoxId());
    mout.setSenderName(m.getFrom().getPhysicalAddress().getName());
    mout.setRefToMessageId(mIn.getMessageId());
    mout.setReceiverEBox(m.getTo().getAddresses().get(0).getPoBoxId());
    mout.setReceiverName(m.getTo().getAddresses().get(0).getPhysicalAddress().
            getName());
    mout.setSubject(m.getSubject());

    Date dt = new Date();
    // set current status
    mout.setStatus(SEDOutboxMailStatus.SUBMITTED.getValue());
    mout.setSubmittedDate(dt);
    mout.setStatusDate(dt);

    // create attachment
    File fDNViz = null;
    try {

      fDNViz = StorageUtils.getNewStorageFile("xml", "AdviceOfDelivery");

      XMLUtils.serialize(d, fDNViz);

      mout.setMSHOutPayload(new MSHOutPayload());
      MSHOutPart mp = new MSHOutPart();
      mp.setDescription("DeliveryAdvice");
      mp.setMimeType(MimeValue.MIME_XML.getMimeType());
      mout.getMSHOutPayload().getMSHOutParts().add(mp);
      mp.setSha256Value(DigestUtils.getHexSha256Digest(fDNViz));
      mp.setSize(BigInteger.valueOf(fDNViz.length()));
      mp.setFilename(fDNViz.getName());
      mp.setFilepath(StorageUtils.getRelativePath(fDNViz));
      mp.setName(mp.getFilename().
              substring(0, mp.getFilename().lastIndexOf(".")));

      mDB.serializeOutMail(mout, "", "ZPPDeliveryPlugin", "");
      mDB.setStatusToInMail(mIn, SEDInboxMailStatus.PREADY,
              "AdviceOfDelivery created and submitted to out queue");
    } catch (StorageException | JAXBException | FileNotFoundException ex) {
      String msg = ex.getMessage();
      LOG.logError(l, msg, ex);
      throw new ZPPException(msg);
    }

  }

  protected synchronized org.w3c.dom.Document signDocument(Document doc,
          List<String> strIds,
          KeyStore.PrivateKeyEntry key) throws SEDSecurityException, JAXBException, ParserConfigurationException {

    org.w3c.dom.Document xDoc = XMLUtils.jaxbToDocument(doc);

    DigestMethodCode digestMethodCode = DigestMethodCode.SHA1;
    String sigMethod = SignatureMethod.RSA_SHA1;
    String strReason = "SVEV2toSVEV1";

    NodeList lst = xDoc.getDocumentElement().getElementsByTagName(
            SIGNATURE_ELEMENT_NAME);
    Element eltSignature = (Element) lst.item(0);

    mssuSignUtils.createXAdESEnvelopedSignature(key, eltSignature, strIds,
            digestMethodCode, sigMethod, strReason);

    return xDoc;

  }

  /**
   * Retrun cron task definition: name, unique type, description, parameters..
   *
   * @return Cron task definition
   */
  @Override
  public CronTaskDef getDefinition() {
    CronTaskDef impd = new CronTaskDef();

    impd.setType("mju-zppmail");
    impd.setName("MJU-ZPP");
    impd.setDescription("Posredovanje obvestila o prispeli pošiljki");
    impd.getCronTaskPropertyDeves().add(createProperty(
            KEY_MJU_SVEV1_URL, "http://cev.sigov.si/...",
            "URL do MJU SVEV 1.0 storitve", true,
            PropertyType.String.getType(), null, null));
    impd.getCronTaskPropertyDeves().add(createProperty(
            KEY_MJU_MAX_COUNT, "false",
            "Max number of delivery advices", false,
            PropertyType.Integer.getType(), null, null));

    impd.getCronTaskPropertyDeves().add(createProperty(
            KEY_MJU_SVEV1_KEYSTORE_FILENAME,
            AppConstants.PLUGIN_ROOT_FOLDER + "/mju-keystore.jks",
            "Shramba digitalnih potrdil", true,
            PropertyType.String.getType(), null, null));

    impd.getCronTaskPropertyDeves().add(createProperty(
            KEY_MJU_SVEV1_KEYSTORE_TYPE, "JKS",
            "Tip shramba digitalnih potrdil", true,
            PropertyType.List.getType(), null,
            "JKS,PKCS12"));
    impd.getCronTaskPropertyDeves().add(createProperty(
            KEY_MJU_SVEV1_KEYSTORE_PASSWD, "test1234",
            "Geslo za dostop do shrambe digitalnih potrdil", true,
            PropertyType.String.getType(), null, null));
    impd.getCronTaskPropertyDeves().add(createProperty(
            KEY_MJU_SVEV1_SIGN_ALIAS, "svev-test",
            "Oznaka (Alias) ključa za podpis generiranega sporočila", true,
            PropertyType.String.getType(), null, null));
    impd.getCronTaskPropertyDeves().add(createProperty(
            KEY_MJU_SVEV1_SIGN_KEY_PASSWD, "key1234",
            "Geslo za dostop do ključa (Alias)", true,
            PropertyType.String.getType(), null, null));

    return impd;
  }

  private CronTaskPropertyDef createProperty(String key, String defValue,
          String desc, boolean mandatory, String type, String valFormat,
          String valList) {
    CronTaskPropertyDef ttp = new CronTaskPropertyDef();
    ttp.setKey(key);
    ttp.setDefValue(defValue);
    ttp.setDescription(desc);
    ttp.setMandatory(mandatory);
    ttp.setType(type);
    ttp.setValueFormat(valFormat);
    ttp.setValueList(valList);
    return ttp;
  }

  private MjuDmsWSPortBinding getMJUService(String url) throws TaskException {
    MjuDmsWSPortBinding dp = null;
    try {
      MjuDmsWebServiceService mjuwebService = new MjuDmsWebServiceService(
              new URL(url));
      dp = mjuwebService.getMjuDmsWebServicePort();

    } catch (WebServiceException | MalformedURLException ex) {
      throw new TaskException(
              TaskException.TaskExceptionCode.InitException, ex.
                      getMessage(), ex);
    }
    return dp;
  }

}
