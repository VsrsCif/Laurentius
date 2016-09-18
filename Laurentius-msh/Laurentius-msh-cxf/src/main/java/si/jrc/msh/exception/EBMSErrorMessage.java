/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.jrc.msh.exception;

/**
 *
 * @author Jože Rihtaršič
 */
public class EBMSErrorMessage {
  public static final String INVALID_SOAP_VERSION = "EBMS AS4 supports only soap 1.2 protocol!";  
  public static final String INVALID_HEADER_DATA="Invalid header data. ";
  public static final String INVALID_HEADER_MISSING_MESSAGING = "Missing SOAP header or Messaging header";
  public static final String INVALID_HEADER_ERROR_PARSING_MESSAGING = "Error reading EMBS header: ";
  public static final String INVALID_HEADER_MESSAGING = "Header is not valid by schema: ";
  public static final String INVALID_HEADER_MULTIPLE_MESSAGING 
      = "Only one EBMS header Messaging is exptected: ";
  public static final String INVALID_HEADER_USER_MESSAGE_COUNT 
      = "Zero or one UserMessage is exptected!: ";
  public static final String INVALID_HEADER_MESSAGING_EMPTY 
      = "UserMessage or SignalMessages are exptected!";
 public static final String  APP_ERR_MISSING_PMODE ="Missing PMode in message context for inbound " +
     "response message! Outbound channel must set pmode in to the context!"; 
 public static final String  INVALID_AGR_REF_URI ="Agreement reference for 'null' type MUST be a URI";
 public static final String  CONF_ERROR_PMODE="Error occured while readingPMode.";
 

}


