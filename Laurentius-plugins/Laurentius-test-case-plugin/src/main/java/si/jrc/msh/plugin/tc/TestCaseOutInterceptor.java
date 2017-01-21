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

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.servlet.http.HttpServletResponse;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
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
public class TestCaseOutInterceptor implements SoapInterceptorInterface {

  protected final SEDLogger LOG = new SEDLogger(TestCaseOutInterceptor.class);

  @Override
  public MailInterceptorDef getDefinition() {
    MailInterceptorDef mid = new MailInterceptorDef();
    mid.setDescription("TestCaseOutInterceptor");
    mid.setName("TestCaseOutInterceptor");
    mid.setType("TestCaseOutInterceptor");
    return mid;
  }

  /**
   *
   * @param t
   */
  @Override
  public void handleFault(SoapMessage t) {
    // ignore
  }

  /**
   *
   * @param message
   */
  @Override
  public boolean handleMessage(SoapMessage message) {
    long l = LOG.logStart(message);

    boolean bBackChannel = !SoapUtils.isRequestMessage(message);
    MSHInMail im = SoapUtils.getMSHInMail(message);
    LOG.formatedWarning("got inmail  %s is backchannel %s", im, bBackChannel);
    if (bBackChannel && im != null) {
      String service = im.getService();
      String rb = im.getReceiverEBox();
      String sb = im.getSenderEBox();
      boolean bER = DisableServiceUtils.existsDisableService(service, sb, rb);
      LOG.formatedWarning("exists rule  %s ", bER);
      if (bER) {
        HttpServletResponse response = (HttpServletResponse) message.getExchange()
            .getInMessage().get(AbstractHTTPDestination.HTTP_RESPONSE);
        response.setStatus(503);
        message.getInterceptorChain().abort();
        return false;
      }

    }
    LOG.logEnd(l);
    return true;
  }

}
