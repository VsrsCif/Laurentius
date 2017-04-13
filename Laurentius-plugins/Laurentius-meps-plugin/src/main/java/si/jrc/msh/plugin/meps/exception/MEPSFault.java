/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.jrc.msh.plugin.meps.exception;

import javax.xml.namespace.QName;
import org.apache.cxf.binding.soap.SoapFault;

/**
 *
 * @author Jože Rihtaršič
 */
public class MEPSFault extends SoapFault {

  MEPSFaultCode ebmsErrorCode;
  String refToMessage;
  String subMessage;

  /**
   *
   * @param ec
   * @param refToMsg
   * @param faultCode
   */
  public MEPSFault(MEPSFaultCode ec, String refToMsg, QName faultCode) {
    super(ec.name, faultCode);
    
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
  public MEPSFault(MEPSFaultCode ec, String refToMsg, String message, QName faultCode) {
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
  public MEPSFault(MEPSFaultCode ec, String refToMsg, String message, Throwable cause, QName faultCode) {
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
  public MEPSFault(MEPSFaultCode ec, String refToMsg, Throwable cause, QName faultCode) {
    super(ec.name, cause, faultCode);
    ebmsErrorCode = ec;
    refToMessage = refToMsg;

  }

  /**
   *
   * @return
   */
  public MEPSFaultCode getEbmsErrorCode() {
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
