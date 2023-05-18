/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.jrc.msh.plugin.zpp;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public class ZPPConstants {

  private ZPPConstants() {
  }

  
  /**
     *
     */
  public static final String ELM_SIGNAL_ENCRYPTED_KEY = "EncryptedKey";

  
  /**
     *
     */
  public static final String FOP_CONFIG_FILENAME = "fop.xconf";

  /**
     *
     */
  public static final String MSG_DELIVERY_NOTIFICATION_DESC = "Obvestilo o prispeli pošiljki";

  /**
     *
     */
  public static final String MSG_DELIVERY_NOTIFICATION_FILENAME = "ZPPObvestilo";

  /**
     *
     */
  public static final String MSG_DOC_PREFIX_DESC = "Sifriran dok.:";

  /**
     *
     */
  public static final  String SVEV_FOLDER = "SVEV";

  /**
     *
     */
  public static final String S_ZPP_ACTION_ADVICE_OF_DELIVERY = "AdviceOfDelivery";
  public static final String S_ZPP_ACTION_DELIVERY_NOTIFICATION = "DeliveryNotification";
  public static final String S_ZPP_ACTION_FICTION_NOTIFICATION = "FictionNotification";
  public static final String S_ZPP_ACTION_ADVICE_OF_DELIVERY_FICTION = "AdviceOfDeliveryFiction";
  //public static final String S_ZPP_ACTION_DELIVERY_RECIEPT = "DeliveryReciept";
  public static final String S_ZPP_ACTION_ADDRESS_NOT_EXISTS = "ReceiverAddressNotExists";
  

  /**
     *
     */
  public static final String S_ZPP_ENC_SUFFIX = ".zpp.enc";
  
  public static final String S_PART_PROPERTY_ORIGIN_MIMETYPE = "OriginMimeType";
  public static final String S_PART_PROPERTY_REF_ID = "REF_ID";
  
  public static final String S_MAIL_PROPERTY_ORIGINAL_SENDER = "originalSender";
  public static final String S_MAIL_PROPERTY_FINAL_RECIPIENT = "finalRecipient";
  

  
  

  /**
     *
     */
  public static final String S_ZPP_PLUGIN_TYPE = "LegalZPP";
  

  
  

  /**
   * LegalDelivery_ZPP -> osebno -> preveri
   * LegalDelivery_ZPPB -> navadno -> preveri
     *
     */
  public static final String S_ZPP_SERVICE = "LegalDelivery_ZPP";
  public static final String S_ZPPB_SERVICE = "LegalDelivery_ZPPB";

  /**
   * B variant ZZPja
   *
   */

  public static final String S_ZPP_B_SERVICE = "LegalDelivery_ZPP_B";
  public static final String S_ZPPB_B_SERVICE = "LegalDelivery_ZPPB_B";

  /**
     *
     */
  public static final String XSLT_FOLDER = "xslt";
}
