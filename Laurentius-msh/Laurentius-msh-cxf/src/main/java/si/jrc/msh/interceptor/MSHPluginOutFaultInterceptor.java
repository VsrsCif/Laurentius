/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.jrc.msh.interceptor;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.commons.cxf.SoapUtils;
import si.laurentius.commons.enums.SEDInterceptorEvent;

import si.laurentius.commons.pmode.EBMSMessageContext;
import si.laurentius.commons.utils.SEDLogger;

/**
 *
 * @author Jože Rihtaršič
 */
public class MSHPluginOutFaultInterceptor extends MSHPluginInterceptorAbstract {

  /**
     *
     */
  protected final static SEDLogger LOG = new SEDLogger(MSHPluginOutFaultInterceptor.class);

  /**
     *
     */
  public MSHPluginOutFaultInterceptor() {
    super(Phase.USER_LOGICAL);
    addAfter(EBMSOutInterceptor.class.getName());
    
  }

  /**
   *
   * @param msg
   */
  @Override
  public void handleMessage(SoapMessage msg) throws Fault {
    long l = LOG.logStart();
    handleInterception(SEDInterceptorEvent.OUT_FAULT_MESSAGE, msg);

    LOG.logEnd(l);
  }

}
