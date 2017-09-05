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
package si.jrc.msh.plugin.tc;

import java.util.Properties;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.interceptor.OutgoingChainInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import si.jrc.msh.plugin.tc.utils.DisableServiceUtils;
import si.laurentius.commons.cxf.SoapUtils;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.msh.inbox.mail.MSHInMail;
import si.laurentius.plugin.interceptor.MailInterceptorDef;
import si.laurentius.plugin.interfaces.SoapInterceptorInterface;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
@Stateless
@Local(SoapInterceptorInterface.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class TestCaseInInterceptor implements SoapInterceptorInterface {

  /**
   *
   */
  protected final SEDLogger LOG = new SEDLogger(TestCaseInInterceptor.class);

  public static final String S_INTERCEPTOR_TYPE = "TestCaseInInterceptor";

  @Override
  public MailInterceptorDef getDefinition() {
    MailInterceptorDef mid = new MailInterceptorDef();
    mid.setDescription(S_INTERCEPTOR_TYPE);
    mid.setName(S_INTERCEPTOR_TYPE);
    mid.setType(S_INTERCEPTOR_TYPE);
    return mid;
  }

  /**
   *
   * @param msg
   */
  @Override
  public boolean handleMessage(SoapMessage msg, Properties contextProperties) {
    long l = LOG.logStart();

    boolean isBackChannel = SoapUtils.isRequestMessage(msg);
    MSHInMail im = SoapUtils.getMSHInMail(msg);

    LOG.formatedWarning("got inmail  %s is backchannel %s", im, isBackChannel);
    if (!msg.getExchange().isOneWay() && !isBackChannel) {

      Endpoint e = msg.getExchange().get(Endpoint.class);

      Message responseMsg = new MessageImpl();
      responseMsg.setExchange(msg.getExchange());
      responseMsg = e.getBinding().createMessage(responseMsg);
      msg.getExchange().setOutMessage(responseMsg);

      MessageFactory mf;
      try {
        mf = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage soapMessage = mf.createMessage();

        responseMsg.setContent(SOAPMessage.class, soapMessage);

        InterceptorChain chainOut
                = OutgoingChainInterceptor.getOutInterceptorChain(msg
                        .getExchange());
        chainOut.add(new CXFOutHttpErrorInterceptor(503));

        LOG.logWarn("got out interceptor:" + chainOut, null);
        responseMsg.setInterceptorChain(chainOut);

        chainOut.doIntercept(responseMsg);

        msg.getInterceptorChain().abort();
        return false;
      } catch (SOAPException ex) {
        LOG.logError(l, ex);
      }
    }

    LOG.logEnd(l);
    return true;
  }

  /**
   *
   * @param t
   */
  @Override
  public void handleFault(SoapMessage t, Properties contextProperties) {
    // ignore
  }

}
