/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package si.vsrs.cif.laurentius.plugin.zkp.utils;

import org.apache.cxf.binding.soap.SoapFault;
import si.vsrs.cif.laurentius.plugin.zkp.ZKPConstants;
import si.vsrs.cif.laurentius.plugin.zkp.enums.FopTransformation;
import si.vsrs.cif.laurentius.plugin.zkp.enums.ZKPPartPropertyType;
import si.vsrs.cif.laurentius.plugin.zkp.enums.ZKPPartType;
import si.vsrs.cif.laurentius.plugin.zkp.exception.ZKPErrorCode;
import si.vsrs.cif.laurentius.plugin.zkp.exception.ZKPException;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.SEDValues;
import si.laurentius.commons.ebms.EBMSError;
import si.laurentius.commons.exception.FOPException;
import si.laurentius.commons.exception.HashException;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.StorageUtils;
import si.laurentius.commons.utils.Utils;
import si.laurentius.commons.utils.xml.XMLUtils;
import si.laurentius.lce.DigestUtils;
import si.laurentius.lce.enc.SEDCrypto;
import si.laurentius.lce.sign.pdf.SignUtils;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.inbox.payload.IMPartProperty;
import si.laurentius.msh.inbox.payload.MSHInPart;
import si.laurentius.msh.mail.MSHMailType;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.laurentius.msh.outbox.payload.MSHOutPayload;
import si.laurentius.msh.outbox.payload.OMPartProperty;
import si.laurentius.enckey.SEDEncryptionKey;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.Key;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Objects;

/**
 *
 * @author sluzba
 */
public class ZKPUtils {

  protected final static SEDLogger LOG = new SEDLogger(ZKPUtils.class);
  SEDCrypto mscCrypto = new SEDCrypto();
  FOPUtils mfpFop = null;

  /**
   *
   * @param skey
   * @param mail
   * @return
   * @throws SEDSecurityException
   * @throws StorageException
   */
  public MSHOutPayload createEncryptedPayloads(Key skey, MSHOutMail mail)
          throws SEDSecurityException, StorageException {
    long l = LOG.logStart();
    MSHOutPayload op = new MSHOutPayload();

    for (MSHOutPart ptSource : mail.getMSHOutPayload().getMSHOutParts()) {

      op.getMSHOutParts().add(createEncryptedPart(skey, ptSource));
    }
    LOG.logEnd(l, "Generated encrypted parts: '" + op.getMSHOutParts().size()
            + "' for out mail" + mail.getId());
    return op;
  }

  /**
   *
   * @param skey
   * @param ptSource
   * @return
   * @throws SEDSecurityException
   * @throws StorageException
   * @throws HashException
   */
  public MSHOutPart createEncryptedPart(Key skey, MSHOutPart ptSource)
          throws SEDSecurityException, StorageException {
    long l = LOG.logStart();

    // get files
    File fIn = StorageUtils.getFile(ptSource.getFilepath());
    File fOut = new File(fIn.getAbsoluteFile() + ZKPPartType.EncryptedPart.
            getFileSuffix());
    String fileName = (Utils.isEmptyString(ptSource.getFilename()) ? fIn.
            getName() : ptSource.
                    getFilename()) + "." + ZKPPartType.EncryptedPart.
                    getFileSuffix();
    // encrypt file
    mscCrypto.encryptFile(fIn, fOut, skey);
    // create part      
    MSHOutPart ptNew = new MSHOutPart();

    ptNew.setSource(ZKPConstants.ZKP_PLUGIN_TYPE);
    ptNew.setIsSent(Boolean.TRUE);
    ptNew.setIsReceived(Boolean.FALSE);
    ptNew.setGeneratedFromPartId(ptSource.getId());

    ptNew.setFilepath(StorageUtils.getRelativePath(fOut));
    ptNew.setEbmsId(Utils.getUUIDWithLocalDomain());
    ptNew.setSha256Value(DigestUtils.getBase64Sha256Digest(fOut));
    ptNew.setSize(BigInteger.valueOf(fOut.length()));
    ptNew.setName(ptSource.getName());

    ptNew.setMimeType(ZKPPartType.EncryptedPart.getMimeType());
    ptNew.setType(ZKPPartType.EncryptedPart.getPartType());
    ptNew.setDescription(ZKPPartType.EncryptedPart.getDescription(ptSource.
            getDescription()));
    ptNew.setFilename(fileName);

    ptNew.setIsEncrypted(Boolean.TRUE);

    /**
     * Backward compatibility for 1.0
     */
    addOrUpdatePartProperty(ptNew, ZKPConstants.PART_PROPERTY_ORIGIN_MIMETYPE,
            ptSource.getMimeType());
    /**
     * Properties
     */
    addOrUpdatePartProperty(ptNew, ZKPPartPropertyType.RefPartEbmsId.getType(),
            ptSource.getEbmsId());
    addOrUpdatePartProperty(ptNew, ZKPPartPropertyType.RefPartMimeType.getType(),
            ptSource.getMimeType());
    addOrUpdatePartProperty(ptNew, ZKPPartPropertyType.RefPartType.getType(),
            ptSource.getType());
    addOrUpdatePartProperty(ptNew, ZKPPartPropertyType.RefPartName.getType(),
            ptSource.getName());
    addOrUpdatePartProperty(ptNew, ZKPPartPropertyType.RefPartDesc.getType(),
            ptSource.getDescription());
    addOrUpdatePartProperty(ptNew, ZKPPartPropertyType.RefPartDigestSHA256.
            getType(), ptSource.getSha256Value());
    addOrUpdatePartProperty(ptNew, ZKPPartPropertyType.PartCreated.getType(),
            LocalDateTime.now().format(
                    DateTimeFormatter.ISO_DATE_TIME));

    for (OMPartProperty sourceProp : ptSource.getOMPartProperties()) {
      addOrUpdatePartProperty(ptNew, sourceProp.getName(), sourceProp.getValue());
    }

    LOG.logEnd(l, String.format(
            "Generated encrypted part (new part id: %s) for mail: %d,  part id: %d '",
            ptNew.getEbmsId(), ptSource.getMailId(), ptSource.getId()));
    return ptNew;
  }

  /**
   *
   * @param mail
   * @param skey
   * @return
   */
  public SEDEncryptionKey createNewKeyForMail(MSHOutMail mail,
          Key skey) {

    long l = LOG.logStart(mail.getId());

    SEDEncryptionKey sk = new SEDEncryptionKey();
    sk.setAlgorithm(skey.getAlgorithm());
    sk.setKeyformat(skey.getFormat());
    sk.setSecredKey(skey.getEncoded());
    sk.setKeySize(BigInteger.valueOf(skey.getEncoded().length));
    sk.setEbmsId(mail.getMessageId());
    LOG.logEnd(l, mail.getId());
    return sk;
  }

  /**
   *
   * @param mail
   * @param skey
   * @param alg
   * @return
   * @throws StorageException
   * @throws JAXBException
   * @throws FileNotFoundException
   */
  public MSHOutPart createLocalEncKeyPart(MSHOutMail mail,
          Key skey, SEDCrypto.SymEncAlgorithms alg)
          throws StorageException, JAXBException, FileNotFoundException {

    long l = LOG.logStart(mail.getId());

    // create local encryption key
    SEDEncryptionKey sk = new SEDEncryptionKey();
    sk.setAlgorithm(alg.getURI());
    sk.setKeyformat(skey.getFormat());
    sk.setSecredKey(skey.getEncoded());
    sk.setKeySize(BigInteger.valueOf(skey.getEncoded().length));
    sk.setEbmsId(mail.getMessageId());

    File fdek = StorageUtils.getNewStorageFile(
            ZKPPartType.LocalEncryptionKey.getFileSuffix(), 
            ZKPPartType.LocalEncryptionKey.name());
    XMLUtils.serialize(sk, fdek);

    MSHOutPart ptNew = new MSHOutPart();
    ptNew.setIsSent(Boolean.FALSE);
    ptNew.setIsReceived(Boolean.FALSE);
    ptNew.setSource(ZKPConstants.ZKP_PLUGIN_TYPE);
    ptNew.setEncoding(SEDValues.ENCODING_UTF8);
    ptNew.setMimeType(ZKPPartType.LocalEncryptionKey.getMimeType());
    ptNew.setName(ZKPPartType.LocalEncryptionKey.getPartName());
    ptNew.setType(ZKPPartType.LocalEncryptionKey.getPartType());
    ptNew.setDescription(ZKPPartType.LocalEncryptionKey.getDescription(null));
    ptNew.setFilepath(StorageUtils.getRelativePath(fdek));
    ptNew.setFilename(String.format("%s-%d.%s",
            ZKPPartType.LocalEncryptionKey.getPartName(),
            mail.getId() != null ? mail.getId() : 0,
            ZKPPartType.LocalEncryptionKey.getFileSuffix()));
    ptNew.setIsEncrypted(Boolean.FALSE);

    addOrUpdatePartProperty(ptNew, ZKPPartPropertyType.PartCreated.getType(),
            LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

    addOrUpdatePartProperty(ptNew, ZKPPartPropertyType.PartKeyAlg.getType(),
            sk.getAlgorithm());
    addOrUpdatePartProperty(ptNew, ZKPPartPropertyType.PartKeyFormat.getType(),
            sk.getKeyformat());
    if (sk.getKeySize() != null) {
      addOrUpdatePartProperty(ptNew, ZKPPartPropertyType.PartKeySize.getType(),
              sk.getKeySize().toString());
    }

    if (sk.getSecredKey() != null) {
      addOrUpdatePartProperty(ptNew, ZKPPartPropertyType.PartKeyValue.getType(),
              Base64.getEncoder().encodeToString(sk.getSecredKey())
      );
    }

    LOG.logEnd(l, mail.getId());
    return ptNew;
  }

  /**
   * Method returns secred key from mail's LocalEncryptionKey part. If outmail
   * does not contain payload part LocalEncryptionKey than ZKPException is
   * returned
   *
   * @param om
   * @return
   * @throws SEDSecurityException
   * @throws ZKPException
   * @throws StorageException
   * @throws JAXBException
   * @throws FileNotFoundException
   */
  public Key getEncKeyFromOut(MSHOutMail om)
          throws SEDSecurityException, ZKPException, StorageException {
    long l = LOG.logStart();
    MSHOutPart encPart = null;
    for (MSHOutPart op : om.getMSHOutPayload().getMSHOutParts()) {
      if (Objects.equals(ZKPPartType.LocalEncryptionKey.getPartType(), op.
              getType())) {
        encPart = op;
        break;
      }

    }
    if (encPart == null) {
      throw new ZKPException("Missing LocalEncryptionKey");
    }
    Key sKey = getEncKeyFromLocalPart(encPart);
    LOG.logEnd(l, String.format(
            "Got LocalEncryptionKey MSHOutPart for mail ebmsID %s.", om.
                    getMessageId()));
    return sKey;
  }

  /**
   * Method returns key from LocalEncryptionKey part.
   *
   * @param encPart
   * @return
   * @throws SEDSecurityException
   * @throws ZKPException
   * @throws StorageException
   */
  public Key getEncKeyFromLocalPart(MSHOutPart encPart)
          throws SEDSecurityException {
    long l = LOG.logStart();
    File sedKey = StorageUtils.getFile(encPart.getFilepath());

    SEDEncryptionKey sk;
    try {
      sk = (SEDEncryptionKey) XMLUtils.deserialize(sedKey,
              SEDEncryptionKey.class);
    } catch (JAXBException ex) {
      throw new SEDSecurityException(
              SEDSecurityException.SEDSecurityExceptionCode.InvalidKey,
              String.format("File '%s',  partId: %d from mailId: %d!", encPart.
                      getFilepath(), encPart.getId(), encPart.getMailId()), ex.
              getMessage());
    }

    SEDCrypto.SymEncAlgorithms sa = SEDCrypto.SymEncAlgorithms.
            getAlgorithmByURI(sk.getAlgorithm());
    if (sa == null) {
      throw new SEDSecurityException(
              SEDSecurityException.SEDSecurityExceptionCode.NoSuchAlgorithm, sk.
                      getAlgorithm(), String.format(
                      "Error occured while retrieving key from payload partId: %d from mailId: %d",
                      encPart.getId(), encPart.getMailId()));
    }

    Key sKey = new SecretKeySpec(sk.getSecredKey(), sa.getJCEName());
    LOG.logEnd(l, String.format("Got key for partId %s.", encPart.getEbmsId()));
    return sKey;
  }

  /**
   * Method generates AdviceOfDelivery for incomming mail.
   *
   * @param mail MSHInMail in mail for which AdviceOfDelivery is created
   * @param pk
   * @param xcert
   * @return
   *
   */
  public MSHOutPart createSignedAdviceOfDelivery(MSHInMail mail,
          PrivateKey pk, X509Certificate xcert) {

    return createMSHOutPart(mail, ZKPPartType.AdviceOfDelivery,
            FopTransformation.AdviceOfDelivery, pk, xcert);
  }

  public MSHInPart createSignedNotDeliveredNotification(MSHOutMail mail,
                                                        PrivateKey pk, X509Certificate xcert) throws FOPException, SEDSecurityException, HashException, StorageException {

    return createMSHInPart(mail, ZKPPartType.NotDeliveredNotification,
            FopTransformation.NotDeliveredNotification, pk, xcert);
  }

  /**
   * Update deliveryNotification with new vizualization. If message is
   * redelivered next day (days) new vizualization with correct day of
   * submittion and delivery to receivery box must be added.
   *
   * @param outMail
   * @param part
   * @param pk
   * @param xcert
   * @throws SEDSecurityException
   * @throws StorageException
   * @throws HashException
   * @throws FOPException
   */
  public void updateSignedDeliveryNotification(MSHOutMail outMail,
          MSHOutPart part, PrivateKey pk, X509Certificate xcert)
          throws SEDSecurityException, StorageException, HashException, FOPException {

    updateMSHOutPartVisualization(outMail, part,
            ZKPPartType.DeliveryNotification,
            FopTransformation.DeliveryNotification, pk, xcert);
  }

  public MSHOutPart createEncryptedKey(Key key, X509Certificate xcert,
          String receiverBox, String convId)
          throws SEDSecurityException, StorageException, IOException {
    long l = LOG.logStart(receiverBox,  convId);

    ZKPPartType partType = ZKPPartType.EncryptedKey;

    // generate ecrypted key
    File fEncryptedKey
            = StorageUtils.getNewStorageFile(partType.getFileSuffix(),
                    partType.name() + "-");

    try (FileOutputStream fos = new FileOutputStream(fEncryptedKey)) {
      String strKey = mscCrypto.encryptKeyWithReceiverPublicKey(key, xcert,
              receiverBox, convId);
      fos.write(strKey.getBytes());
      fos.flush();

    }

    // generate mshout part
    MSHOutPart ptNew = new MSHOutPart();
    ptNew.setSource(ZKPConstants.ZKP_PLUGIN_TYPE);
    ptNew.setEncoding(SEDValues.ENCODING_UTF8);
    ptNew.setMimeType(partType.getMimeType());
    ptNew.setName(partType.getPartName());
    ptNew.setType(partType.getPartType());
    ptNew.setDescription(partType.getDescription(null));

    ptNew.setFilepath(StorageUtils.getRelativePath(fEncryptedKey));
    ptNew.setFilename(String.format("%s.%s", partType.getPartName(),
             partType.getFileSuffix()));

    ptNew.setIsEncrypted(Boolean.FALSE);

    addOrUpdatePartProperty(ptNew, ZKPPartPropertyType.PartCreated.getType(),
            LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

    LOG.logEnd(l, receiverBox, convId);
    return ptNew;
  }

  /**
   *
   * @param outMail
   * @param partType
   * @param ft
   * @param pk
   * @param xcert
   * @return
   * @throws SEDSecurityException
   * @throws StorageException
   * @throws HashException
   * @throws FOPException
   */
  public MSHOutPart createMSHOutPart(MSHMailType outMail, ZKPPartType partType,
          FopTransformation ft, PrivateKey pk, X509Certificate xcert) {
    long l = LOG.logStart();

    // create visualization
    File fDNViz;
    String relativePath;
    try {
      fDNViz = StorageUtils.getNewStorageFile(partType.getFileSuffix(),
              partType.name() + "-");
      relativePath = StorageUtils.getRelativePath(fDNViz);
    } catch (StorageException ex) {
      String msg = String.format(
              "Server error occured while creating file visualization %s  for out mail: %d, Error: %s.",
              ft.name(), outMail.getId(), ex.getMessage());
      LOG.logError(l, msg, ex);
      throw new EBMSError(ZKPErrorCode.ServerError, outMail.getRefToMessageId(),
              msg, SoapFault.FAULT_CODE_SERVER);
    }

    try {
      getFOP().generateVisualization(outMail, fDNViz,
              ft,
              partType.getMimeType());
    } catch (FOPException ex) {
      String msg = String.format(
              "Server error occured while generationg visualization %s  for out mail: %d, Error: %s.",
              ft.name(), outMail.getId(), ex.getMessage());
      LOG.logError(l, msg, ex);
      throw new EBMSError(ZKPErrorCode.ServerError, outMail.getRefToMessageId(),
              msg, SoapFault.FAULT_CODE_SERVER);
    }

    signPDFDocument(pk, xcert, fDNViz, true);

    MSHOutPart ptNew = new MSHOutPart();
    ptNew.setSource(ZKPConstants.ZKP_PLUGIN_TYPE);
    ptNew.setEncoding(SEDValues.ENCODING_BASE64);
    ptNew.setIsSent(Boolean.TRUE);
    ptNew.setIsReceived(Boolean.FALSE);
    ptNew.setMimeType(partType.getMimeType());
    ptNew.setName(partType.getPartName());
    ptNew.setType(partType.getPartType());
    ptNew.setDescription(partType.getDescription(null));
    ptNew.setFilepath(relativePath);
    ptNew.setFilename(String.format("%s.%s", partType.getPartName(),
            partType.getFileSuffix()));

    ptNew.setIsEncrypted(Boolean.FALSE);

    addOrUpdatePartProperty(ptNew, ZKPPartPropertyType.PartCreated.getType(),
            LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

    LOG.logEnd(l, String.format(
            "Generated SignedDeliveryNotification part (new part id: %s) for mail: %d'",
            ptNew.getEbmsId(), outMail.getId()));

    return ptNew;
  }

  public MSHInPart createMSHInPart(MSHMailType outMail, ZKPPartType partType,
          FopTransformation ft, PrivateKey pk, X509Certificate xcert)
          throws SEDSecurityException, StorageException, HashException, FOPException {
    long l = LOG.logStart();

    // create nofitication
    File fDNViz
            = StorageUtils.getNewStorageFile(partType.getFileSuffix(),
                    partType.name() + "-");

    LOG.logJAXBObject("MSH IN PART GENERATION FROM OBJECT: ", outMail);

    getFOP().generateVisualization(outMail, fDNViz,
            ft,
            partType.getMimeType());

    signPDFDocument(pk, xcert, fDNViz, true);

    MSHInPart ptNew = new MSHInPart();
    ptNew.setEbmsId(Utils.getUUIDWithLocalDomain());
    ptNew.setSource(ZKPConstants.ZKP_PLUGIN_TYPE);
    ptNew.setEncoding(SEDValues.ENCODING_BASE64);
    ptNew.setIsSent(Boolean.FALSE);
    ptNew.setIsReceived(Boolean.FALSE);
    ptNew.setMimeType(partType.getMimeType());
    ptNew.setName(partType.getPartName());
    ptNew.setType(partType.getPartType());
    ptNew.setDescription(partType.getDescription(null));
    ptNew.setFilepath(StorageUtils.getRelativePath(fDNViz));
    ptNew.setFilename(String.format("%s.%s", partType.getPartName(),
            partType.getFileSuffix()));

    ptNew.setIsEncrypted(Boolean.FALSE);

    addOrUpdatePartProperty(ptNew, ZKPPartPropertyType.PartCreated.getType(),
            LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

    LOG.logEnd(l, String.format(
            "Generated SignedDeliveryNotification part (new part id: %s) for mail: %d'",
            ptNew.getEbmsId(), outMail.getId()));
    ;
    return ptNew;
  }

  public void updateMSHOutPartVisualization(MSHOutMail outMail,
          MSHOutPart outPart, ZKPPartType partType,
          FopTransformation ft, PrivateKey pk, X509Certificate xcert)
          throws SEDSecurityException, StorageException, HashException, FOPException {
    long l = LOG.logStart();

    // create nofitication
    File fDNViz = StorageUtils.getFile(outPart.getFilepath());
    // delete old file
    fDNViz.delete();
    // create new vizualization
    getFOP().generateVisualization(outMail, fDNViz,
            ft,
            partType.getMimeType());
    signPDFDocument(pk, xcert, fDNViz, true);

    addOrUpdatePartProperty(outPart, ZKPPartPropertyType.PartCreated.getType(),
            LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
    LOG.logEnd(l, String.format(
            "updated SignedDeliveryNotification part (new part id: %s) for mail: %d'",
            outPart.getEbmsId(), outMail.getId()));

  }

  /**
   *
   * @return
   */
  private FOPUtils getFOP() {
    if (mfpFop == null) {
      File fconf
              = new File(SEDSystemProperties.getPluginsFolder(),
                      ZKPConstants.SVEV_FOLDER + File.separator + ZKPConstants.FOP_CONFIG_FILENAME);

      mfpFop
              = new FOPUtils(fconf, SEDSystemProperties.getPluginsFolder().
                      getAbsolutePath()
                      + File.separator + ZKPConstants.SVEV_FOLDER + File.separator
                      + ZKPConstants.XSLT_FOLDER);
    }
    return mfpFop;
  }

  private File signPDFDocument(PrivateKey pk, X509Certificate xcert, File f,
          boolean replace) {
    long l = LOG.logStart();
    File ftmp = null;
    try {
      ftmp = StorageUtils.getNewStorageFile("pdf", "zkp-signed");

      SignUtils su = new SignUtils(pk, xcert);
      su.signPDF(f, ftmp, true);
      if (replace) {
        Files.move(ftmp.toPath(), f.toPath(),
                StandardCopyOption.REPLACE_EXISTING);
        ftmp = f;
      }
    } catch (StorageException | IOException ex) {
      LOG.logError(l, ex);
    }
    return ftmp;
  }

  public String getPartProperty(MSHOutPart op, String propertytype) {
    if (op == null || op.getOMPartProperties().isEmpty()) {
      return null;
    }
    for (OMPartProperty p : op.getOMPartProperties()) {
      if (Objects.equals(p.getName(), propertytype)) {
        return p.getValue();
      }
    }
    return null;
  }

  public void addOrUpdatePartProperty(MSHOutPart op, String propertytype,
          String value) {
    if (op == null) {
      return;
    }
    // !property with null value must not exists
    // check is exits    
    for (OMPartProperty p : op.getOMPartProperties()) {
      if (Objects.equals(p.getName(), propertytype)) {
        if (Utils.isEmptyString(value)) {
          op.getOMPartProperties().remove(p);
          return;
        } else {
          p.setValue(value);
          return;
        }
      }
    }
    // and new property
    if (!Utils.isEmptyString(value)) {
      OMPartProperty prp = new OMPartProperty();
      prp.setName(propertytype);
      prp.setValue(value);
      op.getOMPartProperties().add(prp);
    }
  }

  public void addOrUpdatePartProperty(MSHInPart op, String propertytype,
          String value) {
    if (op == null) {
      return;
    }
    // !property with null value must not exists
    // check is exits    
    for (IMPartProperty p : op.getIMPartProperties()) {
      if (Objects.equals(p.getName(), propertytype)) {
        if (Utils.isEmptyString(value)) {
          op.getIMPartProperties().remove(p);
          return;
        } else {
          p.setValue(value);
          return;
        }
      }
    }
    // and new property
    if (!Utils.isEmptyString(value)) {
      IMPartProperty prp = new IMPartProperty();
      prp.setName(propertytype);
      prp.setValue(value);
      op.getIMPartProperties().add(prp);
    }
  }
}
