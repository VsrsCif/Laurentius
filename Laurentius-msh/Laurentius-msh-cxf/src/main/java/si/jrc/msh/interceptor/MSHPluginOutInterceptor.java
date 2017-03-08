/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.jrc.msh.interceptor;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import si.laurentius.commons.enums.SEDInterceptorEvent;

import si.laurentius.commons.utils.SEDLogger;

/**
 *
 * @author Jože Rihtaršič
 */
public class MSHPluginOutInterceptor extends MSHPluginInterceptorAbstract {

  /**
   *
   */
  protected final static SEDLogger LOG = new SEDLogger(
          MSHPluginOutInterceptor.class);

  /**
   *
   */
  public MSHPluginOutInterceptor() {
    super(Phase.USER_LOGICAL);
  }

  /**
   *
   * @param msg
   */
  @Override
  public void handleMessage(SoapMessage msg)
          throws Fault {
    long l = LOG.logStart();
    handleInterception(SEDInterceptorEvent.OUT_MESSAGE, msg);
    LOG.logEnd(l);
  }

}
