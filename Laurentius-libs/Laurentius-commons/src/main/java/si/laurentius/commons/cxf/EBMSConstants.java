/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.laurentius.commons.cxf;

/**
 *
 * @author Jože Rihtaršič
 */
public class EBMSConstants {

  /**
   * Interceptor context parammeter for out mail
   */
  public static final String EBMS_CP_OUTMAIL = "sed.ebms.cp.outmail";
  /**
   * Interceptor context parammeter for in mail
   */
  public static final String EBMS_CP_INMAIL = "sed.ebms.cp.inmail";
  

  /**
   * Interceptor context variable for out context  
   */
  public static final String EBMS_CP_OUT_CONTEXT = "sed.ebms.cp.context.out";
  
  /**
   * Interceptor context variable for sender party identityset 
   */
  public static final String EBMS_CP_IN_CONTEXT = "sed.ebms.cp.context.int";
  
  
  /**
   * Interceptor context parammeter for in mail for receiver box
   */
  public static final String EBMS_CP_INMAIL_RECEIVER = "sed.ebms.cp.inmail.receiver";
  
  
  /**
   *
   */
  public static final String EBMS_CP_BASE_LOG_SOAP_MESSAGE_FILE = "sed.ebms.cp.base.soap.message.file";

  /**
   *
   */
  public static final String EBMS_CP_IN_LOG_SOAP_MESSAGE_FILE = "sed.ebms.cp.incoming.soap.message.file";
  
  
  /**
   *
   */
  public static final String EBMS_SIGNAL_ELEMENTS = "SIGNAL_ELEMENTS";

  /**
   *
   */
  public static final String EBMS_CP_OUT_LOG_SOAP_MESSAGE_FILE =
      "si.jrc.outgoing.soap.message.file";

  /**
   * http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/profiles/AS4-profile/v1.0/AS4-profile-v1.0.html
   * For XML payloads, an eb:PartInfo/eb:PartProperties/eb:Property/@name="CharacterSet" value is
   * RECOMMENDED to identify the character set of the payload before compression was applied. The
   * values of this property MUST conform to the values defined in section 4.3.3 of Extensible
   * Markup Language (XML) 1.0. W3C Recommendation 26 November 2008. http://www.w3.org/TR/REC-xml/
   */
  public static final String EBMS_PAYLOAD_PROPERTY_ENCODING = "CharacterSet";

  /**
   * Filename as defined by sender.
   */
  public static final String EBMS_PAYLOAD_PROPERTY_FILENAME = "Filename";

  /**
   * Is document SVEV ecrypted
   */
  public static final String EBMS_PAYLOAD_PROPERTY_IS_ENCRYPTED = "IsEncrypted";

  /**
   * http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/profiles/AS4-profile/v1.0/AS4-profile-v1.0.html
   * An eb:PartInfo/eb:PartProperties/eb:Property/@name="MimeType" value is REQUIRED to identify the
   * MIME type of the payload before compression was applied.
   */
  public static final String EBMS_PAYLOAD_PROPERTY_MIME = "MimeType";
  // part properties

  /**
   * Custom name or short desc for payload
   */
  public static final String EBMS_PAYLOAD_PROPERTY_NAME = "Name";

  /**
   * Custom type defined by sender and receiver.
   */
  public static final String EBMS_PAYLOAD_PROPERTY_TYPE = "Type";

  /**
   * Payload compression type for AS4.
   */
  public static final String EBMS_PAYLOAD_COMPRESSION_TYPE = "CompressionType";

  /**
   * ebMS Namespace
   */
  public static final String EBMS_NS =
      "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/";


  /**
   * SVEV PartyInfoID Type for address
   */
  public static final String EBMS_PARTY_TYPE_EBOX =
      "urn:oasis:names:tc:ebcore:partyid-type:unregistered:si-svev:sed-box";

  /**
   *SVEV PartyInfoID Type for name
   */
  public static final String EBMS_PARTY_TYPE_NAME =
      "urn:oasis:names:tc:ebcore:partyid-type:unregistered:si-svev:name";

   /**
   *SVEV PartyInfoID Type for name
   */
  public static final String EBMS_LAU_SERVICE_TYPE = "sed:service";
  
  /**
   *
   */
  public static final String EBMS_PROPERTY_DESC = "description";
  
  /**
   *
   */
  public static final String EBMS_PROPERTY_SENDER_MSG_ID = "sender_msg_id";

  /**
   *
   */
  public static final String EBMS_PROPERTY_SUBMIT_DATE = "submitDate";

  /**
   *
   */
  public static final String EBMS_ROOT_ELEMENT_NAME = "Messaging";

  /**
   *
   */
  public static final String FILE_PREFIX_SOAP_MSG = "EBMS-";

  // log soap file prefix/suffix
  /**
   *
   */
  public static final String FILE_SUFFIX_SOAP_MSG_REQUEST = "-Request.xml";

  /**
   *
   */
  public static final String FILE_SUFFIX_SOAP_MSG_RESPONSE = "-Response.xml";
  
  public static final String ATT_CID_PREFIX = "cid:";

  /**
   * When the value of the element is
   * http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/service, then the receiving MSH
   * MUST NOT deliver this message to the Consumer. With the exception of this delivery behavior,
   * and unless indicated otherwise by the eb:Action element, the processing of the message is not
   * different from any other user message.
   */
  public static final String TEST_SERVICE =
      "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/service";
  /**
   * When the value of this element is
   * http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/test, then the eb:Service element
   * MUST have the value http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/service. Such
   * a value for the eb:Action element only indicates that the user message is sent for testing
   * purposes and does not require any specific handling by the MSH.
   */
  public static final String TEST_ACTION =
      "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/test";

}
