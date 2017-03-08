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
package si.jrc.msh.interceptor;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import si.laurentius.commons.cxf.SoapUtils;
import si.laurentius.commons.enums.SEDInterceptorEvent;
import si.laurentius.commons.pmode.EBMSMessageContext;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.msh.inbox.mail.MSHInMail;

/**
 *
 * @author Jože Rihtaršič
 */
public class MSHPluginInFaultInterceptor extends MSHPluginInterceptorAbstract {

  /**
   *
   */
  protected static final SEDLogger LOG = new SEDLogger(MSHPluginInFaultInterceptor.class);

  /**
   *
   */
  public MSHPluginInFaultInterceptor() {
    super(Phase.PRE_INVOKE);
  }

  /**
   *
   * @param msg
   */
  @Override
  public void handleMessage(SoapMessage msg)
      throws Fault {
    long l = LOG.logStart();
    handleInterception(SEDInterceptorEvent.IN_FAULT_MESSAGE, msg);
   
    LOG.logEnd(l);
  }

}
