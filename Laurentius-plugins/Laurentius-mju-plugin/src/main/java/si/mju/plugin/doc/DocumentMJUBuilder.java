/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package si.mju.plugin.doc;

import java.math.BigDecimal;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.xml.bind.JAXBException;
import si.gov.nio.cev._2015.document.ContentType;
import si.gov.nio.cev._2015.document.DataObjectFormatType;
import si.gov.nio.cev._2015.document.DataType;
import si.gov.nio.cev._2015.document.Document;
import si.gov.nio.cev._2015.document.EmbeddedDataType;
import si.gov.nio.cev._2015.document.EmbeddedMsgDataType;
import si.gov.nio.cev._2015.document.SignaturesType;
import si.gov.nio.cev._2015.document.VisualisationType;
import si.gov.nio.cev._2015.document.VisualisationsType;
import si.gov.nio.cev._2015.message.AddressType;
import si.gov.nio.cev._2015.message.AddressesType;
import si.gov.nio.cev._2015.message.Message;
import si.gov.nio.cev._2015.message.PhysicalAddressType;
import si.laurentius.commons.enums.MimeValue;
import si.laurentius.commons.exception.SEDSecurityException;
import si.laurentius.commons.exception.StorageException;
import si.laurentius.commons.utils.Utils;
import si.laurentius.commons.utils.xml.XMLUtils;
import si.laurentius.msh.mail.MSHMailType;
import si.laurentius.msh.mail.MSHPartType;

/**
 *
 * @author Joze Rihtarsic
 */
public class DocumentMJUBuilder extends DocumentBuilder {

  private static final BigDecimal SCHEMA_VERSION = BigDecimal.valueOf(1.0);
  
  /**
   *
   * @param dce
   * @param lstPars
   * @param fos
   * @param key
   * @throws SEDSecurityException
   */
  @Override
  public Document createMail(MSHMailType dce, List<MSHPartType> lstPars, 
          KeyStore.PrivateKeyEntry key)
          throws SEDSecurityException {
     Document document = null;
    try {
      long t = getTime();
      mlgLogger.debug("DocumentBuilder.createMail: begin ");
      /*
       * - Message/To - PoBoxId - Message/From - PoBoxId - Message/Subject - Message/MessageId -
       * Message/SenderDocumentId - Message/DeliveryType ki je vedno Legal-ZPP2 - Message/Content
       */
      // String strNotfMail = Settings.getInstance().getNotificationMail();
      // send document
      document = new Document();
      document.setSchemaVersion(SCHEMA_VERSION);
      List<String> lstSignatureIDS = new ArrayList<String>();
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
      mt.setMessageId(dce.getConversationId()); 
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

      EmbeddedMsgDataType emb = new EmbeddedMsgDataType();
      emb.getMessages().add(mt);

      dt.setDataFormat(doft);
      dt.setContent(emb);
      dt.setDescription(""); // obvezen element
      // --------------------- end create message ----------------------
      // --------------------- end create visualization ----------------------
      VisualisationsType lstVst = new VisualisationsType();

      

      for (MSHPartType d : lstPars) {
        VisualisationType vst = new VisualisationType();
        vst.setDescription(d.getDescription());
        lstVst.getVisualisations().add(vst);
        
        vst.setId(Utils.getInstance().getGuidString());
        lstSignatureIDS.add(vst.getId());
        doft = new DataObjectFormatType();
        doft.setIdentifier(d.getId() + "");
        doft.setMimeType(d.getMimeType() == null ? MimeValue.MIME_BIN.getMimeType() : d.
                getMimeType());
        doft.setEncoding(ENC_TYPE_B64);
        
        
        ContentType ctVis = new ContentType();
        EmbeddedDataType edt = new EmbeddedDataType();
        edt.getContent().add(
                Base64.getEncoder().encodeToString(msuStorageUtils.getByteArray(
                        d.getFilepath())));
        ctVis.setEmbeddedData(edt);
        
        
        vst.setDataFormat(doft);
        vst.setContent(ctVis);
        document.setData(dt);
        document.setVisualisations(lstVst);
      }
      // --------------------- sign data ----------------------
      document.setSignatures(new SignaturesType());

      // convert to w3c document
      org.w3c.dom.Document dw3c
              = convertEpDoc2W3cDoc(document,
                      new Class[]{Document.class, Message.class});

      // sign document and return value
      if (key!= null) {
        signDocument(dw3c, lstSignatureIDS, key);
      }
    
      document =(Document) XMLUtils.deserialize(dw3c.getDocumentElement(), Document.class);
      mlgLogger.info(
              "DocumentBuilder.DocumentBuilder: - end (" + (getTime() - t) + "ms)");
    } catch (JAXBException| StorageException ex) {
      String strMsg = "DocumentSodBuilder.createMail: error reading file'" + ex.
              getMessage() + "'.";
      mlgLogger.error(strMsg, ex);
      throw new SEDSecurityException(
              SEDSecurityException.SEDSecurityExceptionCode.CreateSignatureException,
              ex);
    } 
    return document;
  }
  
  
  

}
