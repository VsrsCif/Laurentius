/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package si.jrc.msh.interceptor;

import java.util.List;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import si.jrc.msh.exception.EBMSError;
import si.jrc.msh.exception.EBMSErrorCode;
import si.laurentius.msh.outbox.mail.MSHOutMail;
import si.laurentius.msh.pmode.PluginType;
import si.laurentius.commons.cxf.SoapUtils;

import si.laurentius.commons.pmode.EBMSMessageContext;
import si.laurentius.commons.utils.SEDLogger;
import si.laurentius.commons.utils.Utils;
import si.laurentius.plugin.interfaces.SoapInterceptorInterface;

/**
 *
 * @author Jože Rihtaršič
 */
public class MSHPluginOutFaultInterceptor extends AbstractSoapInterceptor {

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
     EBMSMessageContext ectx = SoapUtils.getEBMSMessageOutContext(msg);
     MSHOutMail outMail = SoapUtils.getMSHOutMail(msg);

    if (ectx == null) {
      LOG.formatedlog("No EBMSMessageContext context for out mail: '%d'.",outMail!=null? outMail.getId(): -1);
    } else if (ectx.getPMode() != null &&
         ectx.getPMode().getPlugins() != null &&
         ectx.getPMode().getPlugins().getOutFaultPlugins() != null &&
         !ectx.getPMode().getPlugins().getOutFaultPlugins().getPlugins().isEmpty()) {

      List<PluginType> lst = ectx.getPMode().getPlugins().getOutFaultPlugins().getPlugins();
      lst.stream().map((pt) -> pt.getValue()).filter((str) ->
          (!Utils.isEmptyString(str))).forEach((str) -> {
        try {
          SoapInterceptorInterface example = InitialContext.doLookup(str);
          example.handleMessage(msg);
        } catch (NamingException ex) {
          LOG.logError(l, String.format("SoapInterceptorInterface '%s' not found!", str), ex);
        } catch (Throwable ex) {
          String errmsg = String.format(
              "SoapInterceptorInterface '%s' throws an error with message: %s!", str,
              ex.getMessage());
          LOG.logError(l, errmsg, ex);
          throw new EBMSError(EBMSErrorCode.Other, outMail!=null? outMail.getMessageId(): null,
              errmsg, ex, SoapFault.FAULT_CODE_CLIENT);
        }
      });
    }
    LOG.logEnd(l);
  }

}
