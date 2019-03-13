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

import java.util.Calendar;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
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
import org.apache.cxf.headers.Header;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.SignalMessage;
import org.w3c.dom.Node;
import si.laurentius.commons.ebms.EBMSError;
import si.jrc.msh.utils.EBMSBuilder;
import si.laurentius.commons.SEDSystemProperties;
import si.laurentius.commons.cxf.SoapUtils;
import si.laurentius.commons.pmode.EBMSMessageContext;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.xml.XMLUtils;

/**
 * Sets up the outgoing chain to build a ebms 3.0 (AS4) form message. First it
 * will create Messaging object according pmode configuratin added as
 * "PMode.class" param in message context. For user message attachments are
 * added (and compressed according to pmode settings ) In the end encryption and
 * security interceptors are configured.
 *
 * @author Jože Rihtaršič
 */
public class EBMSOutFaultInterceptor extends AbstractEBMSInterceptor {

    /**
     * Logger for EBMSOutFaultInterceptor class
     */
    protected static final SEDLogger LOG = new SEDLogger(
            EBMSOutFaultInterceptor.class);
    /**
     * ebms message tools for converting between ebms and Laurentius message
     * entity
     */
    protected final EBMSBuilder mEBMSUtil = new EBMSBuilder();

    private boolean handleMessageCalled;

    /**
     * Contstructor EBMSOutFaultInterceptor for setting instance in a phase
     * Phase.PRE_PROTOCOL
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
        assert ex != null : "Exception is expected as message content";
        assert ex instanceof Fault : "Fault type if exception is expected as message content";

        Messaging msgHeader = generateMessaging(ex, sv);
        if (msgHeader != null) {
            Header header;
            try {
                header = new Header(new QName("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/", "Messaging"), XMLUtils.jaxbToDocument(msgHeader).getDocumentElement());
                message.getHeaders().add(header);
            } catch (JAXBException | ParserConfigurationException ex1) {
                LOG.logError(0, ex);
            }

        }
        EBMSMessageContext ectx = SoapUtils.getEBMSMessageOutContext(message);
        handleSecurityForOutFaultMessage(message, ectx);
    }

    private void handleSecurityForOutFaultMessage(SoapMessage message,
            EBMSMessageContext ectx) {
        try {

            WSS4JOutInterceptor sc
                    = ectx != null ? configureOutSecurityInterceptors(ectx.getSecurity(), ectx.
                                    getSenderPartyIdentitySet().getLocalPartySecurity(),
                                    ectx.getReceiverPartyIdentitySet().
                                            getExchangePartySecurity(), "",
                                    SoapFault.FAULT_CODE_CLIENT)
                            : null;
            LOG.formatedlog(
                    "Security for soapfault setted! Security: '%s', Sender '%s', receiver: '%s'.",
                    ectx.getSecurity().getId(), ectx.getSenderPartyIdentitySet().
                    getId(),
                    ectx.getReceiverPartyIdentitySet().getId());
            if (sc != null) {
             
                sc.handleMessage(message);
            }
        } catch (EBMSError err) {
            String msg = "Error occured while creating WSS4JOutInterceptor for out Fault! No security will be created for outFault!: Error: " + err.
                    getMessage();
            LOG.logError(msg, err);
        }
    }

    protected boolean handleMessageCalled() {
        return handleMessageCalled;
    }

    
    private Messaging generateMessaging(Exception exc, SoapVersion sv) {
        Messaging msgHeader = null;
        if (exc instanceof EBMSError) {

            EBMSError sf = (EBMSError) exc;
           msgHeader = EBMSBuilder.createMessaging(sv);
        SignalMessage sm
                    = EBMSBuilder.createErrorSignal(sf, Calendar.getInstance()
                            .getTime());
            msgHeader.getSignalMessages().add(sm);

        } else if (exc instanceof Fault) {

            Fault sf = (Fault) exc;
            msgHeader = EBMSBuilder.createMessaging(sv);
            SignalMessage sgnl = EBMSBuilder.createErrorSignal(sf, null, sf.
                    getMessage(),
                    SEDSystemProperties.getLocalDomain(), Calendar.getInstance()
                    .getTime());
            msgHeader.getSignalMessages().add(sgnl);

        }
        return msgHeader;
    }
}
