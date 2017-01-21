/*
 * Copyright 2016, Supreme Court Republic of Slovenia
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
package si.jrc.msh.interceptor;

import java.io.IOException;
import java.util.Calendar;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.SoapVersion;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.SignalMessage;
import org.w3c.dom.Node;
import si.jrc.msh.exception.EBMSError;
import si.jrc.msh.utils.EBMSBuilder;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.cxf.SoapUtils;
import si.laurentius.commons.pmode.EBMSMessageContext;
import si.laurentius.commons.utils.SEDLogger;

/**
 * Sets up the outgoing chain to build a ebms 3.0 (AS4) form message. First it will create Messaging
 * object according pmode configuratin added as "PMode.class" param in message context. For user
 * message attachments are added (and compressed according to pmode settings ) In the end encryption
 * and security interceptors are configured.
 *
 * @author Jože Rihtaršič
 */
public class EBMSOutFaultInterceptor extends AbstractEBMSInterceptor {

  /**
   * Logger for EBMSOutFaultInterceptor class
   */
  protected final static SEDLogger LOG = new SEDLogger(EBMSOutFaultInterceptor.class);
  /**
   * ebms message tools for converting between ebms and Laurentius message entity
   */
  protected final EBMSBuilder mEBMSUtil = new EBMSBuilder();

  private boolean handleMessageCalled;

  /**
   * Contstructor EBMSOutFaultInterceptor for setting instance in a phase Phase.PRE_PROTOCOL
   */
  public EBMSOutFaultInterceptor() {
    super(Phase.PRE_PROTOCOL);
    getAfter().add(EBMSOutInterceptor.class.getName());
  }

  @Override
  public void handleMessage(SoapMessage message)
      throws Fault {
    

    
    SoapVersion sv = message.getVersion();
    handleMessageCalled = true;

    Exception ex = message.getContent(Exception.class);
    if (ex == null) {
      throw new RuntimeException("Exception is expected");
    }
    if (!(ex instanceof Fault)) {
      throw new RuntimeException("Fault is expected");
    }
    SOAPMessage sm = createSoapFault(ex, sv);
    message.setContent(SOAPMessage.class, sm);
    message.removeContent(Exception.class);
    
    EBMSMessageContext ectx = SoapUtils.getEBMSMessageOutContext(message);
    if (ectx!= null ) {
       WSS4JOutInterceptor sc =
      configureOutSecurityInterceptors(ectx.getSecurity(), ectx.getSenderPartyIdentitySet().getLocalPartySecurity(),
              ectx.getReceiverPartyIdentitySet().getExchangePartySecurity(), "",
              SoapFault.FAULT_CODE_CLIENT);
       LOG.formatedlog("Security for soapfault setted! Security: '%s', Sender '%s', receiver: '%s'.", ectx.getSecurity().getId(), ectx.getSenderPartyIdentitySet().getId(),
              ectx.getReceiverPartyIdentitySet().getId());
        sc.handleMessage(message);
    }
        
    
    // deal with the actual exception : fault.getCause()
    HttpServletResponse response = (HttpServletResponse) message.getExchange()
        .getInMessage().get(AbstractHTTPDestination.HTTP_RESPONSE);
    response.setStatus(500);
    response.setContentType("application/soap+xml");

    try {
      sm.writeTo(response.getOutputStream());

    
      response.getOutputStream().flush();
      message.getInterceptorChain().abort();
    } catch (IOException ioex) {
      throw new RuntimeException("Error writing the response");
    } catch (SOAPException ex1) {
      throw new RuntimeException("Error writing the response");
    }

  }

  protected boolean handleMessageCalled() {
    return handleMessageCalled;
  }

  private SOAPMessage createSoapFault(Exception exc, SoapVersion sv) {
    long l = LOG.logStart();
    SOAPMessage request = null;
    try {
      MessageFactory mf = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
      request = mf.createMessage();

      SOAPBody body = request.getSOAPBody();
      SOAPFault soapFault = body.addFault();

      Messaging msgHeader = null;
      if (exc instanceof EBMSError) {

        EBMSError sf = (EBMSError) exc;

        soapFault.setFaultString(((EBMSError) exc).getSubMessage());
        soapFault.setFaultCode(SOAPConstants.SOAP_SENDER_FAULT);

        msgHeader = EBMSBuilder.createMessaging(sv);

        SignalMessage sm =
            EBMSBuilder.createErrorSignal(sf,  Calendar.getInstance()
                .getTime());

        msgHeader.getSignalMessages().add(sm);

      } else if (exc instanceof Fault) {

        Fault sf = (Fault) exc;

        soapFault.setFaultString(sf.getMessage());
        soapFault.setFaultCode(SOAPConstants.SOAP_RECEIVER_FAULT);

        msgHeader = EBMSBuilder.createMessaging(sv);
        SignalMessage sgnl = EBMSBuilder.createErrorSignal(sf, null, sf.getMessage(),
                SEDSystemProperties.getLocalDomain(), Calendar.getInstance()
            .getTime());
        msgHeader.getSignalMessages().add(sgnl);

        if (sf.hasDetails()) {
          try {
            soapFault.addDetail();

            Node nd = request.getSOAPPart().importNode(sf.getDetail(), true);
            nd = nd.getFirstChild();
            while (nd != null) {
              soapFault.getDetail().appendChild(nd);
              nd = nd.getNextSibling();
            }
          } catch (SOAPException ex) {
            LOG.logError(l, "Error occured while adding detail to Soap fault. ", ex);
          }
        }
      } else {
        soapFault.setFaultString(exc.getMessage());
        soapFault.setFaultCode(SOAPConstants.SOAP_RECEIVER_FAULT);
      }

      try {
        if (msgHeader != null) {
          SOAPHeader sh = request.getSOAPHeader();
          Marshaller marshaller = JAXBContext.newInstance(Messaging.class).createMarshaller();
          marshaller.marshal(msgHeader, sh);
          request.saveChanges();
        }
      } catch (JAXBException | SOAPException ex) {
        String errMsg = "Error adding ebms header to soap: " + ex.getMessage();
        LOG.logError(l, errMsg, ex);

      }
    } catch (SOAPException e) {
      // do nothing
      e.printStackTrace();
    }
    return request;
  }

}
