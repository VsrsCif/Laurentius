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
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.xml.bind.JAXBException;
import javax.xml.ws.WebServiceException;
import si.gov.nio.cev._2015.document.Document;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.Utils;
import si.laurentius.commons.utils.xml.XMLUtils;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.mail.MSHPartType;
import si.laurentius.plugin.interfaces.InMailProcessorInterface;
import si.laurentius.plugin.interfaces.PropertyType;
import si.laurentius.plugin.interfaces.exception.InMailProcessException;
import si.laurentius.plugin.processor.InMailProcessorDef;
import si.laurentius.plugin.processor.MailProcessorPropertyDef;
import si.mju.plugin.doc.DocumentBuilder;
import si.mju.plugin.doc.DocumentMJUBuilder;
import si.mju.plugin.doc.KeystoreUtils;

/**
 *
 * @author Jože Rihtaršič
 */
@Stateless
@Local(InMailProcessorInterface.class)
public class MJUProcessorInZPPMail implements InMailProcessorInterface {

  public static final String KEY_MJU_SVEV1_URL = "mjusvev.url";

  public static final String KEY_MJU_SVEV1_KEYSTORE_FILENAME = "keystore.filename";
  public static final String KEY_MJU_SVEV1_KEYSTORE_TYPE = "keystore.type";
  public static final String KEY_MJU_SVEV1_KEYSTORE_PASSWD = "keystore.passwd";

  public static final String KEY_MJU_SVEV1_SIGN_ALIAS = "sign.key.alias";
  public static final String KEY_MJU_SVEV1_SIGN_KEY_PASSWD = "sign.keystore.passwd";
  public static final String KEY_MJU_SVEV1_DELETE_LOG_FILES = "delete.log.files";

  private static final SEDLogger LOG = new SEDLogger(MJUProcessorInZPPMail.class);

  StorageUtils msStorageUtils = new StorageUtils();
  KeystoreUtils mKeystoreUtils = new KeystoreUtils();
  DocumentBuilder mdbDocBuilder = new DocumentMJUBuilder();

  private static final String PLUGIN_ROOT_FOLDER = String.format("${%s}/%s/",
          SEDSystemProperties.SYS_PROP_PLUGINS_DIR,
          AppConstants.PLUGIN_FOLDER);

  File mfLogFolder = null;

  @Override
  public InMailProcessorDef getDefinition() {
    InMailProcessorDef impd = new InMailProcessorDef();
    impd.setType("mju-zppmail");
    impd.setName("MJU-ZPP");
    impd.setDescription("Posredovanje obvestila o prispeli pošiljki");
    impd.getMailProcessorPropertyDeves().add(createProperty(
            KEY_MJU_SVEV1_URL, "http://cev.sigov.si/...",
            "URL do MJU SVEV 1.0 storitve", true,
            PropertyType.String.getType(), null, null));

    impd.getMailProcessorPropertyDeves().add(createProperty(
            KEY_MJU_SVEV1_KEYSTORE_FILENAME,
            PLUGIN_ROOT_FOLDER + "/mju-keystore.jks",
            "Shramba digitalnih potrdil", true,
            PropertyType.String.getType(), null, null));

    impd.getMailProcessorPropertyDeves().add(createProperty(
            KEY_MJU_SVEV1_KEYSTORE_TYPE, "JKS",
            "Tip shramba digitalnih potrdil", true,
            PropertyType.List.getType(), null,
            "JKS,PKCS12"));
    impd.getMailProcessorPropertyDeves().add(createProperty(
            KEY_MJU_SVEV1_KEYSTORE_PASSWD, "test1234",
            "Geslo za dostop do shrambe digitalnih potrdil", true,
            PropertyType.String.getType(), null, null));
    impd.getMailProcessorPropertyDeves().add(createProperty(
            KEY_MJU_SVEV1_SIGN_ALIAS, "svev-test",
            "Oznaka (Alias) ključa za podpis generiranega sporočila", true,
            PropertyType.String.getType(), null, null));
    impd.getMailProcessorPropertyDeves().add(createProperty(
            KEY_MJU_SVEV1_SIGN_KEY_PASSWD, "key1234",
            "Geslo za dostop do ključa (Alias)", true,
            PropertyType.String.getType(), null, null));

    impd.getMailProcessorPropertyDeves().add(createProperty(
            KEY_MJU_SVEV1_DELETE_LOG_FILES, "false",
            "Izbriši začasne datoteke", false,
            PropertyType.Boolean.getType(), null, null));
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

    String svev1Url = (String) map.get(KEY_MJU_SVEV1_URL);
    String keystoreFilename = (String) map.get(KEY_MJU_SVEV1_KEYSTORE_FILENAME);
    String keystoreType = (String) map.get(KEY_MJU_SVEV1_KEYSTORE_TYPE);
    String keystorePasswd = (String) map.get(KEY_MJU_SVEV1_KEYSTORE_PASSWD);
    String keyAlias = (String) map.get(KEY_MJU_SVEV1_SIGN_ALIAS);
    String keyPasswd = (String) map.get(KEY_MJU_SVEV1_SIGN_KEY_PASSWD);
    boolean bDelLogFiles = Boolean.getBoolean((String) map.get(
            KEY_MJU_SVEV1_DELETE_LOG_FILES));

    
    // 
    KeyStore.PrivateKeyEntry pkKey =  null;
    if (!Utils.isEmptyString(keystoreFilename)
        && !Utils.isEmptyString(keyAlias) ) {
    KeyStore ks = mKeystoreUtils.getKeyStore(keystoreFilename, keystoreType, keystorePasswd);
    pkKey = mKeystoreUtils.getPrivateKeyEntryForAlias(ks, keyAlias,
            keyPasswd);
    }

    MSHPartType mpt = null;
    File fSVEV1DocRequest = null;
    File fSVEV1DocResponse = null;

    try {
      // create delivery status notification
      mpt = createStatusDeliveryNotification();

      fSVEV1DocRequest = new File(getLogFolder(), String.format(
              "mju-svev-file_%07d_request.xml", mi.getId()));

      fSVEV1DocResponse = new File(getLogFolder(), String.format(
              "mju-svev-file_%07d_response.xml", mi.getId()));

      if (fSVEV1DocRequest.exists()) {
        fSVEV1DocRequest.delete();
      }
      if (fSVEV1DocResponse.exists()) {
        fSVEV1DocResponse.delete();
      }
      // build SVEV 1 document

      Document docReq = mdbDocBuilder.
              createMail(mi, Collections.singletonList(mpt), pkKey);
      // test
      if (!bDelLogFiles) {
        XMLUtils.serialize(docReq, fSVEV1DocRequest);
      }
      Document docRes;
      try {
        docRes = getMJUService(svev1Url).deliverMessage(docReq);
        if (!bDelLogFiles) {
          XMLUtils.serialize(docRes, fSVEV1DocResponse);
        }
      } catch (DmsFault ex) {
        if (!bDelLogFiles) {
          XMLUtils.serialize(ex.getFaultInfo(), fSVEV1DocResponse);
        }
        LOG.logError(ex.getMessage(), ex);
        throw new InMailProcessException(
                InMailProcessException.ProcessExceptionCode.ProcessException,
                ex. getMessage(), ex);

      }

      // todo process response
      // submit to webservice
      // 
    } catch (JAXBException | SEDSecurityException | IOException ex) {
      LOG.logError(ex.getMessage(), ex);
      throw new InMailProcessException(
              InMailProcessException.ProcessExceptionCode.ProcessException, ex.
                      getMessage(), ex);
    } finally {

      if (mpt != null) {
        File f = StorageUtils.getFile(mpt.getFilepath());
        f.delete();
      }
    }

    // create mail
    return suc;
  }

  private MjuDmsWSPortBinding getMJUService(String url) throws InMailProcessException {
    MjuDmsWSPortBinding dp = null;
    try {
      MjuDmsWebServiceService mjuwebService = new MjuDmsWebServiceService(
              new URL(url));
      dp = mjuwebService.getMjuDmsWebServicePort();
      
    } catch (WebServiceException | MalformedURLException ex ){
      throw new InMailProcessException(
              InMailProcessException.ProcessExceptionCode.ProcessException, ex.
                      getMessage(), ex);
    }
    return dp;
  }

  private MSHPartType createStatusDeliveryNotification() throws InMailProcessException {
    FileWriter fwriter = null;
    MSHPartType mpt = new MSHPartType();
    try {
      File f = StorageUtils.getNewStorageFile("txt", "mju-svev-sn_");
      fwriter = new FileWriter(f);
      fwriter.append("Pozdravljeni\n\n");
      fwriter.append("Povratnico smo posredovali pošiljatelju. ");
      fwriter.append(
              "Ko bo povratnica izročena pošiljatelju bo omogočen vpogled v vsebino pošiljke.\n");
      fwriter.append(
              "Status dostave pošiljke lahko spremljate na tem naslovu: http://...\n\n\n");
      fwriter.append("Lep pozdrav!");
      fwriter.append("Sistem za varno elektronsko vročanje");
      fwriter.flush();

      mpt.setDescription("Status vročitve");
      mpt.setMimeType(MimeValue.getMimeTypeByFileName(f.getName()));
      mpt.setEncoding(Charset.defaultCharset().name());
      mpt.setFilename(f.getName());
      mpt.setName("Status vročitve");
      mpt.setFilepath(StorageUtils.getRelativePath(f));

    } catch (StorageException | IOException ex) {
      LOG.logError(ex.getMessage(), ex);
      throw new InMailProcessException(
              InMailProcessException.ProcessExceptionCode.ProcessException, ex.
                      getMessage(),
              ex);
    } finally {
      if (fwriter != null) {
        try {
          fwriter.close();
        } catch (IOException ignore) {
          LOG.formatedWarning("Error closing temp file. Error %s", ignore.
                  getMessage());
        }
      }
    }
    return mpt;
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

  public File getLogFolder() {
    if (mfLogFolder == null) {
      mfLogFolder = new File(SEDSystemProperties.getPluginsFolder(),
              AppConstants.PLUGIN_FOLDER + "/" + "log");
      if (!mfLogFolder.exists()) {
        mfLogFolder.mkdirs();
      }
    }
    return mfLogFolder;
  }

}
