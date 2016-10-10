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

import javax.servlet.http.HttpServletResponse;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import si.laurentius.commons.utils.SEDLogger;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */

public class CXFOutHttpErrorInterceptor extends AbstractSoapInterceptor {

  protected final SEDLogger LOG = new SEDLogger(CXFOutHttpErrorInterceptor.class);
  int httpErrorCode = 503;

  public CXFOutHttpErrorInterceptor(int hec) {
    super(Phase.PRE_PROTOCOL);
    this.httpErrorCode = hec;
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
  public void handleMessage(SoapMessage message) {
    long l = LOG.logStart(message);

    HttpServletResponse response = (HttpServletResponse) message.getExchange()
        .getInMessage().get(AbstractHTTPDestination.HTTP_RESPONSE);
    response.setStatus(httpErrorCode);
    message.getInterceptorChain().abort();

    LOG.logEnd(l);
  }

}
