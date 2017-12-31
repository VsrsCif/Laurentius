/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.laurentius.commons.ebms;

import javax.xml.namespace.QName;
import org.apache.cxf.binding.soap.SoapFault;

/**
 *
 * @author Jože Rihtaršič
 */
public class EBMSError extends SoapFault {

  EBMSErrorCodeInterface ebmsErrorCode;
  String refToMessage;
  String subMessage;

  /**
   *
   * @param ec
   * @param refToMsg
   * @param faultCode
   */
  public EBMSError(EBMSErrorCodeInterface ec, String refToMsg, QName faultCode) {
    super(ec.getName(), faultCode);
    
    ebmsErrorCode = ec;
    refToMessage = refToMsg;
  }

  /**
   *
   * @param ec
   * @param refToMsg
   * @param message
   * @param faultCode
   */
  public EBMSError(EBMSErrorCodeInterface ec, String refToMsg, String message, QName faultCode) {
    super(message,  faultCode);
    ebmsErrorCode = ec;
    refToMessage = refToMsg;
    subMessage = message;

  }

  /**
   *
   * @param ec
   * @param refToMsg
   * @param message
   * @param cause
   * @param faultCode
   */
  public EBMSError(EBMSErrorCodeInterface ec, String refToMsg, String message, Throwable cause, QName faultCode) {
    super(message, cause, faultCode);
    ebmsErrorCode = ec;
    refToMessage = refToMsg;
    subMessage = message;
  }

  /**
   *
   * @param ec
   * @param refToMsg
   * @param cause
   * @param faultCode
   */
  public EBMSError(EBMSErrorCodeInterface ec, String refToMsg, Throwable cause, QName faultCode) {
    super(ec.getName(), cause, faultCode);
    ebmsErrorCode = ec;
    refToMessage = refToMsg;

  }

  /**
   *
   * @return
   */
  public EBMSErrorCodeInterface getEbmsErrorCode() {
    return ebmsErrorCode;
  }

  /**
   *
   * @return
   */
  public String getRefToMessage() {
    return refToMessage;
  }

  /**
   *
   * @return
   */
  public String getSubMessage() {
    return subMessage;
  }

}
