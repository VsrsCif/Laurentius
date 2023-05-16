/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package si.digics.laurentius.plugin.zkp.doc;

import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.utils.Utils;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.inbox.payload.MSHInPart;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.outbox.payload.MSHOutPart;
import si.sodisce._2010.mail.document.ContentType;
import si.sodisce._2010.mail.document.DataObjectFormatType;
import si.sodisce._2010.mail.document.DataType;
import si.sodisce._2010.mail.document.Document;
import si.sodisce._2010.mail.document.SignaturesType;
import si.sodisce._2010.mail.document.VisualisationType;
import si.sodisce._2010.mail.document.VisualisationsType;
import si.sodisce._2010.mail.message.AddressType;
import si.sodisce._2010.mail.message.AddressesType;
import si.sodisce._2010.mail.message.Message;
import si.sodisce._2010.mail.message.PhysicalAddressType;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 *
 * @author logos
 */
public class DocumentSodBuilder extends DocumentBuilder {

  private static final BigDecimal SCHEMA_VERSION = BigDecimal.valueOf(1.0);
  private static final String NM_DATA = "http://www.sodisce.si/2010/mail/Document/ObjectType/Data";
  private static final String NM_VIS =
      "http://www.sodisce.si/2010/mail/Document/ObjectType/Visualisation";

  /**
   *
   * @param dce
   * @param fos
   * @param key
   * @throws SEDSecurityException
   */
  @Override
  public void createMail(MSHOutMail dce, FileOutputStream fos, KeyStore.PrivateKeyEntry key)
      throws SEDSecurityException {
    try {
      long t = getTime();
      mlgLogger.debug("DocumentBuilder.createMail: begin ");
      /*
       * - Message/To - PoBoxId - Message/From - PoBoxId - Message/Subject - Message/MessageId -
       * Message/SenderDocumentId - Message/DeliveryType ki je vedno Legal-ZPP2 - Message/Content
       */
      // String strNotfMail = Settings.getInstance().getNotificationMail();
      // send document
      Document document = new Document();
      document.setSchemaVersion(SCHEMA_VERSION);
      List<String> lstSignatureIDS = new ArrayList<>();
      // --------------------- create message ----------------------
      Message mt = new Message();
      mt.setSchemaVersion(SCHEMA_VERSION);
      // --------------------- ADDRESSES ----------------------
      AddressesType lstto = new AddressesType();
      // receiver address
      AddressType ato = new AddressType();
      ato.setPoBoxId(dce.getReceiverEBox());
      ato.setPhysicalAddress(new PhysicalAddressType());
      ato.getPhysicalAddress().setName(dce.getReceiverName());
      lstto.getAddresses().add(ato);
      mt.setTo(lstto);
      // sender address
      AddressType aFrom = new AddressType();
      aFrom.setPoBoxId(dce.getSenderEBox());
      aFrom.setPhysicalAddress(new PhysicalAddressType());
      aFrom.getPhysicalAddress().setName(dce.getSenderName());

      mt.setFrom(aFrom);
      mt.setMessageId(dce.getConversationId()); // TODO
      mt.setSenderDocumentId(dce.getConversationId());
      mt.setDeliveryType(DELIVERY_TYPE);
      mt.setDocumentType(DOCUMENT_TYPE);

      mt.setSubject(dce.getSubject());
      DataType dt = new DataType();
      dt.setId(Utils.getInstance().getGuidString());
      lstSignatureIDS.add(dt.getId());
      DataObjectFormatType doft = new DataObjectFormatType();
      doft.setIdentifier(""); // obvezen element

      doft.setMimeType(MIME_TXT); // obvezen element
      doft.setEncoding(ENC_TYPE_UTF8); // obvezen element
      ContentType ct = new ContentType();
      ContentType.EmbeddedData emb = new ContentType.EmbeddedData();
      emb.getContent().add(mt);
      ct.setEmbeddedData(emb);
      dt.setDataFormat(doft);
      dt.setContent(ct);
      dt.setDescription(""); // obvezen element
      // --------------------- end create message ----------------------
      // --------------------- end create visualization ----------------------
      VisualisationsType lstVst = new VisualisationsType();

      List<MSHOutPart> lst = dce.getMSHOutPayload().getMSHOutParts();

      for (MSHOutPart d : lst) {
        VisualisationType vst = new VisualisationType();
        vst.setDescription(d.getDescription());
        lstVst.getVisualisations().add(vst);
        vst.setId(Utils.getInstance().getGuidString());
        lstSignatureIDS.add(vst.getId());
        doft = new DataObjectFormatType();
        doft.setIdentifier(d.getId() + "");
        doft.setMimeType(d.getMimeType() == null ? "application/pdf" : d.getMimeType());
        doft.setEncoding(ENC_TYPE_B64);
        ct = new ContentType();
        emb = new ContentType.EmbeddedData();

        emb.getContent().add(
            Base64.getEncoder().encodeToString(msuStorageUtils.getByteArray(d.getFilepath())));
        ct.setEmbeddedData(emb);
        vst.setDataFormat(doft);
        vst.setContent(ct);
        document.setData(dt);
        document.setVisualisations(lstVst);
      }
      // --------------------- sign data ----------------------
      document.setSignatures(new SignaturesType());

      // convert to w3c document
      org.w3c.dom.Document dw3c =
          convertEpDoc2W3cDoc(document, new Class[] {Document.class, Message.class});

      // sign document and store file to file
      singDocument(dw3c, lstSignatureIDS, fos, key);
      mlgLogger.info("DocumentBuilder.DocumentBuilder: - end (" + (getTime() - t) + "ms)");
    } catch (StorageException ex) {
      String strMsg = "DocumentSodBuilder.createMail: error reading file'" + ex.getMessage() + "'.";
      mlgLogger.error(strMsg, ex);
      throw new SEDSecurityException(
          SEDSecurityException.SEDSecurityExceptionCode.CreateSignatureException, ex);
    } finally {

    }

  }

  
  /**
   *
   * @param dce
   * @param fos
   * @param key
   * @throws SEDSecurityException
   */
  @Override
  public void createMail(MSHInMail dce, FileOutputStream fos, KeyStore.PrivateKeyEntry key)
      throws SEDSecurityException {
    try {
      long t = getTime();
      mlgLogger.debug("DocumentBuilder.createMail: begin ");
      /*
       * - Message/To - PoBoxId - Message/From - PoBoxId - Message/Subject - Message/MessageId -
       * Message/SenderDocumentId - Message/DeliveryType ki je vedno Legal-ZPP2 - Message/Content
       */
      // String strNotfMail = Settings.getInstance().getNotificationMail();
      // send document
      Document document = new Document();
      document.setSchemaVersion(SCHEMA_VERSION);
      List<String> lstSignatureIDS = new ArrayList<>();
      // --------------------- create message ----------------------
      Message mt = new Message();
      mt.setSchemaVersion(SCHEMA_VERSION);
      // --------------------- ADDRESSES ----------------------
      AddressesType lstto = new AddressesType();
      // receiver address
      AddressType ato = new AddressType();
      ato.setPoBoxId(dce.getReceiverEBox());
      ato.setPhysicalAddress(new PhysicalAddressType());
      ato.getPhysicalAddress().setName(dce.getReceiverName());
      lstto.getAddresses().add(ato);
      mt.setTo(lstto);
      // sender address
      AddressType aFrom = new AddressType();
      aFrom.setPoBoxId(dce.getSenderEBox());
      aFrom.setPhysicalAddress(new PhysicalAddressType());
      aFrom.getPhysicalAddress().setName(dce.getSenderName());

      mt.setFrom(aFrom);
      mt.setMessageId(dce.getConversationId()); // TODO
      mt.setSenderDocumentId(dce.getConversationId());
      mt.setDeliveryType(DELIVERY_TYPE);
      mt.setDocumentType(DOCUMENT_TYPE);

      mt.setSubject(dce.getSubject());
      DataType dt = new DataType();
      dt.setId(Utils.getInstance().getGuidString());
      lstSignatureIDS.add(dt.getId());
      DataObjectFormatType doft = new DataObjectFormatType();
      doft.setIdentifier(""); // obvezen element

      doft.setMimeType(MIME_TXT); // obvezen element
      doft.setEncoding(ENC_TYPE_UTF8); // obvezen element
      ContentType ct = new ContentType();
      ContentType.EmbeddedData emb = new ContentType.EmbeddedData();
      emb.getContent().add(mt);
      ct.setEmbeddedData(emb);
      dt.setDataFormat(doft);
      dt.setContent(ct);
      dt.setDescription(""); // obvezen element
      // --------------------- end create message ----------------------
      // --------------------- end create visualization ----------------------
      VisualisationsType lstVst = new VisualisationsType();

      List<MSHInPart> lst = dce.getMSHInPayload().getMSHInParts();

      for (MSHInPart d : lst) {
        VisualisationType vst = new VisualisationType();
        vst.setDescription(d.getDescription());
        lstVst.getVisualisations().add(vst);
        vst.setId(Utils.getInstance().getGuidString());
        lstSignatureIDS.add(vst.getId());
        doft = new DataObjectFormatType();
        doft.setIdentifier(d.getId() + "");
        doft.setMimeType(d.getMimeType() == null ? "application/pdf" : d.getMimeType());
        doft.setEncoding(ENC_TYPE_B64);
        ct = new ContentType();
        emb = new ContentType.EmbeddedData();

        emb.getContent().add(
            Base64.getEncoder().encodeToString(msuStorageUtils.getByteArray(d.getFilepath())));
        ct.setEmbeddedData(emb);
        vst.setDataFormat(doft);
        vst.setContent(ct);
        document.setData(dt);
        document.setVisualisations(lstVst);
      }
      // --------------------- sign data ----------------------
      document.setSignatures(new SignaturesType());

      // convert to w3c document
      org.w3c.dom.Document dw3c =
          convertEpDoc2W3cDoc(document, new Class[] {Document.class, Message.class});

      // sign document and store file to file
      singDocument(dw3c, lstSignatureIDS, fos, key);
      mlgLogger.info("DocumentBuilder.DocumentBuilder: - end (" + (getTime() - t) + "ms)");
    } catch (StorageException ex) {
      String strMsg = "DocumentSodBuilder.createMail: error reading file'" + ex.getMessage() + "'.";
      mlgLogger.error(strMsg, ex);
      throw new SEDSecurityException(
          SEDSecurityException.SEDSecurityExceptionCode.CreateSignatureException, ex);
    } finally {

    }

  }
}
