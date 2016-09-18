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
package si.laurentius.commons.cxf;

import java.io.File;
import java.net.URI;
import java.util.List;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import org.apache.cxf.message.Message;
import org.w3c.dom.Element;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.ebox.SEDBox;
import org.w3c.dom.NodeList;
import si.laurentius.commons.pmode.EBMSMessageContext;
import si.laurentius.commons.utils.SEDLogger;

/**
 *
 * @author Jože Rihtaršič
 */
public class SoapUtils {
  
  public static final String WSSE_LN = "Security";
  public static final String WSSE_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
  public static final String WSSE11_NS = "http://docs.oasis-open.org/wss/oasis-wss-wssecurity-secext-1.1.xsd";
  
   static final SEDLogger LOG = new SEDLogger(SoapUtils.class);

  public static boolean isRequestMessage(Message message) {
    Boolean requestor = (Boolean) message.get(Message.REQUESTOR_ROLE);
    return requestor != null && requestor;
  }

  public static String getInLogFilePath(Message message) {
    File f = (File) message.getExchange().get(EBMSConstants.EBMS_CP_IN_LOG_SOAP_MESSAGE_FILE);
    return f != null ? f.getAbsolutePath() : "";
  }

  public static MSHOutMail getMSHOutMail(Message message) {
    return (MSHOutMail) message.getExchange().get(EBMSConstants.EBMS_CP_OUTMAIL);
  }

  public static void setMSHOutnMail(MSHOutMail omail, Message message) {
    message.getExchange().put(EBMSConstants.EBMS_CP_OUTMAIL, omail);
  }

  public static void setMSHOutnMail(MSHOutMail omail, javax.xml.ws.Dispatch client) {
    client.getRequestContext().put(EBMSConstants.EBMS_CP_OUTMAIL, omail);
  }

  public static MSHInMail getMSHInMail(Message message) {
    return (MSHInMail) message.getExchange().get(EBMSConstants.EBMS_CP_INMAIL);
  }

  public static void setMSHInMail(MSHInMail imail, Message message) {
    message.getExchange().put(EBMSConstants.EBMS_CP_INMAIL, imail);
  }
  
   public static SEDBox getMSHInMailReceiverBox(Message message) {
    return (SEDBox) message.getExchange().get(EBMSConstants.EBMS_CP_INMAIL_RECEIVER);
  }

  public static void setMSHInMailReceiverBox(SEDBox imail, Message message) {
    message.getExchange().put(EBMSConstants.EBMS_CP_INMAIL_RECEIVER, imail);
  }
  
   public static void setInSignals(List<Element> lst, Message message) {
    message.getExchange().put(EBMSConstants.EBMS_SIGNAL_ELEMENTS, lst);
  }
  
  
  public static List<Element> getInSignals(Message message) {
    return (List<Element> ) message.getExchange().get(EBMSConstants.EBMS_SIGNAL_ELEMENTS);
  }
  
 public static EBMSMessageContext getEBMSMessageOutContext(Message message) {
    return (EBMSMessageContext) message.getExchange().get(EBMSConstants.EBMS_CP_OUT_CONTEXT);
  }
  
  
  public static EBMSMessageContext getEBMSMessageInContext(Message message) {
    return (EBMSMessageContext) message.getExchange().get(EBMSConstants.EBMS_CP_IN_CONTEXT);
  }

  public static void setEBMSMessageOutContext(EBMSMessageContext emc, Message message) {
    message.getExchange().put(EBMSConstants.EBMS_CP_OUT_CONTEXT, emc);
  }
  public static void setEBMSMessageInContext(EBMSMessageContext emc, Message message) {
    message.getExchange().put(EBMSConstants.EBMS_CP_IN_CONTEXT, emc);
  }

  public static void setEBMSMessageOutContext(EBMSMessageContext emc, javax.xml.ws.Dispatch client) {
    client.getRequestContext().put(EBMSConstants.EBMS_CP_OUT_CONTEXT, emc);
  }

  public static boolean isValidURI(final String validateUri) {
    if (validateUri == null) {
      return false;
    }
    try {
      final URI uri = new URI(validateUri.trim());
      return true;
    } catch (Exception e1) {
      LOG.formatedWarning("String %s is not valid URI. Error: %s",validateUri, e1.getMessage());
      return false;
    }
  }
  
  
  public static boolean isSoapFault(SOAPMessage sm){  
    
    try {
      return sm.getSOAPPart().getEnvelope().getBody().hasFault();
    } catch (SOAPException ex) {
      LOG.formatedWarning("Error checking SOAPMessage type. Error: %s", ex.getMessage());
      return false;
    }
  }

  public static boolean hasSecurity(SOAPMessage request){

    try {    
      if (request.getSOAPHeader() == null) {
        return false;
      }

      NodeList lstND =
          request.getSOAPHeader().getElementsByTagNameNS(WSSE_NS,WSSE_LN);
       NodeList lstND1 =
          request.getSOAPHeader().getElementsByTagNameNS(WSSE11_NS,WSSE_LN);
      return lstND != null && lstND.getLength() > 0 || lstND1 != null && lstND1.getLength() > 0;
    } catch (SOAPException ex) {
      LOG.formatedWarning("Error checking SOAPMessage type. Error: %s", ex.getMessage());
    }
    return false;
    
  }
  
}
